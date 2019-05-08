package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class JeroMQParentSubscriber {

//    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQParentSubscriber.class);
    private static final String TAG = JeroMQParentSubscriber.class.getSimpleName();
    protected ExecutorService mExecutorService;

    public JeroMQParentSubscriber(ExecutorService mExecutorService) {
        this.mExecutorService = mExecutorService;
    }

    public void start(){
        Log.i(TAG,"Starting JeroMQ parent subscriber...");
        startClientProcess();
        Log.i(TAG,"JeroMQ parent subscriber started!");
    }

    public void stop(){
        Log.i(TAG,"Stopping JeroMQ parent subscriber...");

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
            Log.e(TAG,"Waiting for parent subscriber termination interrupted: " + e);
        }

        Log.i(TAG,"JeroMQ parent subscriber stopped: " + terminated);
    }

    protected abstract void startClientProcess();
}
