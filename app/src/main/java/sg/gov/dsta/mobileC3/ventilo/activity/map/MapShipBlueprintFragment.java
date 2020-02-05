package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.res.AssetFileDescriptor;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nutiteq.components.Color;

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
import sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.radioLinkStatus.DashboardRadioLinkStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.sitRepPersonnelStatus.DashboardSitRepPersonnelStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.taskPhaseStatus.DashboardTaskPhaseStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.videoStream.DashboardVideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.listener.DebounceOnClickListener;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.BFTViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.MapViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQ;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.FileUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ListenerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SpinnerItemListDataBank;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.enums.bft.EBftType;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import timber.log.Timber;

public class MapShipBlueprintFragment extends Fragment {

    private static final String TAG = MapShipBlueprintFragment.class.getSimpleName();
    private static final String LOCAL_SHIP_BLUEPRINT_DIRECTORY = "file:///android_asset/ship/";
    private static final String EXTERNAL_MAP_BLUEPRINT_DIRECTORY = "file:///".
            concat(FileUtil.getMapBlueprintImagesFilePath()).concat(StringUtil.TRAILING_SLASH);
    private static final String BEACON_DROP_HAZARD = "BEACON DROP HAZARD";
    private static final String BEACON_DROP_DECEASED = "BEACON DROP DECEASED";

    private C2OpenSansSemiBoldTextView mTextXYZ;
    private C2OpenSansSemiBoldTextView mTextBearing;
    private C2OpenSansSemiBoldTextView mTextAction;

    // View models
    private UserViewModel mUserViewModel;
    private MapViewModel mMapViewModel;
    private BFTViewModel mBFTViewModel;

    // Main UI
    private View mRootView;
    private FrameLayout mMainLayout;
    private FrameLayout mFrameLayoutBlueprint;
    private LinearLayout mLinearLayoutDashboardFragments;
    private WebView mWebView;
    private View mMiddleDivider;
    private View mBottomDivider;

    // Dashboard Fragments
    private HorizontalScrollView mHorizontalSVDashboardFragments;
    private View mVideoStreamFragment;
    private View mSitRepPersonnelStatusFragment;
    private View mTaskPhaseStatusFragment;
    private View mRadioLinkStatusFragment;

    private DashboardVideoStreamFragment mDashboardVideoStreamFragment;
    private DashboardSitRepPersonnelStatusFragment mDashboardSitRepPersonnelStatusFragment;
    private DashboardTaskPhaseStatusFragment mDashboardTaskPhaseStatusFragment;
    private DashboardRadioLinkStatusFragment mDashboardRadioLinkStatusFragment;

    private ArrayAdapter<String> mSpinnerBlueprintAdapter;
    private List<String> mSpinnerFloorNameLinkList;
//    private ArrayList<String> mSpinnerFloorNameList;

    // Personnel Link Status
    private ConstraintLayout mConstraintLayoutPersonnelLinkStatus;
    private RelativeLayout mRelativeLayoutPersonnelLinkStatusImgBtn;
    private AppCompatImageView mImgPersonnelLinkStatusIcon;
    private AppCompatImageView mImgPersonnelLinkStatusTriangleIcon;
    private RecyclerView mRecyclerViewPersonnelLinkStatus;
    private MapPersonnelLinkStatusRecyclerAdapter mRecyclerAdapterPersonnelLinkStatus;
    private RecyclerView.LayoutManager mRecyclerLayoutManagerPersonnelLinkStatus;
    private List<UserModel> mPersonnelLinkStatusUserListItems;

    // Killed In Action (KIA)/Hazard and Deceased UI
    private RelativeLayout mRelativeLayoutHazardImgBtn;
    private AppCompatImageView mImgHazardIcon;
    private RelativeLayout mRelativeLayoutDeceasedImgBtn;
    private AppCompatImageView mImgDeceasedIcon;
    private static String mSelectedIconType;

    // Test Logs UI
    private RelativeLayout mRelativeLayoutTestLogToggleImgBtn;
    private AppCompatImageView mImgTestLogToggleIcon;
    private AppCompatButton mBtnNewTestLog;
    private AppCompatButton mBtnAppendTestLog;

    private View mViewTestLogInput;
    private AppCompatImageView mImgTestLogInputOthers;

    private RelativeLayout mLayoutTestLogInputOthers;
    private Spinner mSpinnerTestLog;
    private ArrayAdapter<String> mSpinnerTestLogAdapter;
    private C2OpenSansRegularEditTextView mEtvTestLogOthers;

    private C2OpenSansRegularEditTextView mEtvTestLogHistory;

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
//    private BeaconManagerInterface beaconManager;
//    private BeaconZeroing beaconZeroing;

    private IMQListener mIMQListener;

    // Beacon object message list
    private List<String> mHazardMsgList;
    private List<String> mDeceasedMsgList;
    private BFTModel mOwnBFTModel;

//    private boolean mIsTrackerInitialised; // For tracker to be initialised
//    private boolean mIsTrackerUpdateInitialised; // For onNewCoord listener to fire at least once

    private boolean mIsFragmentVisibleToUser;
    private boolean mIsDbVideoFragmentRefreshed;
    private boolean mIsDbSitRepPersonnelStatusFragmentRefreshed;
    private boolean mIsDbTaskPhaseStatusFragmentRefreshed;
    private boolean mIsDbRadioLinkStatusFragmentRefreshed;
//    private static String mSelectedFloorName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_map_ship_blueprint, container, false);
            initUI(mRootView);

            prefs = new BFTLocalPreferences(MainApplication.getAppContext());

            initTracker();
            initWebviewSettings();
            setupMQListener();
        }

        return mRootView;
    }

    private void initUI(View rootView) {
        mMainLayout = rootView.findViewById(R.id.layout_map_ship_blueprint_fragment);
        mFrameLayoutBlueprint = rootView.findViewById(R.id.layout_map_ship_blueprint);
        mLinearLayoutDashboardFragments = rootView.findViewById(R.id.layout_fragment_dashboard);
        mMiddleDivider = rootView.
                findViewById(R.id.view_map_ship_blueprint_middle_divider);
        mHorizontalSVDashboardFragments = rootView.
                findViewById(R.id.hs_view_map_ship_blueprint_dashboard_fragments);
        mBottomDivider = rootView.
                findViewById(R.id.view_map_ship_blueprint_bottom_divider);

        initPersonnelLinkStatusUI(rootView);

//        if (mWebView != null) {
//            mWebView.evaluateJavascript("javascript: disconnectRabbitMQ()", null);
//        }
        mWebView = rootView.findViewById(R.id.webview_bft);

        mVideoStreamFragment = rootView.
                findViewById(R.id.fragment_dashboard_video_stream);
        mSitRepPersonnelStatusFragment = rootView.
                findViewById(R.id.fragment_dashboard_sitrep_personnel_status);
        mTaskPhaseStatusFragment = rootView.
                findViewById(R.id.fragment_dashboard_task_phase_status);
        mRadioLinkStatusFragment = rootView.
                findViewById(R.id.fragment_dashboard_radio_link_status);

        mDashboardVideoStreamFragment = (DashboardVideoStreamFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_dashboard_video_stream);
        mDashboardSitRepPersonnelStatusFragment = (DashboardSitRepPersonnelStatusFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_dashboard_sitrep_personnel_status);
        mDashboardTaskPhaseStatusFragment = (DashboardTaskPhaseStatusFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_dashboard_task_phase_status);
        mDashboardRadioLinkStatusFragment = (DashboardRadioLinkStatusFragment)
                getChildFragmentManager().findFragmentById(R.id.fragment_dashboard_radio_link_status);

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

        mTextXYZ = rootView.findViewById(R.id.tv_map_blueprint_textXYZ);
        mTextBearing = rootView.findViewById(R.id.tv_map_blueprint_textBearing);
        mTextAction = rootView.findViewById(R.id.tv_map_blueprint_textAction);

        initSideButtons(rootView);
        initTestLogButtons(rootView);
        initFloorSpinner(rootView);

//        final ImageView imgSetting = rootMapShipBlueprintView.findViewById(R.id.img_btn_ship_blueprint_setting);
//        imgSetting.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                startActivity(new Intent(getActivity(), SettingsActivity.class));
//            }
//        });
    }

    private void initPersonnelLinkStatusUI(View rootView) {
        mConstraintLayoutPersonnelLinkStatus = rootView.
                findViewById(R.id.layout_map_ship_blueprint_personnel_link_status);
        View layoutPersonnelLinkStatusImgBtn = rootView.
                findViewById(R.id.layout_map_ship_blueprint_personnel_link_status_icon);
        mRelativeLayoutPersonnelLinkStatusImgBtn = layoutPersonnelLinkStatusImgBtn.
                findViewById(R.id.relative_layout_img_with_img_btn);
        mImgPersonnelLinkStatusIcon = layoutPersonnelLinkStatusImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgPersonnelLinkStatusIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_personnel, null));
        setPersonnelLinkStatusIconUnselectedStateUI();
        mRelativeLayoutPersonnelLinkStatusImgBtn.setOnClickListener(onPersonnelLinkStatusIconClickListener);

        mImgPersonnelLinkStatusTriangleIcon = rootView.findViewById(R.id.img_ship_blueprint_triangle);
        mImgPersonnelLinkStatusTriangleIcon.setVisibility(View.GONE);

        mRecyclerViewPersonnelLinkStatus = rootView.findViewById(R.id.recycler_ship_blueprint_personnel_status_link);
        mRecyclerViewPersonnelLinkStatus.setHasFixedSize(true);
        mRecyclerViewPersonnelLinkStatus.setNestedScrollingEnabled(false);
        mRecyclerViewPersonnelLinkStatus.setVisibility(View.GONE);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            DimensionUtil.setDimensions(mRecyclerViewPersonnelLinkStatus,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (int) getResources().getDimension(
                            R.dimen.map_blueprint_recycler_personnel_link_status_height_for_TL),
                    new LinearLayout(MainApplication.getAppContext()));
        }

        mRecyclerLayoutManagerPersonnelLinkStatus = new LinearLayoutManager(getActivity());
        mRecyclerViewPersonnelLinkStatus.setLayoutManager(mRecyclerLayoutManagerPersonnelLinkStatus);

        if (mPersonnelLinkStatusUserListItems == null) {
            mPersonnelLinkStatusUserListItems = new ArrayList<>();
        }

        mRecyclerAdapterPersonnelLinkStatus = new MapPersonnelLinkStatusRecyclerAdapter(MainApplication.getAppContext(),
                mPersonnelLinkStatusUserListItems, new ArrayList<>());
        mRecyclerViewPersonnelLinkStatus.setAdapter(mRecyclerAdapterPersonnelLinkStatus);
        mRecyclerViewPersonnelLinkStatus.setItemAnimator(new DefaultItemAnimator());
    }

    private void initWebviewSettings() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                updateMapOfHazardBeacon();
                updateMapOfDeceasedBeacon();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient());
