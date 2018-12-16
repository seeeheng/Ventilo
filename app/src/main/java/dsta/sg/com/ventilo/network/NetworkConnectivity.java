package dsta.sg.com.ventilo.network;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest.Builder;
import android.os.Build;

import dsta.sg.com.ventilo.helper.MqttHelper;
import lombok.val;

public class NetworkConnectivity extends LiveData {

    Context mContext;
    ConnectivityManager mConnectivityManager;
    MqttHelper mMqttHelper;

    public NetworkConnectivity(Context context) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        mContext = context;
    }

    public NetworkConnectivity(Context context, MqttHelper mqttHelper) {
        this(context);
        mMqttHelper = mqttHelper;
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
//            if (mMqttHelper != null) {
//                mMqttHelper = new MqttHelper(mContext);
//            }
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
        }
    };

    @Override
    protected void onActive() {
        super.onActive();
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mConnectivityManager.registerDefaultNetworkCallback(networkCallback);
            } else {
                Builder builder = new Builder();
                mConnectivityManager.registerNetworkCallback(builder.build(), networkCallback);
            }
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        mConnectivityManager.unregisterNetworkCallback(networkCallback);
    }
}
