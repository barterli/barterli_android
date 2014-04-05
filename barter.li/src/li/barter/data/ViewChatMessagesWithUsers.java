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

package li.barter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Locale;

import li.barter.utils.Logger;

/**
 * View representation for chat messages, combined with the sender and receiver
 * profile pictures
 * 
 * @author Vinay S Shenoy
 */
public class ViewChatMessagesWithUsers {

    private static final String TAG                  = "ViewChatMessagesWithUsers";

    //Aliases for the tables
    private static final String ALIAS_CHAT_MESSAGES  = "A";
    private static final String ALIAS_USERS_SENT     = "B_SENT";
    private static final String ALIAS_USERS_RECEIVED = "B_RECEIVED";

    public static final String  NAME                 = "CHAT_MESSAGES_WITH_USERS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHAT_MESSAGES, BaseColumns._ID),
                                DatabaseColumns.MESSAGE,
                                DatabaseColumns.MESSAGE_TIMESTAMP,
                                DatabaseColumns.MESSAGE_TIMESTAMP_EPOCH,
                                DatabaseColumns.MESSAGE_TIMESTAMP_HUMAN,
                                DatabaseColumns.PROFILE_PICTURE,
                                DatabaseColumns.SENDER_ID,
                                DatabaseColumns.SENDER_PROFILE_PICTURE,
                                DatabaseColumns.RECEIVER_PROFILE_PICTURE,
                                DatabaseColumns.RECEIVER_ID

                        });
        Logger.d(TAG, "View Column Def: %s", columnDef);

        final String fromDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChatMessages.NAME, ALIAS_CHAT_MESSAGES),
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableUsers.NAME, ALIAS_USERS_SENT),
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableUsers.NAME, ALIAS_USERS_RECEIVED)
                        });
        Logger.d(TAG, "From Def: %s", fromDef);

        final String whereDef = String
                        .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHAT_MESSAGES, DatabaseColumns.LOCATION_ID)
                        + SQLConstants.EQUALS
                        + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_USERS_SENT, DatabaseColumns.LOCATION_ID);
        Logger.d(TAG, "Where Def: %s", whereDef);

        final String selectDef = String
                        .format(Locale.US, SQLConstants.SELECT_FROM_WHERE, columnDef, fromDef, whereDef);

        Logger.d(TAG, "Select Def: %s", selectDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_VIEW, NAME, selectDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        db.execSQL(String
                        .format(Locale.US, SQLConstants.DROP_VIEW_IF_EXISTS, NAME));
        create(db);
    }
}
