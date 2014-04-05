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

package li.barter.chat;

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

        DIRECT("direct"),
        TOPIC("topic"),
        FANOUT("fanout");

        public final String key;

        private ExchangeType(final String key) {
            this.key = key;
        }
    }

    protected Channel    mChannel;
    protected Connection mConnection;

    private boolean      mRunning;

    public AbstractRabbitMQConnector(final String server, final int port, final String virtualHost, final String exchange, final ExchangeType exchangeType) {
        mServer = server;
        mPort = port;
        mVirtualHost = virtualHost;
        mExchange = exchange;
        mExchangeType = exchangeType;
    }

    public void dispose() {
        mRunning = false;

        try {
            if (mConnection != null) {
                mConnection.close();
            }
            if (mChannel != null) {
                mChannel.abort();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    public boolean isRunning() {
        return mRunning;
    }

    protected void setIsRunning(final boolean running) {
        mRunning = running;
    }

    /**
     * Connect to the broker and create the exchange
     * 
     * @return success
     */
    protected boolean connectToRabbitMQ(final String userName,
                    final String password) {
        if ((mChannel != null) && mChannel.isOpen()) {
            return true;
        }
        try {
            final ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(userName);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(mVirtualHost);
            connectionFactory.setPort(mPort);
            mConnection = connectionFactory.newConnection();
            mChannel = mConnection.createChannel();
            mChannel.exchangeDeclare(mExchange, mExchangeType.key);

            return true;
        } catch (final Exception e) {
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
    public void publish(final String queueName, final String routingKey,
                    final String message) {
        if ((mChannel != null) && mChannel.isOpen()) {
            try {
                mChannel.basicPublish(queueName, routingKey, null, message
                                .getBytes(HTTP.UTF_8));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
