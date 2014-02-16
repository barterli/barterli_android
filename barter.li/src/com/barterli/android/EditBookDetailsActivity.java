package com.barterli.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.barterli.android.utils.AppConstants;
import com.barterli.android.utils.PreferenceKeys;
import com.barterli.android.utils.SharedPreferenceHelper;

public class EditBookDetailsActivity extends AbstractBarterLiActivity {
	private Button openLeftPanelButton;
	private EditText titleText;
	private EditText authorText;
	private EditText descriptionText;
	private EditText publicationYearText;
	private ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
	private String Auth_Token = "";
	private String FB_Email = "";
	private boolean Is_Loc_Set = false;
	private String BOOK_ID = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_book);
		titleText = (EditText) findViewById(R.id.edit_text_title);
		authorText = (EditText) findViewById(R.id.edit_text_author);
		descriptionText = (EditText) findViewById(R.id.edit_text_description);
		publicationYearText = (EditText) findViewById(R.id.edit_text_publication_year);
		Auth_Token = SharedPreferenceHelper.getString(this,
				PreferenceKeys.BARTER_LI_AUTH_TOKEN);
		FB_Email = SharedPreferenceHelper.getString(this,
				PreferenceKeys.FB_USER_EMAIL);
		final String prefferedLocation = SharedPreferenceHelper.getString(this, PreferenceKeys.MY_PREFERRED_LOCATION);
		Is_Loc_Set = !TextUtils.isEmpty(prefferedLocation);
		// Toast.makeText(this, "You are aloready Logged in with Auth_token:" +
		// Auth_Token, Toast.LENGTH_SHORT).show();

		Intent _i = getIntent();
		if (_i.hasExtra("BOOK_ID")) {
			BOOK_ID = _i.getExtras().getString("BOOK_ID").toString();
			Toast.makeText(this, "ID Received: " + BOOK_ID, Toast.LENGTH_SHORT)
					.show();
		}
		if (_i.hasExtra("TITLE")) {
			titleText.setText(_i.getExtras().getString("TITLE").toString());
		}
		if (_i.hasExtra("TITLE") && !_i.hasExtra("BOOK_ID")) {
			new getBookInfoFromServerTask().execute(_i.getExtras()
					.getString("TITLE").toString());
		}
		if (_i.hasExtra("AUTHOR")) {
			authorText.setText(_i.getExtras().getString("AUTHOR").toString());
		}
		if (_i.hasExtra("DESCRIPTION")) {
			descriptionText.setText(_i.getExtras().getString("DESCRIPTION")
					.toString());
		}
		if (_i.hasExtra("PUBLICATION_YEAR")) {
			publicationYearText.setText(_i.getExtras()
					.getString("PUBLICATION_YEAR").toString());
		}
		if (_i.hasExtra("BARTER_TYPE")) {
			/*barterChoiceGroup.setText(_i.getExtras().getString("BARTER_TYPE")
					.toString());
			chosenBarterOption = _i.getExtras().getString("BARTER_TYPE")
					.toString();*/
		}

		/*// Set Listeners
		barterChoiceGroup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(EditBookDetailsActivity.this)
						.setTitle("Select Valid Option")
						.setAdapter(spinnerArrayAdapter,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										chosenBarterOption = barterOptions[which];
										barterChoiceGroup
												.setText(chosenBarterOption);
										dialog.dismiss();
									}
								}).create().show();
			}
		});*/

	} // End of oncreate

	public void addBook(View v) {
		if (TextUtils.isEmpty(Auth_Token) || !Is_Loc_Set) {
			Toast.makeText(
					this,
					"You havent yet made account and/or not yet set preferred location.\nPlease complete all steps!",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String _title = titleText.getText().toString();
		if (TextUtils.isEmpty(_title)) {
			Toast.makeText(EditBookDetailsActivity.this, "Please Enter Title",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String _author = authorText.getText().toString();
		if (TextUtils.isEmpty(_author)) {
			Toast.makeText(EditBookDetailsActivity.this,
					"Please Enter Author Name", Toast.LENGTH_SHORT).show();
			return;
		}
		String _description = descriptionText.getText().toString();
		String _publication_year = publicationYearText.getText().toString();
		/*if (TextUtils.isEmpty(chosenBarterOption)) {
			Toast.makeText(EditBookDetailsActivity.this,
					"Please enter what you want to do with the book!",
					Toast.LENGTH_SHORT).show();
			return;
		}*/

		//TODO Decide chosen option
		/*new saveMyBookToServerTask().execute(_title, _author, _description,
				_publication_year, chosenBarterOption, Auth_Token, FB_Email);*/
	} // End of addBook

	// Synctask helper to post book to server

	private class saveMyBookToServerTask extends
			AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			super.onPreExecute();
			myProgressDialogManager.showProgresDialog(
					EditBookDetailsActivity.this, "Saving...");
		}

		protected String doInBackground(String... parameters) {
			/*String post_to_mybooks_url = getResources().getString(
					R.string.post_to_mybooks_url);
			HTTPHelper myHTTPHelper = new HTTPHelper();*/
			String responseString = "";
			String _title = parameters[0];
			String _author = parameters[1];
			String _description = parameters[2];
			String _publication_year = parameters[3];
			String _barter_type = parameters[4];
			String _user_token = parameters[5];
			String _fb_Email = parameters[6];
			/*if (TextUtils.isEmpty(BOOK_ID)) {
				responseString = myHTTPHelper
						.postBookToMyList(post_to_mybooks_url, _title, _author,
								_description, _publication_year, _barter_type,
								_user_token, _fb_Email);
			} else {
				String put_to_mybooks_url = post_to_mybooks_url + BOOK_ID;
				responseString = myHTTPHelper
						.putBookToMyList(put_to_mybooks_url, _title, _author,
								_description, _publication_year, _barter_type,
								_user_token, _fb_Email);
			}*/

			return responseString;
		}

		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			// ** STILL TO DO:- Verify that result is success **//
			Toast.makeText(EditBookDetailsActivity.this,
					"Successfully Added!. Will see where to go now!",
					Toast.LENGTH_SHORT).show();
			resetViews();
		}
	} // End of askServerForSuggestionsTask

	// Synctask helper to get book suggestion

	private class getBookInfoFromServerTask extends
			AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			myProgressDialogManager.showProgresDialog(
					EditBookDetailsActivity.this, "Retrieving...");
		}

		protected String doInBackground(String... parameters) {
			/*String suggestion_url = getResources().getString(
					R.string.book_info_url);
			suggestion_url += "?q=" + parameters[0];
			HTTPHelper myHTTPHelper = new HTTPHelper();*/
			String responseString = "";
			//responseString = myHTTPHelper.getHelper(suggestion_url);
			return responseString;
		}

		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			//barterChoiceGroup.performClick();
			try {
				JSONObject bookObject = new JSONObject(result);
				if (bookObject.has(AppConstants.DESCRIPTION_KEY)) {
					descriptionText.setText(bookObject
							.getString(AppConstants.DESCRIPTION_KEY));
				}
				if (bookObject.has(AppConstants.PUBLICATION_YEAR_KEY)) {
					publicationYearText.setText(bookObject
							.getString(AppConstants.PUBLICATION_YEAR_KEY));
				}
				if (bookObject.has(AppConstants.PUBLICATION_AUTHORS)) {
					JSONObject authorsObject = bookObject
							.getJSONObject(AppConstants.PUBLICATION_AUTHORS);
					if (authorsObject.has(AppConstants.PUBLICATION_AUTHOR)) {

						if (authorsObject.get(AppConstants.PUBLICATION_AUTHOR) instanceof JSONObject) {
							JSONObject singleAuthorObject = authorsObject
									.getJSONObject(AppConstants.PUBLICATION_AUTHOR);
							authorText
									.setText(singleAuthorObject
											.getString(AppConstants.PUBLICATION_AUTHOR_NAME));
						} else if (authorsObject
								.get(AppConstants.PUBLICATION_AUTHOR) instanceof JSONArray) {
							JSONArray authorArray = authorsObject
									.getJSONArray(AppConstants.PUBLICATION_AUTHOR);
							JSONObject firstAuthorObject = authorArray
									.getJSONObject(0);
							authorText
									.setText(firstAuthorObject
											.getString(AppConstants.PUBLICATION_AUTHOR_NAME));
						}
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	} // End of getBookInfoFromServerTask

	public void resetViews() {
		titleText.setText("");
		authorText.setText("");
		descriptionText.setText("");
		publicationYearText.setText("");
		/*barterChoiceGroup.setText(R.string.barter_type_label);
		chosenBarterOption = "";*/
	} // End of resetViews

}
