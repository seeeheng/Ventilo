package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
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
    public static JeroMQPublisher getInstance() {
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
    protected void startProcess() {
        Timber.i("Start server pub sockets connection");
        Timber.i("OWN IP address is: %s" , NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

        mZContext = new ZContext();
        mPubSocket = mZContext.createSocket(SocketType.PUB);
        mPubSocket.setMaxMsgSize(-1);
//        mPubSocket.setMsgAllocationHeapThreshold(1024 * 1024);
//        mPubSocket.setSendBufferSize(1024 * 1024);
        mPubSocket.setLinger(0);
//        mPubSocket.connect(SERVER_PUB_IP_ADDRESS);
        mPubSocket.bind(SERVER_PUB_IP_ADDRESS);
        Timber.i("Server pub sockets connected");

   }

    /**
     * Broadcasts User JSON message to all relevant devices for data storage
     *
     * @param message
     */
    public void sendUserMessage(String message, String actionPrefix) {
        String topicPrefix = getActionPrefix(TOPIC_PREFIX_USER, actionPrefix);

        if (StringUtil.EMPTY_STRING.equalsIgnoreCase(topicPrefix)) {
            return;
        }

        Timber.i("Publishing User");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
                StringBuilder userMessageToSend = new StringBuilder();
                userMessageToSend.append(topicPrefix);
                userMessageToSend.append(StringUtil.SPACE);
                userMessageToSend.append(message);

                Timber.i("Publishing User %s" , actionPrefix) ;
                Timber.i( "message: %s" , userMessageToSend);

                mPubSocket.send(userMessageToSend.toString());

                Timber.i( "User %s" , actionPrefix);

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                } catch (InterruptedException e) {
                    Timber.e( "Interrupted while sleeping: " + e);
                    return;
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
    public void sendWaveRelayRadioMessage(String message, String actionPrefix) {

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_RADIO, actionPrefix);

        Timber.i( "Publishing WaveRelay Radio");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
                StringBuilder radioMessageToSend = new StringBuilder();
                radioMessageToSend.append(topicPrefix);
                radioMessageToSend.append(StringUtil.SPACE);
                radioMessageToSend.append(message);
                Timber.i( "Publishing WaveRelay Radio %s" , actionPrefix );
                Timber.i( "Publishing WaveRelay Radio message %s" , message );

                mPubSocket.send(radioMessageToSend.toString());
                Timber.i( "WaveRelay Radio message sent %s" , actionPrefix );

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                } catch (InterruptedException e) {
                    Timber.e( "Interrupted while sleeping: %s" , e);
                    return;
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
    public void sendBFTMessage(String message, String actionPrefix) {
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

                StringBuilder bftMessageToSend = new StringBuilder();
                bftMessageToSend.append(topicPrefix);
                bftMessageToSend.append(" ");
                bftMessageToSend.append(message);

                Timber.i("Publishing BFT message: %s" , message );

                mPubSocket.send(bftMessageToSend.toString());

                Timber.i("BFT message sent");

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                } catch (InterruptedException e) {
                    Timber.e("Interrupted while sleeping:  %s" , e);
                    return;
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
    public void sendSitRepMessage(String message, String actionPrefix) {
//        for (int i = 0; i < ADDRESSES.size(); i++) {
//            final String currentAddress = ADDRESSES.get(i);

        String topicPrefix = getActionPrefix(TOPIC_PREFIX_SITREP, actionPrefix);

        Timber.i( "Publishing SitRep");

        PowerManagerUtil.acquirePartialWakeLock();

        mCustomThreadPoolManager.addRunnable(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
                StringBuilder sitRepMessageToSend = new StringBuilder();
                sitRepMessageToSend.append(topicPrefix);
                sitRepMessageToSend.append(StringUtil.SPACE);
                sitRepMessageToSend.append(message);

                Timber.i( "Publishing SitRep %s %s" , actionPrefix , message );

                mPubSocket.send(sitRepMessageToSend.toString());

                Timber.i( "SitRep message sent %s" , actionPrefix );

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                } catch (InterruptedException e) {
                    Timber.e( "Interrupted while sleeping: %s" , e);
                    return;
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
    public void sendTaskMessage(String message, String actionPrefix) {
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

                StringBuilder taskMessageToSend = new StringBuilder();
                taskMessageToSend.append(topicPrefix);
                taskMessageToSend.append(StringUtil.SPACE);
                taskMessageToSend.append(message);

                Timber.i( "Publishing Task %s  , messge: %s" , actionPrefix, taskMessageToSend );

                mPubSocket.send(taskMessageToSend.toString());
                Timber.i( "Task message sent %s" , actionPrefix );

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE);
                } catch (InterruptedException e) {
                    Timber.i("Interrupted while sleeping: %s" , e);
                    return;
                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
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

//    /**
//     * Broadcasts UserSitRepJoin JSON message to all relevant devices for data storage
//     *
//     * @param message
//     */
//    public void sendUserSitRepJoinMessage(String message) {
////        for (int i = 0; i < ADDRESSES.size(); i++) {
////            System.out.println("sendTaskMessage two");
////            LOGGER.info(" in sendTaskMessage {}", message);
////            final String currentAddress = ADDRESSES.get(i);
//
//        mExecutorService.submit(new Runnable() {
//            @Override
//            public void run() {
////                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
//
//                StringBuilder userSitRepJoinMessageToSend = new StringBuilder();
//                userSitRepJoinMessageToSend.append(TOPIC_PREFIX_USER_SITREP_JOIN);
//                userSitRepJoinMessageToSend.append(" ");
//                userSitRepJoinMessageToSend.append(message);
//
//                Log.i(TAG, "Publishing UserSitRepJoin message: " + userSitRepJoinMessageToSend + "...");
//
//                mSocket.send(userSitRepJoinMessageToSend.toString());
//
//                Log.i(TAG, "UserSitRepJoin message sent");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Interrupted while sleeping: " + e);
//                    return;
//                }
//            }
//        });
//    }
//
//    /**
//     * Broadcasts UserTaskJoin JSON message to all relevant devices for data storage
//     *
//     * @param message
//     */
//    public void sendUserTaskJoinMessage(String message) {
////        for (int i = 0; i < ADDRESSES.size(); i++) {
////            System.out.println("sendTaskMessage two");
////            LOGGER.info(" in sendTaskMessage {}", message);
////            final String currentAddress = ADDRESSES.get(i);
//
//        mExecutorService.submit(new Runnable() {
//            @Override
//            public void run() {
////                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
//
//                StringBuilder userTaskJoinMessageToSend = new StringBuilder();
//                userTaskJoinMessageToSend.append(TOPIC_PREFIX_USER_TASK_JOIN);
//                userTaskJoinMessageToSend.append(" ");
//                userTaskJoinMessageToSend.append(message);
//
//                Log.i(TAG, "Publishing UserTaskJoin message: " + userTaskJoinMessageToSend + "...");
//
//                mSocket.send(userTaskJoinMessageToSend.toString());
//
//                Log.i(TAG, "UserTaskJoin message sent");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    Log.e(TAG, "Interrupted while sleeping: " + e);
//                    return;
//                }
//            }
//        });
//    }

    /**
     * Disconnect sockets when thread is closed
     */
    @Override
    public void stop() {
        super.stop();

//        CloseSocketAsyncTask task = new CloseSocketAsyncTask(mZContext, mPubSocket);
//        task.execute();

        if (mPubSocket != null) {
            mPubSocket.unbind(SERVER_PUB_IP_ADDRESS);
            Timber.i("Server pub socket unbound.");

            mPubSocket.close();
            Timber.i("Server pub socket closed.");

            if (mZContext != null) {
                mZContext.destroySocket(mPubSocket);
                Timber.i("Server pub socket destroyed.");
            }
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Timber.i("Destroying ZContext.");

            mZContext.destroy();
            Timber.i("ZContext destroyed.");
        }
    }
}
