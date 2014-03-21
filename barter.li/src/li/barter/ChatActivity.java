
package li.barter;

import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;

import li.barter.adapters.ChatAdapter;
import li.barter.http.HttpConstants;
import li.barter.http.rabbitmq.ChatRabbitMQConnector;
import li.barter.http.rabbitmq.AbstractRabbitMQConnector.ExchangeType;
import li.barter.http.rabbitmq.ChatRabbitMQConnector.OnReceiveMessageHandler;

/**
 * @author vinaysshenoy Activity for displaying Chat Messages
 */
public class ChatActivity extends AbstractBarterLiActivity implements
                OnReceiveMessageHandler {

    private static final String TAG = "ChatActivity";

    private ChatAdapter         mChatAdapter;

    private ListView            mChatListView;

    /** {@link ChatRabbitMQConnector} instane for listening to messages */
    private ChatRabbitMQConnector     mMessageConsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mChatListView = (ListView) findViewById(R.id.list_chats);
        mChatAdapter = new ChatAdapter(this);
        mChatListView.setAdapter(mChatAdapter);

        mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants.getChatPort(), "/",
                        "node.barterli", ExchangeType.DIRECT);
        mMessageConsumer.setOnReceiveMessageHandler(this);
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
            protected Void doInBackground(Void... params) {
                if (mMessageConsumer.connectToRabbitMQ("user1", false, false,
                                true, null)) {
                    mMessageConsumer.addBinding("shared.key");
                }
                return null;
            }
        }.execute();

    }

    @Override
    public void onReceiveMessage(byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);
            Log.d(TAG, "Received:" + text);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(text)) {
            mChatAdapter.addMessage(text);
        }

    }
}
