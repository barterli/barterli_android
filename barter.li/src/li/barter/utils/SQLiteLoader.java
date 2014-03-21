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

package li.barter.utils;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import li.barter.data.BarterLiSQLiteOpenHelper;

/**
 * Custom {@link Loader} implementation to read from
 * {@link BarterLiSQLiteOpenHelper}
 * 
 * @author vinaysshenoy
 */
public class SQLiteLoader extends AsyncTaskLoader<Cursor> {
    
    private static final String TAG = "SQLiteLoader";

    /**
     * Cursor loaded from the SQLiteDatabase
     */
    private Cursor   mCursor;

    private boolean  mDistinct;

    private String   mTable;

    private String[] mColumns;

    private String   mSelection;

    private String[] mSelectionArgs;

    private String   mGroupBy;

    private String   mHaving;

    private String   mOrderBy;

    private String   mLimit;

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
    public SQLiteLoader(Context context, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
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
        return BarterLiSQLiteOpenHelper.getInstance(getContext()).query(
                        mDistinct, mTable, mColumns, mSelection,
                        mSelectionArgs, mGroupBy, mHaving, mOrderBy, mLimit);
    }

    @Override
    protected void onStartLoading() {
        // TODO Auto-generated method stub
        super.onStartLoading();
    }

    @Override
    protected void onStopLoading() {
        // TODO Auto-generated method stub
        super.onStopLoading();
    }

    @Override
    protected void onReset() {
        // TODO Auto-generated method stub
        super.onReset();
    }

    @Override
    public void onCanceled(Cursor data) {
        // TODO Auto-generated method stub
        super.onCanceled(data);
    }

    @Override
    public void deliverResult(Cursor data) {
        super.deliverResult(data);
    }

}
