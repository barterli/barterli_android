package com.barterli.android;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HTTPHelper {
	
	Context localContext;
	private ProgressDialogManager myProgressDialogManager = new ProgressDialogManager();
	
	public HTTPHelper(){
		localContext = null;
	}
	
	public HTTPHelper(Context c){
		localContext = c;
	}
	
   // API Methods
	
	public String getHelper(String url){
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = "[]";
        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                //Log.v("RESPONSE0", responseString);
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
        //Log.v("RESPONSE1", responseString);
        return responseString;
	}
	
	/*
	 * Adds a book to my BooksList
	 * All arguments are strings
	 * Usage: <object>.postBookToMyList(post_url_string, title, author, description, publication_year, barter_type)
	 * 
	 */
	
	public String postBookToMyList(String... parameters){
		String post_to_mybooks_url = parameters[0];
		String _title = parameters[1];
		String _author = parameters[2];
		String _description = parameters[3];
		String _publication_year = parameters[4];
		String _barter_type = parameters[5];
		String _user_token = parameters[6];
		String _fb_Email = parameters[7];
		
		String returnString = "";
			JSONObject bookJsonObject = new JSONObject();
			JSONObject userJsonObject = new JSONObject();
			JSONObject masterJsonObject = new JSONObject();
		try {
			bookJsonObject.put("title", _title);
			bookJsonObject.put("author", _author);
			bookJsonObject.put("description", _description);
			bookJsonObject.put("publication_year", _publication_year);
			bookJsonObject.put("barter_type", _barter_type);
			userJsonObject.put("user_token", _user_token);
			userJsonObject.put("user_email", _fb_Email);
			masterJsonObject = userJsonObject;
			masterJsonObject.put("book", bookJsonObject);
			returnString = PostJson(post_to_mybooks_url, masterJsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return returnString; 	
	}
	
	public String postPreferredLocation(String... parameters){
		String returnString = "";
		JSONObject locationJsonObject = new JSONObject();
		JSONObject userJsonObject = new JSONObject();
		JSONObject masterJsonObject = new JSONObject();
	try {
		String post_to_mypref_loc_url = parameters[0];
		locationJsonObject.put("latitude", parameters[1]);
		locationJsonObject.put("longitude", parameters[2]);
		locationJsonObject.put("city", parameters[3]);
		locationJsonObject.put("country", parameters[4]);
		locationJsonObject.put("locality", parameters[5]);
		userJsonObject.put("user_token", parameters[6]);
		userJsonObject.put("user_email", parameters[7]);
		masterJsonObject = userJsonObject;
		masterJsonObject.put("location", locationJsonObject);
		//Log.v("REQUESTXX", masterJsonObject.toString());
		returnString = PostJson(post_to_mypref_loc_url, masterJsonObject);
	} catch (Exception e) {
		e.printStackTrace();
	} 
	return returnString; 	
	}
	
	public String postNewUser(String... parameters){
		String post_to_newuser_url = parameters[0];
		String uid = parameters[1];
		String firstname = parameters[2];
		String lastname = parameters[3];
		String email = parameters[4];
		String token = parameters[5];

		
		String returnString = "";
		JSONObject userJsonObject = new JSONObject();
		try {
			userJsonObject.put("uid", uid);
		    userJsonObject.put("first_name", firstname);
			userJsonObject.put("last_name", lastname);
			userJsonObject.put("email", email);
			userJsonObject.put("token", token);
			returnString = PostJson(post_to_newuser_url, userJsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnString; 	
	}
	
	public String putBookToMyList(String... parameters){
		String post_to_mybooks_url = parameters[0];
		String _title = parameters[1];
		String _author = parameters[2];
		String _description = parameters[3];
		String _publication_year = parameters[4];
		String _barter_type = parameters[5];
		String _user_token = parameters[6];
		String _fb_Email = parameters[7];
		
		String returnString = "";
			JSONObject bookJsonObject = new JSONObject();
			JSONObject userJsonObject = new JSONObject();
			JSONObject masterJsonObject = new JSONObject();
		try {
			bookJsonObject.put("title", _title);
			bookJsonObject.put("author", _author);
			bookJsonObject.put("description", _description);
			bookJsonObject.put("publication_year", _publication_year);
			bookJsonObject.put("barter_type", _barter_type);
			userJsonObject.put("user_token", _user_token);
			userJsonObject.put("user_email", _fb_Email);
			masterJsonObject = userJsonObject;
			masterJsonObject.put("book", bookJsonObject);
			returnString = PutJson(post_to_mybooks_url, masterJsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return returnString; 	
	}
	
	//Helper Methods
	
	public String PostJson(String url, JSONObject object){
		HttpResponse my_response= null;
		String response="";
		InputStream is=null;
		try{	
		URL my_url = new URL(url);	
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(my_url.toURI());
		//HttpPut httpPut = new HttpPut(my_url.toURI());
		// Prepare JSON to send by setting the entity
		//Log.v("HTTPPOST", object.toString());
		Log.v("REQUESTXX", object.toString());
		httpPost.setEntity(new StringEntity(object.toString(), "UTF-8"));
		// Set up the header types needed to properly transfer JSON
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setHeader("Accept-Encoding", "application/json");
		httpPost.setHeader("Accept-Language", "en-US");
		// Execute POST			
		my_response = httpClient.execute(httpPost);
		HttpEntity entity = my_response.getEntity();
		is = entity.getContent();
		response = convertStreamToString(is);
		Log.v("RESPONSEXX",  response);
		//Log.v("RESPONSEYY",  EntityUtils.toString(my_response.getEntity()));
		//String responseStr = EntityUtils.toString(response.getEntity());
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		return response;	
	}

	public String PutJson(String url, JSONObject object){
		HttpResponse my_response= null;
		String response="";
		InputStream is=null;
		try{	
		URL my_url = new URL(url);	
		HttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(my_url.toURI());
		Log.v("REQUESTGG", url);
		Log.v("REQUESTXX", object.toString());
		httpPut.setEntity(new StringEntity(object.toString(), "UTF-8"));
		// Set up the header types needed to properly transfer JSON
		httpPut.setHeader("Content-Type", "application/json");
		httpPut.setHeader("Accept-Encoding", "application/json");
		httpPut.setHeader("Accept-Language", "en-US");
		// Execute PUT			
		my_response = httpClient.execute(httpPut);
		HttpEntity entity = my_response.getEntity();
		is = entity.getContent();
		response = convertStreamToString(is);
		Log.v("RESPONSEXX",  response);
		
	} catch (Exception e) {
		e.printStackTrace();
	}
		return response;	
	}
	
	private static String convertStreamToString(InputStream is) {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return sb.toString();
	}	
	
}
