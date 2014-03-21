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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import li.barter.R;

/**
 * @author vinaysshenoy Class to display Chat messages
 */
public class ChatAdapter extends BaseAdapter {

    private final ArrayList<String> mChatMessages;
    private final LayoutInflater    mLayoutInflater;

    public ChatAdapter(final Context context) {
        mChatMessages = new ArrayList<String>();
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mChatMessages.size();
    }

    @Override
    public Object getItem(final int position) {
        return mChatMessages.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView,
                    final ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.layout_chat_item, null);
        }

        ((TextView) view).setText((String) getItem(position));
        return view;
    }

    /**
     * Clear all messages stored in this adapter
     */
    public void clearMessages() {
        mChatMessages.clear();
        notifyDataSetChanged();
    }

    /**
     * Adds a message to the Chat List
     * 
     * @param message The message added
     */
    public void addMessage(final String message) {
        mChatMessages.add(message);
        notifyDataSetChanged();
    }

}
