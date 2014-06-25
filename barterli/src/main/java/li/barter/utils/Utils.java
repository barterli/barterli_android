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

import com.google.android.gms.internal.cn;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.utils.AppConstants.DeviceInfo;

/**
 * @author Vinay S Shenoy Utility methods for barter.li
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Reads the network info from service and sets up the singleton
     */
    public static void setupNetworkInfo(final Context context) {

        final ConnectivityManager connManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            DeviceInfo.INSTANCE.setNetworkConnected(activeNetwork
                            .isConnectedOrConnecting());
            DeviceInfo.INSTANCE.setCurrentNetworkType(activeNetwork.getType());
        } else {
            DeviceInfo.INSTANCE.setNetworkConnected(false);
            DeviceInfo.INSTANCE
                            .setCurrentNetworkType(ConnectivityManager.TYPE_DUMMY);
        }

        Logger.d(TAG, "Network State Updated Connected: %b Type: %d", DeviceInfo.INSTANCE
                        .isNetworkConnected(), DeviceInfo.INSTANCE
                        .getCurrentNetworkType());
    }

    /**
     * Checks if the current thread is the main thread or not
     * 
     * @return <code>true</code> if the current thread is the main/UI thread,
     *         <code>false</code> otherwise
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Makes an SHA1 Hash of the given string
     * 
     * @param string The string to shash
     * @return The hashed string
     * @throws NoSuchAlgorithmException
     */
    public static String sha1(final String string)
                    throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        final byte[] data = digest.digest(string.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static void emailDatabase(final Context context) {

        final File databaseExt = new File(Environment.getExternalStorageDirectory(), "barterli.sqlite");

        if (copyFile(new File("/data/data/li.barter/databases/barterli.sqlite"), databaseExt)) {
            sendEmail(context, databaseExt);
        }

    }

    public static boolean copyFile(final File src, final File dst) {
        boolean returnValue = true;

        FileChannel inChannel = null, outChannel = null;

        try {

            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();

        } catch (final FileNotFoundException fnfe) {

            Logger.d(TAG, "inChannel/outChannel FileNotFoundException");
            fnfe.printStackTrace();
            return false;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (final IllegalArgumentException iae) {

            Logger.d(TAG, "TransferTo IllegalArgumentException");
            iae.printStackTrace();
            returnValue = false;

        } catch (final NonReadableChannelException nrce) {

            Logger.d(TAG, "TransferTo NonReadableChannelException");
            nrce.printStackTrace();
            returnValue = false;

        } catch (final NonWritableChannelException nwce) {

            Logger.d(TAG, "TransferTo NonWritableChannelException");
            nwce.printStackTrace();
            returnValue = false;

        } catch (final ClosedByInterruptException cie) {

            Logger.d(TAG, "TransferTo ClosedByInterruptException");
            cie.printStackTrace();
            returnValue = false;

        } catch (final AsynchronousCloseException ace) {

            Logger.d(TAG, "TransferTo AsynchronousCloseException");
            ace.printStackTrace();
            returnValue = false;

        } catch (final ClosedChannelException cce) {

            Logger.d(TAG, "TransferTo ClosedChannelException");
            cce.printStackTrace();
            returnValue = false;

        } catch (final IOException ioe) {

            Logger.d(TAG, "TransferTo IOException");
            ioe.printStackTrace();
            returnValue = false;

        } finally {

            if (inChannel != null) {
                try {

                    inChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return returnValue;
    }

    private static void sendEmail(final Context context, final File attachment) {

        if (Environment.getExternalStorageState()
                        .equals(Environment.MEDIA_MOUNTED)) {
            final Uri path = Uri.fromFile(attachment);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("application/octet-stream");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "barter.li database");
            final String to[] = {
                "vinaysshenoy@gmail.com"
            };
            intent.putExtra(Intent.EXTRA_EMAIL, to);
            intent.putExtra(Intent.EXTRA_TEXT, "Database");
            intent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(intent, "Send mail..."));
        } else {
            Logger.e(TAG, "Not mounted");
        }

    }

    /**
     * Gets the distance between two Locations(in metres)
     * 
     * @param start The start location
     * @param end The end location
     * @return The distance between two locations(in metres)
     */
    public static float distanceBetween(final Location start, final Location end) {

        final float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end
                        .getLatitude(), end.getLongitude(), results);
        return results[0];
    }

    /**
     * Gets the current epoch time. Is dependent on the device's H/W time.
     */
    public static long getCurrentEpochTime() {

        return System.currentTimeMillis() / 1000;
    }

    /**
     * hether a screen hit should be reported
     * 
     * @param lastScreenSeenTime The time which the screen was last seen
     * @return <code>true</code> if as screen hit should be reported,
     *         <code>false</code> otherwise
     */
    public static boolean shouldReportScreenHit(final long lastScreenSeenTime) {

        /*
         * last screen time holds the epoch time at which this particular
         * fragment last went to the background. In the case of a newly created
         * fragment, this will be 0 and hence, the condition will be true. In a
         * case of a screen that underwent a rotation change, the last screen
         * time will be very close to the current time, and hence the condition
         * will be false, so the screen hit report won't go through. In the case
         * that the screen was in the background, and it got destroyed due to
         * memory shortage, the screen hit report will go through only if it has
         * crossed the session timeout
         */

        return ((System.currentTimeMillis() - lastScreenSeenTime) >= GoogleAnalyticsManager.SESSION_TIMEOUT);
    }

    /**
     * Generates as chat ID which will be unique for a given sender/receiver
     * pair
     * 
     * @param receiverId The receiver of the chat
     * @param senderId The sender of the chat
     * @return The chat Id
     */
    public static String generateChatId(final String receiverId,
                    final String senderId) {

        /*
         * Method of generating the chat ID is simple. First we compare the two
         * ids and combine them in ascending order separate by a '#'. Then we
         * SHA1 the result to make the chat id
         */

        String combined = null;
        if (receiverId.compareTo(senderId) < 0) {
            combined = String
                            .format(Locale.US, AppConstants.CHAT_ID_FORMAT, receiverId, senderId);
        } else {
            combined = String
                            .format(Locale.US, AppConstants.CHAT_ID_FORMAT, senderId, receiverId);
        }

        String hashed = null;

        try {
            hashed = Utils.sha1(combined);
        } catch (final NoSuchAlgorithmException e) {
            /*
             * Shouldn't happen sinch SHA-1 is standard, but in case it does use
             * the combined string directly since they are local chat IDs
             */
            hashed = combined;
        }

        return hashed;
    }

    /**
     * Generate a user's name from the first name last name
     * 
     * @param firstName
     * @param lastName
     * @return
     */
    public static String makeUserFullName(String firstName, String lastName) {

        if (TextUtils.isEmpty(firstName)) {
            return "";
        }

        final StringBuilder builder = new StringBuilder(firstName);

        if (!TextUtils.isEmpty(lastName)) {
            builder.append(" ").append(lastName);
        }
        return builder.toString();
    }

    /**
     * Test whether a viewgroup already contains a specific instance of a child
     * view
     * 
     * @param viewGroup
     * @param view
     * @return
     */
    public static boolean containsChild(ViewGroup viewGroup, View view) {

        boolean contains = false;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            if (viewGroup.getChildAt(i).equals(view)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Creates a share intent
     * 
     * @param context
     * @param shareText The text to share
     * @return
     */
    public static Intent createShareIntent(Context context,
                    final String shareText) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context
                        .getString(R.string.subject));
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);

        shareIntent.setType("text/plain");

        return shareIntent;
    }

    /**
     * Creates an intent for sharing the app
     * 
     * @param context
     * @return
     */
    public static final Intent createAppShareIntent(Context context) {

        final String referralId = SharedPreferenceHelper
                        .getString(context, R.string.pref_share_token);
        String appShareUrl = context.getString(R.string.app_share_message)
                        .concat(AppConstants.PLAY_STORE_LINK);

        if (!TextUtils.isEmpty(referralId)) {
            appShareUrl = appShareUrl
                            .concat(String.format(Locale.US, AppConstants.REFERRER_FORMAT, referralId));
        }

        return createShareIntent(context, appShareUrl);
    }

}
