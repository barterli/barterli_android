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

package li.barter.activities;

import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;

import li.barter.R;
import li.barter.adapters.ChatAdapter;
import li.barter.http.HttpConstants;
import li.barter.http.rabbitmq.AbstractRabbitMQConnector.ExchangeType;
import li.barter.http.rabbitmq.ChatRabbitMQConnector;
import li.barter.http.rabbitmq.ChatRabbitMQConnector.OnReceiveMessageHandler;

/**
 * @author vinaysshenoy Activity for displaying Chat Messages
 */
@ActivityTransition(createEnterAnimation = R.anim.activity_slide_in_right, createExitAnimation = R.anim.activity_scale_out, destroyEnterAnimation = R.anim.activity_scale_in, destroyExitAnimation = R.anim.activity_slide_out_right)
public class ChatActivity extends AbstractBarterLiActivity implements
                OnReceiveMessageHandler {

    private static final String   TAG = "ChatActivity";

    private ChatAdapter           mChatAdapter;

    private ListView              mChatListView;

    /** {@link ChatRabbitMQConnector} instane for listening to messages */
    private ChatRabbitMQConnector mMessageConsumer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatListView = (ListView) findViewById(R.id.list_chats);
        mChatAdapter = new ChatAdapter(this);
        mChatListView.setAdapter(mChatAdapter);

        mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                        .getChatPort(), "/", "node.barterli", ExchangeType.DIRECT);
        mMessageConsumer.setOnReceiveMessageHandler(this);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMessageConsumer.dispose();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                if (mMessageConsumer
                                .connectToRabbitMQ("user1", false, false, true, null)) {
                    mMessageConsumer.addBinding("shared.key");
                }
                return null;
            }
        }.execute();

    }

    @Override
    public void onReceiveMessage(final byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Log.d(TAG, "Received:" + text);
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(text)) {
            mChatAdapter.addMessage(text);
        }

    }
}
