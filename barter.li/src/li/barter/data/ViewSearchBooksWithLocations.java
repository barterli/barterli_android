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
 * View representation for search results for books, combined with their
 * locations
 * 
 * @author Vinay S Shenoy
 */
public class ViewSearchBooksWithLocations {

    private static final String TAG                = "ViewSearchBooksWithLocations";

    //Aliases for the tables
    private static final String ALIAS_SEARCH_BOOKS = "A";
    private static final String ALIAS_LOCATIONS    = "B";

    public static final String  NAME               = "SEARCH_BOOKS_WITH_LOCATIONS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_SEARCH_BOOKS, BaseColumns._ID),
                                DatabaseColumns.BOOK_ID,
                                DatabaseColumns.USER_ID,
                                DatabaseColumns.ID,
                                DatabaseColumns.TITLE,
                                DatabaseColumns.IMAGE_URL,
                                DatabaseColumns.DESCRIPTION,
                                DatabaseColumns.AUTHOR,
                                DatabaseColumns.OWNER,
                                String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_SEARCH_BOOKS, DatabaseColumns.LOCATION_ID),
                                DatabaseColumns.NAME, DatabaseColumns.ADDRESS

                        });
        Logger.d(TAG, "View Column Def: %s", columnDef);

        final String fromDef = TextUtils
                        .join(",", new String[] {
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableSearchBooks.NAME, ALIAS_SEARCH_BOOKS),
                                String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableLocations.NAME, ALIAS_LOCATIONS)

                        });
        Logger.d(TAG, "From Def: %s", fromDef);

        final String whereDef = String
                        .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_SEARCH_BOOKS, DatabaseColumns.LOCATION_ID)
                        + SQLConstants.EQUALS
                        + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_LOCATIONS, DatabaseColumns.LOCATION_ID);
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
