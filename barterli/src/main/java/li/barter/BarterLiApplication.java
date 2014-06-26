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

package li.barter;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import li.barter.chat.ChatService;
import li.barter.http.IVolleyHelper;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

/**
 * Custom Application class which holds some common functionality for the
 * Application
 * 
 * @author Vinay S Shenoy
 */
public class BarterLiApplication extends Application implements IVolleyHelper {

    private static final String TAG = "BarterLiApplication";

    /**
     * Maintains a reference to the application context so that it can be
     * referred anywhere wihout fear of leaking. It's a hack, but it works.
     */
    private static Context      sStaticContext;

    private RequestQueue        mRequestQueue;

    /**
     * Gets a reference to the application context
     */
    public static Context getStaticContext() {
        if (sStaticContext != null) {
            return sStaticContext;
        }

        //Should NEVER hapen
        throw new RuntimeException("No static context instance");
    }

    @Override
    public void onCreate() {

        sStaticContext = getApplicationContext();
        if (!SharedPreferenceHelper
                        .getBoolean(R.string.pref_migrated_from_alpha)
                        && (SharedPreferenceHelper
                                        .getInt(R.string.pref_last_version_code) == 0)) {
            doMigrationFromAlpha();
            SharedPreferenceHelper.set(R.string.pref_migrated_from_alpha, true);
        }
        /*
         * Saves the current app version into shared preferences so we can use
         * it in a future update if necessary
         */
        saveCurrentAppVersionIntoPreferences();
        if (AppConstants.ENABLE_CRASHLYTICS) {
            Crashlytics.start(this);
        }

        overrideHardwareMenuButton();
        VolleyLog.sDebug = AppConstants.DEBUG;

        mRequestQueue = Volley.newRequestQueue(this);
        // Picasso.with(this).setDebugging(true);
        UserInfo.INSTANCE.setDeviceId(Secure.getString(this
                        .getContentResolver(), Secure.ANDROID_ID));
        readUserInfoFromSharedPref();
        Utils.setupNetworkInfo(this);
        if (DeviceInfo.INSTANCE.isNetworkConnected()) {
            startChatService();
        }

    };

    /**
     * Save the current app version info into preferences. This is purely for
     * future use where we might need to use these values on an app update
     */
    private void saveCurrentAppVersionIntoPreferences() {
        try {
            PackageInfo info = getPackageManager()
                            .getPackageInfo(getPackageName(), 0);
            SharedPreferenceHelper
                            .set(R.string.pref_last_version_code, info.versionCode);
            SharedPreferenceHelper
                            .set(R.string.pref_last_version_name, info.versionName);
        } catch (NameNotFoundException e) {
            //Shouldn't happen
        }
    }

    /**
     * This migrates the locally cached data from alpha. The only thing this is
     * doing currently is clearing the Shared preferences
     */
    private void doMigrationFromAlpha() {
        SharedPreferenceHelper.clearPreferences(this);
    }

    /**
     * Reads the previously fetched auth token from Shared Preferencesand stores
     * it in the Singleton for in memory access
     */
    private void readUserInfoFromSharedPref() {

        UserInfo.INSTANCE.setAuthToken(SharedPreferenceHelper
                        .getString(R.string.pref_auth_token));
        UserInfo.INSTANCE.setId(SharedPreferenceHelper
                        .getString(R.string.pref_user_id));
        UserInfo.INSTANCE.setEmail(SharedPreferenceHelper
                        .getString(R.string.pref_email));
        UserInfo.INSTANCE.setProfilePicture(SharedPreferenceHelper
                        .getString(R.string.pref_profile_image));
        UserInfo.INSTANCE.setFirstName(SharedPreferenceHelper
                        .getString(R.string.pref_first_name));
    }

    /**
     * Some device manufacturers are stuck in the past and stubbornly use H/W
     * menu buttons, which is deprecated since Android 3.0. This breaks the UX
     * on newer devices since the Action Bar overflow just doesn't show. This
     * little hack tricks the Android OS into thinking that the device doesn't
     * have a permanant menu button, and hence the Overflow button gets shown.
     * This doesn't disable the Menu button, however. It will continue to
     * function as normal, so the users who are already used to it will be able
     * to use it as before
     */
    private void overrideHardwareMenuButton() {
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class
                            .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (final Exception ex) {
            // Ignore since we can't do anything
        }

    }

    @Override
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatService() {

        final Intent intent = new Intent(sStaticContext, ChatService.class);
        sStaticContext.startService(intent);
    }

}
