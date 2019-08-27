package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.persistentsystems.socketclient.exceptions.WrSocketNotReadyException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.USBConnectionConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;;

public class WaveRelayRadioClient implements WaveRelayRadioJobService.WaveRelayRadioConnectionListener {

    // Intent Filters
    public static final String WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Connected";
    public static final String WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Disconnected";

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();
    private static final int MISSING_HEARTBEAT_CONNECTION_THRESHOLD = 3;

    // Determines how many interval before indication of disconnection
//    private static final int PHONE_TO_RADIO_HEARTBEAT_THRESHOLD = 3;
//    private static final String OWN_RADIO_IP = "198.18.5.6";

    private WaveRelayRadioJobService mWaveRelayRadioJobService;
    private static WaveRelayRadioSocketClient mWrWebSocketClient;
//    private Timer mCheckRadioConnectionTimer;
//    private static int mLastIncomingMsgCount;
//    private static int mSameIncomingMsgCount;
//    private int mCountBeforeReconnection;
    private int mUsbConnectionLostCount;
    private String mSocketUrl;

    public WaveRelayRadioClient(String ownRadioIpAddress) {
        Log.i(TAG, "OWN Radio IP Address: " + ownRadioIpAddress);
        String socketUrl = "wss://" + ownRadioIpAddress + ":443/xxx";
        establishConnection(socketUrl);
    }

    /**
     * Establish socket connection between client and server
     */
    private void establishConnection(String socketUrl) {
        mSocketUrl = socketUrl;

        WaveRelayRadioJobService.getInstance().setConnectionListener(this);
        WaveRelayRadioJobService.getInstance().scheduleRefresh();
    }

    @Override
    public void runCheckConnection() {

        Runnable runCheckConnectionRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    boolean isRndisTetheringActive = NetworkUtil.
                            isRndisTetheringActive(MainApplication.getAppContext());

                    Timber.i("Running Wave Relay Connection Check...");

                    if ((mWrWebSocketClient == null || !mWrWebSocketClient.isOpen()) &&
                            isUsbConnected() && isRndisTetheringActive && mSocketUrl != null) {

//                        if (mWrWebSocketClient != null && mUsbConnectionLostCount >= 3) {
                        if (mWrWebSocketClient != null) {
//                            mWrWebSocketClient.updateConnectionIfNeeded(ERadioConnectionStatus.OFFLINE.toString());
//                    mWrWebSocketClient.close();
                            mWrWebSocketClient = null;
                        }

                        Timber.i("Connecting new Wave Relay Radio socket client...");
                        mWrWebSocketClient = new WaveRelayRadioSocketClient(new URI(mSocketUrl));

                        mWrWebSocketClient.setSocketUsername("factory");
                        mWrWebSocketClient.setSocketPassword("password");

                        /**
                         * mWrWebSocketClient.connectBlocking() is a blocking connection attempt.
                         * Since this is not a threaded program, not concerned.
                         * mWrWebSocketClient.connect() is non-blocking.
                         */
                        mWrWebSocketClient.connectBlocking();

//                        mIsUsbDisconnected = false;
//                        mUsbConnectionLostCount = 0;
//                        mSameIncomingMsgCount = 0;
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

                    if (mWrWebSocketClient != null && mWrWebSocketClient.isOpen()) {
                        Log.d(TAG, "mWrWebSocketClient.getIncomingMsgCount() is " + mWrWebSocketClient.getIncomingMsgCount());
                        Log.d(TAG, "mWrWebSocketClient.getConnection().getReadyState(): " + mWrWebSocketClient.getConnection().getReadyState());

                        if (!isUsbConnected()) {
                            mUsbConnectionLostCount++;
                        } else {
                            mUsbConnectionLostCount = 0;

                            //                        Log.d(TAG, "mWrWebSocketClient.getConnection().isOpen(): " + mWrWebSocketClient.getConnection().isOpen());
//                        Log.d(TAG, "mWrWebSocketClient.getConnection().isFlushAndClose(): " + mWrWebSocketClient.getConnection().isFlushAndClose());
//                        Log.d(TAG, "mWrWebSocketClient.getConnection().isConnecting(): " + mWrWebSocketClient.getConnection().isConnecting());

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
                        }

                        if (mWrWebSocketClient != null && mUsbConnectionLostCount >= 2) {
                            mWrWebSocketClient.updateConnectionIfNeeded(ERadioConnectionStatus.OFFLINE.toString());
                        }

                        if (mUsbConnectionLostCount >= 3) {
                            mWrWebSocketClient.close();
                            mWrWebSocketClient = null;
                        }
                    }

//                    // Broadcast current user's disconnection to other devices while disconnected from radio
//                    if (mWrWebSocketClient != null && !mWrWebSocketClient.isOpen()) {
//                        mWrWebSocketClient.updateConnectionIfNeeded(ERadioConnectionStatus.OFFLINE.toString());
//                    }

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

                checkMissingHeartBeatOfOtherUsersUserModels();
                checkMissingHeartBeatOfOtherUsersBFTModels();

            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runCheckConnectionRunnable);
    }

//    public static synchronized void closeWebSocketClientAfterNotification() {
//        if (mWrWebSocketClient != null && mWrWebSocketClient.isOpen()) {
//            mWrWebSocketClient.close();
//            mWrWebSocketClient = null;
//        }
//    }

//    protected static boolean isUsbDisconnected() {
//        return mIsUsbDisconnected;
//    }

