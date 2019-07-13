package sg.gov.dh.beacons;

import android.app.Activity;
import android.nfc.NdefMessage;

/**
 * Allow parent classes to use 3rd party beacons without dwelling into individual beacon technologies.<br><br>
 * 3rd party beacons should implement this Interface and implement the 3rd party methods to achieve the methods defined in this Interface class.
 */
public interface BeaconManagerInterface {

    void setAppId(String id);
    void setAppToken(String token);
    void setDistActivate(double dist);
    String getBeaconIdbByNFC(NdefMessage nfcMsg);
    void disableForegroundDispatch();
    void enableForegroundDispatch();


    /**
     * Provide an option to check for active status of beacon<br>
     * To check on BeaconObject status (Active or not)
     * @return A boolean indicating Active (True) or not (False)
     */
    boolean isActive();

    /**
     * Allow parent classes to access new BeaconManagerInterface updates triggered by the 3rd party beacon technologies.<br>
     * Implementaion of this Interface should simply assign the listener to a global private instance.<br>
     * this.listener = listener;
     * @param listener
     */
    void setBeaconListener(BeaconListener listener);

    /**
     * Most 3rd parties beacons would need to be deactivated gracefully to avoid problems to their devices.<br>
     * Implementation of this interface should ensure their beacon is gracefully disabled in their own way.
     *
     */
    void deactivate();

//    /**
//     * Allow parent classes (E.g. Map Provider's LocationDataSource class) to set a fixed coordinate and request the Tracker to start from that.<br>
//     * Implementation of this interface should ensure that the Tracker can handle adhoc forced location updates, otherwise should not implement this.
//     * @param coords
//     */
//    void setManualLocation(Coords coords);

//    /**
//     * Allow parent classes (E.g. Map Provider's LocationDataSource class) to request the 3rd Party tracker to use Android's internal GPS location service<br>
//     * Implementation of this interface should follow Android's standard Location Service APIs to request for GPS updates ONCE.
//     */
//    void setGPSLocation();

    /**
     * Some 3rd party beacons may have their own setup requirements, either headless or interactive.<br>
     * Implementation of this interface should ensure setup of their BeaconManagerInterface and provide their own Activity to perform the actions. (E.g. By means of Intent)
     */
    void setup();


//    /**
//     * Ask the tracker to stop relying on GPS inputs
//     */
//    void stopGPSUpdate();
//
//    /**
//     * Ask the tracker to start getting assistance from GPS inputs
//     */
//    void startGPSUpdate();
}
