/**
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
 */

package li.barter.chat;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.activities.HomeActivity;
import li.barter.chat.AbstractRabbitMQConnector.ExchangeType;
import li.barter.chat.AbstractRabbitMQConnector.OnDisconnectCallback;
import li.barter.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.data.TableUsers;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.IVolleyHelper;
import li.barter.http.JsonUtils;
import li.barter.http.NetworkChangeReceiver;
import li.barter.http.ResponseInfo;
import li.barter.http.VolleyCallbacks;
import li.barter.http.VolleyCallbacks.IHttpCallbacks;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.ChatStatus;
import li.barter.utils.AppConstants.ChatType;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;
import li.barter.utils.Utils;

/**
 * Bound service to send and receive chat messages. The service will receive
 * chat messages and update them in the chats database. <br/>
 * <br/>
 * This service needs to be triggered in two cases -
 * <ol>
 * <li>On application launch - This is done in
 * {@link BarterLiApplication#onCreate()}</li>
 * <li>On network connectivity resumed(if it was lost) - This is done in
 * {@link NetworkChangeReceiver#onReceive(Context, Intent)}</li>
 * </ol>
 * <br/>
 * This will take care of keeping it tied to the chat server and listening for
 * messages. <br/>
 * <br/>
 * For publishing messages, however, you need to bind to this service, check if
 * chat is connected and then publish the message
 * 
 * @author Vinay S Shenoy
 */
