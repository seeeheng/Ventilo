package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

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
        Timber.i("Starting JeroMQ server...");
              startServerProcess();
        Timber.i("JeroMQ server started!");

    }

    public void stop() {
        Timber.i("Stopping JeroMQ server...");


        mExecutorService.shutdownNow();
        boolean terminated = false;
        try {
            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            Timber.e("Waiting for server termination interrupted: %s" , e);

        }
        Timber.i("JeroMQ server stopped: %b", terminated);


    }

    protected abstract void startServerProcess();
}
