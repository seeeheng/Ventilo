package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class JeroMQSubscriber extends JeroMQParentSubscriber implements JeroMQSubscriberRunnable.CloseSocketsListener {

    //    private static final Logger LOGGER = LoggerFactory.getLogger(JeroMQSubscriber.class);
    private static final String TAG = JeroMQSubscriber.class.getSimpleName();
    private static final int DEFAULT_PORT = 5556;

//    protected static final String CLIENT_SUB_IP_ADDRESS = "tcp://192.168.1.2:" +
//            JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT;

    private static volatile JeroMQSubscriber instance;

    //    private List<String> CLIENT_SUB_IP_ADDRESSES;
    private List<String> mClientSubEndpointList;
    private List<Socket> mSocketList;

    private ZContext mZContext;
    private JeroMQSubscriberRunnable mJeroMQSubscriberRunnable;
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
                } else {
                    instance.initExecutorService();
                }
            }
        } else {
            instance.initExecutorService();
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
//        mClientSubEndpointList.add("tcp://192.168.1.3:" + DEFAULT_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.4:" + DEFAULT_PORT);

//        mClientSubEndpointList.add("tcp://192.168.1.17:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.58:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.1.94:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://192.168.43.38:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.43.58:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);
//        mClientSubEndpointList.add("tcp://192.168.43.94:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://198.18.2.6:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://*:" + JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);

//        mClientSubEndpointList.add("tcp://" +
//                NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
//                JeroMQPubSubBrokerProxy.DEFAULT_XPUB_PORT);


//        mSubSocket = mZContext.createSocket(SocketType.SUB);
//        mSubSocket.connect(CLIENT_SUB_IP_ADDRESS);
//        mSubSocket.setLinger(-1);
////        mSocket.setSndHWM(   0 );

        String ownDeviceIpAddress = NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true);

        String[] fixedIpAddresses = MainApplication.getAppContext().getResources().
                getStringArray(R.array.login_user_mobile_ip_addresses);

        Log.i(TAG, "Own Device IP Address to be excluded for broadcast: " + ownDeviceIpAddress);

        for (int i = 0; i < fixedIpAddresses.length; i++) {
            if (!ownDeviceIpAddress.equalsIgnoreCase(fixedIpAddresses[i])) {
                String endpoint = "tcp://";
                endpoint = endpoint.concat(fixedIpAddresses[i]).concat(StringUtil.COLON);
                endpoint = endpoint.concat(String.valueOf(DEFAULT_PORT));

                Log.d(TAG, "Endpoint " + i + ":" + endpoint);

                mClientSubEndpointList.add(endpoint);
            }
        }

        Log.i(TAG, "Client sub sockets connected");

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                PowerManagerUtil.acquirePartialWakeLock();

                for (int i = 0; i < mClientSubEndpointList.size(); i++) {
                    Socket socket = mZContext.createSocket(SocketType.SUB);
//            socket.setHWM(5000);
                    socket.setMaxMsgSize(-1);
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

                //  Initialize poll set
                ZMQ.Poller items = mZContext.createPoller(mSocketList.size());
                for (int i = 0; i < mSocketList.size(); i++) {
                    items.register(mSocketList.get(i), ZMQ.Poller.POLLIN);
                }

                mJeroMQSubscriberRunnable = new JeroMQSubscriberRunnable(mSocketList, items,
                        JeroMQSubscriber.this);
                Thread JeroMQSubscriberThread = new Thread(mJeroMQSubscriberRunnable);
                JeroMQSubscriberThread.start();

//                for (int i = 0; i < mSocketList.size(); i++) {
////                    final String currentAddress = CLIENT_SUB_IP_ADDRESSES.get(i);
////                    socket.connect(currentAddress);
////                    socket.setLinger(-1);
//
//                    Runnable runnable = new JeroMQSubscriberRunnable(mSocketList.get(i));
//                    Thread JeroMQSubscriberThread = new Thread(runnable);
//                    JeroMQSubscriberThread.start();
//                }

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

        if (mJeroMQSubscriberRunnable != null) {
            mJeroMQSubscriberRunnable.closePoller();
        }

//        CloseSocketAsyncTask task = new CloseSocketAsyncTask(mZContext,
//                mSocketList, mClientSubEndpointList);
//        task.execute();
    }

    @Override
    public void closeSockets() {
        if (mZContext != null) {
            for (int i = 0; i < mSocketList.size(); i++) {
                Socket socket = mSocketList.get(i);

                if (mClientSubEndpointList != null &&
                        mClientSubEndpointList.get(i) != null) {
                    socket.disconnect(mClientSubEndpointList.get(i));
                    Log.i(TAG, "Client sub socket [" + i + "] disconnected.");

                    socket.close();
                    Log.i(TAG, "Client sub socket [" + i + "] closed.");
                }

                mZContext.destroySocket(socket);
                Log.i(TAG, "Client sub socket [" + i + "] destroyed.");
            }
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Log.i(TAG, "Destroying ZContext.");
            mZContext.destroy();
            Log.i(TAG, "ZContext destroyed.");
        }
    }

//    private static class CloseSocketAsyncTask extends AsyncTask<String, Void, Void> {
//
//        private ZContext mZContext;
//        private List<Socket> mSocketList;
//        private List<String> mClientSubEndpointList;
//
//        CloseSocketAsyncTask(ZContext zContext, List<Socket> socketList,
//                             List<String> clientSubEndpointList) {
//            mZContext = zContext;
//            mSocketList = socketList;
//            mClientSubEndpointList = clientSubEndpointList;
//        }
//
//        @Override
//        protected Void doInBackground(final String... param) {
//            for (int i = 0; i < mSocketList.size(); i++) {
//                mZContext.destroySocket(mSocketList.get(i));
////                mSocketList.get(i).disconnect(mClientSubEndpointList.get(i));
////                mSocketList.get(i).close();
//            }
//            Log.i(TAG, "Client sub sockets closed.");
//
//            if (mZContext != null) {
//                Log.i(TAG, "Destroying ZContext.");
//                mZContext.destroy();
//                Log.i(TAG, "ZContext destroyed.");
//
////                List<Socket> socketList = mZContext.getSockets();
//////                for (int i = 0; i < mZContext.getSockets().size(); i++) {
//////                    socketList.get(i).disconnect(mClientSubEndpointList.get(i));
//////                    socketList.get(i).close();
//////                }
//            }
//
//            return null;
//        }
//    }
}
