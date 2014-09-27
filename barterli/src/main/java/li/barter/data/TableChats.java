/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Locale;

import li.barter.utils.AppConstants.ChatType;
import li.barter.utils.Logger;

/**
 * @author Vinay S Shenoy Table representing a list of chats
 */
public class TableChats {

    private static final String TAG  = "TableChats";

    public static final String  NAME = "CHATS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LAST_MESSAGE_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.UNREAD_COUNT, 0),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.TIMESTAMP_EPOCH, 0),
                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion == 1) {

            /*
             * Drop & recreate the table if upgrading from DB version 1(alpha
             * version)
             */
            db.execSQL(String
                            .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);

        } else if (oldVersion < 4) {

            String alterTableDef = String
                            .format(Locale.US, SQLConstants.ALTER_TABLE_ADD_COLUMN, NAME, String
                                            .format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP, ""));
            Logger.d(TAG, "Alter Table Def: %s", alterTableDef);
            db.execSQL(alterTableDef);
            alterTableDef = String
                            .format(Locale.US, SQLConstants.ALTER_TABLE_ADD_COLUMN, NAME, String
                                            .format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""));
            Logger.d(TAG, "Alter Table Def: %s", alterTableDef);
            db.execSQL(alterTableDef);
            alterTableDef = String
                            .format(Locale.US, SQLConstants.ALTER_TABLE_ADD_COLUMN, NAME, String
                                            .format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.TIMESTAMP_EPOCH, 0));
            Logger.d(TAG, "Alter Table Def: %s", alterTableDef);
            db.execSQL(alterTableDef);
        }
    }
}
