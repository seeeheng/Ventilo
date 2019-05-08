package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.os.AsyncTask;
import android.util.Log;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZThread;

import java.util.concurrent.Executors;

import sg.gov.dsta.mobileC3.ventilo.util.PowerManagerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;

public class JeroMQPubSubBrokerProxy extends JeroMQBrokerProxy {

    private static final String TAG = JeroMQPubSubBrokerProxy.class.getSimpleName();

    protected static final int DEFAULT_XSUB_PORT = 5557;
    protected static final int DEFAULT_XPUB_PORT = 5558;

//    private static final String SERVER_XSUB_IP_ADDRESS = "tcp://*:" + DEFAULT_XSUB_PORT;

    protected static final String SERVER_XSUB_IP_ADDRESS = "tcp://" +
            NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true) + ":" +
            JeroMQPubSubBrokerProxy.DEFAULT_XSUB_PORT;

    private static final String SERVER_XPUB_IP_ADDRESS = "tcp://*:" + DEFAULT_XPUB_PORT;

    private static volatile JeroMQPubSubBrokerProxy instance;

    private ZContext mZContext;
    private Socket mXSubSocket;
    private Socket mXPubSocket;

//    private Thread mProxyThread;

    /**
     * Fixed number of threads dependant on the number of device IP addresses available
     */
    private JeroMQPubSubBrokerProxy() {
//        super(zContext, Executors.newFixedThreadPool(ADDRESSES.size()));
        super(Executors.newSingleThreadExecutor());
    }

    /**
     * Create/get singleton class instance
     *
     * @return
     */
    public static JeroMQPubSubBrokerProxy getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (JeroMQPublisher.class) {
                if (instance == null) {
                    instance = new JeroMQPubSubBrokerProxy();
                }
            }
        }

        return instance;
    }

    /**
     * Creates a single thread for each IP address for server processing of messages from different topics
     * Processes messages based on topics
     */
    @Override
    protected void startServerProcess() {
        Log.i(TAG, "Start proxy socket connection");

        mZContext = new ZContext();

//        new mProxyThread(shadowContext).run
//        ZContext shadowContext = ZContext.shadow(context);
//        new ProxyThread(shadowContext).run();

//        mSocket = mZContext.createSocket(SocketType.PUB);
//        mSocket.setLinger(-1);
////        .setReuseAddress(true);
////        mSocket.setLinger(   0 );
////        mSocket.setSndHWM(   0 );
////        mSocket.bind(ALL_IP_ADDRESS);
//        mSocket.bind(SERVER_IP_ADDRESS);
//        Log.i(TAG, "Sockets connected");

        PowerManagerUtil.acquirePartialWakeLock();

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                ZThread.fork(mZContext, new JeroMQPubZThreadRunnable());
                ZThread.fork(mZContext, new JeroMQSubZThreadRunnable());

                ZContext shadowContext = ZContext.shadow(mZContext);
                new ProxyAsyncTask(shadowContext).run();


//                // Frontend
//                mXSubSocket = mZContext.createSocket(SocketType.XSUB);
//
//                // Backend
//                mXPubSocket = mZContext.createSocket(SocketType.XPUB);
//
////                mXSubSocket.setLinger(-1);
////                mXPubSocket.setLinger(-1);
//
//
//                System.out.println("SERVER RUNNING 0");
//
//                mXSubSocket.bind(SERVER_XSUB_IP_ADDRESS);
//                mXPubSocket.bind(SERVER_XPUB_IP_ADDRESS);
//
////                mXSubSocket.subscribe("");
//
//                System.out.println("SERVER RUNNING 1");
//
//                JeroMQPublisher.getInstance().start();
//                JeroMQSubscriber.getInstance().start();

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

//                mXSubSocket.subscribe(ZMQ.SUBSCRIPTION_ALL);
//                ZMQ.proxy(mXSubSocket, mXPubSocket, null);

//                System.out.println("SERVER RUNNING 2");

                //  Initialize poll set
//                ZMQ.Poller items = mZContext.createPoller(2);
//                items.register(mXSubSocket, ZMQ.Poller.POLLIN);
//                items.register(mXPubSocket, ZMQ.Poller.POLLIN);

//                System.out.println("SERVER RUNNING 3");
//
//                boolean more = false;
//                byte[] message;
//
//                System.out.println("SERVER RUNNING 4");
//                //  Switch messages between sockets
//                while (!Thread.currentThread().isInterrupted()) {
//                    System.out.println("test 1");
//                    //  poll and memorize multipart detection
//                    items.poll();
//
//                    System.out.println("test 2");
//
//                    if (items.pollin(0)) {
//                        while (true) {
//                            System.out.println("outflow from mXPubSocket is " + more);
//                            // receive message
//                            message = mXSubSocket.recv(0);
//                            more = mXSubSocket.hasReceiveMore();
//
//                            // Broker it
//                            mXPubSocket.send(message, more ? ZMQ.SNDMORE : 0);
//                            if (!more) {
//                                break;
//                            }
//
//
//                        }
//                    }
//
//                    if (items.pollin(1)) {
//                        while (true) {
//                            System.out.println("outflow from mXSubSocket is " + more);
//                            // receive message
//                            message = mXPubSocket.recv(0);
//                            more = mXPubSocket.hasReceiveMore();
//                            // Broker it
//                            mXSubSocket.send(message, more ? ZMQ.SNDMORE : 0);
//                            if (!more) {
//                                break;
//                            }
//
//
//                        }
//                    }
//                }


                //  Store last instance of each topic in a cache
//                Map<String, String> cache = new HashMap<String, String>();
//
//                ZMQ.Poller poller = mZContext.createPoller(2);
//                poller.register(mXSubSocket, ZMQ.Poller.POLLIN);
//                poller.register(mXPubSocket, ZMQ.Poller.POLLIN);
//
//                System.out.println("SERVER RUNNING 3");
//
//                //  We route topic updates from frontend to backend, and we handle
//                //  subscriptions by sending whatever we cached, if anything:
//                while (true) {
//                    if (poller.poll(1000) == -1)
//                        break; //  Interrupted
//
//                    //  Any new topic data we cache and then forward
//                    if (poller.pollin(0)) {
//                        String topic = mXSubSocket.recvStr();
//                        String current = mXSubSocket.recvStr();
//
//                        if (topic == null)
//                            break;
//                        cache.put(topic, current);
//                        mXPubSocket.sendMore(topic);
//                        mXPubSocket.send(current);
//                    }
//                    //  When we get a new subscription, we pull data from the cache:
//                    if (poller.pollin(1)) {
//                        ZFrame frame = ZFrame.recvFrame(mXPubSocket);
//                        if (frame == null)
//                            break;
//                        //  Event is one byte 0=unsub or 1=sub, followed by topic
//                        byte[] event = frame.getData();
//                        if (event[0] == 1) {
//                            String topic = new String(event, 1, event.length - 1, ZMQ.CHARSET);
//                            System.out.printf("Sending cached topic %s\n", topic);
//                            String previous = cache.get(topic);
//                            if (previous != null) {
//                                mXPubSocket.sendMore(topic);
//                                mXPubSocket.send(previous);
//                            }
//                        }
//                        frame.destroy();
//                    }
//                }
            }
        });

        PowerManagerUtil.releasePartialWakeLock();
    }

    /**
     * Disconnect sockets when thread is closed
     */
    @Override
    public void stop() {
        super.stop();

//        CloseSocketAsyncTask task = new CloseSocketAsyncTask(mZContext);
//        task.execute();

        if (mZContext != null && !mZContext.isClosed()) {
            Log.i(TAG, "Destroying ZContext..");
            mZContext.destroy();
            Log.i(TAG, "ZContext destroyed.");
        }
    }

    private class ProxyAsyncTask extends Thread {
        private ZContext mProxyZContext;

        private ProxyAsyncTask(ZContext zContext) {
            mProxyZContext = zContext;
        }

        @Override
        public void run() {
            // Frontend
            mXSubSocket = mZContext.createSocket(SocketType.XSUB);

            // Backend
            mXPubSocket = mZContext.createSocket(SocketType.XPUB);
            mXSubSocket.setLinger(0);
            mXPubSocket.setLinger(0);

            Log.i(TAG, "Proxy sockets created.");

//            mXSubSocket.bind(SERVER_XSUB_IP_ADDRESS);
            mXSubSocket.connect(SERVER_XSUB_IP_ADDRESS);
            mXPubSocket.bind(SERVER_XPUB_IP_ADDRESS);

            Log.i(TAG, "Proxy sockets connected/binded.");

            ZMQ.proxy(mXSubSocket, mXPubSocket, null);
            Log.i(TAG, "Proxy disconnected.");

            mProxyZContext.destroy();
            Log.i(TAG, "Proxy context destroyed.");
        }
    }
}
