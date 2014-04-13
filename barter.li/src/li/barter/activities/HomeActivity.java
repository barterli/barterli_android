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
import android.text.TextUtils;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.BooksAroundMeFragment;
import li.barter.fragments.ChatDetailsFragment;
import li.barter.fragments.ChatsFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;

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

            final String action = getIntent().getAction();
            
            if(action == null) {
                loadBooksAroundMeFragment();
            } else if(action.equals(AppConstants.ACTION_SHOW_ALL_CHATS)) {
                loadChatsFragment();
            } else if(action.equals(AppConstants.ACTION_SHOW_CHAT_DETAIL)) {
                loadChatDetailFragment(getIntent()
                                .getStringExtra(Keys.CHAT_ID), getIntent()
                                .getStringExtra(Keys.USER_ID));
            } else {
                loadBooksAroundMeFragment();
            }
                
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
                        .instantiate(this, ChatDetailsFragment.class.getName(), null), FragmentTags.BOOKS_AROUND_ME, false, null);

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
