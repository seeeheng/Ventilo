package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.components.Color;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dh.trackers.Event;
import sg.gov.dh.trackers.NavisensLocalTracker;
import sg.gov.dh.trackers.TrackerListener;
import sg.gov.dh.utils.Coords;
import sg.gov.dh.utils.FileSaver;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.BFTViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.JSONUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ProgressBarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SpinnerItemListDataBank;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;

public class MapShipBlueprintFragment extends Fragment {

    private static final String TAG = MapShipBlueprintFragment.class.getSimpleName();
    private static final String LOCAL_SHIP_BLUEPRINT_DIRECTORY = "file:///android_asset/ship/";
    private static final String BEACON_DROP_HAZARD = "BEACON DROP HAZARD";
    private static final String BEACON_DROP_DECEASED = "BEACON DROP DECEASED";
    private C2OpenSansSemiBoldTextView mTextXYZ;
    private C2OpenSansSemiBoldTextView mTextBearing;
    private C2OpenSansSemiBoldTextView mTextAction;

    // View models
    private BFTViewModel mBFTViewModel;

    // Main UI
    private LinearLayout mMainLayout;
    private FrameLayout mFrameLayoutBlueprint;
    private WebView myWebView;
    private View mMiddleDivider;
    private View mBottomDivider;

    // Dashboard Fragments
    private HorizontalScrollView mHorizontalSVDashboardFragments;
    private View mVideoStreamFragment;
    private View mSitRepPersonnelStatusFragment;
    private View mTaskPhaseStatusFragment;
    private View mRadioLinkStatusFragment;

    private List<String> mSpinnerFloorNameLinkList;
//    private ArrayList<String> mSpinnerFloorNameList;

    // Killed In Action (KIA)/Hazard and Deceased UI
    private static RelativeLayout mRelativeLayoutHazardImgBtn;
    private AppCompatImageView mImgHazardIcon;
    private static RelativeLayout mRelativeLayoutDeceasedImgBtn;
    private AppCompatImageView mImgDeceasedIcon;

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

    private BFTLocalPreferences prefs;
    //    BeaconManagerInterface beaconManager;
//    private BeaconZeroing beaconZeroing;

    private IMQListener mIMQListener;

    // Beacon object message list
    private List<String> mHazardMsgList;
    private List<String> mDeceasedMsgList;
    private BFTModel mOwnBFTModel;

    // For tracker to be initialised
    private boolean mIsTrackerInitialised;

    // For onNewCoord listener to fire at least once
    private boolean mIsTrackerUpdateInitialised;
    private boolean mIsFragmentVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        observerSetup();

        View rootMapShipBlueprintView = inflater.inflate(R.layout.fragment_map_ship_blueprint, container, false);
        initUI(rootMapShipBlueprintView);

        mOwnBFTModel = new BFTModel();
        prefs = new BFTLocalPreferences(this.getContext());

//        final ImageView imgWorldMap = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_world_map);
//        imgWorldMap.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                MapFragment mapFragment = new MapFragment();
//                navigateToFragment(mapFragment);
//            }
//        });

        initTracker();
        initWebviewSettings();

