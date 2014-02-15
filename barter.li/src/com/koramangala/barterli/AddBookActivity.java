package com.koramangala.barterli;





import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import com.darvds.ribbonmenu.RibbonMenuView;
import com.darvds.ribbonmenu.iRibbonMenuCallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class AddBookActivity extends Activity implements iRibbonMenuCallback {

	private static final int READ_ISBN_SCAN_CODE = 0;
	private RibbonMenuView rbmView;
	private Button openLeftPanelButton;
	private Button scanButton;
	private EditText bookSearch;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private ConnectionDetector connection_status_detector;
	private AlertDialogManager alert = new AlertDialogManager();
	private Boolean connectionStatus;
	private TextView orLable1;
	private TextView orLable2;
	private Button manualButton;
	private int RequestCounter = 0;
	private SharedPreferences mSharedPreferences;
	private String Auth_Token="";
	private boolean Is_Loc_Set = false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		connection_status_detector = new ConnectionDetector(getApplicationContext());
		connectionStatus = connection_status_detector.isConnectingToInternet();
		//Log.v("CONNECTION", connectionStatus.toString());
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView1);
		openLeftPanelButton = (Button) findViewById(R.id.open_left_panel);
		scanButton = (Button) findViewById(R.id.scanButton);
		bookSearch = (EditText) findViewById(R.id.book_search);
		orLable1 = (TextView) findViewById(R.id.first_or);
		orLable2 = (TextView) findViewById(R.id.second_or);
		manualButton = (Button) findViewById(R.id.manual_entry_button);
		mSharedPreferences = getApplicationContext().getSharedPreferences("BarterLiPref", 0);
        if(mSharedPreferences!=null){
        	Auth_Token = mSharedPreferences.getString(AllConstants.PREF_BARTER_LI_AUTHO_TOKEN, "");
        	Is_Loc_Set = mSharedPreferences.getBoolean(AllConstants.IS_PREF_LOCATION_SET, false);
        	//Toast.makeText(this, "You are aloready Logged in with Auth_token:" + Auth_Token, Toast.LENGTH_SHORT).show();
        }
        
        if(TextUtils.isEmpty(Auth_Token) || !Is_Loc_Set){
        	Toast.makeText(this, "You havent yet made account and/or not yet set preferred location.\nPlease complete all steps!", Toast.LENGTH_SHORT).show();
        }
		
		//authorSearch = (EditText) findViewById(R.id.author_search);
		listView = (ListView) findViewById(R.id.list_data);
				
		//Setting Listeners
		rbmView.setMenuClickCallback(this);
        rbmView.setMenuItems(R.menu.design_form);
        openLeftPanelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbmView.toggleMenu();		
			}    	
        });
        bookSearch.addTextChangedListener(mTextEditorWatcher);
        bookSearch.setOnTouchListener(new View.OnTouchListener(){
			public boolean onTouch(View v, MotionEvent event) {
				scanButton.setVisibility(View.GONE);
				orLable1.setVisibility(View.GONE);
				orLable2.setVisibility(View.GONE);
				manualButton.setVisibility(View.GONE);
				return false;
			}
        });
        //End of setting Listeners
        
	}

	public void RibbonMenuItemClick(int itemId) {
		switch(itemId){
		  case R.id.ribbon_menu_signup:
			  Intent loginintent = new Intent(AddBookActivity.this, LoginActivity.class);
			  startActivity(loginintent);
		  break;
		  case R.id.ribbon_menu_my_profile:
			  Intent libintent = new Intent(AddBookActivity.this, MyProfileActivity.class);
			  startActivity(libintent);
		  break;	
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == READ_ISBN_SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				String _isbn = data.getStringExtra("ISBN"); 
				String _type = data.getStringExtra("TYPE");
				if(_type.contentEquals("ISBN")){
					if (!connectionStatus) {
						alert.showAlertDialog(AddBookActivity.this, "Internet Connection Error","Please connect to working Internet connection", false);
						return;
					}
					 new askServerForSuggestionsTask().execute(_isbn);
				} else {
					Toast.makeText(AddBookActivity.this, "The content " + _isbn + " is not an ISBN " + _type, Toast.LENGTH_SHORT).show();
				}
			}
		}
	} //End of onActivityResult
	
	
	public void scanBarCode(View v){
		Intent scanIntent = new Intent(AddBookActivity.this, ScanActivity.class);
		startActivityForResult(scanIntent, READ_ISBN_SCAN_CODE);
	}
	
	public void addNewBook(View v){
		Intent editBookIntent = new Intent(AddBookActivity.this, EditBookDetailsActivity.class);
		startActivity(editBookIntent);
	}
	
	private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void afterTextChanged(Editable s) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!connectionStatus) {
				alert.showAlertDialog(AddBookActivity.this, "Internet Connection Error","Please connect to working Internet connection", false);
				return;
			}
			if(s.length() == 0 && before == 1){
				scanButton.setVisibility(View.VISIBLE);
				orLable1.setVisibility(View.VISIBLE);
				orLable2.setVisibility(View.VISIBLE);
				manualButton.setVisibility(View.VISIBLE);
			}
        	if(s.length() < 5){	listView.setAdapter(null);	}
        	if((s.length() ==10) || (s.length() >=5) && (s.length()%2!=0)){
        		new askServerForSuggestionsTask().execute(s.toString());
        		RequestCounter +=1;	
        	}
        }
	}; //End of mTextEditorWatcher
	
	private class askServerForSuggestionsTask extends AsyncTask<String, Void, String> {
		protected void onPreExecute(){
			if (!connection_status_detector.isConnectingToInternet()) {
				alert.showAlertDialog(AddBookActivity.this, "Internet Connection Error","Please connect to working Internet connection", false);
				return;
			}
		}
		protected String doInBackground(String... parameters) {
			String suggestion_url = getResources().getString(R.string.suggestion_url);
			suggestion_url += "?q=" + parameters[0];
			suggestion_url +="&t=" + "title";

			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "[]";
			responseString = myHTTPHelper.getHelper(suggestion_url);	
			return responseString;
		}
		protected void onPostExecute(String result) {
				RequestCounter -= 1;
				if(RequestCounter!=0){
					return;
				}
				final String[] book_suggestions = new JSONHelper().JsonStringofArraysToArray(result);
				adapter = new ArrayAdapter<String>(AddBookActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, book_suggestions);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
						Intent editBookIntent = new Intent(AddBookActivity.this, EditBookDetailsActivity.class);
						editBookIntent.putExtra("TITLE", book_suggestions[position]);
						startActivity(editBookIntent);
					}				 
				 });
		}
	} // End of askServerForSuggestionsTask

}
