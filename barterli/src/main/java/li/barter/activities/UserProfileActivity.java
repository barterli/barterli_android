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
import android.text.TextUtils;
import android.view.MenuItem;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.ProfileFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;

/**
 * Class to show a User's profile. Send the user id with the key {@linkplain
 * li.barter.utils.AppConstants.Keys#USER_ID} in the bundle, and the Activity will take care of
 * showing the user info
 * <p/>
 * Created by vinay.shenoy on 10/07/14.
 */
@ActivityTransition(createEnterAnimation = R.anim.slide_in_from_right, createExitAnimation = R.anim.zoom_out, destroyEnterAnimation = R.anim.zoom_in, destroyExitAnimation = R.anim.slide_out_to_right)
public class UserProfileActivity extends AbstractDrawerActivity {

    /** The Id of the user to load */
    private String mUserId;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, isMultipane() ? R.id.frame_side_content : R.id.frame_nav_drawer, isMultipane());
        mUserId = getIntent().getStringExtra(AppConstants.Keys.USER_ID);
        if (TextUtils.isEmpty(mUserId)) {
            finish();
        } else if (savedInstanceState == null) {
            loadUserProfileFragment();
        }
    }

    /** Returns the user id */
    public String getUserId() {
        return mUserId;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Loads the user profile into the container
     */
    private void loadUserProfileFragment() {

        final Bundle userArgs = new Bundle(1);
        userArgs.putString(AppConstants.Keys.USER_ID, mUserId);
        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment.instantiate(this,
                                                                                         ProfileFragment.class
                                                                                                 .getName(),
                                                                                         userArgs
                     ),
                     AppConstants.FragmentTags.USER_PROFILE, false, null
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
    public void onSuccess(final int requestId, final IBlRequestContract request, final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId, final IBlRequestContract request, final int errorCode, final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
