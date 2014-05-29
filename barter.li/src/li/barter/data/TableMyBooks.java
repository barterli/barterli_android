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

import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;

/**
 * Table representation for search results for books
 * 
 * @deprecated This class has been deprecated since the table is not in use
 *             anymore. All data is transfered to {@link TableUserBooks} as of
 *             DB version 2
 * @author Vinay S Shenoy
 */
public class TableMyBooks {

    private static final String TAG  = "TableMyBooks";

    static final String         NAME = "MY_BOOKS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.BOOK_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ISBN_10, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ISBN_13, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TITLE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DESCRIPTION, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.AUTHOR, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.BARTER_TYPE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LOCATION_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.IMAGE_URL, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.PUBLICATION_YEAR, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.PUBLICATION_MONTH, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.VALUE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.BOOK_OWNER, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.BOOK_OWNER_IMAGE_URL, "")

                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));
        throw new IllegalStateException("Deprecated Table is getting created "
                        + NAME);

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table
        if (oldVersion == 1) {

            if (!TextUtils.isEmpty(UserInfo.INSTANCE.getId())) {
                /*
                 * Delete all books from User Books Table which belong to the
                 * current user. Shouldn't be needed, but just a precaution.
                 */
                final String deleteStatement = String
                                .format(Locale.US, SQLConstants.DELETE_FROM_WHERE, TableUserBooks.NAME, DatabaseColumns.USER_ID
                                                + SQLConstants.EQUALS_QUOTE
                                                + UserInfo.INSTANCE.getId()
                                                + SQLConstants.QUOTE);
                Logger.v(TAG, "Delete Statement: %s", deleteStatement);
                db.execSQL(deleteStatement);

                final String[] columnsToCopy = new String[] {
                        DatabaseColumns.BOOK_ID, DatabaseColumns.ID,
                        DatabaseColumns.ISBN_10, DatabaseColumns.ISBN_13,
                        DatabaseColumns.TITLE, DatabaseColumns.DESCRIPTION,
                        DatabaseColumns.AUTHOR, DatabaseColumns.BARTER_TYPE,
                        DatabaseColumns.USER_ID, DatabaseColumns.LOCATION_ID,
                        DatabaseColumns.IMAGE_URL,
                        DatabaseColumns.PUBLICATION_YEAR,
                        DatabaseColumns.PUBLICATION_MONTH,
                        DatabaseColumns.VALUE, DatabaseColumns.BOOK_OWNER
                };

                /* Move books from the my books table to the user books table */
                final String selectStatement = String
                                .format(Locale.US, SQLConstants.SELECT_FROM, TextUtils
                                                .join(",", columnsToCopy), NAME);

                final String insertStatement = String
                                .format(Locale.US, SQLConstants.INSERT, TableUserBooks.NAME, TextUtils
                                                .join(",", columnsToCopy), selectStatement);

                Logger.v(TAG, "Insert Statement: %s", insertStatement);
                db.execSQL(insertStatement);

                /* Drop the current table */
                db.execSQL(String
                                .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));

            }

            /*
             * String alterTableDef = String .format(Locale.US,
             * SQLConstants.ALTER_TABLE_ADD_COLUMN, NAME, String
             * .format(Locale.US, SQLConstants.DATA_TEXT,
             * DatabaseColumns.BOOK_OWNER_IMAGE_URL, "")); Logger.d(TAG,
             * "Alter Table Def: %s", alterTableDef); db.execSQL(alterTableDef);
             */
        }
    }
}
