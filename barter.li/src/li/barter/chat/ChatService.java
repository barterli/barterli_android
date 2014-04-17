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
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Locale;

import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.activities.HomeActivity;
import li.barter.chat.AbstractRabbitMQConnector.ExchangeType;
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
import li.barter.utils.AppConstants.ChatType;
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
                AsyncDbQueryCallback, IHttpCallbacks {

    private static final String    TAG                     = "ChatService";
    private static final String    OUTPUT_TIME_FORMAT      = "dd MMM, h:m a";
    private static final String    QUEUE_NAME_FORMAT       = "%squeue";
    private static final String    VIRTUAL_HOST            = "/";
    private static final String    EXCHANGE                = "node.barterli";
    private static final String    USERNAME                = "barterli";
    private static final String    PASSWORD                = "barter";

    /**
     * Notification Id for notifications related to messages
     */
    private static final int       MESSAGE_NOTIFICATION_ID = 1;

    private final IBinder          mChatServiceBinder      = new ChatServiceBinder();

    private final String           mChatSelection          = DatabaseColumns.CHAT_ID
                                                                           + SQLConstants.EQUALS_ARG;

    private final String           mUserSelection          = DatabaseColumns.USER_ID
                                                                           + SQLConstants.EQUALS_ARG;

    /** {@link ChatRabbitMQConnector} instance for listening to messages */
    private ChatRabbitMQConnector  mMessageConsumer;

    private DateFormatter          mDateFormatter;

    private RequestQueue           mRequestQueue;

    private VolleyCallbacks        mVolleyCallbacks;

    private String                 mQueueName;

    /**
     * Task to connect to Rabbit MQ Chat server
     */
    private ConnectToChatAsyncTask mConnectTask;

    private Builder                mNotificationBuilder;

    private NotificationManager    mNotificationManager;

    /**
     * Holds the number of unread received messages
     */
    private int                    mUnreadMessageCount;

    @Override
    public void onCreate() {
        super.onCreate();
        mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                        .getChatPort(), VIRTUAL_HOST, EXCHANGE, ExchangeType.DIRECT);
        mMessageConsumer.setOnReceiveMessageHandler(this);
        mDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, OUTPUT_TIME_FORMAT);
        mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
        mVolleyCallbacks = new VolleyCallbacks(mRequestQueue, this);
        mNotificationBuilder = new Builder(this);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mUnreadMessageCount = 0;

        //testNotifications();
    }

    private void testNotifications() {
        showChatReceivedNotification("Some crap1", "jsdjksncjdn", "Vinay S Shenoy", "I WANTZ THAT BOOKZ!!!");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                showChatReceivedNotification("Some crap2", "janckjdnc", "Some random idiot", "FUUUUUUUUUUU....");
            }
        }, 5000);
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
    public IBinder onBind(Intent intent) {
        return mChatServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isLoggedIn() && !mMessageConsumer.isRunning()) {

            mQueueName = generateQueueNameFromUserId(UserInfo.INSTANCE.getId());
            if (mConnectTask == null) {
                mConnectTask = new ConnectToChatAsyncTask();
                mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                .getId());
            } else {
                Status connectingStatus = mConnectTask.getStatus();

                if (connectingStatus != Status.RUNNING) {

                    // We are not already attempting to connect, let's try connecting
                    if (connectingStatus == Status.PENDING) {
                        //Cancel a pending task
                        mConnectTask.cancel(false);
                    }

                    mConnectTask = new ConnectToChatAsyncTask();
                    //TODO Use actual user id here
                    mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                    .getId());
                }
            }

        }
        return START_STICKY;
    }

    /**
     * Check if user is logged in or not
     */
    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getId());
    }

    @Override
    public void onDestroy() {
        if (mMessageConsumer.isRunning()) {
            mMessageConsumer.dispose();
            mMessageConsumer = null;
        }
        mVolleyCallbacks.cancelAll(TAG);
        super.onDestroy();
    }

    /**
     * Is the chat service connected or not
     */
    public boolean isConnectedToChat() {

        return mMessageConsumer.isRunning();
    }

    /**
     * Send a message to a user
     * 
     * @param toUserId The user Id to send the message to
     * @param message The message to send
     * @param acknowledge An implementation of {@link ChatAcknowledge} to be
     *            notified when the chat request completes
     */
    public void sendMessageToUser(String toUserId, String message,
                    ChatAcknowledge acknowledge) {

        if (!isLoggedIn()) {
            return;
        }
        final JSONObject requestObject = new JSONObject();
        try {
            requestObject.put(HttpConstants.SENDER_ID, UserInfo.INSTANCE
                            .getId());
            requestObject.put(HttpConstants.RECEIVER_ID, toUserId);
            requestObject.put(HttpConstants.MESSAGE, message);
            final ChatRequest request = new ChatRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.AMPQ, requestObject.toString(), mVolleyCallbacks, acknowledge);
            request.setRequestId(RequestId.AMPQ);
            request.setTag(TAG);
            mVolleyCallbacks.queue(request);
        } catch (JSONException e) {
            e.printStackTrace();
            //Should never happen
        }

    }

    /**
     * Generates the queue name from the user Id
     * 
     * @param userId The user Id to generate the queue name for
     * @return The queue name for the user id
     */
    private String generateQueueNameFromUserId(String userId) {
        return String.format(Locale.US, QUEUE_NAME_FORMAT, userId);
    }

    @Override
    public void onReceiveMessage(byte[] message) {

        //TODO Break this method out for readability
        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Logger.d(TAG, "Received:" + text);
            final JSONObject messageJson = new JSONObject(text);
            final JSONObject senderObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.SENDER, true, true);
            final JSONObject receiverObject = JsonUtils
                            .readJSONObject(messageJson, HttpConstants.RECEIVER, true, true);
            final String senderId = JsonUtils
                            .readString(senderObject, HttpConstants.ID_USER, true, true);
            final String receiverId = JsonUtils
                            .readString(receiverObject, HttpConstants.ID_USER, true, true);
            final String messageText = JsonUtils
                            .readString(messageJson, HttpConstants.MESSAGE, true, true);
            final String timestamp = JsonUtils
                            .readString(messageJson, HttpConstants.TIME, true, true);

            //Insert the chat message into DB
            final String chatId = generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mDateFormatter
                            .getEpoch(timestamp));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mDateFormatter
                            .getOutputTimestamp(timestamp));

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
                showChatReceivedNotification(chatId, senderId, senderName, messageText);
            }

        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            //Shouldn't be happening
        } catch (JSONException e) {
            Logger.e(TAG, e, "Invalid Chat Json");
        } catch (ParseException e) {
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
     * The username, password, queue name and routing key in the same order. All
     * parameters should be passed. Send an EMPTY STRING if not required
     * 
     * @author Vinay S Shenoy
     */
    private class ConnectToChatAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            //Validation
            assert (params != null);
            assert (params.length == 4);
            assert (params[0] != null);
            assert (params[1] != null);
            assert (params[2] != null);
            assert (params[3] != null);
            Logger.v(TAG, "Username %s, Password %s, Queue %s, Routing Key %s", params[0], params[1], params[2], params[3]);
            if (mMessageConsumer
                            .connectToRabbitMQ(params[0], params[1], params[2], false, false, true, null)) {
                try {
                    mMessageConsumer.addBinding(params[3]);
                } catch (IOException e) {
                    mMessageConsumer.dispose();
                }
            }
            return null;
        }
    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {

        switch (token) {

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

                    ContentValues values = new ContentValues(4);
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
                break;
            }

            case QueryTokens.INSERT_USER_FOR_CHAT: {
                //Nothing to do here as of now
                break;
            }
        }

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {

        if (token == QueryTokens.UPDATE_CHAT) {
            assert (cookie != null);
            assert (cookie instanceof ContentValues);

            if (updateCount == 0) {
                //Unable to update chats table, create a row
                Logger.v(TAG, "Chat not found. Inserting %s", cookie);
                DBInterface.insertAsync(QueryTokens.INSERT_CHAT, cookie, TableChats.NAME, null, (ContentValues) cookie, true, this);
            } else {
                Logger.v(TAG, "Chat Updated!");
                //TODO Show notification
            }
        } else if (token == QueryTokens.UPDATE_USER_FOR_CHAT) {
            assert (cookie != null);
            assert (cookie instanceof ContentValues);

            if (updateCount == 0) {
                //Unable to update user, create the user
                Logger.v(TAG, "User not found. Inserting %s", cookie);
                DBInterface.insertAsync(QueryTokens.INSERT_USER_FOR_CHAT, cookie, TableUsers.NAME, null, (ContentValues) cookie, true, this);
            }
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

    }

    /**
     * Generates as chat ID which will be unique for a given sernder/receiver
     * pair
     * 
     * @param receiverId The receiver of the chat
     * @param senderId The sender of the chat
     * @return The chat Id
     */
    public static String generateChatId(String receiverId, String senderId) {

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
        } catch (NoSuchAlgorithmException e) {
            /*
             * Shouldn't happen sinch SHA-1 is standard, but in case it does use
             * the combined string directly since they are local chat IDs
             */
            hashed = combined;
        }

        return hashed;
    }

    @Override
    public void onPreExecute(IBlRequestContract request) {

    }

    @Override
    public void onPostExecute(IBlRequestContract request) {

    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

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
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {

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
    public void onAuthError(int requestId, IBlRequestContract request) {

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
    public void onOtherError(int requestId, IBlRequestContract request,
                    int errorCode) {

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

    /**
     * Displays a notification for a received chat message
     * 
     * @param chatId The ID of the chat. This is so that the right chat detail
     *            fragment can be launched when the notification is tapped
     * @param withUserId The id of the user who sent the notification
     * @param senderName The name of the sender
     * @param messageText The message body
     */
    private void showChatReceivedNotification(String chatId, String withUserId,
                    String senderName, String messageText) {

        mUnreadMessageCount++;
        final Intent resultIntent = new Intent(this, HomeActivity.class);
        if (mUnreadMessageCount == 1) {
            mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(senderName)
                            .setContentText(messageText).setAutoCancel(true);
            resultIntent.setAction(AppConstants.ACTION_SHOW_CHAT_DETAIL);
            resultIntent.putExtra(Keys.CHAT_ID, chatId);
            resultIntent.putExtra(Keys.USER_ID, withUserId);

        } else {
            mNotificationBuilder
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle(getString(R.string.new_messages, mUnreadMessageCount))
                            .setContentText(messageText).setAutoCancel(true);
            resultIntent.setAction(AppConstants.ACTION_SHOW_ALL_CHATS);
        }

        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntent(resultIntent);
        final PendingIntent pendingIntent = taskStackBuilder
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(pendingIntent);
        mNotificationManager
                        .notify(MESSAGE_NOTIFICATION_ID, mNotificationBuilder
                                        .build());

    }

    /**
     * Cancels any notifications being displayed. Call this if the relevant
     * screen is opened within the app
     */
    private void cancelMessageReceivedNotification() {

        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        mUnreadMessageCount = 0;
    }
}
