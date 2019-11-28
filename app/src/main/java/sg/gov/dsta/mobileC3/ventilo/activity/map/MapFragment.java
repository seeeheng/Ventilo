package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//import com.esri.arcgisruntime.mapping.view.MapView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.data.kml.KmlLayer;
import com.nutiteq.MapView;
import com.nutiteq.components.Components;
import com.nutiteq.components.MapPos;
import com.nutiteq.components.Options;
import com.nutiteq.components.Vector3D;
import com.nutiteq.geometry.Marker;
import com.nutiteq.geometry.NMLModel;
import com.nutiteq.nmlpackage.NMLPackage;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.rasterdatasources.RasterDataSource;
import com.nutiteq.rasterlayers.RasterLayer;
import com.nutiteq.style.MarkerStyle;
import com.nutiteq.style.ModelStyle;
import com.nutiteq.style.StyleSet;
import com.nutiteq.utils.UnscaledBitmapLoader;
import com.nutiteq.vectorlayers.MarkerLayer;
import com.nutiteq.vectorlayers.NMLModelLayer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.helper.MqttHelper;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;
import sg.gov.dsta.mobileC3.ventilo.util.CustomMapTileProvider;
import sg.gov.dsta.mobileC3.ventilo.util.CustomRasterDataSource;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.VoiceCommands;
//import sg.gov.dh.utils.Coords;
//import sg.gov.dh.trackers.Event;
//import sg.gov.dh.trackers.NavisensLocalTracker;
//import sg.gov.dh.trackers.TrackerListener;
import sg.gov.dsta.mobileC3.ventilo.util.map.MapUtil;
import timber.log.Timber;

public class MapFragment extends Fragment implements RecognitionListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final int RESULT_SPEECH = 100;
    public static boolean isShareLocation;
    public static boolean isTrackAllies;
    public static String coordString;
    public static int count;
    private static final String TAG = "MapFragment";
    private static final double SG_LAT_COORD = 1.290270;
    private static final double SG_LON_COORD = 103.851959;
    private static final double VESSEL_LAT_COORD = 1.2941808;
    private static final double VESSEL_LON_COORD = 103.6513086;
    private static final double OWN_MARKER_LAT_COORD = 1.2944446;
    private static final double OWN_MARKER_LON_COORD = 103.6537337;
    private static final String SPACE = " ";

    private View mRootMapView;
    private Bundle mSavedInstanceState;

//    private MqttHelper mMqttHelper;
    // Map
//    private com.esri.arcgisruntime.mapping.view.MapView mArcGISMapView;
    private com.google.android.gms.maps.MapView mGoogleMapView;
    private GoogleMap mGoogleMap;
    private com.google.android.gms.maps.model.Marker mOwnGoogleMapMarker;
    private double mInitialCoordX;
    private double mInitialCoordY;
    private double mInitialCoordZ;

    private MapView mNutiteqMapView;
    private Button mBtnSubscribe;
    private Button mBtnShareLocation;
    //    private Button mBtnPublish;
//    private Button mBtnPublishTwo;

    private Button mBtnTopView;
    private Button mBtnPortView;
    private Button mBtnSBView;
    private StyleSet<ModelStyle> mModelStyleSet;
    private EPSG3857 mProj;
    private NMLModel mVesselModel;
    private NMLModel mHumanModel;
    private Marker mOwnMarker;
    private double mInitialXCoord;
    private double mInitialYCoord;
    private double mInitialZCoord;
//    private MapView mCesiumMapView;

    private static boolean mIsBFTTracking;
    private Button mBtnSpeak;
    private TextView mTvSpeakText;

    private String mResultText;

    // BFT Tracker
//    private NavisensLocalTracker mBftCustomTracker;
//    private PinView mPinView;
//    private PointF mOffsetLocationPoint;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootMapView = inflater.inflate(R.layout.fragment_map, container, false);
//        mCesiumMapView = rootMapView.findViewById(R.id.mapview_cesium_map);
        mSavedInstanceState = savedInstanceState;

        return mRootMapView;
    }

    @Subscribe
    public void onEvent(PageEvent pageEvent)
    {
//        if (MapShipBlueprintFragment.class.getSimpleName().equalsIgnoreCase(pageEvent.getPreviousFragmentName())) {
//            System.out.println("MapFragment found!");
//            onVisible();
////            initUI(mRootMapView, mSavedInstanceState);
//        }
    }

//    private boolean isPreviousFragmentBlueprint() {
//        FragmentManager fm = getFragmentManager();
//
//        Bundle bundle = this.getArguments();
//
//        if (bundle != null) {
//            String fragmentName = bundle.getString(FragmentConstants.KEY_PREVIOUS_FRAGMENT, FragmentConstants.DEFAULT_STRING);
//            if (fragmentName.equalsIgnoreCase(MapShipBlueprintFragment.class.getSimpleName())) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    private void initUI(View rootMapView, Bundle savedInstanceState) {
        // Using 2D Blueprint
//        init2DBlueprint(rootMapView);

        // Google Map
        if (mGoogleMapView == null) {
            initGoogleMap(rootMapView, savedInstanceState);
        }

        // ArcGIS Map
//        initArcGISMap(rootMapView);

        initVoiceRecognition(rootMapView);
