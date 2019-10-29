package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dh.trackers.NavisensLocalTracker;
import sg.gov.dh.utils.Coords;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.bft.EBftAction;
import sg.gov.dsta.mobileC3.ventilo.util.enums.bft.EBftType;
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

    protected static boolean mIsLocationInitialised;

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

        Timber.i("1.manualLocationUpdateinPixels: %s, %s, %s", xyz[0], xyz[1], xyz[2]);
        Timber.i("2.manualLocationUpdateinPixels: %f, %f ", Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]));
        Timber.i("3.manualLocationUpdateinPixels: %s, %s ", pref.getMetresFromPixels(Double.parseDouble(xyz[0])), pref.getMetresFromPixels(Double.parseDouble(xyz[1])));

        Log.d(TAG, "1.manualLocationUpdateinPixels: " + xyz[0] + "," + xyz[1] + "," + xyz[2]);
        Log.d(TAG, "2.manualLocationUpdateinPixels: " + Double.parseDouble(xyz[0]) + "," + Double.parseDouble(xyz[1]));
        Log.d(TAG, "3.manualLocationUpdateinPixels: " + pref.getMetresFromPixels(Double.parseDouble(xyz[0])) + "," + pref.getMetresFromPixels(Double.parseDouble(xyz[1])));

        tracker.setManualLocation(new Coords(0, 0, Double.parseDouble(xyz[2]), 90, 0, 0, 0, pref.getMetresFromPixels(Double.parseDouble(xyz[0])), pref.getMetresFromPixels(Double.parseDouble(xyz[1])), null));

        mIsLocationInitialised = true;
    }

    @JavascriptInterface
    public String getMQSettings() {
        String message = pref.getMqHost() + "," + pref.getMqUsername() + "," + pref.getMqPassword() + "," + pref.getTopic() + "," + pref.getPort();
        Timber.i("getMQSettings: %s", message);

        return message;
    }

    @JavascriptInterface
    public String getName() {
        String message = pref.getName();
        Timber.i("getName: %s", message);
        return message;
    }

    /**
     * Inserts bft model into database and obtain current coordinate info
     *
     * @param xyInPixelszInMetres
     * @param type
     * @return
     */
    @JavascriptInterface
    public void setCurrentCoordMsg(String xyInPixelszInMetres, String type) {
        String[] xyz = xyInPixelszInMetres.split(",");

        Coords currentCoord = new Coords(0, 0, Double.parseDouble(xyz[2]),
                0, 0, 0, 0,
                pref.getMetresFromPixels(Double.parseDouble(xyz[0])),
                pref.getMetresFromPixels(Double.parseDouble(xyz[1])), null);

//        String currentTimeToDisplay = DateTimeUtil.dateToCustomTimeStringFormat(
//                DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));
//
//        String currentCoordMsg = currentCoord.getX() + "," + currentCoord.getY() + "," + currentCoord.getAltitude() +
//                "," + currentCoord.getBearing() + "," + SharedPreferenceUtil.getCurrentUserCallsignID() +
//                "," + type + "," + currentTimeToDisplay;

        String currentDateTime = DateTimeUtil.dateToStandardIsoDateTimeStringFormat(
                DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));

        BFTModel bFTModel = new BFTModel();
        bFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
        bFTModel.setXCoord(String.valueOf(currentCoord.getX()));
        bFTModel.setYCoord(String.valueOf(currentCoord.getY()));
        bFTModel.setAltitude(String.valueOf(currentCoord.getAltitude()));
        bFTModel.setBearing(String.valueOf(currentCoord.getBearing()));
        bFTModel.setAction(EBftAction.BEACONDROP.toString());
        bFTModel.setType(type);
        bFTModel.setCreatedDateTime(currentDateTime);

        addItemToLocalDatabase(bFTModel);

//        return currentCoordMsg;
    }

    @JavascriptInterface
    public String getIconTypeToMarker() {
        return MapShipBlueprintFragment.getIconTypeToMarker();
    }

    @JavascriptInterface
    public String getHazardString() {
        return EBftType.HAZARD.toString();
    }

    @JavascriptInterface
    public String getHazardStaleString() {
        return EBftType.HAZARD_STALE.toString();
    }

    @JavascriptInterface
    public String getDeceasedString() {
        return EBftType.DECEASED.toString();
    }

    @JavascriptInterface
    public String getDeceasedStaleString() {
        return EBftType.DECEASED_STALE.toString();
    }

    @JavascriptInterface
    public String getOwnString() {
        return EBftType.OWN.toString();
    }

    @JavascriptInterface
    public String getCurrentUserId() {
        return SharedPreferenceUtil.getCurrentUserCallsignID();
    }

    /**
     * Deleting marker from javascript
     *
     * @param bftId
     */
    @JavascriptInterface
    public void deleteMarker(String bftId) {
        Timber.i("Deleting bftId: %d", Long.valueOf(bftId));
        BFTRepository bftRepository = new BFTRepository((Application)
                MainApplication.getAppContext());
        DatabaseOperation.getInstance().deleteBftInDatabase(bftRepository, Long.valueOf(bftId));
    }

    /**
     * Stores Bft data locally with updated Ref Id
     *
     * @param bftModel
     */
    private void addItemToLocalDatabase(BFTModel bftModel) {

        BFTRepository bFTRepository = new BFTRepository((Application) MainApplication.getAppContext());

        SingleObserver<Long> singleObserverAddBft = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long bftId) {
                Timber.i("onSuccess singleObserverAddBft,addItemToLocalDatabase. SitRepId: %d", bftId);

                updateBftModelRefId(bFTRepository, bftId);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAddBft, addItemToLocalDatabase.Error Msg: %s ", e.toString());
            }
        };

        bFTRepository.insertBFTWithObserver(bftModel, singleObserverAddBft);
    }

    /**
     * Updates Ref Id of newly inserted BFT model
     *
     * @param bftId
     */
    private void updateBftModelRefId(BFTRepository bFTRepository, Long bftId) {
        SingleObserver<BFTModel> singleObserverUpdateBftRefId = new SingleObserver<BFTModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(BFTModel bftModel) {
                Timber.i("onSuccess singleObserverUpdateBftRefId, updateBftModelRefId. SitRepId: %d", bftId);

                bftModel.setRefId(bftId);
                bFTRepository.updateBFT(bftModel);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverUpdateBftRefId, updateBftModelRefId. Error Msg: %s ", e.toString());
            }
        };

        bFTRepository.queryBFTById(bftId, singleObserverUpdateBftRefId);
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
