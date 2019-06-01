package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.os.AsyncTask;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;

public class JeroMQSubscriber extends JeroMQParentSubscriber {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQSubscriber.class);
    private static final String TAG = JeroMQSubscriber.class.getSimpleName();

//    protected static final String CLIENT_SUB_IP_ADDRESS = "tcp://192.168.1.2:" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT;


    private static volatile JeroMQSubscriber instance;

//    private List<String> CLIENT_SUB_IP_ADDRESSES;
    private List<String> mClientSubEndpointList;
    private List<Socket> mSocketList;

    private ZContext mZContext;
//    private Socket mSubSocket;

    /**
     * Creates a super class instance
     */
    private JeroMQSubscriber() {
        super(Executors.newSingleThreadExecutor());
        mZContext = new ZContext();
        mClientSubEndpointList = new ArrayList<>();
        mSocketList = new ArrayList<>();
    }

    /**
     * Creates/gets singleton class instance
     *
     * @return
     */
    public static JeroMQSubscriber getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (JeroMQSubscriber.class) {
                if (instance == null) {
                    instance = new JeroMQSubscriber();
                }
            }
        }

        return instance;
    }

    /**
     * Starts a single scheduled thread of defined interval for client message processing
     * based on subscribed topics for each device IP address.
     * Topics are defined as the first word of each incoming message
     */
    @Override
    protected void startClientProcess() {
        Log.i(TAG, "Start client sub sockets connection");

        // Add client IP Addresses to be connected to in this address list
//        mClientSubEndpointList.add("tcp://192.168.1.3:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.2:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
        mClientSubEndpointList.add("tcp://192.168.1.3:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
        mClientSubEndpointList.add("tcp://192.168.1.4:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://192.168.1.17:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.58:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.94:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://192.168.43.58:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.43.94:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://*:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://" +
//                NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//                JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);



//        mSubSocket = mZContext.createSocket(SocketType.SUB);
//        mSubSocket.connect(CLIENT_SUB_IP_ADDRESS);
//        mSubSocket.setLinger(-1);
////        mSocket.setSndHWM(   0 );

        Log.i(TAG, "Client sub sockets connected");

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                PowerManagerUtil.acquirePartialWakeLock();

                for (int i = 0; i < mClientSubEndpointList.size(); i++) {
                    Socket socket = mZContext.createSocket(SocketType.SUB);
//            socket.setHWM(5000);
                    socket.setLinger(0);
                    socket.connect(mClientSubEndpointList.get(i));
//                    boolean isConnected;
//                    isConnected = socket.connect(mClientSubEndpointList.get(i));
//                    if (BuildConfig.DEBUG && !(isConnected)) { throw new AssertionError(); }
//            socket.bind(mClientSubEndpointList.get(i));
//            socket.setLinger(-1);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    mSocketList.add(socket);
                }

                Log.i(TAG, "Creating and connecting SUB socket...");

                for (int i = 0; i < mSocketList.size(); i++) {
//                    final String currentAddress = CLIENT_SUB_IP_ADDRESSES.get(i);
//                    socket.connect(currentAddress);
//                    socket.setLinger(-1);

                    Runnable runnable = new JeroMQSubscriberRunnable(mSocketList.get(i));
                    Thread JeroMQSubscriberThread = new Thread(runnable);
                    JeroMQSubscriberThread.start();
                }

                PowerManagerUtil.releasePartialWakeLock();
            }
        });
    }

    /**
     * Disconnect sockets when thread is closed
     */
    @Override
    public void stop() {
        super.stop();

//        CloseSocketAsyncTask task = new CloseSocketAsyncTask(mZContext,
//                mSocketList, mClientSubEndpointList);
//        task.execute();

        for (int i = 0; i < mSocketList.size(); i++) {
            mZContext.destroySocket(mSocketList.get(i));
            Log.i(TAG, "Client sub socket [" + i + "] closed.");
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Log.i(TAG, "Destroying ZContext.");
            mZContext.destroy();
            Log.i(TAG, "ZContext destroyed.");
        }
    }

    private static class CloseSocketAsyncTask extends AsyncTask<String, Void, Void> {

        private ZContext mZContext;
        private List<Socket> mSocketList;
        private List<String> mClientSubEndpointList;

        CloseSocketAsyncTask(ZContext zContext, List<Socket> socketList,
                             List<String> clientSubEndpointList) {
            mZContext = zContext;
            mSocketList = socketList;
            mClientSubEndpointList = clientSubEndpointList;
        }

        @Override
        protected Void doInBackground(final String... param) {
            for (int i = 0; i < mSocketList.size(); i++) {
                mZContext.destroySocket(mSocketList.get(i));
//                mSocketList.get(i).disconnect(mClientSubEndpointList.get(i));
//                mSocketList.get(i).close();
            }
            Log.i(TAG, "Client sub sockets closed.");

            if (mZContext != null) {
                Log.i(TAG, "Destroying ZContext.");
                mZContext.destroy();
                Log.i(TAG, "ZContext destroyed.");

//                List<Socket> socketList = mZContext.getSockets();
////                for (int i = 0; i < mZContext.getSockets().size(); i++) {
////                    socketList.get(i).disconnect(mClientSubEndpointList.get(i));
////                    socketList.get(i).close();
////                }
            }

            return null;
        }
    }
}
