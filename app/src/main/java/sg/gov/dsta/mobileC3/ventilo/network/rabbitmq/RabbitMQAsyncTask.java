package sg.gov.dsta.mobileC3.ventilo.network.rabbitmq;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;

public class RabbitMQAsyncTask {

    private static final String TAG = RabbitMQAsyncTask.class.getSimpleName();

//    public static boolean mIsServiceRegistered;
    private static RunRabbitMQAsyncTask mTask;

    public RabbitMQAsyncTask() {
    }

    public void runRabbitMQ() {
        mTask = new RunRabbitMQAsyncTask();
        mTask.execute();
    }

    public static void stopRabbitMQ() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }

    private static class RunRabbitMQAsyncTask extends AsyncTask<String, Void, Void> {

        RunRabbitMQAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
//            subscribeToRabbitMQ();
            //                tryingToConnect = true;

//                if (mqttHelper != null) {
//                    boolean isMqttConnected = mqttHelper.handleStart();
//                    if (isMqttConnected) {
//                        notifyMqttConnectedBroadcastIntent();
//                    }
//                }

//                    if (rabbitMQHelper != null) {

//            mIsServiceRegistered = true;
//            Log.i(TAG, "mIsServiceRegistered is " + mIsServiceRegistered);

            System.out.println("rabbitMQHelper not null");
            boolean isRabbitMQConnected = NetworkService.rabbitMQHelper.startRabbitMQWithDefaultSetting();
            if (isRabbitMQConnected) {
                System.out.println("rabbitMQHelper notify");
                notifyRabbitMQConnectedBroadcastIntent();
            }

//                    }

//                if (mConnectionStateMonitor == null) {
////                    dataEnabledReceiver = new BackgroundDataChangeIntentReceiver();
//                    mConnectionStateMonitor = new ConnectionStateMonitor();
//                    mConnectionStateMonitor.enable();
////                    registerReceiver(dataEnabledReceiver,
////                            new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//                }


//                tryingToConnect = false;

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "RabbitMQ async task onPostExecute.");
        }
    }

    private static void notifyRabbitMQConnectedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }
}
