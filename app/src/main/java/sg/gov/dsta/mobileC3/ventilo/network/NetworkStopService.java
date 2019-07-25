package sg.gov.dsta.mobileC3.ventilo.network;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;

public class NetworkStopService extends Service {

    private static final String TAG = NetworkStopService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Stop service started.");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stop service destroyed.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(TAG, "onTaskRemoved");

        // Close network service upon application removal by user from recently used task list
        synchronized (NetworkStopService.class) {
            NetworkService.deactivate();

            if (NetworkService.mIsServiceRegistered) {
                if (getApplication() instanceof MainApplication) {
//                    NetworkService.deactivate();
                    stopService(((MainApplication) getApplication()).getNetworkIntent());
                    Log.i(TAG, "Stopped network intent service.");

                    NetworkService.mIsServiceRegistered = false;
                }
            }

            JeroMQPublisher.getInstance().stop();
            JeroMQSubscriber.getInstance().stop();

            Log.i(TAG, "Stopped all JeroMQ connections.");
        }

        stopSelf();
    }
}
