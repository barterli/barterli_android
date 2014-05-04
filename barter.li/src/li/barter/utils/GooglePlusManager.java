/*******************************************************************************
 * Copyright 2014,  barter.li
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

package li.barter.utils;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Manager for Google+ Connectivity
 * 
 * @author Vinay S Shenoy
 */
public class GooglePlusManager implements ConnectionCallbacks,
                OnConnectionFailedListener {

    private static final String          TAG                     = "GooglePlusManager";

    public static final int              CONNECTION_UPDATE_ERROR = 500;

    /** {@link PlusClient} instance for connecting to Google+ */
    private final PlusClient             mPlusClient;

    /** {@link ConnectionResult} for Google+ login */
    private ConnectionResult             mConnectionResult;

    private final Activity               mActivity;

    private String                       mAccessToken;

    private boolean                      mResolveOnFail;

    private boolean                      mClickedToLogin;

    private final GooglePlusAuthCallback mGooglePlusAuthCallback;

    private final String[]               mScopes                 = new String[] {
            Scopes.PLUS_LOGIN, Scopes.PROFILE,

            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile"
                                                                 };

    /**
     * @param activity Activity in which this is used
     */
    public GooglePlusManager(final Activity activity, final GooglePlusAuthCallback authCallback) {

        mActivity = activity;
        mGooglePlusAuthCallback = authCallback;
        mPlusClient = new PlusClient.Builder(activity, this, this)
                        .setScopes(mScopes).build();
        mClickedToLogin = false;
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {

        // Most of the time, the connection will fail with a
        // user resolvable result. We can store that in our
        // mConnectionResult property ready for to be used
        // when the user clicks the sign-in button.
        if (connectionResult.hasResolution()) {
            mConnectionResult = connectionResult;
            if (mResolveOnFail) {
                // This is a local helper function that starts
                // the resolution of the problem, which may be
                // showing the user an account chooser or similar.
                startResolution();
            }
        }

    }

    @Override
    public void onConnected(final Bundle connectionHint) {

        // Turn off the flag, so if the user signs out they'll have to
        // tap to sign in again.
        mResolveOnFail = false;

        // Retrieve the oAuth 2.0 access token.
        final Context context = mActivity.getApplicationContext();
        final AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(final Void... params) {

                final String scope = "oauth2:" + TextUtils.join(" ", mScopes);

                Logger.v(TAG, "Scope %s Client name %s", scope, mPlusClient
                                .getAccountName());
                try {
                    // We can retrieve the token to check via
                    // tokeninfo or to pass to a service-side
                    // application.
                    mAccessToken = GoogleAuthUtil.getToken(context, mPlusClient
                                    .getAccountName(), scope);

                } catch (final UserRecoverableAuthException e) {
                    // This error is recoverable, so we could fix
                    // this
                    // by displaying the intent to the user.

                    mAccessToken = null;
                    if (mClickedToLogin) {

                        mActivity.startActivityForResult(e.getIntent(), CONNECTION_UPDATE_ERROR);
                    }
                } catch (final IOException e) {
                    mGooglePlusAuthCallback.onLoginError(e);
                    mAccessToken = null;
                } catch (final GoogleAuthException e) {
                    mGooglePlusAuthCallback.onLoginError(e);
                    mAccessToken = null;
                } catch (final IllegalStateException e) {
                    /*
                     * Catching this here because it is not supposed to be
                     * coming here before getting connected, but it is.
                     * Apparently a bug with Plus Client as Google apps also
                     * crash with this same issue occassionally. Should
                     * investigate more
                     */
                    mGooglePlusAuthCallback.onLoginError(e);
                    mAccessToken = null;
                }
                return mAccessToken;
            }

            @Override
            protected void onPostExecute(final String accessToken) {

                if ((mAccessToken != null) && mClickedToLogin) {
                    mGooglePlusAuthCallback.onLogin();
                }
            }

        };
        task.execute();

    }

    @Override
    public void onDisconnected() {

        Logger.v(TAG, "Disconnected");
        mConnectionResult = null;
    }

    /**
     * Call in the Activity's onStart() lifecycle method
     */
    public void onActivityStarted() {

        mClickedToLogin = false;
        mPlusClient.connect();
    }

    /**
     * Call in the Activity's onStop() lifecycle method
     */
    public void onActivityStopped() {

        mPlusClient.disconnect();
    }

    public void onActivityResult() {

        // If we have a successful result, we will want to be able to
        // resolve any further errors, so turn on resolution with our
        // flag.
        mResolveOnFail = true;
        // If we have a successful result, lets call connect() again. If
        // there are any more errors to resolve we'll get our
        // onConnectionFailed, but if not, we'll get onConnected.
        mPlusClient.connect();
    }

    public void login() {

        if (!mPlusClient.isConnected() || TextUtils.isEmpty(mAccessToken)) {
            mClickedToLogin = true;
            // Make sure that we will start the resolution (e.g. fire the
            // intent and pop up a dialog for the user) for any errors
            // that come in.
            mResolveOnFail = true;
            // We should always have a connection result ready to resolve,
            // so we can start that process.
            if (mConnectionResult != null) {
                startResolution();
            } else {
                // If we don't have one though, we can start connect in
                // order to retrieve one.
                mPlusClient.connect();
            }
        } else {
            mGooglePlusAuthCallback.onLogin();
        }
    }

    /**
     * A helper method to flip the mResolveOnFail flag and start the resolution
     * of the ConnenctionResult from the failed connect() call.
     */
    private void startResolution() {

        try {
            // Don't start another resolution now until we have a
            // result from the activity we're about to start.
            mResolveOnFail = false;
            // If we can resolve the error, then call start resolution
            // and pass it an integer tag we can use to track. This means
            // that when we get the onActivityResult callback we'll know
            // its from being started here.
            mConnectionResult
                            .startResolutionForResult(mActivity, CONNECTION_UPDATE_ERROR);
        } catch (final SendIntentException e) {
            // Any problems, just try to connect() again so we get a new
            // ConnectionResult.
            mPlusClient.connect();
        }
    }

    /**
     * Call this method to retrieve the access token.
     */
    public String getAccessToken() {

        return mAccessToken;
    }

    public void logout(final boolean revokeAccess) {

        mAccessToken = null;
        if (mPlusClient.isConnected()) {
            mPlusClient.clearDefaultAccount();

            if (revokeAccess) {
                mPlusClient.revokeAccessAndDisconnect(new OnAccessRevokedListener() {

                    @Override
                    public void onAccessRevoked(final ConnectionResult status) {

                        mPlusClient.connect();
                        if (mGooglePlusAuthCallback != null) {
                            mGooglePlusAuthCallback.onLogout();
                        }

                    }
                });
            }

            else {
                mPlusClient.disconnect();
                mPlusClient.connect();
                if (mGooglePlusAuthCallback != null) {
                    mGooglePlusAuthCallback.onLogout();
                }
            }

        }

        else {
            if (mGooglePlusAuthCallback != null) {
                mGooglePlusAuthCallback.onLogout();
            }
        }
    }

    public boolean isLoggedIn() {

        return !TextUtils.isEmpty(mAccessToken);
    }

    /**
     * Callback for Google Plus login
     * 
     * @author Vinay S Shenoy
     */
    public static interface GooglePlusAuthCallback {

        /**
         * Method called when login is completed
         */
        public void onLogin();

        /**
         * Method called when there was an exception during login
         * 
         * @param error The {@link Exception} that was raised
         */
        public void onLoginError(Exception error);

        /**
         * Method called when logout was done
         */
        public void onLogout();
    }

}
