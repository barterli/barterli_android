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

package li.barter.utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import li.barter.AbstractBarterLiActivity;

/**
 * @author Vinay S Shenoy Class to wrap the Google Play Connection code
 */
public class GooglePlayClientWrapper implements ConnectionCallbacks,
                OnConnectionFailedListener {

    private static final String            TAG              = "GooglePlayClientWrapper";

    // Update frequency in milliseconds
    private static final int               UPDATE_INTERVAL  = 5 * 60 * 1000;

    // A fast frequency ceiling in milliseconds
    private static final long              FASTEST_INTERVAL = 1 * 60 * 1000;

    private final LocationClient           mLocationClient;

    private final AbstractBarterLiActivity mActivity;

    private final LocationListener         mLocationListener;

    private ConnectionResult               mConnectionResult;

    private final LocationRequest          mLocationRequest;

    public GooglePlayClientWrapper(final AbstractBarterLiActivity activity,
                    final LocationListener locationListener) {
        mActivity = activity;
        mLocationClient = new LocationClient(mActivity, this, this);
        mLocationListener = locationListener;
        mLocationRequest = LocationRequest
                        .create()
                        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        .setInterval(UPDATE_INTERVAL)
                        .setFastestInterval(FASTEST_INTERVAL);
    }

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(final Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public void onActivityStart() {
        mLocationClient.connect();
    }

    public void onActivityStop() {

        if (servicesConnected() && mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(mLocationListener);
        }
        mLocationClient.disconnect();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    public void handleActivityResult(final int requestCode,
                    final int resultCode, final Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                 * If the result code is Activity.RESULT_OK, try to connect
                 * again
                 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        /*
                         * Try the request again
                         */
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        final int resultCode = GooglePlayServicesUtil
                        .isGooglePlayServicesAvailable(mActivity);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            final int errorCode = mConnectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                            errorCode, mActivity,
                            CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(mActivity.getSupportFragmentManager(),
                                "Location Updates");
            }

            return false;
        }
    }

    public boolean willHandleActivityResult(final int requestCode,
                    final int resultCode, final Intent data) {
        return requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST;
    }

    /*
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(final Bundle dataBundle) {
        // Display the connection status
        mLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationListener);
    }

    @Override
    public void onDisconnected() {
        // Display the connection status
    }

    /*
     * Called by Location Services if the attempt to Location Services fails.
     */
    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        mConnectionResult = connectionResult;
        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(mActivity,
                                CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (final IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the user with
             * the error.
             */
            // showErrorDialog(connectionResult.getErrorCode());

            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionResult.getErrorCode(), mActivity,
                            CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                final ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(mActivity.getSupportFragmentManager(),
                                "Location Updates");
            }
        }
    }

}
