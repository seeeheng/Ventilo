package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.persistentsystems.socketclient.WrWebSocketClient;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQClientPair;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import timber.log.Timber;

public class WaveRelayRadioSocketClient extends WrWebSocketClient {

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();
    // Intent Filters
    public static final String WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Connected";
    public static final String WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Disconnected";
    public static final int MISSING_HEARTBEAT_CONNECTION_THRESHOLD = 3;
    private static final int SYNC_DATA_INTERVAL = 3;
//    private static final int INCOMING_MSG_INTERVAL = 3;

//    private int mIncomingMsgCount = 0;
    private static int mSyncDataCount = 3;

    /**
     * Basic constructor. See (@link com.persistentsystems.socketclient.WrWebSocketClient#WrWebSocketClient(URI, Draft))
     *
     * @param serverUri - See (@link com.persistentsystems.socketclient.WrWebSocketClient#WrWebSocketClient(URI, Draft))
     * @throws IOException - Throws due to reading input streams
     */
    protected WaveRelayRadioSocketClient(URI serverUri) throws IOException {
        super(serverUri);
//        Toast.makeText(MainApplication.getAppContext(), "Class initialised",
//                Toast.LENGTH_LONG).show();
        Log.i(TAG, "Class initialised");
    }

    /**
     * Simple override
     *
     * @param handshakeData - Passed in from parent. Contains information about the connection
     */
    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.i(TAG, "Socket opened");
        Toast.makeText(MainApplication.getAppContext(), "Socket opened",
                Toast.LENGTH_LONG).show();
    }

    /**
     * Simple override and printing out of incoming messages
     *
     * @param message - A pure string representation of the message received by this websocket clients
     */
    @Override
    public void onMessage(String message) {
//        mIncomingMsgCount++;
        Log.i(TAG, "Received: " + message + System.lineSeparator());

//        if (mIncomingMsgCount >= INCOMING_MSG_INTERVAL) {
//            mIncomingMsgCount = 0;
//        Toast.makeText(MainApplication.getAppContext(), "Received: " + message + System.lineSeparator(),
//                Toast.LENGTH_LONG).show();
//        Gson gson = GsonCreator.createGson();
//        String newSitRepModelJson = gson.toJson(sitRepModel);

        String connectionStatus = ERadioConnectionStatus.OFFLINE.toString();
        Map<String, String> neighbourRadioIPAddressToSnrMap = new HashMap<>();
//        List<String> neighbourRadioIPAddressList = new ArrayList<>();
//        List<String> signalToNoiseRatioList = new ArrayList<>();
        JSONTokener tokener = new JSONTokener(message);

        try {
            JSONObject jso = (JSONObject) tokener.nextValue();
//            Log.i(TAG, "command: " + jso.get("command"));

            Log.i(TAG, "==================== Msg Type ====================");
            Log.i(TAG, "msgtype: " + jso.get("msgtype"));
//            stringBuilder.append("token: " + jso.get("token"));
//            stringBuilder.append("protocol_version: " + jso.get("protocol_version"));
//            Toast.makeText(MainApplication.getAppContext(), "token: " + jso.get("token"),
//                    Toast.LENGTH_LONG).show();

            Log.i(TAG, "==================== UNIT ID ====================");
            JSONObject unitId = (JSONObject) jso.get("unit_id");
            if (unitId != null) {
                Iterator<String> itr = unitId.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    String value = (String) unitId.get(key);

                    if (key.equalsIgnoreCase("management_ip") &&
                            !value.equalsIgnoreCase(StringUtil.EMPTY_STRING)) {
                        connectionStatus = ERadioConnectionStatus.ONLINE.toString();
                    }

                    Log.i(TAG, key + " = " + value);
                }
            }

            JSONObject variables = (JSONObject) jso.get("variables");
            Log.i(TAG, "==================== Variables ====================");

            if (variables != null) {
                Iterator<String> itr = variables.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    JSONObject obj = (JSONObject) variables.get(key);

//                    Log.i(TAG, "key: " + key);
//                    if () {
//                        Toast.makeText(MainApplication.getAppContext(), "token: " + jso.get("token"), Toast.LENGTH_LONG).show();
//                    }

                    if (obj.has("value")) {
//                        String value = obj.get("value").toString();
//                        Log.i(TAG, key + " = " + value.replace(System.lineSeparator(), StringUtil.SPACE));
                        JSONArray valueJSONArray = obj.getJSONArray("value");

                        for (int i = 0; i < valueJSONArray.length(); i++) {
                            JSONObject jsonObject = valueJSONArray.getJSONObject(i);

                            if (jsonObject.has("ip")) {
                                String radioIPAddress = jsonObject.get("ip").toString();
                                String snr = jsonObject.get("snr").toString();
                                Log.i(TAG, "Neighbour's IP [" + i + "]: " + radioIPAddress);
                                Log.i(TAG, "SNR [" + i + "]: " + snr);

                                neighbourRadioIPAddressToSnrMap.put(radioIPAddress, snr);
//                                neighbourRadioIPAddressList.add(radioIPAddress);
//                                signalToNoiseRatioList.add(snr);
                            }
                        }

                    } else if (obj.has("error")) {
                        JSONObject errObj = (JSONObject) obj.get("error");
                        String display = errObj.get("display").toString();
                        Log.i(TAG, key + " = ERROR: " + display);
                    }
                }
            }

        } catch (JSONException e) {
            Log.d(TAG, "Error:" + e);
//            Toast.makeText(MainApplication.getAppContext(), "Error is " + e,
//                    Toast.LENGTH_LONG).show();
        }

        // At this point, it means current user and target users are connected
        // to the network.
        // Broadcast own radio connection status and self-created BFT models
        // to other devices
        boolean isRndisTetheringActive = NetworkUtil.
                isRndisTetheringActive(MainApplication.getAppContext());

