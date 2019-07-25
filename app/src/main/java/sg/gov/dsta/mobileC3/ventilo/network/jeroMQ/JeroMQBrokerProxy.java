package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public abstract class JeroMQBrokerProxy {

    private static final String TAG = JeroMQBrokerProxy.class.getSimpleName();
    protected ExecutorService mExecutorService;

    public JeroMQBrokerProxy(ExecutorService executorService) {
        this.mExecutorService = executorService;
    }

    protected void initExecutorService() {
        if (mExecutorService.isShutdown()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public void start() {
        Timber.i("Starting JeroMQ broker proxy...");

        startServerProcess();

        Timber.i("JeroMQ broker proxy started!");

    }

    public void stop() {
        Timber.i("Stopping JeroMQ broker proxy...");


        mExecutorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Timber.e("Waiting for broker proxy termination interrupted: %s" , e);

        }
        Timber.i("JeroMQ broker proxy stopped: %b " , terminated);

    }

    protected abstract void startServerProcess();
}
