package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public abstract class JeroMQParentSubscriber {

//    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQParentSubscriber.class);
    private static final String TAG = JeroMQParentSubscriber.class.getSimpleName();
    protected ExecutorService mExecutorService;

    public JeroMQParentSubscriber(ExecutorService mExecutorService) {
        this.mExecutorService = mExecutorService;
    }

    protected void initExecutorService() {
        if (mExecutorService.isShutdown()) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public void start(){

        Timber.i("Starting JeroMQ parent subscriber...");

        startClientProcess();

        Timber.i("JeroMQ parent subscriber started!");

    }

    public void stop(){

        Timber.i("Stopping JeroMQ parent subscriber...");


        mExecutorService.shutdownNow();
//        mScheduledExecutorService.shutdownNow();
        boolean terminated = false;
        try {
//            if (mExecutorService.awaitTermination(1, TimeUnit.SECONDS) &&
//                    mScheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
//                terminated = true;
//            }
            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Timber.e("Waiting for parent subscriber termination interrupted: %s " , e);

        }
        Timber.i("JeroMQ parent subscriber stopped:  %b" , terminated);

    }

    protected abstract void startClientProcess();
}
