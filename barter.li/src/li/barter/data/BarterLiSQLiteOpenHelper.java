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

import java.util.ArrayList;

import li.barter.utils.AppConstants;
import li.barter.utils.Logger;
import li.barter.utils.Utils;

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

    // Array of loader entries to hold for notifying changes
    private ArrayList<SQLiteLoaderObserver> mActiveLoaders;

    /**
     * Gets a reference to the SQLIte Open Helper for the app, creating it if
     * necessary. This method is thread-safe
     * 
     * @param context The Context reference
     * @return the reference to {@link BarterLiSQLiteOpenHelper}
     */
    public static BarterLiSQLiteOpenHelper getInstance(final Context context) {

        synchronized (LOCK) {

            if (sSQLiteOpenHelper == null) {

                synchronized (LOCK) {
                    sSQLiteOpenHelper = new BarterLiSQLiteOpenHelper(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
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
    private BarterLiSQLiteOpenHelper(final Context context, final String name, final CursorFactory factory, final int version) {
        //Private so you need to use the getInstance() method
        super(context, name, factory, version);
        mActiveLoaders = new ArrayList<SQLiteLoaderObserver>();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        TableSearchBooks.create(db);

    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {
        TableSearchBooks.upgrade(db, oldVersion, newVersion);
    }

    /**
     * Checks if the current database query is being made on the main thread. If
     * yes, either throws an Exception(if in DEBUG mode) orLoggers an error(in
     * PRODUCTION mode)
     */
    private static void throwIfOnMainThread() {
        if (Utils.isMainThread()) {
            if (AppConstants.DEBUG) {
                throw new RuntimeException("Accessing database on main thread!");
            } else {
                Logger.e(TAG, "Accessing database on main thread");
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
    public Cursor query(final boolean distinct, final String table,
                    final String[] columns, final String selection,
                    final String[] selectionArgs, final String groupBy,
                    final String having, final String orderBy,
                    final String limit) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getReadableDatabase();
        return database.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
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
    public long insert(final String table, final String nullColumnHack,
                    final ContentValues values) {

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
    public int update(final String table, final ContentValues values,
                    final String whereClause, final String[] whereArgs) {

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
    public int delete(final String table, final String whereClause,
                    final String[] whereArgs) {

        throwIfOnMainThread();
        final SQLiteDatabase database = getWritableDatabase();
        return database.delete(table, whereClause, whereArgs);
    }

    /**
     * Register a loader for maintaining notify changes
     * 
     * @param loader The {@link SQLiteLoader} loader to register
     * @param table The table name
     * @return The {@link SQLiteLoaderObserver} that was created. Use this to
     *         unregister the loader entry
     */
    public SQLiteLoaderObserver registerLoader(final SQLiteLoader loader,
                    final String table) {

        Logger.d(TAG, "Add Loader Observer: %s", table);
        final SQLiteLoaderObserver entry = new SQLiteLoaderObserver(loader, table);
        mActiveLoaders.add(entry);
        return entry;
    }

    public void unregisterLoader(final SQLiteLoaderObserver entry) {

        Logger.d(TAG, "Remove Loader Observer: %s", entry.table);
        mActiveLoaders.remove(entry);
    }

    /**
     * Notify loaders whenever a table is modified
     * 
     * @param table The table that was modified
     */
    public void notifyChange(final String table) {
        //TODO Optimize this later, Maybe a sorted list of loaders by table name?
        for (final SQLiteLoaderObserver entry : mActiveLoaders) {
            Logger.d(TAG, "Notify change: %s", entry.table);
            if (entry.table.equals(table)) {
                entry.loader.onContentChanged();
            }
        }
    }

}
