package sg.gov.dh.trackers;

import sg.gov.dh.utils.Coords;

/**
 * This interface allows parent classes (E.g. Map Provider's LocationDataSource class) to receive<br>
 * 1. New location updates<br>
 * 2. 3rd party specific event information<br>
 */
public interface TrackerListener {
    /**
     * Provide Location Updates to parent classes (E.g. Map Provider's LocationDataSource class)<br>
     * The implementation of Tracker Interface should call this method when they receive new location updates.
     * @param coords
     * @see Tracker
     */
    void onNewCoords(Coords coords);

    /**
     * Provide Event updates to parent classes (E.g. Map Provider's LocationDataSource class)<br>
     * The implementation of Tracker Interface should call this method when they receive event info that they want to pass on to the parent classes.
     * @param event
     *  @see Tracker
     */
    void onNewEvent(Event event);
}
