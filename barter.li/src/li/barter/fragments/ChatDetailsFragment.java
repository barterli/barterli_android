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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import li.barter.R;
import li.barter.adapters.ChatDetailAdapter;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableChatMessages;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;

/**
 * Activity for displaying Chat Messages
 * 
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ChatDetailsFragment extends AbstractBarterLiFragment implements
                ServiceConnection, LoaderCallbacks<Cursor> {

    private static final String TAG            = "ChatFragment";

    private ChatDetailAdapter   mChatDetailAdapter;

    private ListView            mChatListView;

    private ChatService         mChatService;

    private boolean             mBoundToChatService;

    private final String        mChatSelection = DatabaseColumns.CHAT_ID
                                                               + SQLConstants.EQUALS_ARG;

    private String              mChatId;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        final View view = inflater
                        .inflate(R.layout.fragment_chat_details, container, false);
        mChatListView = (ListView) view.findViewById(R.id.list_chats);
        mChatDetailAdapter = new ChatDetailAdapter(getActivity(), null);
        mChatListView.setAdapter(mChatDetailAdapter);
        mChatId = getArguments().getString(Keys.CHAT_ID);
        setActionBarDrawerToggleEnabled(false);
        getLoaderManager().restartLoader(Loaders.CHAT_DETAILS, null, this);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(activity, ChatService.class);
        activity.bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        if (mBoundToChatService) {
            getActivity().unbindService(this);
        }
        super.onDetach();
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == Loaders.CHAT_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableChatMessages.NAME, null, mChatSelection, new String[] {
                mChatId
            }, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                            + SQLConstants.DESCENDING, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if(loader.getId() == Loaders.CHAT_DETAILS) {
            mChatDetailAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if(loader.getId() == Loaders.CHAT_DETAILS) {
            mChatDetailAdapter.swapCursor(null);
        }
    }
}
