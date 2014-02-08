package com.koramangala.barterli;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class TakeLatLongActivity extends Activity {
	private GPSTracker gps;
	private ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
	private ListView listView;
	private ArrayAdapter<String> adapter;
	//private TextView latLongText;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take_lat_long);
        listView = (ListView) findViewById(R.id.list_data);
        
        gps = new GPSTracker(TakeLatLongActivity.this);
        if(gps.canGetLocation()){
        	double latitude = gps.getLatitude();
        	double longitude = gps.getLongitude();
        	Toast.makeText(TakeLatLongActivity.this, "Your Location is - \nLatitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_LONG).show();	
        	//latLongText.setText("Latitude:" + latitude + "\nLongitude:" + longitude);
        	String place_suggestion_url = getResources().getString(R.string.suggestion_url);
        	//new askServerForSuggestedPlacesTask().execute(place_suggestion_url, Double.toString(latitude), Double.toString(longitude));
        }else{
        	// can't get location
        	// GPS or Network is not enabled
        	// Ask user to enable GPS/network in settings
        	gps.showSettingsAlert();
        }  
    }    
    
    private class askServerForSuggestedPlacesTask extends AsyncTask<String, Void, String> {
    	protected void onPreExecute() {
			myProgressDialogManager.showProgresDialog(TakeLatLongActivity.this, "Retrieving...");
        }
		protected String doInBackground(String... params) {
			String place_suggestion_url = params[0];
			String latitude = params[1];
			String longitude = params[2];
			place_suggestion_url += "?latitude=" + latitude;
			place_suggestion_url +="&longitude=" + longitude;
			// USe HTTP Helper
			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "[]";
			responseString = myHTTPHelper.getHelper(place_suggestion_url);
			return responseString;
			
		}
		protected void onPostExecute(String result) {
			Toast.makeText(TakeLatLongActivity.this, result, Toast.LENGTH_SHORT).show();
			final String[] book_suggestions = new JSONHelper().JsonStringofArraysToArray(result);
			adapter = new ArrayAdapter<String>(TakeLatLongActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, book_suggestions);
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
					//Intent editBookIntent = new Intent(AddBookActivity.this, EditBookDetailsActivity.class);
					//editBookIntent.putExtra("TITLE", book_suggestions[position]);
					//startActivity(editBookIntent);
				}				 
			 });
		}
    }

}