//        initNutiteqMap(rootMapView);
        initButtons(rootMapView);
    }

    private void initGoogleMap(View rootMapView, @Nullable Bundle savedInstanceState) {
        mGoogleMapView = rootMapView.findViewById(R.id.map_view_google_map);

        mGoogleMapView.onCreate(savedInstanceState);
        mGoogleMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mGoogleMapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getContext() != null) {
            mGoogleMap = googleMap;
            googleMap.setOnMarkerClickListener(this);

            System.out.println("Map Ready");
//        initLocationService(googleMap);

            setMapStyle(mGoogleMap);
        }

    }

    public void setMapStyle(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(
                new CustomMapTileProvider(getResources().getAssets()))).setZIndex(-1);

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromAsset("background_plane.png");
        LatLng sw = new LatLng(-85.05115, -180);
        LatLng ne = new LatLng(85.05115, 180);
        LatLng nw = new LatLng(85.05115, -180);
        LatLng se = new LatLng(-85.05115, 180);
        LatLngBounds latLngBounds = new LatLngBounds(sw, ne).including(nw).including(se);
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.image(bitmapDescriptor);
        groundOverlayOptions.positionFromBounds(latLngBounds);
        googleMap.addGroundOverlay(groundOverlayOptions);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                System.out.println("MAP CLICK");
                Timber.i("latit is %d", arg0.latitude );
                Timber.i("- longitude is %d" , arg0.longitude);



            }
        });
        // Map background, visible if no map tiles loaded - optional, default - white
//        mNutiteqMapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
//        mNutiteqMapView.getOptions().setBackgroundPlaneBitmap(
//                UnscaledBitmapLoader.decodeResource(getResources(),
//                        R.drawable.background_plane));

        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(new LatLng(VESSEL_LAT_COORD, VESSEL_LON_COORD), 14);
        googleMap.moveCamera(upd);
//        boolean success = googleMap.setMapStyle(new MapStyleOptions(getContext().getApplicationContext().getResources()
//                .getString(mapStyleID)));
//
//        if (!success) {
//            Log.e(TAG, "Style parsing failed.");
//        }

        final ViewGroup googleLogo = (ViewGroup) getView().findViewById(R.id.map_view_google_map).findViewWithTag("GoogleWatermark").getParent();
        googleLogo.setVisibility(View.GONE);


//        System.out.println("first lat lon is " + MapUtil.toWgs84(0, 0));
//        System.out.println("second lat lon is " + MapUtil.toWgs84(1, 1));
//        8.983152840993819E-6