    /**
     * Closes Wave Relay web socket properly
     */
    public static synchronized void closeWebSocketClient() {
        Timber.i("Closing Wave Relay Web Socket Client...");

//        if (mCheckRadioConnectionTimer != null) {
//            mCheckRadioConnectionTimer.cancel();
//        }

        if (mWrWebSocketClient != null && !mWrWebSocketClient.isClosed()) {
//            mWrWebSocketClient.close();
            mWrWebSocketClient.updateConnectionIfNeeded(ERadioConnectionStatus.OFFLINE.toString());
        }

        stopWaveRelayRadioJobService();
    }

    private static synchronized void stopWaveRelayRadioJobService() {
        if (WaveRelayRadioJobService.isSchedulerRunning()) {
            Timber.i("Stopping Wave Relay Radio Job Service...");

            WaveRelayRadioJobService.stopScheduler();
//            Intent WaveRelayRadioJobServiceIntent = new Intent(MainApplication.getAppContext(),
//                    WaveRelayRadioJobService.class);
//            MainApplication.getAppContext().stopService(WaveRelayRadioJobServiceIntent);
        }
    }

    /**
     * Checks if cable is connected through power consumption and USB state
     *
     * @return
     */
    protected static boolean isUsbConnected() {
        boolean isUsbConnected = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(USBConnectionConstants.ACTION_POWER_CONNECTED);
        filter.addAction(USBConnectionConstants.ACTION_POWER_DISCONNECTED);
        filter.addAction(USBConnectionConstants.USB_STATE);

        Intent intent = MainApplication.getAppContext().
                registerReceiver(null, filter);

        if (intent != null) {
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
        }

        return isUsbConnected;
    }

    /**
     * Update wave relay radio detail of user id to other devices
     */
    private synchronized void updateWaveRelayRadioDetailsOfUser(String userId, boolean isConnected) {
        WaveRelayRadioRepository waveRelayRadioRepository = new WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadio = new SingleObserver<WaveRelayRadioModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                if (waveRelayRadioModel != null) {
                    Log.d(TAG, "onSuccess singleObserverWaveRelayRadio, " +
                            "updateWaveRelayRadioDetailsOfUser. " +
                            "UserId: " + waveRelayRadioModel.getUserId());

                    if (!SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(waveRelayRadioModel.getUserId()) && !isConnected) {

                        waveRelayRadioModel.setUserId(null);
                        waveRelayRadioModel.setPhoneIpAddress(StringUtil.INVALID_STRING);
                        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverWaveRelayRadio, " +
                        "updateWaveRelayRadioDetailsOfUser. " +
                        "Error Msg: " + e.toString());
            }
        };