//        mWebView.loadUrl(LOCAL_SHIP_BLUEPRINT_DIRECTORY + prefs.getOverview());
        mWebView.addJavascriptInterface(new WebAppInterface(this.getActivity(), prefs, tracker), "Android");

//        setupMessageQueue();
        setupFileSaver();
    }

    private void initFloorSpinner(View rootView) {
        // Floor Spinner
        Spinner spinnerBlueprintList = rootView.findViewById(R.id.spinner_ship_blueprint_floor_selector);
        List<String> spinnerFloorNameList = new ArrayList<>(Arrays.asList(SpinnerItemListDataBank.getInstance().
                getBlueprintFloorStrArray()));
        mSpinnerFloorNameLinkList = Arrays.asList(SpinnerItemListDataBank.getInstance().
                getBlueprintFloorHtmlLinkStrArray());

        mSpinnerBlueprintAdapter = new ArrayAdapter<String>(getActivity(),
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

        spinnerBlueprintList.setAdapter(mSpinnerBlueprintAdapter);
        spinnerBlueprintList.setOnItemSelectedListener(onMapBlueprintFloorItemSelectedListener);
    }

    private AdapterView.OnItemSelectedListener onMapBlueprintFloorItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mWebView.loadUrl(getBlueprintDirectory() +
                            mSpinnerFloorNameLinkList.get(position));

                    //TODO: Temporary Solution; Remove after demo
                    if (prefs != null) {
                        prefs.setOnePixelToMetresFromSelectedMapName(mSpinnerFloorNameLinkList.get(position));
                    }

//                    mSelectedFloorName = mSpinnerFloorNameLinkList.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            };

    /**
     * Initialises Hazard and Deceased icon buttons UI and listeners
     *
     * @param rootView
     */
    private void initSideButtons(View rootView) {
        View layoutMainHazardImgBtn = rootView.findViewById(R.id.layout_map_ship_blueprint_hazard_icon);
        mRelativeLayoutHazardImgBtn = layoutMainHazardImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgHazardIcon = layoutMainHazardImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgHazardIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_btn_hazard, null));
        setHazardIconUnselectedStateUI();

        mRelativeLayoutHazardImgBtn.setOnClickListener(onHazardIconClickListener);

        View layoutDeceasedImgBtn = rootView.findViewById(R.id.layout_map_ship_blueprint_deceased_icon);
        mRelativeLayoutDeceasedImgBtn = layoutDeceasedImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgDeceasedIcon = layoutDeceasedImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgDeceasedIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_btn_deceased, null));
        setDeceasedIconUnselectedStateUI();

        mRelativeLayoutDeceasedImgBtn.setOnClickListener(onDeceasedIconClickListener);

        mHazardMsgList = new ArrayList<>();
        mDeceasedMsgList = new ArrayList<>();
    }

    /**
     * Initialises test log buttons UI and listeners
     *
     * @param rootView
     */
    private void initTestLogButtons(View rootView) {
        // Test log toggle button
        View layoutTestLogToggleImgBtn = rootView.findViewById(R.id.layout_map_ship_blueprint_test_log_toggle_icon);
        mRelativeLayoutTestLogToggleImgBtn = layoutTestLogToggleImgBtn.findViewById(R.id.relative_layout_img_with_img_btn);
        mImgTestLogToggleIcon = layoutTestLogToggleImgBtn.findViewById(R.id.img_pic_within_img_btn);
        mImgTestLogToggleIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_test_log_toggle, null));

        mRelativeLayoutTestLogToggleImgBtn.setOnClickListener(onTestLogToggleIconClickListener);

        // Actual test log button
        mBtnNewTestLog = rootView.findViewById(R.id.btn_map_ship_blueprint_new_test_log);
        mBtnAppendTestLog = rootView.findViewById(R.id.btn_map_ship_blueprint_append_test_log);

        mEtvTestLogHistory = rootView.findViewById(R.id.etv_map_ship_blueprint_test_log);
        mEtvTestLogHistory.setFocusableInTouchMode(false);
        mEtvTestLogHistory.setScrollbarFadingEnabled(false);

        mBtnNewTestLog.setOnClickListener(onNewTestLogIconClickListener);
        mBtnAppendTestLog.setOnClickListener(onAppendTestLogIconClickListener);

        // For test log input UI
        initTestLogInput(rootView);

        setTestLogToggleIconUnselectedStateUI();
    }

    /**
     * Initialise Sit Rep input activity request UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initTestLogInput(View rootView) {
        mViewTestLogInput = rootView.findViewById(R.id.layout_map_ship_blueprint_input_test_log);
        mLayoutTestLogInputOthers = mViewTestLogInput.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgTestLogInputOthers = mViewTestLogInput.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerTestLog = mViewTestLogInput.findViewById(R.id.spinner_broad);
        mEtvTestLogOthers = mViewTestLogInput.findViewById(R.id.etv_spinner_edittext_others_info);
        mEtvTestLogOthers.setOnTouchListener(onViewTouchListener);

        List<String> motionLabelList = new ArrayList<>(Arrays.asList(SpinnerItemListDataBank.getInstance().
                getMotionLabelStrArray()));

        mLayoutTestLogInputOthers.setOnClickListener(onTestLogInputOthersClickListener);

        mSpinnerTestLogAdapter = getSpinnerArrayAdapter(motionLabelList);
        mSpinnerTestLog.setAdapter(mSpinnerTestLogAdapter);

        mEtvTestLogOthers.setHint(MainApplication.getAppContext().
                getString(R.string.map_blueprint_test_log_hint));
    }

    /**
     * Enable internal vertical scrolling for edit text views where content exceed maximum height
     */
    private View.OnTouchListener onViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.hasFocus()) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return false;
        }
    };

    private DebounceOnClickListener onTestLogInputOthersClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
