package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import sg.gov.dh.trackers.NavisensLocalTracker;
import sg.gov.dh.utils.Coords;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

/**
 * Javascript to Android interaction
 */
public class WebAppInterface {
    Context mContext;
    BFTLocalPreferences pref = null;
    NavisensLocalTracker tracker = null;
    String TAG = "JStoANDROIDinterface";

    /**
     * Instantiate the interface and set the context
     */
    WebAppInterface(Context c, BFTLocalPreferences prefs, NavisensLocalTracker tracker) {
        mContext = c;
        pref = prefs;
        this.tracker = tracker;
    }

    /**
     * Show a toast from the web page
     */
    @JavascriptInterface
    public void manualLocationUpdateinPixels(String xyInPixelszInMetres) {
        String[] xyz = xyInPixelszInMetres.split(",");

        Timber.i("1.manualLocationUpdateinPixels: %s %s %s" , xyz[0] , xyz[1] , xyz[2]);

        Timber.i("2.manualLocationUpdateinPixels: %d %d " , Double.parseDouble(xyz[0]) ,Double.parseDouble(xyz[1]));

        Timber.i("3.manualLocationUpdateinPixels: %s %s " , pref.getMetresFromPixels(Double.parseDouble(xyz[0])) , pref.getMetresFromPixels(Double.parseDouble(xyz[1])));


        Log.d(TAG, "1.manualLocationUpdateinPixels: " + xyz[0] + "," + xyz[1] + "," + xyz[2]);
        Log.d(TAG, "2.manualLocationUpdateinPixels: " + Double.parseDouble(xyz[0]) + "," + Double.parseDouble(xyz[1]));
        Log.d(TAG, "3.manualLocationUpdateinPixels: " + pref.getMetresFromPixels(Double.parseDouble(xyz[0])) + "," + pref.getMetresFromPixels(Double.parseDouble(xyz[1])));

        tracker.setManualLocation(new Coords(0, 0, Double.parseDouble(xyz[2]), 0, 0, 0, 0, pref.getMetresFromPixels(Double.parseDouble(xyz[0])), pref.getMetresFromPixels(Double.parseDouble(xyz[1])), null));
    }

    @JavascriptInterface
    public String getMQSettings() {
        String message = pref.getMqHost() + "," + pref.getMqUsername() + "," + pref.getMqPassword() + "," + pref.getTopic() + "," + pref.getPort();

        Timber.i("getMQSettings: %s" , message);

        return message;
    }

    @JavascriptInterface
    public String getName() {
        String message = pref.getName();
        Timber.i("getName: %s" , message);
        return message;
    }

    @JavascriptInterface
    public String getCurrentCoordMsg(String xyInPixelszInMetres, String type) {
        String[] xyz = xyInPixelszInMetres.split(",");
        String currentTime = DateTimeUtil.dateToCustomTimeStringFormat(
                DateTimeUtil.stringToDate(DateTimeUtil.getCurrentTime()));

        Coords currentCoord = new Coords(0, 0, Double.parseDouble(xyz[2]),
                0, 0, 0, 0,
                pref.getMetresFromPixels(Double.parseDouble(xyz[0])),
                pref.getMetresFromPixels(Double.parseDouble(xyz[1])), null);
        String currentCoordMsg = currentCoord.getX() + "," + currentCoord.getY() + "," + currentCoord.getAltitude() +
                "," + currentCoord.getBearing() + "," + "others" +
                "," + type + "," + currentTime;

        BFTModel bFTModel = new BFTModel();
        bFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
        bFTModel.setXCoord(String.valueOf(currentCoord.getX()));
        bFTModel.setYCoord(String.valueOf(currentCoord.getY()));
        bFTModel.setAltitude(String.valueOf(currentCoord.getAltitude()));
        bFTModel.setBearing(String.valueOf(currentCoord.getBearing()));
        bFTModel.setType(type);
        bFTModel.setCreatedTime(currentTime);

        DatabaseOperation databaseOperation = new DatabaseOperation();
        BFTRepository bFTRepository = new BFTRepository((Application) MainApplication.getAppContext());
        databaseOperation.insertBFTIntoDatabase(bFTRepository, bFTModel);

        return currentCoordMsg;
    }

    @JavascriptInterface
    public String getIconTypeToMarker() {
        return MapShipBlueprintFragment.getIconTypeToMarker();
    }

    @JavascriptInterface
    public String getHazardString() {
        return MainApplication.getAppContext().getString(R.string.map_blueprint_hazard_type);
    }

    @JavascriptInterface
    public String getDeceasedString() {
        return MainApplication.getAppContext().getString(R.string.map_blueprint_deceased_type);
    }

//    @JavascriptInterface
//    public String getHazardMsgListInJSObject() {
//        Gson gson = new GsonBuilder().create();
//        JsonArray hazardMsgJsonArray = gson.toJsonTree(MapShipBlueprintFragment.getHazardMsgList(),
//                ArrayList.class).getAsJsonArray();
////        String hazardMsgStrList = gson.toJson(MapShipBlueprintFragment.getHazardMsgList());
//
//        System.out.println("hazardMsgJsonArray is " + hazardMsgJsonArray.toString());
//
//        return hazardMsgJsonArray.toString();
//    }
//
//    @JavascriptInterface
//    public String getDeceasedMsgListInJSObject() {
//        Gson gson = new GsonBuilder().create();
//        JsonArray deceasedMsgJsonArray = gson.toJsonTree(MapShipBlueprintFragment.getDeceasedMsgList(),
//                ArrayList.class).getAsJsonArray();
//
//        System.out.println("deceasedMsgJsonArray is " + deceasedMsgJsonArray);
//        return deceasedMsgJsonArray.toString();
//    }

    @JavascriptInterface
    public String getDroppedBeacons() {
        return null;
    }

}
