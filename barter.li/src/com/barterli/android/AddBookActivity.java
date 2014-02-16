package com.barterli.android;

import java.io.IOException;
import java.util.Collection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.barterli.android.utils.PreferenceKeys;
import com.barterli.android.utils.SharedPreferenceHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.jwetherell.quick_response_code.DecoderActivityHandler;
import com.jwetherell.quick_response_code.IDecoderActivity;
import com.jwetherell.quick_response_code.ViewfinderView;
import com.jwetherell.quick_response_code.camera.CameraManager;
import com.jwetherell.quick_response_code.result.ResultHandler;
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;

public class AddBookActivity extends AbstractBarterLiActivity implements
		IDecoderActivity, SurfaceHolder.Callback {

	private static final String TAG = "AddBookActivity";

	private static final int READ_ISBN_SCAN_CODE = 0;
	private AlertDialogManager alert = new AlertDialogManager();
	private int RequestCounter = 0;
	private String Auth_Token = "";
	private boolean Is_Loc_Set = false;

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

		Auth_Token = SharedPreferenceHelper.getString(this,
				PreferenceKeys.PREF_BARTER_LI_AUTHO_TOKEN);
		Is_Loc_Set = SharedPreferenceHelper.getBoolean(this,
				PreferenceKeys.IS_PREF_LOCATION_SET);

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
		Log.v(TAG, "onResume()");

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
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "onPause()");

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
			startActivity(new Intent(this, EditBookDetailsActivity.class));
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
		// drawResultPoints(barcode, rawResult);

		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		handleDecodeInternally(rawResult, resultHandler, barcode);
	}

	private void handleDecodeInternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {
		onPause();
		// Toast.makeText(this, resultHandler.getDisplayContents().toString(),
		// Toast.LENGTH_SHORT).show();
		Log.v("RAWRESULT", resultHandler.getDisplayContents().toString());
		Log.v("FORMAT", rawResult.getBarcodeFormat().toString());
		Log.v("TYPE", resultHandler.getType().toString());

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == READ_ISBN_SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				String _isbn = data.getStringExtra("ISBN");
				String _type = data.getStringExtra("TYPE");
				if (_type.contentEquals("ISBN")) {
					if (!isConnectedToInternet()) {
						alert.showAlertDialog(
								AddBookActivity.this,
								"Internet Connection Error",
								"Please connect to working Internet connection",
								false);
						return;
					}
					new askServerForSuggestionsTask().execute(_isbn);
				} else {
					Toast.makeText(
							AddBookActivity.this,
							"The content " + _isbn + " is not an ISBN " + _type,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	} // End of onActivityResult

	public void addNewBook(View v) {
		Intent editBookIntent = new Intent(AddBookActivity.this,
				EditBookDetailsActivity.class);
		startActivity(editBookIntent);
	}

	private final TextWatcher mTextEditorWatcher = new TextWatcher() {
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (!isConnectedToInternet()) {
				alert.showAlertDialog(AddBookActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				return;
			}
			/*
			 * if (s.length() == 0 && before == 1) {
			 * scanButton.setVisibility(View.VISIBLE);
			 * orLable1.setVisibility(View.VISIBLE);
			 * orLable2.setVisibility(View.VISIBLE);
			 * manualButton.setVisibility(View.VISIBLE); } if (s.length() < 5) {
			 * listView.setAdapter(null); }
			 */
			if ((s.length() == 10) || (s.length() >= 5)
					&& (s.length() % 2 != 0)) {
				new askServerForSuggestionsTask().execute(s.toString());
				RequestCounter += 1;
			}
		}
	}; // End of mTextEditorWatcher

	private class askServerForSuggestionsTask extends
			AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			if (!isConnectedToInternet()) {
				alert.showAlertDialog(AddBookActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				return;
			}
		}

		protected String doInBackground(String... parameters) {
			String suggestion_url = getResources().getString(
					R.string.suggestion_url);
			suggestion_url += "?q=" + parameters[0];
			suggestion_url += "&t=" + "title";

			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "[]";
			responseString = myHTTPHelper.getHelper(suggestion_url);
			return responseString;
		}

		protected void onPostExecute(String result) {
			RequestCounter -= 1;
			if (RequestCounter != 0) {
				return;
			}
			final String[] book_suggestions = new JSONHelper()
					.JsonStringofArraysToArray(result);
			/*
			 * adapter = new ArrayAdapter<String>(AddBookActivity.this,
			 * android.R.layout.simple_list_item_1, android.R.id.text1,
			 * book_suggestions); listView.setAdapter(adapter);
			 * listView.setOnItemClickListener(new OnItemClickListener() {
			 * public void onItemClick(AdapterView<?> arg0, View arg1, int
			 * position, long id) { Intent editBookIntent = new
			 * Intent(AddBookActivity.this, EditBookDetailsActivity.class);
			 * editBookIntent .putExtra("TITLE", book_suggestions[position]);
			 * startActivity(editBookIntent); } });
			 */
		}
	} // End of askServerForSuggestionsTask

}
