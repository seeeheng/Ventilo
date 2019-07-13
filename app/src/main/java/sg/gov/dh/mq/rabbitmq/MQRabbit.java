package sg.gov.dh.mq.rabbitmq;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import sg.gov.dh.mq.MQListener;

/**
 * Simple MQ client. Only support 1 channel and 1 queue
 * If you want another, declare another or make one that supports more.
 */
public class MQRabbit {

    private String TAG="MQRABBIT";
    private ConnectionFactory connFactory;
    private Connection conn;
    private Channel channel;

    private boolean isConnected=false;
    private boolean isActive=false;

    private String queueName=null;

    private String routingKey = "123";
    private int counter=0;

    private MQListener mqListener;

    public MQRabbit()
    {
        connFactory= new ConnectionFactory();
    }

    public boolean connect(String host, int port, String user, String password)
    {
//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host + " Port:"+String.valueOf(port)+ " User:"+user + " Password:" + password);
        connFactory.setHost(host);
        connFactory.setPort(port); //Commented to use defaults
        connFactory.setUsername(user);
        connFactory.setPassword(password);
        newConnection();

        return isActive();
    }



    public boolean connect(String host, String user, String password)
    {

//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host + " User:"+user + " Password:" + password);
        connFactory.setHost(host);
        connFactory.setUsername(user);
        connFactory.setPassword(password);
        connFactory.setVirtualHost("bfttracks");
        newConnection();
        return isActive();
    }

    public boolean connect(String host)
    {
//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host);
        connFactory.setHost(host);
        newConnection();
        return isActive();
    }

    private void newConnection()
    {

        try {
            conn=connFactory.newConnection();
            if (conn!=null) {
                channel = conn.createChannel();
            }
            createQueue("bfttracks");
//            createQueue("bft");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    public boolean isConnected() {
        isConnected=false;
        if (conn!=null)
        {
            if (conn.isOpen())
            {
                isConnected=true;
            }
        }
        return isConnected;
    }

    public boolean isActive(){
        isActive=false;
        if(channel!=null)
        {
            if (channel.isOpen())
            {
                isActive=true;
            }
        }
        return isActive;
    }

    private void createQueue(String _queueName)
    {
        if(queueName!=null)
        {
            try {
                channel.queueDelete(queueName);
                channel.exchangeDelete(queueName+"Exchange");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        queueName=_queueName;
        String exchangeName=queueName+"Exchange";
        try {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-max-length", 10);
            channel.exchangeDeclare(exchangeName, "fanout", true);
            channel.queueDeclare(queueName, true, false, false, args);
            channel.queueBind(queueName, exchangeName, routingKey);




        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setListener(MQListener listener)
    {
        mqListener=listener;
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                String[] messageArray=message.split(",");
                String action =messageArray[5];
                Log.d(TAG," [x] Received '" + message + "'");
                mqListener.onNewMessage(message);
            }
        };
        try {
            channel.basicConsume(queueName, true, consumer);
            Log.d(TAG,"Listening to MQ Queue " + queueName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(String message) throws IOException {
        message=message+","+counter;
        counter=counter+1;
        boolean isSuccess=false;
        if (isActive) {
            byte[] messageBodyBytes = message.getBytes();
            try {
                channel.basicPublish(queueName + "Exchange", routingKey, null, messageBodyBytes);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isSuccess=false;
            }
            isSuccess=true;
        }
        return isSuccess;
    }

    public void close()
    {
        try {
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
