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
import org.json.JSONObject;

import android.util.Log;

public class HTTPHelper {
	
	public String getHelper(String url){
		HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(url));
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
		
		HttpResponse response= null;
		JSONObject bookJsonObject = new JSONObject();
		try {
			bookJsonObject.put("title", _title);
			bookJsonObject.put("author", _author);
			bookJsonObject.put("description", _description);
			bookJsonObject.put("publication_year", _publication_year);
			bookJsonObject.put("barter_type", _barter_type);
			
			//String post_to_mybooks_url = getResources().getString(R.string.post_to_mybooks_url);				
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
			response = httpClient.execute(httpPost);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
		
	}
	
	
	
}