//        if (WaveRelayRadioClient.isUsbConnected() && isRndisTetheringActive) {
        if (isRndisTetheringActive) {
            broadcastOwnRadioConnectionStatus();
            broadcastOwnUserBFTModel();
//            updateWaveRelayDatabaseOfOtherUserIds(neighbourRadioIPAddressList);
            updateWaveRelayDatabaseOfOtherUserIds(neighbourRadioIPAddressToSnrMap);
            updateConnectionIfNeeded(connectionStatus);

            if (mSyncDataCount >= SYNC_DATA_INTERVAL) {
//                syncWithCallsign();
                Timber.i("Synchronising data over the network...");
                JeroMQBroadcastOperation.broadcastDataSyncOverSocket();
                mSyncDataCount = 0;
            }
        }

        mSyncDataCount++;
//        if (WaveRelayRadioClient.isUsbConnected()) {
//            updateWaveRelayDatabaseOfOtherUserIds(neighbourRadioIPAddressList);
//            updateConnectionIfNeeded(connectionStatus);
//        }

//        }
    }

    /**
     * Simple override and printing when the socket closes
     *
     * @param code   - Integer for the error code (reason why the socket was closed)
     * @param reason - String representation and reason of why the socket was closed
     * @param remote - May also be true if this endpoint started the closing handshake
     *               since the other endpoint may not simply echo the code but close
     *               the connection the same time this endpoint does
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "Socket closed: Code = " + code + ". Reason = " + reason);

        updateConnectionIfNeeded(ERadioConnectionStatus.OFFLINE.toString());
    }

    /**
     * Simple override
     *
     * @param ex - Exception class that may be thrown, raised, displayed etc.
     */
    @Override
    public void onError(Exception ex) {
        Log.i(TAG, "Socket error: " + ex);
    }

    /**
     * Notifies and broadcast change in user connection status
     *
     * @param connectionStatus
     */
    protected synchronized void updateConnectionIfNeeded(String connectionStatus) {

        if (!SharedPreferenceUtil.getCurrentUserRadioLinkStatus().equalsIgnoreCase(connectionStatus)) {
            boolean isConnected;

            if (ERadioConnectionStatus.OFFLINE.toString().equalsIgnoreCase(connectionStatus)) {
                isConnected = false;
            } else {
                isConnected = true;
            }

            updateAndBroadcastUserRadioConnectionStatus(
                    SharedPreferenceUtil.getCurrentUserCallsignID(), isConnected);
            updateAndBroadcastOwnWaveRelayRadioDetails(
                    SharedPreferenceUtil.getCurrentUserCallsignID(), isConnected);
            notifyWaveRelayClientConnectionBroadcastIntent(isConnected);
        }
    }

    /**
     * Broadcast own radio connection status to other devices
     */
    private synchronized void broadcastOwnRadioConnectionStatus() {
        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverUser, " +
                        "broadcastOwnRadioConnectionStatus. " +
                        "UserId: " + userModel.getUserId());

                // Send updated User model data to other connected devices
                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

                // If current user is online, his Wave Relay details will already be updated;
                // Broadcast Wave Relay details for other users who have just logged in
                if (ERadioConnectionStatus.ONLINE.toString().equalsIgnoreCase(
                        userModel.getRadioFullConnectionStatus())) {
                    broadcastWaveRelayRadioDetailsOfUser(userModel.getUserId(), true);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "broadcastOwnRadioConnectionStatus. " +
                        "Error Msg: " + e.toString());
            }
        };

        userRepository.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUser);
    }

    /**
     * Broadcasts wave relay radio detail of user to other devices
     */
    private synchronized void broadcastWaveRelayRadioDetailsOfUser(String userId, boolean isConnected) {
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
                            "broadcastWaveRelayRadioDetailsOfUser. " +
                            "UserId: " + waveRelayRadioModel.getUserId());

                    if (!SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(waveRelayRadioModel.getUserId())) {

                        if (!isConnected) {
                            waveRelayRadioModel.setUserId(null);
                            waveRelayRadioModel.setPhoneIpAddress(StringUtil.INVALID_STRING);
                            waveRelayRadioModel.setSignalToNoiseRatio(StringUtil.N_A);

                        } else {
                            waveRelayRadioModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                            waveRelayRadioModel.setPhoneIpAddress(NetworkUtil.
                                    getOwnIPAddressThroughWiFiOrEthernet(true));

                        }

                        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
                    }

                    JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverWaveRelayRadio, " +
                        "broadcastWaveRelayRadioDetailsOfUser. " +
                        "Error Msg: " + e.toString());
            }
        };

        waveRelayRadioRepository.queryRadioByUserId(userId, singleObserverWaveRelayRadio);
    }

    /**
     * Updates and broadcasts wave relay radio detail of current user to other devices
     */
    private synchronized void updateAndBroadcastOwnWaveRelayRadioDetails(String userId, boolean isConnected) {
        WaveRelayRadioRepository waveRelayRadioRepository = new WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // If not connected, update userId and phone IP address of Wave Relay Radio model to null and invalid respectively
        if (!isConnected) {

            // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
            // asynchronously in the background thread
            SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo = new SingleObserver<WaveRelayRadioModel>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add it to a CompositeDisposable
                }

                @Override
                public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                    if (waveRelayRadioModel != null) {
                        Log.d(TAG, "onSuccess singleObserverWaveRelayRadioByRadioNo not connected, " +
                                "updateAndBroadcastOwnWaveRelayRadioDetails. " +
                                "UserId: " + waveRelayRadioModel.getUserId());

                        waveRelayRadioModel.setUserId(null);
                        waveRelayRadioModel.setPhoneIpAddress(StringUtil.INVALID_STRING);
                        waveRelayRadioModel.setSignalToNoiseRatio(StringUtil.N_A);
                        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);

                        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError singleObserverWaveRelayRadioByRadioNo not connected, " +
                            "updateAndBroadcastOwnWaveRelayRadioDetails. " +
                            "Error Msg: " + e.toString());
                }
            };

