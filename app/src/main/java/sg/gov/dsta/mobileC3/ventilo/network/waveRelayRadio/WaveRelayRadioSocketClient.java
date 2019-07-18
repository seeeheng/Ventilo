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
import java.util.Iterator;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;

public class WaveRelayRadioSocketClient extends WrWebSocketClient {

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();
    // Intent Filters
    public static final String WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Connected";
    public static final String WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION =
            "Wave Relay Radio Client Disconnected";

    private int mIncomingMsgCount = 0;
    private boolean mIsConnected;

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
        mIncomingMsgCount++;
        Log.i(TAG, "Received: " + message + System.lineSeparator());
//        Toast.makeText(MainApplication.getAppContext(), "Received: " + message + System.lineSeparator(),
//                Toast.LENGTH_LONG).show();
//        Gson gson = GsonCreator.createGson();
//        String newSitRepModelJson = gson.toJson(sitRepModel);

        boolean isConnected = false;
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
                        isConnected = true;
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
                                Log.i(TAG, "Neighbour's IP [" + i + "]: " + jsonObject.get("ip").toString());

//                                updateAllNodesOfNeighbourNodeConnectionStatus();
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

        System.out.println("mIncomingMsgCount is " + mIncomingMsgCount);

        if (mIsConnected != isConnected) {
            mIsConnected = isConnected;
            notifyWaveRelayClientConnectionBroadcastIntent(mIsConnected);
            updateAndBroadcastRadioConnectionStatus(isConnected);
        }

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
        updateAndBroadcastRadioConnectionStatus(false);
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

    protected int getIncomingMsgCount() {
        return mIncomingMsgCount;
    }

    /**
     * Updates and broadcast radio connection status of current user
     */
    protected void updateAndBroadcastRadioConnectionStatus(boolean isConnected) {
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
                        "updateAndBroadcastRadioConnectionStatus. " +
                        "UserId: " + userModel.getUserId());

                if (isConnected) {
                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.ONLINE.toString());

                } else {
                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                }

                userRepository.updateUser(userModel);
                // Send updated Sit Rep data to other connected devices
                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "addSitRepToCompositeTableInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        userRepository.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUser);
    }

    protected void notifyWaveRelayClientConnectionBroadcastIntent(boolean isConnected) {
        Intent broadcastIntent = new Intent();

        if (isConnected) {
            broadcastIntent.setAction(WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION);
        } else {
            broadcastIntent.setAction(WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION);
        }

        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    /**
     * Updates and broadcast radio connection status of current user
     */
//    private void updateAllNodesOfNeighbourNodeConnectionStatus(String neighbourNodeIpAddr) {
//        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());
//
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread
//        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                // add it to a CompositeDisposable
//            }
//
//            @Override
//            public void onSuccess(UserModel userModel) {
//                Log.d(TAG, "onSuccess singleObserverUser, " +
//                        "updateAndBroadcastRadioConnectionStatus. " +
//                        "UserId: " + userModel.getUserId());
//
//                if (isConnected) {
//                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
//                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
//                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.ONLINE.toString());
//
//                } else {
//                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
//                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
//                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
//                }
//
//                userRepository.updateUser(userModel);
//                // Send updated Sit Rep data to other connected devices
//                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d(TAG, "onError singleObserverUser, " +
//                        "addSitRepToCompositeTableInDatabase. " +
//                        "Error Msg: " + e.toString());
//            }
//        };
//
//        userRepository.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
//                singleObserverUser);
//    }
}
