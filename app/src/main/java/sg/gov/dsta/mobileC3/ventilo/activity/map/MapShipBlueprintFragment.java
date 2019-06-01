package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nutiteq.components.Color;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import sg.gov.dh.beacons.BeaconListener;
import sg.gov.dh.beacons.BeaconManagerInterface;
import sg.gov.dh.beacons.BeaconObject;
import sg.gov.dh.beacons.DroppedBeacon;
import sg.gov.dh.beacons.estimote.EstimoteBeaconManager;
import sg.gov.dh.trackers.Event;
import sg.gov.dh.trackers.NavisensLocalTracker;
import sg.gov.dh.trackers.TrackerListener;
import sg.gov.dh.utils.Coords;
import sg.gov.dh.utils.FileSaver;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;
import sg.gov.dsta.mobileC3.ventilo.util.JSONUtil;

public class MapShipBlueprintFragment extends Fragment {

    private static final String TAG = "BFTLOCAL";
    private static final String LOCAL_SHIP_BLUEPRINT_DIRECTORY = "file:///android_asset/ship/";
    //    private BottomNavigationView mBottomNavigationView;
    private EditText mTextXYZ;
    private EditText mTextBearing;
    private EditText mTextAction;

    // Killed In Action (KIA)/Hazard and Deceased UI
    RelativeLayout mRelativeLayoutHazardImgBtn;
    AppCompatImageView mImgHazardIcon;
    RelativeLayout mRelativeLayoutDeceasedImgBtn;
    AppCompatImageView mImgDeceasedIcon;

    //Somehow need to think of a way to make these 2 variables eternal
    double currentHeight = 0.0;
    double mapHeight = 0.0;
    double newHeight = 0.0;
    double currentOffset = 0.0;
    String SOUND_BEACON_DETECT = "to-the-point.mp3";
    String SOUND_BEACON_DROP = "drop.mp3";
    RabbitMQ mqRabbit;
    private FileSaver fs;
    private NavisensLocalTracker tracker;
    private WebView myWebView;
    private BFTLocalPreferences prefs;
    BeaconManagerInterface beaconManager;
    private BeaconZeroing beaconZeroing;

    private IMQListener mIMQListener;
    private boolean mIsFragmentVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootMapShipBlueprintView = inflater.inflate(R.layout.fragment_map_ship_blueprint, container, false);
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
        mTextXYZ = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_textXYZ);
        mTextBearing = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_textBearing);
        mTextAction = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_textAction);

        initSideButtons(rootMapShipBlueprintView);

        prefs = new BFTLocalPreferences(this.getContext());

        myWebView = rootMapShipBlueprintView.findViewById(R.id.bft_webview);
        
        final ImageView imgUpDeck = rootMapShipBlueprintView.findViewById(R.id.img_ship_blueprint_upButton);
        imgUpDeck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getNextFloorUp());
            }
        });

        final ImageView imgDownDeck = rootMapShipBlueprintView.findViewById(R.id.img_ship_blueprint_downButton);
        imgDownDeck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getNextFloorDown());
            }
        });

        final ImageView imgZeroDeck = rootMapShipBlueprintView.findViewById(R.id.img_ship_blueprint_zeroButton);
        imgZeroDeck.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getOverview());
            }
        });

        final ImageView imgWorldMap = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_world_map);
        imgWorldMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MapFragment mapFragment = new MapFragment();
                navigateToFragment(mapFragment);
            }
        });

        final ImageView imgSetting = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_setting);
        imgSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

