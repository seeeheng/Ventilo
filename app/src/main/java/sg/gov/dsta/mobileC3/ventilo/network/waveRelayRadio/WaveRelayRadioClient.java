package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.app.Application;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.persistentsystems.socketclient.exceptions.WrSocketNotReadyException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.login.TestActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.ERadioConnectionStatus;

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

    public WaveRelayRadioClient(String ownRadioIpAddress, UsbManager cm) {
        Log.i(TAG, "Own Radio IP Address: " + ownRadioIpAddress);
        String socketUrl = "wss://" + ownRadioIpAddress + ":443/xxx";
        establishConnection(socketUrl, cm);
    }

    /**
     * Establish socket connection between client and server
     */
    private void establishConnection(String socketUrl, UsbManager cm) {

//        ConnectivityManager cm = (ConnectivityManager) MainApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {

//                    List<UsbSerialDriver> availableDrivers =  UsbSerialProber.getDefaultProber().findAllDrivers(manager);

                    if (cm.getAccessoryList() != null) {
                        for (int i = 0; i < cm.getAccessoryList().length; i++) {
                            Log.d(TAG, "cm.getAccessoryList()[" + i + "]: " + cm.getAccessoryList()[i]);

//                        NetworkInfo activeNetwork = cm.getDeviceList().get();
                            Log.d(TAG, "cm.getDeviceList()[" + i + "]: " + cm.getDeviceList().get(cm.getAccessoryList()[i]));
                        }
                    }

//                    NetworkInfo activeNetwork = cm.getDeviceList().get();
//                    boolean isConnected = activeNetwork != null &&
//                            activeNetwork.isConnected();
//
//                    Log.d(TAG, "activeNetwork isConnected: " + isConnected);
//                    Log.d(TAG, "activeNetwork activeNetwork.getExtraInfo(): " + activeNetwork.getExtraInfo());



                    if (mWrWebSocketClient == null || (mWrWebSocketClient != null &&
                            !mWrWebSocketClient.isOpen() || (mWrWebSocketClient != null &&
                            mSameIncomingMsgCount >= PHONE_TO_RADIO_HEARTBEAT_THRESHOLD))) {

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

                    // Increments mSameIncomingMsgCount each time client's last incoming msg count and
                    // socket client's incoming msg count matches (indicating signs of disconnection)
                    if (mWrWebSocketClient.getIncomingMsgCount() != 0) {
                        if (mLastIncomingMsgCount == mWrWebSocketClient.getIncomingMsgCount()) {
                            mSameIncomingMsgCount++;
                        } else {
                            mLastIncomingMsgCount = mWrWebSocketClient.getIncomingMsgCount();
                        }
                    }

                    if (mSameIncomingMsgCount >= PHONE_TO_RADIO_HEARTBEAT_THRESHOLD) {
                        mWrWebSocketClient.close();
                    }

                    Log.d(TAG, "mWrWebSocketClient.getIncomingMsgCount() is " + mWrWebSocketClient.getIncomingMsgCount());
                    Log.d(TAG, "mLastIncomingMsgCount is " + mLastIncomingMsgCount);
                    Log.d(TAG, "mSameIncomingMsgCount is " + mSameIncomingMsgCount);


//                    mWrWebSocketClient.getConnection().getReadyState()
                    Log.d(TAG, "mWrWebSocketClient.getConnection().getReadyState(): " + mWrWebSocketClient.getConnection().getReadyState());

                    Log.d(TAG, "mWrWebSocketClient.getConnection().isOpen(): " + mWrWebSocketClient.getConnection().isOpen());
                    Log.d(TAG, "mWrWebSocketClient.getConnection().isFlushAndClose(): " + mWrWebSocketClient.getConnection().isFlushAndClose());
                    Log.d(TAG, "mWrWebSocketClient.getConnection().isConnecting(): " + mWrWebSocketClient.getConnection().isConnecting());

//                    if (mWrWebSocketClient != null && mWrWebSocketClient.getConnection().isClosed()) {
////                        System.out.println("mWrWebSocketClient.isClosed");
////                        mWrWebSocketClient.updateAndBroadcastRadioConnectionStatus(false);
////                    }

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
        }
    }

    public WaveRelayRadioSocketClient getWrWebSocketClient() {
        if (mWrWebSocketClient != null) {
            return mWrWebSocketClient;
        }
        return null;
    }
}