public class ChatService extends Service implements OnReceiveMessageHandler,
                AsyncDbQueryCallback, IHttpCallbacks, OnDisconnectCallback {

    private static final String    TAG                      = "ChatService";
    private static final String    OUTPUT_TIME_FORMAT       = "dd MMM, h:mm a";
    private static final String    QUEUE_NAME_FORMAT        = "%squeue";
    private static final String    VIRTUAL_HOST             = "/";
    private static final String    EXCHANGE_NAME_FORMAT     = "%sexchange";
    private static final String    USERNAME                 = "barterli";
    private static final String    PASSWORD                 = "barter";
    /**
     * Minimum time interval(in seconds) to wait between subsequent connect
     * attempts
     */
    private static final int       CONNECT_BACKOFF_INTERVAL = 5;

    /**
     * Maximum multiplier for the connect interval
     */
    private static final int       MAX_CONNECT_MULTIPLIER   = 180;

    private final IBinder          mChatServiceBinder       = new ChatServiceBinder();

    private final String           mChatSelection           = DatabaseColumns.CHAT_ID
                                                                            + SQLConstants.EQUALS_ARG;

    private final String           mUserSelection           = DatabaseColumns.USER_ID
                                                                            + SQLConstants.EQUALS_ARG;

    /** {@link ChatRabbitMQConnector} instance for listening to messages */
    private ChatRabbitMQConnector  mMessageConsumer;

    private DateFormatter          mDateFormatter;

    private RequestQueue           mRequestQueue;

    private VolleyCallbacks        mVolleyCallbacks;

    private String                 mQueueName;

    /**
     * Current multiplier for connecting to chat. Can vary between 0 to
     * {@link #MAX_CONNECT_MULTIPLIER}
     */
    private int                    mCurrentConnectMultiplier;
    /**
     * Task to connect to Rabbit MQ Chat server
     */
    private ConnectToChatAsyncTask mConnectTask;

    /**
     * Holds an array of chat messages, to be processed in order
     */
    private ArrayDeque<String>     mMessageQueue;

    private Handler                mHandler;

    private Runnable               mConnectRunnable;

    /**
     * Whether any message is being currently processed
     */
    private boolean                mIsProcessingMessage;

    private String                 mCurrentMessage;

    /**
     * Single thread executor to process incoming chat messages in a queue
     */
    private ExecutorService        mChatProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        mMessageQueue = new ArrayDeque<String>();
        mDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, OUTPUT_TIME_FORMAT);
        mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
        mVolleyCallbacks = new VolleyCallbacks(mRequestQueue, this);
        mCurrentConnectMultiplier = 0;
        mHandler = new Handler();
        mChatProcessor = Executors.newSingleThreadExecutor();
        mIsProcessingMessage = false;
    }

    /**
     * Sets the id of the user the current chat is being done with. Set this to
     * the user id when the chat detail screen opens, and clear it when the
     * screen is paused. It is used to hide notifications when the chat message
     * received is from the user currently being chatted with
     * 
     * @param currentChattingUserId The id of the current user being chatted
     *            with
     */
    public void setCurrentChattingUserId(final String currentChattingUserId) {
        ChatNotificationHelper.getInstance(this)
                        .setCurrentChattingUserId(currentChattingUserId);
    }

    /**
     * Binder to connect to the Chat Service
     * 
     * @author Vinay S Shenoy
     */
    public class ChatServiceBinder extends Binder {

        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mChatServiceBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                    final int startId) {

        final String action = intent != null ? intent.getAction() : null;

        if ((action != null)
                        && action.equals(AppConstants.ACTION_DISCONNECT_CHAT)) {

            if (isConnectedToChat()) {

                mMessageConsumer.dispose(true);
                mMessageConsumer = null;
            }
        } else {
            mCurrentConnectMultiplier = 0;
            initMessageConsumer();
            connectChatService();
        }

        return START_STICKY;
    }

    /**
     * Connects to the Chat Service
     */
    private void connectChatService() {

        //If there already is a pending connect task, remove it since we have a newer one
        if (mConnectRunnable != null) {
            mHandler.removeCallbacks(mConnectRunnable);
        }
        if (isLoggedIn() && !mMessageConsumer.isRunning()) {

            mConnectRunnable = new Runnable() {

                @Override
                public void run() {

                    if (!isLoggedIn()
                                    || !DeviceInfo.INSTANCE
                                                    .isNetworkConnected()) {

                        //If there is no internet connection or we are not logged in, we need not attempt to connect
                        mConnectRunnable = null;
                        return;
                    }

                    final String string = UserInfo.INSTANCE.getEmail();
                    final String[] parts = string.split("@");
                    mQueueName = UserInfo.INSTANCE.getDeviceId() + parts[0];/*
                                                                             * generateQueueNameFromUserId
                                                                             * (
                                                                             * UserInfo
                                                                             * .
                                                                             * INSTANCE
                                                                             * .
                                                                             * getId
                                                                             * (
                                                                             * )
                                                                             * )
                                                                             * ;
                                                                             */
                    if (mConnectTask == null) {
                        mConnectTask = new ConnectToChatAsyncTask();
                        mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                        .getId());
                    } else {
                        final Status connectingStatus = mConnectTask
                                        .getStatus();

                        if (connectingStatus != Status.RUNNING) {

                            // We are not already attempting to connect, let's try connecting
                            if (connectingStatus == Status.PENDING) {
                                //Cancel a pending task
                                mConnectTask.cancel(false);
                            }

                            mConnectTask = new ConnectToChatAsyncTask();
                            mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                            .getId());
                        }
                    }
                    mConnectRunnable = null;

                }

            };

            mHandler.postDelayed(mConnectRunnable, mCurrentConnectMultiplier
                            * CONNECT_BACKOFF_INTERVAL * 1000);
            mCurrentConnectMultiplier = (++mCurrentConnectMultiplier > MAX_CONNECT_MULTIPLIER) ? MAX_CONNECT_MULTIPLIER
                            : mCurrentConnectMultiplier;
        }

    }

    /**
     * Check if user is logged in or not
     */
    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getId());
    }

    @Override
    public void onDestroy() {
        if (isConnectedToChat()) {
            mMessageConsumer.dispose(true);
            mMessageConsumer = null;
        }
        mVolleyCallbacks.cancelAll(TAG);
        super.onDestroy();
    }

    /**
     * Is the chat service connected or not
     */
    public boolean isConnectedToChat() {

        return (mMessageConsumer != null) && mMessageConsumer.isRunning();
    }

    /**
     * Send a message to a user
     * 
     * @param toUserId The user Id to send the message to
     * @param message The message to send
     * @param acknowledge An implementation of {@link ChatAcknowledge} to be
     *            notified when the chat request completes
     */
    public void sendMessageToUser(final String toUserId, final String message,
                    final ChatAcknowledge acknowledge, final String timeSentAt) {

        if (!isLoggedIn()) {
            return;
        }
        final JSONObject requestObject = new JSONObject();
        try {

            String senderId = UserInfo.INSTANCE.getId();
            String receiverId = toUserId;
            requestObject.put(HttpConstants.SENDER_ID, senderId);
            requestObject.put(HttpConstants.RECEIVER_ID, receiverId);
            requestObject.put(HttpConstants.SENT_AT, timeSentAt);

            requestObject.put(HttpConstants.MESSAGE, message);
            final ChatRequest request = new ChatRequest(Method.POST, HttpConstants.getChangedChatUrl()
                            + ApiEndpoints.AMPQ_EVENT_MACHINE, requestObject.toString(), mVolleyCallbacks, acknowledge);
            request.setRequestId(RequestId.AMPQ);
            request.setTag(TAG);

            final String chatId = generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, message);
            chatValues.put(DatabaseColumns.SENT_AT, timeSentAt);
            chatValues.put(DatabaseColumns.CHAT_STATUS, ChatStatus.SENDING);

            chatValues.put(DatabaseColumns.TIMESTAMP, timeSentAt);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mDateFormatter
                            .getEpoch(timeSentAt));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mDateFormatter
                            .getOutputTimestamp(timeSentAt));

            DBInterface.insertAsync(QueryTokens.INSERT_CHAT_MESSAGE_LOCALLY, chatValues, TableChatMessages.NAME, null, chatValues, true, this);

            mVolleyCallbacks.queue(request, true);
        } catch (final JSONException e) {
            e.printStackTrace();
            //Should never happen
        } catch (final ParseException e) {
            Logger.e(TAG, e, "Invalid chat timestamp");
        }

    }

    /**
     * Cancels any notifications being displayed. Call this if the relevant
     * screen is opened within the app
     */
    public void clearChatNotifications() {

        ChatNotificationHelper.getInstance(this).clearChatNotifications();
    }

    /**
     * Set notifications enabled
     * 
     * @param enabled <code>true</code> to enable notifications,
     *            <code>false</code> to disable them
     */
    public void setNotificationsEnabled(final boolean enabled) {
        ChatNotificationHelper.getInstance(this).setNotificationsEnabled(enabled);
    }

    /**
     * Generates the queue name from the user Id
     * 
     * @param userId The user Id to generate the queue name for
     * @return The queue name for the user id
     */
    private String generateQueueNameFromUserId(final String userId) {
        return String.format(Locale.US, QUEUE_NAME_FORMAT, userId);
    }

    @Override
    public void onReceiveMessage(final byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Logger.d(TAG, "Received:" + text);

            mChatProcessor.submit(new ChatProcessTask(this, text, mDateFormatter));
           /* mMessageQueue.add(text);
            //queueNextMessageForProcessing();
            if (!mIsProcessingMessage) {
                //If there aren't any messages in the queue, process the message immediately
                queueNextMessageForProcessing();
            }*/

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            //Shouldn't be happening
        }

    }

    /**
     * Processes a received chat message
     * 
     * @param message The chat message
     */
    private void updateChatMessage(final String message) {
        try {
            final JSONObject messageJson = new JSONObject(message);
            final JSONObject senderObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.SENDER, true, true);
            final JSONObject receiverObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.RECEIVER, true, true);
            final String senderId = JsonUtils
                            .readString(senderObject, HttpConstants.ID_USER, true, true);
            final String sentAtTime = JsonUtils
                            .readString(messageJson, HttpConstants.SENT_AT, true, true);
            final String receiverId = JsonUtils
                            .readString(receiverObject, HttpConstants.ID_USER, true, true);
            final String messageText = JsonUtils
                            .readString(messageJson, HttpConstants.MESSAGE, true, true);
            final String timestamp = JsonUtils
                            .readString(messageJson, HttpConstants.TIME, true, true);

            //Insert the chat message into DB
            final String selection = DatabaseColumns.SENDER_ID
                            + SQLConstants.EQUALS_ARG + SQLConstants.AND
                            + DatabaseColumns.MESSAGE + SQLConstants.EQUALS_ARG
                            + SQLConstants.AND + DatabaseColumns.SENT_AT
                            + SQLConstants.EQUALS_ARG;

            final String[] args = new String[] {
                    senderId, messageText, sentAtTime
            };

            final String chatId = generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.CHAT_STATUS, ChatStatus.SENT);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mDateFormatter
                            .getEpoch(timestamp));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mDateFormatter
                            .getOutputTimestamp(timestamp));

            //First try to update the table if a book already exists
            DBInterface.updateAsync(QueryTokens.UPDATE_CHAT_MESSAGE, null, TableChatMessages.NAME, chatValues, selection, args, true, this);

        } catch (final JSONException e) {
            Logger.e(TAG, e, "Invalid Chat Json");
        } catch (final ParseException e) {
            Logger.e(TAG, e, "Invalid chat timestamp");
        }
    }

    /**
     * Processes a received chat message
     * 
     * @param message The chat message
     */
    private void processChatMessage(final String message) {
        try {
            final JSONObject messageJson = new JSONObject(message);
            final JSONObject senderObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.SENDER, true, true);
            final JSONObject receiverObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.RECEIVER, true, true);
            final String senderId = JsonUtils
                            .readString(senderObject, HttpConstants.ID_USER, true, true);
            final String sentAtTime = JsonUtils
                            .readString(messageJson, HttpConstants.SENT_AT, true, true);
            final String receiverId = JsonUtils
                            .readString(receiverObject, HttpConstants.ID_USER, true, true);
            final String messageText = JsonUtils
                            .readString(messageJson, HttpConstants.MESSAGE, true, true);
            final String timestamp = JsonUtils
                            .readString(messageJson, HttpConstants.TIME, true, true);

            final String chatId = generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mDateFormatter
                            .getEpoch(timestamp));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mDateFormatter
                            .getOutputTimestamp(timestamp));

            // Unable to update, insert the item
            DBInterface.insertAsync(QueryTokens.INSERT_CHAT_MESSAGE, chatValues, TableChatMessages.NAME, null, chatValues, true, this);

            /*
             * Parse and store sender info. We will receive messages both when
             * we send and receive, so we need to check the sender id if it is
             * our own id first to detect who sent the message
             */
            if (senderId.equals(UserInfo.INSTANCE.getId())) {
                parseAndStoreChatUserInfo(receiverId, receiverObject);
            } else {
                final String senderName = parseAndStoreChatUserInfo(senderId, senderObject);
                /*if ((mCurrentChattingUserId != null)
                                && mCurrentChattingUserId.equals(senderId)) {

                    
                     * Don't show notification if the user is currently chatting
                     * with this same user
                     
                    return;
                }
                showChatReceivedNotification(chatId, senderId, senderName, messageText);*/
            }

        } catch (final JSONException e) {
            Logger.e(TAG, e, "Invalid Chat Json");
        } catch (final ParseException e) {
            Logger.e(TAG, e, "Invalid chat timestamp");
        }
    }

    /**
     * Parses the user info of the user who sent the message and updates the
     * local users table
     * 
     * @param senderId The id for the user who sent the chat message
     * @param senderObject The Sender object received in the chat message
     * @return The name of the sender
     * @throws JSONException If the JSON is invalid
     */
    private String parseAndStoreChatUserInfo(final String senderId,
                    final JSONObject senderObject) throws JSONException {

        final String senderFirstName = JsonUtils
                        .readString(senderObject, HttpConstants.FIRST_NAME, true, false);
        final String senderLastName = JsonUtils
                        .readString(senderObject, HttpConstants.LAST_NAME, true, false);
        final String senderImage = JsonUtils
                        .readString(senderObject, HttpConstants.PROFILE_IMAGE, true, false);

        final ContentValues senderValues = new ContentValues(4);
        senderValues.put(DatabaseColumns.USER_ID, senderId);
        senderValues.put(DatabaseColumns.FIRST_NAME, senderFirstName);
        senderValues.put(DatabaseColumns.LAST_NAME, senderLastName);
        senderValues.put(DatabaseColumns.PROFILE_PICTURE, senderImage);

        DBInterface.updateAsync(QueryTokens.UPDATE_USER_FOR_CHAT, senderValues, TableUsers.NAME, senderValues, mUserSelection, new String[] {
            senderId
        }, true, this);

        return String.format("%s %s", senderFirstName, senderLastName);
    }

    /**
     * Asynchronously connect to Chat Server TODO: Move the connect async task
     * to the Rabbit MQ Connector The execute() call requires 4 string params -
     * The username, password, queue name in the same order. All parameters
     * should be passed. Send an EMPTY STRING if not required
     * 
     * @author Vinay S Shenoy
     */
    private class ConnectToChatAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            //Validation
            assert (params != null);
            assert (params.length == 3);
            assert (params[0] != null);
            assert (params[1] != null);
            assert (params[2] != null);
            Logger.v(TAG, "Username %s, Password %s, Queue %s", params[0], params[1], params[2]);
            mMessageConsumer.connectToRabbitMQ(params[0], params[1], params[2], true, false, false, null);
            return null;
        }

        @Override
        protected void onPostExecute(final Void result) {
            if (!isConnectedToChat()) {
                /* If it's not connected, try connecting again */
                connectChatService();
            } else {
                mCurrentConnectMultiplier = 0;
            }
        }
    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                    final long insertRowId) {

        switch (token) {

            case QueryTokens.INSERT_CHAT_MESSAGE_LOCALLY:
            case QueryTokens.INSERT_CHAT_MESSAGE: {
                assert (cookie != null);
                assert (cookie instanceof ContentValues);

                if (insertRowId >= 0) {
                    Logger.v(TAG, "Inserted chat with row Id %d with cookie %s", insertRowId, cookie);
                    //TODO Show notification
                    //Try to update the Chats table
                    final ContentValues chatData = (ContentValues) cookie;
                    final String chatId = chatData
                                    .getAsString(DatabaseColumns.CHAT_ID);

                    final ContentValues values = new ContentValues(4);
                    values.put(DatabaseColumns.CHAT_ID, chatId);
                    values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                    values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);

                    final String senderId = chatData
                                    .getAsString(DatabaseColumns.SENDER_ID);
                    final String receiverId = chatData
                                    .getAsString(DatabaseColumns.RECEIVER_ID);

                    final boolean isSenderCurrentUser = senderId
                                    .equals(UserInfo.INSTANCE.getId());

                    values.put(DatabaseColumns.USER_ID, isSenderCurrentUser ? receiverId
                                    : senderId);

                    Logger.v(TAG, "Updating chats for Id %s", chatId);
                    DBInterface.updateAsync(QueryTokens.UPDATE_CHAT, values, TableChats.NAME, values, mChatSelection, new String[] {
                        chatId
                    }, true, this);
                } else {
                    //Rare case
                    Logger.e(TAG, "Unable to insert chat message");
                }
                break;
            }

            case QueryTokens.INSERT_CHAT: {
                assert (cookie != null);
                assert (cookie instanceof ContentValues);
                //Chat was successfully created
                mIsProcessingMessage = false;
                queueNextMessageForProcessing();
                break;
            }

            case QueryTokens.INSERT_USER_FOR_CHAT: {
                //Nothing to do here as of now
                break;
            }
        }

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                    final int deleteCount) {

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                    final int updateCount) {

        if (token == QueryTokens.UPDATE_CHAT) {
            assert (cookie != null);
            assert (cookie instanceof ContentValues);

            if (updateCount == 0) {
                //Unable to update chats table, create a row
                Logger.v(TAG, "Chat not found. Inserting %s", cookie);
                DBInterface.insertAsync(QueryTokens.INSERT_CHAT, cookie, TableChats.NAME, null, (ContentValues) cookie, true, this);
            } else {
                Logger.v(TAG, "Chat Updated!");
                mIsProcessingMessage = false;
                queueNextMessageForProcessing();
            }
        } else if (token == QueryTokens.UPDATE_USER_FOR_CHAT) {
            assert (cookie != null);
            assert (cookie instanceof ContentValues);

            if (updateCount == 0) {
                //Unable to update user, create the user
                Logger.v(TAG, "User not found. Inserting %s", cookie);
                DBInterface.insertAsync(QueryTokens.INSERT_USER_FOR_CHAT, cookie, TableUsers.NAME, null, (ContentValues) cookie, true, this);
            }
        } else if (token == QueryTokens.UPDATE_CHAT_MESSAGE) {
            if (updateCount == 0) {

                processChatMessage(mCurrentMessage);
                Logger.d(TAG, "added = " + mCurrentMessage);
            } else {
                Logger.d(TAG, "UPDATED = " + updateCount);
                mIsProcessingMessage = false;
                queueNextMessageForProcessing();
            }
        }
    }

    /**
     * Checks whether there are any pending messages in the queue, and adds them
     * for processing if there are
     */
    private void queueNextMessageForProcessing() {

        if ((mMessageQueue != null) && (mMessageQueue.peek() != null)) {
            mIsProcessingMessage = true;
            mCurrentMessage = mMessageQueue.poll();
            updateChatMessage(mCurrentMessage);
            //processChatMessage(mCurrentMessage);

        }
    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                    final Cursor cursor) {

    }

    /**
     * Generates as chat ID which will be unique for a given sender/receiver
     * pair
     * 
     * @param receiverId The receiver of the chat
     * @param senderId The sender of the chat
     * @return The chat Id
     */
    public static String generateChatId(final String receiverId,
                    final String senderId) {

        /*
         * Method of generating the chat ID is simple. First we compare the two
         * ids and combine them in ascending order separate by a '#'. Then we
         * SHA1 the result to make the chat id
         */

        String combined = null;
        if (receiverId.compareTo(senderId) < 0) {
            combined = String
                            .format(Locale.US, AppConstants.CHAT_ID_FORMAT, receiverId, senderId);
        } else {
            combined = String
                            .format(Locale.US, AppConstants.CHAT_ID_FORMAT, senderId, receiverId);
        }

        String hashed = null;

        try {
            hashed = Utils.sha1(combined);
        } catch (final NoSuchAlgorithmException e) {
            /*
             * Shouldn't happen sinch SHA-1 is standard, but in case it does use
             * the combined string directly since they are local chat IDs
             */
            hashed = combined;
        }

        return hashed;
    }

    @Override
    public void onPreExecute(final IBlRequestContract request) {

    }

    @Override
    public void onPostExecute(final IBlRequestContract request) {

    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

        if (requestId == RequestId.AMPQ) {

            if (request instanceof ChatRequest) {

                final ChatAcknowledge acknowledge = ((ChatRequest) request)
                                .getAcknowledge();

                if (acknowledge != null) {
                    acknowledge.onChatRequestComplete(true);
                }
            }
        }
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

        if (requestId == RequestId.AMPQ) {

            if (request instanceof ChatRequest) {

                final ChatAcknowledge acknowledge = ((ChatRequest) request)
                                .getAcknowledge();

                if (acknowledge != null) {
                    acknowledge.onChatRequestComplete(false);
                }
            }
        }
    }

    @Override
    public void onAuthError(final int requestId,
                    final IBlRequestContract request) {

        if (requestId == RequestId.AMPQ) {

            if (request instanceof ChatRequest) {

                final ChatAcknowledge acknowledge = ((ChatRequest) request)
                                .getAcknowledge();

                if (acknowledge != null) {
                    acknowledge.onChatRequestComplete(false);
                }
            }
        }
    }

    @Override
    public void onOtherError(final int requestId,
                    final IBlRequestContract request, final int errorCode) {

        if (requestId == RequestId.AMPQ) {

            if (request instanceof ChatRequest) {

                final ChatAcknowledge acknowledge = ((ChatRequest) request)
                                .getAcknowledge();

                if (acknowledge != null) {
                    acknowledge.onChatRequestComplete(false);
                }
            }
        }
    }

    @Override
    public void onDisconnect(final boolean manual) {
        if (!manual) {
            connectChatService();
        }
    }

    /**
     * Creates a new consumer
     */
    private void initMessageConsumer() {
        if ((mMessageConsumer == null) && isLoggedIn()) {
            mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                            .getChatPort(), VIRTUAL_HOST, String
                            .format(Locale.US, EXCHANGE_NAME_FORMAT, UserInfo.INSTANCE
                                            .getId()), ExchangeType.FANOUT);

            mMessageConsumer.setOnReceiveMessageHandler(ChatService.this);
            mMessageConsumer.setOnDisconnectCallback(ChatService.this);
        }

    }

}
