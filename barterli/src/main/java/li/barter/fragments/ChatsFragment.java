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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.HomeActivity;
import li.barter.adapters.ChatsAdapter;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.data.ViewChatsWithMessagesAndUsers;
import li.barter.fragments.dialogs.SingleChoiceDialogFragment;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.Logger;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

/**
 * Activity for displaying all the ongoing chats
 *
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ChatsFragment extends AbstractBarterLiFragment implements
LoaderCallbacks<Cursor>, OnItemClickListener,OnItemLongClickListener, ServiceConnection,AsyncDbQueryCallback {

	private static final String TAG = "ChatsFragment";

	private ChatsAdapter        mChatsAdapter;

	private ListView            mChatsListView;

	private ChatService         mChatService;

	private boolean             mBoundToChatService;

	private String 				mDeleteChatId,mBlockUserId;

	private final String        mChatSelectionForDelete = DatabaseColumns.CHAT_ID
			+ SQLConstants.EQUALS_ARG;



	/**
	 * Reference to the Dialog Fragment for selecting the chat options
	 */
	private SingleChoiceDialogFragment   mChatDialogFragment;

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
		mChatsListView.setOnItemLongClickListener(this);
		mChatDialogFragment = (SingleChoiceDialogFragment) getFragmentManager()
				.findFragmentByTag(FragmentTags.DIALOG_CHAT_LONGCLICK);
		getLoaderManager().restartLoader(Loaders.ALL_CHATS, null, this);
		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mBoundToChatService) {
			mChatService.setChatScreenVisible(true);
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
	protected Object getTaskTag() {
		return hashCode();
	}

	@Override
	public void onSuccess(final int requestId,
			final IBlRequestContract request,
			final ResponseInfo response) {

		if(requestId==RequestId.BLOCK_CHATS)
		{
			showCrouton(R.string.success_message_for_chatblock, AlertStyle.INFO);
		}


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
		mChatService.setChatScreenVisible(false);
	}

	@Override
	public void onServiceDisconnected(final ComponentName name) {
		mBoundToChatService = false;
	}

	@Override
	protected String getAnalyticsScreenName() {
		return Screens.CHATS;
	}

	@Override
	public void onBackPressed() {

		final int backStackEntryCount = getFragmentManager()

				.getBackStackEntryCount();
		if (backStackEntryCount == 0) {
			((HomeActivity) getActivity()).loadBooksAroundMeFragment();

		} else {
			onUpNavigate();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
			long id) {

		final Cursor cursor = (Cursor) mChatsAdapter.getItem(position);


		mDeleteChatId= cursor.getString(cursor
				.getColumnIndex(DatabaseColumns.CHAT_ID));
		mBlockUserId=cursor.getString(cursor
				.getColumnIndex(DatabaseColumns.USER_ID));
		showChatOptions();
		return true;
	}

	/**
	 * Show dialog for adding books
	 */
	private void showChatOptions() {

		mChatDialogFragment = new SingleChoiceDialogFragment();
		mChatDialogFragment
		.show(AlertDialog.THEME_HOLO_LIGHT, R.array.chat_longclick_choices, 0, R.string.chat_longclick_dialog_head, getFragmentManager(), true, FragmentTags.DIALOG_CHAT_LONGCLICK);

	}

	@Override
	public boolean willHandleDialog(final DialogInterface dialog) {

		if ((mChatDialogFragment != null)
				&& mChatDialogFragment.getDialog().equals(dialog)) {
			return true;
		}
		return super.willHandleDialog(dialog);
	}

	@Override
	public void onDialogClick(final DialogInterface dialog, final int which) {

		if ((mChatDialogFragment != null)
				&& mChatDialogFragment.getDialog().equals(dialog)) {

			if (which == 0) {


				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

				// set title
				alertDialogBuilder.setTitle("Confirm");

				// set dialog message
				alertDialogBuilder
				.setMessage(getResources().getString(R.string.delete_chat_alert_message))
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							final DialogInterface dialog,
							final int id) {

						deleteChat(mDeleteChatId);
							dialog.dismiss();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

				// create alert dialog
				final AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();



			} else if (which == 1) {

				final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

				// set title
				alertDialogBuilder.setTitle("Confirm");

				// set dialog message
				alertDialogBuilder
				.setMessage(getResources().getString(R.string.block_user_alert_message))
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							final DialogInterface dialog,
							final int id) {

						blockUser(mBlockUserId);
							dialog.dismiss();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							final DialogInterface dialog,
							final int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
					}
				});

				// create alert dialog
				final AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();


			}
		} else {
			super.onDialogClick(dialog, which);
		}
	}

	private void deleteChat(String chatId)
	{
		DBInterface.deleteAsync(QueryTokens.DELETE_CHATS, getTaskTag(), null, TableChats.NAME, mChatSelectionForDelete, new String[] {
				chatId
		}, true, this);
		DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGES, getTaskTag(), null, TableChatMessages.NAME, mChatSelectionForDelete, new String[] {
				chatId
		}, true, this);

	}
	/**
	 * Method to block user on the server using the userId
	 *
	 * @param userId representing the user selected
	 */
	private void blockUser(final String userId) {

		final JSONObject requestObject = new JSONObject();
		try {
			requestObject.put(HttpConstants.USER_ID, userId);

		final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
				+ ApiEndpoints.CHAT_BLOCK, requestObject.toString(), mVolleyCallbacks);
		request.setRequestId(RequestId.BLOCK_CHATS);

		final Map<String, String> params = new HashMap<String, String>(1);
		params.put(HttpConstants.USER_ID, userId);
		Logger.d(TAG, userId);
		request.setParams(params);
		addRequestToQueue(request, true, R.string.error_block_user, true);
		} catch (JSONException e) {
			Logger.e(TAG, e, "Error building create user json");
		}
	}


	@Override
	public void onInsertComplete(int token, Object cookie, long insertRowId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeleteComplete(int token, Object cookie, int deleteCount) {
		switch (token) {

		case QueryTokens.DELETE_CHAT_MESSAGES:

			//add after delete features
			break;

		case QueryTokens.DELETE_CHATS:

			//add after delete features
			break;

		default:
			break;
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
}
