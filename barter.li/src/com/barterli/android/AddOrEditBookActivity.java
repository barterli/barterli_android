package com.barterli.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.barterli.android.http.HttpConstants;
import com.barterli.android.http.JsonUtils;
import com.barterli.android.http.HttpConstants.ApiEndpoints;
import com.barterli.android.utils.AppConstants;

public class AddOrEditBookActivity extends AbstractBarterLiActivity implements
		OnClickListener, Listener<JSONObject>, ErrorListener {

	private static final String TAG = "AddOrEditBookActivity";

	private EditText mIsbnEditText;
	private EditText mTitleEditText;
	private EditText mAuthorEditText;
	private EditText mDescriptionEditText;
	private EditText mPublicationYearEditText;
	private String mBookId;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_book);
		mIsbnEditText = (EditText) findViewById(R.id.edit_text_isbn);
		mTitleEditText = (EditText) findViewById(R.id.edit_text_title);
		mAuthorEditText = (EditText) findViewById(R.id.edit_text_author);
		mDescriptionEditText = (EditText) findViewById(R.id.edit_text_description);
		mPublicationYearEditText = (EditText) findViewById(R.id.edit_text_publication_year);

		findViewById(R.id.button_submit).setOnClickListener(this);

		loadDetailsForIntent(getIntent());

		/*
		 * // Set Listeners barterChoiceGroup.setOnClickListener(new
		 * OnClickListener() { public void onClick(View v) { new
		 * AlertDialog.Builder(EditBookDetailsActivity.this)
		 * .setTitle("Select Valid Option") .setAdapter(spinnerArrayAdapter, new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int which) { chosenBarterOption =
		 * barterOptions[which]; barterChoiceGroup .setText(chosenBarterOption);
		 * dialog.dismiss(); } }).create().show(); } });
		 */

	} // End of oncreate

	/**
	 * @param launchIntent
	 *            The intent which launched the screen
	 */
	private void loadDetailsForIntent(Intent launchIntent) {

		final Bundle extras = launchIntent.getExtras();

		// If extras are null, it means that user has to decided to add the book
		// completely manually
		if (extras != null) {

			mBookId = extras.getString(AppConstants.BOOK_ID);
			final String title = extras.getString(AppConstants.BOOK_TITLE);
			final String author = extras.getString(AppConstants.AUTHOR);
			final String description = extras
					.getString(AppConstants.DESCRIPTION);
			final String publicationYear = extras
					.getString(AppConstants.PUBLICATION_YEAR);
			final String[] barterTypes = extras
					.getStringArray(AppConstants.BARTER_TYPES);

			mIsbnEditText.setText(mBookId);
			mTitleEditText.setText(title);
			mAuthorEditText.setText(author);
			mDescriptionEditText.setText(description);
			mPublicationYearEditText.setText(publicationYear);

			setCheckBoxesForBarterTypes(barterTypes);

			if (mBookId != null && TextUtils.isEmpty(title)) {

				// User has come here after scanning a book, fetch the book
				// details from server TODO: Fetch book details
				getBookInfoFromServer();
			}
		}

	}

	/**
	 * Fetches the book info from server based on the ISBN number
	 */
	private void getBookInfoFromServer() {

		JsonObjectRequest request = new JsonObjectRequest(
				HttpConstants.getApiBaseUrl() + ApiEndpoints.BOOK_INFO, null,
				this, this);
		Map<String, String> params = new HashMap<String, String>();
		params.put(HttpConstants.Q, mBookId);
		request.setParams(params);
		addRequestToQueue(request, true, R.string.unable_to_fetch_book_info);
	}

	/**
	 * Updates the barter types checkboxes
	 * 
	 * @param barterTypes
	 *            An array of barter types chosen for the book
	 */
	private void setCheckBoxesForBarterTypes(String[] barterTypes) {
		if (barterTypes != null && barterTypes.length >= 1) {
			// TODO Set barter types
		}
	}

	/**
	 * Add the book to the server
	 */
	private void addBookToServer() {

		String _title = mTitleEditText.getText().toString();
		if (TextUtils.isEmpty(_title)) {
			Toast.makeText(AddOrEditBookActivity.this, "Please Enter Title",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String _author = mAuthorEditText.getText().toString();
		if (TextUtils.isEmpty(_author)) {
			Toast.makeText(AddOrEditBookActivity.this,
					"Please Enter Author Name", Toast.LENGTH_SHORT).show();
			return;
		}
		String _description = mDescriptionEditText.getText().toString();
		String _publication_year = mPublicationYearEditText.getText()
				.toString();
		/*
		 * if (TextUtils.isEmpty(chosenBarterOption)) {
		 * Toast.makeText(EditBookDetailsActivity.this,
		 * "Please enter what you want to do with the book!",
		 * Toast.LENGTH_SHORT).show(); return; }
		 */

		// TODO Decide chosen option
		/*
		 * new saveMyBookToServerTask().execute(_title, _author, _description,
		 * _publication_year, chosenBarterOption, Auth_Token, FB_Email);
		 */
	} // End of addBook

	// Synctask helper to post book to server

	private class saveMyBookToServerTask extends
			AsyncTask<String, Void, String> {

		protected String doInBackground(String... parameters) {
			/*
			 * String post_to_mybooks_url = getResources().getString(
			 * R.string.post_to_mybooks_url); HTTPHelper myHTTPHelper = new
			 * HTTPHelper();
			 */
			String responseString = "";
			String _title = parameters[0];
			String _author = parameters[1];
			String _description = parameters[2];
			String _publication_year = parameters[3];
			String _barter_type = parameters[4];
			String _user_token = parameters[5];
			String _fb_Email = parameters[6];
			/*
			 * if (TextUtils.isEmpty(BOOK_ID)) { responseString = myHTTPHelper
			 * .postBookToMyList(post_to_mybooks_url, _title, _author,
			 * _description, _publication_year, _barter_type, _user_token,
			 * _fb_Email); } else { String put_to_mybooks_url =
			 * post_to_mybooks_url + BOOK_ID; responseString = myHTTPHelper
			 * .putBookToMyList(put_to_mybooks_url, _title, _author,
			 * _description, _publication_year, _barter_type, _user_token,
			 * _fb_Email); }
			 */

			return responseString;
		}

		protected void onPostExecute(String result) {
			showToast(R.string.book_added, false);
		}
	}

	private class getBookInfoFromServerTask extends
			AsyncTask<String, Void, String> {

		protected String doInBackground(String... parameters) {
			/*
			 * String suggestion_url = getResources().getString(
			 * R.string.book_info_url); suggestion_url += "?q=" + parameters[0];
			 * HTTPHelper myHTTPHelper = new HTTPHelper();
			 */
			String responseString = "";
			// responseString = myHTTPHelper.getHelper(suggestion_url);
			return responseString;
		}

		protected void onPostExecute(String result) {
			// barterChoiceGroup.performClick();
			try {
				JSONObject bookObject = new JSONObject(result);
				if (bookObject.has(AppConstants.DESCRIPTION_KEY)) {
					mDescriptionEditText.setText(bookObject
							.getString(AppConstants.DESCRIPTION_KEY));
				}
				if (bookObject.has(AppConstants.PUBLICATION_YEAR_KEY)) {
					mPublicationYearEditText.setText(bookObject
							.getString(AppConstants.PUBLICATION_YEAR_KEY));
				}
				if (bookObject.has(AppConstants.PUBLICATION_AUTHORS)) {
					JSONObject authorsObject = bookObject
							.getJSONObject(AppConstants.PUBLICATION_AUTHORS);
					if (authorsObject.has(AppConstants.PUBLICATION_AUTHOR)) {

						if (authorsObject.get(AppConstants.PUBLICATION_AUTHOR) instanceof JSONObject) {
							JSONObject singleAuthorObject = authorsObject
									.getJSONObject(AppConstants.PUBLICATION_AUTHOR);
							mAuthorEditText
									.setText(singleAuthorObject
											.getString(AppConstants.PUBLICATION_AUTHOR_NAME));
						} else if (authorsObject
								.get(AppConstants.PUBLICATION_AUTHOR) instanceof JSONArray) {
							JSONArray authorArray = authorsObject
									.getJSONArray(AppConstants.PUBLICATION_AUTHOR);
							JSONObject firstAuthorObject = authorArray
									.getJSONObject(0);
							mAuthorEditText
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

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.button_submit && isInputValid()) {
			showToast("Feature under implementation", true);
			// addBookToServer();
		}
	}

	/**
	 * Validates the current input. Sets errors for the text fields if there are
	 * any errors
	 * 
	 * @return <code>true</code> if there are no errors, <code>false</code>
	 *         otherwise
	 */
	private boolean isInputValid() {

		// TODO Add input validation
		return true;
	}

	@Override
	public void onErrorResponse(VolleyError error) {

		onRequestFinished();
		showToast(R.string.unable_to_fetch_book_info, false);
	}

	@Override
	public void onResponse(JSONObject response) {

		onRequestFinished();
		readBookDetailsFromResponse(response);
	}

	/**
	 * Reads the book details from the response and populates the text fields
	 * 
	 * @param response
	 *            The {@link JSONObject} which contains the response
	 */
	private void readBookDetailsFromResponse(JSONObject response) {

		final String bookTitle = JsonUtils.getStringValue(response,
				HttpConstants.TITLE);
		final String bookDescription = JsonUtils.getStringValue(response,
				HttpConstants.DESCRIPTION);

		JSONObject authorObject = JsonUtils.getJsonObject(response,
				HttpConstants.AUTHORS);
		String authorName = null;
		if (authorObject != null) {
			authorObject = JsonUtils.getJsonObject(authorObject,
					HttpConstants.AUTHOR);
			if (authorObject != null) {
				authorName = JsonUtils.getStringValue(authorObject,
						HttpConstants.NAME);
			}
		}

		final String publicationYear = JsonUtils.getStringValue(response,
				HttpConstants.PUBLICATION_YEAR);

		mTitleEditText.setText(bookTitle);
		mDescriptionEditText.setText(bookDescription);
		mAuthorEditText.setText(authorName);
		mPublicationYearEditText.setText(publicationYear);

	}

}
