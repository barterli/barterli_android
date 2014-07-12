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

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import li.barter.R;
import li.barter.analytics.AnalyticsConstants;
import li.barter.fragments.SettingsFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;

/**
 * Activity to provide Settings
 * Created by vinay.shenoy on 05/07/14.
 *
 */
public class SettingsActivity extends AbstractBarterLiActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setActionBarTitle(R.string.action_settings);
        if(savedInstanceState == null) {
            loadSettingsFragment();
        }
    }

    /** Loads the Settings fragment into the View */
    private void loadSettingsFragment() {

        final FragmentManager fragmentManager = getFragmentManager();
        final SettingsFragment settingsFragment = (SettingsFragment) Fragment.instantiate(this, SettingsFragment.class.getName());
        final android.app.FragmentTransaction transaction = fragmentManager
                .beginTransaction();

        transaction.replace(R.id.frame_content, settingsFragment, AppConstants.FragmentTags.SETTINGS);
        transaction.commit();
    }

    @Override
    protected String getAnalyticsScreenName() {
        return AnalyticsConstants.Screens.SETTINGS;
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
}
