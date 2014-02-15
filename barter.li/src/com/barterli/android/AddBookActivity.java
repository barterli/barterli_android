package com.barterli.android;

import com.google.zxing.Result;
import com.jwetherell.quick_response_code.DecoderActivity;
import com.jwetherell.quick_response_code.result.ResultHandler;
import com.jwetherell.quick_response_code.result.ResultHandlerFactory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookActivity extends DecoderActivity {

	private static final int READ_ISBN_SCAN_CODE = 0;
	private ConnectionDetector connection_status_detector;
	private AlertDialogManager alert = new AlertDialogManager();
	private Boolean connectionStatus;
	private int RequestCounter = 0;
	private SharedPreferences mSharedPreferences;
	private String Auth_Token = "";
	private boolean Is_Loc_Set = false;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		connection_status_detector = new ConnectionDetector(
				getApplicationContext());
		connectionStatus = connection_status_detector.isConnectingToInternet();
		
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"BarterLiPref", 0);
		if (mSharedPreferences != null) {
			Auth_Token = mSharedPreferences.getString(
					AllConstants.PREF_BARTER_LI_AUTHO_TOKEN, "");
			Is_Loc_Set = mSharedPreferences.getBoolean(
					AllConstants.IS_PREF_LOCATION_SET, false);
			// Toast.makeText(this,
			// "You are aloready Logged in with Auth_token:" + Auth_Token,
			// Toast.LENGTH_SHORT).show();
		}

		if (TextUtils.isEmpty(Auth_Token) || !Is_Loc_Set) {
			Toast.makeText(
					this,
					"You havent yet made account and/or not yet set preferred location.\nPlease complete all steps!",
					Toast.LENGTH_SHORT).show();
		}
		//scanBarCode(viewfinderView);

		// End of setting Listeners

	}
	
	@Override
    public void handleDecode(Result rawResult, Bitmap barcode) {
        //drawResultPoints(barcode, rawResult);

        ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
        handleDecodeInternally(rawResult, resultHandler, barcode);
    }
	
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
        onPause();
        //Toast.makeText(this, resultHandler.getDisplayContents().toString(), Toast.LENGTH_SHORT).show();
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
					if (!connectionStatus) {
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
			if (!connectionStatus) {
				alert.showAlertDialog(AddBookActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				return;
			}
			/*if (s.length() == 0 && before == 1) {
				scanButton.setVisibility(View.VISIBLE);
				orLable1.setVisibility(View.VISIBLE);
				orLable2.setVisibility(View.VISIBLE);
				manualButton.setVisibility(View.VISIBLE);
			}
			if (s.length() < 5) {
				listView.setAdapter(null);
			}*/
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
			if (!connection_status_detector.isConnectingToInternet()) {
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
			/*adapter = new ArrayAdapter<String>(AddBookActivity.this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					book_suggestions);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					Intent editBookIntent = new Intent(AddBookActivity.this,
							EditBookDetailsActivity.class);
					editBookIntent
							.putExtra("TITLE", book_suggestions[position]);
					startActivity(editBookIntent);
				}
			});*/
		}
	} // End of askServerForSuggestionsTask

}
