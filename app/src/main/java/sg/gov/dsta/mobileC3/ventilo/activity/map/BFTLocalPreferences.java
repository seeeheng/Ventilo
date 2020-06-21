package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.repository.MapRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.FileUtil;
import timber.log.Timber;

public class BFTLocalPreferences {

    private Context context = null;
    private SharedPreferences prefs = null;

    private ArrayList<String> floors = new ArrayList();
    private int currentFloor = 0;
    //    private double mapScale = 44.98599; // For Indoor Range; 1cm to 250cm (Avatar), 1cm to 300cm (BW Paris), 1cm to 44.98599 (Indoor Range)
//    private static double mapScale = 250; // For Avatar; 1cm to 250cm (Avatar), 1cm to 300cm (BW Paris), 1cm to 44.98599 (Indoor Range)
    private static double mapScale = 100; // For Avatar; 1cm to 250cm (Avatar), 1cm to 300cm (BW Paris), 1cm to 44.98599 (Indoor Range), 1cm to 100cm (Fast Map)
    //    private double mapScale = 300; // For BW Paris; 1cm to 250cm (Avatar), 1cm to 300cm (BW Paris), 1cm to 44.98599 (Indoor Range)
//    private double onePixelToMetres = (82.07 / 3102 * mapScale / 100); // For Indoor Range; 0.021 (Avatar), 0.05291 (BW Paris), 0.011902 (Indoor Range)
//    private static double onePixelToMetres = (11.82 / 1400 * mapScale / 100); // For Avatar; 0.021 (Avatar), 0.05291 (BW Paris), 0.011902 (Indoor Range)
//    private double onePixelToMetres = (41.45 / 2350 * mapScale / 100); // For BW Paris; 0.021 (Avatar), 0.05291 (BW Paris), 0.011902 (Indoor Range)
//    private static double onePixelToMetres = (5.93 / 168 * mapScale / 100); // For Avatar-Fastmap; 0.021 (Avatar), 0.05291 (BW Paris), 0.011902 (Indoor Range)
    private static double onePixelToMetres = 0.1; // For Avatar-Fastmap; 0.021 (Avatar), 0.05291 (BW Paris), 0.011902 (Indoor Range)

//    private double onePixelToMetres;

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

//        // TODO: Uncomment this after test
        setGaScale();

        //Ground floor up
//        floors.add(0, "leaflet-indoor-range.html");

//        //Ground floor up
        floors.add(0, "leaflet-avatar-deck3rd.html");
        floors.add(1, "leaflet-avatar-deck2nd.html");
        floors.add(2, "leaflet-avatar-decktween.html");
        floors.add(3, "leaflet-avatar-maindeck.html");
        floors.add(4, "leaflet-avatar-deck2.html");
        floors.add(5, "leaflet-avatar-deck3.html");
        floors.add(6, "leaflet-avatar-deck4.html");
        floors.add(7, "leaflet-avatar-deck5.html");
        floors.add(8, "leaflet-avatar-deck6.html");
        floors.add(9, "leaflet-avatar-deck7.html");
        floors.add(10, "leaflet-avatar-deckplatform.html");
        floors.add(11, "leaflet-avatar-lateral.html");

        //Ground floor up
//        floors.add(0, "leaflet-bw-paris-tank-top.html");
//        floors.add(1, "leaflet-bw-paris-a-deck.html");
//        floors.add(2, "leaflet-bw-paris-upper-deck.html");
//        floors.add(3, "leaflet-bw-paris-b-deck.html");
//        floors.add(4, "leaflet-bw-paris-c-deck.html");
//        floors.add(5, "leaflet-bw-paris-d-deck.html");
//        floors.add(6, "leaflet-bw-paris-nav-bri-deck.html");
//        floors.add(7, "leaflet-bw-paris-midship.html");
//        floors.add(8, "leaflet-bw-paris-profile.html");
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

