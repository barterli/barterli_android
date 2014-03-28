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
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import li.barter.BarterLiApplication;
import li.barter.data.DBInterface.AsyncDbQueryCallback;

/**
 * Class to encapsulate asynchronous database queries. This class should not be
 * used directly. Instead, send in your queries through the
 * <code>DBUtils.async()</code> methods.
 * 
 * @author Vinay S Shenoy
 */
@SuppressLint("UseSparseArrays")
class BarterLiAsyncQueryHandler {

    /**
     * The type of DB Query
     */
    private enum Type {
        INSERT,
        UPDATE,
        DELETE,
        QUERY
    }

    /**
     * Map of tokens to async tasks
     */
    private Map<Integer, QueryTask> mTasks;

    BarterLiAsyncQueryHandler() {

        /*
         * Don't worry about sparse array optimization. Real world tests have
         * shown that the performance benefits are seen only if the data size is
         * > 10k or so, in which case we should be worrying about our
         * implementation, rather than performance
         */

        //http://www.javacodegeeks.com/2012/07/android-performance-tweaking-parsearray.html
        mTasks = new HashMap<Integer, BarterLiAsyncQueryHandler.QueryTask>();
    }

    /**
     * Method for inserting rows into the database
     * 
     * @param token Unique id for this operation
     * @param table The table to insert into
     * @param nullColumnHack column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter
     *            provides the name of nullable column name to explicitly insert
     *            a NULL into in the case where your values is empty.
     * @param values The fields to insert
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startInsert(final int token, final String table,
                    final String nullColumnHack, final ContentValues values,
                    final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.INSERT, token, callback);
        task.mTableName = table;
        task.mNullColumnHack = nullColumnHack;
        task.mValues = values;
        task.mAutoNotify = autoNotify;
        mTasks.put(token, task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);
    }

    /**
     * Delete rows from the database
     * 
     * @param token A unique id for this operation
     * @param table The table to delete from
     * @param selection The WHERE clause
     * @param selectionArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startDelete(final int token, final String table,
                    final String selection, final String[] selectionArgs,
                    final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.DELETE, token, callback);
        task.mTableName = table;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mAutoNotify = autoNotify;
        mTasks.put(token, task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Updates the table with the given data
     * 
     * @param A unique id for this operation
     * @param table The table to update
     * @param values The fields to update
     * @param selection The WHERE clause
     * @param selectionArgs Arguments for the where clause
     * @param autoNotify Whether to automatically notify any changes to the
     *            table
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startUpdate(final int token, final String table,
                    final ContentValues values, final String selection,
                    final String[] selectionArgs, final boolean autoNotify,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.UPDATE, token, callback);
        task.mTableName = table;
        task.mValues = values;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mAutoNotify = autoNotify;
        mTasks.put(token, task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Query the given table, returning a Cursor over the result set.
     * 
     * @param token A unique id for this query
     * @param distinct <code>true</code> if dataset should be unique
     * @param table The table to query
     * @param columns The columns to fetch
     * @param selection The selection string, formatted as a WHERE clause
     * @param selectionArgs The arguments for the selection parameter
     * @param groupBy GROUP BY clause
     * @param having HAVING clause
     * @param orderBy ORDER BY clause
     * @param limit LIMIT clause
     * @param callback A {@link AsyncDbQueryCallback} to be notified when the
     *            async operation finishes
     */
    void startQuery(final int token, final boolean distinct,
                    final String table, final String[] columns,
                    final String selection, final String[] selectionArgs,
                    final String groupBy, final String having,
                    final String orderBy, final String limit,
                    final AsyncDbQueryCallback callback) {

        final QueryTask task = new QueryTask(Type.QUERY, token, callback);
        task.mDistinct = distinct;
        task.mTableName = table;
        task.mColumns = columns;
        task.mSelection = selection;
        task.mSelectionArgs = selectionArgs;
        task.mGroupBy = groupBy;
        task.mHaving = having;
        task.mOrderBy = orderBy;
        task.mLimit = limit;

        mTasks.put(token, task);
        new QueryAsyncTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task);

    }

    /**
     * Cancels any pending async operations
     * 
     * @param token The token to cancel
     */
    void cancel(int token) {

        for (Iterator<Map.Entry<Integer, QueryTask>> it = mTasks.entrySet()
                        .iterator(); it.hasNext();) {
            Map.Entry<Integer, QueryTask> entry = it.next();
            QueryTask task = entry.getValue();
            if (task.mToken == token) {
                task.mCancelled = true;
                task.mCallback = null;
                it.remove();
            }
        }
    }

    /**
     * Class that holds the result of the DB Operation
     * 
     * @author Vinay S Shenoy
     */
    private static final class QueryResult {

        /**
         * The task that triggered this result
         */
        private final QueryTask mTask;

        /**
         * Inserted row id in case of an insert operation
         */
        private long            mInsertRowId;

        /**
         * Update count in case of an update operation
         */
        private int             mUpdateCount;

        /**
         * Delete count in case of a delete operation
         */
        private int             mDeleteCount;

        /**
         * Cursor in case of a query operation
         */
        private Cursor          mCursor;

        /**
         * Construct a query result
         * 
         * @param task The {@link QueryTask} that triggered this result
         */
        private QueryResult(QueryTask task) {
            mTask = task;
        }
    }

    /**
     * @author Vinay S Shenoy Clas representing a query task
     */
    private static final class QueryTask {

        /**
         * Type of the operation, whether it is INSERT, UPDATE, DELETE or QUERY
         */
        private final Type           mType;

        /**
         * The token for the task
         */
        private final int            mToken;

        /**
         * Callback for the result of the operation
         */
        private AsyncDbQueryCallback mCallback;

        private String               mTableName;
        private boolean              mDistinct;
        private String[]             mColumns;
        private String               mSelection;
        private String[]             mSelectionArgs;
        private String               mGroupBy;
        private String               mOrderBy;
        private String               mHaving;
        private String               mLimit;
        private String               mNullColumnHack;
        private ContentValues        mValues;
        private boolean              mAutoNotify;

        private boolean              mCancelled;

        /**
         * Construct a Query Task
         * 
         * @param type The {@link Type} of task
         * @param token The token to pass into the callback
         * @param callback The Callback for when the db query completes
         */
        private QueryTask(Type type, int token, AsyncDbQueryCallback callback) {
            mType = type;
            mToken = token;
            mCallback = callback;
            mCancelled = false;
        }

    }

    /**
     * Custom AsyncTask to do the background work for database operations
     * 
     * @author Vinay S Shenoy
     */
    private static class QueryAsyncTask extends
                    AsyncTask<QueryTask, Void, QueryResult> {

        @Override
        protected QueryResult doInBackground(QueryTask... params) {

            final QueryTask task = params[0];
            final QueryResult result = new QueryResult(task);

            if (task.mCancelled) {
                return result;
            }

            switch (task.mType) {
                case INSERT: {
                    result.mInsertRowId = BarterLiSQLiteOpenHelper
                                    .getInstance(BarterLiApplication.getStaticContext())
                                    .insert(task.mTableName, task.mNullColumnHack, task.mValues, task.mAutoNotify);
                }

                case DELETE: {
                    result.mDeleteCount = BarterLiSQLiteOpenHelper
                                    .getInstance(BarterLiApplication.getStaticContext())
                                    .delete(task.mTableName, task.mSelection, task.mSelectionArgs, task.mAutoNotify);
                }

                case UPDATE: {
                    result.mUpdateCount = BarterLiSQLiteOpenHelper
                                    .getInstance(BarterLiApplication.getStaticContext())
                                    .update(task.mTableName, task.mValues, task.mSelection, task.mSelectionArgs, task.mAutoNotify);

                }

                case QUERY: {
                    result.mCursor = BarterLiSQLiteOpenHelper
                                    .getInstance(BarterLiApplication.getStaticContext())
                                    .query(task.mDistinct, task.mTableName, task.mColumns, task.mSelection, task.mSelectionArgs, task.mGroupBy, task.mHaving, task.mOrderBy, task.mLimit);
                }
            }

            return result;

        }

        @Override
        protected void onPostExecute(QueryResult result) {

            if (!result.mTask.mCancelled) {
                switch (result.mTask.mType) {

                    case INSERT: {
                        result.mTask.mCallback
                                        .onInsertComplete(result.mTask.mToken, result.mInsertRowId);
                        break;
                    }

                    case DELETE: {
                        result.mTask.mCallback
                                        .onDeleteComplete(result.mTask.mToken, result.mDeleteCount);
                        break;
                    }

                    case UPDATE: {
                        result.mTask.mCallback
                                        .onUpdateComplete(result.mTask.mToken, result.mUpdateCount);
                        break;
                    }

                    case QUERY: {
                        result.mTask.mCallback
                                        .onQueryComplete(result.mTask.mToken, result.mCursor);
                        break;
                    }
                }
            }
        }
    }

}
