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

package li.barter.activities;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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

import li.barter.GPSTracker;
import li.barter.JSONHelper;
import li.barter.ProgressDialogManager;
import li.barter.R;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.SharedPreferenceHelper;

public class TakeLatLongActivity extends AbstractBarterLiActivity {

    private static final String         TAG                     = "TakeLatLongActivity";

    private GPSTracker                  gps;
    private final ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
    private ListView                    listView;
    private ArrayAdapter<String>        adapter;
    // private TextView latLongText;
    RequestQueue                        queue;
    String                              place_suggestion_url;
    String                              post_to_my_preferred_loc;
    private String                      Auth_Token              = "";
    private String                      FB_Email                = "";
    private boolean                     is_loc_pref_set;
    private String                      my_Pref_loc;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_lat_long);
        listView = (ListView) findViewById(R.id.list_data);
        queue = Volley.newRequestQueue(this);
        /*
         * place_suggestion_url = getResources().getString(
         * R.string.hangouts_location); post_to_my_preferred_loc =
         * getResources().getString( R.string.preferred_location);
         */

        Auth_Token = UserInfo.INSTANCE.authToken;
        FB_Email = SharedPreferenceHelper.getString(this, R.string.pref_email);
        final String prefferedLocation = SharedPreferenceHelper
                        .getString(this, R.string.pref_preferred_location);
        is_loc_pref_set = !TextUtils.isEmpty(prefferedLocation);

        gps = new GPSTracker(TakeLatLongActivity.this);
        if (gps.canGetLocation()) {
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            new askServerForSuggestedPlacesTask()
                            .execute(place_suggestion_url, Double
                                            .toString(latitude), Double
                                            .toString(longitude));
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    private class savePreferredTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            myProgressDialogManager
                            .showProgresDialog(TakeLatLongActivity.this, "Setting Prefered Location!");
        }

        @Override
        protected String doInBackground(final String... params) {
            /* HTTPHelper myHTTPHelper = new HTTPHelper(); */
            return ""/* myHTTPHelper.postPreferredLocation(params) */;
        }

        @Override
        protected void onPostExecute(final String result) {
            myProgressDialogManager.dismissProgresDialog();
            SharedPreferenceHelper
                            .set(TakeLatLongActivity.this, R.string.pref_preferred_location, my_Pref_loc);

            /* showToast(R.string.preferred_location, false); */
            // Intent profileIntent = new Intent(TakeLatLongActivity.this,
            // MyProfileActivity.class);
            // startActivity(profileIntent);
        }
    }

    private class askServerForSuggestedPlacesTask extends
                    AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            myProgressDialogManager
                            .showProgresDialog(TakeLatLongActivity.this, "Retrieving...");
        }

        @Override
        protected String doInBackground(final String... params) {
            String place_suggestion_url = params[0];
            final String latitude = params[1];
            final String longitude = params[2];
            place_suggestion_url += "?latitude=" + latitude;
            place_suggestion_url += "&longitude=" + longitude;
            // USe HTTP Helper
            // HTTPHelper myHTTPHelper = new HTTPHelper();
            final String responseString = "[]";
            // responseString = myHTTPHelper.getHelper(place_suggestion_url);
            return responseString;

        }

        @Override
        protected void onPostExecute(final String result) {
            myProgressDialogManager.dismissProgresDialog();
            // Toast.makeText(TakeLatLongActivity.this, result,
            // Toast.LENGTH_SHORT).show();
            final String[] place_suggestions = new JSONHelper()
                            .JsonStringofArraysToArray(result);
            adapter = new ArrayAdapter<String>(TakeLatLongActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, place_suggestions);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> arg0,
                                final View arg1, final int position,
                                final long id) {
                    // Log.v("DETAILS1", place_suggestions[position]);
                    final Address my_address = gps
                                    .getMyLocationAddress(place_suggestions[position]);
                    if (my_address == null) {
                        Toast.makeText(getApplicationContext(), "Not able to reverse geocode!", Toast.LENGTH_SHORT)
                                        .show();
                        return;
                    }
                    // Log.v("DETAILS2", my_address.toString());
                    final double _lat = my_address.getLatitude();
                    final double _long = my_address.getLongitude();
                    final String _country = my_address.getCountryName();
                    final String _city = my_address.getSubAdminArea();
                    my_Pref_loc = place_suggestions[position];
                    new savePreferredTask().execute(post_to_my_preferred_loc, Double
                                    .toString(_lat), Double.toString(_long), _city, _country, place_suggestions[position], Auth_Token, FB_Email);
                }
            });
        }
    } // End of askServerForSuggestedPlacesTask

}
