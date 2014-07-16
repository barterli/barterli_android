/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.LoginFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.GooglePlusManager;

/**
 * Activity to perform methods to Login & reset the user's password.
 *
 * The Activity will return an RESULT_OK as result code if it was successful.
 * <p/>
 * Created by vinay.shenoy on 09/07/14.
 */
@ActivityTransition(createEnterAnimation = R.anim.slide_in_from_right, createExitAnimation = R.anim.zoom_out, destroyEnterAnimation = R.anim.zoom_in, destroyExitAnimation = R.anim.slide_out_to_right)
public class AuthActivity extends AbstractDrawerActivity implements GooglePlusManager
        .GooglePlusAuthCallback {

    public static final String TAG = "AuthActivity";


    /**
     * Helper class for connecting to GooglePlus for login
     */
    private GooglePlusManager mGooglePlusManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, isMultipane() ? R.id.frame_side_content : R.id.frame_nav_drawer, isMultipane());
        mGooglePlusManager = new GooglePlusManager(this, this);
        if (savedInstanceState == null) {
            loadLoginFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlusManager.onActivityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGooglePlusManager.onActivityStopped();
    }

    /** Load the fragment for login */
    private void loadLoginFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                             .instantiate(this, LoginFragment.class.getName(), getIntent().getExtras()),
                     AppConstants.FragmentTags.LOGIN, false, null
        );
    }


    @Override
    protected boolean isDrawerActionBarToggleEnabled() {
        return false;
    }

    @Override
    protected String getAnalyticsScreenName() {
        return null;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId, final IBlRequestContract request,
                          final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId, final IBlRequestContract request,
                                  final int errorCode, final String errorMessage,
                                  final Bundle errorResponseBundle) {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        }
    }

    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }

    @Override
    public void onLogin() {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.LOGIN));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogin();
        }
    }

    @Override
    public void onLoginError(final Exception error) {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.LOGIN));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLoginError(error);
        }

    }

    @Override
    public void onLogout() {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.LOGIN));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogout();
        }
    }

}