        return rootMapShipBlueprintView;
    }

    private void initUI(View rootMapShipBlueprintView) {
        mMainLayout = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint_fragment);
        mFrameLayoutBlueprint = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint);
        mMiddleDivider = rootMapShipBlueprintView.
                findViewById(R.id.view_map_ship_blueprint_middle_divider);
        mHorizontalSVDashboardFragments = rootMapShipBlueprintView.
                findViewById(R.id.hs_view_map_ship_blueprint_dashboard_fragments);
        mBottomDivider = rootMapShipBlueprintView.
                findViewById(R.id.view_map_ship_blueprint_bottom_divider);
        myWebView = rootMapShipBlueprintView.findViewById(R.id.webview_bft);

        mVideoStreamFragment = rootMapShipBlueprintView.
                findViewById(R.id.fragment_dashboard_video_stream);
        mSitRepPersonnelStatusFragment = rootMapShipBlueprintView.
                findViewById(R.id.fragment_dashboard_sitrep_personnel_status);
        mTaskPhaseStatusFragment = rootMapShipBlueprintView.
                findViewById(R.id.fragment_dashboard_task_phase_status);
        mRadioLinkStatusFragment = rootMapShipBlueprintView.
                findViewById(R.id.fragment_dashboard_radio_link_status);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    760.0f
            );
            mFrameLayoutBlueprint.setLayoutParams(param);

            mMiddleDivider.setVisibility(View.GONE);
            mHorizontalSVDashboardFragments.setVisibility(View.GONE);
            mBottomDivider.setVisibility(View.GONE);
        } else {

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 385.0f
            );
            mFrameLayoutBlueprint.setLayoutParams(param);

            mMiddleDivider.setVisibility(View.VISIBLE);
            mHorizontalSVDashboardFragments.setVisibility(View.VISIBLE);
            mBottomDivider.setVisibility(View.VISIBLE);
        }

        mTextXYZ = rootMapShipBlueprintView.findViewById(R.id.tv_map_blueprint_textXYZ);
        mTextBearing = rootMapShipBlueprintView.findViewById(R.id.tv_map_blueprint_textBearing);
        mTextAction = rootMapShipBlueprintView.findViewById(R.id.tv_map_blueprint_textAction);

        initSideButtons(rootMapShipBlueprintView);
        initFloorSpinner(rootMapShipBlueprintView);

        final ImageView imgSetting = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_setting);
        imgSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });
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
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                updateMapOfHazardBeacon();
                updateMapOfDeceasedBeacon();
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient());
        myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getOverview());
        myWebView.addJavascriptInterface(new WebAppInterface(this.getActivity(), prefs, tracker), "Android");

        setupMessageQueue();
        setupFileSaver();
    }

    private void initFloorSpinner(View rootBlueprintView) {
        // Floor Spinner
        Spinner spinnerBlueprintList = rootBlueprintView.findViewById(R.id.spinner_ship_blueprint_floor_selector);
        List<String> spinnerFloorNameList = new ArrayList<>(Arrays.asList(SpinnerItemListDataBank.getInstance().
                getBlueprintFloorStrArray()));
        mSpinnerFloorNameLinkList = Arrays.asList(SpinnerItemListDataBank.getInstance().
                getBlueprintFloorHtmlLinkStrArray());

        ArrayAdapter spinnerBlueprintAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_row_item,
                R.id.tv_spinner_row_item_text, spinnerFloorNameList) {

            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);
                tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));
                return view;
            }
        };

        spinnerBlueprintList.setAdapter(spinnerBlueprintAdapter);
        spinnerBlueprintList.setOnItemSelectedListener(onMapBlueprintFloorItemSelectedListener);
    }

    private AdapterView.OnItemSelectedListener onMapBlueprintFloorItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    myWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY +
                            mSpinnerFloorNameLinkList.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            };

    private void initSideButtons(View rootMapShipBlueprintView) {
        View layoutMainHazardImgBtn = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint_fragment_hazard_icon);
        mRelativeLayoutHazardImgBtn = layoutMainHazardImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgHazardIcon = layoutMainHazardImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgHazardIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_btn_hazard, null));
        setHazardIconUnselectedStateUI();

        mRelativeLayoutHazardImgBtn.setOnClickListener(onHazardIconClickListener);

        View layoutDeceasedImgBtn = rootMapShipBlueprintView.findViewById(R.id.layout_map_ship_blueprint_fragment_deceased_icon);
        mRelativeLayoutDeceasedImgBtn = layoutDeceasedImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgDeceasedIcon = layoutDeceasedImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgDeceasedIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_btn_deceased, null));
        setDeceasedIconUnselectedStateUI();

        mRelativeLayoutDeceasedImgBtn.setOnClickListener(onDeceasedIconClickListener);

        mHazardMsgList = new ArrayList<>();
        mDeceasedMsgList = new ArrayList<>();
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
//            closeWebSocketClient();
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                if (mRelativeLayoutDeceasedImgBtn.isSelected()) {
                    mRelativeLayoutDeceasedImgBtn.setSelected(false);
                    setDeceasedIconUnselectedStateUI();
                }

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
                if (mRelativeLayoutHazardImgBtn.isSelected()) {
                    mRelativeLayoutHazardImgBtn.setSelected(false);
                    setHazardIconUnselectedStateUI();
                }

                setDeceasedIconSelectedStateUI();
            } else {
                setDeceasedIconUnselectedStateUI();
            }
        }
    };

    /**
     * Refreshes BFT objects on map on fragment load
     */
    private synchronized void refreshBFT() {
        SingleObserver<List<BFTModel>> singleObserverBFTForUser =
                new SingleObserver<List<BFTModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<BFTModel> bFTModelList) {
                        if (bFTModelList != null) {
                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
                                    "refreshBFT. " +
                                    "bFTModelList, size: " + bFTModelList.size());

                            List<BFTModel> currentUserBFTModelList = bFTModelList.stream().
                                    filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                                            equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());

                            // Get own BFT model
                            List<BFTModel> ownBFTModelList = currentUserBFTModelList.stream().
                                    filter(bftModel -> MainApplication.getAppContext().
                                            getString(R.string.map_blueprint_own_type).
                                            equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                            Log.i(TAG, "ownBFTModelList, size:" + ownBFTModelList.size());

                            // There should only be one 'Own' type BFTModel belonging to a user
                            if (ownBFTModelList.size() == 1) {
                                mOwnBFTModel = ownBFTModelList.get(0);

                                if (mOwnBFTModel.getXCoord() != null && mOwnBFTModel.getYCoord() != null &&
                                        mOwnBFTModel.getAltitude() != null) {
                                    Coords ownBFTModelCoord = new Coords(0, 0,
                                            Double.parseDouble(mOwnBFTModel.getAltitude().trim()),
                                            0, 0, 0, 0,
                                            Double.parseDouble(mOwnBFTModel.getXCoord().trim()),
                                            Double.parseDouble(mOwnBFTModel.getYCoord().trim()), null);

                                    Log.i(TAG, "ownBFTModelCoord.getX(): " + ownBFTModelCoord.getX());
                                    Log.i(TAG, "ownBFTModelCoord.getY(): " + ownBFTModelCoord.getY());
                                    Log.i(TAG, "ownBFTModelCoord.getAltitude(): " + ownBFTModelCoord.getAltitude());

                                    // Set own BFT model location manually on map refresh
                                    tracker.setManualLocation(ownBFTModelCoord);
                                }

                            } else if (ownBFTModelList.size() == 0) {

                                // Created default own BFT model
                                mOwnBFTModel.setXCoord(StringUtil.DEFAULT_INT);
                                mOwnBFTModel.setYCoord(StringUtil.DEFAULT_INT);
                                mOwnBFTModel.setAltitude(StringUtil.DEFAULT_INT);
                                mOwnBFTModel.setBearing(StringUtil.DEFAULT_INT);
                                mOwnBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                                mOwnBFTModel.setType(MainApplication.getAppContext().
                                        getString(R.string.map_blueprint_own_type));
                                mOwnBFTModel.setCreatedTime(DateTimeUtil.dateToCustomTimeStringFormat(
                                        DateTimeUtil.stringToDate(DateTimeUtil.getCurrentTime())));

                                Log.i(TAG, "Own BFT model inserted");
                                mBFTViewModel.insertBFT(mOwnBFTModel);
                            }

                            // Refresh 'Hazard' and 'Deceased' type objects on map
                            if (mHazardMsgList == null || mDeceasedMsgList == null ||
                                    mHazardMsgList.size() == 0 && mDeceasedMsgList.size() == 0) {
                                List<BFTModel> hazardBFTModelList = currentUserBFTModelList.stream().
                                        filter(bftModel -> MainApplication.getAppContext().
                                                getString(R.string.map_blueprint_hazard_type).
                                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                                List<String> hazardBFTMsgList = formBFTMsgList(hazardBFTModelList);
                                mHazardMsgList.addAll(hazardBFTMsgList);

                                List<BFTModel> deceasedBFTModelList = currentUserBFTModelList.stream().
                                        filter(bftModel -> MainApplication.getAppContext().
                                                getString(R.string.map_blueprint_deceased_type).
                                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                                List<String> deceasedBFTMsgList = formBFTMsgList(deceasedBFTModelList);
                                mDeceasedMsgList.addAll(deceasedBFTMsgList);

                                updateMapOfHazardBeacon();
                                updateMapOfDeceasedBeacon();
                            }

                        } else {
                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
                                    "refreshBFT. " +
                                    "bFTModelList is null/empty");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverBFTForUser, " +
                                "refreshBFT. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mBFTViewModel.getAllBFTs(singleObserverBFTForUser);
    }

    private void initTracker() {
        this.tracker = new NavisensLocalTracker(this.getActivity());
        this.tracker.setTrackerListener(new TrackerListener() {
            @Override
            public void onNewCoords(Coords coords) {

                if (!mIsTrackerUpdateInitialised) {
                    refreshBFT();
                }

                mIsTrackerUpdateInitialised = true;

                Log.d(TAG, "X:" + coords.getX());
                Log.d(TAG, "Y:" + coords.getY());
                Log.d(TAG, "Z:" + coords.getAltitude());
                Log.d(TAG, "bearing:" + coords.getBearing());
                Log.d(TAG, "Action:" + coords.getAction());
                Log.d(TAG, "RealAlt:" + coords.getLatitude());

                updateMap(coords);
//                sendCoords(coords);
                saveCoords(coords);
                showCoords(coords);
            }

            @Override
            public void onNewEvent(Event event) {

            }
        });

        Toast.makeText(this.getActivity().getApplicationContext(),
                "Tracker setup complete", Toast.LENGTH_LONG).show();
        mIsTrackerInitialised = true;
    }

    private void saveCoords(Coords coords) {
        if (fs != null) {
            String pattern = "yyyyMMddHHmmss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
            try {
                fs.write(coords.getX() + "," + coords.getY() + "," + coords.getAltitude() + "," + coords.getBearing() + "," + prefs.getName() + "," + coords.getAction() + "," + coords.getLatitude() + "," + date);
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
        String message;

        message = coords.getX() + "," + coords.getY() + "," + coords.getAltitude() +
                "," + coords.getBearing() + "," + SharedPreferenceUtil.getCurrentUserCallsignID() +
                "," + coords.getAction();

        if (mOwnBFTModel != null) {
            mOwnBFTModel.setXCoord(String.valueOf(coords.getX()));
            mOwnBFTModel.setYCoord(String.valueOf(coords.getY()));
            mOwnBFTModel.setAltitude(String.valueOf(coords.getAltitude()));
            mOwnBFTModel.setBearing(String.valueOf(coords.getBearing()));
            mOwnBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
            mOwnBFTModel.setType(MainApplication.getAppContext().
                    getString(R.string.map_blueprint_own_type));
            mOwnBFTModel.setCreatedTime(DateTimeUtil.dateToCustomTimeStringFormat(
                    DateTimeUtil.stringToDate(DateTimeUtil.getCurrentTime())));
        }

        Log.d(TAG, "Calling JAVASCRIPT with " + message);

        myWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
    }

//    private void updateMapOfBeacon(Coords coords, String beaconId) {
////        // Android to Javascript
////        String message = coords.getX() + "," + coords.getY() + "," + coords.getAltitude() +
////                "," + coords.getBearing() + "," + beaconId + "," + BeaconZeroing.BEACONOBJ;
////
////        Log.d(TAG, "Calling JAVASCRIPT with " + message);
////
////        myWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
////    }

    /**
     * (Android to Javascript)
     * Add a Hazard beacon to represent a hazardous object
     */
    private void updateMapOfHazardBeacon() {
        for (int i = 0; i < mHazardMsgList.size(); i++) {
            myWebView.evaluateJavascript("javascript: " + "androidToJScreateLocation(\""
                    + mHazardMsgList.get(i) + "\")", null);
            Log.d(TAG, "Calling JAVASCRIPT with Hazard Msg List: " + mHazardMsgList.get(i));
        }
    }

    /**
     * (Android to Javascript)
     * Add a Deceased beacon to represent a deceased person
     */
    private void updateMapOfDeceasedBeacon() {
        for (int i = 0; i < mDeceasedMsgList.size(); i++) {
            myWebView.evaluateJavascript("javascript: " + "androidToJScreateLocation(\""
                    + mDeceasedMsgList.get(i) + "\")", null);
            Log.d(TAG, "Calling JAVASCRIPT with Deceased Msg List: " + mDeceasedMsgList.get(i));
        }
    }

//    private void placeHazardBeacon() {
//        updateMapOfHazardBeacon();
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

//    private void placeBeacon() {
////        Coords coords = this.tracker.getCurrentXYZLocation();
////        this.beaconZeroing.dropBeacon(coords, "ice");
////        this.beaconZeroing.dropBeacon(coords, "mint");
////        this.beaconZeroing.dropBeacon(coords, "coconut");
////        this.beaconZeroing.dropBeacon(coords, "blueberry");
//    }
//
//    private void placeBeacon(String beaconId) {
//        Coords coords = this.tracker.getCurrentXYZLocation();
//        this.beaconZeroing.dropBeacon(coords, beaconId);
//        updateMapOfBeacon(coords, beaconId);
//        sendBeacon(coords, beaconId);
//        Log.d(TAG, "Placed Beacon ID " + beaconId + " on " + coords.getX() + "," + coords.getY() + "," + coords.getAltitude());
//        Toast.makeText(getActivity().getApplicationContext(), "Placed Beacon ID " + beaconId + " on " + coords.getX() + "," + coords.getY() + "," + coords.getAltitude(), Toast.LENGTH_LONG).show();
//        try {
//            playAudio(SOUND_BEACON_DROP);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void initBeacon() {
//        beaconZeroing = new BeaconZeroing();
//        beaconManager = new EstimoteBeaconManager(getActivity());
//        beaconManager.setBeaconListener(new BeaconListener() {
//            @Override
//            public void onNewUpdate(BeaconObject beacon) {
//                Log.d(TAG, "Detected beacon with ID: " + beacon.getId());
//                DroppedBeacon droppedBeacon = beaconZeroing.getBeacon(beacon.getId());
//                if (droppedBeacon != null) {
//                    Log.d(TAG, "Beacon " + beacon.getId() + " is recognized, zeroing location");
//                    Coords coord = droppedBeacon.getCoords();
////                    coord.setAltitude(tracker.getCurrentXYZLocation().getAltitude()); //Effectively ignoring the alt info from beacon
//                    coord.setBearing(tracker.getCurrentXYZLocation().getBearing());
//                    tracker.setManualLocation(coord);
//
//                    try {
//                        playAudio(SOUND_BEACON_DETECT);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Log.d(TAG, "Beacon " + beacon.getId() + " is NOT recognized, skipping");
//                }
//
//            }
//        });
//
//        beaconManager.setAppId(prefs.getBeaconAppId());
//        beaconManager.setAppToken(prefs.getBeaconToken());
//        beaconManager.setDistActivate(prefs.getBeaconActivateDistance());
//        beaconManager.setup();
//        Toast.makeText(getActivity().getApplicationContext(), "Beacon setup complete", Toast.LENGTH_LONG).show();
//    }

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

//                        String[] messageArray = message.split(",");
//                        String action = messageArray[5];
//                        if (action.equals(BeaconZeroing.BEACONREQ)) {
//                            Log.d(TAG, "Loading Beacons to send");
//                            ArrayList<DroppedBeacon> droppedBeaconsList = beaconZeroing.getAllDroppedbeacons();
//                            for (int i = 0; i < droppedBeaconsList.size(); i++) {
//                                Log.d(TAG, "BEACON SEND");
//                                sendBeacon(droppedBeaconsList.get(i).getCoords(), droppedBeaconsList.get(i).getId());
//                            }
//
//                        } else if (action.equals(BeaconZeroing.BEACONOBJ)) {
//                            Log.d(TAG, "Add this beacon in if its not here");
//                            String x = messageArray[0];
//                            String y = messageArray[1];
//                            String z = messageArray[2];
//                            String bearing = messageArray[3];
//                            String beaconId = messageArray[4];
//                            if (beaconZeroing.getBeacon(beaconId) == null) {
//                                Log.d(TAG, "Beacon not here, adding it");
//                                beaconZeroing.dropBeacon(new Coords(0.0, 0.0, Double.valueOf(z), Double.valueOf(bearing), 0, 0, 0, Double.valueOf(x), Double.valueOf(y), ""), beaconId);
//                            }
//                        }

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
        RabbitMQHelper.getInstance().sendBFTMessage(_coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," + _coords.getBearing() + "," + SharedPreferenceUtil.getCurrentUserCallsignID() + "," + _coords.getAction() + "," + _coords.getLatitude() + "," + date);
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
     * For javascript interface class to receive button selected state
     *
     * @return
     */
    public static String getIconTypeToMarker() {
        String iconType;
        if (mRelativeLayoutHazardImgBtn.isSelected()) {
            iconType = MainApplication.getAppContext().
                    getString(R.string.map_blueprint_hazard_type);
        } else if (mRelativeLayoutDeceasedImgBtn.isSelected()) {
            iconType = MainApplication.getAppContext().
                    getString(R.string.map_blueprint_deceased_type);
        } else {
            iconType = MainApplication.getAppContext().
                    getString(R.string.map_blueprint_other_type);
        }

        return iconType;
    }

    /**
     * Get own BFT model from database and updates with latest location for future viewing
     */
    private void updateOwnBFTModel() {
        Log.i(TAG, "Update own BFT model: " + mOwnBFTModel);

        SingleObserver<List<BFTModel>> singleObserverBFTForUser =
                new SingleObserver<List<BFTModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<BFTModel> bFTModelList) {
                        if (bFTModelList != null) {
                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
                                    "updateOwnBFTModel. " +
                                    "bFTModelList, size: " + bFTModelList.size());

                            // Get BFT models belonging to a current user (including hazard and deceased entities)
                            List<BFTModel> currentUserBFTModelList = bFTModelList.stream().
                                    filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                                            equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());

                            // Get own BFT model
                            List<BFTModel> ownBFTModelList = currentUserBFTModelList.stream().
                                    filter(bftModel -> MainApplication.getAppContext().
                                            getString(R.string.map_blueprint_own_type).
                                            equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                            Log.i(TAG, "ownBFTModelList, size:" + ownBFTModelList.size());

                            // There should only be one 'own' type BFTModel belonging to a user
                            if (ownBFTModelList.size() == 1) {
                                BFTModel ownBFTModel = ownBFTModelList.get(0);

                                // Update location and push updated model back to database
                                ownBFTModel.setXCoord(mOwnBFTModel.getXCoord());
                                ownBFTModel.setYCoord(mOwnBFTModel.getYCoord());
                                ownBFTModel.setAltitude(mOwnBFTModel.getAltitude());
                                ownBFTModel.setBearing(mOwnBFTModel.getBearing());
                                mBFTViewModel.updateBFT(ownBFTModel);
                            }

                        } else {
                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
                                    "updateOwnBFTModel. " +
                                    "bFTModelList is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverBFTForUser, " +
                                "updateOwnBFTModel. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mBFTViewModel.getAllBFTs(singleObserverBFTForUser);
    }

    /**
     * Form BFT message list for javascript location update
     *
     * @param bFTModelList
     * @return
     */
    private List<String> formBFTMsgList(List<BFTModel> bFTModelList) {
        List<String> msgList = new ArrayList<>();

        for (int i = 0; i < bFTModelList.size(); i++) {
            BFTModel bFTModel = bFTModelList.get(i);
            StringBuilder msgStrBuilder = new StringBuilder();
            msgStrBuilder.append(bFTModel.getXCoord());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getYCoord());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getAltitude());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getBearing());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getUserId());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getType());
            msgStrBuilder.append(StringUtil.COMMA);
            msgStrBuilder.append(bFTModel.getCreatedTime());

            msgList.add(msgStrBuilder.toString());
        }

        return msgList;
    }

    /**
     * -------------------- UI Components Visibility Methods --------------------
     **/
    public void setBlueprintVisibility(int isVisible) {
        mFrameLayoutBlueprint.setVisibility(isVisible);
    }

    public void setMiddleDividerVisibility(int isVisible) {
        mMiddleDivider.setVisibility(isVisible);
    }

    public void setBottomDividerVisibility(int isVisible) {
        mBottomDivider.setVisibility(isVisible);
    }

    public View getHorizontalSVDashboardFragments() {
        return mHorizontalSVDashboardFragments;
    }

    public View getVideoStreamFragment() {
        return mVideoStreamFragment;
    }

    public void setVideoStreamFragmentVisibility(int isVisible) {
        mVideoStreamFragment.setVisibility(isVisible);
    }

    public void setSitRepPersonnelStatusFragmentVisibility(int isVisible) {
        mSitRepPersonnelStatusFragment.setVisibility(isVisible);
    }

    public void setTaskPhaseStatusFragmentVisibility(int isVisible) {
        mTaskPhaseStatusFragment.setVisibility(isVisible);
    }

    public void setRadioLinkStatusFragmentVisibility(int isVisible) {
        mRadioLinkStatusFragment.setVisibility(isVisible);
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mBFTViewModel = ViewModelProviders.of(this).get(BFTViewModel.class);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = sharedPrefs.getString(SharedPreferenceConstants.USER_ID,
                SharedPreferenceConstants.DEFAULT_STRING);

        /*
         * Refreshes spinner UI whenever there is a change in BFTs (insert, update or delete)
         */
        mBFTViewModel.getAllBFTsLiveDataForUser(userId).observe(this, new Observer<List<BFTModel>>() {
            @Override
            public void onChanged(@Nullable List<BFTModel> bFTModelList) {
                Log.i(TAG, "bFTModelList onChanged: " + bFTModelList);
                if (mHazardMsgList == null) {
                    mHazardMsgList = new ArrayList<>();
                } else {
                    mHazardMsgList.clear();
                }

                if (mDeceasedMsgList == null) {
                    mDeceasedMsgList = new ArrayList<>();
                } else {
                    mDeceasedMsgList.clear();
                }

                List<BFTModel> hazardBFTModelList = bFTModelList.stream().
                        filter(bftModel -> MainApplication.getAppContext().
                                getString(R.string.map_blueprint_hazard_type).
                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                List<String> hazardBFTMsgList = formBFTMsgList(hazardBFTModelList);
                mHazardMsgList.addAll(hazardBFTMsgList);

                List<BFTModel> deceasedBFTModelList = bFTModelList.stream().
                        filter(bftModel -> MainApplication.getAppContext().
                                getString(R.string.map_blueprint_deceased_type).
                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

                List<String> deceasedBFTMsgList = formBFTMsgList(deceasedBFTModelList);
                mDeceasedMsgList.addAll(deceasedBFTMsgList);
            }
        });

    }

    /**
     * Get Snackbar view from main activity
     *
     * @return
     */
    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
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
                    R.id.layout_map_ship_blueprint, this, toFragment);
        }
    }

