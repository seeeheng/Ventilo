package sg.gov.dh.beacons;

import sg.gov.dh.utils.Coords;

public class DroppedBeacon extends BeaconObject{
    Coords coords = null;

    public DroppedBeacon(Coords coords, String beaconId)
    {
        super(beaconId);
        setCoords(coords);
    }

    public Coords getCoords() {
        return coords;
    }

    public void setCoords(Coords coords) {
        this.coords = coords;
    }

}
