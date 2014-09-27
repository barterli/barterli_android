/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants;
import li.barter.utils.AvatarBitmapTransformation;
import li.barter.widgets.RoundedCornerImageView;

/**
 * Adapter used to display information for books around me
 *
 * @author Vinay S Shenoy
 */
public class BooksGridAdapter extends CursorAdapter {

    private static final String TAG = "BooksGridAdapter";

    private static final int VIEW_BOOK = 0;
    private static final int VIEW_GRAPHIC = 1;

    private boolean mAddGraphicEnabled;

    private int mCount = 0;

    /**
     * @param context           A reference to the {@link Context}
     * @param addGraphicEnabled Whether the addGraphic should be added to the end of the adapter
     *                          data set
     */
    public BooksGridAdapter(final Context context, final boolean addGraphicEnabled) {
        super(context, null, 0);
        mCount = 0;
        mAddGraphicEnabled = addGraphicEnabled;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public void notifyDataSetChanged() {

        mCount = 0;
        if (mCursor != null && !mCursor.isClosed()) {
            if (mCursor.getCount() > 0) {
                mCount = mCursor.getCount() + (mAddGraphicEnabled ? 1 : 0);//Empty graphic
            }
        }

        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (mAddGraphicEnabled && (position == (mCount - 1))) ? VIEW_GRAPHIC
                : VIEW_BOOK;
    }

    @Override
    public int getViewTypeCount() {
        return mAddGraphicEnabled ? 2 : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int viewType = getItemViewType(position);
        View view = convertView;
        if (viewType == VIEW_BOOK) {

            if (view == null) {
                view = inflateBookView(parent);
            }

            if (!mCursor.isClosed()) {
                mCursor.moveToPosition(position);
                bindView(view, parent.getContext(), mCursor);
            }

        } else if (viewType == VIEW_GRAPHIC) {

            if (view == null) {
                view = inflateGraphicView(parent);
            }
        }

        return view;
    }

    /**
     * @param parent
     * @return
     */
    private View inflateGraphicView(ViewGroup parent) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_add_graphic, parent, false);
        return view;
    }

    /**
     * @param parent
     * @return
     */
    private View inflateBookView(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_book, parent, false);

        view.setTag(R.id.image_book, view.findViewById(R.id.image_book));
        view.setTag(R.id.text_book_name, view.findViewById(R.id.text_book_name));
        view.setTag(R.id.text_book_author, view
                .findViewById(R.id.text_book_author));
        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.string.tag_avatar_transformation, new AvatarBitmapTransformation(AvatarBitmapTransformation.AvatarSize.X_SMALL));
        return view;
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

        final String bookImageUrl = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.IMAGE_URL));

        //if book image not present
        if (bookImageUrl == null || bookImageUrl.contains(AppConstants.FALSE)) {

            ((TextView) view.getTag(R.id.text_book_name))
                    .setVisibility(View.VISIBLE);
            ((TextView) view.getTag(R.id.text_book_author))
                    .setVisibility(View.VISIBLE);

            // this gives blank image. Its a hack for disabling caching issue for no image present book

            Picasso.with(context).load(bookImageUrl).fit()
                    .into((ImageView) view.getTag(R.id.image_book));
        } else {

            Picasso.with(context).load(bookImageUrl).fit()
                    .into((ImageView) view.getTag(R.id.image_book));
            ((TextView) view.getTag(R.id.text_book_name))
                    .setVisibility(View.GONE);
            ((TextView) view.getTag(R.id.text_book_author))
                    .setVisibility(View.GONE);

        }

        final String ownerImageUrl = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.BOOK_OWNER_IMAGE_URL));

        final RoundedCornerImageView roundedCornerImageView = (RoundedCornerImageView) view
                .getTag(R.id.image_user);
        final AvatarBitmapTransformation bitmapTransformation = (AvatarBitmapTransformation) view
                .getTag(R.string.tag_avatar_transformation);

        if (!TextUtils.isEmpty(ownerImageUrl)) {

            roundedCornerImageView.setImageResource(0);

            Picasso.with(context)
                    .load(ownerImageUrl)
                    .transform(bitmapTransformation)
                    .into(roundedCornerImageView.getTarget());

        } else {
            //TODO DIsplay default image for user
        }

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {

        return null;
    }

}
