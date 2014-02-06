package com.koramangala.barterli;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.darvds.ribbonmenu.RibbonMenuView;
import com.darvds.ribbonmenu.iRibbonMenuCallback;

public class EditBookDetailsActivity extends Activity implements iRibbonMenuCallback{
	private RibbonMenuView rbmView;
	private Button openLeftPanelButton;
	private EditText titleText;
	private EditText authorText;
	private EditText descriptionText;
	private EditText publicationYearText;
	private RadioGroup barterChoiceGroup;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_book);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView1);
		openLeftPanelButton = (Button) findViewById(R.id.open_left_panel);
		titleText = (EditText)findViewById(R.id.title);
		authorText = (EditText)findViewById(R.id.author);
		descriptionText = (EditText)findViewById(R.id.description);
		publicationYearText = (EditText)findViewById(R.id.publication);
		barterChoiceGroup = (RadioGroup) findViewById(R.id.barter_type);
		rbmView.setMenuClickCallback(this);
        rbmView.setMenuItems(R.menu.design_form);
        
		Intent _i = getIntent();
		if(_i.hasExtra("TITLE")){
			titleText.setText(_i.getExtras().getString("TITLE").toString());
			new getBookInfoFromServerTask().execute(_i.getExtras().getString("TITLE").toString());		
		}
		
        openLeftPanelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbmView.toggleMenu();		
			}    	
        });
	} //End of oncreate

	@Override
	public void RibbonMenuItemClick(int itemId) {
		// TODO Auto-generated method stub
		
	}
	
	public void addBook (View v){
		String _title = titleText.getText().toString();
		if(TextUtils.isEmpty(_title)){
			Toast.makeText(EditBookDetailsActivity.this, "Please Enter Title" , Toast.LENGTH_SHORT).show();
			return;
		}
		String _author = authorText.getText().toString();
		if(TextUtils.isEmpty(_author)){
			Toast.makeText(EditBookDetailsActivity.this, "Please Enter Author Name" , Toast.LENGTH_SHORT).show();
			return;
		}
		String _description = descriptionText.getText().toString();
		String _publication_year = publicationYearText.getText().toString();
		String _barter_type ="";
		int _selected_barter_id = barterChoiceGroup.getCheckedRadioButtonId();
		if (_selected_barter_id == -1){
			Toast.makeText(EditBookDetailsActivity.this, "Please Enter Barter Type" , Toast.LENGTH_SHORT).show();
			return;
		}
		RadioButton _selected_barter_button = (RadioButton) findViewById(_selected_barter_id);
		_barter_type = _selected_barter_button.getText().toString();	
		new saveMyBookToServerTask().execute(_title, _author, _description, _publication_year, _barter_type);		
		//Toast.makeText(EditBookDetailsActivity.this, "" + _title + "::" + _author + "::" + _description + "::" + _publication_year + "::" 
				//+ _barter_type, Toast.LENGTH_SHORT).show();
	} // End of addBook
	
	private class saveMyBookToServerTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
        }
		protected String doInBackground(String... parameters) {
			String _title = parameters[0];
			String _author = parameters[1];
			String _description = parameters[2];
			String _publication_year = parameters[3];
			String _barter_type = parameters[4];
			JSONObject bookJsonObject = new JSONObject();
			try {
				bookJsonObject.put("title", _title);
				bookJsonObject.put("author", _author);
				bookJsonObject.put("description", _description);
				bookJsonObject.put("publication_year", _publication_year);
				bookJsonObject.put("barter_type", _barter_type);
				
				String post_to_mybooks_url = getResources().getString(R.string.post_to_mybooks_url);				
				final String _url_final = post_to_mybooks_url;
				URL url = new URL(_url_final);
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url.toURI());
				// Prepare JSON to send by setting the entity
				Log.v("HTTPPOST", bookJsonObject.toString());
				httpPost.setEntity(new StringEntity(bookJsonObject.toString(), "UTF-8"));
				// Set up the header types needed to properly transfer JSON
				httpPost.setHeader("Content-Type", "application/json");
				httpPost.setHeader("Accept-Encoding", "application/json");
				httpPost.setHeader("Accept-Language", "en-US");
				// Execute POST			
				HttpResponse response = httpClient.execute(httpPost);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			return null;

		}
		protected void onPostExecute(String result) {
			Toast.makeText(EditBookDetailsActivity.this, "Successfully Added!. Will see where to go now!", Toast.LENGTH_SHORT).show();
		}
	} // End of askServerForSuggestionsTask

	private class getBookInfoFromServerTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
        }
		protected String doInBackground(String... parameters) {
			
			String suggestion_url = getResources().getString(R.string.book_info_url);
			//String suggestion_url = "http://162.243.198.171/book_info.json/?q=truth";
			suggestion_url += "?q=" + parameters[0];
							
			HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response;
	        String responseString = null;
	        try {
	            response = httpclient.execute(new HttpGet(suggestion_url));
	            StatusLine statusLine = response.getStatusLine();
	            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                response.getEntity().writeTo(out);
	                out.close();
	                responseString = out.toString();
	            } else{
	                //Closes the connection.
	                response.getEntity().getContent().close();
	                throw new IOException(statusLine.getReasonPhrase());
	            }
	        } catch (ClientProtocolException e) {
	        	e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();e.printStackTrace();
	        }
	        return responseString;
		}
		protected void onPostExecute(String result) {
			Log.v("BOOK_INFO", result);
			try {
				JSONObject bookObject = new JSONObject(result);
				if(bookObject.has(AllConstants.DESCRIPTION_KEY)){
					descriptionText.setText(bookObject.getString(AllConstants.DESCRIPTION_KEY));
				}
				if(bookObject.has(AllConstants.PUBLICATION_YEAR_KEY)){
					publicationYearText.setText(bookObject.getString(AllConstants.PUBLICATION_YEAR_KEY));
				}
				if(bookObject.has(AllConstants.PUBLICATION_AUTHORS)){
					JSONObject authorsObject =  bookObject.getJSONObject(AllConstants.PUBLICATION_AUTHORS);
					if(authorsObject.has(AllConstants.PUBLICATION_AUTHOR)){
						
						if (authorsObject.get(AllConstants.PUBLICATION_AUTHOR) instanceof JSONObject){
							JSONObject singleAuthorObject =  authorsObject.getJSONObject(AllConstants.PUBLICATION_AUTHOR);
							authorText.setText(singleAuthorObject.getString(AllConstants.PUBLICATION_AUTHOR_NAME));
						} else if(authorsObject.get(AllConstants.PUBLICATION_AUTHOR) instanceof JSONArray){
							JSONArray authorArray =  authorsObject.getJSONArray(AllConstants.PUBLICATION_AUTHOR);
							JSONObject firstAuthorObject = authorArray.getJSONObject(0);
							authorText.setText(firstAuthorObject.getString(AllConstants.PUBLICATION_AUTHOR_NAME));
						}
					}
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	} // End of getBookInfoFromServerTask
	
}
