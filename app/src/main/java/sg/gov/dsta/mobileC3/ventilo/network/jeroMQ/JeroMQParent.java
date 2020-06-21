package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import timber.log.Timber;

public abstract class JeroMQParent {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQParent.class);
    private static final String TAG = JeroMQParent.class.getSimpleName();

    // Time Interval
    protected static final int MISSING_ZERO_MQ_HEARTBEAT_CONNECTION_THRESHOLD = 3;
    protected static final int HEARTBEAT_INTERVAL_IN_MILLISEC = 3000;
    protected static final int HEARTBEAT_TIMEOUT_IN_MILLISEC = 3000;
    protected static final int HEARTBEAT_TTL_IN_MILLISEC = 3000;
    protected static final int TCP_KEEP_ALIVE_COUNT = -1;
    protected static final int TCP_KEEP_ALIVE_IDLE_IN_MILLISEC = 3000;
    protected static final int TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC = 3000;
    protected static final int SOCKET_TIMEOUT_IN_MILLISEC = 0;
    protected static final int POLL_TIMEOUT_IN_MILLISEC = 0;

    protected static final int PUB_SUB_PORT = 5556;
    protected static final int PAIR_PORT = 5557;

    // Topics
    public static final String TOPIC_PREFIX_MAP = "TOPIC-FASTMAP_MAP";
    public static final String TOPIC_PREFIX_FASTMAP_BFT = "TOPIC-FASTMAP_BFT";
    protected static final String TOPIC_PREFIX_BFT = "TOPIC-BFT";
    protected static final String TOPIC_PREFIX_USER = "TOPIC-USER";
    protected static final String TOPIC_PREFIX_RADIO = "TOPIC-RADIO";
    protected static final String TOPIC_PREFIX_SITREP = "TOPIC-SITREP";
    protected static final String TOPIC_PREFIX_TASK = "TOPIC-TASK";
    protected static final String TOPIC_SYNC = "SYNC";
    public static final String TOPIC_INSERT = "INSERT";
    protected static final String TOPIC_UPDATE = "UPDATE";
    protected static final String TOPIC_DELETE = "DELETE";
    public static final String TOPIC_OWN_FORCE = "OWN_FORCE";
    public static final String TOPIC_OTHERS = "OTHERS";
    protected static final String SYNC_DATA = "Sync Data";

    protected CustomThreadPoolManager mCustomThreadPoolManager;
//    protected ExecutorService mExecutorService;

//    protected JeroMQParent(ExecutorService executorService) {
//        this.mExecutorService = executorService;
//    }
//
//    protected void initExecutorService() {
//        if (mExecutorService.isShutdown()) {
//            mExecutorService = Executors.newSingleThreadExecutor();
//        }
//    }

    protected JeroMQParent(CustomThreadPoolManager customThreadPoolManager) {
        this.mCustomThreadPoolManager = customThreadPoolManager;
    }

    protected void initCustomThreadPoolManagerService() {
        if (mCustomThreadPoolManager == null) {
            mCustomThreadPoolManager = CustomThreadPoolManager.getInstance();
        }
    }

    public void start() {
        Timber.i("Starting JeroMQ server...");
        startProcess();
        Timber.i("JeroMQ server started!");
    }

//    public void stop() {
//        Timber.i("Stopping JeroMQ server...");
//
//        mExecutorService.shutdownNow();
//        boolean terminated = false;
//        try {
//            terminated = mExecutorService.awaitTermination(1, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//
//            Timber.e("Waiting for server termination interrupted: %s" , e);
//
//        }
//        Timber.i("JeroMQ server stopped: %b", terminated);
//    }

    public void stop() {
        Timber.i("Stopping JeroMQ server...");

        mCustomThreadPoolManager.cancelAllTasks();
        mCustomThreadPoolManager = null;
//        boolean terminated = false;
//        try {
//            terminated = mCustomThreadPoolManagerawaitTermination(1, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//
//            Timber.e("Waiting for server termination interrupted: %s" , e);
//
//        }
        Timber.i("JeroMQ server stopped");
    }

    protected abstract void startProcess();
}
