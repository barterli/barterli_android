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

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.BooksAroundMeFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;

/**
 * @author Vinay S Shenoy Main Activity for holding the Navigation Drawer and
 *         manages loading different fragments/options menus on Navigation items
 *         clicked
 */
public class HomeActivity extends AbstractBarterLiActivity {

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        initDrawer(R.id.drawer_layout, R.id.list_nav_drawer, true);
        if (savedInstanceState == null) {
            loadBooksAroundMeFragment();
        }

    }

    /**
     * Loads the {@link BooksAroundMeFragment} into the fragment container
     */
    private void loadBooksAroundMeFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                        .instantiate(this, BooksAroundMeFragment.class
                                        .getName(), null), FragmentTags.BOOKS_AROUND_ME, false, null);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        
    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        
    }

}
