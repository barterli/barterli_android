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

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import android.os.Handler;

import java.io.IOException;
import java.util.Map;

import li.barter.utils.Logger;

/**
 * Consumes messages from a RabbitMQ broker
 */
public class ChatRabbitMQConnector extends AbstractRabbitMQConnector {

    private static final String TAG = "ChatRabbitMQConnector";

    public ChatRabbitMQConnector(final String server, final int port, final String virtualHost, final String exchange, final ExchangeType exchangeType) {
        super(server, port, virtualHost, exchange, exchangeType);
    }

    // The Queue name for this consumer
    private String           mQueue;
    private QueueingConsumer mSubscription;

    // last message to post back
    private byte[]           mLastMessage;

    // An interface to be implemented by an object that is interested in
    // messages(listener)
    public interface OnReceiveMessageHandler {
        public void onReceiveMessage(byte[] message);
    };

    // A reference to the listener, we can only have one at a time(for now)
    private OnReceiveMessageHandler mOnReceiveMessageHandler;

    private final Handler           mHandler       = new Handler();

    // Create runnable for posting back to main thread
    private final Runnable          mReturnMessage = new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           mOnReceiveMessageHandler
                                                                           .onReceiveMessage(mLastMessage);
                                                       }
                                                   };

    private final Runnable          mConsumeRunner = new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           consume();
                                                       }
                                                   };

    /**
     * Create Exchange and then start consuming. A binding needs to be added
     * before any messages will be delivered
     */
    public boolean connectToRabbitMQ(final String userName,
                    final String password, final String queueName,
                    final boolean durable, final boolean exclusive,
                    final boolean autoDelete, final Map<String, Object> args) {
        if (super.connectToRabbitMQ(userName, password)) {

            try {
                Logger.d(TAG, "Connected");
                mQueue = declareQueue(queueName, durable, exclusive, autoDelete, args);
                mSubscription = new QueueingConsumer(mChannel);
                mChannel.basicConsume(mQueue, false, mSubscription);
                if (mExchangeType == ExchangeType.FANOUT) {
                    addBinding("");// fanout has default binding
                }
            } catch (final IOException e) {
                e.printStackTrace();
                return false;
            }

            setIsRunning(true);
            mHandler.post(mConsumeRunner);

            return true;
        }
        return false;
    }

    /**
     * Add a binding between this consumers Queue and the Exchange with
     * routingKey
     * 
     * @param routingKey
     * @throws IOException If the binding could not be done
     */
    public void addBinding(final String routingKey) throws IOException {
        addBinding(mQueue, routingKey);
    }

    /**
     * Add a binding between a queue and a routing key on this consumers
     * exchange. The queue should already have been declared
     * 
     * @param queue The queue to bind to
     * @param routingKey The routing key
     * @throws IOException If the binding could not be done
     */
    public void addBinding(final String queue, final String routingKey)
                    throws IOException {
        mChannel.queueBind(queue, mExchange, routingKey);
    }

    /**
     * Declare a queue on this exchange
     * 
     * @param queue The queue to declare
     * @param durable
     * @param exclusive
     * @param autoDelete
     * @param args
     * @return The queue name
     * @throws IOException If the queue could not be declared
     */
    public String declareQueue(final String queue, final boolean durable,
                    final boolean exclusive, final boolean autoDelete,
                    final Map<String, Object> args) throws IOException {
        return mChannel.queueDeclare(queue, durable, exclusive, autoDelete, args)
                        .getQueue();
    }

    /**
     * Remove binding between this consumers Queue and the Exchange with
     * routingKey
     * 
     * @param routingKey the binding key
     * @throws IOException If the binding could not be removed
     */
    public void removeBinding(final String routingKey) throws IOException {
        mChannel.queueUnbind(mQueue, mExchange, routingKey);
    }

    /**
     * Set the callback for received messages
     * 
     * @param handler The callback
     */
    public void setOnReceiveMessageHandler(final OnReceiveMessageHandler handler) {
        mOnReceiveMessageHandler = handler;
    };

    private void consume() {
        final Thread thread = new Thread() {

            @Override
            public void run() {
                while (isRunning()) {
                    QueueingConsumer.Delivery delivery;
                    try {
                        delivery = mSubscription.nextDelivery();
                        mLastMessage = delivery.getBody();
                        mHandler.post(mReturnMessage);
                        try {
                            mChannel.basicAck(delivery.getEnvelope()
                                            .getDeliveryTag(), false);
                        } catch (final IOException e) {
                            Logger.e(TAG, e, "Unable to ack message");
                        }
                    } catch (final InterruptedException ie) {
                        ie.printStackTrace();
                    } catch (final ShutdownSignalException e) {
                        e.printStackTrace();
                    } catch (final ConsumerCancelledException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();

    }

}
