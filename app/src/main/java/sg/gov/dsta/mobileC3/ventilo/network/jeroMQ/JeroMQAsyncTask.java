package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkServiceBinder;

public class JeroMQAsyncTask {

    private static final String TAG = JeroMQAsyncTask.class.getSimpleName();

    public JeroMQAsyncTask() {}

    public void runJeroMQ() {
        runJeroMQAsyncTask task =
                new runJeroMQAsyncTask();
        task.execute();
    }

    private static class runJeroMQAsyncTask extends AsyncTask<String, Void, Void> {

        runJeroMQAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
            JeroMQPubSubBrokerProxy.getInstance().start();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "JeroMQ started.");
        }
    }
}