    private int getReferenceMapHeightInPixel() {
        File[] mapFiles = FileUtil.getAllFilesInMapBlueprintImagesFolder();

        for (File mapFile : mapFiles) {
            if (mapFile.isFile()) {
//                System.out.println("File " + mapFile.getName());
                Bitmap mapBitmap = FileUtil.getBitmapFromFile(mapFile.getAbsolutePath());

                if (mapBitmap != null && mapBitmap.getWidth() != 0) {
                    return mapBitmap.getWidth();
                }
            }
        }

        return 0;
    }

    /**
     * Set one pixel to metres measurement factor based on map blueprint in external folder
     *
     * @param gaScale
     */
    public void setOnePixelToMetresFromExternalFolder(float gaScale) {

        int referenceMapHeightInPixel = getReferenceMapHeightInPixel();

        if (referenceMapHeightInPixel != 0) {
            mapScale = gaScale;
//            onePixelToMetres = (41.45 / 2350 * mapScale / 100);
            double referenceMapHeightInCm = DimensionUtil.convertPixelToCm(referenceMapHeightInPixel);
            onePixelToMetres = (referenceMapHeightInCm / referenceMapHeightInPixel) * mapScale / 100;

        } else {
//            mapScale = 250;
//            onePixelToMetres = (11.82 / 1400 * mapScale / 100);

//            mapScale = 100;
//            onePixelToMetres = (5.93 / 168 * mapScale / 100);

            mapScale = 0.1;
            onePixelToMetres = 0.1;
        }
    }

    /**
     * Extract GA scale from the first map model in local database
     */
    public void setGaScale() {

        MapRepository mapRepo = new MapRepository((Application) MainApplication.getAppContext());

        // Access Map model to get GA scale of image
        SingleObserver<List<MapModel>> singleObserverGetAllMaps = new SingleObserver<List<MapModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<MapModel> mapModelList) {

                Timber.i("onSuccess singleObserverGetAllMaps, setGaScale. mapModelList.size(): %s", mapModelList.size());

                if (mapModelList.size() > 0) {

                    int referenceMapHeightInPixel = getReferenceMapHeightInPixel();

                    if (referenceMapHeightInPixel != 0) {
                        mapScale = Float.valueOf(mapModelList.get(0).getGaScale());
//                        onePixelToMetres = (41.45 / 2350 * mapScale / 100);
                        double referenceMapHeightInCm = DimensionUtil.convertPixelToCm(referenceMapHeightInPixel);
                        onePixelToMetres = (referenceMapHeightInCm / referenceMapHeightInPixel) * mapScale / 100;
                    }

                }

            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverGetAllMaps, setGaScale. Error Msg: %s", e.toString());
            }
        };

        mapRepo.getAllMaps(singleObserverGetAllMaps);
    }

    /**
     * TODO: Temporary solution to include Indoor Range and Avatar maps; Remove after demo
     * Set one pixel to metres measurement factor based on map blueprint in external folder
     *
     * @param mapName
     */
    public void setOnePixelToMetresFromSelectedMapName(String mapName) {

        String[] indoorRangeMapNamePrefixArray = {"indoor", "range"};

        boolean allFound = true;
        for (String s : indoorRangeMapNamePrefixArray)
        {
            if (!mapName.trim().contains(s))
            {
                allFound = false;
                break;
            }
        }

        if (allFound) {
            mapScale = 44.98599;
            onePixelToMetres = (82.07 / 3102 * mapScale / 100);

        } else {
//            mapScale = 250;
//            onePixelToMetres = (11.82 / 1400 * mapScale / 100);

//            mapScale = 100;
//            onePixelToMetres = (5.93 / 168 * mapScale / 100);

            mapScale = 0.1;
            onePixelToMetres = 0.1;
        }
    }

    public static double getMetresFromPixels(double pixels) {
        double metres = pixels * onePixelToMetres;
        return metres;
    }
}
