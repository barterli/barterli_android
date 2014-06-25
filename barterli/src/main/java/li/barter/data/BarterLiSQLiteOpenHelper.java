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
import android.provider.BaseColumns;

import java.text.ParseException;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.ChatStatus;
import li.barter.utils.DateFormatter;
import li.barter.utils.Logger;

/**
 * @author Vinay S Shenoy {@link SQLiteOpenHelper} to provide database
 *         connectivity for the application. The Methods of this class should
 *         not be accessed directly. Access them through the
 *         {@linkplain DBInterface} class
 */
class BarterLiSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String                              TAG        = "BarterLiSQLiteOpenHelper";

    /** Lock for synchronized methods */
    private static final Object                              LOCK       = new Object();

    /** Database file name and version */
    private static final String                              DB_NAME    = "barterli.sqlite";
    private static final int                                 DB_VERSION = 4;

    /** SQLite Open Helper instance */
    private static BarterLiSQLiteOpenHelper                  sSQLiteOpenHelper;

    /** Array of loader entries to hold for notifying changes */
    private final CopyOnWriteArrayList<SQLiteLoaderObserver> mActiveLoaders;

    /**
     * Gets a reference to the SQLIte Open Helper for the app, creating it if
     * necessary. This method is thread-safe. The Methods of this class should
     * not be accessed directly. Access them through the
     * {@linkplain DBInterface} class
     * 
     * @param context The Context reference
     * @return the reference to {@link BarterLiSQLiteOpenHelper}
     */
    static BarterLiSQLiteOpenHelper getInstance(final Context context) {

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
        mActiveLoaders = new CopyOnWriteArrayList<SQLiteLoaderObserver>();
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {

        //Create tables
        TableSearchBooks.create(db);
        TableLocations.create(db);
        TableUsers.create(db);
        TableChats.create(db);
        TableChatMessages.create(db);
        TableUserBooks.create(db);

        //Create Views
        ViewSearchBooksWithLocations.create(db);
        ViewChatsWithMessagesAndUsers.create(db);
        ViewUserBooksWithLocations.create(db);
        ViewUsersWithLocations.create(db);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        TableMyBooks.upgrade(db, oldVersion, newVersion);
        ViewMyBooksWithLocations.upgrade(db, oldVersion, newVersion);

        //Upgrade tables
        TableSearchBooks.upgrade(db, oldVersion, newVersion);
        TableLocations.upgrade(db, oldVersion, newVersion);
        TableUsers.upgrade(db, oldVersion, newVersion);
        TableChats.upgrade(db, oldVersion, newVersion);
        TableChatMessages.upgrade(db, oldVersion, newVersion);
        TableUserBooks.upgrade(db, oldVersion, newVersion);

        //Upgrade Views
        ViewSearchBooksWithLocations.upgrade(db, oldVersion, newVersion);
        ViewChatsWithMessagesAndUsers.upgrade(db, oldVersion, newVersion);
        ViewUserBooksWithLocations.upgrade(db, oldVersion, newVersion);
        ViewUsersWithLocations.upgrade(db, oldVersion, newVersion);

        if (oldVersion < 4) {
            migrateChats(db);
        }

    }

    /**
     * Migrate the chat messages & chats(if any) from the old way of
     * representation to the new way. The representation was changed in DB
     * version 4.
     */
    private void migrateChats(SQLiteDatabase db) {

        //Delete all the old chats
        db.delete(TableChats.NAME, null, null);

        //Build the chats from the chat messages
        final String orderBy = new StringBuilder(DatabaseColumns.CHAT_ID)
                        .append(SQLConstants.COMMA)
                        .append(DatabaseColumns.TIMESTAMP_EPOCH)
                        .append(SQLConstants.DESCENDING).toString();
        Logger.d(TAG, "Order by %s", orderBy);
        final Cursor cursor = db
                        .query(false, TableChatMessages.NAME, null, null, null, null, null, orderBy, null);

        if (cursor.getCount() > 0) {

            /*
             * Store all the visited chat ids so we don't need to convert them
             * again since they are already arranged according to descending
             * order of timestamps
             */
            final HashMap<String, ContentValues> chats = new HashMap<String, ContentValues>();
            final DateFormatter dateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.CHAT_TIME_FORMAT);

            String chatId, userId, timestamp;
            int chatStatus;
            ContentValues values;
            while (cursor.moveToNext()) {

                chatId = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.CHAT_ID));

                if (chats.containsKey(chatId)) {
                    continue;
                }

                values = new ContentValues(6);
                values.put(DatabaseColumns.CHAT_ID, chatId);
                values.put(DatabaseColumns.LAST_MESSAGE_ID, cursor.getString(cursor
                                .getColumnIndex(BaseColumns._ID)));
                chatStatus = cursor.getInt(cursor
                                .getColumnIndex(DatabaseColumns.CHAT_STATUS));

                switch (chatStatus) {

                    case ChatStatus.SENT: {
                        userId = cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.RECEIVER_ID));
                        break;
                    }

                    case ChatStatus.RECEIVED: {
                        userId = cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.SENDER_ID));
                        break;
                    }

                    default: {
                        //Shouldn't happen when migrating
                        continue;
                    }
                }

                values.put(DatabaseColumns.USER_ID, userId);
                timestamp = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TIMESTAMP));

                values.put(DatabaseColumns.TIMESTAMP, timestamp);
                try {
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, dateFormatter
                                    .getEpoch(timestamp));
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, dateFormatter
                                    .getOutputTimestamp(timestamp));
                } catch (ParseException e) {
                    // Not much we can do here
                    continue;
                }

                chats.put(chatId, values);

            }

            if (chats.size() > 0) {

                for (String eachChat : chats.keySet()) {
                    db.insert(TableChats.NAME, null, chats.get(eachChat));
                }
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
    Cursor query(final boolean distinct, final String table,
                    final String[] columns, final String selection,
                    final String[] selectionArgs, final String groupBy,
                    final String having, final String orderBy,
                    final String limit) {

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
     * @param autoNotify Whethr to automatically notify a change on the table
     *            which was inserted into
     * @return The row Id of the newly inserted row, or -1 if unable to insert
     */
    long insert(final String table, final String nullColumnHack,
                    final ContentValues values, final boolean autoNotify) {

        final SQLiteDatabase database = getWritableDatabase();
        final long insertRowId = database.insert(table, nullColumnHack, values);
        if (autoNotify && (insertRowId >= 0)) {
            notifyChange(table);
        }
        return insertRowId;
    }

    /**
     * Updates the table with the given data
     * 
     * @param table The table to update
     * @param values The fields to update
     * @param whereClause The WHERE clause
     * @param whereArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @return The number of rows updated
     */
    int update(final String table, final ContentValues values,
                    final String whereClause, final String[] whereArgs,
                    final boolean autoNotify) {

        final SQLiteDatabase database = getWritableDatabase();
        final int updateCount = database
                        .update(table, values, whereClause, whereArgs);

        if (autoNotify && (updateCount > 0)) {
            notifyChange(table);
        }
        return updateCount;
    }

    /**
     * Delete rows from the database
     * 
     * @param table The table to delete from
     * @param whereClause The WHERE clause
     * @param whereArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @return The number of rows deleted
     */
    int delete(final String table, final String whereClause,
                    final String[] whereArgs, final boolean autoNotify) {

        final SQLiteDatabase database = getWritableDatabase();
        final int deleteCount = database.delete(table, whereClause, whereArgs);

        if (autoNotify && (deleteCount > 0)) {
            notifyChange(table);
        }
        return deleteCount;
    }

    /**
     * Register a loader for maintaining notify changes
     * 
     * @param loader The {@link SQLiteLoader} loader to register
     * @param table The table name
     * @return The {@link SQLiteLoaderObserver} that was created. Use this to
     *         unregister the loader entry
     */
    SQLiteLoaderObserver registerLoader(final SQLiteLoader loader,
                    final String table) {

        Logger.d(TAG, "Add Loader Observer: %s", table);
        final SQLiteLoaderObserver entry = new SQLiteLoaderObserver(loader, table);
        mActiveLoaders.add(entry);
        return entry;
    }

    void unregisterLoader(final SQLiteLoaderObserver entry) {

        Logger.d(TAG, "Remove Loader Observer: %s", entry.table);
        mActiveLoaders.remove(entry);
    }

    /**
     * Notify loaders whenever a table is modified
     * 
     * @param table The table that was modified
     */
    void notifyChange(final String table) {
        //TODO Optimize this later, Maybe a sorted list of loaders by table name?
        for (final SQLiteLoaderObserver entry : mActiveLoaders) {
            Logger.d(TAG, "Notify change: %s", entry.table);
            /*
             * Using contains instead of equals because we are using View, which
             * are just named by appending the table names. Notifying should
             * also update any Loaders connected to the Views
             */
            if (entry.table.contains(table)) {
                entry.loader.onContentChanged();
            }
        }
    }

}
