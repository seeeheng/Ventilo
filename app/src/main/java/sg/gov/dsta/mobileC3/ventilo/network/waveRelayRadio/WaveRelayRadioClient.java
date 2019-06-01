package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.util.Log;

import com.persistentsystems.socketclient.exceptions.WrSocketNotReadyException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WaveRelayRadioClient {

    private static final String TAG = WaveRelayRadioSocketClient.class.getSimpleName();
    private static final String OWN_RADIO_IP = "10.1.3.254";
    private static final String SOCKET_URL = "wss://" + OWN_RADIO_IP + ":443/xxx";

    private WaveRelayRadioClient() {
        establishConnection();
    }

    /**
     * Establish socket connection between client and server
     *
     */
    private void establishConnection() {
        try {
            WaveRelayRadioSocketClient wrWebSocketClient = new WaveRelayRadioSocketClient(new URI(SOCKET_URL));

            wrWebSocketClient.setSocketUsername("factory");
            wrWebSocketClient.setSocketPassword("password");

            /**
             * wrWebSocketClient.connectBlocking() is a blocking connection attempt.
             * Since this is not a threaded program, not concerned.
             * wrWebSocketClient.connect() is non-blocking.
             */
            wrWebSocketClient.connectBlocking();

            /**
             * Actual connection and sending of data takes place here.
             * wrWebSocketClient.get(String var) returns a string of what was sent
             * to the socket server (NOT THE RESPONSE FROM THE SERVER)
             */
            Log.i(TAG, "********************");
            String[] vars = {"radio1_bandwidth", "radio1_frequency"};
            Log.i(TAG, "wrWebSocketClient packets received: " + wrWebSocketClient.get(vars));
            Log.i(TAG, "********************");

            wrWebSocketClient.close();

        } catch (IOException e) {
            Log.i(TAG, "wrWebSocketClient IOException: " + e);
        } catch (URISyntaxException e) {
            Log.i(TAG, "wrWebSocketClient URISyntaxException: " + e);
        } catch (InterruptedException e) {
            Log.i(TAG, "wrWebSocketClient InterruptedException: " + e);
        } catch (WrSocketNotReadyException e) {
            Log.i(TAG, "wrWebSocketClient WrSocketNotReadyException: " + e);
        }
    }
}
