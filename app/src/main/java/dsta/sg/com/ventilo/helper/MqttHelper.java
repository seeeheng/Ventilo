package dsta.sg.com.ventilo.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;

import dsta.sg.com.ventilo.constants.ActivityResultConstants;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class MqttHelper {
    public MqttClient mMqttAndroidClient;

    public static MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

    public static final String MQTT_CONNECT_INTENT_ACTION = "Mqtt Connected";
    private static final String TAG = "MQTT";
    private static final String CLIENT_ID = "1234";
    private static final String SERVER_URI = "tcp://broker.hivemq.com:1883";
//    final String serverUri = "tcp://broker.mqttdashboard.com:8000";

//    private Context mContext;
    private static MqttHelper mMqttHelper;
    private String defaultTopic = "Ventilo";

    final String username = "xxxxxxx";
    final String password = "yyyyyyyyyy";

    // constants used to define MQTT connection status
    public enum MQTTConnectionStatus {
        INITIAL,                            // initial status
        CONNECTED_TO_INTERNET,                //data is enabled
        CLIENT_IS_DEFINED,
        CONNECTED_TO_BROKER,                    // connected to broker
        CONNECTED,                          // client is created and default topic is set
        NOT_CONNECTED_DATA_DISABLED          // can't connect because the user has disabled data access
    }

    public static MqttHelper getInstance() {
        if (mMqttHelper == null) {
            mMqttHelper = new MqttHelper();
        }

        return mMqttHelper;
    }

    public MqttHelper() {
//        mMqttAndroidClient = new MqttClient(context, SERVER_URI, CLIENT_ID);

//        try {
//            IMqttToken token = mqttAndroidClient.connect();
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // We are connected
//                    Log.d(TAG, "onSuccess");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    // Something went wrong e.g. connection timeout or firewall problems
//                    Log.d(TAG, "onFailure");
//
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }




//        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean b, String s) {
//                Log.w(TAG, s);
//            }
//
//            @Override
//            public void connectionLost(Throwable throwable) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w(TAG, mqttMessage.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//            }
//        });
//        connect();
    }



    public void setCallback(MqttCallbackExtended callback) {
        mMqttAndroidClient.setCallback(callback);
    }

//    public void connect(){
//        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
//        // Reconnects in 1s; doubles each time, up to a maximum of 2 minutes
//        mqttConnectOptions.setAutomaticReconnect(true);
//        mqttConnectOptions.setCleanSession(false);
//        mqttConnectOptions.setUserName(username);
//        mqttConnectOptions.setPassword(password.toCharArray());
//
//        try {
//
//            mMqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//
//                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
//                    disconnectedBufferOptions.setBufferEnabled(true);
//                    disconnectedBufferOptions.setBufferSize(100);
//                    disconnectedBufferOptions.setPersistBuffer(false);
//                    disconnectedBufferOptions.setDeleteOldestMessages(false);
//                    mMqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
//                    subscribeToTopic();
//
//                    Log.d(TAG, "onSuccess");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w(TAG, "Failed to connect to: " + SERVER_URI + exception.toString());
//                }
//            });
//
//
//        } catch (MqttException ex){
//            ex.printStackTrace();
//        }
//    }


