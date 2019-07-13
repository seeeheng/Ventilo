package sg.gov.dh.beacons;

/**
 * This interface allows parent classes to receive<br>
 * 1. New beacon updates<br>
 * 2. 3rd party specific event information<br>
 */
public interface BeaconListener {
    /**
     * Provide beacon to parent classes<br>
     * The implementation of BeaconObject Interface should call this method when they receive new updates.
     * @param beacon
     */
    void onNewUpdate(BeaconObject beacon);

}
