package sg.gov.dsta.mobileC3.ventilo.helper;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import sg.gov.dh.mq.MQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class RabbitMQHelper {

    public static final String RABBITMQ_CONNECT_INTENT_ACTION = "RabbitMQ Connected";

    private static final String TAG = "RabbitMQHelper";
//    private static final String SERVER_HOST = "jax79sg.hopto.org";
//    private static final String SERVER_HOST = "192.168.1.5";
//    private static final String SERVER_HOST = SharedPreferenceUtil.getCurrentUserWithPredefinedContext();
    private static final String SERVER_HOST = "111";
    private static final int SERVER_PORT = 15674;
    private static final String USERNAME = "jax";
    private static final String PASSWORD = "password";

    private static RabbitMQHelper mRabbitMQHelper;
    private RabbitMQ mqRabbit;

    public static RabbitMQHelper.RabbitMQConnectionStatus connectionStatus = RabbitMQHelper.RabbitMQConnectionStatus.INITIAL;

    // constants used to define MQTT connection status
    public enum RabbitMQConnectionStatus {
        INITIAL,                            // initial status
//        CONNECTED_TO_INTERNET,                //data is enabled
//        CLIENT_IS_DEFINED,
//        CONNECTED_TO_BROKER,                    // connected to broker
        CONNECTED,                          // client is created and default topic is set
        DISCONNECTED,                          // consumer got disconnected
//        NOT_CONNECTED_DATA_DISABLED          // can't connect because the user has disabled data access
    }

    public static RabbitMQHelper getInstance() {
        if (mRabbitMQHelper == null) {
            mRabbitMQHelper = new RabbitMQHelper();
        }

        return mRabbitMQHelper;
    }

    public RabbitMQHelper() {
    }

    public boolean startRabbitMQWithDefaultSetting() {
        boolean isSuccess = false;

        if (mqRabbit != null) {
            Log.w(TAG, "You already have a Rabbit running, killing previous queue and restarting another");
//            mqRabbit.close();
        } else {
            mqRabbit = new RabbitMQ();

            Log.d(TAG, "Connecting to MQ on " + SERVER_HOST);
            isSuccess = mqRabbit.connect(SERVER_HOST, USERNAME, PASSWORD);
            if (isSuccess) {
                System.out.println("mqRabbit connected successfully");
//                Toast.makeText(mContext, "RabbitMQ setup complete", Toast.LENGTH_SHORT).show();
                connectionStatus = RabbitMQConnectionStatus.CONNECTED;
//                setupMQListener();
            } else {
                System.out.println("mqRabbit failed");
//                Toast.makeText(mContext, "RabbitMQ failed. C2 capabilities disabled", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "Connection to MQ: " + isSuccess);
        }

        return isSuccess;
    }

    public void startRabbitMQWithSetting(Context context, String host, String username, String password){
        if (mqRabbit != null) {
            Log.w(TAG, "You already have a Rabbit running, killing previous queue and restarting another");
//            mqRabbit.close();
        } else {
            mqRabbit = new RabbitMQ();

            Log.d(TAG, "Connecting to MQ on " + SERVER_HOST);
            boolean isSuccess = mqRabbit.connect(host, username, password);
            if (isSuccess) {
                Toast.makeText(context, "RabbitMQ setup complete", Toast.LENGTH_SHORT).show();
                connectionStatus = RabbitMQConnectionStatus.CONNECTED;
//                setupMQListener();
            } else {
                Toast.makeText(context, "RabbitMQ failed. C2 capabilities disabled", Toast.LENGTH_SHORT).show();
                connectionStatus = RabbitMQConnectionStatus.DISCONNECTED;
            }
            Log.d(TAG, "Connection to MQ is successful: " + isSuccess);
        }
    }

    public void addRabbitListener(IMQListener mqListener) {
        mqRabbit.addListener(mqListener);
    }

    public boolean sendBFTMessage(String message) {
        boolean isSuccess = false;
        try {
            isSuccess = mqRabbit.sendBFTMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    public boolean sendMessage(String message) {
        boolean isSuccess = false;
        try {
            isSuccess = mqRabbit.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    public void closeConnection() {
        mqRabbit.close();
    }

}
