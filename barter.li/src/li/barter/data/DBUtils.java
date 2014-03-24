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

import android.content.ContentValues;

import li.barter.BarterLiApplication;

/**
 * Utility class to save methods to database
 * 
 * @author Vinay S Shenoy
 */
public class DBUtils {

    /**
     * Insert a row into the database
     * 
     * @table The table to insert into
     * @param nullColumnHack column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter
     *            provides the name of nullable column name to explicitly insert
     *            a NULL into in the case where your values is empty.
     * @param values The fields to insert
     * @return The row Id if inserted, -1 if not
     */
    public static long insert(String table, String nullColumnHack,
                    ContentValues values) {
        return BarterLiSQLiteOpenHelper
                        .getInstance(BarterLiApplication.getStaticContext())
                        .insert(table, nullColumnHack, values);
    }

    /**
     * Updates the table with the given data
     * 
     * @param table The table to update
     * @param values The fields to update
     * @param whereClause The WHERE clause
     * @param whereArgs Arguments for the where clause
     * @return The number of rows updated
     */
    public static int update(String table, ContentValues values,
                    String whereClause, String[] whereArgs) {
        return BarterLiSQLiteOpenHelper
                        .getInstance(BarterLiApplication.getStaticContext())
                        .update(table, values, whereClause, whereArgs);
    }

    /**
     * Notify any loaders that the content of the table has changed
     * 
     * @param tableName The table name that has been updated
     */
    public static void notifyChange(String tableName) {
        BarterLiSQLiteOpenHelper
                        .getInstance(BarterLiApplication.getStaticContext())
                        .notifyChange(tableName);
    }
}
