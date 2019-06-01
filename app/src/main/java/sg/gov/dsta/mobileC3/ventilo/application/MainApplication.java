package sg.gov.dsta.mobileC3.ventilo.application;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkServiceBinder;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQAsyncTask;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPubSubBrokerProxy;
import sg.gov.dsta.mobileC3.ventilo.repository.ExcelSpreadsheetRepository;
import sg.gov.dsta.mobileC3.ventilo.worker.NetworkWorker;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();
    private static boolean applicationLock;
    private static Context appContext;
    public static NetworkService networkService;
    public static Intent rabbitMQIntent;

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
//                    startWorker();
//                    initJeroMQ();

                    JeroMQAsyncTask jeroMQAsyncTask = new JeroMQAsyncTask();
                    jeroMQAsyncTask.runJeroMQ();

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
        rabbitMQIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(rabbitMQIntent);
        getApplicationContext().bindService(rabbitMQIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    public static ServiceConnection rabbitMQServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Bind to LocalService, cast the IBinder and get LocalService instance
            NetworkServiceBinder binder = (NetworkServiceBinder) service;
            networkService = binder.getService();
            Log.i(TAG, "Network service activated.");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            networkService.deactivate();
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
}
