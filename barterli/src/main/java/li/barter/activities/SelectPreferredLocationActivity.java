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
import android.view.View;
import android.widget.FrameLayout;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.SelectPreferredLocationFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.Utils;

/**
 * Activity to allow users to select their preferred location
 * <p/>
 * Created by vinay.shenoy on 11/07/14.
 */
@ActivityTransition(createEnterAnimation = R.anim.slide_in_from_right, createExitAnimation = R.anim.zoom_out, destroyEnterAnimation = R.anim.zoom_in, destroyExitAnimation = R.anim.slide_out_to_right)
public class SelectPreferredLocationActivity extends AbstractDrawerActivity {

    /**
     * Framelayout for adding overlay views
     */
    private FrameLayout mOverlayFrameLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);
        mOverlayFrameLayout = (FrameLayout) findViewById(R.id.frame_overlay);
        if (savedInstanceState == null) {
            loadSelectPreferredLocationFragment();
        }
    }

    /**
     * Displays an overlay view
     *
     * @param view TODO: Animate the view in
     */
    public void showOverlayView(View view) {

        if (Utils.containsChild(mOverlayFrameLayout, view)) {
            return;
        }
        mOverlayFrameLayout.addView(view);
        mOverlayFrameLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the overlay view TODO: Animate the view out
     */
    public void hideOverlayView() {
        mOverlayFrameLayout.removeAllViews();
        mOverlayFrameLayout.setVisibility(View.GONE);
    }

    /** Loads the Select Preferred fragment into the screen */
    private void loadSelectPreferredLocationFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment.instantiate(
                             this, SelectPreferredLocationFragment.class.getName(),
                             getIntent().getExtras()),
                     AppConstants.FragmentTags.SELECT_PREFERRED_LOCATION, false, null
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