//            mIsActivityInputOthersSelected = !view.isSelected();
//            view.setSelected(mIsActivityInputOthersSelected);

            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgTestLogInputOthers, mSpinnerTestLog, mEtvTestLogOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgTestLogInputOthers, mSpinnerTestLog, mEtvTestLogOthers);
            }
        }
    };

    private void setInputOthersSelectedUI(View view, AppCompatImageView inputOthersImageView,
                                          Spinner spinner, C2OpenSansRegularEditTextView editTextView) {

        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        inputOthersImageView.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        spinner.setVisibility(View.GONE);
        editTextView.setVisibility(View.VISIBLE);
    }

    private void setInputOthersUnselectedUI(View view, AppCompatImageView inputOthersImageView,
                                            Spinner spinner, C2OpenSansRegularEditTextView editTextView) {

        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        inputOthersImageView.setColorFilter(null);
        spinner.setVisibility(View.VISIBLE);
        editTextView.setVisibility(View.GONE);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(List<String> stringArray) {

        return new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_row_item, R.id.tv_spinner_row_item_text, stringArray) {

            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be used as hint
                    return false;
                } else {
                    return true;
                }
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                LinearLayout layoutSpinner = view.findViewById(R.id.layout_spinner_text_item);
                layoutSpinner.setGravity(Gravity.END);
                layoutSpinner.setPadding(0, 0,
                        (int) getResources().getDimension(R.dimen.elements_large_margin_spacing), 0);

                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Set appropriate height for spinner items
                DimensionUtil.setDimensions(view,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (int) getResources().getDimension(R.dimen.spinner_broad_height),
                        new LinearLayout(MainApplication.getAppContext()));

                LinearLayout layoutSpinner = view.findViewById(R.id.layout_spinner_text_item);
                layoutSpinner.setGravity(Gravity.END);
                layoutSpinner.setPadding(0, 0,
                        (int) getResources().getDimension(R.dimen.elements_large_margin_spacing), 0);

                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(android.graphics.Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));
                }

                return view;
            }
        };
    }

    /**
     * -------------------- Personnel Link Status --------------------
     **/

    private void setPersonnelLinkStatusIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutPersonnelLinkStatusImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_white));
        mRelativeLayoutPersonnelLinkStatusImgBtn.setBackground(layoutDrawable);

        mImgPersonnelLinkStatusIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
    }

    private void setPersonnelLinkStatusIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutPersonnelLinkStatusImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutPersonnelLinkStatusImgBtn.setBackground(layoutDrawable);

        mImgPersonnelLinkStatusIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
    }

    private DebounceOnClickListener onPersonnelLinkStatusIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setPersonnelLinkStatusIconSelectedStateUI();
                mImgPersonnelLinkStatusTriangleIcon.setVisibility(View.VISIBLE);
                mRecyclerViewPersonnelLinkStatus.setVisibility(View.VISIBLE);
            } else {
                setPersonnelLinkStatusIconUnselectedStateUI();
                mImgPersonnelLinkStatusTriangleIcon.setVisibility(View.GONE);
                mRecyclerViewPersonnelLinkStatus.setVisibility(View.GONE);
            }
        }
    };

    /**
     * -------------------- Hazard Personnel --------------------
     **/

    private void setHazardIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutHazardImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_white));
        mRelativeLayoutHazardImgBtn.setBackground(layoutDrawable);

        mImgHazardIcon.setColorFilter(ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.dull_orange), PorterDuff.Mode.SRC_ATOP);

        mSelectedIconType = MainApplication.getAppContext().
                getString(R.string.map_blueprint_other_type);
    }

    private void setHazardIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutHazardImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutHazardImgBtn.setBackground(layoutDrawable);

        mImgHazardIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        mSelectedIconType = MainApplication.getAppContext().
                getString(R.string.map_blueprint_hazard_type);
    }

    private DebounceOnClickListener onHazardIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
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

    /**
     * -------------------- Deceased Personnel --------------------
     **/

    private void setDeceasedIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutDeceasedImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_white));
        mRelativeLayoutDeceasedImgBtn.setBackground(layoutDrawable);

        mImgDeceasedIcon.setColorFilter(ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_text_grey), PorterDuff.Mode.SRC_ATOP);

        mSelectedIconType = MainApplication.getAppContext().
                getString(R.string.map_blueprint_other_type);
    }

    private void setDeceasedIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutDeceasedImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutDeceasedImgBtn.setBackground(layoutDrawable);

        mImgDeceasedIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        mSelectedIconType = MainApplication.getAppContext().
                getString(R.string.map_blueprint_deceased_type);
    }

    private DebounceOnClickListener onDeceasedIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
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
     * -------------------- Test Log --------------------
     **/

    /**
     * Set test log UI to invisible
     */
    private void setTestLogToggleIconUnselectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutTestLogToggleImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_white));
        mRelativeLayoutTestLogToggleImgBtn.setBackground(layoutDrawable);

        mImgTestLogToggleIcon.setColorFilter(ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.background_dark_grey),
                PorterDuff.Mode.SRC_ATOP);

        mBtnAppendTestLog.setVisibility(View.GONE);
        mBtnNewTestLog.setVisibility(View.GONE);
        mViewTestLogInput.setVisibility(View.GONE);
        mEtvTestLogHistory.setVisibility(View.GONE);
    }

    /**
     * Set test log UI to visible
     */
    private void setTestLogToggleIconSelectedStateUI() {
        Drawable layoutDrawable = mRelativeLayoutTestLogToggleImgBtn.getBackground();
        layoutDrawable = DrawableCompat.wrap(layoutDrawable);
        DrawableCompat.setTint(layoutDrawable, ContextCompat.getColor(MainApplication.getAppContext(),
                R.color.primary_highlight_cyan));
        mRelativeLayoutTestLogToggleImgBtn.setBackground(layoutDrawable);

        mImgTestLogToggleIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        mBtnAppendTestLog.setVisibility(View.VISIBLE);
        mBtnNewTestLog.setVisibility(View.VISIBLE);
        mViewTestLogInput.setVisibility(View.VISIBLE);
        mEtvTestLogHistory.setVisibility(View.VISIBLE);
    }

//    private void displayStopMotionDnaUiButton() {
//        mImgNewTestLogIcon = mBtnNewTestLog.findViewById(R.id.img_pic_within_img_btn);
//        mImgNewTestLogIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
//                R.drawable.icon_stop, null));
//        mImgNewTestLogIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_red),
//                PorterDuff.Mode.SRC_ATOP);
//    }