//    private void subscribeToTopic() {
//        try {
//            mMqttAndroidClient.subscribe(defaultTopic, 0, null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    Log.w(TAG,"Subscribed!");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Log.w(TAG, "Subscribed fail!");
//                }
//            });
//
//        } catch (MqttException ex) {
//            System.err.println("Exception subscribing");
//            ex.printStackTrace();
//        }
//    }

    public MqttClient getMqttClient() {
        return mMqttAndroidClient;
    }

    public boolean defineConnectionToBroker() {
        try {
//            BROKER = General.NOTIFICATION_SERVER;

//            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//            CLIENT_ID = sharedPrefs.getString("UserId", String.valueOf(0));
            if (!CLIENT_ID.isEmpty() && Integer.parseInt(CLIENT_ID) > 0) {
                mMqttAndroidClient = new MqttClient(SERVER_URI, CLIENT_ID, new MemoryPersistence());
//                mMqttAndroidClient = new MqttAndroidClient(mContext, SERVER_URI, CLIENT_ID);
                connectionStatus = MQTTConnectionStatus.CLIENT_IS_DEFINED;
                Log.d(TAG, "MQTT Connection: Client is registered as " + CLIENT_ID);
                return true;
            } else {
                mMqttAndroidClient = null;
                Log.e(TAG, "MQTT Connection: Client id is invalid at " + CLIENT_ID);
            }
        } catch (MqttException e) {
            // something went wrong!
            mMqttAndroidClient = null;
            Log.e(TAG, "MQTT Connection: Could not register client.", e);
        }
        return false;
    }

    public synchronized boolean handleStart() {
//        Log.d("SharedSense", "MQTT Connection: Handling start.");
        if (mMqttAndroidClient == null && !defineConnectionToBroker()) {
            Log.e(TAG, "MQTT Connection: Create connection failed");
            return false;
        }
        if (!isAlreadyConnected()) {
            // set the status to show we're trying to connect
            connectionStatus = MQTTConnectionStatus.CONNECTED_TO_INTERNET;
//            Log.d("SharedSense", "MQTT Connection: Trying to connect to broker.");
            // we think we have an Internet connection, so try to connect to the message broker
            connectToBroker();
            if (connectionStatus == MQTTConnectionStatus.CONNECTED_TO_BROKER) {
                //subscribe to default topic
                defaultTopic = String.valueOf(CLIENT_ID);
                subscribeToTopic(defaultTopic);
                return true;
            }
        } else {
//            Log.d("SharedSense", "MQTT Connection: Already connected.");
        }

        return false;
    }

    /*
     * Terminates a connection to the message broker.
     */
    public void disconnectFromBroker() {
        if (mMqttAndroidClient != null) {
            try {
                if (mMqttAndroidClient.isConnected()) {
                    mMqttAndroidClient.disconnect();
//                    Log.d("SharedSense", "MQTT Connection: disconnect succeeded");
                }
            } catch (MqttException e) {
                Log.e(TAG, "MQTT Connection: disconnect failed", e);
            } finally {
                mMqttAndroidClient = null;
            }
        }
    }

    /*
     * Checks if the MQTT client thinks it has an active connection
     */
    private boolean isAlreadyConnected() {
        return (mMqttAndroidClient != null && mMqttAndroidClient.isConnected());
    }

    /*
     * (Re-)connect to the message broker
     */
    private void connectToBroker() {
        try {
            // try to connect
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setConnectionTimeout(5);
            mMqttAndroidClient.connect(connOpts);
            mMqttAndroidClient.setCallback(getDummyCallback());
//            mMqttAndroidClient.setCallback(new NotificationCallback(this));
            // we are connected
            connectionStatus = MQTTConnectionStatus.CONNECTED_TO_BROKER;
        } catch (MqttException e) {
            // something went wrong!
            Log.e(TAG, "MQTT Connection: Could not connect to broker", e);
            Log.e(TAG, e.getMessage());
        }
    }

    public void subscribeToTopic(String topic) {
        if (!isAlreadyConnected()) {
            // quick sanity check - don't try and subscribe if we don't have a connection
            Log.e(TAG, "MQTT Connection: Unable to subscribe as we are not connected");
        } else {
            try {
                mMqttAndroidClient.subscribe(new String[] {defaultTopic}, new int[] {1});
                connectionStatus = MQTTConnectionStatus.CONNECTED;
//                Log.d("SharedSense", "MQTT Connection: properly subscribed to " + topic+", connection is complete.");
            } catch (MqttException e) {
                Log.e(TAG, "MQTT Connection: subscribe failed - MQTT exception", e);
            }
        }
    }


    // Dummy
    private MqttCallbackExtended getDummyCallback() {
        return new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w(TAG, s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w(TAG, mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        };
    }


    public void publishMessage() {
        if (isAlreadyConnected()) {
            String payload = "Payload 2";
            byte[] encodedPayload;
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                mMqttAndroidClient.publish(defaultTopic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
