package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import timber.log.Timber;

public class JeroMQClientPair extends JeroMQParent implements JeroMQClientPairRunnable.CloseSocketsListener {

    private static final String TAG = JeroMQClientPair.class.getSimpleName();

    private static volatile JeroMQClientPair instance;

    private List<String> mClientPairEndpointList;
    private List<Socket> mSocketList;

    private ZContext mZContext;
    private JeroMQClientPairRunnable mJeroMQClientPairRunnable;
//    private Socket mSubSocket;

    private String mTargetPhoneIPAddress;

    /**
     * Creates a super class instance
     */
    private JeroMQClientPair() {
        super(CustomThreadPoolManager.getInstance());
        mZContext = new ZContext();
        mClientPairEndpointList = new ArrayList<>();
        mSocketList = new ArrayList<>();
    }

    /**
     * Creates/gets singleton class instance
     *
     * @return
     */
    public static JeroMQClientPair getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (JeroMQClientPair.class) {
                if (instance == null) {
                    instance = new JeroMQClientPair();
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
     * Set target phone IP address to unicast message
     * @param targetPhoneIPAddress
     */
    public void setTargetPhoneIPAddress(String targetPhoneIPAddress) {
        Timber.i("Target Phone IP Address: %s ", targetPhoneIPAddress);
        mTargetPhoneIPAddress = targetPhoneIPAddress;
    }

    /**
     * Send synchronise request message
     */
    public void sendSyncReqMessage() {

        if (mTargetPhoneIPAddress != null) {
            Socket socket = null;

            for (int i = 0; i < mSocketList.size(); i++) {
                Timber.i("Socket list, Socket (%d) last endpoint: %s",
                        i,  mSocketList.get(i).getLastEndpoint());

                String endpoint = "tcp://";
                endpoint = endpoint.concat(mTargetPhoneIPAddress).concat(StringUtil.COLON);
                endpoint = endpoint.concat(String.valueOf(PAIR_PORT));

                if (endpoint.equalsIgnoreCase(mSocketList.get(i).getLastEndpoint())) {
                    socket = mSocketList.get(i);
                }
            }

            if (socket != null) {
                Timber.i("Sending Sync message request...");
                socket.send(SYNC_DATA);
            }
        }
    }

    /**
     * Starts a single scheduled thread of defined interval for client message processing
     * based on subscribed topics for each device IP address.
     * Topics are defined as the first word of each incoming message
     */
    @Override
    protected void startProcess() {

        if (mTargetPhoneIPAddress != null) {
            boolean isTargetEndpointFound = false;

            String endpoint = "tcp://";
            endpoint = endpoint.concat(mTargetPhoneIPAddress).concat(StringUtil.COLON);
            endpoint = endpoint.concat(String.valueOf(PAIR_PORT));

            Timber.i("Endpoint %s ", endpoint);

            for (int i = 0; i < mClientPairEndpointList.size(); i++) {
                if (endpoint.equalsIgnoreCase(mClientPairEndpointList.get(i))) {
                    isTargetEndpointFound = true;
                }
            }

            if (!isTargetEndpointFound) {
                Timber.i("Start new client pair socket connection");

                mClientPairEndpointList.add(endpoint);

                Timber.i("Creating and connecting client pair socket...");

                PowerManagerUtil.acquirePartialWakeLock();

                for (int i = 0; i < mClientPairEndpointList.size(); i++) {
                    Socket socket = mZContext.createSocket(SocketType.PAIR);
//            socket.setHWM(5000);
                    socket.setMaxMsgSize(-1);
                    socket.setLinger(0);
                    socket.connect(mClientPairEndpointList.get(i));
                    mSocketList.add(socket);
                }

                Timber.i("Client pair socket connected");

                //  Initialize poll set
                ZMQ.Poller items = mZContext.createPoller(mSocketList.size());
                for (int i = 0; i < mSocketList.size(); i++) {
                    items.register(mSocketList.get(i), ZMQ.Poller.POLLIN);
                }

                mJeroMQClientPairRunnable = new JeroMQClientPairRunnable(mSocketList, items,
                        JeroMQClientPair.this);

                mCustomThreadPoolManager.addRunnable(mJeroMQClientPairRunnable);

                PowerManagerUtil.releasePartialWakeLock();

            } else {
                Timber.i("Client pair socket connection has already been established.");
            }
        }
    }

    /**
     * Disconnect sockets when thread is closed
     */
    @Override
    public void stop() {
        super.stop();

        if (mJeroMQClientPairRunnable != null) {
            mJeroMQClientPairRunnable.closePoller();
        }
    }

    @Override
    public void closeSockets() {
        if (mZContext != null) {
            for (int i = 0; i < mSocketList.size(); i++) {
                Socket socket = mSocketList.get(i);

                if (mClientPairEndpointList != null &&
                        mClientPairEndpointList.get(i) != null) {
                    socket.disconnect(mClientPairEndpointList.get(i));
                    Timber.i("Client pair socket disconnected %d", i);

                    socket.close();
                    Timber.i("Client pair socket closed %d", i);
                }

                mZContext.destroySocket(socket);
                Timber.i("Client pair socket destroyed %d", i);
            }
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Timber.i("Destroying ZContext.");

            mZContext.destroy();

            Timber.i("ZContext destroyed.");
        }
    }
}
