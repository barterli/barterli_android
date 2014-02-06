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
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class AddBookActivity extends Activity implements iRibbonMenuCallback {

	private static final int READ_ISBN_SCAN_CODE = 0;
	private RibbonMenuView rbmView;
	private Button openLeftPanelButton;
	private EditText titleSearch;
	private EditText authorSearch;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView1);
		openLeftPanelButton = (Button) findViewById(R.id.open_left_panel);
		titleSearch = (EditText) findViewById(R.id.title_search);
		authorSearch = (EditText) findViewById(R.id.author_search);
		listView = (ListView) findViewById(R.id.list_data);
		
		//Setting Listeners
		rbmView.setMenuClickCallback(this);
        rbmView.setMenuItems(R.menu.design_form);
        openLeftPanelButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbmView.toggleMenu();		
			}    	
        });
        titleSearch.addTextChangedListener(mTextEditorWatcher);
        authorSearch.addTextChangedListener(mTextEditorWatcher);
        //End of setting Listeners
        
	}


	@Override
	public void RibbonMenuItemClick(int itemId) {
		switch(itemId){
		  case R.id.ribbon_menu_search:
			  Intent loginintent = new Intent(AddBookActivity.this, LoginActivity.class);
			  startActivity(loginintent);
		  break;	  
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == READ_ISBN_SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				String _isbn = data.getStringExtra("ISBN"); 
				String _type = data.getStringExtra("TYPE");
				if(_type.contentEquals("ISBN")){
					 new askServerForSuggestionsTask().execute(_type, _isbn);
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        	if(s.length() < 5){
        		listView.setAdapter(null);
        	}
        	if((s.length() >=5) && (s.length()%2!=0)){
        		if(titleSearch.hasFocus()){
        			new askServerForSuggestionsTask().execute("title", s.toString());
        		} else if(authorSearch.hasFocus()){
        			new askServerForSuggestionsTask().execute("author", s.toString());
        		}	
        	}
        }
		public void afterTextChanged(Editable s) {}
	}; //End of mTextEditorWatcher
	
	private class askServerForSuggestionsTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... parameters) {
			String suggestion_url = getResources().getString(R.string.suggestion_url);
			suggestion_url += "?q=" + parameters[1];
			if(parameters[0].contentEquals("title")){
				suggestion_url +="&t=" + "title";
			} else if(parameters[0].contentEquals("ISBN")){
				suggestion_url +="&t=" + "isbn";
			}
			HTTPHelper myHTTPHelper = new HTTPHelper();
			String responseString = "";
			responseString = myHTTPHelper.getHelper(suggestion_url);
	        return responseString;
		}
		protected void onPostExecute(String result) {
			JSONArray array;
			try {
				array = new JSONArray(result);
				final String[] sentences = new String[array.length()];
				if(array.length()==0){
					Toast.makeText(AddBookActivity.this, "No match found. Please enter the details yourself!", Toast.LENGTH_SHORT).show();  
					return;
				}
				for (int i = 0; i < array.length(); i++){
				    sentences[i] = array.getString(i);
				}
				adapter = new ArrayAdapter<String>(AddBookActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, sentences);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
						Intent editBookIntent = new Intent(AddBookActivity.this, EditBookDetailsActivity.class);
						editBookIntent.putExtra("TITLE", sentences[position]);
						startActivity(editBookIntent);
					}				 
				 });
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	} // End of askServerForSuggestionsTask

}