//        addKMLLayer(mGoogleMap, R.raw.deck_2nd);
//        addKMLLayer(mGoogleMap, R.raw.deck_3);
//        addKMLLayer(mGoogleMap, R.raw.main_deck);
//        addKMLLayer(mGoogleMap, R.raw.platform_deck);
//        addKMLLayer(mGoogleMap, R.raw.tween_deck);
        addKMLLayer(mGoogleMap, R.raw.main_deck_projected);
        addOwnMarker(OWN_MARKER_LAT_COORD, OWN_MARKER_LON_COORD);
    }

    // Add KML Layer onto google map
    private void addKMLLayer(GoogleMap map, int imgID) {
        KmlLayer kmlLayer = null;
        try {
            kmlLayer = new KmlLayer(map, imgID, getActivity().getApplicationContext());
//            mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider()));
            kmlLayer.addLayerToMap();

//            Iterator<KmlGroundOverlay> kmlGroundOverlayIterator = kmlLayer.getGroundOverlays().iterator();
//            while (kmlGroundOverlayIterator.hasNext()) {
//                kmlGroundOverlayIterator.next().
//            }
//            mGoogleMap.
//            mGoogleMap.addTileOverlay(new TileOverlayOptions().tileProvider(kmlLayer.getGroundOverlays().forEach();))
//            kmlLayer.getContainers().iterator().next().
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void addOwnMarker(double lat, double lon) {
//        Projection projection = mGoogleMap.getProjection();
//        Point ownPoint = projection.toScreenLocation(new LatLng(lat, lon));
//        mProj = new EPSG3857();
//        MapPos mapPos = mProj.fromWgs84(lon, lat);

        Point ownInitialPoint = MapUtil.fromWgs84(lon, lat);
        mInitialCoordX = ownInitialPoint.x;
        mInitialCoordY = ownInitialPoint.y;
//        mInitialCoordZ = mapPos.z;

        // Instantiating the class MarkerOptions to plot marker on the map
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng markerLatLng = new LatLng(lat, lon);

        markerOptions.position(markerLatLng);
        markerOptions.title("Position");
//        markerOptions.snippet("Latitude:" + point.latitude + "," + "Longitude:" + point.longitude);
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                MapUtil.createCustomMarker(getActivity(), R.drawable.icon_vessel, "Avatar")));

        mOwnGoogleMapMarker = mGoogleMap.addMarker(markerOptions);
    }

    private void updateOwnMarkerPos(double x, double y, double bearing) {
        double newCoordX = mInitialCoordX + x;
        double newCoordY = mInitialCoordY + y;
//        Point newPoint = new Point((int) newCoordX, (int) newCoordY);

//        Projection projection = mGoogleMap.getProjection();
//        LatLng newOwnLatLng = projection.fromScreenLocation(newPoint);
//        mOwnGoogleMapMarker.setPosition(newOwnLatLng);
//mProj.toWgs84(newCoordX, newCoordX)
        mOwnGoogleMapMarker.setPosition(MapUtil.toWgs84(newCoordX, newCoordY));
        mOwnGoogleMapMarker.setRotation((float) bearing);
    }

    private void initArcGISMap(View rootMapView) {
//        mArcGISMapView = rootMapView.findViewById(R.id.mapview_arcgis_map);
//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 1.290270, 103.851959, 16);
//        mArcGISMapView.setMap(map);
    }

    private void initVoiceRecognition(View rootMapView) {
        mBtnSpeak = rootMapView.findViewById(R.id.btn_mic_speak);
        mTvSpeakText = rootMapView.findViewById(R.id.tv_mic_text);
        mBtnSpeak.setOnClickListener(micSpeakOnClickListener());
    }

    private void initButtons(View rootMapView) {
        initMQTTButtons(rootMapView);
        initMapButtons(rootMapView);
    }

    private void initMQTTButtons(View rootMapView) {
        mBtnSubscribe = rootMapView.findViewById(R.id.btn_mqtt_subscribe);

//        mBtnSubscribe.setVisibility(View.VISIBLE);
        mBtnSubscribe.setOnClickListener(subscribeOnClickListener());

//        mBtnPublish = rootMapView.findViewById(R.id.btn_mqtt_publish);
//
//        mBtnPublish.setVisibility(View.VISIBLE);
//        mBtnPublish.setOnClickListener(publishOnClickListener());
//
//        mBtnPublishTwo = rootMapView.findViewById(R.id.btn_mqtt_publish_two);
//
//        mBtnPublishTwo.setVisibility(View.VISIBLE);
//        mBtnPublishTwo.setOnClickListener(publishTwoOnClickListener());

        mBtnShareLocation = rootMapView.findViewById(R.id.btn_mqtt_share_location);

//        mBtnShareLocation.setVisibility(View.VISIBLE);
        mBtnShareLocation.setOnClickListener(shareLocationOnClickListener());
    }

    private void initMapButtons(View rootMapView) {
        mBtnTopView = rootMapView.findViewById(R.id.btn_map_top_view);
        mBtnTopView.setOnClickListener(mapTopOnClickListener());

        mBtnPortView = rootMapView.findViewById(R.id.btn_map_port_view);
        mBtnPortView.setOnClickListener(mapPortOnClickListener());

        mBtnSBView = rootMapView.findViewById(R.id.btn_map_sb_view);
        mBtnSBView.setOnClickListener(mapSBOnClickListener());
    }

    private View.OnClickListener subscribeOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeToMQTTTopic("Ventilo");
            }
        };
    }

//    private View.OnClickListener publishOnClickListener() {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MqttHelper.getInstance().publishMessage("publishhh");
//            }
//        };
//    }
//
//    private View.OnClickListener publishTwoOnClickListener() {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MqttHelper.getInstance().publishMessage("publish two");
//            }
//        };
//    }

    private View.OnClickListener mapTopOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToMapShipBlueprintFragment();
            }
        };
    }

    private View.OnClickListener mapPortOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MapPos mapPos1 = mProj.fromWgs84(103.851959f, 1.290270f);
//                MapPos mapPos = new MapPos(mapPos1.x, mapPos1.y, 0.1f);
//                mNutiteqMapView.setFocusPoint(mapPos);
//                mNutiteqMapView.setMapRotation(270);
//                mNutiteqMapView.setTilt(0);
//                mNutiteqMapView.setZoom(16.0f);

                CameraPosition pos = CameraPosition.fromLatLngZoom(new LatLng(VESSEL_LAT_COORD, VESSEL_LON_COORD), 16);
                CameraPosition posWithBearing = CameraPosition.builder(pos).bearing(180).tilt(90).build();
                CameraUpdate upd = CameraUpdateFactory.newCameraPosition(posWithBearing);
                mGoogleMap.moveCamera(upd);
            }
        };
    }

    private View.OnClickListener mapSBOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MapPos mapPos1 = mProj.fromWgs84(103.851959f, 1.290270f);
//                MapPos mapPos = new MapPos(mapPos1.x, mapPos1.y, 0.1f);
//                mNutiteqMapView.setFocusPoint(mapPos);
//                mNutiteqMapView.setMapRotation(90);
//                mNutiteqMapView.setTilt(0);
//                mNutiteqMapView.setZoom(16.0f);

                CameraPosition pos = CameraPosition.fromLatLngZoom(new LatLng(VESSEL_LAT_COORD, VESSEL_LON_COORD), 16);
                CameraPosition posWithBearing = CameraPosition.builder(pos).bearing(0).tilt(90).build();
                CameraUpdate upd = CameraUpdateFactory.newCameraPosition(posWithBearing);
                mGoogleMap.moveCamera(upd);
            }
        };
    }

    private View.OnClickListener shareLocationOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("isShareLocation to true");
                isShareLocation = true;
            }
        };
    }

    private View.OnClickListener micSpeakOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateMicrophone();
            }
        };
    }

