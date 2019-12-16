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

    private ZContext mZContext;
    private JeroMQClientPairRunnable mJeroMQClientPairRunnable;
    private ZMQ.Poller mPoller;
//    private Socket mSubSocket;

    private String mTargetPhoneIPAddress;

    /**
     * Creates a super class instance
     */
    private JeroMQClientPair() {
        super(CustomThreadPoolManager.getInstance());
        mZContext = new ZContext();
        mZContext.setLinger(0);
        mZContext.setRcvHWM(0);
        mZContext.setSndHWM(0);

        mPoller = mZContext.createPoller(10);
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
     *
     * @param targetPhoneIPAddress
     */
    public synchronized void setTargetPhoneIPAddress(String targetPhoneIPAddress) {
        Timber.i("Target Phone IP Address: %s ", targetPhoneIPAddress);
        mTargetPhoneIPAddress = targetPhoneIPAddress;
    }

    /**
     * Send synchronise request message
     */
    public synchronized void sendSyncReqMessage() {

        if (mTargetPhoneIPAddress != null && mPoller != null) {
            Socket socket = null;

            for (int i = 0; i < mPoller.getSize(); i++) {

                if (mPoller.getItem(i) != null) {

                    socket = mPoller.getItem(i).getSocket();

                    Timber.i("Poller list, Poller item (%d) last endpoint: %s",
                            i, socket.getLastEndpoint());

                    String endpoint = "tcp://";
                    endpoint = endpoint.concat(mTargetPhoneIPAddress).concat(StringUtil.COLON);
                    endpoint = endpoint.concat(String.valueOf(PAIR_PORT));

                    if (endpoint.equalsIgnoreCase(socket.getLastEndpoint())) {
                        socket.setMaxMsgSize(-1);
                        socket.setMsgAllocationHeapThreshold(0);

                        socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                        socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                        socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                        socket.setLinger(0);
                        socket.setRcvHWM(0);
                        socket.setSndHWM(0);
                        socket.setImmediate(true);
                        socket.setTCPKeepAlive(0);
                        socket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                        socket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                        socket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                        socket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                        socket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);

                        break;
                    }
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
    protected synchronized void startProcess() {

        if (mTargetPhoneIPAddress != null) {
            boolean isTargetEndpointFound = false;

            String endpoint = "tcp://";
            endpoint = endpoint.concat(mTargetPhoneIPAddress).concat(StringUtil.COLON);
            endpoint = endpoint.concat(String.valueOf(PAIR_PORT));

            Timber.i("Endpoint %s ", endpoint);

            for (int i = 0; i < mPoller.getSize(); i++) {

                if (mPoller.getItem(i) != null && endpoint.
                        equalsIgnoreCase(mPoller.getSocket(i).getLastEndpoint())) {
                    isTargetEndpointFound = true;
                }
            }

            if (!isTargetEndpointFound) {
                Timber.i("Start new client pair socket connection");

                Timber.i("Creating and connecting client pair socket...");

                PowerManagerUtil.acquirePartialWakeLock();

                Socket socket = mZContext.createSocket(SocketType.PAIR);
                socket.setMaxMsgSize(-1);
                socket.setMsgAllocationHeapThreshold(0);

                socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                socket.setLinger(0);
                socket.setRcvHWM(0);
                socket.setSndHWM(0);
                socket.setImmediate(true);
                socket.setTCPKeepAlive(0);
                socket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                socket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                socket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                socket.setSendTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                socket.setReceiveTimeOut(SOCKET_TIMEOUT_IN_MILLISEC);
                socket.connect(endpoint);

                Timber.i("Client pair socket connected");

                if (mJeroMQClientPairRunnable == null) {
                    Timber.i("Creating new client pair runnable...");
                    mPoller.register(socket, ZMQ.Poller.POLLIN);
                    mJeroMQClientPairRunnable = new JeroMQClientPairRunnable(mZContext,
                            mPoller, JeroMQClientPair.this);

                    mCustomThreadPoolManager.addRunnable(mJeroMQClientPairRunnable);

                } else {
                    Timber.i("Client pair runnable exists, adding socket to poller...");
                    mJeroMQClientPairRunnable.addPollerItem(socket);

                }

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
        if (mZContext != null && mPoller != null) {
//            for (int i = 0; i < mPoller.getSize(); i++) {
//                Socket socket = mPoller.getItem(i).getSocket();
//                String socketEndpoint = socket.getLastEndpoint();
//
//                if (socketEndpoint != null) {
//                    socket.disconnect(socketEndpoint);
//                    Timber.i("Client pair socket disconnected %d", i);
//
//                    socket.close();
//                    Timber.i("Client pair socket closed %d", i);
//                }
//
//                mZContext.destroySocket(socket);
//                Timber.i("Client pair socket destroyed %d", i);
//            }
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Timber.i("Destroying ZContext.");

            mZContext.destroy();

            Timber.i("ZContext destroyed.");
        }
    }
}
