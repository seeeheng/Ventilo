package sg.gov.dsta.mobileC3.ventilo.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class SharedPreferenceUtil {

    // TODO: Remove after demo
    private static Context mContext;
    /*
     * Get current user
     */
    public static String getCurrentUser(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String user = pref.getString(SharedPreferenceConstants.CALLSIGN_USER, "A11");

        return user;
    }

    // TODO: Remove after demo
    /*
     * Get current user
     */
    public static String getCurrentUserWithPredefinedContext() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String user = pref.getString(SharedPreferenceConstants.CALLSIGN_USER, "A11");

        return user;
    }

    // TODO: Remove after demo
    public static void setContext(Context context) {
        mContext = context;
    }
}
