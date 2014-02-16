package com.barterli.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Vinay S Shenoy Utility methods for barter.li
 */
public class UtilityMethods {

	/**
	 * This method returns whether the device is connected to a network
	 * 
	 * @return <code>true</code> if connected, <code>false</code> otherwise
	 */
	public static boolean isNetworkConnected(final Context context) {
		final ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
		if ((activeNetwork != null) && activeNetwork.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
}