//    private void displayAppendTestLogUiButton() {
//        mImgAppendTestLogIcon = mBtnAppendTestLog.findViewById(R.id.img_pic_within_img_btn);
//        mImgAppendTestLogIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
//                R.drawable.icon_append_test_log, null));
//        mImgAppendTestLogIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.background_main_black),
//                PorterDuff.Mode.SRC_ATOP);
//    }
//
//    private void displayNewTestLogUiButton() {
//        mImgNewTestLogIcon = mBtnNewTestLog.findViewById(R.id.img_pic_within_img_btn);
//        mImgNewTestLogIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
//                R.drawable.icon_new_test_log, null));
//        mImgNewTestLogIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.background_main_black),
//                PorterDuff.Mode.SRC_ATOP);
//    }

    private DebounceOnClickListener onTestLogToggleIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setTestLogToggleIconSelectedStateUI();
            } else {
                setTestLogToggleIconUnselectedStateUI();
            }
        }
    };

    /**
     * OnClickListener to create new test log file
     */
    private DebounceOnClickListener onNewTestLogIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {

            if (tracker != null) {

                String motionLabel = mEtvTestLogHistory.getText().toString().trim();

                // Valid entered motion label
                if (!TextUtils.isEmpty(motionLabel)) {
                    FileUtil.saveMotionLogIntoFile(motionLabel);
                }

                tracker.deactivate();
                tracker.startMotionDnaAndCreateNewTestLogFile();

                // Show snackbar message to indicate new test log header has been logged
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_map_blueprint_new_test_log_file_saved_message));
                }

                mEtvTestLogHistory.setText(MainApplication.getAppContext().
                        getString(R.string.map_blueprint_test_log_new_file_created));
            }

        }
    };

    /**
     * OnClickListener to append a new header into existing test log file
     */
    private DebounceOnClickListener onAppendTestLogIconClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {

            if (tracker != null) {

                String motionLabel = "";
                if (mLayoutTestLogInputOthers.isSelected()) {

                    motionLabel = mEtvTestLogOthers.getText().toString().trim();

                    // Valid entered motion label
                    if (!TextUtils.isEmpty(motionLabel)) {

                        // Show snackbar message to indicate new test log header has been logged
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    MainApplication.getAppContext().
                                            getString(R.string.snackbar_map_blueprint_new_test_log_header_saved_message));
                        }

                        // Updates test log spinner data with newly added motion
                        SpinnerItemListDataBank.getInstance().addItemToMotionLabelList(motionLabel);
                        List<String> spinnerTestLogList = new ArrayList<>(Arrays.asList(SpinnerItemListDataBank.
                                getInstance().getMotionLabelStrArray()));
                        mSpinnerTestLogAdapter.clear();
                        mSpinnerTestLogAdapter.addAll(spinnerTestLogList);

                        tracker.appendHeaderToExistingTestLogFile(motionLabel);

                        if (!TextUtils.isEmpty(mEtvTestLogHistory.getText().toString().trim())) {
                            mEtvTestLogHistory.append(System.lineSeparator());
                        }

                        mEtvTestLogHistory.append(motionLabel);

                    } else {

                        // Show snackbar message to request user to enter test log header
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    MainApplication.getAppContext().
                                            getString(R.string.snackbar_map_blueprint_no_test_log_header_message));
                        }

                    }

                } else {

                    // Valid selected spinner item
                    if (mSpinnerTestLog.getSelectedItemPosition() != 0) {

                        motionLabel = mSpinnerTestLog.getSelectedItem().toString();

                        // Show snackbar message to indicate new test log header has been logged
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    MainApplication.getAppContext().
                                            getString(R.string.snackbar_map_blueprint_new_test_log_header_saved_message));
                        }

                        tracker.appendHeaderToExistingTestLogFile(motionLabel);

                        if (!TextUtils.isEmpty(mEtvTestLogHistory.getText().toString().trim())) {
                            mEtvTestLogHistory.append(System.lineSeparator());
                        }

                        mEtvTestLogHistory.append(motionLabel);

                    } else {
                        // Show snackbar message to indicate invalid selected motion label
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    MainApplication.getAppContext().
                                            getString(R.string.snackbar_map_blueprint_motion_label_not_selected));
                        }
                    }

                }

            }
        }
    };

    private String getBlueprintDirectory() {
        if (SpinnerItemListDataBank.getInstance().isLocalBlueprintDirectory()) {
            return LOCAL_SHIP_BLUEPRINT_DIRECTORY;
        } else {
            return EXTERNAL_MAP_BLUEPRINT_DIRECTORY;
        }
    }
