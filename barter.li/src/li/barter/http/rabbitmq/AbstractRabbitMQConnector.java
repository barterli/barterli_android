
package li.barter.http.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class AbstractRabbitMQConnector {

    private static final String  TAG = "AbstractRabbitMQConnector";

    protected final String       mServer;
    protected final String       mExchange;
    protected final String       mVirtualHost;
    protected final int          mPort;
    protected final ExchangeType mExchangeType;

    public enum ExchangeType {

        DIRECT("direct"), TOPIC("topic"), FANOUT("fanout");

        public final String key;

        private ExchangeType(String key) {
            this.key = key;
        }
    }

    protected Channel    mChannel;
    protected Connection mConnection;

    private boolean      mRunning;

    public AbstractRabbitMQConnector(final String server, final int port,
                    final String virtualHost, final String exchange,
                    final ExchangeType exchangeType) {
        mServer = server;
        mPort = port;
        mVirtualHost = virtualHost;
        mExchange = exchange;
        mExchangeType = exchangeType;
    }

    public void dispose() {
        mRunning = false;

        try {
            if (mConnection != null)
                mConnection.close();
            if (mChannel != null)
                mChannel.abort();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isRunning() {
        return mRunning;
    }

    protected void setIsRunning(boolean running) {
        mRunning = running;
    }

    /**
     * Connect to the broker and create the exchange
     * 
     * @return success
     */
    protected boolean connectToRabbitMQ(final String userName,
                    final String password) {
        if (mChannel != null && mChannel.isOpen())// already declared
            return true;
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(userName);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(mVirtualHost);
            connectionFactory.setPort(mPort);
            mConnection = connectionFactory.newConnection();
            mChannel = mConnection.createChannel();
            mChannel.exchangeDeclare(mExchange, mExchangeType.key);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Publish a message to a queue
     * 
     * @param queueName The Queue to publish to
     * @param routingKey The routing key
     * @param message The message to publish
     */
    protected void publish(final String queueName, final String routingKey,
                    final String message) {
        if (mChannel != null && mChannel.isOpen()) {
            try {
                mChannel.basicPublish(queueName, routingKey, null,
                                message.getBytes(HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
