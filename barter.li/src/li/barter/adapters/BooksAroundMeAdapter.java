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

import com.android.volley.toolbox.ImageLoader;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import li.barter.R;
import li.barter.data.DatabaseColumns;

/**
 * Adapter used to display information for books around me
 * 
 * @author Vinay S Shenoy
 */
public class BooksAroundMeAdapter extends CursorAdapter {

    private final ImageLoader mImageLoader;

    /**
     * @param context A reference to the {@link Context}
     * @param imageLoader An {@link ImageLoader} reference for loading images
     *            from the network
     */
    public BooksAroundMeAdapter(final Context context, final ImageLoader imageLoader) {
        super(context, null, 0);
        mImageLoader = imageLoader;
    }

    @Override
    public void bindView(final View view, final Context context,
                    final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_book_name))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.TITLE)));
        ((TextView) view.getTag(R.id.text_book_desc))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.DESCRIPTION)));
        /*
         * ((NetworkImageView) view.getTag(R.id.image_book))
         * .setImageUrl(cursor.getString(cursor
         * .getColumnIndex(DatabaseColumns.IMAGE_URL)), mImageLoader);
         */
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_item_book, parent, false);

        view.setTag(R.id.image_book, view.findViewById(R.id.image_book));
        view.setTag(R.id.text_book_name, view.findViewById(R.id.text_book_name));
        view.setTag(R.id.text_book_desc, view.findViewById(R.id.text_book_desc));
        return view;
    }

}