//    /**
//     * Refreshes BFT objects on map on fragment load
//     */
//    private synchronized void refreshBFT() {
//        SingleObserver<List<BFTModel>> singleObserverBFTForUser =
//                new SingleObserver<List<BFTModel>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(List<BFTModel> bFTModelList) {
//                        if (bFTModelList != null) {
//                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
//                                    "refreshBFT. " +
//                                    "bFTModelList, size: " + bFTModelList.size());
//
//                            List<BFTModel> currentUserBFTModelList = bFTModelList.stream().
//                                    filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
//                                            equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());
//
//                            // Get own BFT model
//                            List<BFTModel> ownBFTModelList = currentUserBFTModelList.stream().
//                                    filter(bftModel -> MainApplication.getAppContext().
//                                            getString(R.string.map_blueprint_own_type).
//                                            equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());
//
//                            Log.i(TAG, "ownBFTModelList, size:" + ownBFTModelList.size());
//
//                            // There should only be one 'OWN' type BFTModel belonging to a user
//                            if (ownBFTModelList.size() == 1) {
//                                mOwnBFTModel = ownBFTModelList.get(0);
//
//                                if (mOwnBFTModel.getXCoord() != null && mOwnBFTModel.getYCoord() != null &&
//                                        mOwnBFTModel.getAltitude() != null) {
//                                    Coords ownBFTModelCoord = new Coords(0, 0,
//                                            Double.parseDouble(mOwnBFTModel.getAltitude().trim()),
//                                            0, 0, 0, 0,
//                                            Double.parseDouble(mOwnBFTModel.getXCoord().trim()),
//                                            Double.parseDouble(mOwnBFTModel.getYCoord().trim()), null);
//
//                                    Log.i(TAG, "ownBFTModelCoord.getX(): " + ownBFTModelCoord.getX());
//                                    Log.i(TAG, "ownBFTModelCoord.getY(): " + ownBFTModelCoord.getY());
//                                    Log.i(TAG, "ownBFTModelCoord.getAltitude(): " + ownBFTModelCoord.getAltitude());
//
//                                    // Set own BFT model location manually on map refresh
//                                    tracker.setManualLocation(ownBFTModelCoord);
//                                }
//
//                            } else if (ownBFTModelList.size() == 0) {
//
//                                // Created default own BFT model
//                                mOwnBFTModel.setXCoord(StringUtil.DEFAULT_INT);
//                                mOwnBFTModel.setYCoord(StringUtil.DEFAULT_INT);
//                                mOwnBFTModel.setAltitude(StringUtil.DEFAULT_INT);
//                                mOwnBFTModel.setBearing(StringUtil.DEFAULT_INT);
//                                mOwnBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
//                                mOwnBFTModel.setType(MainApplication.getAppContext().
//                                        getString(R.string.map_blueprint_own_type));
//                                mOwnBFTModel.setCreatedDateTime(DateTimeUtil.dateToCustomTimeStringFormat(
//                                        DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime())));
//
//                                Log.i(TAG, "OWN BFT model inserted");
//                                mBFTViewModel.insertBFT(mOwnBFTModel);
//                            }
//
//                            // Refresh 'Hazard' and 'Deceased' type objects on map
//                            if (mHazardMsgList == null || mDeceasedMsgList == null ||
//                                    mHazardMsgList.size() == 0 && mDeceasedMsgList.size() == 0) {
//                                List<BFTModel> hazardBFTModelList = currentUserBFTModelList.stream().
//                                        filter(bftModel -> MainApplication.getAppContext().
//                                                getString(R.string.map_blueprint_hazard_type).
//                                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());
//
//                                List<String> hazardBFTMsgList = formBFTMsgList(hazardBFTModelList);
//                                mHazardMsgList.addAll(hazardBFTMsgList);
//
//                                List<BFTModel> deceasedBFTModelList = currentUserBFTModelList.stream().
//                                        filter(bftModel -> MainApplication.getAppContext().
//                                                getString(R.string.map_blueprint_deceased_type).
//                                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());
//
//                                List<String> deceasedBFTMsgList = formBFTMsgList(deceasedBFTModelList);
//                                mDeceasedMsgList.addAll(deceasedBFTMsgList);
//
//                                updateMapOfHazardBeacon();
//                                updateMapOfDeceasedBeacon();
//                            }
//
//                        } else {
//                            Log.d(TAG, "onSuccess singleObserverBFTForUser, " +
//                                    "refreshBFT. " +
//                                    "bFTModelList is null/empty");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError singleObserverBFTForUser, " +
//                                "refreshBFT. " +
//                                "Error Msg: " + e.toString());
//                    }
//                };
//
//        mBFTViewModel.getAllBFTs(singleObserverBFTForUser);
//    }

    private void initTracker() {
        this.tracker = new NavisensLocalTracker(this.getActivity());
        this.tracker.setTrackerListener(new TrackerListener() {
            @Override
            public synchronized void onNewCoords(Coords coords) {

//                if (!mIsTrackerUpdateInitialised) {
//                    refreshBFT();
//                }
//
//                mIsTrackerUpdateInitialised = true;

                synchronized (MapShipBlueprintFragment.class) {
//                    coords.setX(coords.getY());
//                    coords.setY(coords.getX());
//                    coords.setBearing(coords.getBearing() + 90);

                    Timber.i("X: %s", coords.getX());
                    Timber.i("Y: %s", coords.getY());
                    Timber.i("Z: %s", coords.getAltitude());
                    Timber.i("bearing: %s", coords.getBearing());
                    Timber.i("Action: %s", coords.getAction());
                    Timber.i("RealAlt: %s", coords.getLatitude());

                    updateMap(coords);
//                sendCoords(coords);
                    saveCoords(coords);
                    showCoords(coords);
                }
            }

            @Override
            public void onNewEvent(Event event) {

            }
        });

        Toast.makeText(this.getActivity().getApplicationContext(),
                "Tracker setup complete", Toast.LENGTH_LONG).show();
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
                Timber.e("FileSaver failed to save coords");
            }
        }
    }

    private void showCoords(Coords coords) {
        DecimalFormat df2dec = new DecimalFormat("###.##");
        mTextXYZ.setText("XYZ: " + df2dec.format(coords.getX()) + "  ,  " + df2dec.format(coords.getY()) + "  ,  " + df2dec.format(coords.getAltitude()));
        mTextBearing.setText("Bearing: " + df2dec.format(coords.getBearing()));
        mTextAction.setText("Action: " + coords.getAction());
    }

    private synchronized void updateMap(Coords coords) {
//        if (mOwnBFTModel == null) {
//            mOwnBFTModel = new BFTModel();
//            mOwnBFTModel.setXCoord(String.valueOf(coords.getX()));
//            mOwnBFTModel.setYCoord(String.valueOf(coords.getY()));
//            mOwnBFTModel.setAltitude(String.valueOf(coords.getAltitude()));
//            mOwnBFTModel.setBearing(String.valueOf(coords.getBearing()));
//            mOwnBFTModel.setAction(String.valueOf(coords.getAction()));
//            mOwnBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
//            mOwnBFTModel.setType(EBftType.OWN.toString());
//            mOwnBFTModel.setCreatedDateTime(DateTimeUtil.dateToStandardIsoDateTimeStringFormat(
//                    DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime())));
//
//            addItemToLocalDatabase(mOwnBFTModel);
//
//        } else {
//            mOwnBFTModel.setXCoord(String.valueOf(coords.getX()));
//            mOwnBFTModel.setYCoord(String.valueOf(coords.getY()));
//            mOwnBFTModel.setAltitude(String.valueOf(coords.getAltitude()));
//            mOwnBFTModel.setBearing(String.valueOf(coords.getBearing()));
//            mOwnBFTModel.setAction(String.valueOf(coords.getAction()));
//            mOwnBFTModel.setType(EBftType.OWN.toString());
//
//            insertOrUpdateOwnTypeBftInfo(mOwnBFTModel);
//
//        }

        insertOrUpdateOwnTypeBftInfo(coords);
        accessDatabaseAndRefreshBftUI();
    }

    private void androidToJsCreateObjectAtLocation(String message) {
        if (mWebView != null) {
            mWebView.evaluateJavascript("javascript: " + "androidToJScreateLocation(\""
                    + message + "\")", null);
        }
    }

    private void androidToJsUpdateObjectToLocation(BFTModel bftModel) {
        if (mWebView != null) {
            // Android to Javascript
            String message;

            message = bftModel.getRefId() + "," + bftModel.getXCoord() + "," + bftModel.getYCoord() + "," + bftModel.getAltitude() +
                    "," + bftModel.getBearing() + "," + bftModel.getUserId() +
                    "," + bftModel.getAction() + "," + bftModel.getType();

            Timber.i("Calling JAVASCRIPT with %s", message);

            mWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\""
                    + message + "\")", null);
        }
    }

    /**
     * Insert/Update 'Own' or 'Own-Stale' type BFT info into database
     *
     * @param coords
     */
    private synchronized void insertOrUpdateOwnTypeBftInfo(Coords coords) {

        // If available, there should only be ONE Bft model which has current user Id AND 'Own' or 'Own-Stale' Type.
        // If network causes multiple copies of such Bft models, get the latest one and delete the rest
        SingleObserver<List<BFTModel>> singleObserverGetOwnTypeBFT = new SingleObserver<List<BFTModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<BFTModel> bftModelToUpdateList) {

                Timber.i("onSuccess singleObserverGetOwnTypeBFT, insertOrUpdateOwnTypeBftInfo. bftModelToUpdateList.size(): %s", bftModelToUpdateList.size());

                if (bftModelToUpdateList.size() == 0) {
                    BFTModel ownBFTModel = new BFTModel();
                    ownBFTModel.setXCoord(String.valueOf(coords.getX()));
                    ownBFTModel.setYCoord(String.valueOf(coords.getY()));
                    ownBFTModel.setAltitude(String.valueOf(coords.getAltitude()));
                    ownBFTModel.setBearing(String.valueOf(coords.getBearing()));
                    ownBFTModel.setAction(String.valueOf(coords.getAction()));
                    ownBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                    ownBFTModel.setType(EBftType.OWN.toString());
                    ownBFTModel.setCreatedDateTime(DateTimeUtil.dateToStandardIsoDateTimeStringFormat(
                            DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime())));

                    addItemToLocalDatabase(ownBFTModel);
                }

                BFTModel bftModelToUpdate = null;

                for (int i = 0; i < bftModelToUpdateList.size(); i++) {
                    BFTModel currentBftModel = bftModelToUpdateList.get(i);

                    if (i == 0) {
                        bftModelToUpdate = currentBftModel;

                    } else if (currentBftModel.getCreatedDateTime().
                            compareTo(bftModelToUpdate.getCreatedDateTime()) >= 0) {

                        mBFTViewModel.deleteBFT(bftModelToUpdate.getId());
                        bftModelToUpdate = currentBftModel;

                    }
                }

                // Update existing entry in database
                if (bftModelToUpdate != null) {
                    bftModelToUpdate.setXCoord(String.valueOf(coords.getX()));
                    bftModelToUpdate.setYCoord(String.valueOf(coords.getY()));
                    bftModelToUpdate.setAltitude(String.valueOf(coords.getAltitude()));
                    bftModelToUpdate.setBearing(String.valueOf(coords.getBearing()));
                    bftModelToUpdate.setAction(String.valueOf(coords.getAction()));
                    bftModelToUpdate.setType(EBftType.OWN.toString());

                    mBFTViewModel.updateBFT(bftModelToUpdate);

                    androidToJsUpdateObjectToLocation(bftModelToUpdate);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverGetOwnTypeBFT, insertOrUpdateOwnTypeBftInfo. Error Msg: %s", e.toString());

                BFTModel ownBFTModel = new BFTModel();
                ownBFTModel.setXCoord(String.valueOf(coords.getX()));
                ownBFTModel.setYCoord(String.valueOf(coords.getY()));
                ownBFTModel.setAltitude(String.valueOf(coords.getAltitude()));
                ownBFTModel.setBearing(String.valueOf(coords.getBearing()));
                ownBFTModel.setAction(String.valueOf(coords.getAction()));
                ownBFTModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                ownBFTModel.setType(EBftType.OWN.toString());
                ownBFTModel.setCreatedDateTime(DateTimeUtil.dateToStandardIsoDateTimeStringFormat(
                        DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime())));

                addItemToLocalDatabase(ownBFTModel);
            }
        };

        mBFTViewModel.queryBFTByUserIdAndOwnType(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverGetOwnTypeBFT);
    }

    /**
     * Stores Bft data locally with updated Ref Id
     *
     * @param bftModel
     */
    private void addItemToLocalDatabase(BFTModel bftModel) {

        SingleObserver<Long> singleObserverAddBft = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long bftId) {
                Timber.i("onSuccess singleObserverAddBft, addItemToLocalDatabase. BftId: %d", bftId);

                updateBftModelRefId(bftId);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAddBft, addItemToLocalDatabase. Error Msg: %s ", e.toString());
            }
        };

        mBFTViewModel.insertBFTWithObserver(bftModel, singleObserverAddBft);
    }

    /**
     * Updates Ref Id of newly inserted BFT model
     *
     * @param bftId
     */
    private void updateBftModelRefId(Long bftId) {
        SingleObserver<BFTModel> singleObserverUpdateBftRefId = new SingleObserver<BFTModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(BFTModel bftModel) {
                Timber.i("onSuccess singleObserverUpdateBftRefId, updateBftModelRefId. BftId: %d", bftId);

                bftModel.setRefId(bftId);
                mBFTViewModel.updateBFT(bftModel);

                androidToJsUpdateObjectToLocation(bftModel);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverUpdateBftRefId, updateBftModelRefId. Error Msg: %s ", e.toString());
            }
        };

        mBFTViewModel.queryBFTById(bftId, singleObserverUpdateBftRefId);
    }

    /**
     * Set Wave Relay Radio Info model into recycler adapter of personnel link status
     */
    private synchronized void setWaveRelayRadiosInfo() {
        WaveRelayRadioRepository waveRelayRadioRepository = new
                WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadio = new
                SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
                        Timber.i("onSuccess singleObserverAllWaveRelayRadio, setWaveRelayRadiosInfo. waveRelayRadioModel.size(): %d", waveRelayRadioModelList.size());

                        mRecyclerAdapterPersonnelLinkStatus.setWaveRelayListItems(waveRelayRadioModelList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError singleObserverAllWaveRelayRadio, " +
                                "setWaveRelayRadiosInfo. " +
                                "Error Msg: " + e.toString());
                    }
                };

        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverAllWaveRelayRadio);
    }

