package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.os.AsyncTask;
import android.util.Log;

public class WaveRelayRadioAsyncTask {
    private static final String TAG = WaveRelayRadioAsyncTask.class.getSimpleName();
    private WaveRelayRadioClient mWaveRelayRadioClient;

    public WaveRelayRadioAsyncTask() {}

    public void runWrRadioSocketConnection(String ipAddress) {
        runWrRadioSocketConnectionAsyncTask task =
                new runWrRadioSocketConnectionAsyncTask(ipAddress);
        task.execute();
    }

    public WaveRelayRadioClient getWaveRelayRadioClient() {
        return mWaveRelayRadioClient;
    }

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
