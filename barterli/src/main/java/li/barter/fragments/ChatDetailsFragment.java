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

package li.barter.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.UserProfileActivity;
import li.barter.adapters.ChatDetailAdapter;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableChatMessages;
import li.barter.data.TableUsers;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AvatarBitmapTransformation;
import li.barter.utils.Logger;
import li.barter.utils.Utils;
import li.barter.widgets.RoundedCornerImageView;

/**
 * Activity for displaying Chat Messages
 *
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
                    popEnterAnimation = R.anim.zoom_in,
                    popExitAnimation = R.anim.slide_out_to_right)
public class ChatDetailsFragment extends AbstractBarterLiFragment implements
        ServiceConnection, LoaderCallbacks<Cursor>, OnClickListener,
        AsyncDbQueryCallback, OnItemClickListener {

    private static final String TAG = "ChatDetailsFragment";

    private ChatDetailAdapter mChatDetailAdapter;

    private ListView mChatListView;

    private EditText mSubmitChatEditText;

    private ImageButton mSubmitChatButton;

    private ChatService mChatService;

    private boolean mBoundToChatService;

    private AvatarBitmapTransformation mAvatarBitmapTransformation;

    private final String mChatSelection = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    private final String mUserSelection = DatabaseColumns.USER_ID
            + SQLConstants.EQUALS_ARG;

    private final String mMessageSelection = BaseColumns._ID
            + SQLConstants.EQUALS_ARG;

    /**
     * The Id of the Chat
     */
    private String mChatId;

    /**
     * Id of the user with whom the current user is chatting
     */
    private String mWithUserId;

    /** Profile image of the user with whom the current user is chatting */
    private String mWithUserImage;

    /** Name of the user with whom the current user is chatting */
    private String mWithUserName;

    /**
     * User with whom the chat is happening
     */
    private RoundedCornerImageView mWithImageView;

    private SimpleDateFormat mFormatter;

    /** Bundle which contains the user info to load the chats for */
    private Bundle mUserInfo;

    /**
     * Whether the Activity should be finished on Back press. This will be used in 2 cases
     * <p/>
     * <ol> <li>When the chat screen is opened directly from a user's profile page. In this case,
     * pressing back shouldn't open the Chats list</li> <li> When the chats screen is opened in a
     * multipane layout</li> </ol>
     */
    private boolean mFinishOnBack;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarTitle(R.string.app_name);
        mAvatarBitmapTransformation = new AvatarBitmapTransformation(AvatarBitmapTransformation.AvatarSize.AB_CHAT);
        final View view = inflater
                .inflate(R.layout.fragment_chat_details, container, false);

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        mChatListView = (ListView) view.findViewById(R.id.list_chats);
        mChatDetailAdapter = new ChatDetailAdapter(getActivity(), null);
        mChatListView.setAdapter(mChatDetailAdapter);
        mChatListView.setOnItemClickListener(this);
        mSubmitChatEditText = (EditText) view
                .findViewById(R.id.edit_text_chat_message);

        mSubmitChatButton = (ImageButton) view.findViewById(R.id.button_send);
        mSubmitChatButton.setOnClickListener(this);


        if (savedInstanceState == null) {
            mUserInfo = getArguments();
        } else {
            mUserInfo = savedInstanceState.getBundle(Keys.USER_INFO);
        }

        if (getArguments() != null) {

            mFinishOnBack = getArguments().getBoolean(Keys.FINISH_ON_BACK);
        }

        loadChatMessages();

        return view;
    }

    @Override
    public boolean onBackPressed() {

        if (mFinishOnBack) {
            getActivity().finish();
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    /** Updates the chat details screen with a new user */
    public void updateUserInfo(Bundle userInfo) {
        mUserInfo = userInfo;
        loadChatMessages();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(Keys.USER_INFO, mUserInfo);
    }

    /** Loads the chat messages based on the User info bundle */
    private void loadChatMessages() {

        mChatId = mUserInfo.getString(Keys.CHAT_ID);
        mWithUserId = mUserInfo.getString(Keys.USER_ID);

        getLoaderManager().restartLoader(Loaders.CHAT_DETAILS, null, this);
        getLoaderManager()
                .restartLoader(Loaders.USER_DETAILS_CHAT_DETAILS, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_chat_details, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_user);

        final View actionView = MenuItemCompat.getActionView(menuItem);
        if (actionView != null) {
            mWithImageView = (RoundedCornerImageView) actionView
                    .findViewById(R.id.image_user);
            mWithImageView.setOnClickListener(this);
            loadUserInfoIntoActionBar();
        }
    }

    /**
     * Load the screen with whom the user is chatting
     */
    private void loadChattingWithUser() {

        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);
        userProfileIntent.putExtra(Keys.USER_ID, mWithUserId);
        startActivity(userProfileIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBoundToChatService) {
            mChatService.setCurrentChattingUserId(null);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
        mChatService.setCurrentChattingUserId(mWithUserId);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        if (id == Loaders.CHAT_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableChatMessages.NAME, null,
                                    mChatSelection, new String[]{
                    mChatId
            }, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                                            + SQLConstants.ASCENDING, null
            );
        } else if (id == Loaders.USER_DETAILS_CHAT_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, mUserSelection,
                                    new String[]{
                                            mWithUserId
                                    }, null, null, null, null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        final int id = loader.getId();
        if (id == Loaders.CHAT_DETAILS) {

            if ((mChatDetailAdapter.getCount() == 0) && (cursor.getCount() > 0)) {
                //Initial load. Swap cursor AND set position to last
                mChatDetailAdapter.swapCursor(cursor);
                mChatListView.setSelection(mChatDetailAdapter.getCount() - 1);
            } else {
                mChatDetailAdapter.swapCursor(cursor);
                if (mChatDetailAdapter.getCount() > 0) {

                    final int lastAdapterPosition = mChatDetailAdapter
                            .getCount() - 1;

                    Logger.v(TAG, "Last Adapter Position %d and Last visible position %d",
                             lastAdapterPosition, mChatListView
                                    .getLastVisiblePosition()
                    );
                    /*
                     * Smooth scroll only if there's already some data AND the
                     * last visible position is the last item in the adapter,
                     * i.e, don't scroll if a new message arrives while the user
                     * has scrolled down to view earlier messages
                     */
                    if ((lastAdapterPosition - 1) == mChatListView
                            .getLastVisiblePosition()) {
                        mChatListView.smoothScrollToPosition(lastAdapterPosition);
                    }
                }
            }

        } else if (id == Loaders.USER_DETAILS_CHAT_DETAILS) {
            if (cursor.moveToFirst()) {
                mWithUserImage = cursor
                        .getString(cursor
                                           .getColumnIndex(DatabaseColumns.PROFILE_PICTURE));

                final String firstName = cursor
                        .getString(cursor.getColumnIndex(DatabaseColumns.FIRST_NAME));
                final String lastName = cursor
                        .getString(cursor.getColumnIndex(DatabaseColumns.LAST_NAME));

                mWithUserName = Utils.makeUserFullName(firstName, lastName);
                loadUserInfoIntoActionBar();

            }
        }
    }

    /**
     * Loads the user image into the Action Bar profile pic
     */
    private void loadUserInfoIntoActionBar() {

        if (mWithImageView != null) {

            if (!TextUtils.isEmpty(mWithUserImage)) {
                Picasso.with(getActivity())
                       .load(mWithUserImage)
                       .error(R.drawable.pic_avatar)
                       .transform(mAvatarBitmapTransformation)
                       .into(mWithImageView.getTarget());
            }
        }

        if (!TextUtils.isEmpty(mWithUserName)) {
            setActionBarTitle(mWithUserName);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.CHAT_DETAILS) {
            mChatDetailAdapter.swapCursor(null);
        }
    }

    @Override
    public void onClick(final View v) {

        final int id = v.getId();

        if (id == R.id.button_send) {
            if (sendChatMessage(mSubmitChatEditText.getText().toString())) {
                mSubmitChatEditText.setText(null);
            } else {
                showCrouton(R.string.error_not_connected_to_chat_service, AlertStyle.ERROR);
            }
        } else if (id == R.id.image_user) {
            loadChattingWithUser();
        }
    }

    /**
     * Send a chat message to the user the current user is chatting with
     *
     * @param message The message to send.
     * @return <code>true</code> If the message was sent, <code>false</code> otherwise
     */
    private boolean sendChatMessage(String message) {

        boolean sent = true;
        if (!TextUtils.isEmpty(message)) {
            if (mBoundToChatService) {
                final String sentAt = mFormatter.format(new Date());
                mChatService.sendMessageToUser(mWithUserId, message, sentAt);
            } else {
                sent = false;
            }
        }
        return sent;

    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {

        if (token == QueryTokens.DELETE_CHAT_MESSAGE) {
            //Do nothing for now
        }
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

    @Override
    protected String getAnalyticsScreenName() {
        return Screens.CHAT_DETAILS;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        if (parent.getId() == R.id.list_chats) {

            final boolean resendOnClick = (Boolean) view
                    .getTag(R.string.tag_resend_on_click);

            if (resendOnClick) {
                final Cursor cursor = (Cursor) mChatDetailAdapter
                        .getItem(position);

                final int dbRowId = cursor.getInt(cursor
                                                          .getColumnIndex(BaseColumns._ID));
                final String message = cursor.getString(cursor
                                                                .getColumnIndex(
                                                                        DatabaseColumns.MESSAGE));

                if (sendChatMessage(message)) {
                    //Delete the older message from the table
                    DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGE, getTaskTag(), null,
                                            TableChatMessages.NAME, mMessageSelection, new String[]{
                                    String.valueOf(dbRowId)
                            }, true, this
                    );
                } else {
                    showCrouton(R.string.error_not_connected_to_chat_service, AlertStyle.ERROR);
                }
            }
        }
    }

}
