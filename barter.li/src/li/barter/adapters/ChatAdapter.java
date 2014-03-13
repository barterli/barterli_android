
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

    private ArrayList<String> mChatMessages;
    private LayoutInflater    mLayoutInflater;

    public ChatAdapter(Context context) {
        mChatMessages = new ArrayList<String>();
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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
    public void addMessage(String message) {
        mChatMessages.add(message);
        notifyDataSetChanged();
    }

}
