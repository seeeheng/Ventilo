package sg.gov.dsta.mobileC3.ventilo.thread;

import android.os.Process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class CustomThreadPoolManager {

    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 0;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static final CustomThreadPoolManager INSTANCE = new CustomThreadPoolManager();

    public static final int THREAD_SLEEP_DURATION_NONE = 0;
    public static final int THREAD_SLEEP_DURATION_SHORT = 500;

    private final ExecutorService mExecutorService;
    private final BlockingQueue<Runnable> mTaskQueue;
    private List<Future> mRunningTaskList;


//    private WeakReference<UiThreadCallback> uiThreadCallbackWeakReference;

    // The class is used as a singleton
//    static {
//        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
//        INSTANCE = new CustomThreadPoolManager();
//    }

    // Made constructor private to avoid the class being initiated from outside
    private CustomThreadPoolManager() {
        // initialize a queue for the thread pool. New tasks will be added to this queue
        mTaskQueue = new LinkedBlockingQueue<Runnable>();
        mRunningTaskList = new ArrayList<>();
        mExecutorService = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mTaskQueue,
                new BackgroundThreadFactory());
    }

    public static CustomThreadPoolManager getInstance() {
        return INSTANCE;
    }

//    // Add a callable to the queue, which will be executed by the next available thread in the pool
//    public void addCallable(Callable callable){
//        Future future = mExecutorService.submit(callable);
//        mRunningTaskList.add(future);
//    }
//
//    /* Remove all tasks in the queue and stop all running threads
//     * Notify UI thread about the cancellation
//     */
//    public void cancelAllTasks() {
//        synchronized (this) {
//            mTaskQueue.clear();
//            for (Future task : mRunningTaskList) {
//                if (!task.isDone()) {
//                    task.cancel(true);
//                }
//            }
//            mRunningTaskList.clear();
//        }
//
////        sendMessageToUiThread(Util.createMessage(Util.MESSAGE_ID, "All tasks in the thread pool are cancelled"));
//    }

    // Add a callable to the queue, which will be executed by the next available thread in the pool
    public void addRunnable(Runnable runnable) {
        Future future = mExecutorService.submit(runnable);
        mRunningTaskList.add(future);
    }

    /* Remove all tasks in the queue and stop all running threads
     * Notify UI thread about the cancellation
     */
    public void cancelAllTasks() {
        synchronized (this) {
            mTaskQueue.clear();
            for (Future task : mRunningTaskList) {
                if (task != null && !task.isDone()) {
                    task.cancel(true);
                }
            }

            mRunningTaskList.clear();
        }

//        sendMessageToUiThread(Util.createMessage(Util.MESSAGE_ID, "All tasks in the thread pool are cancelled"));
    }

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static final String TAG = BackgroundThreadFactory.class.getSimpleName();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread - " + TAG);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Timber.e("%s  encountered an error: %s", thread.getName(), ex.getMessage());
                }
            });

            return thread;
        }
    }
}
