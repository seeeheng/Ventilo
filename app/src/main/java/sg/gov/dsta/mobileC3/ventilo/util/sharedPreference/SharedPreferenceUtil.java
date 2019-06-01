package sg.gov.dsta.mobileC3.ventilo.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.task.EAccessRight;

public class SharedPreferenceUtil {

    private static final String DEFAULT_CALLSIGN = "111";

    /**
     * Get current user Callsign
     *
     * @return
     */
    public static String getCurrentUserCallsignID() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String user = pref.getString(SharedPreferenceConstants.CALLSIGN_USER, DEFAULT_CALLSIGN);

        return user;
    }

    /**
     * Get current user Access Token
     *
     * @return
     */
    public static String getCurrentUserAccessToken() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String accessToken = pref.getString(SharedPreferenceConstants.ACCESS_TOKEN, StringUtil.INVALID_STRING);

        return accessToken;
    }

    /**
     * Get current user Access Rights
     *
     * @return
     */
    public static String getCurrentUserAccessRight() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String accessToken = pref.getString(SharedPreferenceConstants.ACCESS_RIGHT, EAccessRight.TEAM_LEAD.toString());

        return accessToken;
    }
}
