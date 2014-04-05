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

import org.apache.http.protocol.HTTP;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import li.barter.chat.AbstractRabbitMQConnector.ExchangeType;
import li.barter.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import li.barter.http.HttpConstants;
import li.barter.utils.AppConstants;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;

/**
 * Bound service to send and receive chat messages. The service will receive
 * chat messages and update them in the chats database. <br/>
 * <br/>
 * This service needs to be triggered in three cases -
 * <ol>
 * <li>On device boot</li>
 * <li>On application launch</li>
 * <li>On network connectivity resumed(if it was lost)</li>
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
public class ChatService extends Service implements OnReceiveMessageHandler {

    private static final String    TAG                = "ChatService";

    private static final String    ROUTING_KEY        = "shared.key";

    private static final String    OUTPUT_TIME_FORMAT = "dd MMM, h:m a";

    private final IBinder          mChatServiceBinder = new ChatServiceBinder();

    /** {@link ChatRabbitMQConnector} instance for listening to messages */
    private ChatRabbitMQConnector  mMessageConsumer;

    private DateFormatter          mDateFormatter;

    /**
     * Task to connect to Rabbit MQ Chat server
     */
    private ConnectToChatAsyncTask mConnectTask;

    @Override
    public void onCreate() {
        super.onCreate();
        mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                        .getChatPort(), "/", "node.barterli", ExchangeType.DIRECT);
        mMessageConsumer.setOnReceiveMessageHandler(this);
        mDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, OUTPUT_TIME_FORMAT);

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

        if (!mMessageConsumer.isRunning()) {

            if (mConnectTask == null) {
                mConnectTask = new ConnectToChatAsyncTask();
                mConnectTask.execute("barterli", "barter", generateQueueNameFromUserId("user1"), ROUTING_KEY);
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
                    mConnectTask.execute("barterli", "barter", generateQueueNameFromUserId("user1"), ROUTING_KEY);
                }
            }

        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMessageConsumer.isRunning()) {
            mMessageConsumer.dispose();
            mMessageConsumer = null;
        }
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
     * @return Whether the message was delivered to the chat server or not
     */
    public boolean sendMessageToUser(String toUserId, String message) {
        //TODO Construct the message
        if (mMessageConsumer.isRunning()) {
            try {
                final String queue = mMessageConsumer
                                .declareQueue(generateQueueNameFromUserId(toUserId), false, false, true, null);
                mMessageConsumer.addBinding(queue, ROUTING_KEY);
                mMessageConsumer.publish(queue, ROUTING_KEY, message);
                return true;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Generates the queue name from the user Id
     * 
     * @param userId The user Id to generate the queue name for
     * @return The queue name for the user id
     */
    private String generateQueueNameFromUserId(String userId) {
        //TODO Discuss how to generate queue names for user Ids
        return userId;
    }

    @Override
    public void onReceiveMessage(byte[] message) {

        //TODO Read sender info
        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Logger.d(TAG, "Received:" + text);
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(text)) {
            //TODO Store Chats in table, send notification
        }
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

}
