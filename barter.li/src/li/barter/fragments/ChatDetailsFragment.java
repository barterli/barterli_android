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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.HomeActivity;
import li.barter.adapters.ChatDetailAdapter;
import li.barter.chat.ChatAcknowledge;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
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
import li.barter.utils.Logger;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


/**
 * Activity for displaying Chat Messages
 * 
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ChatDetailsFragment extends AbstractBarterLiFragment implements
                ServiceConnection, LoaderCallbacks<Cursor>, OnClickListener,AsyncDbQueryCallback {

    private static final String     TAG            = "ChatDetailsFragment";

    private ChatDetailAdapter       mChatDetailAdapter;

    private ListView                mChatListView;

    private EditText                mSubmitChatEditText;

    private Button                  mSubmitChatButton;

    private ChatService             mChatService;

    private boolean                 mBoundToChatService;

    private boolean                 mFirstMessage;

    private final String            mChatSelection = DatabaseColumns.CHAT_ID
                                                                   + SQLConstants.EQUALS_ARG;

    private final String            mUserSelection = DatabaseColumns.USER_ID
                                                                   + SQLConstants.EQUALS_ARG;

    /**
     * The Id of the Chat
     */
    private String                  mChatId;

    /**
     * Id of the user with whom the current user is chatting
     */
    private String                  mWithUserId;

    /**
     * Implementation of {@link ConcreteChatAcknowledge} to receive
     * notifications when chat requests are complete
     */
    private ConcreteChatAcknowledge mAcknowledge;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarTitle(R.string.app_name);
        final View view = inflater
                        .inflate(R.layout.fragment_chat_details, container, false);
        /*
         * getActivity().getWindow()
         * .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
         * | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
         */
        mChatListView = (ListView) view.findViewById(R.id.list_chats);
        mChatDetailAdapter = new ChatDetailAdapter(getActivity(), null);
        mChatListView.setAdapter(mChatDetailAdapter);
        mChatId = getArguments().getString(Keys.CHAT_ID);
        mWithUserId = getArguments().getString(Keys.USER_ID);

        mSubmitChatEditText = (EditText) view
                        .findViewById(R.id.edit_text_chat_message);

        mSubmitChatButton = (Button) view.findViewById(R.id.button_send);
        mSubmitChatButton.setOnClickListener(this);

        setActionBarDrawerToggleEnabled(false);

        getLoaderManager().restartLoader(Loaders.CHAT_DETAILS, null, this);
        getLoaderManager().restartLoader(Loaders.USER_DETAILS, null, this);
        mAcknowledge = new ConcreteChatAcknowledge();

        if (savedInstanceState == null) {
            mFirstMessage = true;
        } else {
            mFirstMessage = savedInstanceState.getBoolean(Keys.FIRST_MESSAGE);
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.FIRST_MESSAGE, mFirstMessage);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {

                final int backStackEntryCount = getFragmentManager()

                .getBackStackEntryCount();
                if (backStackEntryCount == 0) {
                    ((HomeActivity) getActivity()).loadBooksAroundMeFragment();
                } else {
                    onUpNavigate();
                }
                return true;

            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAcknowledge.mChatDetailsFragment = null;
        if (mBoundToChatService) {
            mChatService.setCurrentChattingUserId(null);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAcknowledge.mChatDetailsFragment = this;
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected Object getVolleyTag() {
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
            return new SQLiteLoader(getActivity(), false, TableChatMessages.NAME, null, mChatSelection, new String[] {
                mChatId
            }, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                            + SQLConstants.ASCENDING, null);
        } else if (id == Loaders.USER_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, mUserSelection, new String[] {
                mWithUserId
            }, null, null, null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        final int id = loader.getId();
        if (id == Loaders.CHAT_DETAILS) {

            if (mFirstMessage && (cursor.getCount() == 0)) {
                //First chat message, autofill the edit text with the message
                mFirstMessage = false;
                final String bookTitle = getArguments()
                                .getString(Keys.BOOK_TITLE);

                if (!TextUtils.isEmpty(bookTitle)) {
                    mSubmitChatEditText
                                    .setText(getString(R.string.chat_opened_from, bookTitle));
                }
            }

            if ((mChatDetailAdapter.getCount() == 0) && (cursor.getCount() > 0)) {
                //Initial load. Swap cursor AND set position to last
                mChatDetailAdapter.swapCursor(cursor);
                mChatListView.setSelection(mChatDetailAdapter.getCount() - 1);
            } else {
                mChatDetailAdapter.swapCursor(cursor);
                if (mChatDetailAdapter.getCount() > 0) {

                    final int lastAdapterPosition = mChatDetailAdapter
                                    .getCount() - 1;

                    Logger.v(TAG, "Last Adapter Position %d and Last visible position %d", lastAdapterPosition, mChatListView
                                    .getLastVisiblePosition());
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

        } else if (id == Loaders.USER_DETAILS) {
            if (cursor.moveToFirst()) {
                final String profilePic = cursor
                                .getString(cursor
                                                .getColumnIndex(DatabaseColumns.PROFILE_PICTURE));

                mChatDetailAdapter.setChatUserProfilePic(profilePic);
            }
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

        if (v.getId() == R.id.button_send) {
            final String message = mSubmitChatEditText.getText().toString();

            if (!TextUtils.isEmpty(message)) {
                if (mBoundToChatService && mChatService.isConnectedToChat()) {
                  
        			SimpleDateFormat formatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
                  final String sentAt = formatter.format(new Date());
                    mChatService.sendMessageToUser(mWithUserId, message, mAcknowledge,sentAt);
                   
                    mSubmitChatEditText.setText(null);
                } else {
                    showCrouton(R.string.error_not_connected_to_chat_service, AlertStyle.ERROR);
                    
                }
            }
            
            
		

        }
    }

    /**
     * While a chat message is being sent, disable sending of any more chats
     * until the current one either fails or succeeds
     * 
     * @param enabled <code>true</code> to enable the actions,
     *            <code>false</code> to disable
     */
    private void setActionEnabled(final boolean enabled) {
        mSubmitChatEditText.setEnabled(enabled);
        mSubmitChatButton.setEnabled(enabled);
    }

    /**
     * Concrete implementation of {@link ChatAcknowledge} for receiving
     * callbacks when a sent chat message completes. The reason we are making a
     * concrete implementation is because the fragment can go to background or
     * get destroyed before the request completes(which is done in
     * {@link ChatService}). This class will act as a check to make sure the
     * fragment is still visible before updating the UI
     * 
     * @author Vinay S Shenoy
     */
    private static class ConcreteChatAcknowledge implements ChatAcknowledge {

        private ChatDetailsFragment mChatDetailsFragment;

        @Override
        public void onChatRequestComplete(final boolean success) {

            if ((mChatDetailsFragment != null)
                            && mChatDetailsFragment.isVisible()) {

                mChatDetailsFragment.onChatComplete(success);
            }
        }

    }

    /**
     * Whether the sent chat message was sent successfully or not
     * 
     * @param success <code>true</code> if the message was sent sucessfully,
     *            <code>false</code> otherwise
     */
    public void onChatComplete(final boolean success) {

        if (success) {
            //Clear the submit chat text since it was sent successfully
           //TODO
        } else {
            //Show error message
            showCrouton(R.string.error_unable_to_send_chat, AlertStyle.ERROR);
        }

        setActionEnabled(true);

    }

	@Override
	public void onInsertComplete(int token, Object cookie, long insertRowId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeleteComplete(int token, Object cookie, int deleteCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpdateComplete(int token, Object cookie, int updateCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}
}
