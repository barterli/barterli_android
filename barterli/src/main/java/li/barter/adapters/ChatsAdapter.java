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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AvatarBitmapTransformation;
import li.barter.widgets.RoundedCornerImageView;

/**
 * Adapter for displaying list of all ongoing chats
 *
 * @author Vinay S Shenoy
 */
public class ChatsAdapter extends CursorAdapter {

    private static final String TAG = "ChatsAdapter";

    private final String mUserNameFormat = "%s %s";

    public ChatsAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_user_name))
                .setText(String.format(mUserNameFormat, cursor.getString(cursor
                                                                                 .getColumnIndex(DatabaseColumns.FIRST_NAME)), cursor
                                               .getString(cursor
                                                                  .getColumnIndex(DatabaseColumns.LAST_NAME))));

        ((TextView) view.getTag(R.id.text_chat_message))
                .setText(cursor.getString(cursor
                                                  .getColumnIndex(DatabaseColumns.MESSAGE)));

        ((TextView) view.getTag(R.id.text_chat_time))
                .setText(cursor.getString(cursor
                                                  .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN)));


        final RoundedCornerImageView roundedCornerImageView = (RoundedCornerImageView) view.getTag(R.id.image_user);
        final AvatarBitmapTransformation bitmapTransformation = (AvatarBitmapTransformation) view.getTag(R.string.tag_avatar_transformation);

        Picasso.with(context)
               .load(cursor.getString(cursor
                                              .getColumnIndex(DatabaseColumns.PROFILE_PICTURE)))
               .error(R.drawable.pic_avatar)
               .transform(bitmapTransformation)
               .into(roundedCornerImageView.getTarget());


    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                                        .inflate(R.layout.layout_chat_item, parent, false);

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_user_name, view.findViewById(R.id.text_user_name));
        view.setTag(R.id.text_chat_message, view
                .findViewById(R.id.text_chat_message));
        view.setTag(R.id.text_chat_time, view.findViewById(R.id.text_chat_time));
        view.setTag(R.string.tag_avatar_transformation, new AvatarBitmapTransformation(AvatarBitmapTransformation.AvatarSize.MEDIUM));
        return view;
    }

}