//    private void updateMapOfBeacon(Coords coords, String beaconId) {
////        // Android to Javascript
////        String message = coords.getX() + "," + coords.getY() + "," + coords.getAltitude() +
////                "," + coords.getBearing() + "," + beaconId + "," + BeaconZeroing.BEACONOBJ;
////
////        Log.d(TAG, "Calling JAVASCRIPT with " + message);
////
////        mWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" + message + "\")", null);
////    }

    /**
     * (Android to Javascript)
     * Add a Hazard beacon to represent a hazardous object
     */
    private void updateMapOfHazardBeacon() {
        for (int i = 0; i < mHazardMsgList.size(); i++) {
            androidToJsCreateObjectAtLocation(mHazardMsgList.get(i));

            Timber.i("Calling JAVASCRIPT with Hazard Msg List: %s", mHazardMsgList.get(i));

        }
    }

    /**
     * (Android to Javascript)
     * Add a Deceased beacon to represent a deceased person
     */
    private void updateMapOfDeceasedBeacon() {
        for (int i = 0; i < mDeceasedMsgList.size(); i++) {
            androidToJsCreateObjectAtLocation(mDeceasedMsgList.get(i));

            Timber.i("Calling JAVASCRIPT with Deceased Msg List: %s ", mDeceasedMsgList.get(i));

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
            Timber.e("FileSaver cannot initialise");

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
//                    if (!JSONUtil.isJSONValid(message)) {
//
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
//                    }


//                    Timber.i("New BFT message: %s ", message);
//
//                    String messageTopic = StringUtil.getFirstWord(message);
//                    String messageContent = StringUtil.removeFirstWord(message);
//                    String[] messageTopicParts = messageTopic.split(StringUtil.HYPHEN);
//
//                    String messageTopicAction = messageTopicParts[2];
//
//                    switch (messageTopicAction) {
//                        case JeroMQParent.TOPIC_OWN_FORCE:
//                            Timber.i("onNewBFTMessage (OWN Force) insert");
//
//                            if (!StringUtil.EMPTY_STRING.equalsIgnoreCase(messageContent)) {
//                                mWebView.evaluateJavascript("javascript: " + "androidToJSupdateLocation(\"" +
//                                        messageContent + "\")", null);
//                            }
//
//                            break;
//
//                        case JeroMQParent.TOPIC_OTHERS:
//                            Timber.i("onNewBFTMessage (Others) insert");
//
//                            if (!StringUtil.EMPTY_STRING.equalsIgnoreCase(messageContent)) {
//                                mWebView.evaluateJavascript("javascript: " + "androidToJScreateLocation(\""
//                                        + messageContent + "\")", null);
//                            }
//
//                            break;
//                    }
                }
            };

//            RabbitMQHelper.getInstance().addRabbitListener(mIMQListener);

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

//        String pattern = "yyyyMMddHHmmss";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//        String date = simpleDateFormat.format(new Date());
//        String message = _coords.getX() + "," + _coords.getY() + "," + _coords.getAltitude() + "," +
//                _coords.getBearing() + "," + SharedPreferenceUtil.getCurrentUserCallsignID() + "," +
//                _coords.getAction() + "," + _coords.getLatitude() + "," + date;

//        RabbitMQHelper.getInstance().sendBFTMessage(message);
//        JeroMQBroadcastOperation.broadcastBftOwnForceDataOverSocket(message);


//        JeroMQBroadcastOperation.broadcastBftOthersDataOverSocket(message);


//        SingleObserver<List<BFTModel>> singleObserverGetOwnTypeBFT = new SingleObserver<List<BFTModel>>() {
//            @Override
//            public void onSubscribe(Disposable d) {}
//
//            @Override
//            public void onSuccess(List<BFTModel> bftModelList) {
//
//                Timber.i("onSuccess singleObserverGetOwnTypeBFT, sendCoords. bftModelList.size(): %d" , bftModelList.size());
//
//                // If available, There should only be ONE Bft model which has current user Id AND 'Own' Type
//                if (bftModelList.size() == 1) {
//
//                } else { // Insert new entry of Own Type Bft model
//
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//                Timber.e("onError singleObserverGetOwnTypeBFT, sendCoords. Error Msg: %s" , e.toString());
//
//            }
//        };
//
//        mBFTViewModel.queryBFTByUserIdAndOwnType(SharedPreferenceUtil.getCurrentUserCallsignID(),
//                EBftType.OWN.toString(), singleObserverGetOwnTypeBFT);
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
        return mSelectedIconType;
    }

//    public static String getSelectedFloorName() {
//        return mSelectedFloorName;
//    }

//    /**
//     * Get own BFT model from database and updates with latest location for future viewing
//     */
//    private void updateOwnBFTModel() {
//
//        Timber.i("Update own BFT model: %s ", mOwnBFTModel);
//
//        SingleObserver<List<BFTModel>> singleObserverBFTForUser =
//                new SingleObserver<List<BFTModel>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(List<BFTModel> bFTModelList) {
//                        if (bFTModelList != null) {
//
//                            Timber.i("onSuccess singleObserverBFTForUser, updateOwnBFTModel. bFTModelList, size: %s", bFTModelList.size());
//
//
//                            // Get BFT models belonging to a current user (including hazard and deceased entities)
//                            List<BFTModel> currentUserBFTModelList = bFTModelList.stream().
//                                    filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
//                                            equalsIgnoreCase(bftModel.getUserId())).collect(Collectors.toList());
//
//                            // Get own BFT model
//                            List<BFTModel> ownBFTModelList = currentUserBFTModelList.stream().
//                                    filter(bftModel -> MainApplication.getAppContext().
//                                            getString(R.string.map_blueprint_own_type).
//                                            equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());
//
//                            Timber.i("ownBFTModelList, size: %s", ownBFTModelList.size());
//
//
//                            // There should only be one 'own' type BFTModel belonging to a user
//                            if (ownBFTModelList.size() == 1) {
//                                BFTModel ownBFTModel = ownBFTModelList.get(0);
//
//                                // Update location and push updated model back to database
//                                ownBFTModel.setXCoord(mOwnBFTModel.getXCoord());
//                                ownBFTModel.setYCoord(mOwnBFTModel.getYCoord());
//                                ownBFTModel.setAltitude(mOwnBFTModel.getAltitude());
//                                ownBFTModel.setBearing(mOwnBFTModel.getBearing());
//                                ownBFTModel.setAction(mOwnBFTModel.getAction());
//                                mBFTViewModel.updateBFT(ownBFTModel);
//                            }
//
//                        } else {
//                            Timber.i("onSuccess singleObserverBFTForUser, updateOwnBFTModel. bFTModelList is null");
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Timber.e("onError singleObserverBFTForUser, updateOwnBFTModel.bError Msg: %s", e.toString());
//
//
//                    }
//                };
//
//        mBFTViewModel.getAllBFTs(singleObserverBFTForUser);
//    }

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

            String currentTimeToDisplay = DateTimeUtil.dateToCustomTimeStringFormat(
                    DateTimeUtil.stringToISO8601Date(bFTModel.getCreatedDateTime()));

