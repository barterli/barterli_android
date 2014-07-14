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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import li.barter.R;
import li.barter.fragments.AboutUsPagerFragment;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;

/**
 * Activity for displaying info about barter.li
 * <p/>
 * Created by vinay.shenoy on 12/07/14.
 */
@ActivityTransition(createEnterAnimation = R.anim.slide_in_from_right, createExitAnimation = R.anim.zoom_out, destroyEnterAnimation = R.anim.zoom_in, destroyExitAnimation = R.anim.slide_out_to_right)
public class AboutUsActivity extends AbstractDrawerActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);
        if (savedInstanceState == null) {
            loadAboutUsFragment();
        }
    }

    /**
     * Loads the About Us Pager fragment into the view
     */
    private void loadAboutUsFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment.instantiate(this,
                                                                                         AboutUsPagerFragment.class
                                                                                                 .getName()
                     ),
                     AppConstants.FragmentTags.ABOUT_US, false, null
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
    public void onBadRequestError(final int requestId, final IBlRequestContract request, final
    int errorCode, final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
