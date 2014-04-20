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

package li.barter.fragments;

import java.io.IOException;
import java.util.Arrays;

import com.android.volley.Request.Method;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.activities.HomeActivity;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class LoginFragment extends AbstractBarterLiFragment implements
                OnClickListener, StatusCallback, ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG                = "LoginFragment";

    /**
     * Minimum length of the entered password
     */
    private final int          		  mMinPasswordLength = 8;
    private Button        		 	  mFacebookLoginButton;
    private Button            		  mGoogleLoginButton;
    private Button            		  mSubmitButton;
    private EditText          		  mEmailEditText;
    private EditText          		  mPasswordEditText;
    private GoogleApiClient           mGoogleApiClient;
   

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        final View view = inflater.inflate(R.layout.fragment_login, null);

        mFacebookLoginButton = (Button) view
                        .findViewById(R.id.button_facebook_login);
        mGoogleLoginButton = (Button) view
                        .findViewById(R.id.button_google_login);
        mSubmitButton = (Button) view.findViewById(R.id.button_submit);
        mEmailEditText = (EditText) view.findViewById(R.id.edit_text_email);
        mPasswordEditText = (EditText) view
                        .findViewById(R.id.edit_text_password);

        
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, this, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
            	session.openForRead(new Session.OpenRequest(this).setPermissions(Arrays.asList(AppConstants.FBPERMISSIONS)).setCallback(this));
            }
        }
        mFacebookLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
        setActionBarDrawerToggleEnabled(false);
        return view;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }
    
    

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession()
                        .onActivityResult(getActivity(), requestCode, resultCode, data);
        
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {

            case R.id.button_facebook_login: {
                // TODO FacebookLoggerin
            	  Session session = Session.getActiveSession();
                  if (!session.isOpened() && !session.isClosed()) {
                	  session.openForRead(new Session.OpenRequest(this).setPermissions(Arrays.asList(AppConstants.FBPERMISSIONS)).setCallback(this));
                  } else {
                      Session.openActiveSession(getActivity(), this, true, this);
                  }
                break;
            }

            case R.id.button_google_login: {
                // TODO GoogleLoggerin
            	
            	
            	Bundle appActivities = new Bundle();
        		appActivities.putString(GoogleAuthUtil.KEY_REQUEST_VISIBLE_ACTIVITIES,
        		          "<app-activity1> <app-activity2>");
        		String scopes = "oauth2:server:client_id:685603964337.apps.googleusercontent.com:api_scope:<scope1> <scope2>";
        		String code = null;
        			
        		try {
        		  code = GoogleAuthUtil.getToken(
        		      getActivity(),                                              // Context context
        		      Plus.AccountApi.getAccountName(mGoogleApiClient),  // String accountName
        		      scopes,                                            // String scope
        		      appActivities                                      // Bundle bundle
        				  );
        	      Logger.i(TAG, code, "debug oath google");


        		} catch (IOException transientEx) {
        		  // network or server error, the call is expected to succeed if you try again later.
        		  // Don't attempt to call again immediately - the request is likely to
        		  // fail, you'll hit quotas or back-off.
        		  return;
        		} catch (UserRecoverableAuthException e) {
        		  // Requesting an authorization code will always throw
        		  // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
        		  // because the user must consent to offline access to their data.  After
        		  // consent is granted control is returned to your activity in onActivityResult
        		  // and the second call to GoogleAuthUtil.getToken will succeed.

        			return;
        		} catch (GoogleAuthException authEx) {
        		  // Failure. The call is not expected to ever succeed so it should not be
        		  // retried.
        		  return;
        		} catch (Exception e) {
        		  throw new RuntimeException(e);
        		}
            	
                break;
            }

            case R.id.button_submit: {
                if (isInputValid()) {
                    login(mEmailEditText.getText().toString(), mPasswordEditText
                                    .getText().toString());
                }
                break;
            }
        }
    }

    /**
     * Call the login Api
     * 
     * @param email The entered email
     * @param password The entered password
     */
    private void login(final String email, final String password) {

        final JSONObject requestObject = new JSONObject();

        try {
            requestObject.put(HttpConstants.PROVIDER, AppConstants.MANUAL);
            requestObject.put(HttpConstants.EMAIL, email);
            requestObject.put(HttpConstants.PASSWORD, password);
            final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.CREATE_USER, requestObject.toString(), mVolleyCallbacks);
            request.setRequestId(RequestId.CREATE_USER);
            addRequestToQueue(request, true, 0);
        } catch (final JSONException e) {
            // Should never happen
            Logger.e(TAG, e, "Error building create user json");
        }

    }
    
    /**
     * Call the login Api
     * 
     * @param oath token we get from providers
     * @param facebook or google in our case
     */
    private void loginprovider(final String token, final String provider) {

        final JSONObject requestObject = new JSONObject();

        try {
            requestObject.put(HttpConstants.PROVIDER, AppConstants.FACEBOOK);
            requestObject.put(HttpConstants.ACCESS_TOKEN, token);
            final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.CREATE_USER, requestObject.toString(), mVolleyCallbacks);
            request.setRequestId(RequestId.CREATE_USER);
            addRequestToQueue(request, true, 0);
        } catch (final JSONException e) {
            // Should never happen
            Logger.e(TAG, e, "Error building create user json");
        }

    }

    /**
     * Validates the text fields for creating a user. Automatically sets the
     * error messages for the text fields
     * 
     * @return <code>true</code> If the input is valid, <code>false</code>
     *         otherwise
     */
    private boolean isInputValid() {

        final String email = mEmailEditText.getText().toString();
        boolean isValid = !TextUtils.isEmpty(email);

        if (!isValid) {
            mEmailEditText.setError(getString(R.string.error_enter_email));
        } else {
            //Using a regex for email comparison is pointless
            isValid &= email.contains("@");
            if (!isValid) {
                mEmailEditText.setError(getString(R.string.error_invalid_email));
            }
        }

        if (isValid) {
            final String password = mPasswordEditText.getText().toString();
            isValid &= !TextUtils.isEmpty(password);

            if (!isValid) {
                mPasswordEditText
                                .setError(getString(R.string.error_enter_password));
            } else {
                isValid &= (password.length() >= mMinPasswordLength);
                if (!isValid) {
                    mPasswordEditText
                                    .setError(getString(R.string.error_password_minimum_length, mMinPasswordLength));
                }
            }
        }

        return isValid;
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        if (requestId == RequestId.CREATE_USER) {

            final Bundle userInfo = response.responseBundle;

            UserInfo.INSTANCE.setAuthToken(userInfo
                            .getString(HttpConstants.AUTH_TOKEN));
            UserInfo.INSTANCE.setEmail(userInfo.getString(HttpConstants.EMAIL));
            UserInfo.INSTANCE.setId(userInfo.getString(HttpConstants.ID_USER));
            UserInfo.INSTANCE.setProfilePicture(userInfo
                            .getString(HttpConstants.IMAGE_URL));

            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_auth_token, userInfo
                                            .getString(HttpConstants.AUTH_TOKEN));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_email, userInfo
                                            .getString(HttpConstants.EMAIL));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_description, userInfo
                                            .getString(HttpConstants.DESCRIPTION));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_first_name, userInfo
                                            .getString(HttpConstants.FIRST_NAME));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_name, userInfo
                                            .getString(HttpConstants.LAST_NAME));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_user_id, userInfo
                                            .getString(HttpConstants.ID_USER));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_location, userInfo
                                            .getString(HttpConstants.LOCATION));
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_profile_image, userInfo
                                            .getString(HttpConstants.IMAGE_URL));

            BarterLiApplication.startChatService();

            final String locationId = userInfo
                            .getString(HttpConstants.LOCATION);
            if (TextUtils.isEmpty(locationId)) {
                final Bundle myArgs = getArguments();
                Bundle preferredLocationArgs = null;
                if (myArgs != null) {
                    preferredLocationArgs = new Bundle(myArgs);
                }
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), SelectPreferredLocationFragment.class
                                                .getName(), preferredLocationArgs), FragmentTags.SELECT_PREFERRED_LOCATION_FROM_LOGIN, true, FragmentTags.BS_PREFERRED_LOCATION);

            } else {
                final String tag = getTag();
                if (tag.equals(FragmentTags.LOGIN_FROM_NAV_DRAWER)) {
                    loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                    .instantiate(getActivity(), ProfileFragment.class
                                                    .getName(), null), FragmentTags.PROFILE, true, FragmentTags.BOOKS_AROUND_ME);

                } else if (tag.equals(FragmentTags.LOGIN_TO_ADD_BOOK)) {
                    onUpNavigate();
                }
            }

        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

        if (requestId == RequestId.CREATE_USER) {
            showCrouton(errorMessage, AlertStyle.ERROR);
        }
    }


@Override
public void onStart() {
	// TODO Auto-generated method stub
	super.onStart();
	mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
	.addConnectionCallbacks(this)
	.addOnConnectionFailedListener(this)
	.addApi(Plus.API, null)
	.addScope(Plus.SCOPE_PLUS_LOGIN)
	.build();
	mGoogleApiClient.connect();
}
@Override
public void onStop() {
	// TODO Auto-generated method stub
	super.onStop();
	if (mGoogleApiClient.isConnected()) {
		mGoogleApiClient.disconnect();
	}
}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		// TODO session returns the user_token
		if(!session.getAccessToken().equals(""))
		{
		loginprovider(session.getAccessToken(), AppConstants.FACEBOOK);
		}
		
	}


	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		if(mGoogleApiClient.isConnected()||mGoogleApiClient.isConnecting())
		{
			Logger.d(TAG, "google connect", "GOOGLE");
		}
	}


	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
