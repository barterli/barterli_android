package li.barter.utils;

import li.barter.activities.AbstractBarterLiActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.plus.Plus;

public class GooglePlusClientWrapper  implements ConnectionCallbacks,
OnConnectionFailedListener{
	
	private static final String            TAG              = "GooglePlusClientWrapper";
	
	public  GoogleApiClient           mGoogleApiClient;
	
	private final AbstractBarterLiActivity mActivity;
	
	private ConnectionResult               mConnectionResult;
	
	public GooglePlusClientWrapper(final AbstractBarterLiActivity activity) {
		mActivity = activity;
		mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Plus.API, null)
		.addScope(Plus.SCOPE_PLUS_LOGIN)
		.build();
	}


	/* Request code used to invoke sign in user interactions. */
	private static final int RC_SIGN_IN = 0;

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

	public void onStart() {
		
			mGoogleApiClient.connect();
	}

	public void onStop() {
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}

	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	public void handleActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {


			case RC_SIGN_IN:
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
	



	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(final Bundle dataBundle) {
		// Display the connection status
		if(mGoogleApiClient.isConnected()||mGoogleApiClient.isConnecting())
		{
			Logger.d(TAG, "google connect", "GOOGLE");
		}
	}


	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(final ConnectionResult connectionResult) {
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

}




