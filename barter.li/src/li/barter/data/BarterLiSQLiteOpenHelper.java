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
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import li.barter.utils.AppConstants;
import li.barter.utils.UtilityMethods;

/**
 * @author vinaysshenoy {@link SQLiteOpenHelper} to provide database
 *         connectivity for the application
 */
public class BarterLiSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String             TAG        = "BarterLiSQLiteOpenHelper";

    // Lock for synchronized methods
    private static final Object             LOCK       = new Object();

    // Database file name and version
    private static final String             DB_NAME    = "barterli.sqlite";
    private static final int                DB_VERSION = 1;

    // SQLite Open Helper instance
    private static BarterLiSQLiteOpenHelper sSQLiteOpenHelper;

    /**
     * Gets a reference to the SQLIte Open Helper for the app, creating it if
     * necessary. This method is thread-safe
     * 
     * @param context The Context reference
     * @return the reference to {@link BarterLiSQLiteOpenHelper}
     */
    public static BarterLiSQLiteOpenHelper getInstance(Context context) {

        synchronized (LOCK) {

            if (sSQLiteOpenHelper == null) {

                synchronized (LOCK) {
                    sSQLiteOpenHelper = new BarterLiSQLiteOpenHelper(context,
                                    DB_NAME, null, DB_VERSION);
                }
            }
        }

        return sSQLiteOpenHelper;
    }

    /**
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public BarterLiSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Create Tables

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Upgrade Tables

    }

    /**
     * Checks if the current database query is being made on the main thread. If
     * yes, either throws an Exception(if in DEBUG mode) or Logs an error(in
     * PRODUCTION mode)
     */
    private static void throwIfOnMainThread() {
        if (UtilityMethods.isMainThread()) {
            if (AppConstants.DEBUG) {
                throw new RuntimeException("Accessing database on main thread!");
            } else {
                Log.e(TAG, "Accessing database on main thread");
            }
        }
    }

    /**
     * Query the given URL, returning a Cursor over the result set.
     * 
     * @param distinct <code>true</code> if dataset should be unique
     * @param table The table to query
     * @param columns The columns to fetch
     * @param selection The selection string, formatted as a WHERE clause
     * @param selectionArgs The arguments for the selection parameter
     * @param groupBy GROUP BY clause
     * @param having HAVING clause
     * @param orderBy ORDER BY clause
     * @param limit LIMIT clause
     * @return A {@link Cursor} over the dataset result
     */
    public Cursor query(boolean distinct, String table, String[] columns,
                    String selection, String[] selectionArgs, String groupBy,
                    String having, String orderBy, String limit) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getReadableDatabase();
        return database.query(distinct, table, columns, selection,
                        selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Method for inserting rows into the database
     * 
     * @param table The table to insert into
     * @param nullColumnHack column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter
     *            provides the name of nullable column name to explicitly insert
     *            a NULL into in the case where your values is empty.
     * @param values The fields to insert
     * @return The row Id of the newly inserted row, or -1 if unable to insert
     */
    public long insert(String table, String nullColumnHack, ContentValues values) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getWritableDatabase();
        return database.insert(table, nullColumnHack, values);
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
    public int update(String table, ContentValues values, String whereClause,
                    String[] whereArgs) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getWritableDatabase();
        return database.update(table, values, whereClause, whereArgs);
    }

    /**
     * Delete rows from the database
     * 
     * @param table The table to delete from
     * @param whereClause The WHERE clause
     * @param whereArgs Arguments for the where clause
     * @return The number of rows deleted
     */
    public int delete(String table, String whereClause, String[] whereArgs) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getWritableDatabase();
        return database.delete(table, whereClause, whereArgs);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {

        return super.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

}