//    private void init2DBlueprint(View rootMapView) {
//        mPinView = rootMapView.findViewById(R.id.img_map_main);
//
//        BlueprintManager.getInstance().initBlueprintList(getContext());
//
//        for (Blueprint blueprint : BlueprintManager.getInstance().getBlueprintList()) {
//            Integer resID = blueprint.getResID();
//            String resName = blueprint.getResName();
//
//            if ("map_heli_deck_116_5m_x_27_3m_height_45p5_length_68_to_225".equalsIgnoreCase(resName)) {
//                mPinView.setImage(ImageSource.resource(resID));
//                BlueprintManager.getInstance().setDisplayedBlueprint(blueprint);
//
//                Pin pin2 = new Pin();
//                pin2.setPinID(2);
//                pin2.setBearing(180.0f);
//                pin2.setPinInitialLocationPoint(new PointF(300.0f, 300.0f));
//                pin2.setPinCurrentLocationPoint(new PointF(300.0f, 300.0f));
//
//                mPinView.addPin(pin2.getPinID(), pin2);
//            }
//        }
//    }

    private void initNutiteqMap(View rootMapView) {
        mNutiteqMapView = rootMapView.findViewById(R.id.mapview_nutiteq);

        /** Nutiteq Map **/
        // Optional, but very useful: restore map state during device rotation,
        // it is saved in onRetainNonConfigurationInstance() below
//        Components retainObject = (Components) getLastNonConfigurationInstance();
//        if (retainObject != null) {
//            // just restore configuration, skip other initializations
//            mNutiteqMapView.setComponents(retainObject);
////            return;
//        } else {
//            // 2. create and set MapView components - mandatory
//            Components components = new Components();
//            // set stereo view: works if you rotate to landscape and device has HTC 3D or LG Real3D
//            mNutiteqMapView.setComponents(components);
//        }

        Components components = new Components();
        mNutiteqMapView.setComponents(components);

        // 3. Define map layer for basemap - mandatory.
        // Almost all online maps use EPSG3857 projection.
        this.mProj = new EPSG3857();

        // 1. define individual data sources
        RasterDataSource offlineMapDataSource = new CustomRasterDataSource(new EPSG3857(),
                2, 16, getActivity(), 1);

//        RasterDataSource onlineDataSource = new HTTPRasterDataSource(new EPSG3857(), 0, 18,
//                "android.resource://raw/z_{zoom}/{x}/{y}.png");

//        System.out.println("android.resource://" + getActivity().getPackageName() + "/raw/z_{zoom}/{x}/{y}.png");
//        // 2. define combined data source
//        RasterDataSource dataSource = new FallbackRasterDataSource(offlineDataSource, onlineDataSource);
        RasterLayer mapLayer = new RasterLayer(offlineMapDataSource, 0);
        mNutiteqMapView.getLayers().setBaseLayer(mapLayer);

        // define style for 3D to define minimum zoom = 14
        ModelStyle modelStyle = ModelStyle.builder().build();
        mModelStyleSet = new StyleSet<ModelStyle>(null);
        mModelStyleSet.setZoomStyle(15, modelStyle);

        // rotation - 0 = north-up
        mNutiteqMapView.setMapRotation(0f);
        // tilt means perspective view. Default is 90 degrees for "normal" 2D map view, minimum allowed is 30 degrees.
        mNutiteqMapView.setTilt(90.0f);

        // Activate some mapview options to make it smoother - optional
        mNutiteqMapView.getOptions().setPreloading(false);
        mNutiteqMapView.getOptions().setSeamlessHorizontalPan(true);
        mNutiteqMapView.getOptions().setTileFading(false);
        mNutiteqMapView.getOptions().setKineticPanning(true);
        mNutiteqMapView.getOptions().setDoubleClickZoomIn(true);
        mNutiteqMapView.getOptions().setDualClickZoomOut(true);


//        Range range = new Range(2, 16);
//        mNutiteqMapView.getLayers().getBaseLayer().setVisibleZoomRange(range);

//        Bounds
//        Bounds allowedBounds = mNutiteqMapView.getLayers().getBaseProjection().getBounds();
//        if (allowedBounds.)
//        getBaseProjection().getBounds().


        // set sky bitmap - optional, default - white
        mNutiteqMapView.getOptions().setSkyDrawMode(Options.DRAW_BITMAP);
        mNutiteqMapView.getOptions().setSkyOffset(4.86f);
        mNutiteqMapView.getOptions().setSkyBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.sky_small));

        // Map background, visible if no map tiles loaded - optional, default - white
        mNutiteqMapView.getOptions().setBackgroundPlaneDrawMode(Options.DRAW_BITMAP);
        mNutiteqMapView.getOptions().setBackgroundPlaneBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(),
                        R.drawable.background_plane));
        mNutiteqMapView.getOptions().setClearColor(Color.WHITE);

        // configure texture caching - optional, suggested
        mNutiteqMapView.getOptions().setTextureMemoryCacheSize(20 * 1024 * 1024);
        mNutiteqMapView.getOptions().setCompressedMemoryCacheSize(8 * 1024 * 1024);

        // define online map persistent caching - optional, suggested. Default - no caching
//        mNutiteqMapView.getOptions().setPersistentCachePath(getActivity().getDatabasePath("mapcache").getPath());
        // set persistent raster cache limit to 100MB
