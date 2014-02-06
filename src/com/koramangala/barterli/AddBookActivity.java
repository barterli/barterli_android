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
		// TODO Auto-generated method status	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == READ_ISBN_SCAN_CODE) {
			if (resultCode == RESULT_OK) {
				String _isbn = data.getStringExtra("ISBN"); 
				String _type = data.getStringExtra("TYPE");
				if(_type.contentEquals("ISBN")){
					//Toast.makeText(AddBookActivity.this, "I shall soon make a HTTP request for " +  _type + "::" + _isbn, Toast.LENGTH_SHORT).show();
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
        			//Toast.makeText(AddBookActivity.this,"I shall soon make a HTTP request for title::" + s, Toast.LENGTH_SHORT).show();
        			new askServerForSuggestionsTask().execute("title", s.toString());
        		} else if(authorSearch.hasFocus()){
        			//Toast.makeText(AddBookActivity.this,"I shall soon make a HTTP request for author:" + s, Toast.LENGTH_SHORT).show();
        			new askServerForSuggestionsTask().execute("author", s.toString());
        		}	
        	}
        }
		public void afterTextChanged(Editable s) {}
	}; //End of mTextEditorWatcher
	
	private class askServerForSuggestionsTask extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... parameters) {
			Log.v("ASYNCTASK-TYPE", parameters[0]);
			Log.v("ASYNCTASK-VALUE", parameters[1]);
			String suggestion_url = getResources().getString(R.string.suggestion_url);
			//String suggestion_url = "http://162.243.198.171/book_info.json/?q=truth";
			suggestion_url += "?q=" + parameters[1];
			if(parameters[0].contentEquals("title")){
				suggestion_url +="&t=" + "title";
			} else if(parameters[0].contentEquals("ISBN")){
				suggestion_url +="&t=" + "isbn";
			}
				
			
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
			//Toast.makeText(AddBookActivity.this, "Execution Complete!", Toast.LENGTH_SHORT).show();  
			Log.v("ASYNC_RESULT", result);
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
				    //Log.v("ASYNC_RESULT", sentences[i]);
				}
				adapter = new ArrayAdapter<String>(AddBookActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, sentences);
				listView.setAdapter(adapter);
				listView.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
						//Toast.makeText(getApplicationContext(), sentences[position]  , Toast.LENGTH_LONG).show();
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