//        myWebView.setOnTouchListener(webViewOnTouchListener);
//        myWebView.setOnLongClickListener(webViewOnLongClickListener);

        return rootMapShipBlueprintView;
    }
    
    private void initWebviewSettings() {
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getOverview());
        myWebView.addJavascriptInterface(new WebAppInterface(this.getActivity(), prefs, tracker), "Android");

        setupMessageQueue();
        setupFileSaver();
    }

    private void initSideButtons(View rootMapShipBlueprintView) {
        View layoutMainHazardImgBtn = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint_fragment_hazard_icon);
        mRelativeLayoutHazardImgBtn = layoutMainHazardImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgHazardIcon = layoutMainHazardImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgHazardIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_kia, null));
        setHazardIconUnselectedStateUI();

        mRelativeLayoutHazardImgBtn.setOnClickListener(onHazardIconClickListener);

        View layoutDeceasedImgBtn = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint_fragment_deceased_icon);
        mRelativeLayoutDeceasedImgBtn = layoutDeceasedImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgDeceasedIcon = layoutDeceasedImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgDeceasedIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_deceased, null));
        setDeceasedIconUnselectedStateUI();

        mRelativeLayoutDeceasedImgBtn.setOnClickListener(onDeceasedIconClickListener);
    }

    private void setHazardIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutHazardImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(getContext(),
                R.color.primary_white));
        mRelativeLayoutHazardImgBtn.setBackground(layoutDrawable);

        mImgHazardIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_orange),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setHazardIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutHazardImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(getContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutHazardImgBtn.setBackground(layoutDrawable);

        mImgHazardIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
    }

    private void setDeceasedIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutDeceasedImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(getContext(),
                R.color.primary_white));
        mRelativeLayoutDeceasedImgBtn.setBackground(layoutDrawable);

        mImgDeceasedIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary_text_grey),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setDeceasedIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutDeceasedImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(getContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutDeceasedImgBtn.setBackground(layoutDrawable);

        mImgDeceasedIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
    }

    private View.OnClickListener onHazardIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setHazardIconSelectedStateUI();
            } else {
                setHazardIconUnselectedStateUI();
            }
        }
    };

    private View.OnClickListener onDeceasedIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setDeceasedIconSelectedStateUI();
            } else {
                setDeceasedIconUnselectedStateUI();
            }
        }
    };

    // the purpose of the touch listener is just to store the touch X,Y coordinates
//    View.OnTouchListener webViewOnTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            // save the X,Y coordinates
//            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
//                mLastTouchDownXY[0] = motionEvent.getX();
//                mLastTouchDownXY[1] = motionEvent.getY();
//            }
//
//            // let the touch event pass on to whoever needs it
//            return false;
//        }
//    };

//    View.OnLongClickListener webViewOnLongClickListener = new View.OnLongClickListener() {
//        @Override
//        public boolean onLongClick(View v) {
//
//            // retrieve the stored coordinates
//            float x = mLastTouchDownXY[0];
//            float y = mLastTouchDownXY[1];
//
//            Coords coords = new Coords(0, 0, 0,
//                    0, 0, 0, 0,
//                    x, y, true, "standing");
//            updateMap(coords);
////            myWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
//
//            // use the coordinates for whatever
//            Log.i("TAG", "onLongClick: x = " + x + ", y = " + y);
//
//            // we have consumed the touch event
//            return true;
//        }
//    };

    private void initTracker() {
//        this.tracker.startGPSUpdate();
        this.tracker = new NavisensLocalTracker(this.getActivity());
        this.tracker.setTrackerListener(new TrackerListener() {
            @Override
            public void onNewCoords(Coords coords) {

                Log.d(TAG, "X:" + coords.getX());
                Log.d(TAG, "Y:" + coords.getY());
                Log.d(TAG, "Z:" + coords.getAltitude());
                Log.d(TAG, "bearing:" + coords.getBearing());
                Log.d(TAG, "Action:" + coords.getAction());
                Log.d(TAG, "RealAlt:" + coords.getLatitude());

                updateMap(coords);
                sendCoords(coords);
                saveCoords(coords);
                showCoords(coords);
            }

            @Override
            public void onNewEvent(Event event) {

            }
        });
        Toast.makeText(this.getActivity().getApplicationContext(), "Tracker setup complete", Toast.LENGTH_LONG).show();
    }

    private void saveCoords(Coords _coords) {
        if (fs != null) {
            String pattern = "yyyyMMddHHmmss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            try {
                fs.write(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + prefs.getName() + "," + _coords.getAction() + "," + _coords.getLatitude() + "," + date);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "FileSaver failed to save coords");
            }
        }
    }

    private void showCoords(Coords coords) {
        DecimalFormat df2dec = new DecimalFormat("###.##");
        mTextXYZ.setText("XYZ: " + df2dec.format(coords.getX()) + "  ,  " + df2dec.format(coords.getY()) + "  ,  " + df2dec.format(coords.getAltitude()));
        mTextBearing.setText("Bearing: " + df2dec.format(coords.getBearing()));
        mTextAction.setText("Action: " + coords.getAction());
    }

    private void updateMap(Coords coords) {
        // Android to Javascript
        String message = coords.getX() + "," + coords.getY() + "," + coords.getAltitude() + "," + coords.getBearing() + "," + this.prefs.getName() + "," + coords.getAction();
        Log.d(TAG, "Calling JAVASCRIPT with " + message);
        myWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
    }

    private void updateMapOfBeacon(Coords coords, String beaconId) {
        // Android to Javascript
        String message = coords.getX() + "," + coords.getY() + "," + coords.getAltitude() + "," + coords.getBearing() + "," + beaconId + "," + BeaconZeroing.BEACONOBJ;
        Log.d(TAG, "Calling JAVASCRIPT with " + message);
        myWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
    }

