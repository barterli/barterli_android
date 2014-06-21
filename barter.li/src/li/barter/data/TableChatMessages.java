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

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;

import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.ChatStatus;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;

/**
 * @author Vinay S Shenoy Table representing a list of chat messages
 */
public class TableChatMessages {

    private static final String TAG  = "TableChatMessages";

    public static final String  NAME = "CHAT_MESSAGES";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENDER_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RECEIVER_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SENT_AT, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.MESSAGE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.TIMESTAMP_EPOCH, 0),
                                String.format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.CHAT_STATUS, ChatStatus.FAILED)
                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    @SuppressLint("UseSparseArrays")
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

            final String alterTableDef = String
                            .format(Locale.US, SQLConstants.ALTER_TABLE_ADD_COLUMN, NAME, String
                                            .format(Locale.US, SQLConstants.DATA_INTEGER, DatabaseColumns.CHAT_STATUS, ChatStatus.SENT));
            Logger.d(TAG, "Alter Table Def: %s", alterTableDef);
            db.execSQL(alterTableDef);

            if (!TextUtils.isEmpty(UserInfo.INSTANCE.getId())) {

                /*
                 * User is logged in. There might be some chat messages that
                 * need to be updated to use the new chat status field
                 */

                //Update all current user's sent messages with chat status sent
                String updateTable = String
                                .format(Locale.US, SQLConstants.UPDATE, NAME, DatabaseColumns.CHAT_STATUS
                                                + SQLConstants.EQUALS
                                                + ChatStatus.SENT, DatabaseColumns.SENDER_ID
                                                + SQLConstants.EQUALS_QUOTE
                                                + UserInfo.INSTANCE.getId()
                                                + SQLConstants.QUOTE);
                Logger.d(TAG, "Update table def %s", updateTable);
                db.execSQL(updateTable);

                //Update all current user's received messages with chat status received
                updateTable = String
                                .format(Locale.US, SQLConstants.UPDATE, NAME, DatabaseColumns.CHAT_STATUS
                                                + SQLConstants.EQUALS
                                                + ChatStatus.RECEIVED, DatabaseColumns.RECEIVER_ID
                                                + SQLConstants.EQUALS_QUOTE
                                                + UserInfo.INSTANCE.getId()
                                                + SQLConstants.QUOTE);

                Logger.d(TAG, "Update table def %s", updateTable);
                db.execSQL(updateTable);

                final Cursor cursor = db.query(NAME, new String[] {
                        BaseColumns._ID, DatabaseColumns.TIMESTAMP
                }, null, null, null, null, null, null);

                if (cursor.getCount() > 0) {
                    //Update the timestamps from the old human readable version to the new version

                    final DateFormatter formatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.MESSAGE_TIME_FORMAT);
                    final HashMap<Integer, String> idTimestampMap = new HashMap<Integer, String>(cursor
                                    .getCount());

                    while (cursor.moveToNext()) {

                        try {
                            idTimestampMap.put(cursor.getInt(cursor
                                            .getColumnIndex(BaseColumns._ID)), formatter
                                            .getOutputTimestamp(cursor.getString(cursor
                                                            .getColumnIndex(DatabaseColumns.TIMESTAMP))));
                        } catch (ParseException e) {
                            // Shouldn't happen while migrating
                        }
                    }

                    for (int rowId : idTimestampMap.keySet()) {

                        updateTable = String
                                        .format(Locale.US, SQLConstants.UPDATE, NAME, DatabaseColumns.TIMESTAMP_HUMAN
                                                        + SQLConstants.EQUALS
                                                        + idTimestampMap.get(rowId), BaseColumns._ID
                                                        + SQLConstants.EQUALS
                                                        + rowId);
                        Logger.d(TAG, "Update table %s", updateTable);
                        db.execSQL(updateTable);
                    }
                }
            }
        }
    }
}
