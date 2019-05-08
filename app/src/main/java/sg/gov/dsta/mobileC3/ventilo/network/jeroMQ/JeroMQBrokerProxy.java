package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class JeroMQBrokerProxy {

    private static final String TAG = JeroMQBrokerProxy.class.getSimpleName();
    protected ExecutorService mExecutorService;

    public JeroMQBrokerProxy(ExecutorService executorService) {
        this.mExecutorService = executorService;
    }

    public void start() {
        Log.i(TAG,"Starting JeroMQ broker proxy...");
        startServerProcess();
        Log.i(TAG,"JeroMQ broker proxy started!");
    }

    public void stop() {
        Log.i(TAG,"Stopping JeroMQ broker proxy...");

        mExecutorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG,"Waiting for broker proxy termination interrupted: " + e);
        }

        Log.i(TAG,"JeroMQ broker proxy stopped: " + terminated);
    }

    protected abstract void startServerProcess();
}
