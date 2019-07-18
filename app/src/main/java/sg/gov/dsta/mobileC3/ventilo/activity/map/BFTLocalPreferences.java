package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import sg.gov.dsta.mobileC3.ventilo.R;

public class BFTLocalPreferences {

    private Context context = null;
    private SharedPreferences prefs = null;

    private ArrayList<String> floors = new ArrayList();
    private int currentFloor = 0;
    private double mapScale = 300; // 1cm to 250cm (Avatar), 1cm to 300cm (BW Paris, however, works on 119.91)
//    private double onePixelToMetres = (11.82 / 1400 * mapScale / 100); // For Avatar; 0.021 (Avatar); 0.05291 (BW Paris)
    private double onePixelToMetres = (41.45 / 2350 * mapScale / 100); // For BW Paris; 0.021 (Avatar); 0.05291 (BW Paris)

    public double getBeaconActivateDistance() {
        return Double.valueOf(this.prefs.getString(context.getResources().getString(R.string.estimoteDist), "null"));
    }

    public String getLogLocation() {
        return this.prefs.getString(context.getResources().getString(R.string.logsloc), "null");
    }

    public String getBeaconAppId() {
        return this.prefs.getString(context.getResources().getString(R.string.estimoteId), "null");
    }

    public String getBeaconToken() {
        return this.prefs.getString(context.getResources().getString(R.string.estimoteToken), "null");
    }

    public String getMqHost() {
        return this.prefs.getString(context.getResources().getString(R.string.mqhost), "null");
    }

    public String getPort() {
        return this.prefs.getString(context.getResources().getString(R.string.mqport), "null");
    }

    public String getTopic() {
        return this.prefs.getString(context.getResources().getString(R.string.mqTopic), "null");
    }

    public String getMqUsername() {
        return this.prefs.getString(context.getResources().getString(R.string.mqUser), "null");
    }

    public String getMqPassword() {
        return this.prefs.getString(context.getResources().getString(R.string.mqPassword), "null");
    }

    public String getBfthost() {

        return this.prefs.getString(context.getResources().getString(R.string.mqhost), "null");
    }

    public String getName() {
        return this.prefs.getString(context.getResources().getString(R.string.callsign), "null");
    }

    public BFTLocalPreferences(Context context) {

        this.context = context;
        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);

//        //Ground floor up
//        floors.add(0, "leaflet-avatar-deck3rd.html");
//        floors.add(1, "leaflet-avatar-deck2nd.html");
//        floors.add(2, "leaflet-avatar-decktween.html");
//        floors.add(3, "leaflet-avatar-maindeck.html");
//        floors.add(4, "leaflet-avatar-deck2.html");
//        floors.add(5, "leaflet-avatar-deck3.html");
//        floors.add(6, "leaflet-avatar-deck4.html");
//        floors.add(7, "leaflet-avatar-deck5.html");
//        floors.add(8, "leaflet-avatar-deck6.html");
//        floors.add(9, "leaflet-avatar-deck7.html");
//        floors.add(10, "leaflet-avatar-deckplatform.html");
//        floors.add(11, "leaflet-avatar-lateral.html");

        //Ground floor up
        floors.add(0, "leaflet-bw-paris-tank-top.html");
        floors.add(1, "leaflet-bw-paris-a-deck.html");
        floors.add(2, "leaflet-bw-paris-upper-deck.html");
        floors.add(3, "leaflet-bw-paris-b-deck.html");
        floors.add(4, "leaflet-bw-paris-c-deck.html");
        floors.add(5, "leaflet-bw-paris-d-deck.html");
        floors.add(6, "leaflet-bw-paris-nav-bri-deck.html");
        floors.add(7, "leaflet-bw-paris-midship.html");
        floors.add(8, "leaflet-bw-paris-profile.html");
    }

//    private int getOneFloorUp() {
//        if ((currentFloor + 1) < floors.size()) {
//            currentFloor = currentFloor + 1;
//        }
//        return currentFloor;
//    }
//
//    private int getOneFloorDown() {
//        if (currentFloor > 0) {
//            currentFloor = currentFloor - 1;
//        }
//        return currentFloor;
//    }
//
//    public String getNextFloorUp() {
//        return floors.get(getOneFloorUp());
//    }
//
//    public String getNextFloorDown() {
//        return floors.get(getOneFloorDown());
//    }

    public String getOverview() {
        return floors.get(floors.size() - 1);
    }

    public double getMetresFromPixels(double pixels) {
        double metres = pixels * onePixelToMetres;
        return metres;
    }
}