//            waveRelayRadioRepository.queryRadioByUserId(userId, singleObserverWaveRelayRadio);

            Object userRadioId = SharedPreferenceUtil.getSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                    0);

            if (userRadioId != null) {
                if (userRadioId instanceof Integer) {
                    waveRelayRadioRepository.queryRadioByRadioId((int) userRadioId, singleObserverWaveRelayRadioByRadioNo);
                }
            }

        } else {    // If connected, update Wave Relay Radio model with current userId and phone IP address,
                    // if not already updated

            SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                    new SingleObserver<WaveRelayRadioModel>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            // add it to a CompositeDisposable
                        }

                        @Override
                        public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                            if (waveRelayRadioModel != null) {
                                Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo connected, " +
                                        "updateAndBroadcastOwnWaveRelayRadioDetails. " +
                                        "waveRelayRadioModel: %s", waveRelayRadioModel);

                                waveRelayRadioModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                                waveRelayRadioModel.setPhoneIpAddress(NetworkUtil.
                                        getOwnIPAddressThroughWiFiOrEthernet(true));

                                waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
                                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);

                            } else {
                                Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo connected, " +
                                        "updateAndBroadcastOwnWaveRelayRadioDetails. " +
                                        "waveRelayRadioModel is null");
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                            Timber.e("onError singleObserverWaveRelayRadioByRadioNo connected, " +
                                    "updateAndBroadcastOwnWaveRelayRadioDetails. " +
                                    "Error Msg: %s", e.toString());
                        }
                    };


            Object userRadioId = SharedPreferenceUtil.getSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                    0);

            if (userRadioId != null) {
                if (userRadioId instanceof Integer) {
                    waveRelayRadioRepository.queryRadioByRadioId((int) userRadioId, singleObserverWaveRelayRadioByRadioNo);
                }
            }

        }
    }

    /**
     * Updates and broadcast radio connection status of specific user in local database
     */
    private synchronized void updateAndBroadcastUserRadioConnectionStatus(String userId, boolean isConnected) {
        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverUser, " +
                        "updateAndBroadcastUserRadioConnectionStatus. " +
                        "UserId: " + userModel.getUserId());

                boolean isSecondConnectionCheckMet;
                String currentUserId = userModel.getUserId();

                // Check missing heart beat connection of other devices
                if (userModel.getMissingHeartBeatCount() >= MISSING_HEARTBEAT_CONNECTION_THRESHOLD &&
                        !SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(currentUserId)) {

                    isSecondConnectionCheckMet = false;
                    broadcastWaveRelayRadioDetailsOfUser(currentUserId, false);

                } else {
                    isSecondConnectionCheckMet = true;

                }

                if (isConnected && isSecondConnectionCheckMet) {

                    // Update last connected time only if user was previously offline
                    if (!ERadioConnectionStatus.ONLINE.toString().
                            equalsIgnoreCase(userModel.getRadioFullConnectionStatus()) ||
                            StringUtil.INVALID_STRING.
                                    equalsIgnoreCase(userModel.getLastKnownConnectionDateTime())) {

                        userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
                    }

                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.ONLINE.toString());
