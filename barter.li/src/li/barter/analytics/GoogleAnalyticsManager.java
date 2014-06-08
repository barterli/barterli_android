/**
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
 */

package li.barter.analytics;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import android.content.Context;
import android.text.TextUtils;

import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.analytics.AnalyticsConstants.ParamKeys;
import li.barter.analytics.AnalyticsConstants.ParamValues;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.UserInfo;

/**
 * Class for managing Google Analytics
 * 
 * @author Vinay S Shenoy
 */
public class GoogleAnalyticsManager {

    private static final String           TAG             = "GoogleAnalyticsManager";
    private static final Object           LOCK            = new Object();
    public static final int               SESSION_TIMEOUT = 300;                     //seconds

    private static GoogleAnalyticsManager sInstance;

    private Tracker                       mApplicationTracker;

    private GoogleAnalyticsManager() {

        initTracker();
    }

    /**
     * @return the instance of {@link GoogleAnalyticsManager}, creating it if
     *         necessary
     */
    public static GoogleAnalyticsManager getInstance() {

        synchronized (LOCK) {

            if (sInstance == null) {
                synchronized (LOCK) {
                    sInstance = new GoogleAnalyticsManager();
                }
            }
        }

        return sInstance;
    }

    /**
     * Initialize the tracker
     */
    private void initTracker() {

        final Context context = BarterLiApplication.getStaticContext();

        if (!AppConstants.REPORT_GOOGLE_ANALYTICS) {
            GoogleAnalytics.getInstance(context).setDryRun(true);
            GoogleAnalytics.getInstance(context).getLogger()
                            .setLogLevel(Logger.LogLevel.VERBOSE);
        }
        mApplicationTracker = GoogleAnalytics.getInstance(context)
                        .newTracker(context.getString(R.string.ga_tracking_id));

        //We will track manually since we use Fragments
        mApplicationTracker.enableAutoActivityTracking(false);
        mApplicationTracker.setSessionTimeout(SESSION_TIMEOUT);

    }

    /**
     * Inform a screen hit to Google Analytics
     * 
     * @param screenName A name for the screen
     */
    public void sendScreenHit(String screenName) {

        mApplicationTracker.setScreenName(screenName);
        final ScreenViewBuilder screenViewBuilder = new ScreenViewBuilder();
        screenViewBuilder.set(ParamKeys.LOGGED_IN, TextUtils
                        .isEmpty(UserInfo.INSTANCE.getId()) ? ParamValues.NO
                        : ParamValues.YES);
        mApplicationTracker.send(screenViewBuilder.build());
    }

    /**
     * Inform an event to Google Analytics
     * 
     * @param builder
     */
    public void sendEvent(EventBuilder builder) {

        builder.set(ParamKeys.LOGGED_IN, TextUtils.isEmpty(UserInfo.INSTANCE
                        .getId()) ? ParamValues.NO : ParamValues.YES);
        mApplicationTracker.send(builder.build());
    }
}
