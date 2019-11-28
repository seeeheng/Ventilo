package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;

import com.google.gson.Gson;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

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
public class JeroMQServerPair extends JeroMQParent {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQPublisher.class);
    private static final String TAG = JeroMQServerPair.class.getSimpleName();

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://" +
//            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://" +
//            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//            DEFAULT_PORT;

    protected static final String SERVER_PAIR_IP_ADDRESS = "tcp://*:" + PAIR_PORT;

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://192.168.1.3:" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

    public static final String TOPIC_PREFIX_SITREP = "PREFIX-SITREP";
    public static final String TOPIC_PREFIX_TASK = "PREFIX-TASK";
    //    public static final String TOPIC_PREFIX_USER_SITREP_JOIN = "PREFIX-USER-SITREP-JOIN";
//    public static final String TOPIC_PREFIX_USER_TASK_JOIN = "PREFIX-USER-TASK-JOIN";
    public static final String TOPIC_SYNC = "SYNC";

    private static volatile JeroMQServerPair instance;
    //    private Socket mSocket;
//    private ZContext mZContext;
//    private Socket mServerPairSocket;

    private boolean mIsSocketToBeClosed;

    /**
     * Fixed number of threads dependant on the number of device IP addresses available
     */
    private JeroMQServerPair() {
        super(CustomThreadPoolManager.getInstance());
    }

    /**
     * Create/get singleton class instance
     *
     * @return
     */
    public static JeroMQServerPair getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (JeroMQServerPair.class) {
                if (instance == null) {
                    instance = new JeroMQServerPair();
                } else {
                    instance.initCustomThreadPoolManagerService();
                }
            }
        } else {
            instance.initCustomThreadPoolManagerService();
        }

        return instance;
    }

    /**
     * Creates a single thread for each IP address for server processing of messages from different topics
     * Processes messages based on topics
     */
    @Override
    protected void startProcess() {
        Timber.i("Start server pair sockets connection");
        Timber.i("OWN IP address is: %s", NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {

                ZContext zContext = new ZContext();
                zContext.setLinger(0);
                zContext.setRcvHWM(0);
                zContext.setSndHWM(0);

                Socket serverPairSocket = zContext.createSocket(SocketType.PAIR);
                serverPairSocket.setMaxMsgSize(-1);
                serverPairSocket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                serverPairSocket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                serverPairSocket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                serverPairSocket.setLinger(0);
                serverPairSocket.setRcvHWM(0);
                serverPairSocket.setSndHWM(0);
                serverPairSocket.setImmediate(true);
                serverPairSocket.setTCPKeepAlive(1);
                serverPairSocket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                serverPairSocket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                serverPairSocket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                serverPairSocket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                serverPairSocket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                serverPairSocket.bind(SERVER_PAIR_IP_ADDRESS);
                Timber.i("Server pair sockets connected");

                while (!Thread.currentThread().isInterrupted()) {

                    if (mIsSocketToBeClosed) {
                        closeSockets(zContext, serverPairSocket);
                        break;
                    }

                    String message = "";

                    try {
                        message = serverPairSocket.recvStr();

                        if (message != null) {
                            Timber.i("Received message: %s", message);

                            switch (message) {
                                case SYNC_DATA:
                                    sendSyncData(serverPairSocket);
                                    break;
                                default:
                                    break;
                            }
                        }

                    } catch (ZMQException e) {
                        Timber.i("Socket exception - %s", e);
                    }
                }

            }
        });

    }

    private void sendSyncData(Socket serverPairSocket) {
        unicastSitRepForSync(serverPairSocket);
        unicastTaskForSync(serverPairSocket);
    }

    /**
     * Synchronise Sit Rep with CCT over the network
     */
    private void unicastSitRepForSync(Socket serverPairSocket) {
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

                sendSitRepMessageList(serverPairSocket, modelJsonList, JeroMQPublisher.TOPIC_SYNC);
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllSitReps, broadcastSitRepForSync. Error Msg: %s ", e.toString());
            }
        };

        sitRepRepo.getAllSitReps(singleObserverGetAllSitReps);
    }

    /**
     * Synchronise Task with CCT over the network
     */
    private void unicastTaskForSync(Socket serverPairSocket) {
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

                sendTaskMessageList(serverPairSocket, modelJsonList, JeroMQPublisher.TOPIC_SYNC);
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllTasks, broadcastTaskForSync. Error Msg: %s ", e.toString());
            }
        };

        taskRepo.getAllTasks(singleObserverGetAllTasks);
    }

    /**
     * Unicasts Sit Rep JSON message list to all targeted device(s) for data storage
     *
     * @param messageList
     */
    public synchronized void sendSitRepMessageList(Socket serverPairSocket,
                                                   List<String> messageList,
                                                   String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            final String currentAddress = ADDRESSES.get(i);

//        mCustomThreadPoolManager.addRunnable(new Runnable() {
//            @Override
//            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
        PowerManagerUtil.acquirePartialWakeLock();

        for (int i = 0; i < messageList.size(); i++) {
            String message = messageList.get(i);

            String topicPrefix = getActionPrefix(TOPIC_PREFIX_SITREP, actionPrefix);

            StringBuilder sitRepMessageToSend = new StringBuilder();
            sitRepMessageToSend.append(topicPrefix);
            sitRepMessageToSend.append(StringUtil.SPACE);
            sitRepMessageToSend.append(message);

            Timber.i("Publishing SitRep %s, message: %s", actionPrefix, message);

            serverPairSocket.send(sitRepMessageToSend.toString());

            Timber.i("SitRep message sent %s", actionPrefix);

            try {
                Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
            } catch (InterruptedException e) {
                Timber.e("Interrupted while sleeping: %s", e);
                return;
            }
        }

        PowerManagerUtil.releasePartialWakeLock();
//            }
//        });
//        }
    }

    /**
     * Unicasts Task JSON message list to all targeted device(s) for data storage
     *
     * @param messageList
     */
    public synchronized void sendTaskMessageList(Socket serverPairSocket,
                                                 List<String> messageList,
                                                 String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            System.out.println("sendTaskMessage two");
//            LOGGER.info(" in sendTaskMessage {}", message);
//            final String currentAddress = ADDRESSES.get(i);

//        mCustomThreadPoolManager.addRunnable(new Runnable() {
//            @Override
//            public void run() {

        PowerManagerUtil.acquirePartialWakeLock();

        for (int i = 0; i < messageList.size(); i++) {
            String message = messageList.get(i);

//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
            String topicPrefix = getActionPrefix(TOPIC_PREFIX_TASK, actionPrefix);

            StringBuilder taskMessageToSend = new StringBuilder();
            taskMessageToSend.append(topicPrefix);
            taskMessageToSend.append(StringUtil.SPACE);
            taskMessageToSend.append(message);

            Timber.i("Publishing Task %s, message: %s", actionPrefix, taskMessageToSend);

            serverPairSocket.send(taskMessageToSend.toString());
            Timber.i("Task %s message sent", actionPrefix);

            try {
                Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
            } catch (InterruptedException e) {
                Timber.i("Interrupted while sleeping: %s", e);
                return;
            }
        }

        PowerManagerUtil.releasePartialWakeLock();
//            }
//        });
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
    public void stop() {
        super.stop();

        mIsSocketToBeClosed = true;
    }

    /**
     * Close all required/opened Pair sockets
     */
    private void closeSockets(ZContext zContext, Socket serverPairSocket) {
        if (serverPairSocket != null) {
            serverPairSocket.unbind(SERVER_PAIR_IP_ADDRESS);
            Timber.i("Server pair socket unbound.");

            serverPairSocket.close();
            Timber.i("Server pair socket closed.");

            if (zContext != null) {
                zContext.destroySocket(serverPairSocket);
                Timber.i("Server pair socket destroyed.");
            }
        }

        if (zContext != null && !zContext.isClosed()) {
            Timber.i("Destroying ZContext.");
            zContext.destroy();
            Timber.i("ZContext destroyed.");
        }
    }
}
