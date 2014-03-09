package li.barter.http.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class IConnectToRabbitMQ {
      public String mServer;
      public String mExchange;
 
      protected Channel mModel = null;
      protected Connection  mConnection;
 
      protected boolean mRunning ;
 
      protected  String mExchangeType ;
 
      /**
       *
       * @param server The server address
       * @param exchange The named exchange
       * @param exchangeType The exchange type name
       */
      public IConnectToRabbitMQ(String server, String exchange, String exchangeType)
      {
          mServer = server;
          mExchange = exchange;
          mExchangeType = exchangeType;
      }
 
      public void Dispose()
      {
          mRunning = false;
 
            try {
                if (mConnection!=null)
                    mConnection.close();
                if (mModel != null)
                    mModel.abort();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
 
      }
 
      /**
       * Connect to the broker and create the exchange
       * @return success
       */
      public boolean connectToRabbitMQ()
      {
          if(mModel!= null && mModel.isOpen() )//already declared
              return true;
          try
          {
              ConnectionFactory connectionFactory = new ConnectionFactory();
              connectionFactory.setHost(mServer);
              mConnection = connectionFactory.newConnection();
              mModel = mConnection.createChannel();
              mModel.exchangeDeclare(mExchange, mExchangeType, true);
 
              return true;
          }
          catch (Exception e)
          {
              e.printStackTrace();
              return false;
          }
      }
}