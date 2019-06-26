package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;

public class WaveRelayRadioAsyncTask {
    private static final String TAG = WaveRelayRadioAsyncTask.class.getSimpleName();
    private WaveRelayRadioClient mWaveRelayRadioClient;

    public WaveRelayRadioAsyncTask() {}

    public void runWrRadioSocketConnection(String ipAddress, UsbManager cm) {
        runWrRadioSocketConnectionAsyncTask task =
                new runWrRadioSocketConnectionAsyncTask(ipAddress, cm);
        task.execute();
    }

    public WaveRelayRadioClient getWaveRelayRadioClient() {
        return mWaveRelayRadioClient;
    }

    private class runWrRadioSocketConnectionAsyncTask extends AsyncTask<String, Void, Void> {

        private String mIpAddress;
        private UsbManager mCm;

        runWrRadioSocketConnectionAsyncTask(String ipAddress, UsbManager cm) {
            mIpAddress = ipAddress;
            mCm = cm;
        }

        @Override
        protected Void doInBackground(final String... param) {
            mWaveRelayRadioClient = new WaveRelayRadioClient(mIpAddress, mCm);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Log.i(TAG, "WaveRelayRadioClient started.");
        }
    }
}
