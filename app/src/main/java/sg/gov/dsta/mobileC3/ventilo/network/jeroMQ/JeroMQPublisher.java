package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.os.AsyncTask;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.List;
import java.util.concurrent.Executors;

import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;

/**
 * Reads from multiple sockets in Java
 */
public class JeroMQPublisher extends JeroMQParentPublisher {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQPublisher.class);
    private static final String TAG = JeroMQPublisher.class.getSimpleName();
//    private static final int DEFAULT_PORT = 5556;

//    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://" +
//            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

    protected static final String SERVER_PUB_IP_ADDRESS = "tcp://*:" +
            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

    public static final String TOPIC_PREFIX_BFT = "PREFIX-BFT";
    public static final String TOPIC_PREFIX_SITREP = "PREFIX-SITREP";
    public static final String TOPIC_PREFIX_TASK = "PREFIX-TASK";
    //    public static final String TOPIC_PREFIX_USER_SITREP_JOIN = "PREFIX-USER-SITREP-JOIN";
//    public static final String TOPIC_PREFIX_USER_TASK_JOIN = "PREFIX-USER-TASK-JOIN";
    public static final String TOPIC_SYNC = "SYNC";
    public static final String TOPIC_INSERT = "INSERT";
    public static final String TOPIC_UPDATE = "UPDATE";
    public static final String TOPIC_DELETE = "DELETE";

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
        super(Executors.newSingleThreadExecutor());
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
                }
            }
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
    protected void startServerProcess() {
        Log.i(TAG, "Start server pub sockets connection");
        Log.i(TAG, "Own IP address is: " + NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

        mZContext = new ZContext();
        mPubSocket = mZContext.createSocket(SocketType.PUB);
        mPubSocket.setLinger(0);
//        mPubSocket.connect(SERVER_PUB_IP_ADDRESS);
        mPubSocket.bind(SERVER_PUB_IP_ADDRESS);

        Log.i(TAG, "Server pub sockets connected");
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

        if ("".equalsIgnoreCase(topicPrefix)) {
            return;
        }

        PowerManagerUtil.acquirePartialWakeLock();

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);

                StringBuilder bftMessageToSend = new StringBuilder();
                bftMessageToSend.append(topicPrefix);
                bftMessageToSend.append(" ");
                bftMessageToSend.append(message);

                Log.i(TAG, "Publishing BFT message: " + message + "...");

                mPubSocket.send(bftMessageToSend.toString());

                Log.i(TAG, "BFT message sent");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while sleeping: " + e);
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

        if ("".equalsIgnoreCase(topicPrefix)) {
            return;
        }
        Log.d(TAG, "Publishing SitRep");

        PowerManagerUtil.acquirePartialWakeLock();

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);
                StringBuilder sitRepMessageToSend = new StringBuilder();
                sitRepMessageToSend.append(topicPrefix);
                sitRepMessageToSend.append(" ");
                sitRepMessageToSend.append(message);

                Log.i(TAG, "Publishing SitRep" + actionPrefix + " message: " + message + "...");

                mPubSocket.send(sitRepMessageToSend.toString());

                Log.i(TAG, "SitRep" + actionPrefix + " message sent");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while sleeping: " + e);
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

        if ("".equalsIgnoreCase(topicPrefix)) {
            return;
        }

        PowerManagerUtil.acquirePartialWakeLock();

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
//                    LOGGER.info("Creating and binding PUB socket with address={}", currentAddress);

                StringBuilder taskMessageToSend = new StringBuilder();
                taskMessageToSend.append(topicPrefix);
                taskMessageToSend.append(" ");
                taskMessageToSend.append(message);

                Log.i(TAG, "Publishing Task" + actionPrefix + " message: " + taskMessageToSend + "...");

                mPubSocket.send(taskMessageToSend.toString());

                Log.i(TAG, "Task" + actionPrefix + " message sent");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted while sleeping: " + e);
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
            mZContext.destroySocket(mPubSocket);
            Log.i(TAG, "Server pub socket closed.");
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Log.i(TAG, "Destroying ZContext.");
            mZContext.destroy();
            Log.i(TAG, "ZContext destroyed.");
        }
    }

    private static class CloseSocketAsyncTask extends AsyncTask<String, Void, Void> {

        private ZContext mZContext;
        private Socket mPubSocket;

        CloseSocketAsyncTask(ZContext zContext, Socket pubSocket) {
            mZContext = zContext;
            mPubSocket = pubSocket;
        }

        @Override
        protected Void doInBackground(final String... param) {
//            if (mPubSocket != null) {
//                mZContext.destroySocket(mPubSocket);
////                mPubSocket.disconnect(SERVER_PUB_IP_ADDRESS);
////                mPubSocket.close();
//
//                Log.i(TAG, "Server pub socket closed.");
//            }

            if (mZContext != null) {
                Log.i(TAG, "Destroying ZContext.");
                mZContext.destroy();
                Log.i(TAG, "ZContext destroyed.");

//                List<Socket> socketList = mZContext.getSockets();
//                for (int i = 0; i < mZContext.getSockets().size(); i++) {
//                    mZContext.destroySocket(socketList.get(i));
//                }
            }

            return null;
        }
    }
}
