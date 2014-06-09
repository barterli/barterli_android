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

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants;
import li.barter.utils.Logger;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.widgets.CircleImageView;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Class to display Chat messages
 * 
 * @author Vinay S Shenoy
 */
public class ChatDetailAdapter extends CursorAdapter {

    /*
     * View Types. If there are n types of views, these HAVE to be numbered from
     * 0 to n-1
     */
    private static final int            INCOMING_MESSAGE = 0;
    private static final int            OUTGOING_MESSAGE = 1;
    /**
     * Map to maintain a reference between the positions and the view types
     */
    private final Map<Integer, Integer> mPositionViewTypeMap;

    private final String TAG="ChatDetailAdapter";
    /**
     * Profile picture of the user the current user is chatting with
     */
    private String                      mChatUserProfilePic;

    public ChatDetailAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
        mPositionViewTypeMap = new HashMap<Integer, Integer>();
        buildMapForCursor(cursor);
    }

    public void setChatUserProfilePic(final String profileImage) {
        mChatUserProfilePic = profileImage;
        notifyDataSetChanged();
    }

    /**
     * Traverses the cursor once to make a map of the position to the view type
     * for in memory lookup. Call this whenever the cursor backing the adapter
     * changes
     * 
     * @param cursor The cursor to traverse
     */
    private void buildMapForCursor(final Cursor cursor) {

        mPositionViewTypeMap.clear();
        if ((cursor != null) && !cursor.isClosed()) {
            cursor.moveToPosition(-1);
            String receiverId = null;
            while (cursor.moveToNext()) {
                receiverId = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.RECEIVER_ID));

                if (receiverId.equals(UserInfo.INSTANCE.getId())) {
                    //Incoming message 
                    mPositionViewTypeMap
                                    .put(cursor.getPosition(), INCOMING_MESSAGE);
                } else {
                    //Outgoing message
                    mPositionViewTypeMap
                                    .put(cursor.getPosition(), OUTGOING_MESSAGE);
                }
            }
        }
    }

    @Override
    public void notifyDataSetChanged() {
        buildMapForCursor(mCursor);
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(final int position) {

        return mPositionViewTypeMap.get(position);
    }

    @Override
    public int getViewTypeCount() {
        //Incoming and outgoing message
        return 2;
    }

    @Override
    public void bindView(final View view, final Context context,
                    final Cursor cursor) {

        ((TextView) view.getTag(R.id.text_chat_message))
                        .setText(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.MESSAGE)));

        final int itemViewType = getItemViewType(cursor.getPosition());
        if (itemViewType == INCOMING_MESSAGE) {
            if (!TextUtils.isEmpty(mChatUserProfilePic)) {
            	 CircleImageView circleImageView=(CircleImageView) view.getTag(R.id.image_user);
            	 Picasso.with(context)
                 .load(mChatUserProfilePic)
                  .error(R.drawable.pic_avatar)
                 .resizeDimen(R.dimen.chat_detail_image_size, R.dimen.chat_detail_image_size)
                 .centerCrop().into(circleImageView.getTarget());
            }
        } else if (itemViewType == OUTGOING_MESSAGE) {
            final String imageUrl = UserInfo.INSTANCE.getProfilePicture();
            Logger.d(TAG,imageUrl);
            if(cursor.getString(cursor
                                        .getColumnIndex(DatabaseColumns.CHAT_ACK)).equals(context.getResources().getString(R.string.sent)))
            {
            	  ((TextView) view.getTag(R.id.chat_ack))
                  .setText(context.getResources().getString(R.string.sent));
            }
            else
            {
            	 ((TextView) view.getTag(R.id.chat_ack))
                 .setText(context.getResources().getString(R.string.sending));
            }
          
            CircleImageView circleImageView=(CircleImageView) view.getTag(R.id.image_user);
            if (!TextUtils.isEmpty(imageUrl)) {
            	
            	 Picasso.with(context)
                 .load(imageUrl)
                 .resizeDimen(R.dimen.chat_detail_image_size, R.dimen.chat_detail_image_size)
                 .centerCrop().into(circleImageView.getTarget());
            }
        }

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {

        final int viewType = mPositionViewTypeMap.get(cursor.getPosition());
        View view = null;
        if (viewType == INCOMING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_incoming_chat, parent, false);
        } else if (viewType == OUTGOING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_outgoing_chat, parent, false);
            view.setTag(R.id.chat_ack, view
                    .findViewById(R.id.chat_ack));
        }

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_chat_message, view
                        .findViewById(R.id.text_chat_message));
        return view;
    }

}
