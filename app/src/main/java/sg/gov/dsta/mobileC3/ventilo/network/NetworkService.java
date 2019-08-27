package sg.gov.dsta.mobileC3.ventilo.network;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQServerPair;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
//import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;


public class NetworkService extends IntentService {

    private static final String TAG = NetworkService.class.getSimpleName();
    private static final String NETWORK_INTERFACE_RNDIS = "RNDIS";


    // receiver that notifies the Service when the user changes data use preferences
//    private static MqttHelper mqttHelper;
    public static RabbitMQHelper rabbitMQHelper;

    public static boolean mIsServiceRegistered;

    //checks if the service is trying to connect, to prevent race conditions between startService and dataEnabledReceiver
//    private boolean tryingToConnect;

    public NetworkService() {
        super(NetworkService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Creating network service...");
//        createConnection();
    }

//    private void createConnection() {
////        mqttHelper = MqttHelper.getInstance();
////        mqttHelper.connectionStatus = MqttHelper.MQTTConnectionStatus.INITIAL;
//        // register to be notified whenever the user changes their preferences
//        //  relating to background data use - so that we can respect the current
//        //  preference
//
//        rabbitMQHelper = RabbitMQHelper.getInstance();
//        rabbitMQHelper.connectionStatus = RabbitMQHelper.RabbitMQConnectionStatus.INITIAL;
//    }

//    private boolean isOnline() {
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected();
//    }

    public static void deactivate() {
        // disconnect immediately
//        mqttHelper.disconnectFromBroker();

        if (rabbitMQHelper != null) {
            rabbitMQHelper.closeConnection();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");

//        if (rabbitMQHelper != null) {
//            mIsServiceRegistered = true;
//            Log.i(TAG, "mIsServiceRegistered: " + mIsServiceRegistered);
//
////            System.out.println("rabbitMQHelper not null");
////            boolean isRabbitMQConnected = NetworkService.rabbitMQHelper.startRabbitMQWithDefaultSetting();
////            if (isRabbitMQConnected) {
////                System.out.println("rabbitMQHelper notify");
////                notifyRabbitMQConnectedBroadcastIntent();
////            }
//
//            JeroMQPublisher.getInstance().start();
//            JeroMQSubscriber.getInstance().start();
//        }

        mIsServiceRegistered = true;
        Log.i(TAG, "mIsServiceRegistered: " + mIsServiceRegistered);

        JeroMQPublisher.getInstance().start();
        JeroMQSubscriber.getInstance().start();
        JeroMQServerPair.getInstance().start();

        mIsServiceRegistered = false;
        stopSelf();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        // START_NOT_STICKY - Service will NOT be left running after it has been killed
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsServiceRegistered = false;
        Log.i(TAG, "Network intent service destroyed.");
    }

    private static void notifyRabbitMQConnectedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }
}
