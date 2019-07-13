package sg.gov.dh.beacons;

public class BeaconObject {
    String event = null;
    String id = null;

    public BeaconObject(String id)
    {
        setId(id);
    }
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



}
