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

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import li.barter.BarterLiApplication;
import li.barter.chat.AbstractRabbitMQConnector.ExchangeType;
import li.barter.chat.AbstractRabbitMQConnector.OnDisconnectCallback;
import li.barter.chat.ChatProcessTask.Builder;
import li.barter.chat.ChatProcessTask.SendChatCallback;
import li.barter.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableChatMessages;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.IVolleyHelper;
import li.barter.http.NetworkChangeReceiver;
import li.barter.http.ResponseInfo;
import li.barter.http.VolleyCallbacks;
import li.barter.http.VolleyCallbacks.IHttpCallbacks;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.ChatStatus;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;

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
                IHttpCallbacks, OnDisconnectCallback, AsyncDbQueryCallback {

    private static final String    TAG                      = "ChatService";
    private static final String    QUEUE_NAME_FORMAT        = "%s%s";
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

    private static final String    MESSAGE_SELECT_BY_ID     = BaseColumns._ID
                                                                            + SQLConstants.EQUALS_ARG;

    private final IBinder          mChatServiceBinder       = new ChatServiceBinder();

    /** {@link ChatRabbitMQConnector} instance for listening to messages */
    private ChatRabbitMQConnector  mMessageConsumer;

    private DateFormatter          mChatDateFormatter;

    private DateFormatter          mMessageDateFormatter;

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

    private Handler                mHandler;

    private Runnable               mConnectRunnable;

    private Builder                mChatProcessTaskBuilder;

    /**
     * Single thread executor to process incoming chat messages in a queue
     */
    private ExecutorService        mChatProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        mChatDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.CHAT_TIME_FORMAT);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.MESSAGE_TIME_FORMAT);
        mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
        mVolleyCallbacks = new VolleyCallbacks(mRequestQueue, this);
        mCurrentConnectMultiplier = 0;
        mHandler = new Handler();
        mChatProcessor = Executors.newSingleThreadExecutor();
        mChatProcessTaskBuilder = new Builder(this);
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

                    mQueueName = generateQueueNameFromUserEmailAndDeviceId(UserInfo.INSTANCE
                                    .getEmail(), UserInfo.INSTANCE
                                    .getDeviceId());

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
        mChatProcessor.shutdownNow();
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
     */
    public void sendMessageToUser(final String toUserId, final String message,
                    final String timeSentAt) {

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

            final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                            .setProcessType(ChatProcessTask.PROCESS_SEND)
                            .setMessage(requestObject.toString())
                            .setMessageDateFormatter(mMessageDateFormatter)
                            .setChatDateFormatter(mChatDateFormatter)
                            .setSendChatCallback(new SendChatCallback() {

                                @Override
                                public void sendChat(String text,

                                long dbRowId) {

                                    final BlRequest request = new BlRequest(Method.POST, HttpConstants
                                                    .getChangedChatUrl()
                                                    + ApiEndpoints.AMPQ_EVENT_MACHINE, text, mVolleyCallbacks);
                                    request.setRequestId(RequestId.AMPQ);
                                    request.addExtra(Keys.ID, dbRowId);
                                    request.setTag(TAG);

                                    request.getRetryPolicy().setMaxRetries(0); //Don't retry chat requests

                                    //Post on main thread
                                    mHandler.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            mVolleyCallbacks.queue(request, true);
                                        }
                                    });
                                }
                            }).build();

            mChatProcessTaskBuilder.reset();
            mChatProcessor.submit(chatProcessTask);

        } catch (final JSONException e) {
            e.printStackTrace();
            //Should never happen
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
        ChatNotificationHelper.getInstance(this)
                        .setNotificationsEnabled(enabled);
    }

    /**
     * Uses the portion of the user's email before the "@" to generate the queue
     * name
     * 
     * @param userEmail The user email
     * @param deviceId The device Id
     * @return The queue name for the user email
     */
    private String generateQueueNameFromUserEmailAndDeviceId(
                    final String userEmail, final String deviceId) {

        final String emailPart1 = userEmail
                        .substring(0, userEmail.indexOf("@"));
        Logger.d(TAG, "User email part 1 %s", emailPart1);
        return String.format(Locale.US, QUEUE_NAME_FORMAT, deviceId, emailPart1);

    }

    @Override
    public void onReceiveMessage(final byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Logger.d(TAG, "Received:" + text);

            final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                            .setProcessType(ChatProcessTask.PROCESS_RECEIVE)
                            .setMessage(text)
                            .setChatDateFormatter(mChatDateFormatter)
                            .setMessageDateFormatter(mMessageDateFormatter)
                            .build();
            mChatProcessTaskBuilder.reset();
            mChatProcessor.submit(chatProcessTask);

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            //Shouldn't be happening
        }

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
            Logger.v(TAG, "Chat sent");
        }
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

        if (requestId == RequestId.AMPQ) {

            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));

            markChatAsFailed(messageDbId);
        }
    }

    @Override
    public void onAuthError(final int requestId,
                    final IBlRequestContract request) {

        if (requestId == RequestId.AMPQ) {

            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));

            markChatAsFailed(messageDbId);
        }
    }

    @Override
    public void onOtherError(final int requestId,
                    final IBlRequestContract request, final int errorCode) {

        if (requestId == RequestId.AMPQ) {

            final long messageDbId = (Long) (request.getExtras().get(Keys.ID));

            markChatAsFailed(messageDbId);
        }
    }

    /**
     * @param messageDbId The database row of the locally inserted chat message
     */
    private void markChatAsFailed(long messageDbId) {
        final ContentValues values = new ContentValues(1);
        values.put(DatabaseColumns.CHAT_STATUS, ChatStatus.FAILED);

        DBInterface.updateAsync(QueryTokens.UPDATE_MESSAGE_STATUS, hashCode(), null, TableChatMessages.NAME, values, MESSAGE_SELECT_BY_ID, new String[] {
            String.valueOf(messageDbId)
        }, true, this);
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

    /*
     * (non-Javadoc)
     * @see
     * li.barter.data.DBInterface.AsyncDbQueryCallback#onInsertComplete(int,
     * java.lang.Object, long)
     */
    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * li.barter.data.DBInterface.AsyncDbQueryCallback#onDeleteComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see
     * li.barter.data.DBInterface.AsyncDbQueryCallback#onUpdateComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see li.barter.data.DBInterface.AsyncDbQueryCallback#onQueryComplete(int,
     * java.lang.Object, android.database.Cursor)
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

}
