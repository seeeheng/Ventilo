package sg.gov.dsta.mobileC3.ventilo.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.worker.NetworkWorker;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getSimpleName();
    private static Context appContext;
    private NetworkService mNetworkService;

    public MainApplication() {}

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        startWorker();
//        subscribeToRabbitMQ();
        Log.i(TAG, "onCreate fired");
    }

    private void startWorker() {
        OneTimeWorkRequest socketWork =
                new OneTimeWorkRequest.Builder(NetworkWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(socketWork);
    }

//    private void subscribeToRabbitMQ() {
//        Intent mqttIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mqttIntent);
//        getApplicationContext().bindService(mqttIntent, mMqttServiceConnection, Context.BIND_AUTO_CREATE);
//    }

//    /**
//     * Defines callbacks for service binding, passed to bindService()
//     */
//    private ServiceConnection mMqttServiceConnection = new ServiceConnection() {
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
//            Log.i(TAG, "Network service deactivated.");
//        }
//    };

    public static Context getAppContext() {
        return appContext;
    }
}
