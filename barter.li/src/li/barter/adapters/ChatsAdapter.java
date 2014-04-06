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

package li.barter.adapters;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.data.DatabaseColumns;

/**
 * Adapter for displaying list of all ongoing chats
 * 
 * @author Vinay S Shenoy
 */
public class ChatsAdapter extends CursorAdapter {

    private static final String TAG             = "ChatsAdapter";

    private final String        mUserNameFormat = "%s %s";

    public ChatsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

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

        Picasso.with(context)
                        .load(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.PROFILE_PICTURE)))
                        .fit().error(R.drawable.pic_avatar)
                        .into((ImageView) view.getTag(R.id.image_user));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                        .inflate(R.layout.layout_chat_item, parent, false);

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_user_name, view.findViewById(R.id.text_user_name));
        view.setTag(R.id.text_chat_message, view
                        .findViewById(R.id.text_chat_message));
        view.setTag(R.id.text_chat_time, view.findViewById(R.id.text_chat_time));
        return view;
    }

}
