package sg.gov.dsta.mobileC3.ventilo.network;

import android.os.Binder;

public class NetworkServiceBinder extends Binder {

    private NetworkService mRabbitMQService;

    NetworkServiceBinder(NetworkService rabbitMQService) {
        this.mRabbitMQService = rabbitMQService;
    }

    /**
     * @return a reference to the Service
     */
    public NetworkService getService() {
        return mRabbitMQService;
    }

}
