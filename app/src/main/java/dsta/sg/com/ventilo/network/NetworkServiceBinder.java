package dsta.sg.com.ventilo.network;

import android.os.Binder;

import dsta.sg.com.ventilo.network.NetworkService;

public class NetworkServiceBinder extends Binder {

    private NetworkService mMqttService;

    NetworkServiceBinder(NetworkService mqttService) {
        this.mMqttService = mqttService;
    }

    /**
     * @return a reference to the Service
     */
    public NetworkService getService() {
        return mMqttService;
    }

}