//                    userModel.setMissingHeartBeatCount(0);

                    // For own device, simply reset missing heart beat to 0
                    if (SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(currentUserId)) {

                        userModel.setMissingHeartBeatCount(0);
                        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                                ERadioConnectionStatus.ONLINE.toString());

                    }

//                    else {    // For other devices, increment missing heart beat count by 1.
//                                // Their heart beat counts will be updated to 0 by their respective devices when
//                                // they broadcast their own connection with heartbeat as 0.
//
//                        if (userModel.getMissingHeartBeatCount() != Integer.valueOf(StringUtil.INVALID_STRING)) {
//                            userModel.setMissingHeartBeatCount(userModel.getMissingHeartBeatCount() + 1);
//                        } else {
//                            userModel.setMissingHeartBeatCount(0);
//                        }
//                    }

                } else {

                    // Update last connected time only if user was previously online
                    if (!ERadioConnectionStatus.OFFLINE.toString().
                            equalsIgnoreCase(userModel.getRadioFullConnectionStatus())) {
                        userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
                    }

                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                    userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

                    if (SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(currentUserId)) {

                        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                                ERadioConnectionStatus.OFFLINE.toString());
                    }
                }

                userRepository.updateUser(userModel);

                // Send updated User data to other connected devices
                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "updateAndBroadcastUserRadioConnectionStatus. " +
                        "Error Msg: " + e.toString());
            }
        };

        userRepository.queryUserByUserId(userId, singleObserverUser);
    }