//    private void killFragment() {
//        this.getActivity().finishAndRemoveTask();
//    }

    private void setupFileSaver() {
        try {
            fs = new FileSaver(this.getActivity().getApplicationContext(), prefs.getLogLocation());
            if (fs != null) {
                Toast.makeText(this.getActivity().getApplicationContext(), "Logging to " + prefs.getLogLocation(), Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "FileSaver cannot initialise");
            Toast.makeText(this.getActivity().getApplicationContext(), "FileSaver cannot initialise", Toast.LENGTH_LONG).show();
        }
    }

    private void placeBeacon() {
//        Coords coords = this.tracker.getCurrentXYZLocation();
//        this.beaconZeroing.dropBeacon(coords, "ice");
//        this.beaconZeroing.dropBeacon(coords, "mint");
//        this.beaconZeroing.dropBeacon(coords, "coconut");
//        this.beaconZeroing.dropBeacon(coords, "blueberry");
    }

    private void placeBeacon(String beaconId) {
        Coords coords = this.tracker.getCurrentXYZLocation();
        this.beaconZeroing.dropBeacon(coords, beaconId);
        updateMapOfBeacon(coords, beaconId);
        sendBeacon(coords, beaconId);
        Log.d(TAG, "Placed Beacon ID " + beaconId + " on " + coords.getX() + "," + coords.getY() + "," + coords.getAltitude());
        Toast.makeText(getActivity().getApplicationContext(), "Placed Beacon ID " + beaconId + " on " + coords.getX() + "," + coords.getY() + "," + coords.getAltitude(), Toast.LENGTH_LONG).show();
        try {
            playAudio(SOUND_BEACON_DROP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initBeacon() {
        beaconZeroing = new BeaconZeroing();
        beaconManager = new EstimoteBeaconManager(getActivity());
        beaconManager.setBeaconListener(new BeaconListener() {
            @Override
            public void onNewUpdate(BeaconObject beacon) {
                Log.d(TAG, "Detected beacon with ID: " + beacon.getId());
                DroppedBeacon droppedBeacon = beaconZeroing.getBeacon(beacon.getId());
                if (droppedBeacon != null) {
                    Log.d(TAG, "Beacon " + beacon.getId() + " is recognized, zeroing location");
                    Coords coord = droppedBeacon.getCoords();
//                    coord.setAltitude(tracker.getCurrentXYZLocation().getAltitude()); //Effectively ignoring the alt info from beacon
                    coord.setBearing(tracker.getCurrentXYZLocation().getBearing());
                    tracker.setManualLocation(coord);

                    try {
                        playAudio(SOUND_BEACON_DETECT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Beacon " + beacon.getId() + " is NOT recognized, skipping");
                }

            }
        });
        beaconManager.setAppId(prefs.getBeaconAppId());
        beaconManager.setAppToken(prefs.getBeaconToken());
        beaconManager.setDistActivate(prefs.getBeaconActivateDistance());
        beaconManager.setup();
        Toast.makeText(getActivity().getApplicationContext(), "Beacon setup complete", Toast.LENGTH_LONG).show();
    }

    private void playAudio(String audioName) throws IOException {
        AssetFileDescriptor afd = getActivity().getAssets().openFd(audioName);
        MediaPlayer player = new MediaPlayer();
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        player.prepare();
        player.start();
    }

    private synchronized void setupMessageQueue() {

        String host = prefs.getBfthost();

        System.out.println("RabbitMQHelper.connectionStatus is " + RabbitMQHelper.connectionStatus.toString());
//        RabbitMQHelper.getInstance().startRabbitMQWithSetting(getActivity(), host, prefs.getMqUsername(), prefs.getMqPassword());
        if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
            setupMQListener();
        } else if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.DISCONNECTED) {
            RabbitMQHelper.getInstance().startRabbitMQWithSetting(getActivity(), host, prefs.getMqUsername(), prefs.getMqPassword());
            if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
                setupMQListener();
            }
        }

//        if (mqRabbit != null) {
//            Log.w(TAG, "You already have a Rabbit running, killing previous queue and restarting another");
//            mqRabbit.close();
//        } else {
//            mqRabbit = new RabbitMQ();
//
//            Log.d(TAG, "Connecting to MQ on " + host);
//            boolean isSuccess = mqRabbit.connect(host, prefs.getMqUsername(), prefs.getMqPassword());
//            if (isSuccess) {
//                Toast.makeText(getActivity().getApplicationContext(), "RabbitMQ setup complete", Toast.LENGTH_SHORT).show();
//                setupMQListener();
//            } else {
//                Toast.makeText(getActivity().getApplicationContext(), "RabbitMQ failed. C2 capabilities disabled", Toast.LENGTH_SHORT).show();
//            }
//            Log.d(TAG, "Connection to MQ is successful: " + isSuccess);
//        }
    }

    /**
     * This will setup a MQ listener for requests to mq send all beacons known to this device.
     */
    private void setupMQListener() {

        if (mIMQListener == null) {
            mIMQListener = new IMQListener() {
                //        mqRabbit.setListener(new MQListener() {
                @Override
                public void onNewMessage(String message) {
                    if (!JSONUtil.isJSONValid(message)) {

                        String[] messageArray = message.split(",");
                        String action = messageArray[5];
                        if (action.equals(BeaconZeroing.BEACONREQ)) {
                            Log.d(TAG, "Loading Beacons to send");
                            ArrayList<DroppedBeacon> droppedBeaconsList = beaconZeroing.getAllDroppedbeacons();
                            for (int i = 0; i < droppedBeaconsList.size(); i++) {
                                Log.d(TAG, "BEACON SEND");
                                sendBeacon(droppedBeaconsList.get(i).getCoords(), droppedBeaconsList.get(i).getId());
                            }

                        } else if (action.equals(BeaconZeroing.BEACONOBJ)) {
                            Log.d(TAG, "Add this beacon in if its not here");
                            String x = messageArray[0];
                            String y = messageArray[1];
                            String z = messageArray[2];
                            String bearing = messageArray[3];
                            String beaconId = messageArray[4];
                            if (beaconZeroing.getBeacon(beaconId) == null) {
                                Log.d(TAG, "Beacon not here, adding it");
                                beaconZeroing.dropBeacon(new Coords(0.0, 0.0, Double.valueOf(z), Double.valueOf(bearing), 0, 0, 0, Double.valueOf(x), Double.valueOf(y), ""), beaconId);
                            }
                        }

                    }

                }
            };

            RabbitMQHelper.getInstance().addRabbitListener(mIMQListener);
        }
    }

    private void sendCoords(Coords _coords) {
//        try {
//            String pattern = "yyyyMMddHHmmss";
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//            String date = simpleDateFormat.format(new Date());
//            mqRabbit.sendMessage(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + prefs.getName() + "," + _coords.getAction() + "," + _coords.getLatitude() + "," + date);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        String pattern = "yyyyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        RabbitMQHelper.getInstance().sendBFTMessage(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + prefs.getName() + "," + _coords.getAction() + "," + _coords.getLatitude() + "," + date);
    }

    private void sendBeacon(Coords _coords, String beaconId) {
//        try {
//            mqRabbit.sendMessage(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + beaconId + "," + BeaconZeroing.BEACONOBJ);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        RabbitMQHelper.getInstance().sendBFTMessage(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + beaconId + "," + BeaconZeroing.BEACONOBJ);
    }

    /**
     * Adds designated fragment to Back Stack of Base Child Fragment
     * before navigating to it
     *
     * @param toFragment
     */
    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateWithAnimatedTransitionToFragment(
                    R.id.layout_map_ship_blueprint_fragment, this, toFragment);
        }
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    private void onVisible() {
        System.out.println("mapShipBlueprintFragment onVisible");

        if (tracker == null) {
            System.out.println("mapShipBlueprintFragment onResume");
            initBeacon();
            initTracker();
            initWebviewSettings();
//            initTracker();
        }

        Log.d(TAG, "onVisible");

        FragmentManager fm = getChildFragmentManager();
        boolean isFragmentFound = false;

        int count = fm.getBackStackEntryCount();

        // Checks if current fragment exists in Back stack
        for (int i = 0; i < count; i++) {
            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
                isFragmentFound = true;
            }
        }

        // If not found, add to current fragment to Back stack
        if (!isFragmentFound) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.addToBackStack(this.getClass().getSimpleName());
            ft.commit();
        }

        beaconManager.enableForegroundDispatch();
    }

    private void onInvisible() {
        beaconManager.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
        if (tracker != null) {
            System.out.println("mapShipBlueprintFragment onStop");
            tracker.deactivate();
        }

        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.settingsMenu) {
//            // launch settings activity
//            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }

//        if (tracker != null) {
//            System.out.println("mapShipBlueprintFragment onStop");
//            tracker.deactivate();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        if (tracker != null) {
//            System.out.println("mapShipBlueprintFragment onPause");
//            tracker.deactivate();
//        }

//        beaconManager.disableForegroundDispatch();
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
    public void onResume() {
        super.onResume();
//        mBottomNavigationView = getActivity().findViewById(R.id.btm_nav_view_main_nav);
//        mBottomNavigationView.setVisibility(View.GONE);

//        if (tracker == null) {
//            System.out.println("mapShipBlueprintFragment onResume");
//            initTracker();
//        }

//        setupMessageQueue();
//        if (mqRabbit != null) {
//
//        }

//        MapShipBlueprintFragment mapShipBlueprintFragment = (MapShipBlueprintFragment) getActivity().getSupportFragmentManager()
//                .findFragmentByTag(MapShipBlueprintFragment.this.getClass().getSimpleName());
//        if (this != null && this.isVisible()) {
//            System.out.println("mapShipBlueprintFragment visible");
//            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
//        else {
//            //Whatever
//        }


//        beaconManager.enableForegroundDispatch();
    }

    @Subscribe
    public void onEvent(PageEvent pageEvent) {

//        if (SettingsActivity.class.getSimpleName().equalsIgnoreCase(pageEvent.getPreviousActivityName())) {
//            System.out.println("Settings Activity found!");
////            setupMessageQueue();
////            initUI(mRootMapView, mSavedInstanceState);
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        Log.d(TAG, "setUserVisibleHint");
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Log.d(TAG, "setUserVisibleHint onVisible");
                onVisible();
            }
            else {
                onInvisible();
            }
        }
    }

//    @Override
//    public void onNewIntent(Intent intent) {
//        Log.d(TAG, "NFC: New Intent");
//        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
//                NfcAdapter.EXTRA_NDEF_MESSAGES);
//        Log.d(TAG, "NFC: RawMsg is " + rawMsgs);
//        if (rawMsgs != null) {
//            for (int i = 0; i < rawMsgs.length; i++) {
//                NdefMessage msg = (NdefMessage) rawMsgs[i];
//                String beaconId = beaconManager.getBeaconIdbByNFC(msg);
//                Log.d(TAG, "NFC: Received Beacon ID " + beaconId);
//                placeBeacon(beaconId);
//            }
//        }
//        return;
//    }

}
