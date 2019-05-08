package sg.gov.dsta.mobileC3.ventilo.util;

import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;

public class PowerManagerUtil {

    private static final String TAG = PowerManagerUtil.class.getSimpleName();

    private static WakeLock mWl;

    // Protect against the phone switching off by requesting a minimum possible wake lock -
    // just enough to keep the CPU running until operation is done
    public static void acquirePartialWakeLock() {
        if (mWl == null) {
            PowerManager pm = (PowerManager) MainApplication.getAppContext().getSystemService(
                    MainApplication.getAppContext().POWER_SERVICE);
            mWl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWl.acquire();
        } else if (!mWl.isHeld()) {
            mWl.acquire();
        }
    }

    // Operation is complete. If the phone is switched off, CPU may go to sleep now
    public static void releasePartialWakeLock() {
        if (mWl != null && mWl.isHeld()) {
            mWl.release();
        }
    }
}
