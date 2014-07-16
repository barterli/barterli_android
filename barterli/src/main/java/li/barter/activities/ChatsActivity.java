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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.MenuItem;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.ChatsFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;

/**
 * Activity that displays a list of chats and take care of swapping between the chats and chat
 * detail fragments
 * <p/>
 * Created by vinay.shenoy on 13/07/14.
 */
@ActivityTransition(createEnterAnimation = R.anim.slide_in_from_right, createExitAnimation = R.anim.zoom_out, destroyEnterAnimation = R.anim.zoom_in, destroyExitAnimation = R.anim.slide_out_to_right)
public class ChatsActivity extends AbstractDrawerActivity {

    public static final String ACTION_LOAD_CHAT = "li.barter.ACTION_LOAD_CHAT";

    /** User id to load a chat for immediately after loading chats list */
    private String mUserIdToLoad;

    /** Whether a chat should be loaded instantly */
    private boolean mShouldLoadChat;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, isMultipane() ? R.id.frame_side_content : R.id.frame_nav_drawer, isMultipane());

        if (savedInstanceState == null) {
            checkIfShouldLoadChat();
            loadChatsListFragment();
        }


    }

    /** Checks if a particular chat should be loaded immediately */
    private void checkIfShouldLoadChat() {

        final String action = getIntent().getAction();

        if (action != null && action.equals(ACTION_LOAD_CHAT)) {

            mUserIdToLoad = getIntent().getStringExtra(AppConstants.Keys.USER_ID);

            if (!TextUtils.isEmpty(mUserIdToLoad)) {
                mShouldLoadChat = true;
            }

        }
    }

    /** Loads the chats fragment into the screen */
    private void loadChatsListFragment() {

        Bundle args = null;
        if (mShouldLoadChat) {
            args = new Bundle(2);
            args.putBoolean(AppConstants.Keys.LOAD_CHAT, true);
            args.putString(AppConstants.Keys.USER_ID, mUserIdToLoad);
        }
        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment.instantiate(this,
                                                                                         ChatsFragment.class
                                                                                                 .getName(),
                                                                                         args
                     ),
                     AppConstants.FragmentTags.CHATS, false, null
        );
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
}
