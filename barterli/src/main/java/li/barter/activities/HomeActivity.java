/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
  * limitations under the License.
 */

package li.barter.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.android.volley.Request.Method;
import com.facebook.AppEventsLogger;
import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.BooksAroundMeFragment;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.SharedPreferenceHelper;

/**
 * @author Vinay S Shenoy Main Activity for holding the Navigation Drawer and manages loading
 *         different fragments/options menus on Navigation items clicked
 */
@ActivityTransition(createEnterAnimation = R.anim.main_activity_launch,
                    createExitAnimation = R.anim.launch_zoom_out,
                    destroyEnterAnimation = R.anim.exit_zoom_in,
                    destroyExitAnimation = R.anim.main_activity_exit)
public class HomeActivity extends AbstractDrawerActivity implements
        LocationListener {

    private static final String TAG = "HomeActivity";

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper mGooglePlayClientWrapper;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, isMultipane() ? R.id.frame_side_content : R.id.frame_nav_drawer, isMultipane());
        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this,
                                                               this);

        if (isMultipane()) {
            setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar
                    .DISPLAY_SHOW_HOME);
        }
        if (savedInstanceState == null) {
            loadBooksAroundMeFragment();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlayClientWrapper.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
        if (DeviceInfo.INSTANCE.isNetworkConnected()) {
            informReferralToServer();
        }
    }

    /**
     * Informs the referral to server if it exists
     */
    private void informReferralToServer() {

        final String referrer = SharedPreferenceHelper
                .getString(R.string.pref_referrer);

        if (!TextUtils.isEmpty(referrer)) {

            final JSONObject requestObject = new JSONObject();

            try {
                requestObject.put(HttpConstants.REFERRAL_ID,
                                  referrer);
                requestObject.put(HttpConstants.DEVICE_ID,
                                  UserInfo.INSTANCE
                                          .getDeviceId()
                );

                final BlRequest request = new BlRequest(Method.POST,
                                                        HttpConstants.getApiBaseUrl()
                                                                + ApiEndpoints.REFERRAL,
                                                        requestObject.toString(),
                                                        mVolleyCallbacks
                );
                request.setRequestId(RequestId.REFERRAL);
                addRequestToQueue(request,
                                  false,
                                  0,
                                  true);
            } catch (JSONException e) {
            }

        }
    }

    @Override
    protected void onStop() {
        mGooglePlayClientWrapper.onStop();
        super.onStop();
    }

    @Override
    public void onLocationChanged(final Location location) {
        DeviceInfo.INSTANCE.setLatestLocation(location);
        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if (fragment instanceof BooksAroundMeFragment) {
            ((BooksAroundMeFragment) fragment).updateLocation(location);
        }
    }

    /**
     * Loads the {@link BooksAroundMeFragment} into the fragment container
     */
    public void loadBooksAroundMeFragment() {

        loadFragment(R.id.frame_content,
                     (AbstractBarterLiFragment) Fragment
                             .instantiate(this,
                                          BooksAroundMeFragment.class
                                                  .getName(),
                                          null
                             ),
                     FragmentTags.BOOKS_AROUND_ME,
                     false,
                     null
        );

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (isMultipane() && item.getItemId() == android.R.id.home) {

            /* If it's a multipane layout, the drawer is already loaded
            *  and the drawer toggle is disabled. */
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Object getTaskTag() {
        /* Here, hashCode() might show an Ambiguous method call bug. It's a bug in IntelliJ IDEA 13
        * http://youtrack.jetbrains.com/issue/IDEA-72835 */
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {

        if (requestId == RequestId.REFERRAL) {
            SharedPreferenceHelper.removeKeys(this,
                                              R.string.pref_referrer);
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request,
                                  final int errorCode,
                                  final String errorMessage,
                                  final Bundle errorResponseBundle) {

    }

    @Override
    protected String getAnalyticsScreenName() {

        /*
         * We don't want to track this Activity since it is an empty activity
         * for controlling fragments. Instead, we will track the fragments
         * themselves
         */
        return "";
    }

    @Override
    protected boolean isDrawerActionBarToggleEnabled() {

        if (isMultipane()) {

            return false;
        }
        return true;
    }
}
