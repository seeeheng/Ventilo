package sg.gov.dsta.mobileC3.ventilo.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sg.gov.dsta.mobileC3.ventilo.helper.MqttHelper;
import lombok.Data;

@Data
public class NetworkBroadcastReceiver extends BroadcastReceiver {

    public NetworkBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        MqttHelper mqttHelper = new MqttHelper(context);
    }
}
