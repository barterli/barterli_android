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
 * View representation for chats, messages and users
 * 
 * @author Vinay S Shenoy
 */
public class ViewChatsWithMessagesAndUsers {

    private static final String TAG                 = "ViewChatsWithMessagesAndUsers";

    //Aliases for the tables
    private static final String ALIAS_CHATS         = "A";
    private static final String ALIAS_CHAT_MESSAGES = "B";
    private static final String ALIAS_USERS         = "C";

    public static final String  NAME                = "CHATS_WITH_CHAT_MESSAGES_AND_LOCATIONS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.CHAT_ID),
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.USER_ID),
                                DatabaseColumns.CHAT_TYPE,
                                DatabaseColumns.PROFILE_PICTURE,
                                DatabaseColumns.FIRST_NAME,
                                DatabaseColumns.LAST_NAME,
                                DatabaseColumns.UNREAD_COUNT,
                                DatabaseColumns.MESSAGE,
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP_HUMAN),
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP_EPOCH),
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.TIMESTAMP)
                        });
        Logger.d(TAG, "View Column Def: %s", columnDef);

        final String fromDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChats.NAME, ALIAS_CHATS),
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableChatMessages.NAME, ALIAS_CHAT_MESSAGES),
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableUsers.NAME, ALIAS_USERS)
                        });
        Logger.d(TAG, "From Def: %s", fromDef);

        final String whereDef = String
                        .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.LAST_MESSAGE_ID)
                        + SQLConstants.EQUALS
                        + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHAT_MESSAGES, BaseColumns._ID)
                        + SQLConstants.AND
                        + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CHATS, DatabaseColumns.USER_ID)
                        + SQLConstants.EQUALS
                        + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_USERS, DatabaseColumns.USER_ID);
        Logger.d(TAG, "Where Def: %s", whereDef);

        final String selectDef = String
                        .format(Locale.US, SQLConstants.SELECT_FROM_WHERE, columnDef, fromDef, whereDef);

        Logger.d(TAG, "Select Def: %s", selectDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_VIEW, NAME, selectDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion < 4) {

            /*
             * Drop & recreate the view if upgrading from DB version 1(alpha
             * version)
             */
            db.execSQL(String
                            .format(Locale.US, SQLConstants.DROP_VIEW_IF_EXISTS, NAME));
            create(db);

        }
    }
}
