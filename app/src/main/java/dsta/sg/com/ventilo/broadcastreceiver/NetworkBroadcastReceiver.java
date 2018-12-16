package dsta.sg.com.ventilo.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import dsta.sg.com.ventilo.helper.MqttHelper;
import lombok.Data;

@Data
public class NetworkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        MqttHelper mqttHelper = new MqttHelper(context);
    }
}
