/*******************************************************************************
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package li.barter;

import java.io.IOException;
import java.util.Collection;

import li.barter.R;
import li.barter.utils.AppConstants;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.jwetherell.quick_response_code.DecoderActivityHandler;
import com.jwetherell.quick_response_code.IDecoderActivity;
import com.jwetherell.quick_response_code.ViewfinderView;
import com.jwetherell.quick_response_code.camera.CameraManager;
import com.jwetherell.quick_response_code.result.ResultHandler;
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;

public class ScanIsbnActivity extends AbstractBarterLiActivity implements
		IDecoderActivity, SurfaceHolder.Callback {

	private static final String TAG = "AddBookActivity";

	protected DecoderActivityHandler handler = null;
	protected ViewfinderView viewfinderView = null;
	protected CameraManager cameraManager = null;
	protected boolean hasSurface = false;
	protected Collection<BarcodeFormat> decodeFormats = null;
	protected String characterSet = null;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		handler = null;
		hasSurface = false;
		// scanBarCode(viewfinderView);

		// End of setting Listeners

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_scan_book, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startScanningForBarcode();

	}

	@Override
	protected void onPause() {
		super.onPause();
		stopScanningForBarcode();
	}

	/**
	 * Initialize the camera and start scanning for a barcode
	 */
	private void startScanningForBarcode() {
		// CameraManager must be initialized here, not in onCreate().
		if (cameraManager == null)
			cameraManager = new CameraManager(getApplication());

		if (viewfinderView == null) {
			viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
			viewfinderView.setCameraManager(cameraManager);
		}

		showScanner();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

	}

	/**
	 * Stop scanning for a barcode, and release the camera
	 */
	private void stopScanningForBarcode() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}

		cameraManager.closeDriver();

		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_add_manually) {
			startActivity(new Intent(this, AddOrEditBookActivity.class));
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_FOCUS
				|| keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null)
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Ignore
	}

	@Override
	public ViewfinderView getViewfinder() {
		return viewfinderView;
	}

	@Override
	public Handler getHandler() {
		return handler;
	}

	@Override
	public CameraManager getCameraManager() {
		return cameraManager;
	}

	protected void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_image_border));
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2,
					barcode.getHeight() - 2);
			canvas.drawRect(border, paint);

			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	protected static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	protected void showScanner() {
		viewfinderView.setVisibility(View.VISIBLE);
	}

	protected void initCamera(SurfaceHolder surfaceHolder) {
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null)
				handler = new DecoderActivityHandler(this, decodeFormats,
						characterSet, cameraManager);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
		}
	}

	@Override
	public void handleDecode(Result rawResult, Bitmap barcode) {
		stopScanningForBarcode();
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		handleDecodeInternally(rawResult, resultHandler, barcode);
	}

	private void handleDecodeInternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {

		final String isbnNumber = resultHandler.getDisplayContents().toString(); // Barcode
																					// data
		final String barcodeSymbology = rawResult.getBarcodeFormat().toString(); // Barcode
																					// symbology
		final String type = resultHandler.getType().toString(); // Whether
																// barcode is of
																// type URI or
																// ISBN

		if (!AppConstants.ISBN.equals(type)) {
			showToast(R.string.not_a_valid_isbn, true);
		} else {

			final Intent addBookIntent = new Intent(this,
					AddOrEditBookActivity.class);
			addBookIntent.putExtra(AppConstants.BOOK_ID, isbnNumber);
			startActivity(addBookIntent);
		}

	}

}
