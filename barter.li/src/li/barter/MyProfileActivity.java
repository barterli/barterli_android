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

import li.barter.utils.SharedPreferenceHelper;

public class MyProfileActivity extends AbstractBarterLiActivity {

    private String                   my_email;
    private String                   my_name;
    private String                   my_pref_location;

    private TextView                 my_name_text;
    private TextView                 my_email_text;
    private TextView                 my_pref_location_text;
    private ListView                 listView;
    private final AlertDialogManager alert = new AlertDialogManager();
    private Boolean                  connectionStatus;
    private String                   Auth_Token;
    private String                   get_profile_url;
    private ArrayAdapter             adapter;

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.show_my_profile);

        my_email = SharedPreferenceHelper.getString(this,
                        R.string.email);
        my_name = SharedPreferenceHelper.getString(this,
                        R.string.fb_username);
        my_pref_location = SharedPreferenceHelper.getString(this,
                        R.string.preferred_location);
        Auth_Token = SharedPreferenceHelper.getString(this,
                        R.string.auth_token);
        my_name_text = (TextView) findViewById(R.id.my_name);
        my_email_text = (TextView) findViewById(R.id.my_email);
        my_pref_location_text = (TextView) findViewById(R.id.my_pref_loc);
        my_name_text.setText(my_name);
        my_email_text.setText(my_email);
        my_pref_location_text.setText(my_pref_location);
        // get_profile_url =
        // getResources().getString(R.string.preferred_location);
        listView = (ListView) findViewById(R.id.list_my_books);
        new askServerForMyDetails().execute(get_profile_url, my_email,
                        Auth_Token);
    }

    private class askServerForMyDetails extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            if (!isConnectedToInternet()) {
                alert.showAlertDialog(
                                MyProfileActivity.this,
                                "Internet Connection Error",
                                "Please connect to working Internet connection",
                                false);
                return;
            }
        }

        @Override
        protected String doInBackground(final String... parameters) {
            String local_profile_url = parameters[0];
            local_profile_url += "?user_email=" + parameters[1];
            local_profile_url += "&user_token=" + parameters[2];

            // HTTPHelper myHTTPHelper = new HTTPHelper();
            final String responseString = "[]";
            // responseString = myHTTPHelper.getHelper(local_profile_url);
            return responseString;
        }

        @Override
        protected void onPostExecute(final String result) {
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
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1, my_books);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> arg0,
                                final View arg1, final int position,
                                final long id) {
                    // Toast.makeText(MyProfileActivity.this,
                    // "Edit will be built soon!", Toast.LENGTH_SHORT).show();
                    final Intent editBookIntent = new Intent(
                                    MyProfileActivity.this,
                                    AddOrEditBookActivity.class);
                    editBookIntent.putExtra("TITLE", my_books[position]);
                    try {
                        editBookIntent.putExtra("BOOK_ID", my_book_objects
                                        .getJSONObject(position)
                                        .optString("id"));
                        editBookIntent.putExtra("AUTHOR",
                                        my_book_objects.getJSONObject(position)
                                                        .optString("author"));
                        editBookIntent.putExtra(
                                        "DESCRIPTION",
                                        my_book_objects.getJSONObject(position)
                                                        .optString("description"));
                        editBookIntent.putExtra(
                                        "PUBLICATION_YEAR",
                                        my_book_objects.getJSONObject(position)
                                                        .optString("publication_year"));
                        editBookIntent.putExtra(
                                        "BARTER_TYPE",
                                        my_book_objects.getJSONObject(position)
                                                        .optString("barter_type"));
                    } catch (final JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(editBookIntent);
                }
            });
        }
    } // End of askServerForMyDetails

}
