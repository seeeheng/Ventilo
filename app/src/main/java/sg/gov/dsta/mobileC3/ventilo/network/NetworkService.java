package sg.gov.dsta.mobileC3.ventilo.network;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
//import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;


public class NetworkService extends IntentService {

    private static final String TAG = NetworkService.class.getSimpleName();
    private static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "1";
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
//        JeroMQServerPair.getInstance().start();

        mIsServiceRegistered = false;
        stopSelf();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//            Notification.Builder builder = new Notification.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID)
//                    .setContentTitle(getString(R.string.notification_header))
//                    .setContentText(getString(R.string.notification_service_ended))
//                    .setAutoCancel(true);
//
//            Notification notification = builder.build();
//            startForeground(1, notification);

            startMyOwnForeground();

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_header))
                    .setContentText(getString(R.string.notification_service_ended))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        // START_NOT_STICKY - Service will NOT be left running after it has been killed
        return START_NOT_STICKY;
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.default_soldier_icon)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
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
