package com.barterli.android;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.barterli.android.utils.PreferenceKeys;
import com.barterli.android.utils.SharedPreferenceHelper;

public class MyProfileActivity extends AbstractBarterLiActivity {

	private String my_email;
	private String my_name;
	private String my_pref_location;

	private TextView my_name_text;
	private TextView my_email_text;
	private TextView my_pref_location_text;
	private ListView listView;
	private AlertDialogManager alert = new AlertDialogManager();
	private Boolean connectionStatus;
	private String Auth_Token;
	private String get_profile_url;
	private ArrayAdapter adapter;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.show_my_profile);

		my_email = SharedPreferenceHelper.getString(this,
				PreferenceKeys.FB_USER_EMAIL);
		my_name = SharedPreferenceHelper
				.getString(this, PreferenceKeys.FB_USERNAME);
		my_pref_location = SharedPreferenceHelper.getString(this,
				PreferenceKeys.MY_PREF_LOCATION);
		Auth_Token = SharedPreferenceHelper.getString(this,
				PreferenceKeys.PREF_BARTER_LI_AUTHO_TOKEN);
		my_name_text = (TextView) findViewById(R.id.my_name);
		my_email_text = (TextView) findViewById(R.id.my_email);
		my_pref_location_text = (TextView) findViewById(R.id.my_pref_loc);
		my_name_text.setText(my_name);
		my_email_text.setText(my_email);
		my_pref_location_text.setText(my_pref_location);
		get_profile_url = getResources().getString(R.string.preferred_location);
		listView = (ListView) findViewById(R.id.list_my_books);
		new askServerForMyDetails().execute(get_profile_url, my_email,
				Auth_Token);
	}

	private class askServerForMyDetails extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			if (!isConnectedToInternet()) {
				alert.showAlertDialog(MyProfileActivity.this,
						"Internet Connection Error",
						"Please connect to working Internet connection", false);
				return;
			}
		}

		protected String doInBackground(String... parameters) {
			String local_profile_url = parameters[0];
			local_profile_url += "?user_email=" + parameters[1];
			local_profile_url += "&user_token=" + parameters[2];

			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "[]";
			responseString = myHTTPHelper.getHelper(local_profile_url);
			return responseString;
		}

		protected void onPostExecute(String result) {
			// Log.v("PROFILE_INFO", result);
			final String[] my_books = new JSONHelper()
					.getBookNamesFromUserProfile(result);
			final JSONArray my_book_objects = new JSONHelper()
					.getBookObjectsFromUserProfile(result);
			if (my_books.length == 0) {
				Toast.makeText(MyProfileActivity.this, "No Books!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			adapter = new ArrayAdapter<String>(MyProfileActivity.this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					my_books);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					// Toast.makeText(MyProfileActivity.this,
					// "Edit will be built soon!", Toast.LENGTH_SHORT).show();
					Intent editBookIntent = new Intent(MyProfileActivity.this,
							EditBookDetailsActivity.class);
					editBookIntent.putExtra("TITLE", my_books[position]);
					try {
						editBookIntent.putExtra("BOOK_ID", my_book_objects
								.getJSONObject(position).optString("id"));
						editBookIntent.putExtra("AUTHOR", my_book_objects
								.getJSONObject(position).optString("author"));
						editBookIntent.putExtra("DESCRIPTION",
								my_book_objects.getJSONObject(position)
										.optString("description"));
						editBookIntent.putExtra("PUBLICATION_YEAR",
								my_book_objects.getJSONObject(position)
										.optString("publication_year"));
						editBookIntent.putExtra("BARTER_TYPE",
								my_book_objects.getJSONObject(position)
										.optString("barter_type"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					startActivity(editBookIntent);
				}
			});
		}
	} // End of askServerForMyDetails

}
