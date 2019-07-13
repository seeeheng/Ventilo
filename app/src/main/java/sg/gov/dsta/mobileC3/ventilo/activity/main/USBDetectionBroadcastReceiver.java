package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class USBDetectionBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = USBDetectionBroadcastReceiver.class.getSimpleName();
    public static final String RECEIVED_USB_UPDATE_ACTION = "Received USB Update";
    private boolean mIsUSBConnected;

    @Override
    public void onReceive(final Context context, Intent intent) {

        String action = intent.getAction();
        Log.v(TAG,"action: " + action);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(action.equals("android.hardware.usb.action.USB_STATE")) {
                if(intent.getExtras().getBoolean("connected")){
                    mIsUSBConnected = true;
                    Toast.makeText(context, "USB Connected", Toast.LENGTH_SHORT).show();
                } else {
                    mIsUSBConnected = false;
                    Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
                mIsUSBConnected = true;
                Toast.makeText(context, "USB Connected", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                mIsUSBConnected = false;
                Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isUSBConnected() {
        return mIsUSBConnected;
    }
}
