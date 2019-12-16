package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;

import com.google.gson.Gson;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import timber.log.Timber;

/**
 * Reads from multiple sockets in Java
 */
public class JeroMQPublisher extends JeroMQParent {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQPublisher.class);
    private static final String TAG = JeroMQPublisher.class.getSimpleName();

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://" +
//            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://" +
//            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//            DEFAULT_PORT;

    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://*:" + PUB_SUB_PORT;

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://192.168.1.3:" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

    private static volatile JeroMQPublisher instance;
    //    private Socket mSocket;
    private ZContext mZContext;
    private Socket mPubSocket;

    private List<String> mServerSubEndpointList;
    private List<Socket> mSocketList;

    private static volatile boolean isSocketStarted;

    //  Prepare our context and sockets
//    public static void subscribeToMessages() {
//        try (ZContext context = new ZContext()) {
//            // Connect to task ventilator
//            ZMQ.Socket receiver = context.createSocket(SocketType.PULL);
//            receiver.connect("tcp://localhost:5557");
//
//            //  Connect to weather server
//            ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
//            subscriber.connect("tcp://localhost:5556");
//            subscriber.subscribe("10001 ".getBytes(ZMQ.CHARSET));
//
//            //  Initialize poll set
//            ZMQ.Poller items = context.createPoller(2);
//            items.register(receiver, ZMQ.Poller.POLLIN);
//            items.register(subscriber, ZMQ.Poller.POLLIN);
//
//            //  Process messages from both sockets
//            while (!Thread.currentThread().isInterrupted()) {
//                byte[] message;
//                items.poll();
//                if (items.pollin(0)) {
//                    message = receiver.recv(0);
//                    System.out.println("Process task");
//                }
//                if (items.pollin(1)) {
//                    message = subscriber.recv(0);
//                    System.out.println("Process weather update");
//                }
//            }
//        }
//    }

    /**
     * Fixed number of threads dependant on the number of device IP addresses available
     */
    private JeroMQPublisher() {
//        super(zContext, Executors.newFixedThreadPool(ADDRESSES.size()));
//        super(Executors.newSingleThreadExecutor());
        super(CustomThreadPoolManager.getInstance());
    }

    /**
     * Create/get singleton class instance
     *
     * @return
     */
    public synchronized static JeroMQPublisher getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (JeroMQPublisher.class) {
                if (instance == null) {
                    instance = new JeroMQPublisher();
                } else {
                    instance.initCustomThreadPoolManagerService();
                }
            }
        } else {
            instance.initCustomThreadPoolManagerService();
        }

        return instance;
    }

