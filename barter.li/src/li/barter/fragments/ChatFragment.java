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

package li.barter.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import li.barter.R;
import li.barter.adapters.ChatAdapter;
import li.barter.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import li.barter.chat.ChatService;
import li.barter.chat.ChatService.ChatServiceBinder;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;

/**
 * Activity for displaying Chat Messages
 * 
 * @author Vinay S Shenoy
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ChatFragment extends AbstractBarterLiFragment implements
                OnReceiveMessageHandler, ServiceConnection {

    private static final String TAG = "ChatFragment";

    private ChatAdapter         mChatAdapter;

    private ListView            mChatListView;

    private ChatService         mChatService;

    private boolean             mBoundToChatService;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        final View view = inflater
                        .inflate(R.layout.fragment_chat, container, false);
        mChatListView = (ListView) view.findViewById(R.id.list_chats);
        mChatAdapter = new ChatAdapter(getActivity());
        mChatListView.setAdapter(mChatAdapter);

        setActionBarDrawerToggleEnabled(false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(activity, ChatService.class);
        activity.bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_books_around_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_scan_book) {
            if (mBoundToChatService) {
                mChatService.sendMessageToUser("", "This is a test message. Hope it works. ^_^");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        if (mBoundToChatService) {
            getActivity().unbindService(this);
        }
        super.onPause();
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onReceiveMessage(final byte[] message) {

    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundToChatService = false;
    }
}
