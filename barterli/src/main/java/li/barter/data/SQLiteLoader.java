/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

/**
 * Custom {@link Loader} implementation to read from
 * {@link BarterLiSQLiteOpenHelper}
 * 
 * @author vinaysshenoy
 */
public class SQLiteLoader extends AsyncTaskLoader<Cursor> {

    private static final String  TAG = "SQLiteLoader";

    /**
     * Cursor loaded from the SQLiteDatabase
     */
    private Cursor               mCursor;

    private final boolean        mDistinct;

    private final String         mTable;

    private final String[]       mColumns;

    private final String         mSelection;

    private final String[]       mSelectionArgs;

    private final String         mGroupBy;

    private final String         mHaving;

    private final String         mOrderBy;

    private final String         mLimit;

    private SQLiteLoaderObserver mObserver;

    /**
     * Construct a loader to load from the database
     * 
     * @param context Reference to a {@link Context}
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
    public SQLiteLoader(final Context context, final boolean distinct, final String table, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        super(context);
        mDistinct = distinct;
        mTable = table;
        mColumns = columns;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mGroupBy = groupBy;
        mHaving = having;
        mOrderBy = orderBy;
        mLimit = limit;
    }

    @Override
    public Cursor loadInBackground() {
        return BarterLiSQLiteOpenHelper
                        .getInstance(getContext())
                        .query(mDistinct, mTable, mColumns, mSelection, mSelectionArgs, mGroupBy, mHaving, mOrderBy, mLimit);
    }

    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }

        if (mObserver == null) {
            mObserver = BarterLiSQLiteOpenHelper.getInstance(getContext())
                            .registerLoader(this, mTable);
        }

        if (takeContentChanged() || (mCursor == null)) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if ((mCursor != null) && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
        if (mObserver != null) {
            BarterLiSQLiteOpenHelper.getInstance(getContext())
                            .unregisterLoader(mObserver);
        }
        mObserver = null;
    }

    @Override
    public void onCanceled(final Cursor data) {
        if ((data != null) && !data.isClosed()) {
            data.close();
        }
    }

    @Override
    public void deliverResult(final Cursor data) {

        if (isReset()) {
            if (data != null) {
                data.close();
            }
            return;
        }
        final Cursor oldCursor = mCursor;
        mCursor = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if ((oldCursor != null) && (oldCursor != data) && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

}