//        mNutiteqMapView.getOptions().setPersistentCacheSize(100 * 1024 * 1024);

        // 4. zoom buttons using Android widgets - optional
        // get the zoomcontrols that was defined in main.xml
//        ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoomcontrols);
//        // set zoomcontrols listeners to enable zooming
//        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
//            public void onClick(final View v) {
//                mapView.zoomIn();
//            }
//        });
//        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
//            public void onClick(final View v) {
//                mapView.zoomOut();
//            }
//        });

        // set initial map view camera from database
//        Envelope extent = mapLayer.getProjection().getBounds()..getDataExtent();

        // or you can just set map view bounds directly
//        mNutiteqMapView.setBoundingBox(new Bounds(extent.minX, extent.maxY, extent.maxX, extent.minY), false);
        mNutiteqMapView.setBoundingBox(mapLayer.getProjection().getBounds(), false);

        add2DBlueprintLayers();
        addModel(R.raw.milktruck, false);
        addModel(R.raw.man3d, true);
        addMarker();
    }

    /**
     * Adds a 2D raster (image) layers
     */
    private void add2DBlueprintLayers() {
        add2DBlueprintLayer();
    }

    /**
     * Adds a 2D raster (image) layer
     */
    private void add2DBlueprintLayer() {
        RasterDataSource vesselBlueprintDataSource = new CustomRasterDataSource(mProj,
                2, 16, getActivity(), 2);
//        VectorDataSource vesselBlueprintVectorDataSource =
//        SimplifierVectorDataSource vesselBlueprintDataSource = new SimplifierVectorDataSource();
        RasterLayer vesselBlueprintLayer = new RasterLayer(vesselBlueprintDataSource, 1);
        mNutiteqMapView.getLayers().addLayer(vesselBlueprintLayer);
    }

    /**
     * Adds a 3D Model layer to map
     */
    private void addModel(int modelID, boolean isHumanModel) {
        try {
//            Bundle b = getActivity().getIntent().getExtras();
//            String mapFile = b.getString("selectedFile");

            addNml(new BufferedInputStream(getActivity().getResources().openRawResource(modelID)),
                    isHumanModel);

//            if(mapFile.endsWith("nml")){
//                // single model nml file
//                System.out.print("nmlnmlnml");
//                addNml(new BufferedInputStream(getActivity().getResources().openRawResource(
//                        R.raw.milktruck)));
//
//            }else if(mapFile.endsWith("dae") || mapFile.endsWith("zip")){
//                // convert dae to NML using online API
////                new DaeConverterServiceTask(this, mapFile).execute(IOUtils.readFully(new BufferedInputStream(new FileInputStream(new File(mapFile)))));
//
//            }else{
//                // nmlDB, if sqlite or nmldb file extension
//                addNmlDb(mapFile);
//
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a Marker to map
     */
    private void addMarker() {
        // add a layer for Markers, do only once during map initialization
        MarkerLayer markerLayer = new MarkerLayer(mNutiteqMapView.getLayers().getBaseProjection());
        mNutiteqMapView.getLayers().addLayer(markerLayer);

        // define style for Marker. Here one style for all Markers, so need to do only once
        MarkerStyle markerStyle = MarkerStyle.builder().setSize(0.5f).setBitmap(
                UnscaledBitmapLoader.decodeResource(getResources(), R.drawable.marker_blue)
        ).build();

        MapPos pos = mProj.fromWgs84(SG_LON_COORD, SG_LAT_COORD);
        // create marker and add to map
        mOwnMarker = new Marker(pos, null, markerStyle, "Any Object as extra data, can be String or null");
        markerLayer.add(mOwnMarker);
    }

//    private void addNmlDb(String mapFile) throws IOException {
//
//        NMLModelDbLayer modelLayer = new NMLModelDbLayer(mProj,
//                mapFile, mModelStyleSet);
//        modelLayer.setMemoryLimit(20 * 1024 * 1024);
//        mNutiteqMapView.getLayers().addLayer(modelLayer);
//
//        // set initial map view camera from database
//        Envelope extent = modelLayer.getDataExtent();
//
//        // or you can just set map view bounds directly
//        mNutiteqMapView.setBoundingBox(new Bounds(extent.minX, extent.maxY, extent.maxX, extent.minY), false);
//    }

    /**
     * Adds Nml to map
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void addNml(InputStream is, boolean isHumanModel)
            throws FileNotFoundException, IOException {

        // create layer and an model
        MapPos mapPos1 = mProj.fromWgs84(103.851959f, 1.290270f);

        // set it to fly a bit with Z = 0.1f
        MapPos mapPos = new MapPos(mapPos1.x, mapPos1.y, 0.1f);
        NMLModelLayer nmlModelLayer = new NMLModelLayer(mProj);
        mNutiteqMapView.getLayers().addLayer(nmlModelLayer);

        NMLPackage.Model nmlModel = NMLPackage.Model.parseFrom(new BufferedInputStream(is));
        // set initial position for the milk truck
        com.nutiteq.log.Log.debug("nmlModel loaded");

        if (!isHumanModel) {
            mVesselModel = new NMLModel(mapPos, null, mModelStyleSet, nmlModel, null);

            // set size, 10 is clear oversize, but this makes it visible
            mVesselModel.setScale(new Vector3D(100, 100, 100));
            nmlModelLayer.add(mVesselModel);
        } else {
            // set it to fly a bit with Z = 0.1f
            mapPos = new MapPos(mapPos1.x, mapPos1.y, 4f);
            mInitialXCoord = mapPos.x;
            mInitialYCoord = mapPos.y;
            mInitialZCoord = mapPos.z;
            mHumanModel = new NMLModel(mapPos, null, mModelStyleSet, nmlModel, null);

            // set size, 10 is clear oversize, but this makes it visible
            mHumanModel.setScale(new Vector3D(10, 10, 10));
            nmlModelLayer.add(mHumanModel);
        }

        mNutiteqMapView.setFocusPoint(mapPos);
        mNutiteqMapView.setTilt(45);
        mNutiteqMapView.setZoom(16.0f);
    }

    private void activateMicrophone() {
        SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speech.setRecognitionListener(this);

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getActivity().getPackageName());

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

//        if (speech.g)
        speech.startListening(recognizerIntent);

//        try {
//
//            startActivityForResult(recognizerIntent, RESULT_SPEECH);
//        } catch (ActivityNotFoundException a) {
//
//            Toast.makeText(getActivity().getApplicationContext(),
//                    "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode) {

        switch (errorCode) {

            case SpeechRecognizer.ERROR_AUDIO:

                mResultText = "Error - audio";

                break;

            case SpeechRecognizer.ERROR_CLIENT:

                mResultText = "Error - client";

                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

                mResultText = "Error - insufficient permissions";

                break;

            case SpeechRecognizer.ERROR_NETWORK:

                mResultText = "Error - network";

                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:

                mResultText = "Error - network timeout";

                break;

            case SpeechRecognizer.ERROR_NO_MATCH:

                mResultText = "Error - no match";

                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

                mResultText = "Error - recogniser busy";

                break;

            case SpeechRecognizer.ERROR_SERVER:

                mResultText = "Error - server";

                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

                mResultText = "Error - speech timeout";

                break;

            default:

                mResultText = "Error - do not understand";

                break;
        }

        mTvSpeakText.setText(mResultText);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        mTvSpeakText.setText("text is " + VoiceCommands.getPredictedWords(matches));

        Timber.i("SPEAK %s", matches.toString());

}

    @Override
    public void onRmsChanged(float rmsdB) {

    }


//    @Override
//    public void onMapClicked(double x, double y, boolean b) {
//        Toast.makeText(activity, "onMapClicked " + (new EPSG3857()).toWgs84(x, y).x + " " + (new EPSG3857()).toWgs84(x, y).y, Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "onMapClicked " + x + " " + y);
//        activity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mPoiGlanceView.setVisibility(View.INVISIBLE);
//            }
//        });
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case RESULT_SPEECH: {
//                if (resultCode == RESULT_OK && null != data) {
//                    ArrayList<String> text = data
//                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    mTvSpeakText.setText("text is " + text);
//                    Log.d("ACTIVITY SPEAK", "activity speaking");
//                }
//
//                break;
//            }
//        }
//    }

//    private void update2DBlueprintBFCoord(Coords coords) {
//        BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInPixels();
//        float pixelPerMetreScaleFactor = MapDistanceConversionUtil.getScaledImagePixelsPerMetre(
//                BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInPixels(),
//                BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInMetres());
//
//        mPinView.setOwnPinBearing((float) coords.getBearing());
//        setOffsetLocationPoint(((float) coords.getX() * pixelPerMetreScaleFactor),
//                ((float) -coords.getY() * pixelPerMetreScaleFactor));
//        mPinView.setOwnPinCoordinates(mOffsetLocationPoint);
//    }

//    private void updateHumanModelCoord(double x, double y, double altitude, double bearing) {
//        double newX = mInitialXCoord + x;
//        double newY = mInitialYCoord + y;
//        double newZ = mInitialZCoord + altitude;
//        MapPos newPos = new MapPos(newX, newY, newZ);
//
////        mHumanModel.setMapPos(newPos);
////        mHumanModel.setRotation(mHumanModel.getRotationAxis(), (float) bearing);
//    }
//
//    private void updateHumanModelCoord(Coords coords) {
//        double newX = mInitialXCoord + coords.getX();
//        double newY = mInitialYCoord + coords.getY();
//        double newZ = mInitialZCoord + coords.getAltitude();
//        MapPos newPos = new MapPos(newX, newY, newZ);
//
////        mHumanModel.setMapPos(newPos);
////        mHumanModel.setRotation(mHumanModel.getRotationAxis(), (float) coords.getBearing());
//    }
//
////    private void setOffsetLocationPoint(float centerX, float centerY) {
////        if (mOffsetLocationPoint == null) {
////            mOffsetLocationPoint = new PointF(centerX, centerY);
////        } else {
////            mOffsetLocationPoint.set(centerX, centerY);
////        }
////    }
//
////    // Note that this NavisensLocalTracker class can only register XYZ offsets in metres, NOT lat/long.
////    private void startBFTTracking() {
////        if (!mIsBFTTracking) {
////            mBftCustomTracker = new NavisensLocalTracker(getActivity());
//////            mBftCustomTracker.setActive(true);
////            mBftCustomTracker.setTrackerListener(new TrackerListener() {
////                @Override
////                public void onNewCoords(Coords coords) {
////                    //You receive the coordinates here, you can do whatever you want with it.
////                    Log.d(TAG, "X:" + coords.getX());
////                    Log.d(TAG, "Y:" + coords.getY());
////                    Log.d(TAG, "Z:" + coords.getAltitude());
////                    Log.d(TAG, "bearing:" + coords.getBearing());
////                    Log.d(TAG, "Action:" + coords.getAction());
////                    Log.d(TAG, "Lat:" + coords.getLatitude());
////
////                    // Update human pin coordinates on 2D image
//////                    update2DBlueprintBFCoord(coords);
////
////                    // Update human 3D model coordinates
////                    if (isShareLocation) {
////                        System.out.println("isShareLocation");
////                        StringBuilder sb = new StringBuilder();
////                        sb.append("Coords:");
////                        sb.append(SPACE);
////                        sb.append(coords.getX());
////                        sb.append(SPACE);
////                        sb.append(coords.getY());
////                        sb.append(SPACE);
////                        sb.append(coords.getAltitude());
////                        sb.append(SPACE);
////                        sb.append(coords.getBearing());
////
////
////                        count++;
////                        if (count > 4) {
//////                            MqttHelper.getInstance().publishMessage("PAYLOAD_RELOADED_2");
////                            MqttHelper.getInstance().publishMessage(sb.toString());
////                            count = 0;
////                        }
////                    } else if (isTrackAllies) {
////                        System.out.println("isTrackAllies");
//////                        String mapCoord = getArguments().getCharSequence("mapCoord").toString();
////
//////                        Bundle mapCoordBundle = getActivity().getIntent().getExtras();
//////
//////                        if (mapCoordBundle != null) {
//////
//////                            String mapCoord = mapCoordBundle.getString("mapCoord");
//////                            if (mapCoord != null) {
////                        if (!coordString.isEmpty()) {
////                            System.out.println("coordString is " + coordString);
////
////                            String mapCoordSplit[] = coordString.split(SPACE);
//////
////                            System.out.println("final coord is :" + Double.valueOf(mapCoordSplit[1]) + " " +
////                                    Double.valueOf(mapCoordSplit[2]) + " " + Double.valueOf(mapCoordSplit[3]) +
////                                    " " + Double.valueOf(mapCoordSplit[4]));
////
//////                            updateHumanModelCoord(Double.valueOf(mapCoordSplit[1]), Double.valueOf(mapCoordSplit[2]),
//////                                    Double.valueOf(mapCoordSplit[3]), Double.valueOf(mapCoordSplit[4]));
////
////                            //Update Google Map own marker
////                            updateOwnMarkerPos(Double.valueOf(mapCoordSplit[1]),
////                                    Double.valueOf(mapCoordSplit[2]), Double.valueOf(mapCoordSplit[4]));
////                        } else {
//////                            updateHumanModelCoord(coords);
////                            updateOwnMarkerPos(coords.getX(), coords.getY(), coords.getBearing());
////                        }
//////                    }
////
//////                        else {
//////                            updateHumanModelCoord(coords);
//////                        }
////
////                    } else {
////                        System.out.println("none");
////                        updateOwnMarkerPos(coords.getX(), coords.getY(), coords.getBearing());
////                    }
////
//////                    if (mMqttHelper == null) {
//////                        mMqttHelper = (MqttHelper) getArguments().getSerializable("mqttHelper");
//////
//////                        if (mMqttHelper == null) {
//////                            updateHumanModelCoord(coords);
//////                        } else if (mMqttHelper.getMqttClient().isConnected()) {
//////                            String mapCoord = getArguments().getCharSequence("mapCoord").toString() ;
//////                            if (mapCoord != null || "".equalsIgnoreCase(mapCoord)) {
//////                                String mapCoordSplit[] = mapCoord.split(" ");
//////
//////                                updateHumanModelCoord(Double.valueOf(mapCoordSplit[0]), Double.valueOf(mapCoordSplit[1]),
//////                                        Double.valueOf(mapCoordSplit[2]), Double.valueOf(mapCoordSplit[3]));
//////                            }
//////                        }
//////                    }
////                }
////
////                @Override
////                public void onNewEvent(Event event) {
////                    //This is deprecated in current version. Replaced by coords.getAction().
////                }
////            });
////
////            mIsBFTTracking = true;
////        }
////
////    }
////
////    private void stopBFTTracking() {
////        if (mIsBFTTracking && mBftCustomTracker != null && mBftCustomTracker.isActive()) {
////            mBftCustomTracker.deactivate();
////            mBftCustomTracker = null;
////            mIsBFTTracking = false;
////        }
////    }

    private void subscribeToMQTTTopic(String topic) {
        MqttHelper.getInstance().subscribeToTopic(topic);
    }

    private void navigateToMapShipBlueprintFragment() {
        dispose();

        popChildBackStack();


//        Fragment mapShipBlueprintFragment = new MapShipBlueprintFragment();
////        FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentManager fm = getChildFragmentManager();
//
//        int count = fm.getBackStackEntryCount();
//        boolean isMapShipBlueprintFragmentFound = false;
//
//        // Checks if MapShipBlueprintFragment was previous fragment; if it is, simply pop current stack
//        // By popping stack, we do not re-initialise blueprint in MapShipBlueprintFragment
//        for (int i = 0; i < count; i++) {
//            if (MapShipBlueprintFragment.class.getSimpleName().equalsIgnoreCase(
//                    fm.getBackStackEntryAt(i).getName())) {
//                if (i == (count - 2) || i == (count - 3)) {
//                    isMapShipBlueprintFragmentFound = true;
//                }
//            }
//        }
//
//        if (!isMapShipBlueprintFragmentFound) {
//            Log.d(TAG, "MapShipBlueprintFragment not found in back stack. Creating new map fragment.");
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right,
//                    R.anim.slide_in_from_right, R.anim.slide_out_to_right);
//            ft.replace(R.id.layout_map_fragment, mapShipBlueprintFragment,
//                    mapShipBlueprintFragment.getClass().getSimpleName());
////            ft.addToBackStack(mapShipBlueprintFragment.getClass().getSimpleName());
//            ft.addToBackStack(null);
//            ft.commit();
//        } else {
//            Log.d(TAG, "MapShipBlueprintFragment found in back stack. Popping " +
//                    MapShipBlueprintFragment.class.getSimpleName() + ".");
////            fm.popBackStack();
//            popBackStack();
//        }
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     *
     * Selected View Pager Item: Map
     * Child Base Fragment: MapShipBlueprintFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.popChildFragmentBackStack(
                    MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID);
        }
    }

//    public class DaeConverterServiceTask extends AsyncTask<byte[], Void, InputStream> {
//
//        private MapFragment offlineActivity;
//        private String mapFile;
//
//        public DaeConverterServiceTask(MapFragment offlineActivity, String mapFile){
//            this.offlineActivity = offlineActivity;
//            // replace .zip -> .dae in end of dae file name, and remove folder
//            this.mapFile = mapFile.substring(mapFile.lastIndexOf("/")+1, mapFile.length()-4);
//        }
//
//        protected InputStream doInBackground(byte[]... dae) {
//
//            String url = "http://aws-lb.nutiteq.com/daeconvert/?key=Aq7M28a93Huik&dae="+mapFile+".dae&max-single-texture-size=2048";
//            com.nutiteq.log.Log.debug("connecting "+url);
//            ByteArrayEntity daeEntity;
//            daeEntity = new ByteArrayEntity(dae[0]);
//
//            InputStream nmlStream = NetUtils.postUrlasStream(url, null, false, daeEntity);
//            return nmlStream;
//        }
//
//        protected void onPostExecute(InputStream nml) {
//            try {
//                offlineActivity.addNml(nml);
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        if(isVisibleToUser) {
//            Activity a = getActivity();
//            if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
//    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (hidden) {
//            Log.d(TAG, ((Object) this).getClass().getSimpleName() + " is NOT on screen");
//        }
//        else
//        {
//            Log.d(TAG, ((Object) this).getClass().getSimpleName() + " is on screen");
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
//
////        System.out.println("mapFragment onResume");
////        if (this != null && this.isVisible()) {
////            System.out.println("mapFragment visible");
////            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////        }
////        else {
////            //Whatever
////        }
//    }

    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
        if (marker.equals(mOwnGoogleMapMarker)) {
            navigateToMapShipBlueprintFragment();
            return true;
        }

        return false;
    }

    public void onVisible() {
        if (mGoogleMapView != null) {
            mGoogleMapView.setVisibility(View.VISIBLE);
            mGoogleMapView.onResume();
            System.out.println("MapFragment not null mGoogleMapView!");
        } else {
            initUI(mRootMapView, mSavedInstanceState);
            System.out.println("MapFragment null mGoogleMapView!");
        }
        Timber.i("onVisible");


//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        boolean isFragmentFound = false;
//
//        int count = fm.getBackStackEntryCount();
//
//        // Checks if current fragment exists in Back stack
//        for (int i = 0; i < count; i++) {
//            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
//                isFragmentFound = true;
//            }
//        }
//
//        // If not found, add to current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
    }

    private void onInvisible() {
        dispose();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleMapView != null) {
            mGoogleMapView.onPause();
        }

        // To suspend map rendering while the activity is paused, which can save battery usage.
        if (mNutiteqMapView != null) {
            mNutiteqMapView.stopMapping();
        }

//        stopBFTTracking();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onVisible();
//        if (mGoogleMapView != null) {
//            mGoogleMapView.onResume();
//        } else {
//
//        }

        // To resume map rendering when the activity returns to the foreground.
//        if (mNutiteqMapView != null) {
//            mNutiteqMapView.startMapping();
//        }

//        startBFTTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispose();
    }

    private synchronized void dispose() {
        if (mGoogleMapView != null) {
            mGoogleMapView.setVisibility(View.GONE);
        }

        // To suspend map rendering while the activity is paused, which can save battery usage.
        if (mNutiteqMapView != null) {
            mNutiteqMapView.stopMapping();
        }

//        stopBFTTracking();

        mNutiteqMapView = null;
        mProj = null;
        mVesselModel = null;
        mHumanModel = null;
        mOwnMarker = null;
    }
//    @Override
//    public void onInitMQTT(MqttHelper mqttHelper) {
//        mMqttHelper = mqttHelper;
//    }
}
