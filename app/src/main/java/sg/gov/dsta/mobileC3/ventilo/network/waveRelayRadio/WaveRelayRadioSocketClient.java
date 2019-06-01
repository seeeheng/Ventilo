package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.util.Log;

import com.persistentsystems.socketclient.WrWebSocketClient;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;

public class WaveRelayRadioSocketClient extends WrWebSocketClient {

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();

    /**
     * Basic constructor. See (@link com.persistentsystems.socketclient.WrWebSocketClient#WrWebSocketClient(URI, Draft))
     *
     * @param serverUri     - See (@link com.persistentsystems.socketclient.WrWebSocketClient#WrWebSocketClient(URI, Draft))
     * @throws IOException  - Throws due to reading input streams
     */
    protected WaveRelayRadioSocketClient(URI serverUri) throws IOException {
        super(serverUri);
        Log.i(TAG, "Class initialised");
    }

    /**
     * Simple override
     *
     * @param handshakeData   - Passed in from parent. Contains information about the connection
     */
    @Override
    public void onOpen(ServerHandshake handshakeData) {
        Log.i(TAG, "Socket opened");
    }

    /**
     * Simple override and printing out of incoming messages
     *
     * @param message   - A pure string representation of the message received by this websocket clients
     */
    @Override
    public void onMessage(String message) {
        Log.i(TAG, "Received: " + message + System.lineSeparator());

        JSONTokener tokener = new JSONTokener(message);

        try {
            JSONObject jso = (JSONObject) tokener.nextValue();
            Log.i(TAG, "command: " + jso.get("command"));
            Log.i(TAG, "msgtype: " + jso.get("msgtype"));
            Log.i(TAG, "token: " + jso.get("token"));
            Log.i(TAG, "protocol_version: " + jso.get("protocol_version"));

            Log.i(TAG, "==================== UNIT ID ====================");
            JSONObject unitId = (JSONObject) jso.get("unit_id");
            if (unitId != null) {
                Iterator<String> itr = unitId.keys();
                while (itr.hasNext()) {
                    String key = itr.next();
                    String value = (String) unitId.get(key);
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

                    if (obj.has("value")) {
                        String value = obj.get("value").toString();
                        Log.i(TAG, key + " = " + value.replace(System.lineSeparator(), StringUtil.SPACE));
                    } else if (obj.has("error")) {
                        JSONObject errObj = (JSONObject) obj.get("error");
                        String display = errObj.get("display").toString();
                        Log.i(TAG, key + " = ERROR: " + display);
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Simple override and printing when the socket closes
     *
     * @param code      - Integer for the error code (reason why the socket was closed)
     * @param reason    - String representation and reason of why the socket was closed
     * @param remote    - May also be true if this endpoint started the closing handshake
     *                  since the other endpoint may not simply echo the code but close
     *                  the connection the same time this endpoint does
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "Socket closed");
    }

    /**
     * Simple override
     *
     * @param ex    - Exception class that may be thrown, raised, displayed etc.
     */
    @Override
    public void onError(Exception ex) {
        Log.i(TAG, "Socket error: " + ex);
    }
}
