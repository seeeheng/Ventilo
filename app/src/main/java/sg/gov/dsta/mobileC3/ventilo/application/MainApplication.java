package sg.gov.dsta.mobileC3.ventilo.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkServiceBinder;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();
    private static boolean applicationLock;
    private static Context appContext;
    private NetworkService mNetworkService;
    private Intent mRabbitMQIntent;

    public static boolean isNetworkServiceRunning;

    public MainApplication() {}

    @Override
    public void onCreate() {
        super.onCreate();

        // Double check locking pattern (check if instance is null twice)
        if (!applicationLock && appContext == null) {
            synchronized (MainApplication.class) {
                if (!applicationLock && appContext == null) {
                    applicationLock = true;
                    appContext = getApplicationContext();

                    if (!isNetworkServiceRunning) {
                        subscribeToRabbitMQ();
                        isNetworkServiceRunning = true;
                    }

////                    startWorker();
////                    initJeroMQ();

                    Log.i(TAG, "onCreate fired");
                }
            }
        }
    }

//    private void startWorker() {
//        OneTimeWorkRequest socketWork =
//                new OneTimeWorkRequest.Builder(NetworkWorker.class)
//                        .build();
//        WorkManager.getInstance().enqueue(socketWork);
//    }

    private void subscribeToRabbitMQ() {
        mRabbitMQIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(mRabbitMQIntent);
//        stopService(mRabbitMQIntent);
//        getApplicationContext().bindService(mRabbitMQIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    public ServiceConnection rabbitMQServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Bind to LocalService, cast the IBinder and get LocalService instance
            NetworkServiceBinder binder = (NetworkServiceBinder) service;
            mNetworkService = binder.getService();
            Log.i(TAG, "Network service activated.");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mNetworkService.deactivate();
            isNetworkServiceRunning = false;
            Log.i(TAG, "Network service deactivated.");
        }
    };

//    private void initJeroMQ() {
//        JeroMQPubSubBrokerProxy.getInstance().start();
//        Log.i(TAG, "JeroMQ initialised");
//    }

    public static Context getAppContext() {
        return appContext;
    }

    public ServiceConnection getRabbitMQServiceConnection() {
        return rabbitMQServiceConnection;
    }

    public NetworkService getNetworkService() {
        return mNetworkService;
    }

    public Intent getRabbitMQIntent() {
        return mRabbitMQIntent;
    }
}
