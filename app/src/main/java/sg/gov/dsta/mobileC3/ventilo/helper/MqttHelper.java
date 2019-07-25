package sg.gov.dsta.mobileC3.ventilo.helper;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import timber.log.Timber;

public class MqttHelper implements Serializable {
    public MqttClient mMqttAndroidClient;

    public static MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

    public static final String MQTT_CONNECT_INTENT_ACTION = "Mqtt Connected";
    private static final String TAG = "MQTT";
    //private static final String SERVER_URI = "tcp://broker.hivemq.com:1883";
    private static final String CLIENT_ID = NetworkUtil.getOwnDeviceMacAddr(); // previously, MqttClient.generateClientId()
    private static final String SERVER_URI = "tcp://mqtt.flespi.io:1883";
//    private static final String SERVER_URI = "tcp://192.168.1.5:1883";
//    private static final String SERVER_URI = "tcp://198.18.111.115:1883";
    private static final int QOS = 1;
//    final String serverUri = "tcp://broker.mqttdashboard.com:8000";

//    private Context mContext;
    private static MqttHelper mMqttHelper;

    private static String mTopic = "Ventilo";
    final String username = "Rp3XUF126FW2kBN6BryruzWDEXokpLj6ZaRJGIBAclsmL4iajErD3d6yYiGdUO7J";
//    final String username = "";
    final String password = "";

    // constants used to define MQTT connection status
    public enum MQTTConnectionStatus {
        INITIAL,                            // initial status
        CONNECTED_TO_INTERNET,                //data is enabled
        CLIENT_IS_DEFINED,
        CONNECTED_TO_BROKER,                    // connected to broker
        CONNECTED,                          // client is created and default topic is set
        NOT_CONNECTED_DATA_DISABLED          // can't connect because the user has disabled data access
    }

    // constants used to define MQTT topic
    public enum MqttTopic {
        TASK,
        INCIDENT,
        SITREP,
        NOTIFICATION
    }

    public static MqttHelper getInstance() {
        if (mMqttHelper == null) {
            mMqttHelper = new MqttHelper();
        }

        return mMqttHelper;
    }

    public MqttHelper() {
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo wInfo = wifiManager.getConnectionInfo();
//        String macAddress = wInfo.getMacAddress();
        System.out.println("ClientID is " + CLIENT_ID);

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
        if (mMqttAndroidClient != null) {
            mMqttAndroidClient.setCallback(callback);
        }
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
            if (!CLIENT_ID.isEmpty()) {
                mMqttAndroidClient = new MqttClient(SERVER_URI, CLIENT_ID, new MemoryPersistence());
//                mMqttAndroidClient = new MqttAndroidClient(mContext, SERVER_URI, CLIENT_ID);
                connectionStatus = MQTTConnectionStatus.CLIENT_IS_DEFINED;
                Log.d(TAG, "MQTT Connection: Client is registered as " + CLIENT_ID);
                return true;
            } else {
                mMqttAndroidClient = null;
                Timber.i("MQTT Connection: Client id is invalid at  %s" , CLIENT_ID);

            }
        } catch (MqttException e) {
            // something went wrong!
            mMqttAndroidClient = null;

            Timber.e("MQTT Connection: Could not register client. %s", e);

        }
        return false;
    }

    public synchronized boolean handleStart() {
//        Log.d("SharedSense", "MQTT Connection: Handling start.");
        if (mMqttAndroidClient == null && !defineConnectionToBroker()) {

            Timber.i("MQTT Connection: Create connection failed");

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
//                mTopic = String.valueOf(CLIENT_ID);
                subscribeToTopic(mTopic);
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

                Timber.e("MQTT Connection: disconnect failed %s", e);

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
            connOpts.setUserName(username);
            connOpts.setCleanSession(false);
            connOpts.setConnectionTimeout(15);
            connOpts.setAutomaticReconnect(true);
            mMqttAndroidClient.connect(connOpts);
            mMqttAndroidClient.setCallback(getDummyCallback());
//            mMqttAndroidClient.setCallback(new NotificationCallback(this));
            // we are connected
            connectionStatus = MQTTConnectionStatus.CONNECTED_TO_BROKER;
        } catch (MqttException e) {
            // something went wrong!
            Timber.e("MQTT Connection: Could not connect to broker %s", e);

            Log.e(TAG, e.getMessage());
        }
    }

    public void subscribeToTopic(String topic) {
        if (!mTopic.equalsIgnoreCase(topic)) {
            mTopic = topic;
        }

        if (!isAlreadyConnected()) {
            // quick sanity check - don't try and subscribe if we don't have a connection
            Timber.i("MQTT Connection: Unable to subscribe as we are not connected");


        } else {
            try {
                System.out.println("topic is " + mTopic);
                mMqttAndroidClient.subscribe(new String[] {topic}, new int[] {QOS});
                connectionStatus = MQTTConnectionStatus.CONNECTED;
//                Log.d("SharedSense", "MQTT Connection: properly subscribed to " + topic+", connection is complete.");
            } catch (MqttException e) {
                Timber.e("MQTT Connection: subscribe failed - MQTT exception %s", e);

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

    public void publishMessage(String payload) {
        this.publishMessage(payload, mTopic);
    }

    public void publishMessage(String payload, String topic) {
        if (isAlreadyConnected()) {
//            String payload = "Payload 2";
            byte[] encodedPayload;
            try {
                System.out.println("payload is " + payload);
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                message.setQos(QOS);
                mMqttAndroidClient.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
