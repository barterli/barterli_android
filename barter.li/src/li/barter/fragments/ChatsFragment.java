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

package li.barter.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import li.barter.R;
import li.barter.activities.HomeActivity;
import li.barter.adapters.ChatsAdapter;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewChatsWithMessagesAndUsers;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;

/**
 * Activity for displaying all the ongoing chats
 * 
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ChatsFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, OnItemClickListener, ServiceConnection {

    private static final String TAG = "ChatsFragment";

    private ChatsAdapter        mChatsAdapter;

    private ListView            mChatsListView;

    private ChatService         mChatService;

    private boolean             mBoundToChatService;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarTitle(R.string.chat_fragment_title);
        final View view = inflater
                        .inflate(R.layout.fragment_chats, container, false);
        mChatsListView = (ListView) view.findViewById(R.id.list_chats);
        mChatsAdapter = new ChatsAdapter(getActivity(), null);
        mChatsListView.setAdapter(mChatsAdapter);
        mChatsListView.setOnItemClickListener(this);
        setActionBarDrawerToggleEnabled(false);
        getLoaderManager().restartLoader(Loaders.ALL_CHATS, null, this);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBoundToChatService) {
            mChatService.setNotificationsEnabled(true);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    };

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        if (id == Loaders.ALL_CHATS) {
            return new SQLiteLoader(getActivity(), false, ViewChatsWithMessagesAndUsers.NAME, null, null, null, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                            + SQLConstants.DESCENDING, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.ALL_CHATS) {
            mChatsAdapter.swapCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.ALL_CHATS) {
            mChatsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

        if (parent.getId() == R.id.list_chats) {

            final Cursor cursor = (Cursor) mChatsAdapter.getItem(position);

            final Bundle args = new Bundle(2);
            args.putString(Keys.CHAT_ID, cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.CHAT_ID)));
            args.putString(Keys.USER_ID, cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.USER_ID)));

            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), ChatDetailsFragment.class
                                            .getName(), args), FragmentTags.CHAT_DETAILS, true, null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {

                final int backStackEntryCount = getFragmentManager()
                                .getBackStackEntryCount();
                if (backStackEntryCount == 0) {
                    ((HomeActivity) getActivity()).loadBooksAroundMeFragment();
                    return true;
                }

                else {
                    onUpNavigate();
                    return true;
                }
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onServiceConnected(final ComponentName name,
                    final IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
        mChatService.clearChatNotifications();
        mChatService.setNotificationsEnabled(false);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    protected String getAnalyticsScreenName() {
        return "Chats";
    }
}