//    /**
//     * Updates Wave Relay database table of connected users and remove User Ids those who are not connected
//     * excluding current user (as this method is called with neighbours' IP addresses as input argument).
//     * Radio IP address list contains those who are connected to the network
//     */
//    private synchronized void updateWaveRelayDatabaseOfOtherUserIds(List<String> connectedRadioIpAddressList) {
//        WaveRelayRadioRepository waveRelayRadioRepository = new
//                WaveRelayRadioRepository((Application) MainApplication.getAppContext());
//
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread
//        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadio = new
//                SingleObserver<List<WaveRelayRadioModel>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
//                        Timber.i("onSuccess singleObserverAllWaveRelayRadio, updateWaveRelayDatabaseOfOtherUserIds. waveRelayRadioModel.size(): %d", waveRelayRadioModelList.size());
//
//                        // Obtain all Wave Relay models which are CONNECTED from database
//                        List<WaveRelayRadioModel> waveRelayConnectedRadioModelList = waveRelayRadioModelList.stream().
//                                filter(waveRelayRadioModel -> connectedRadioIpAddressList.contains(
//                                        waveRelayRadioModel.getRadioIpAddress())).collect(Collectors.toList());
//
//                        for (int i = 0; i < waveRelayConnectedRadioModelList.size(); i++) {
//                            WaveRelayRadioModel waveRelayRadioModel = waveRelayConnectedRadioModelList.get(i);
////                            queryAndUpdateUserOfRadioIPAddress(waveRelayRadioModel.getRadioIpAddress(),
////                                    true);
//
//                            String userId = waveRelayRadioModel.getUserId();
//                            if (userId != null) {
//                                updateAndBroadcastUserRadioConnectionStatus(userId, true);
////                                updateVideoStreamUrlOfUser(String.valueOf(waveRelayRadioModel.getRadioId()),
////                                        waveRelayRadioModel.getRadioIpAddress());
//                            }
//                        }
//
//                        // Obtain all Wave Relay models which are NOT CONNECTED from database
//                        List<WaveRelayRadioModel> waveRelayNotConnectedRadioModelList = waveRelayRadioModelList.stream().
//                                filter(waveRelayRadioModel -> !connectedRadioIpAddressList.contains(
//                                        waveRelayRadioModel.getRadioIpAddress())).collect(Collectors.toList());
//
//                        // Reset to NULL of user ids which are NOT CONNECTED to the network
//                        for (int i = 0; i < waveRelayNotConnectedRadioModelList.size(); i++) {
//                            WaveRelayRadioModel waveRelayRadioModel = waveRelayNotConnectedRadioModelList.get(i);
//
//                            // Set user connection to offline in User Table, if user was previously online
//                            String userId = waveRelayRadioModel.getUserId();
//                            if (userId != null && !userId.equalsIgnoreCase(SharedPreferenceUtil.
//                                    getCurrentUserCallsignID())) {
//
//                                updateAndBroadcastUserRadioConnectionStatus(userId, false);
//                                waveRelayRadioModel.setUserId(null);
//                                waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
//                            }
//
////                            queryAndUpdateUserOfRadioIPAddress(waveRelayRadioModel.getRadioIpAddress(),
////                                    false);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError singleObserverAllWaveRelayRadio, " +
//                                "updateWaveRelayDatabaseOfOtherUserIds. " +
//                                "Error Msg: " + e.toString());
//                    }
//                };
//
//        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverAllWaveRelayRadio);
//    }

    private synchronized void updateWaveRelayDatabaseOfOtherUserIds(Map<String, String> neighbourRadioIPAddressToSnrMap) {
        WaveRelayRadioRepository waveRelayRadioRepository = new
                WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadio = new
                SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
                        Timber.i("onSuccess singleObserverAllWaveRelayRadio, updateWaveRelayDatabaseOfOtherUserIds. waveRelayRadioModel.size(): %d", waveRelayRadioModelList.size());

                        Set<String> connectedRadioIpAddressSet = neighbourRadioIPAddressToSnrMap.keySet();

                        // Obtain all Wave Relay models which are CONNECTED from database
                        List<WaveRelayRadioModel> waveRelayConnectedRadioModelList = waveRelayRadioModelList.stream().
                                filter(waveRelayRadioModel -> connectedRadioIpAddressSet.contains(
                                        waveRelayRadioModel.getRadioIpAddress())).collect(Collectors.toList());

                        for (int i = 0; i < waveRelayConnectedRadioModelList.size(); i++) {
                            WaveRelayRadioModel waveRelayRadioModel = waveRelayConnectedRadioModelList.get(i);
//                            queryAndUpdateUserOfRadioIPAddress(waveRelayRadioModel.getRadioIpAddress(),
//                                    true);

                            String userId = waveRelayRadioModel.getUserId();
                            if (userId != null) {

                                String currentRadioSnr = neighbourRadioIPAddressToSnrMap.
                                        get(waveRelayRadioModel.getRadioIpAddress());

                                waveRelayRadioModel.setSignalToNoiseRatio(currentRadioSnr);
                                waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);

//                                updateAndBroadcastUserRadioConnectionStatus(userId, true);
//                                updateVideoStreamUrlOfUser(String.valueOf(waveRelayRadioModel.getRadioId()),
//                                        waveRelayRadioModel.getRadioIpAddress());
                            }
                        }

