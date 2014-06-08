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

package li.barter.activities;

import com.android.volley.Request.Method;
import com.crashlytics.android.Crashlytics;
import com.facebook.AppEventsLogger;
import com.google.android.gms.location.LocationListener;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Intent;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.BooksAroundMeFragment;
import li.barter.fragments.ChatDetailsFragment;
import li.barter.fragments.ChatsFragment;
import li.barter.fragments.LoginFragment;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.GooglePlusManager;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.GooglePlusManager.GooglePlusAuthCallback;

/**
 * @author Vinay S Shenoy Main Activity for holding the Navigation Drawer and
 *         manages loading different fragments/options menus on Navigation items
 *         clicked
 */
public class HomeActivity extends AbstractBarterLiActivity implements
                LocationListener, GooglePlusAuthCallback {

    private static final String     TAG = "HomeActivity";

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper mGooglePlayClientWrapper;

    /**
     * Helper class for connectiong to GooglePlus for login
     */
    private GooglePlusManager       mGooglePlusManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
        setContentView(R.layout.activity_home);
        setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        initDrawer(R.id.drawer_layout, R.id.list_nav_drawer, true);
        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);
        mGooglePlusManager = new GooglePlusManager(this, this);
        if (savedInstanceState == null) {

            final String action = getIntent().getAction();

            if (action == null) {
                loadBooksAroundMeFragment();
            } else if (action.equals(AppConstants.ACTION_SHOW_ALL_CHATS)) {
                loadChatsFragment();
            } else if (action.equals(AppConstants.ACTION_SHOW_CHAT_DETAIL)) {
                loadChatDetailFragment(getIntent().getStringExtra(Keys.CHAT_ID), getIntent()
                                .getStringExtra(Keys.USER_ID));
            } else {
                loadBooksAroundMeFragment();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mGooglePlayClientWrapper.onStart();
        mGooglePlusManager.onActivityStarted();

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
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
                        .getString(this, R.string.pref_referrer);

        if (!TextUtils.isEmpty(referrer)) {

            final JSONObject requestObject = new JSONObject();

            try {
                requestObject.put(HttpConstants.REFERRAL_ID, referrer);
                requestObject.put(HttpConstants.DEVICE_ID, UserInfo.INSTANCE
                                .getDeviceId());

                final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                                + ApiEndpoints.REFERRAL, requestObject.toString(), mVolleyCallbacks);
                request.setRequestId(RequestId.REFERRAL);
                addRequestToQueue(request, false, 0, true);
            } catch (JSONException e) {
            }

        }
    }

    @Override
    protected void onStop() {
        mGooglePlayClientWrapper.onStop();
        mGooglePlusManager.onActivityStopped();
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
     * Loads the {@link ChatsFragment} into the fragment container
     */
    private void loadChatsFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                        .instantiate(this, ChatsFragment.class.getName(), null), FragmentTags.CHATS, false, null);

    }

    /**
     * Loads the {@link ChatDetailsFragment} into the fragment container
     * 
     * @param chatId The chat detail to load
     * @param userId The user Id of the user with which the current user is
     *            chatting
     */
    private void loadChatDetailFragment(final String chatId, final String userId) {

        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(userId)) {
            finish();
        }

        final Bundle args = new Bundle(2);
        args.putString(Keys.CHAT_ID, chatId);
        args.putString(Keys.USER_ID, userId);
        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class.getName(), args), FragmentTags.BOOKS_AROUND_ME, false, null);

    }

    /**
     * Loads the {@link BooksAroundMeFragment} into the fragment container
     */
    public void loadBooksAroundMeFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                        .instantiate(this, BooksAroundMeFragment.class
                                        .getName(), null), FragmentTags.BOOKS_AROUND_ME, false, null);

    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

        if (requestId == RequestId.REFERRAL) {
            SharedPreferenceHelper.removeKeys(this, R.string.pref_referrer);
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                    final Intent data) {

        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                        && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }

    @Override
    public void onLogin() {

        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && (fragment instanceof LoginFragment)) {
            ((LoginFragment) fragment).onGoogleLogin();
        }
    }

    @Override
    public void onLoginError(final Exception error) {
        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && (fragment instanceof LoginFragment)) {
            ((LoginFragment) fragment).onGoogleLoginError(error);
        }
    }

    @Override
    public void onLogout() {
        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && (fragment instanceof LoginFragment)) {
            ((LoginFragment) fragment).onGoogleLogout();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);

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

}
