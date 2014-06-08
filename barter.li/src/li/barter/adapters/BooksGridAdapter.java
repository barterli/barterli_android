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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants;
import li.barter.widgets.CircleImageView;

/**
 * Adapter used to display information for books around me
 * 
 * @author Vinay S Shenoy
 */
public class BooksGridAdapter extends CursorAdapter {

    private static final String TAG = "BooksGridAdapter";

    /**
     * @param context A reference to the {@link Context}
     */
    public BooksGridAdapter(final Context context) {
        super(context, null, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                    final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_book_name))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.TITLE)));

        ((TextView) view.getTag(R.id.text_book_author))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.AUTHOR)));

        /*((TextView) view.getTag(R.id.text_book_location))
                        .setText(String.format(mLocationFormat, cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.NAME)), cursor
                                        .getString(cursor
                                                        .getColumnIndex(DatabaseColumns.ADDRESS))));*/

        final String bookImageUrl = cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.IMAGE_URL));

        //if book image not present
        if (bookImageUrl == null
                        || bookImageUrl.contains(AppConstants.FALSE)) {
        	
        	((TextView) view.getTag(R.id.text_book_name)).setVisibility(View.VISIBLE);
        	((TextView) view.getTag(R.id.text_book_author)).setVisibility(View.VISIBLE);
        	
        	// this gives blank image. Its a hack for disabling caching issue for no image present book
        	
        	 Picasso.with(context).load(bookImageUrl).fit()
             .into((ImageView) view.getTag(R.id.image_book));
        } else {

            Picasso.with(context).load(bookImageUrl).fit()
                            .into((ImageView) view.getTag(R.id.image_book));
            ((TextView) view.getTag(R.id.text_book_name)).setVisibility(View.GONE);
        	((TextView) view.getTag(R.id.text_book_author)).setVisibility(View.GONE);

        }

        final String ownerImageUrl = cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.BOOK_OWNER_IMAGE_URL));

        if (!TextUtils.isEmpty(ownerImageUrl)) {
            final CircleImageView circleImageView = (CircleImageView) view
                            .getTag(R.id.image_user);

            Picasso.with(context)
                            .load(ownerImageUrl)
                            .resizeDimen(R.dimen.book_user_image_size, R.dimen.book_user_image_size)
                            .into(circleImageView.getTarget());
        } else {
            //TODO DIsplay default image for user
        }
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_item_book, parent, false);

        view.setTag(R.id.image_book, view.findViewById(R.id.image_book));
        view.setTag(R.id.text_book_name, view.findViewById(R.id.text_book_name));
        view.setTag(R.id.text_book_author, view
                        .findViewById(R.id.text_book_author));
       /* view.setTag(R.id.text_book_location, view
                        .findViewById(R.id.text_book_location));*/
        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));

        return view;
    }

}
