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

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;

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

/**
 * Runnable implementation to process chat messages
 * 
 * @author Vinay S Shenoy
 */
class ChatProcessTask implements Runnable {

    private static final String TAG            = "ChatProcessTask";

    private static final String CHAT_SELECTION = DatabaseColumns.CHAT_ID
                                                               + SQLConstants.EQUALS_ARG;

    private static final String USER_SELECTION = DatabaseColumns.USER_ID
                                                               + SQLConstants.EQUALS_ARG;

    private final Context       mContext;
    private final String        mMessage;
    private final DateFormatter mDateFormatter;

    /**
     * @param context
     * @param message the Chat message to process
     * @param dateFormatter A date formatter to format dates
     */
    public ChatProcessTask(final Context context, final String message, final DateFormatter dateFormatter) {

        mContext = context;
        mMessage = message;
        mDateFormatter = dateFormatter;
    }

    @Override
    public void run() {

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

            final String chatId = ChatService
                            .generateChatId(receiverId, senderId);
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
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mDateFormatter
                            .getEpoch(timestamp));
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mDateFormatter
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
                ChatNotificationHelper.getInstance(mContext).showChatReceivedNotification(mContext, chatId, senderId, senderName, messageText);

                final ContentValues values = new ContentValues(4);
                values.put(DatabaseColumns.CHAT_ID, chatId);
                values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                values.put(DatabaseColumns.USER_ID, isSenderCurrentUser ? receiverId
                                : senderId);

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

        return String.format("%s %s", senderFirstName, senderLastName);
    }

}
