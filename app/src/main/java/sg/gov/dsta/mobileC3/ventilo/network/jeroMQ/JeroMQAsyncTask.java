package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.os.AsyncTask;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;

public class JeroMQAsyncTask {

    private static final String TAG = JeroMQAsyncTask.class.getSimpleName();

    public JeroMQAsyncTask() {}

    public void runJeroMQ() {
        RunJeroMQAsyncTask task = new RunJeroMQAsyncTask();
        task.execute();
    }

    public void stopJeroMQ() {
        StopJeroMQAsyncTask task = new StopJeroMQAsyncTask();
        task.execute();
    }

    private static class RunJeroMQAsyncTask extends AsyncTask<String, Void, Void> {

        RunJeroMQAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
//            JeroMQPublisher.getInstance().start();
//            JeroMQSubscriber.getInstance().start();

            JeroMQPubSubBrokerProxy.getInstance().start();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "JeroMQ started.");
        }
    }

    private static class StopJeroMQAsyncTask extends AsyncTask<String, Void, Void> {

        StopJeroMQAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
//            JeroMQPublisher.getInstance().stop();
//            JeroMQSubscriber.getInstance().stop();
            JeroMQPubSubBrokerProxy.getInstance().stop();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "JeroMQ fully stopped successfully.");
        }
    }
}
