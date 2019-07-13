package sg.gov.dh.trackers;

/**
 * This class is meant to allow 3rd party trackers to encapsulate their runtime events into something that  parent classes (E.g. Map Provider's LocationDataSource class) can view.
 * The event should be Tracker specific
 */
public class Event {

    /**
     * Name of the TRX's NAVIGATION LOCK event
     */
    public static String TRX_NAV_LOCK="TRX_NAV_LOCK";
    public static String TRX_GPS_ASSIST="GPS_ASSIST";

    /**
     * The name of the Event
     */
    String name;

    /**
     * Location 'Locked' status in the form of integer numbers. (Default: -1)
     */
    int lockBars=-1;

    /**
     *
     * @param _name A static string that describes the event. This should be unique and set as one of the static strings.
     */
    public Event(String _name){
        this.setName(_name);
    }

    /**
     * Parent classes (E.g. Map Provider's LocationDataSource class) can get location 'Locked' status in the form of integer numbers (E.g. TRX)
     * @return An integer representing the level of 'Locked'.
     */
    public int getLockBars() {
        return lockBars;
    }

    /**
     * This is for trackers that can offer a location 'Locked' status in the form of integer numbers (E.g. TRX)
     * @param lockBars An integer representing the level of 'Locked'.
     */
    public void setLockBars(int lockBars) {
        this.lockBars = lockBars;
    }

    /**
     * Get the name of the event
     * @return A string that identifies the name of the Event.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the event.
     * @param name A string that identifies the name of the Event.
     */
    public void setName(String name) {
        this.name = name;
    }
}
