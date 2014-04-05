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
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;

import li.barter.http.IVolleyHelper;
import li.barter.utils.AppConstants;
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

    private ImageLoader         mImageLoader;

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
        overrideHardwareMenuButton();
        VolleyLog.sDebug = AppConstants.DEBUG;
        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue);
        readUserInfoFromSharedPref();
        Utils.setupNetworkInfo(this);
    };

    /**
     * Reads the previously fetched auth token from Shared Preferencesand stores
     * it in the Singleton for in memory access
     */
    private void readUserInfoFromSharedPref() {

        UserInfo.INSTANCE.setAuthToken(SharedPreferenceHelper
                        .getString(this, R.string.pref_auth_token));
        UserInfo.INSTANCE.setId(SharedPreferenceHelper
                        .getString(this, R.string.pref_user_id));
        UserInfo.INSTANCE.setEmail(SharedPreferenceHelper
                        .getString(this, R.string.pref_email));
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

    /*@Override
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }*/

}
