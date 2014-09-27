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

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.utils.AppConstants.ChatStatus;

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
    private static final int INCOMING_MESSAGE = 0;
    private static final int OUTGOING_MESSAGE = 1;

    private final String     TAG              = "ChatDetailAdapter";

    /* Strings that indicate the chat status */
    private final String     mSendingString;
    private final String     mFailedString;

    @SuppressLint("UseSparseArrays")
    /* Sparse Array benefits are noticable only upwards of 10k items */
    public ChatDetailAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
        mSendingString = context.getString(R.string.sending);
        mFailedString = context.getString(R.string.failed);
    }

    @Override
    public int getItemViewType(final int position) {

        mCursor.moveToPosition(position);
        final int chatStatus = mCursor.getInt(mCursor
                        .getColumnIndex(DatabaseColumns.CHAT_STATUS));

        switch (chatStatus) {

            case ChatStatus.FAILED:
            case ChatStatus.SENDING:
            case ChatStatus.SENT: {
                return OUTGOING_MESSAGE;
            }

            case ChatStatus.RECEIVED: {
                return INCOMING_MESSAGE;
            }

            default: {
                throw new IllegalStateException("Unknown chat status");
            }
        }
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
        final String timestamp = cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN));
        view.setTag(R.string.tag_resend_on_click, false);

        if (itemViewType == INCOMING_MESSAGE) {
            ((TextView) view.getTag(R.id.chat_ack)).setText(timestamp);
        } else if (itemViewType == OUTGOING_MESSAGE) {

            final int chatStatus = cursor.getInt(cursor
                            .getColumnIndex(DatabaseColumns.CHAT_STATUS));
            final TextView chatStatusTextView = ((TextView) view
                            .getTag(R.id.chat_ack));

            switch (chatStatus) {

                case ChatStatus.SENDING: {
                    chatStatusTextView.setText(mSendingString);
                    break;
                }
                case ChatStatus.SENT: {
                    chatStatusTextView.setText(timestamp);
                    break;
                }
                case ChatStatus.FAILED: {
                    chatStatusTextView.setText(mFailedString);
                    view.setTag(R.string.tag_resend_on_click, true);
                    break;
                }
            }

        }

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                    final ViewGroup parent) {

        final int viewType = getItemViewType(cursor.getPosition());
        View view = null;
        if (viewType == INCOMING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_incoming_chat, parent, false);
            view.setTag(R.id.chat_ack, view.findViewById(R.id.chat_ack));
        } else if (viewType == OUTGOING_MESSAGE) {
            view = LayoutInflater
                            .from(context)
                            .inflate(R.layout.layout_outgoing_chat, parent, false);
            view.setTag(R.id.chat_ack, view.findViewById(R.id.chat_ack));
        }

        view.setTag(R.id.image_user, view.findViewById(R.id.image_user));
        view.setTag(R.id.text_chat_message, view
                        .findViewById(R.id.text_chat_message));
        return view;
    }

}
