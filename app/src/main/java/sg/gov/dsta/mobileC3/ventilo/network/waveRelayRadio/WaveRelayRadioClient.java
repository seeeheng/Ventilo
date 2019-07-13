package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.persistentsystems.socketclient.exceptions.WrSocketNotReadyException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.util.constant.USBConnectionConstants;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;

public class WaveRelayRadioClient {

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();
    private static final int CHECK_RADIO_CONNECTION_INTERVAL_IN_MILLISEC = 1000;

    // Determines how many interval before indication of disconnection
    private static final int PHONE_TO_RADIO_HEARTBEAT_THRESHOLD = 3;
//    private static final String OWN_RADIO_IP = "198.18.5.6";

    private static WaveRelayRadioSocketClient mWrWebSocketClient;
    private Timer mCheckRadioConnectionTimer;
    private int mLastIncomingMsgCount;
    private int mSameIncomingMsgCount;
//    private int mCountBeforeReconnection;

    public WaveRelayRadioClient(String ownRadioIpAddress) {
        Log.i(TAG, "Own Radio IP Address: " + ownRadioIpAddress);
        String socketUrl = "wss://" + ownRadioIpAddress + ":443/xxx";
        establishConnection(socketUrl);
    }

    /**
     * Establish socket connection between client and server
     */
    private void establishConnection(String socketUrl) {

//        ConnectivityManager cm = (ConnectivityManager) MainApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    boolean isRndisTetheringActive = NetworkUtil.
                            isRndisTetheringActive(MainApplication.getAppContext());

                    if ((mWrWebSocketClient == null || (!mWrWebSocketClient.isOpen())) &&
                            isRndisTetheringActive) {

                        mWrWebSocketClient = new WaveRelayRadioSocketClient(new URI(socketUrl));

                        mWrWebSocketClient.setSocketUsername("factory");
                        mWrWebSocketClient.setSocketPassword("password");

                        /**
                         * mWrWebSocketClient.connectBlocking() is a blocking connection attempt.
                         * Since this is not a threaded program, not concerned.
                         * mWrWebSocketClient.connect() is non-blocking.
                         */
                        mWrWebSocketClient.connectBlocking();

                        mSameIncomingMsgCount = 0;
                    }

//                    // Increments mSameIncomingMsgCount each time client's last incoming msg count and
//                    // socket client's incoming msg count matches (indicating signs of disconnection)
//                    //
//                    // Rationale: Socket is receiving onMessage data every second, while this TimerTask
//                    // is also running every second. Hence if the msg count in socket client class does
//                    // not increment (meaning no incoming messages), the last recorded msg count will
//                    // be a match. Once the seld-declared threshold is reached, it can be assumed that it
//                    // is disconnected from the network.
//                    if (mWrWebSocketClient.getIncomingMsgCount() != 0) {
//                        if (mLastIncomingMsgCount == mWrWebSocketClient.getIncomingMsgCount()) {
//                            mSameIncomingMsgCount++;
//                        } else {
//                            mLastIncomingMsgCount = mWrWebSocketClient.getIncomingMsgCount();
//                        }
//                    }

//                    if (mSameIncomingMsgCount >= PHONE_TO_RADIO_HEARTBEAT_THRESHOLD || !isUsbConnected()) {
//                        mWrWebSocketClient.close();
//                    }

                    if (mWrWebSocketClient != null && !isUsbConnected()) {
                        mWrWebSocketClient.close();
                        mWrWebSocketClient.updateAndBroadcastRadioConnectionStatus(false);
                    }

                    Log.d(TAG, "mWrWebSocketClient.getIncomingMsgCount() is " + mWrWebSocketClient.getIncomingMsgCount());
                    Log.d(TAG, "mLastIncomingMsgCount is " + mLastIncomingMsgCount);
                    Log.d(TAG, "mSameIncomingMsgCount is " + mSameIncomingMsgCount);

//                    mWrWebSocketClient.getConnection().getReadyState()
                    Log.d(TAG, "mWrWebSocketClient.getConnection().getReadyState(): " + mWrWebSocketClient.getConnection().getReadyState());

                    Log.d(TAG, "mWrWebSocketClient.getConnection().isOpen(): " + mWrWebSocketClient.getConnection().isOpen());
                    Log.d(TAG, "mWrWebSocketClient.getConnection().isFlushAndClose(): " + mWrWebSocketClient.getConnection().isFlushAndClose());
                    Log.d(TAG, "mWrWebSocketClient.getConnection().isConnecting(): " + mWrWebSocketClient.getConnection().isConnecting());

//                    if ((mWrWebSocketClient != null && mWrWebSocketClient.getConnection().isClosed())) {
//                        System.out.println("mWrWebSocketClient.isClosed");
//                        mWrWebSocketClient.updateAndBroadcastRadioConnectionStatus(false);
//                    }

                    /**
                     * Actual connection and sending of data takes place here.
                     * mWrWebSocketClient.get(String var) returns a string of what was sent
                     * to the socket server (NOT THE RESPONSE FROM THE SERVER)
                     */
                    Log.i(TAG, "********************");

//                    String[] vars = {"waverelay_name", "waverelay_ip", "ip_flow_list"};
                    String[] vars = {"waverelay_neighbors_json"};


                    Log.i(TAG, "mWrWebSocketClient packets received: " + mWrWebSocketClient.get(vars));
                    Log.i(TAG, "********************");

                } catch (InterruptedException e) {
                    Log.i(TAG, "wrWebSocketClient InterruptedException: " + e);
//            Toast.makeText(MainApplication.getAppContext(), "wrWebSocketClient InterruptedException: " + e,
//                    Toast.LENGTH_LONG).show();
                } catch (WrSocketNotReadyException e) {
                    Log.i(TAG, "wrWebSocketClient WrSocketNotReadyException: " + e);
                } catch (IOException e) {
                    Log.i(TAG, "wrWebSocketClient IOException: " + e);
//            Toast.makeText(MainApplication.getAppContext(), "wrWebSocketClient IOException: " + e,
//                    Toast.LENGTH_LONG).show();
                } catch (URISyntaxException e) {
                    Log.i(TAG, "wrWebSocketClient URISyntaxException: " + e);
//            Toast.makeText(MainApplication.getAppContext(), "wrWebSocketClient URISyntaxException: " + e,
//                    Toast.LENGTH_LONG).show();
                }
            }
        };

        mCheckRadioConnectionTimer = new Timer();
        long delay = 0;

        // Schedules task to be run in an interval
        mCheckRadioConnectionTimer.scheduleAtFixedRate(task, delay,
                CHECK_RADIO_CONNECTION_INTERVAL_IN_MILLISEC);


    }

    public void closeWebSocketClient() {
        if (mCheckRadioConnectionTimer != null) {
            mCheckRadioConnectionTimer.cancel();
        }

        if (mWrWebSocketClient != null && !mWrWebSocketClient.isClosed()) {
            mWrWebSocketClient.close();
            mWrWebSocketClient.updateAndBroadcastRadioConnectionStatus(false);
        }
    }

//    public WaveRelayRadioSocketClient getWrWebSocketClient() {
//        if (mWrWebSocketClient != null) {
//            return mWrWebSocketClient;
//        }
//        return null;
//    }

    private boolean isUsbConnected() {
        boolean isUsbConnected = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(USBConnectionConstants.ACTION_POWER_CONNECTED);
        filter.addAction(USBConnectionConstants.ACTION_POWER_DISCONNECTED);
        filter.addAction(USBConnectionConstants.USB_STATE);

        Intent intent = MainApplication.getAppContext().
                registerReceiver(null, filter);

        String action = intent.getAction();
        Log.i(TAG, "action: " + action);

        if (action != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (action.equals(USBConnectionConstants.USB_STATE)) {
                    if (intent.getExtras() != null &&
                            intent.getExtras().getBoolean("connected")) {
                        isUsbConnected = true;
                    } else {
                        isUsbConnected = false;
                    }
                }
            } else {
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    isUsbConnected = true;
                } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    isUsbConnected = false;
                }
            }
        }

        return isUsbConnected;
    }
}
