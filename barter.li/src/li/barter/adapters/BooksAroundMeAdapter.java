/*******************************************************************************
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
 ******************************************************************************/

package li.barter.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import li.barter.R;
import li.barter.data.DatabaseColumns;

public class BooksAroundMeAdapter extends CursorAdapter {

    public BooksAroundMeAdapter(Context context) {
        super(context, null, 0);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ((TextView) view.getTag(R.id.textBookName))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.TITLE)));
        ((TextView) view.getTag(R.id.textBookDesc))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.DESCRIPTION)));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_item_book, parent, false);

        view.setTag(R.id.imageBook, view.findViewById(R.id.imageBook));
        view.setTag(R.id.textBookName, view.findViewById(R.id.textBookName));
        view.setTag(R.id.textBookDesc, view.findViewById(R.id.textBookDesc));
        return view;
    }

}