//                        // Obtain all Wave Relay models which are NOT CONNECTED from database
//                        List<WaveRelayRadioModel> waveRelayNotConnectedRadioModelList = waveRelayRadioModelList.stream().
//                                filter(waveRelayRadioModel -> !connectedRadioIpAddressSet.contains(
//                                        waveRelayRadioModel.getRadioIpAddress())).collect(Collectors.toList());
//
//                        // Reset to NULL of user ids which are NOT CONNECTED to the network
//                        for (int i = 0; i < waveRelayNotConnectedRadioModelList.size(); i++) {
//                            WaveRelayRadioModel waveRelayRadioModel = waveRelayNotConnectedRadioModelList.get(i);
//
//                            // Set user connection to offline in User Table, if user was previously online
//                            String userId = waveRelayRadioModel.getUserId();
//                            if (userId != null && !userId.equalsIgnoreCase(SharedPreferenceUtil.
//                                    getCurrentUserCallsignID())) {
//
//                                updateAndBroadcastUserRadioConnectionStatus(userId, false);
//                                waveRelayRadioModel.setUserId(null);
//                                waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
//                            }
//
////                            queryAndUpdateUserOfRadioIPAddress(waveRelayRadioModel.getRadioIpAddress(),
////                                    false);
//                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverAllWaveRelayRadio, " +
                                "updateWaveRelayDatabaseOfOtherUserIds. " +
                                "Error Msg: " + e.toString());
                    }
                };

        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverAllWaveRelayRadio);
    }


    /**
     * Updates radio connection status of specific user in local database
     */
