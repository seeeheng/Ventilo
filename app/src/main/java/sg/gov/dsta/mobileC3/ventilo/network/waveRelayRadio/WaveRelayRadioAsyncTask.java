package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.os.AsyncTask;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class WaveRelayRadioAsyncTask {
    private static final String TAG = WaveRelayRadioAsyncTask.class.getSimpleName();
    private WaveRelayRadioClient mWaveRelayRadioClient;

    public WaveRelayRadioAsyncTask() {}

    public void runWrRadioSocketConnection(String ipAddress) {
        runWrRadioSocketConnectionAsyncTask task =
                new runWrRadioSocketConnectionAsyncTask(ipAddress);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

//    public WaveRelayRadioClient getWaveRelayRadioClient() {
//        return mWaveRelayRadioClient;
//    }

    private class runWrRadioSocketConnectionAsyncTask extends AsyncTask<String, Void, Void> {

        private String mIpAddress;

        runWrRadioSocketConnectionAsyncTask(String ipAddress) {
            mIpAddress = ipAddress;
        }

        @Override
        protected Void doInBackground(final String... param) {
            mWaveRelayRadioClient = new WaveRelayRadioClient(mIpAddress);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "WaveRelayRadioClient started.");
        }
    }
}
