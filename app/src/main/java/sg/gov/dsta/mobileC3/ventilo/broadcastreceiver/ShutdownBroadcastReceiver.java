package sg.gov.dsta.mobileC3.ventilo.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sg.gov.dsta.mobileC3.ventilo.util.StopServiceUtil;

public class ShutdownBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION_QUICKBOOT_POWEROFF =
            "android.intent.action.QUICKBOOT_POWEROFF";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_SHUTDOWN.equalsIgnoreCase(intent.getAction()) ||
                Intent.ACTION_REBOOT.equalsIgnoreCase(intent.getAction()) ||
                ACTION_QUICKBOOT_POWEROFF.equalsIgnoreCase(intent.getAction())) {

            StopServiceUtil.stopAllServices();
        }
    }

}