//    private static int getOpenPorts() {
//        try {
//            int port = Utils.findOpenPort();
//            System.out.println("Port is " + port);
//            return port;
//        } catch (IOException e) {
//            Log.d(TAG, "Cannot find open ports " + e);
//        }
//
//        return DEFAULT_PORT;
//    }

    /**
     * Creates a single thread for each IP address for server processing of messages from different topics
     * Processes messages based on topics
     */
    @Override
    protected synchronized void startProcess() {

        if (!isSocketStarted) {
            Timber.i("Start server pub sockets connection");
            Timber.i("OWN IP address is: %s", NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

            mZContext = new ZContext();
            mZContext.setLinger(0);
            mZContext.setRcvHWM(0);
            mZContext.setSndHWM(0);

            mPubSocket = mZContext.createSocket(SocketType.PUB);
            mPubSocket.setMaxMsgSize(-1);
            mPubSocket.setMsgAllocationHeapThreshold(0);

            mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
            mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
            mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
//            mPubSocket.setMsgAllocationHeapThreshold(5 * 1024 * 1024);
//        mPubSocket.setSendBufferSize(1024 * 1024);
            mPubSocket.setLinger(0);
            mPubSocket.setRcvHWM(0);
            mPubSocket.setSndHWM(0);
            mPubSocket.setImmediate(true);
            mPubSocket.setTCPKeepAlive(0);
            mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
            mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
            mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
            mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
            mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
//        mPubSocket.connect(SERVER_PUB_IP_ADDRESS);
            mPubSocket.bind(SERVER_PUB_IP_ADDRESS);
            Timber.i("Server pub sockets connected");

            isSocketStarted = true;
        }
    }

    /**
     * Broadcasts User JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public synchronized void sendUserMessage(String message, String actionPrefix) {
        String topicPrefix = getActionPrefix(TOPIC_PREFIX_USER, actionPrefix);

        if (StringUtil.EMPTY_STRING.equalsIgnoreCase(topicPrefix)) {
            return;
        }

        Timber.i("Publishing User");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {

                if (mPubSocket != null) {
                    StringBuilder userMessageToSend = new StringBuilder();
                    userMessageToSend.append(topicPrefix);
                    userMessageToSend.append(StringUtil.SPACE);
                    userMessageToSend.append(message);

                    Timber.i("Publishing User %s", actionPrefix);
                    Timber.i("message: %s", userMessageToSend);

                    mPubSocket.setMaxMsgSize(-1);
                    mPubSocket.setMsgAllocationHeapThreshold(0);

                    mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                    mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                    mPubSocket.setLinger(0);
                    mPubSocket.setRcvHWM(0);
                    mPubSocket.setSndHWM(0);
                    mPubSocket.setImmediate(true);
                    mPubSocket.setTCPKeepAlive(0);
                    mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                    mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                    mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                    mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.send(userMessageToSend.toString());

                    Timber.i("User %s", actionPrefix);

                    try {
                        Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                    } catch (InterruptedException e) {
                        Timber.e("Interrupted while sleeping: " + e);
                        return;
                    }
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
    }

    /**
     * Broadcasts WaveRelay Radio JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public synchronized void sendWaveRelayRadioMessage(String message, String actionPrefix) {

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_RADIO, actionPrefix);

        Timber.i("Publishing WaveRelay Radio");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {

                if (mPubSocket != null) {
                    StringBuilder radioMessageToSend = new StringBuilder();
                    radioMessageToSend.append(topicPrefix);
                    radioMessageToSend.append(StringUtil.SPACE);
                    radioMessageToSend.append(message);
                    Timber.i("Publishing WaveRelay Radio %s", actionPrefix);
                    Timber.i("Publishing WaveRelay Radio message %s", message);

                    mPubSocket.setMaxMsgSize(-1);
                    mPubSocket.setMsgAllocationHeapThreshold(0);

                    mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                    mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                    mPubSocket.setLinger(0);
                    mPubSocket.setRcvHWM(0);
                    mPubSocket.setSndHWM(0);
                    mPubSocket.setImmediate(true);
                    mPubSocket.setTCPKeepAlive(0);
                    mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                    mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                    mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                    mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.send(radioMessageToSend.toString());
                    Timber.i("WaveRelay Radio message sent %s", actionPrefix);

                    try {
                        Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                    } catch (InterruptedException e) {
                        Timber.e("Interrupted while sleeping: %s", e);
                        return;
                    }
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
    }

    /**
     * Broadcasts BFT JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public synchronized void sendBFTMessage(String message, String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            final String currentAddress = ADDRESSES.get(i);

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_BFT, actionPrefix);

        if (StringUtil.EMPTY_STRING.equalsIgnoreCase(topicPrefix)) {
            return;
        }

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);

                if (mPubSocket != null) {
                    StringBuilder bftMessageToSend = new StringBuilder();
                    bftMessageToSend.append(topicPrefix);
                    bftMessageToSend.append(" ");
                    bftMessageToSend.append(message);

                    Timber.i("Publishing BFT message: %s", message);

                    mPubSocket.setMaxMsgSize(-1);
                    mPubSocket.setMsgAllocationHeapThreshold(0);

                    mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                    mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                    mPubSocket.setLinger(0);
                    mPubSocket.setRcvHWM(0);
                    mPubSocket.setSndHWM(0);
                    mPubSocket.setImmediate(true);
                    mPubSocket.setTCPKeepAlive(0);
                    mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                    mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                    mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                    mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.send(bftMessageToSend.toString());

                    Timber.i("BFT message sent");

                    try {
                        Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                    } catch (InterruptedException e) {
                        Timber.e("Interrupted while sleeping:  %s", e);
                        return;
                    }
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
//        }
    }

    /**
     * Broadcasts Sit Rep JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public synchronized void sendSitRepMessage(String message, String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            final String currentAddress = ADDRESSES.get(i);

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_SITREP, actionPrefix);

        Timber.i("Publishing SitRep");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
                if (mPubSocket != null) {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
                    StringBuilder sitRepMessageToSend = new StringBuilder();
                    sitRepMessageToSend.append(topicPrefix);
                    sitRepMessageToSend.append(StringUtil.SPACE);
                    sitRepMessageToSend.append(message);

                    Timber.i("Publishing SitRep %s %s", actionPrefix, message);

                    mPubSocket.setMaxMsgSize(-1);
                    mPubSocket.setMsgAllocationHeapThreshold(0);

                    mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                    mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                    mPubSocket.setLinger(0);
                    mPubSocket.setRcvHWM(0);
                    mPubSocket.setSndHWM(0);
                    mPubSocket.setImmediate(true);
                    mPubSocket.setTCPKeepAlive(0);
                    mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                    mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                    mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                    mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.send(sitRepMessageToSend.toString());

                    Timber.i("SitRep message sent %s", actionPrefix);

                    try {
                        Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                    } catch (InterruptedException e) {
                        Timber.e("Interrupted while sleeping: %s", e);
                        return;
                    }
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
//        }
    }

    /**
     * Broadcasts Task JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public synchronized void sendTaskMessage(String message, String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            System.out.println("sendTaskMessage two");
//            LOGGER.info(" in sendTaskMessage {}", message);
//            final String currentAddress = ADDRESSES.get(i);

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_TASK, actionPrefix);

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);

                if (mPubSocket != null) {
                    StringBuilder taskMessageToSend = new StringBuilder();
                    taskMessageToSend.append(topicPrefix);
                    taskMessageToSend.append(StringUtil.SPACE);
                    taskMessageToSend.append(message);

                    Timber.i("Publishing Task %s  , messge: %s", actionPrefix, taskMessageToSend);

                    mPubSocket.setMaxMsgSize(-1);
                    mPubSocket.setMsgAllocationHeapThreshold(0);

                    mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                    mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                    mPubSocket.setLinger(0);
                    mPubSocket.setRcvHWM(0);
                    mPubSocket.setSndHWM(0);
                    mPubSocket.setImmediate(true);
                    mPubSocket.setTCPKeepAlive(0);
                    mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                    mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                    mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                    mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                    mPubSocket.send(taskMessageToSend.toString());
                    Timber.i("Task message sent %s", actionPrefix);

                    try {
                        Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                    } catch (InterruptedException e) {
                        Timber.i("Interrupted while sleeping: %s", e);
                        return;
                    }
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
    }

//    /**
//     * Send synchronise request message
//     */
//    public synchronized void sendSyncReqMessage() {
//
//        PowerManagerUtil.acquirePartialWakeLock();
//
//        mCustomThreadPoolManager.addRunnable(new Runnable() {
//            @Override
//            public void run() {
//
//                Timber.i( "Publishing %s, message: %s" , TOPIC_SYNC, SYNC_DATA);
//
//                mPubSocket.setMaxMsgSize(-1);
//                mPubSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
//                mPubSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
//                mPubSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
//                mPubSocket.setLinger(0);
//                mPubSocket.setRcvHWM(0);
//                mPubSocket.setSndHWM(0);
//                mPubSocket.setImmediate(true);
//                mPubSocket.setTCPKeepAlive(1);
//                mPubSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
//                mPubSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
//                mPubSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
//                mPubSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
//                mPubSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
//                mPubSocket.send(SYNC_DATA);
//
//                Timber.i( "Sync message sent");
//
//                try {
//                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
//                } catch (InterruptedException e) {
//                    Timber.i("Interrupted while sleeping: %s" , e);
//                    return;
//                }
//            }
//        });
//
//        PowerManagerUtil.releasePartialWakeLock();
//    }

    public synchronized void broadcastSyncData() {
        broadcastSitRepForSync();
        broadcastTaskForSync();
    }

    /**
     * Synchronise Sit Rep with other users over the network
     */
    private synchronized void broadcastSitRepForSync() {
        Timber.i("Synchronising Sit Rep data...");

        SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());

        SingleObserver<List<SitRepModel>> singleObserverGetAllSitReps = new SingleObserver<List<SitRepModel>>() {

            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<SitRepModel> sitRepModelList) {
                Timber.i("onSuccess singleObserverGetAllSitReps, broadcastSitRepForSync. sitRepModelList.size(): %d",
                        sitRepModelList.size());

                List<String> modelJsonList = new ArrayList<>();

                for (int i = 0; i < sitRepModelList.size(); i++) {
                    Gson gson = GsonCreator.createGson();
                    String modelJson = gson.toJson(sitRepModelList.get(i));

                    modelJsonList.add(modelJson);

                    Timber.i("Sending SitRep message: %s", modelJson);
                }

//                sendSitRepMessageList(serverPairSocket, modelJsonList, JeroMQPublisher.TOPIC_SYNC);
                for (int i = 0; i < modelJsonList.size(); i++) {
                    String message = modelJsonList.get(i);
                    sendSitRepMessage(message, JeroMQPublisher.TOPIC_SYNC);
                }
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllSitReps, broadcastSitRepForSync. Error Msg: %s ", e.toString());
            }
        };

        sitRepRepo.getAllSitReps(singleObserverGetAllSitReps);
    }

    /**
     * Synchronise Task with other users over the network
     */
    private synchronized void broadcastTaskForSync() {
        Timber.i("Synchronising Task data...");

        TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());

        SingleObserver<List<TaskModel>> singleObserverGetAllTasks = new SingleObserver<List<TaskModel>>() {

            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<TaskModel> taskModelList) {
                Timber.i("onSuccess singleObserverGetAllTasks, broadcastTaskForSync. taskModelList.size(): %d",
                        taskModelList.size());

                List<String> modelJsonList = new ArrayList<>();

                for (int i = 0; i < taskModelList.size(); i++) {
                    Gson gson = GsonCreator.createGson();
                    String modelJson = gson.toJson(taskModelList.get(i));

                    modelJsonList.add(modelJson);

                    Timber.i("Sending Task message: %s", modelJson);
                }

                for (int i = 0; i < modelJsonList.size(); i++) {
                    String message = modelJsonList.get(i);
                    sendTaskMessage(message, JeroMQPublisher.TOPIC_SYNC);
                }
//                sendTaskMessageList(serverPairSocket, modelJsonList, JeroMQPublisher.TOPIC_SYNC);
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllTasks, broadcastTaskForSync. Error Msg: %s ", e.toString());
            }
        };

        taskRepo.getAllTasks(singleObserverGetAllTasks);
    }

    /**
     * Combines initial prefix with action prefix
     * For e.g., initial prefix - 'PREFIX-SITREP', action prefix - 'INSERT', result is 'PREFIX-SITREP-INSERT'
     *
     * @param initialPrefix
     * @param actionPrefix
     * @return
     */
    private String getActionPrefix(String initialPrefix, String actionPrefix) {
        StringBuilder topicPrefix = new StringBuilder();
        topicPrefix.append(initialPrefix);
        topicPrefix.append(StringUtil.HYPHEN);
        topicPrefix.append(actionPrefix);

        return topicPrefix.toString();
    }

    /**
     * Disconnect sockets when thread is closed
     */
    @Override
    public synchronized void stop() {
        super.stop();

//        CloseSocketAsyncTask task = new CloseSocketAsyncTask(mZContext, mPubSocket);
//        task.execute();

        if (isSocketStarted) {
//            if (mPubSocket != null) {
//                mPubSocket.unbind(SERVER_PUB_IP_ADDRESS);
//                Timber.i("Server pub socket unbound.");
//
//                mPubSocket.close();
//                Timber.i("Server pub socket closed.");
//
//                if (mZContext != null) {
//                    mZContext.destroySocket(mPubSocket);
//                    Timber.i("Server pub socket destroyed.");
//                }
//
//            }

            if (mZContext != null && !mZContext.isClosed()) {
                Timber.i("Destroying ZContext.");

                mZContext.destroy();
                Timber.i("ZContext destroyed.");
            }

            mPubSocket = null;
            isSocketStarted = false;
        }
    }
}
