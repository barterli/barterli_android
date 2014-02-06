package com.koramangala.barterli;



import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.facebook.*;
import com.facebook.model.*;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LoginActivity extends Activity {
	
	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;
	// Shared Preferences
	private static SharedPreferences mSharedPreferences;
	// Internet Connection detector
	private ConnectionDetector connection_status_detector;
	// Alert Dialog Manager
	private AlertDialogManager alert = new AlertDialogManager();
	private TextView welcome;
	private Editor sharedPrefEditor;
	
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        setContentView(R.layout.login_activity);  
        mSharedPreferences = getApplicationContext().getSharedPreferences("BarterLiPref", 0);
        welcome = (TextView) findViewById(R.id.welcome);
        connection_status_detector = new ConnectionDetector(getApplicationContext());
        sharedPrefEditor = mSharedPreferences.edit();
        
        //Act when twitter returns
        if (!isTwitterLoggedInAlready()) {
        	Uri uri = getIntent().getData();
        	if (uri != null && uri.toString().startsWith(AllConstants.TWITTER_CALLBACK_URL)) {
        		String verifier = uri.getQueryParameter(AllConstants.URL_TWITTER_OAUTH_VERIFIER);
        		new twitterAsyncTaskSecondRound().execute(verifier);
        	}
        }
        
        
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	public void LoginThroughFacebook(View v){
		if (!connection_status_detector.isConnectingToInternet()) {
			alert.showAlertDialog(LoginActivity.this, "Internet Connection Error","Please connect to working Internet connection", false);
			return;
		}
		
		
		// start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {
          // callback when session changes state
          public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
            	String accessToken = session.getAccessToken();
            
            	sharedPrefEditor.putString(AllConstants.FB_ACCESS_TOKEN, accessToken);
            	sharedPrefEditor.commit();

              // make request to the /me API
            	Request.newMeRequest(session, new Request.GraphUserCallback() {
            		  // callback after Graph API response with user object
            		  public void onCompleted(GraphUser user, Response response) {
            		    if (user != null) {
            		      sharedPrefEditor.putString(AllConstants.FB_USERNAME, user.getName());	
            		      sharedPrefEditor.putString(AllConstants.FB_USERID, user.getId());	
            		      //Few more details are yet to come!
            		      sharedPrefEditor.commit();	
            		      welcome.setText("Hello " + user.getName() + "!");
            		      //Save all details in preferences 
            		    }
            		  }
            	}).executeAsync();
            }
          }
        });		
	} //End of LoginThroughFacebook
	
	public void loginToTwitter(View v){
		if (!isTwitterLoggedInAlready()) {
			if (!connection_status_detector.isConnectingToInternet()) {
				alert.showAlertDialog(LoginActivity.this, "Internet Connection Error","Please connect to working Internet connection", false);
				return;
			}
			new twitterAsyncTaskFirstRound().execute();
		} else {
			Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean isTwitterLoggedInAlready() {
		//Consult shared preferences. Yet to be completed!
		return mSharedPreferences.getBoolean(AllConstants.PREF_KEY_TWITTER_LOGIN, false);
	}
	
	private class twitterAsyncTaskFirstRound extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... parameters) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(AllConstants.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(AllConstants.TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();
			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			
			try{
				requestToken = twitter.getOAuthRequestToken(AllConstants.TWITTER_CALLBACK_URL);
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
			} catch(TwitterException e){
				e.printStackTrace();
			}
			return null;
		}	
	} //End of twitterAsyncTask
	
	private class twitterAsyncTaskSecondRound extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... params) {
			String verifier = params[0];
			String username="";
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				// Save details in Preferences
				
				sharedPrefEditor.putString(AllConstants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
				sharedPrefEditor.putString(AllConstants.PREF_KEY_OAUTH_SECRET,accessToken.getTokenSecret());
				// Store login status - true
				sharedPrefEditor.putBoolean(AllConstants.PREF_KEY_TWITTER_LOGIN, true);
				sharedPrefEditor.commit(); // save changes
				Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
				long userID = accessToken.getUserId();
				User user = twitter.showUser(userID);
				username = user.getName();
				//
    		}catch(Exception e){
    			Log.e("Twitter Login Error", "> " + e.getMessage());
    		}
    		return username;
		}
		protected void onPostExecute(String result) {
			welcome.setText("Hi " + result + "!");
		}	
	} // End of twitterAsyncTaskSecondRound

}
