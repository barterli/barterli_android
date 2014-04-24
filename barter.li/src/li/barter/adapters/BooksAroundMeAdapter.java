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

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants;

/**
 * Adapter used to display information for books around me
 * 
 * @author Vinay S Shenoy
 */
public class BooksAroundMeAdapter extends CursorAdapter {

    private static final String TAG = "BooksAroundMeAdapter";

    /**
     * Format string for formatting the location of books
     */
    private final String        mLocationFormat;

    /**
     * @param context A reference to the {@link Context}
     */
    public BooksAroundMeAdapter(final Context context) {
        super(context, null, 0);
        mLocationFormat = context.getString(R.string.location_format);
    }

    @Override
    public void bindView(final View view, final Context context,
                    final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_book_name))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.TITLE)));
        ((TextView) view.getTag(R.id.text_book_desc))
                        .setText(Html.fromHtml(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.DESCRIPTION))));

        ((TextView) view.getTag(R.id.text_book_location))
                        .setText(String.format(mLocationFormat, cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.NAME)), cursor
                                        .getString(cursor
                                                        .getColumnIndex(DatabaseColumns.ADDRESS))));
        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.IMAGE_URL))
                        .contains(AppConstants.DEFAULT_BOOKIMAGE_URL)) {
            ((ImageView) view.getTag(R.id.image_book))
                            .setImageResource(R.drawable.default_book_icon);
        } else {
            Picasso.with(context)
                            .load(cursor.getString(cursor
                                            .getColumnIndex(DatabaseColumns.IMAGE_URL)))
                            .fit()
                            .into((ImageView) view.getTag(R.id.image_book));
        }
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_item_book, parent, false);

        view.setTag(R.id.image_book, view.findViewById(R.id.image_book));
        view.setTag(R.id.text_book_name, view.findViewById(R.id.text_book_name));
        view.setTag(R.id.text_book_desc, view.findViewById(R.id.text_book_desc));
        view.setTag(R.id.text_book_location, view
                        .findViewById(R.id.text_book_location));

        return view;
    }

}