        waveRelayRadioRepository.queryRadioByUserId(userId, singleObserverWaveRelayRadio);
    }

    /**
     * Checks the missing heart beat of other users' User models and update status
     */
    private synchronized void checkMissingHeartBeatOfOtherUsersUserModels() {
        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<UserModel>> singleObserverGetAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                Log.d(TAG, "onSuccess singleObserverGetAllUsers, " +
                        "checkMissingHeartBeatOfOtherUsersUserModels. " +
                        "userModelList size: " + userModelList.size());

                // Obtain all User models of other users
                List<UserModel> otherUsersUserModelList = userModelList.stream().
                        filter(userModel -> !SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(userModel.getUserId())).collect(Collectors.toList());

                // Increment missing heart beat count by 1
                // Their heart beat counts will be updated to 0 by their respective devices when
                // they broadcast their own connection with heartbeat as 0.
                // (if it reaches 3 or above, it is considered stale data)
                for (int i = 0; i < otherUsersUserModelList.size(); i++) {
                    UserModel userModel = otherUsersUserModelList.get(i);

                    Log.d(TAG, "userModel.getUserId(): " + userModel.getUserId());

                    // Check heart beat only if user is online (when heart beat is not invalid)
                    if (userModel.getMissingHeartBeatCount() != Integer.valueOf(StringUtil.INVALID_STRING)) {

                        userModel.setMissingHeartBeatCount(userModel.getMissingHeartBeatCount() + 1);

                        if (userModel.getMissingHeartBeatCount() >= MISSING_HEARTBEAT_CONNECTION_THRESHOLD) {

                            // Update last connected time only if user was previously online
                            if (!ERadioConnectionStatus.OFFLINE.toString().
                                    equalsIgnoreCase(userModel.getRadioFullConnectionStatus())) {
                                userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
                            }

                            userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                            userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                            userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                            userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

                            updateWaveRelayRadioDetailsOfUser(userModel.getUserId(), false);

                        }

                    } else {
                        userModel.setMissingHeartBeatCount(0);

                    }

                    userRepository.updateUser(userModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllUsers, " +
                        "checkMissingHeartBeatOfOtherUsersUserModels. " +
                        "Error Msg: " + e.toString());
            }
        };

        userRepository.getAllUsers(singleObserverGetAllUsers);
    }

    /**
     * Checks the missing heart beat of other users' BFT models and update status
     */
    private synchronized void checkMissingHeartBeatOfOtherUsersBFTModels() {
        BFTRepository bftRepository = new BFTRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<BFTModel>> singleObserverGetAllBFTs = new SingleObserver<List<BFTModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<BFTModel> bftModelList) {
                Log.d(TAG, "onSuccess singleObserverGetAllBFTs, " +
                        "checkMissingHeartBeatOfOtherUsersBFTModels. " +
                        "bftModelList size: " + bftModelList.size());

                // Obtain all bft models of other users
                List<BFTModel> otherUsersBFTModelList = bftModelList.stream().
                        filter(bftModel -> !SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());

                // Increment missing heart beat count by 1
                // Their heart beat counts will be updated to 0 by their respective devices when
                // they broadcast their own bft models with heartbeat as 0.
                // (if it reaches 3 or above, it is considered stale data)
                for (int i = 0; i < otherUsersBFTModelList.size(); i++) {
                    BFTModel bftModel = otherUsersBFTModelList.get(i);

                    bftModel.setMissingHeartBeatCount(bftModel.getMissingHeartBeatCount() + 1);

                    if (bftModel.getMissingHeartBeatCount() >= MISSING_HEARTBEAT_CONNECTION_THRESHOLD) {

                        // Check if bft model is already stale by checking if
                        // 'Type' contains the word 'stale'
                        if (!bftModel.getType().contains(MainApplication.getAppContext().
                                getString(R.string.map_blueprint_type_stale))) {

                            String staleType = bftModel.getType().concat(StringUtil.HYPHEN).
                                    concat(MainApplication.getAppContext().
                                            getString(R.string.map_blueprint_type_stale));
                            bftModel.setType(staleType);
                        }
                    }

                    bftRepository.updateBFT(bftModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllBFTs, " +
                        "checkMissingHeartBeatOfOtherUsersBFTModels. " +
                        "Error Msg: " + e.toString());
            }
        };

        bftRepository.getAllBFTs(singleObserverGetAllBFTs);
    }
}
