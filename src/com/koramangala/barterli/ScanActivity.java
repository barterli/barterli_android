package com.koramangala.barterli;

import java.text.DateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.jwetherell.quick_response_code.DecoderActivity;
import com.jwetherell.quick_response_code.R;
import com.jwetherell.quick_response_code.result.ResultHandler;
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;

public class ScanActivity extends DecoderActivity{

    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER, ResultMetadataType.SUGGESTED_PRICE,
            ResultMetadataType.ERROR_CORRECTION_LEVEL, ResultMetadataType.POSSIBLE_COUNTRY);

    private TextView statusView = null;
    private View resultView = null;
    private boolean inScanMode = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.capture);
        Log.v(TAG, "onCreate()");

        resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);

        inScanMode = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (inScanMode)
                finish();
            else
                onResume();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode) {
        //drawResultPoints(barcode, rawResult);

        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        handleDecodeInternally(rawResult, resultHandler, barcode);
    }

    protected void showScanner() {
        inScanMode = true;
        resultView.setVisibility(View.GONE);
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
    }

    protected void showResults() {
        inScanMode = false;
        statusView.setVisibility(View.GONE);
        viewfinderView.setVisibility(View.GONE);
        resultView.setVisibility(View.VISIBLE);
    }

    // Put up our own UI for how to handle the decodBarcodeFormated contents.
    private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        onPause();
        //Toast.makeText(this, resultHandler.getDisplayContents().toString(), Toast.LENGTH_SHORT).show();
        Log.v("RAWRESULT", resultHandler.getDisplayContents().toString());
        Log.v("FORMAT", rawResult.getBarcodeFormat().toString());
        Log.v("TYPE", resultHandler.getType().toString());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("ISBN",resultHandler.getDisplayContents().toString());
        returnIntent.putExtra("TYPE",resultHandler.getType().toString());
        setResult(RESULT_OK,returnIntent);     
        finish();
    }
}
