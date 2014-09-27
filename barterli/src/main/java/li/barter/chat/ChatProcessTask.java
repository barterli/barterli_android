/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import java.text.ParseException;

import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.data.TableUsers;
import li.barter.http.HttpConstants;
import li.barter.http.JsonUtils;
import li.barter.utils.AppConstants.ChatStatus;
import li.barter.utils.AppConstants.ChatType;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;
import li.barter.utils.Utils;

/**
 * Runnable implementation to process chat messages
 * 
 * @author Vinay S Shenoy
 */
class ChatProcessTask implements Runnable {

    private static final String TAG             = "ChatProcessTask";

    private static final String CHAT_SELECTION  = DatabaseColumns.CHAT_ID
                                                                + SQLConstants.EQUALS_ARG;

    private static final String USER_SELECTION  = DatabaseColumns.USER_ID
                                                                + SQLConstants.EQUALS_ARG;

    public static final int     PROCESS_SEND    = 1;
    public static final int     PROCESS_RECEIVE = 2;

    /**
     * The process type of the task, either {@linkplain #PROCESS_SEND} or
     * {@linkplain #PROCESS_RECEIVE}
     */
    private int                 mProcessType;

    /**
     * Reference to the context to prepare notifications
     */
    private Context             mContext;

    /**
     * The message text, currently the JSON formatted string
     */
    private String              mMessage;

    /**
     * Date formatter for formatting chat timestamps
     */
    private DateFormatter       mChatDateFormatter;

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter       mMessageDateFormatter;

    /**
     * Callback for receiving when it is ready to send the chat message
     */
    private SendChatCallback    mSendChatCallback;

    /**
     * Callback defined for when the local chat has been saved to the database
     * and the request can be sent
     * 
     * @author Vinay S Shenoy
     */
    public static interface SendChatCallback {

        /**
         * Send the chat request
         * 
         * @param text The request body
         * @param dbRowId The row id of the inserted local chat message
         */
        public void sendChat(final String text, final long dbRowId);
    }

    private ChatProcessTask() {
        //Private constructor
    }

    private ChatProcessTask(final Context context) {
        mContext = context;
    }

    public int getProcessType() {
        return mProcessType;
    }

    public String getMessage() {
        return mMessage;
    }

    public DateFormatter getChatDateFormatter() {
        return mChatDateFormatter;
    }

    public DateFormatter getMessageDateFormatter() {
        return mMessageDateFormatter;
    }

    public SendChatCallback getSendChatCallback() {
        return mSendChatCallback;
    }

    @Override
    public void run() {

        if (mProcessType == PROCESS_RECEIVE) {
            processReceivedMessage();
        } else if (mProcessType == PROCESS_SEND) {
            saveMessageAndCallback();
        }

    }

