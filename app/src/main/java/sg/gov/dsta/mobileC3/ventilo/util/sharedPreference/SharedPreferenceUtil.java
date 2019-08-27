package sg.gov.dsta.mobileC3.ventilo.util.sharedPreference;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;

public class SharedPreferenceUtil {

    private static final String DEFAULT_CALLSIGN = "111";
    private static final String DEFAULT_TEAM = "ALPHA";

    /**
     * Get current user Callsign
     *
     * @return
     */
    public static String getCurrentUserCallsignID() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String user = pref.getString(SharedPreferenceConstants.USER_ID, DEFAULT_CALLSIGN);

        return user;
    }

    /**
     * Get current user Callsign
     *
     * @return
     */
    public static String getCurrentUserTeam() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String user = pref.getString(SharedPreferenceConstants.USER_TEAM, DEFAULT_TEAM);

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
        String accessToken = pref.getString(SharedPreferenceConstants.USER_ACCESS_RIGHT, EAccessRight.TEAM_LEAD.toString());

        return accessToken;
    }

    /**
     * Get current user radio link status
     *
     * @return
     */
    public static String getCurrentUserRadioLinkStatus() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        String userRadioLinkStatus = pref.getString(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                ERadioConnectionStatus.OFFLINE.toString());

        return userRadioLinkStatus;
    }

    public static Object getSharedPreference(String key, Object defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        Object sharedPrefStoredObj = null;

        if (defaultValue instanceof String) {
            sharedPrefStoredObj = pref.getString(key, String.valueOf(defaultValue));

        } else if (defaultValue instanceof Integer) {
            sharedPrefStoredObj = pref.getInt(key, (int) defaultValue);

        } else if (defaultValue instanceof Long) {
            sharedPrefStoredObj = pref.getLong(key, (long) defaultValue);

        } else if (defaultValue instanceof Float) {
            sharedPrefStoredObj = pref.getFloat(key, (float) defaultValue);

        } else if (defaultValue instanceof Boolean) {
            sharedPrefStoredObj = pref.getBoolean(key, (boolean) defaultValue);

        } else if (defaultValue instanceof Set) {
            sharedPrefStoredObj = pref.getStringSet(key, (Set<String>) defaultValue);
        }

        return sharedPrefStoredObj;
    }

    public static void setSharedPreference(String key, Object value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getAppContext());
        SharedPreferences.Editor editor = pref.edit();

        if (value instanceof String) {
            editor.putString(key, String.valueOf(value));

        } else if (value instanceof Integer) {
            editor.putInt(key, (int) value);

        } else if (value instanceof Long) {
            editor.putLong(key, (long) value);

        } else if (value instanceof Float) {
            editor.putFloat(key, (float) value);

        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (boolean) value);

        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        }

        editor.apply();
    }
}
