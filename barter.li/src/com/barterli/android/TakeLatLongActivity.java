package com.barterli.android;

import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.barterli.android.utils.PreferenceKeys;
import com.barterli.android.utils.SharedPreferenceHelper;

public class TakeLatLongActivity extends AbstractBarterLiActivity {
	private GPSTracker gps;
	private ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
	private ListView listView;
	private ArrayAdapter<String> adapter;
	// private TextView latLongText;
	RequestQueue queue;
	String place_suggestion_url;
	String post_to_my_preferred_loc;
	private String Auth_Token = "";
	private String FB_Email = "";
	private boolean is_loc_pref_set;
	private String my_Pref_loc;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.take_lat_long);
		listView = (ListView) findViewById(R.id.list_data);
		queue = Volley.newRequestQueue(this);
		place_suggestion_url = getResources().getString(
				R.string.hangouts_location);
		post_to_my_preferred_loc = getResources().getString(
				R.string.preferred_location);

		Auth_Token = SharedPreferenceHelper.getString(this,
				PreferenceKeys.BARTER_LI_AUTH_TOKEN);
		FB_Email = SharedPreferenceHelper.getString(this,
				PreferenceKeys.FB_USER_EMAIL);
		final String prefferedLocation = SharedPreferenceHelper.getString(this, PreferenceKeys.MY_PREFERRED_LOCATION);
		is_loc_pref_set = !TextUtils.isEmpty(prefferedLocation);

		gps = new GPSTracker(TakeLatLongActivity.this);
		if (gps.canGetLocation()) {
			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();
			new askServerForSuggestedPlacesTask().execute(place_suggestion_url,
					Double.toString(latitude), Double.toString(longitude));
		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}
	}

	private class savePreferredTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			myProgressDialogManager.showProgresDialog(TakeLatLongActivity.this,
					"Setting Prefered Location!");
		}

		protected String doInBackground(String... params) {
			HTTPHelper myHTTPHelper = new HTTPHelper();
			return myHTTPHelper.postPreferredLocation(params);
		}

		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			SharedPreferenceHelper.set(TakeLatLongActivity.this,
					PreferenceKeys.MY_PREFERRED_LOCATION, my_Pref_loc);

			showToast(R.string.preferred_location, false);
			// Intent profileIntent = new Intent(TakeLatLongActivity.this,
			// MyProfileActivity.class);
			// startActivity(profileIntent);
		}
	}

	private class askServerForSuggestedPlacesTask extends
			AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			myProgressDialogManager.showProgresDialog(TakeLatLongActivity.this,
					"Retrieving...");
		}

		protected String doInBackground(String... params) {
			String place_suggestion_url = params[0];
			String latitude = params[1];
			String longitude = params[2];
			place_suggestion_url += "?latitude=" + latitude;
			place_suggestion_url += "&longitude=" + longitude;
			// USe HTTP Helper
			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "[]";
			responseString = myHTTPHelper.getHelper(place_suggestion_url);
			return responseString;

		}

		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			// Toast.makeText(TakeLatLongActivity.this, result,
			// Toast.LENGTH_SHORT).show();
			final String[] place_suggestions = new JSONHelper()
					.JsonStringofArraysToArray(result);
			adapter = new ArrayAdapter<String>(TakeLatLongActivity.this,
					android.R.layout.simple_list_item_1, android.R.id.text1,
					place_suggestions);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long id) {
					// Log.v("DETAILS1", place_suggestions[position]);
					Address my_address = gps
							.getMyLocationAddress(place_suggestions[position]);
					if (my_address == null) {
						Toast.makeText(getApplicationContext(),
								"Not able to reverse geocode!",
								Toast.LENGTH_SHORT).show();
						return;
					}
					// Log.v("DETAILS2", my_address.toString());
					double _lat = my_address.getLatitude();
					double _long = my_address.getLongitude();
					String _country = my_address.getCountryName();
					String _city = my_address.getSubAdminArea();
					my_Pref_loc = place_suggestions[position];
					new savePreferredTask().execute(post_to_my_preferred_loc,
							Double.toString(_lat), Double.toString(_long),
							_city, _country, place_suggestions[position],
							Auth_Token, FB_Email);
				}
			});
		}
	} // End of askServerForSuggestedPlacesTask

}
