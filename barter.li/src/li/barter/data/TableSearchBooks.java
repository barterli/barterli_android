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
 * Table representation for search results for books
 * 
 * @author Vinay S Shenoy
 */
public class TableSearchBooks {

    private static final String TAG  = "TableSearchBooks";

    public static final String  NAME = "SEARCH_BOOKS";

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
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.VALUE, "")
                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table
        db.execSQL(String
                        .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
        create(db);
    }
}
