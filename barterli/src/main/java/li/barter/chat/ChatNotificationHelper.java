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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.NotificationCompat.Builder;

import li.barter.R;
import li.barter.activities.HomeActivity;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.Keys;

/**
 * Class to help with sending chat notifications
 * 
 * @author Vinay S Shenoy
 */
class ChatNotificationHelper {

    /**
     * Notification Id for notifications related to messages
     */
    private static final int              MESSAGE_NOTIFICATION_ID = 1;

    private static final String           TAG                     = "ChatNotificationHelper";

    private static final Object           LOCK                    = new Object();

    /**
     * Holds the number of unread received messages
     */
    private int                           mUnreadMessageCount;

    /**
     * Id of the user with whom the user is currently chatting.
     */
    private String                        mCurrentChattingUserId;

    /**
     * Whether chat notifications are enabled or not
     */
    private boolean                       mNotificationsEnabled;

    private Builder                       mNotificationBuilder;

    private NotificationManager           mNotificationManager;

    private Uri                           mNotificationSoundUri;

    private static ChatNotificationHelper sInstance;

    private ChatNotificationHelper() {
        //Private constructor
        mCurrentChattingUserId = null;
        mNotificationsEnabled = true;
        mUnreadMessageCount = 0;
    }

    private ChatNotificationHelper(Context context) {
        //Private Constructor
        this();
        mNotificationBuilder = new Builder(context);
        mNotificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationSoundUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    }

    /**
     * Returns an instance of the {@link ChatNotificationHelper}
     * 
     * @param context
     * @return
     */
    public static ChatNotificationHelper getInstance(Context context) {

        synchronized (LOCK) {
            if (sInstance == null) {
                synchronized (LOCK) {
                    sInstance = new ChatNotificationHelper(context);
                }
            }
        }
        return sInstance;
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
        mCurrentChattingUserId = currentChattingUserId;
    }

    /**
     * Set notifications enabled
     * 
     * @param enabled <code>true</code> to enable notifications,
     *            <code>false</code> to disable them
     */
    public void setNotificationsEnabled(final boolean enabled) {
        mNotificationsEnabled = enabled;
    }

    /**
     * Cancels any notifications being displayed. Call this if the relevant
     * screen is opened within the app
     */
    public void clearChatNotifications() {

        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        mUnreadMessageCount = 0;
    }

    /**
     * Displays a notification for a received chat message
     * 
     * @param context
     * @param chatId The ID of the chat. This is so that the right chat detail
     *            fragment can be launched when the notification is tapped
     * @param withUserId The id of the user who sent the notification
     * @param senderName The name of the sender
     * @param messageText The message body
     */
    public void showChatReceivedNotification(final Context context,
                    final String chatId, final String withUserId,
                    final String senderName, final String messageText) {

        //Don't show notification if we are currently chatting with the same user
        if (mCurrentChattingUserId != null
                        && mCurrentChattingUserId.equals(withUserId)) {
            return;
        }

        if (mNotificationsEnabled) {
            mUnreadMessageCount++;
            final Intent resultIntent = new Intent(context, HomeActivity.class);
            if (mUnreadMessageCount == 1) {
                mNotificationBuilder.setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(senderName)
                                .setContentText(messageText)
                                .setAutoCancel(true);
                resultIntent.setAction(AppConstants.ACTION_SHOW_CHAT_DETAIL);
                resultIntent.putExtra(Keys.CHAT_ID, chatId);
                resultIntent.putExtra(Keys.USER_ID, withUserId);

            } else {
                mNotificationBuilder
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle(context.getString(R.string.new_messages, mUnreadMessageCount))
                                .setContentText(messageText)
                                .setAutoCancel(true);
                resultIntent.setAction(AppConstants.ACTION_SHOW_ALL_CHATS);
            }

            mNotificationBuilder.setSound(mNotificationSoundUri);
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder
                            .create(context);
            taskStackBuilder.addNextIntent(resultIntent);
            final PendingIntent pendingIntent = taskStackBuilder
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.setContentIntent(pendingIntent);
            mNotificationManager
                            .notify(MESSAGE_NOTIFICATION_ID, mNotificationBuilder
                                            .build());
        }

    }
}