//    protected synchronized void updateRadioConnectionStatusOfUser(String userId, boolean isConnected) {
//        UserRepository userRepository = new
//                UserRepository((Application) MainApplication.getAppContext());
//
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread
//        SingleObserver<UserModel> singleObserverUser = new
//                SingleObserver<UserModel>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(UserModel userModel) {
//                        if (userModel != null) {
//                            Log.d(TAG, "onSuccess singleObserverUser, " +
//                                    "updateRadioConnectionStatusOfUser. " +
//                                    "UserId: " + userModel.getUserId());
//
//                            if () {
//                            // Update last connected time only if user was previously offline/online
//                            // or last known connection date time is an invalid value (which means
//                            // not updated before)
//                            if (!ERadioConnectionStatus.ONLINE.toString().
//                                    equalsIgnoreCase(userModel.getRadioFullConnectionStatus()) ||
//                                    StringUtil.INVALID_STRING.
//                                            equalsIgnoreCase(userModel.getLastKnownConnectionDateTime())) {
//                                userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
//                            }
//
//                            userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
//                            userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
//                            userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.ONLINE.toString());
//
//                            userRepository.updateUser(userModel);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError singleObserverUser, " +
//                                "updateRadioConnectionStatusOfUser. " +
//                                "Error Msg: " + e.toString());
//                    }
//                };
//
//        userRepository.queryUserByUserId(userId,
//                singleObserverUser);
//    }

    /**
     * Updates and broadcast radio connection status of specific user in local database
     */
    private synchronized void broadcastOwnUserBFTModel() {
        BFTRepository bftRepository = new BFTRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<BFTModel>> singleObserverAllBFTs = new SingleObserver<List<BFTModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<BFTModel> bftModelList) {
                Log.d(TAG, "onSuccess singleObserverAllBFTs, " +
                        "broadcastOwnUserBFTModel. " +
                        "bftModelList size: " + bftModelList.size());

                // Obtain all bft models created by current user
                List<BFTModel> currentUserBftModelList = bftModelList.stream().
                        filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());

                for (int i = 0; i < currentUserBftModelList.size(); i++) {
                    BFTModel bftModel = currentUserBftModelList.get(i);

                    // Send current user's bft data to other connected devices
                    JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(bftModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAllBFTs, " +
                        "broadcastOwnUserBFTModel. " +
                        "Error Msg: " + e.toString());
            }
        };

        bftRepository.getAllBFTs(singleObserverAllBFTs);
    }

    /**
     * Sends a synchronisation request message to all connection devices
     */
    private synchronized void syncWithCallsign() {
        WaveRelayRadioRepository waveRelayRadioRepository = new
                WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        SingleObserver<List<WaveRelayRadioModel>> singleObserverGetAllWaveRelayRadios =
                new SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
                        Timber.i("onSuccess singleObserverGetAllWaveRelayRadios, syncWithCallsign. waveRelayRadioModelList.size(): %d",
                                waveRelayRadioModelList.size());

                        String targetPhoneIPAddress;

                        for (int i = 0; i < waveRelayRadioModelList.size(); i++) {
                            WaveRelayRadioModel waveRelayRadioModel = waveRelayRadioModelList.get(i);
                            String userId = waveRelayRadioModel.getUserId();

                            /**
                             * Request socket has to be executed in this order:
                             * 1) Set target phone ip address
                             * 2) Start process of creating and connected socket of target address
                             * 3) Send message
                             */
                            if (userId != null &&
                                    !SharedPreferenceUtil.getCurrentUserCallsignID().equalsIgnoreCase(userId)) {

                                Timber.i("Synchronising data with %s over the network...", userId);

                                targetPhoneIPAddress = waveRelayRadioModel.getPhoneIpAddress();
                                JeroMQClientPair.getInstance().setTargetPhoneIPAddress(targetPhoneIPAddress);
                                JeroMQClientPair.getInstance().start();
                                JeroMQClientPair.getInstance().sendSyncReqMessage();

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError singleObserverGetAllWaveRelayRadios, syncWithCallsign. Error Msg: %s ", e.toString());
                    }
                };

        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverGetAllWaveRelayRadios);
    }

    /**
     * Notify Wave Relay broadcast listeners of connection status
     *
     * @param isConnected
     */
    private synchronized void notifyWaveRelayClientConnectionBroadcastIntent(boolean isConnected) {
        Intent broadcastIntent = new Intent();

        if (isConnected) {
            broadcastIntent.setAction(WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION);
        } else {
            broadcastIntent.setAction(WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION);
        }

        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }
}
