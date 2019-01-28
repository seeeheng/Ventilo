package sg.gov.dsta.mobileC3.ventilo.network;

import android.os.Binder;

import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;

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
