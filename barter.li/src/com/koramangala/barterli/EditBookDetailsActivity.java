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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private Button barterChoiceGroup;
	private String[] barterOptions;
	private String chosenBarterOption = "";
	private ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
	private HTTPHelper myHelper;
	private SharedPreferences mSharedPreferences;
	private String Auth_Token="";
	private String FB_Email="";
	private boolean Is_Loc_Set = false;
	private String BOOK_ID = "";
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_book);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		myHelper = new HTTPHelper(EditBookDetailsActivity.this);
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView1);
		openLeftPanelButton = (Button) findViewById(R.id.open_left_panel);
		titleText = (EditText)findViewById(R.id.title);
		authorText = (EditText)findViewById(R.id.author);
		descriptionText = (EditText)findViewById(R.id.description);
		publicationYearText = (EditText)findViewById(R.id.publication);
		barterChoiceGroup = (Button) findViewById(R.id.barter_option_button);
		rbmView.setMenuClickCallback(this);
        rbmView.setMenuItems(R.menu.design_form);
        barterOptions = getResources().getStringArray(R.array.barterOptions);
		final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(EditBookDetailsActivity.this, android.R.layout.simple_spinner_dropdown_item, barterOptions);
		mSharedPreferences = getApplicationContext().getSharedPreferences("BarterLiPref", 0);
        if(mSharedPreferences!=null){
        	Auth_Token = mSharedPreferences.getString(AllConstants.PREF_BARTER_LI_AUTHO_TOKEN, "empty");
        	FB_Email = mSharedPreferences.getString(AllConstants.FB_USER_EMAIL, "");
        	Is_Loc_Set = mSharedPreferences.getBoolean(AllConstants.IS_PREF_LOCATION_SET, false);
        	//Toast.makeText(this, "You are aloready Logged in with Auth_token:" + Auth_Token, Toast.LENGTH_SHORT).show();
        }
		
		Intent _i = getIntent();
		if(_i.hasExtra("BOOK_ID")){ 
			BOOK_ID = _i.getExtras().getString("BOOK_ID").toString();	
			Toast.makeText(this, "ID Received: " + BOOK_ID, Toast.LENGTH_SHORT).show();
		} 
		if(_i.hasExtra("TITLE")){titleText.setText(_i.getExtras().getString("TITLE").toString());}
		if(_i.hasExtra("TITLE") && !_i.hasExtra("BOOK_ID")){
			new getBookInfoFromServerTask().execute(_i.getExtras().getString("TITLE").toString());
		}
		if(_i.hasExtra("AUTHOR")){ authorText.setText(_i.getExtras().getString("AUTHOR").toString());	}
		if(_i.hasExtra("DESCRIPTION")){ descriptionText.setText(_i.getExtras().getString("DESCRIPTION").toString());	}
		if(_i.hasExtra("PUBLICATION_YEAR")){ publicationYearText.setText(_i.getExtras().getString("PUBLICATION_YEAR").toString());	}
		if(_i.hasExtra("BARTER_TYPE")){ 
			barterChoiceGroup.setText(_i.getExtras().getString("BARTER_TYPE").toString());
			chosenBarterOption = _i.getExtras().getString("BARTER_TYPE").toString();
		 }
			
		// Set Listeners
		barterChoiceGroup.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				new AlertDialog.Builder(EditBookDetailsActivity.this)
				  .setTitle("Select Valid Option")
				  .setAdapter(spinnerArrayAdapter, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				      chosenBarterOption = barterOptions[which];
				      barterChoiceGroup.setText(chosenBarterOption);
				      dialog.dismiss();
				    }
				  }).create().show();
			}
		});	
		
        openLeftPanelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbmView.toggleMenu();		
			}    	
        });
        // End of setting Listeners
        
	} //End of oncreate

	public void RibbonMenuItemClick(int itemId) {
		switch(itemId){
		  case R.id.ribbon_menu_signup:
			  Intent loginintent = new Intent(EditBookDetailsActivity.this, LoginActivity.class);
			  startActivity(loginintent);
		  break;
		  case R.id.ribbon_menu_my_profile:
			  Intent libintent = new Intent(EditBookDetailsActivity.this, MyProfileActivity.class);
			  startActivity(libintent);
		  break;	
		}
	}
	
	public void addBook (View v){
        if(TextUtils.isEmpty(Auth_Token) || !Is_Loc_Set){
        	Toast.makeText(this, "You havent yet made account and/or not yet set preferred location.\nPlease complete all steps!", Toast.LENGTH_SHORT).show();
        	return;
        }
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
		if(TextUtils.isEmpty(chosenBarterOption)){
			Toast.makeText(EditBookDetailsActivity.this, "Please enter what you want to do with the book!" , Toast.LENGTH_SHORT).show();
			return;
		}

		new saveMyBookToServerTask().execute(_title, _author, _description, _publication_year, chosenBarterOption, Auth_Token, FB_Email);		
	} // End of addBook
	
	
	//Synctask helper to post book to server
	
	private class saveMyBookToServerTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			super.onPreExecute();
			myProgressDialogManager.showProgresDialog(EditBookDetailsActivity.this, "Saving...");
		}
		protected String doInBackground(String... parameters) {
			String post_to_mybooks_url = getResources().getString(R.string.post_to_mybooks_url);
			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "";
			String _title = parameters[0];
			String _author = parameters[1];
			String _description = parameters[2];
			String _publication_year = parameters[3];
			String _barter_type = parameters[4];
			String _user_token =  parameters[5];
			String _fb_Email = parameters[6];
			if(TextUtils.isEmpty(BOOK_ID)){
				responseString = myHTTPHelper.postBookToMyList(post_to_mybooks_url, _title, _author, _description, _publication_year, _barter_type, _user_token, _fb_Email);
			} else {
				String put_to_mybooks_url = post_to_mybooks_url + BOOK_ID;
				responseString = myHTTPHelper.putBookToMyList(put_to_mybooks_url, _title, _author, _description, _publication_year, _barter_type, _user_token, _fb_Email);
			}
			
	        return responseString;
		}
		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			//** STILL TO DO:- Verify that result is success **//
			Toast.makeText(EditBookDetailsActivity.this, "Successfully Added!. Will see where to go now!", Toast.LENGTH_SHORT).show();
			resetViews();
		}
	} // End of askServerForSuggestionsTask

	
	
	//Synctask helper to get book suggestion
	
	private class getBookInfoFromServerTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			myProgressDialogManager.showProgresDialog(EditBookDetailsActivity.this, "Retrieving...");
        }
		protected String doInBackground(String... parameters) {
			String suggestion_url = getResources().getString(R.string.book_info_url);
			suggestion_url += "?q=" + parameters[0];
			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "";
			responseString = myHTTPHelper.getHelper(suggestion_url);
	        return responseString;
		}
		protected void onPostExecute(String result) {
			myProgressDialogManager.dismissProgresDialog();
			barterChoiceGroup.performClick();
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
	
	
	
	
	
	public void resetViews(){
		titleText.setText("");
		authorText.setText("");
		descriptionText.setText("");
		publicationYearText.setText("");
		barterChoiceGroup.setText(R.string.barter_type_button_label);
		chosenBarterOption = "";
	} //End of resetViews
	
}