    /**
     * Save a local message in the database, and give a callback to make the
     * chat send request once it is saved
     */
    private void saveMessageAndCallback() {

        try {
            final JSONObject messageJson = new JSONObject(mMessage);
            final String senderId = JsonUtils
                            .readString(messageJson, HttpConstants.SENDER_ID, true, true);
            final String sentAtTime = JsonUtils
                            .readString(messageJson, HttpConstants.SENT_AT, true, true);
            final String receiverId = JsonUtils
                            .readString(messageJson, HttpConstants.RECEIVER_ID, true, true);
            final String messageText = JsonUtils
                            .readString(messageJson, HttpConstants.MESSAGE, true, true);

            final String chatId = Utils.generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, sentAtTime);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.CHAT_STATUS, ChatStatus.SENDING);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                            .getEpoch(sentAtTime));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                            .getOutputTimestamp(sentAtTime));

            final long insertRowId = DBInterface
                            .insert(TableChatMessages.NAME, null, chatValues, true);

            if (insertRowId >= 0) {

                //Update or insert the chats table
                final ContentValues values = new ContentValues(7);
                values.put(DatabaseColumns.CHAT_ID, chatId);
                values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                values.put(DatabaseColumns.TIMESTAMP, sentAtTime);
                try {
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                                    .getOutputTimestamp(sentAtTime));
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                                    .getEpoch(sentAtTime));
                } catch (ParseException e) {
                    //Shouldn't happen
                }

                values.put(DatabaseColumns.USER_ID, receiverId);

                Logger.v(TAG, "Updating chats for Id %s", chatId);
                final int updateCount = DBInterface
                                .update(TableChats.NAME, values, CHAT_SELECTION, new String[] {
                                    chatId
                                }, true);

                if (updateCount == 0) {
                    //Insert the chat message
                    DBInterface.insert(TableChats.NAME, null, values, true);
                }

                //After finishing the local chat insertion, give a callback to do the actual network call
                mSendChatCallback.sendChat(mMessage, insertRowId);
            }
        } catch (JSONException e) {
            Logger.e(TAG, e, "Invalid message json");
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
        }
    }

    /**
     * Processes a received message, stores it in the database
     */
    private void processReceivedMessage() {
        try {
            final JSONObject messageJson = new JSONObject(mMessage);
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

            final String chatId = Utils.generateChatId(receiverId, senderId);
            final ContentValues chatValues = new ContentValues(7);

            final boolean isSenderCurrentUser = senderId
                            .equals(UserInfo.INSTANCE.getId());

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.CHAT_STATUS, isSenderCurrentUser ? ChatStatus.SENT
                            : ChatStatus.RECEIVED);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                            .getEpoch(timestamp));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                            .getOutputTimestamp(timestamp));

            if (isSenderCurrentUser) {
                //Update the locally saved message to mark it as sent

                //Insert the chat message into DB
                final String selection = DatabaseColumns.SENDER_ID
                                + SQLConstants.EQUALS_ARG + SQLConstants.AND
                                + DatabaseColumns.SENT_AT
                                + SQLConstants.EQUALS_ARG;

                final String[] args = new String[] {
                        senderId, sentAtTime
                };

                DBInterface.update(TableChatMessages.NAME, chatValues, selection, args, true);

            } else {
                //Insert the message in the db
                final long insertRowId = DBInterface
                                .insert(TableChatMessages.NAME, null, chatValues, true);

                /*
                 * Parse and store sender info. We will receive messages both
                 * when we send and receive, so we need to check the sender id
                 * if it is our own id first to detect who sent the message
                 */
                final String senderName = parseAndStoreChatUserInfo(senderId, senderObject);
                ChatNotificationHelper
                                .getInstance(mContext)
                                .showChatReceivedNotification(mContext, chatId, senderId, senderName, messageText);

                final ContentValues values = new ContentValues(7);
                values.put(DatabaseColumns.CHAT_ID, chatId);
                values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                values.put(DatabaseColumns.USER_ID, isSenderCurrentUser ? receiverId
                                : senderId);
                values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                                .getOutputTimestamp(timestamp));
                values.put(DatabaseColumns.TIMESTAMP, timestamp);
                values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                                .getEpoch(timestamp));

                Logger.v(TAG, "Updating chats for Id %s", chatId);
                final int updateCount = DBInterface
                                .update(TableChats.NAME, values, CHAT_SELECTION, new String[] {
                                    chatId
                                }, true);

                if (updateCount == 0) {
                    DBInterface.insert(TableChats.NAME, null, values, true);
                }
            }

        } catch (JSONException e) {
            Logger.e(TAG, e, "Invalid message json");
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
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

        final int updateCount = DBInterface
                        .update(TableUsers.NAME, senderValues, USER_SELECTION, new String[] {
                            senderId
                        }, true);

        if (updateCount == 0) {
            DBInterface.insert(TableUsers.NAME, null, senderValues, true);
        }

        return Utils.makeUserFullName(senderFirstName, senderLastName);
    }

    /**
     * Builder for Chat Process tasks
     * 
     * @author Vinay S Shenoy
     */
    public static class Builder {

        private Context         mContext;

        private ChatProcessTask mChatProcessTask;

        public Builder(Context context) {
            mContext = context;
            mChatProcessTask = new ChatProcessTask(mContext);
        }

        public Builder setProcessType(int processType) {
            mChatProcessTask.mProcessType = processType;
            return this;
        }

        public Builder setMessage(String message) {
            mChatProcessTask.mMessage = message;
            return this;
        }

        public Builder setChatDateFormatter(DateFormatter chatDateFormatter) {
            mChatProcessTask.mChatDateFormatter = chatDateFormatter;
            return this;
        }

        public Builder setMessageDateFormatter(
                        DateFormatter messageDateFormatter) {
            mChatProcessTask.mMessageDateFormatter = messageDateFormatter;
            return this;
        }

        public Builder setSendChatCallback(SendChatCallback sendChatCallback) {
            mChatProcessTask.mSendChatCallback = sendChatCallback;
            return this;
        }

        /**
         * Builds the chat process task
         * 
         * @return The complete chat process task
         * @throws IllegalStateException If the chat process task is invalid
         */
        public ChatProcessTask build() {

            if (mChatProcessTask.mProcessType != PROCESS_RECEIVE
                            && mChatProcessTask.mProcessType != PROCESS_SEND) {
                throw new IllegalStateException("Invalid process type");
            }

            if (TextUtils.isEmpty(mChatProcessTask.mMessage)) {
                throw new IllegalStateException("Empty or null message");
            }

            if (mChatProcessTask.mChatDateFormatter == null) {
                throw new IllegalStateException("No chat date formatter set");
            }

            if (mChatProcessTask.mMessageDateFormatter == null) {
                throw new IllegalStateException("No message date formatter set");
            }

            if (mChatProcessTask.mProcessType == PROCESS_SEND
                            && mChatProcessTask.mSendChatCallback == null) {
                throw new IllegalStateException("No send chat callback set for a send message");
            }

            return mChatProcessTask;
        }

        /**
         * Resets the builder for preparing another chat process task
         */
        public Builder reset() {
            mChatProcessTask = new ChatProcessTask(mContext);
            return this;
        }
    }

}
