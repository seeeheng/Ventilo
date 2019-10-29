package sg.gov.dsta.mobileC3.ventilo.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkStopService;
import sg.gov.dsta.mobileC3.ventilo.util.log.LoggerUtil;
import timber.log.Timber;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();
    private static boolean applicationLock;
    private static Context appContext;
    private Intent mNetworkServiceIntent;
    private Intent mNetworkStopServiceIntent;
//    private NetworkService mNetworkService;
//    private Intent mRabbitMQIntent;

    private boolean isNetworkServiceRunning;

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

                    // Required for storing Excel .xlsx file formats
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
                    System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

                    if (!isNetworkServiceRunning) {
                        subscribeToNetwork();
//                        turnOffDozeMode();
                        isNetworkServiceRunning = true;
                    }

                    Timber.plant(new LoggerUtil.DebugLogTree());
                    Timber.plant(new LoggerUtil.FileLoggingTree());

////                    startWorker();
////                    initJeroMQ();

//                    Log.i(TAG, "onCreate fired");

                    Timber.i("onCreate fired");
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

    private void subscribeToNetwork() {
        mNetworkServiceIntent = new Intent(getApplicationContext(), NetworkService.class);
        mNetworkStopServiceIntent = new Intent(getApplicationContext(), NetworkStopService.class);
        startService(mNetworkServiceIntent);
        startService(mNetworkStopServiceIntent);
    }

    /**
     * To prevent network from cutting upon going into 'Doze' mode
     */
    private void turnOffDozeMode() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            if (pm.isIgnoringBatteryOptimizations(packageName)) // disable doze mode for this package
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            else { // enable doze mode
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }

            startActivity(intent);
        }
    }
//    private void subscribeToRabbitMQ() {
//        mRabbitMQIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mRabbitMQIntent);
////        stopService(mRabbitMQIntent);
////        getApplicationContext().bindService(mRabbitMQIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
//    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
//    public ServiceConnection rabbitMQServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // Bind to LocalService, cast the IBinder and get LocalService instance
//            NetworkServiceBinder binder = (NetworkServiceBinder) service;
//            mNetworkService = binder.getService();
//            Log.i(TAG, "Network service activated.");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mNetworkService.deactivate();
//            isNetworkServiceRunning = false;
//            Log.i(TAG, "Network service deactivated.");
//        }
//    };

//    private void initJeroMQ() {
//        JeroMQPubSubBrokerProxy.getInstance().start();
//        Log.i(TAG, "JeroMQ initialised");
//    }

    public static Context getAppContext() {
        return appContext;
    }

//    public ServiceConnection getRabbitMQServiceConnection() {
//        return rabbitMQServiceConnection;
//    }

//    public NetworkService getNetworkService() {
//        return mNetworkService;
//    }

    public Intent getNetworkServiceIntent() {
        return mNetworkServiceIntent;
    }

    public Intent getNetworkStopServiceIntent() {
        return mNetworkStopServiceIntent;
    }
}