//            String createdTime = DateTimeUtil.dateToCustomTimeStringFormat(
//                    DateTimeUtil.stringToDate(bFTModel.getCreatedDateTime()));

            StringBuilder msgStrBuilder = new StringBuilder();
            msgStrBuilder.append(bFTModel.getId());
            msgStrBuilder.append(StringUtil.COMMA);
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
            msgStrBuilder.append(currentTimeToDisplay);

            msgList.add(msgStrBuilder.toString());
        }

        return msgList;
    }

    /**
     * Get own BFT model from database and updates with latest location for future viewing
     */
    private void accessDatabaseAndRefreshBftUI() {

        Timber.i("Pulling BFT models from database...");

        SingleObserver<List<BFTModel>> singleObserverGetAllBFTs =
                new SingleObserver<List<BFTModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<BFTModel> bFTModelList) {

                        if (bFTModelList != null) {
                            Timber.i("onSuccess singleObserverGetAllBFTs, accessDatabaseAndRefreshBftUI. bFTModelList size: %s", bFTModelList.size());
                            refreshBftUI(bFTModelList);

                        } else {
                            Timber.i("onSuccess singleObserverGetAllBFTs, accessDatabaseAndRefreshBftUI. bFTModelList is empty");

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError singleObserverGetAllBFTs, accessDatabaseAndRefreshBftUI. Error Msg: %s", e.toString());

                    }
                };

        mBFTViewModel.getAllBFTs(singleObserverGetAllBFTs);
    }

    /**
     * There are FIVE groups of BFT models to check and update:
     * 1) Current user 'Own' BFT
     * 2) Other users 'Own' BFT
     * 3) 'Hazard' BFT
     * 4) 'Deceased' BFT
     *
     * @param bFTModelList
     */
    private void refreshBftUI(List<BFTModel> bFTModelList) {
        Timber.i("Refreshing UI of all BFT models...");

        /** ---------- Current user 'Own' BFT model ---------- **/
        List<BFTModel> currentUserOwnBFTModelList = bFTModelList.stream().
                filter(bftModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                        equalsIgnoreCase(bftModel.getUserId()) &&
                        EBftType.OWN.toString().
                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

        // If available, there should only be ONE 'Own' Bft model of current user
        if (currentUserOwnBFTModelList.size() == 1) {
            BFTModel bftModel = currentUserOwnBFTModelList.get(0);

            // Automatically initialise location from database if user
            // has not initialise current location and database has this info
            if (tracker != null && mWebView != null && !WebAppInterface.mIsLocationInitialised) {
                tracker.setManualLocation(new Coords(0, 0,
                        Double.parseDouble(bftModel.getAltitude()), Double.parseDouble(bftModel.getBearing()),
                        0, 0, 0, Double.parseDouble(bftModel.getXCoord()),
                        Double.parseDouble(bftModel.getYCoord()), null));
            }
        }

        /** ---------- Other users' 'Own' BFT model ---------- **/
        List<BFTModel> otherUsersOwnBFTModelList = bFTModelList.stream().
                filter(bftModel -> !SharedPreferenceUtil.getCurrentUserCallsignID().
                        equalsIgnoreCase(bftModel.getUserId()) &&
                        EBftType.OWN.toString().
                                equalsIgnoreCase(bftModel.getType()) ||
                        EBftType.OWN_STALE.toString().
                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

        for (int i = 0; i < otherUsersOwnBFTModelList.size(); i++) {

            BFTModel bftModel = otherUsersOwnBFTModelList.get(i);

            String message = bftModel.getId() + "," + bftModel.getXCoord() + "," + bftModel.getYCoord() + "," + bftModel.getAltitude() +
                    "," + bftModel.getBearing() + "," + bftModel.getUserId() +
                    "," + bftModel.getAction() + "," + bftModel.getType();

            if (mWebView != null) {
                mWebView.evaluateJavascript("javascript: " +
                        "androidToJSupdateLocation(\"" + message + "\")", null);
            }
        }

        /** ---------- 'Hazard' or 'Hazard-Stale' BFT models ---------- **/
        if (mHazardMsgList == null) {
            mHazardMsgList = new ArrayList<>();
        } else {
            mHazardMsgList.clear();
        }

        List<BFTModel> hazardBftModelList = bFTModelList.stream().
                filter(bftModel -> EBftType.HAZARD.toString().
                        equalsIgnoreCase(bftModel.getType()) ||
                        EBftType.HAZARD_STALE.toString().
                                equalsIgnoreCase(bftModel.getType())).
                collect(Collectors.toList());

        List<String> hazardBftMsgList = formBFTMsgList(hazardBftModelList);
        mHazardMsgList.addAll(hazardBftMsgList);

        for (int i = 0; i < hazardBftMsgList.size(); i++) {
            String hazardBftMsg = hazardBftMsgList.get(i);

            androidToJsCreateObjectAtLocation(hazardBftMsg);
        }

        /** ---------- 'Deceased' or 'Deceased-Stale' BFT models ---------- **/
        if (mDeceasedMsgList == null) {
            mDeceasedMsgList = new ArrayList<>();
        } else {
            mDeceasedMsgList.clear();
        }

        List<BFTModel> deceasedBftModelList = bFTModelList.stream().
                filter(bftModel -> EBftType.DECEASED.toString().
                        equalsIgnoreCase(bftModel.getType()) ||
                        EBftType.DECEASED_STALE.toString().
                                equalsIgnoreCase(bftModel.getType())).collect(Collectors.toList());

        List<String> deceasedBftMsgList = formBFTMsgList(deceasedBftModelList);
        mDeceasedMsgList.addAll(deceasedBftMsgList);

        for (int i = 0; i < deceasedBftMsgList.size(); i++) {
            String deceasedBftMsg = deceasedBftMsgList.get(i);

            androidToJsCreateObjectAtLocation(deceasedBftMsg);
        }
    }

    /**
     * -------------------- UI Components Visibility Methods --------------------
     **/
    public void setBlueprintVisibility(int isVisible) {
        mFrameLayoutBlueprint.setVisibility(isVisible);
        mConstraintLayoutPersonnelLinkStatus.setVisibility(isVisible);
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

    public View getLinearLayoutDashboardFragments() {
        return mLinearLayoutDashboardFragments;
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
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mMapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        mBFTViewModel = ViewModelProviders.of(this).get(BFTViewModel.class);

//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String userId = sharedPrefs.getString(SharedPreferenceConstants.USER_ID,
//                SharedPreferenceConstants.DEFAULT_STRING);

        /*
         * Refreshes nodes whenever there is a change in BFTs (insert, update or delete)
         */
        mBFTViewModel.getAllBFTsLiveData().observe(this, new Observer<List<BFTModel>>() {
            @Override
            public void onChanged(@Nullable List<BFTModel> bFTModelList) {
                refreshBftUI(bFTModelList);
            }
        });

        /*
         * Refreshes recyclerview UI whenever there is a change in user data (insert, update or delete)
         */
        mUserViewModel.getAllUsersLiveData().observe(this, new Observer<List<UserModel>>() {
            @Override
            public void onChanged(@Nullable List<UserModel> userModelList) {

                synchronized (mPersonnelLinkStatusUserListItems) {
                    if (mPersonnelLinkStatusUserListItems == null) {
                        mPersonnelLinkStatusUserListItems = new ArrayList<>();
                    } else {
                        mPersonnelLinkStatusUserListItems.clear();
                    }

                    if (userModelList != null) {
                        mPersonnelLinkStatusUserListItems.addAll(userModelList);
                    }

                    if (mRecyclerAdapterPersonnelLinkStatus != null) {
                        mRecyclerAdapterPersonnelLinkStatus.setUserListItems(mPersonnelLinkStatusUserListItems);
                        setWaveRelayRadiosInfo();
                    }
                }
            }
        });

        /*
         * Refreshes spinner UI whenever there is a change in Map data (insert or update)
         */
        mMapViewModel.getAllMapLiveData().observe(this, new Observer<List<MapModel>>() {
            @Override
            public void onChanged(@Nullable List<MapModel> mapModelList) {

                if (mapModelList != null && mapModelList.size() != 0) {

                    FileUtil.createHtmlFilesFromImagesUsingAssetsTemplate(mapModelList);
                    SpinnerItemListDataBank.getInstance().repopulateBlueprintDetails();

                    // Update BFT pixel to metres conversion factor (Referencing the first map file's GA scale)
                    prefs.setOnePixelToMetresFromExternalFolder(Float.valueOf(mapModelList.get(0).getGaScale()));

                    List<String> spinnerFloorNameList = new ArrayList<>(Arrays.asList(SpinnerItemListDataBank.getInstance().
                            getBlueprintFloorStrArray()));
                    mSpinnerBlueprintAdapter.clear();
                    mSpinnerBlueprintAdapter.addAll(spinnerFloorNameList);
                    mSpinnerFloorNameLinkList = Arrays.asList(SpinnerItemListDataBank.getInstance().
                            getBlueprintFloorHtmlLinkStrArray());

                    // Reload default (first) html link of web view for every new update
                    mWebView.loadUrl(getBlueprintDirectory() +
                            mSpinnerFloorNameLinkList.get(0));

                }
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

    /**
     * Properly releases all resources related to / referenced by fragment (itself)
     * when main (parent) activity is destroyed
     */
    public void destroySelf() {
        if (tracker != null) {
            tracker.deactivate();
        }

        if (fs != null) {
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        // Save the fragments' instance
//        getChildFragmentManager().putFragment(outState, "myFragmentName", mDashboardSitRepPersonnelStatusFragment);
//        getChildFragmentManager().putFragment(outState, "myFragmentName", mDashboardTaskPhaseStatusFragment);
//        getChildFragmentManager().putFragment(outState, "myFragmentName", mDashboardRadioLinkStatusFragment);
//    }

    private boolean isViewFullyVisible(HorizontalScrollView scrollView, View view) {
        Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);

//        float top = view.getY();
//        float bottom = top + view.getHeight();
        float left = view.getX();
        float right = left + view.getWidth();

        Timber.i("scrollBounds.left: " + scrollBounds.left);
        Timber.i("left: " + left);

//        if (scrollBounds.top < top && scrollBounds.bottom < bottom &&
        if (scrollBounds.left < left && scrollBounds.right > right) {
            return true;
        } else {
            return false;
        }
    }

    private void updateVisibleDashboardViews(HorizontalScrollView scrollView) {
        Rect scrollBounds = new Rect();
        scrollView.getHitRect(scrollBounds);
        if (isViewFullyVisible(scrollView, mVideoStreamFragment)) {
            // Any portion of the imageView, even a single pixel, is within the visible window
//            Timber.i("mVideoStreamFragment is VISIBLE");

            if (mDashboardVideoStreamFragment != null &&
                    !mIsDbVideoFragmentRefreshed) {
                mDashboardVideoStreamFragment.onVisible();
            }

            mIsDbVideoFragmentRefreshed = true;
        } else {
            mIsDbVideoFragmentRefreshed = false;
        }

//        if (mSitRepPersonnelStatusFragment.getLocalVisibleRect(scrollBounds)) {
        if (isViewFullyVisible(scrollView, mSitRepPersonnelStatusFragment)) {
//            Timber.i("mSitRepPersonnelStatusFragment is VISIBLE");

            if (mDashboardSitRepPersonnelStatusFragment != null &&
                    !mIsDbSitRepPersonnelStatusFragmentRefreshed) {
                mDashboardSitRepPersonnelStatusFragment.onVisible();
            }

            mIsDbSitRepPersonnelStatusFragmentRefreshed = true;

        } else {
            mIsDbSitRepPersonnelStatusFragmentRefreshed = false;
        }

        if (isViewFullyVisible(scrollView, mTaskPhaseStatusFragment)) {
//            Timber.i("mTaskPhaseStatusFragment is VISIBLE");

            if (mDashboardTaskPhaseStatusFragment != null &&
                    !mIsDbTaskPhaseStatusFragmentRefreshed) {
                mDashboardTaskPhaseStatusFragment.onVisible();
            }

            mIsDbTaskPhaseStatusFragmentRefreshed = true;

        } else {
            mIsDbTaskPhaseStatusFragmentRefreshed = false;
        }

        if (isViewFullyVisible(scrollView, mRadioLinkStatusFragment)) {
//            Timber.i("mRadioLinkStatusFragment is VISIBLE");

            if (mDashboardRadioLinkStatusFragment != null &&
                    !mIsDbRadioLinkStatusFragmentRefreshed) {
                mDashboardRadioLinkStatusFragment.onVisible();
            }

            mIsDbRadioLinkStatusFragmentRefreshed = true;

        } else {
            mIsDbRadioLinkStatusFragmentRefreshed = false;
        }
    }

    private void onVisible() {
        Timber.i("onVisible");

//        if (mHorizontalSVDashboardFragments != null) {
////            updateVisibleDashboardViews(mHorizontalSVDashboardFragments);
////
////            mHorizontalSVDashboardFragments.setOnScrollChangeListener(new View.OnScrollChangeListener() {
////                @Override
////                public void onScrollChange(View scrollView, int x, int y, int oldX, int oldY) {
////                    updateVisibleDashboardViews(mHorizontalSVDashboardFragments);
////                }
////            });
////        }

        if (mDashboardVideoStreamFragment != null) {
            mDashboardVideoStreamFragment.onVisible();
        }

        if (mDashboardSitRepPersonnelStatusFragment != null) {
            mDashboardSitRepPersonnelStatusFragment.onVisible();
        }

        if (mDashboardTaskPhaseStatusFragment != null) {
            mDashboardTaskPhaseStatusFragment.onVisible();
        }

        if (mDashboardRadioLinkStatusFragment != null) {
            mDashboardRadioLinkStatusFragment.onVisible();
        }

//        if ((tracker == null || !tracker.isActive()) && !mIsTrackerInitialised) {
////            initBeacon();
//            Log.i(TAG, "onVisible: Initialising tracker, " +
//                    "webview settings and refreshing BFT");
//
//            mIsTrackerInitialised = true;
//            initTracker();
//            initWebviewSettings();
//        }

//        if ((tracker == null || !tracker.isActive())) {
////            initBeacon();
//            Log.i(TAG, "onVisible: Initialising tracker, " +
//                    "webview settings and refreshing BFT");
//
//            initTracker();
//            initWebviewSettings();
//        }

        // Show snackbar message to request user for location initialisation
        if (getSnackbarView() != null) {
            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                    MainApplication.getAppContext().
                            getString(R.string.snackbar_map_blueprint_init_location_message));
        }

//        beaconManager.enableForegroundDispatch();
    }

    private void onInvisible() {
        Timber.i("onInvisible");
//        updateOwnBFTModel();

        mIsDbVideoFragmentRefreshed = false;
        mIsDbSitRepPersonnelStatusFragmentRefreshed = false;
        mIsDbTaskPhaseStatusFragmentRefreshed = false;
        mIsDbRadioLinkStatusFragmentRefreshed = false;

//        if (mRelativeLayoutHazardImgBtn.isSelected()) {
//            mRelativeLayoutHazardImgBtn.setSelected(false);
//            setHazardIconUnselectedStateUI();
//        }
//
//        if (mRelativeLayoutDeceasedImgBtn.isSelected()) {
//            mRelativeLayoutDeceasedImgBtn.setSelected(false);
//            setDeceasedIconUnselectedStateUI();
//        }
//
//        if (tracker != null) {
//            tracker.deactivate();
//            mIsTrackerInitialised = false;
//            mIsTrackerUpdateInitialised = false;
//        }

//        beaconManager.disableForegroundDispatch();
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
    public void onResume() {
        super.onResume();

//        beaconManager.enableForegroundDispatch();
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
    public void onPause() {
        super.onPause();

//        beaconManager.disableForegroundDispatch();
    }

    @Override
    public void onStop() {
        super.onStop();

//        EventBus.getDefault().unregister(this);

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }

//        beaconManager.disableForegroundDispatch();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Timber.i("onDestroy");


//        if (tracker != null) {
//            tracker.deactivate();
//        }
//
//        if (fs != null) {
//            try {
//                fs.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
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

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(BftEvent bftEvent) {
//        if (bftEvent != null) {
//            String bftMessage = bftEvent.getBftMessage();
//
//            Timber.i("New bftEvent message: %s ", bftMessage);
//
//            if (!StringUtil.EMPTY_STRING.equalsIgnoreCase(bftMessage) && mIMQListener != null) {
//                mIMQListener.onNewMessage(bftMessage);
//            }
//        }
////        if (SettingsActivity.class.getSimpleName().equalsIgnoreCase(bftEvent.getPreviousActivityName())) {
////            System.out.println("Settings Activity found!");
////            setupMessageQueue();
////            initUI(mRootMapView, mSavedInstanceState);
////        }
//    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        Timber.i("setUserVisibleHint");
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Timber.i("setUserVisibleHint onVisible");
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
