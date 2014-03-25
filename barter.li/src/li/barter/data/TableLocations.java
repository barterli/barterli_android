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
 * @author Vinay S Shenoy Table representing a list of locations used in the app
 */
public class TableLocations {

    private static final String TAG  = "TableLocations";

    public static final String  NAME = "LOCATIONS";

    public static void create(SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LOCATION_ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.NAME),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ADDRESS),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STREET),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LOCALITY),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CITY),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STATE),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COUNTRY),
                                String.format(Locale.US, SQLConstants.DATA_REAL, DatabaseColumns.LATITUDE),
                                String.format(Locale.US, SQLConstants.DATA_REAL, DatabaseColumns.LONGITUDE)
                        });

        Logger.d(TAG, "Column Def:", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table
        db.execSQL(String
                        .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
        create(db);
    }
}
