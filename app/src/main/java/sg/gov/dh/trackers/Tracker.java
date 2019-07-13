package sg.gov.dh.trackers;

import sg.gov.dh.utils.Coords;

/**
 * Allow parent classes (E.g. Map Provider's LocationDataSource class) to use 3rd party personnel trackers without dwelling into individual tracker technologies.<br><br>
 * 3rd party trackers should implement this Interface and implement the 3rd party methods to achieve the methods defined in this Interface class.
 */
public interface Tracker {
    /**
     * Provide an option to check for active status of tracker<br>
     * To check on Tracker status (Active or not)
     * @return A boolean indicating Active (True) or not (False)
     */
    boolean isActive();

    /**
     * Allow parent classes (E.g. Map Provider's LocationDataSource class) to access new location updates triggered by the 3rd party tracker technologies.<br>
     * Implementaion of this Interface should simply assign the listener to a global private instance.<br>
     * this.listener = listener;
     * @param listener
     */
    void setTrackerListener(TrackerListener listener);

    /**
     * Most 3rd parties trackers would need to be deactivated gracefully to avoid problems to their devices.<br>
     * Implementation of this interface should ensure their tracker is gracefully disabled in their own way.
     *
     */
    void deactivate();

    /**
     * Allow parent classes (E.g. Map Provider's LocationDataSource class) to set a fixed coordinate and request the Tracker to start from that.<br>
     * Implementation of this interface should ensure that the Tracker can handle adhoc forced location updates, otherwise should not implement this.
     * @param coords
     */
    void setManualLocation(Coords coords);

    /**
     * Allow parent classes (E.g. Map Provider's LocationDataSource class) to request the 3rd Party tracker to use Android's internal GPS location service<br>
     * Implementation of this interface should follow Android's standard Location Service APIs to request for GPS updates ONCE.
     */
    void setGPSLocation();

    /**
     * Some 3rd party trackers may have their own setup requirements, either headless or interactive.<br>
     * Implementation of this interface should ensure setup of their Tracker and provide their own Activity to perform the actions. (E.g. By means of Intent)
     */
    void setup();


    /**
     * Ask the tracker to stop relying on GPS inputs
     */
    void stopGPSUpdate();

    /**
     * Ask the tracker to start getting assistance from GPS inputs
     */
    void startGPSUpdate();
}
