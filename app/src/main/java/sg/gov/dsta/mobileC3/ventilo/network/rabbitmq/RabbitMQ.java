package sg.gov.dsta.mobileC3.ventilo.network.rabbitmq;

import android.os.StrictMode;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import sg.gov.dh.mq.MQListener;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;

/**
 * Simple MQ client. Only support 1 channel and 1 queue
 * If you want another, declare another or make one that supports more.
 */
public class RabbitMQ {

//    private static final String EXCHANGE_NAME = "BFT_Track_Exchange";
//    private static final String QUEUE_NAME = "bfttracks";
    private String TAG = "MQRABBIT";
    private ConnectionFactory connFactory;
    private Connection conn;
    private Channel channel;

    private boolean isConnected = false;
    private boolean isActive = false;

    private String queueName = null;

    private String routingKey = "123";
    private int counter = 0;

//    private Map<String, IMQListener> mqListenerMap;
    private IMQListener mqListener;
    private List<IMQListener> mqListenerArray;

    public RabbitMQ() {
        connFactory = new ConnectionFactory();
        mqListenerArray = new ArrayList<>();
    }

    public boolean connect(String host, int port, String user, String password) {
//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host + " Port:"+String.valueOf(port)+ " User:"+user + " Password:" + password);
        connFactory.setHost(host);
        connFactory.setPort(port); //Commented to use defaults
        connFactory.setUsername(user);
        connFactory.setPassword(password);
        newConnection();

        return isActive();
    }


    public boolean connect(String host, String user, String password) {

//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host + " User:"+user + " Password:" + password);
        connFactory.setHost(host);
        connFactory.setUsername(user);
        connFactory.setPassword(password);
        connFactory.setVirtualHost("bfttracks");
        newConnection();
        return isActive();
    }

    public boolean connect(String host) {
//        Log.d(TAG,"Connecting to RabbitMQ with " + "Host:"+host);
        connFactory.setHost(host);
        newConnection();
        return isActive();
    }

    private void newConnection() {

        try {
            conn = connFactory.newConnection();
            if (conn != null) {
                channel = conn.createChannel();
            }

//            createQueue("Ventilo");
            createQueue("bfttracks");
//            createChannel();
//            createQueue("bft");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        isConnected = false;
        if (conn != null) {
            if (conn.isOpen()) {
                isConnected = true;
            }
        }
        return isConnected;
    }

    public boolean isActive() {
        isActive = false;
        if (channel != null) {
            if (channel.isOpen()) {
                isActive = true;
            }
        }
        return isActive;
    }

    private void createQueue(String _queueName) {
        if (queueName != null) {
            try {
                channel.queueDelete(queueName);
                channel.exchangeDelete(queueName + "Exchange");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        queueName = _queueName;

        String exchangeName = queueName + "Exchange";

        try {
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-max-length", 10);
            channel.exchangeDeclare(exchangeName, "fanout", true);
//            channel.queueDeclare(queueName, true, false, false, args);
            channel.queueDeclare(queueName, true, false, false, args);
            channel.queueBind(queueName, exchangeName, routingKey);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createChannel() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
//                String[] messageArray = message.split(",");
//                String action = messageArray[5];
                Log.d(TAG, " [x] Received '" + message + "'");

//                for (int i = 0; i < mqListenerArray.size(); i++) {
//                    mqListenerArray.get(i).onNewMessage(message);
//                }
                mqListener.onNewMessage(message);
            }
        };
        try {
            channel.basicConsume(queueName, true, consumer);
            Log.d(TAG, "Listening to MQ Queue " + queueName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(IMQListener listener) {
//        createQueue("bfttracks");
//        createQueue(queueName);
//        mqListenerMap.put(queueName, listener);
        mqListener = listener;
        createChannel();
//        mqListenerArray.add(listener);
    }

    public boolean sendBFTMessage(String message) throws IOException {
        message = message + "," + counter;
        counter = counter + 1;
        return sendMessage(message);
    }

    public boolean sendMessage(String message) throws IOException {
        boolean isSuccess = false;
        if (isActive) {
            byte[] messageBodyBytes = message.getBytes();
            try {
                channel.basicPublish(queueName + "Exchange", routingKey, null, messageBodyBytes);
            } catch (Exception e) {
                e.printStackTrace();
                isSuccess = false;
            }
            isSuccess = true;
        }
        return isSuccess;
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}