//    /**
//     * Release all resources related to / referenced by fragment upon onDestroy of Main Activity
//     */
//    public void onMainActivityDestroy() {
//        if (fs != null) {
//            try {
//                fs.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
        Log.i(TAG, "onVisible");

        if ((tracker == null || !tracker.isActive()) && !mIsTrackerInitialised) {
//            initBeacon();
            Log.i(TAG, "onVisible: Initialising tracker, " +
                    "webview settings and refreshing BFT");
            initTracker();
            initWebviewSettings();

            mIsTrackerInitialised = true;
        }

        // Show snackbar message to request user for location initialisation
        if (getSnackbarView() != null) {
            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                    MainApplication.getAppContext().
                            getString(R.string.snackbar_map_blueprint_init_location_message));
        }

//        beaconManager.enableForegroundDispatch();
    }

    private void onInvisible() {
        Log.i(TAG, "onInvisible");
        updateOwnBFTModel();

        if (mRelativeLayoutHazardImgBtn.isSelected()) {
            mRelativeLayoutHazardImgBtn.setSelected(false);
            setHazardIconUnselectedStateUI();
        }

        if (mRelativeLayoutDeceasedImgBtn.isSelected()) {
            mRelativeLayoutDeceasedImgBtn.setSelected(false);
            setDeceasedIconUnselectedStateUI();
        }

        if (tracker != null) {
            tracker.deactivate();
            mIsTrackerInitialised = false;
            mIsTrackerUpdateInitialised = false;
        }

//        beaconManager.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
//        if (tracker != null) {
//            System.out.println("mapShipBlueprintFragment onStop");
//            tracker.deactivate();
//        }

        Log.i(TAG, "onDestroy");

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

//        EventBus.getDefault().unregister(this);

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }

//        if (tracker != null) {
//            System.out.println("mapShipBlueprintFragment onStop");
//            tracker.deactivate();
//        }
//        beaconManager.disableForegroundDispatch();
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
//        EventBus.getDefault().register(this);

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }

//        beaconManager.enableForegroundDispatch();
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

//    @Subscribe
//    public void onEvent(PageEvent pageEvent) {
//
//        if (SettingsActivity.class.getSimpleName().equalsIgnoreCase(pageEvent.getPreviousActivityName())) {
//            System.out.println("Settings Activity found!");
////            setupMessageQueue();
////            initUI(mRootMapView, mSavedInstanceState);
//        }
//    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        Log.d(TAG, "setUserVisibleHint");
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Log.d(TAG, "setUserVisibleHint onVisible");
                onVisible();
            } else {
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
