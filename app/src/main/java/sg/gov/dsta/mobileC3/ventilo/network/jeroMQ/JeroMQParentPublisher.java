package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class JeroMQParentPublisher {

//    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQParentPublisher.class);
    private static final String TAG = JeroMQParentPublisher.class.getSimpleName();
    protected ExecutorService mExecutorService;

    protected JeroMQParentPublisher(ExecutorService executorService) {
        this.mExecutorService = executorService;
    }

    protected void initExecutorService() {
        if (mExecutorService.isShutdown()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public void start() {
        Log.i(TAG,"Starting JeroMQ server...");
        startServerProcess();
        Log.i(TAG,"JeroMQ server started!");
    }

    public void stop() {
        Log.i(TAG,"Stopping JeroMQ server...");

        mExecutorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG,"Waiting for server termination interrupted: " + e);
        }

        Log.i(TAG,"JeroMQ server stopped: " + terminated);
    }

    protected abstract void startServerProcess();
}
