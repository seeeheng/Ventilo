package sg.gov.dsta.mobileC3.ventilo.activity.main;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.NoSwipeViewPager;
import sg.gov.dsta.mobileC3.ventilo.R;
//import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus.RadioLinkStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.task.TaskFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.user.UserSettingsFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.listener.DebounceOnClickListener;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.WaveRelayRadioViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioAsyncTask;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioClient;
import sg.gov.dsta.mobileC3.ventilo.repository.ExcelSpreadsheetRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.FileUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ListenerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.videoStream.EOwner;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private WaveRelayRadioViewModel mWaveRelayRadioViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    // Main
    private RelativeLayout mMainLayout;

    private NoSwipeViewPager mNoSwipeViewPager;
//    private BottomNavigationView mBottomNavigationView;

    // Side Tab Panel
    private View mViewSideMenuPanel;
    private ImageView mImgViewTabMap;
    private ImageView mImgViewTabVideoStream;
    private ImageView mImgViewTabReport;
    private ImageView mImgViewTabTimeline;
    private ImageView mImgViewTabTask;
    private ImageView mImgViewTabRadioLink;

    private LinearLayout mLinearLayoutLineSelectorMap;
    private LinearLayout mLinearLayoutLineSelectorVideoStream;
    private LinearLayout mLinearLayoutLineSelectorReport;
    private LinearLayout mLinearLayoutLineSelectorTimeline;
    private LinearLayout mLinearLayoutLineSelectorTask;
    private LinearLayout mLinearLayoutLineSelectorRadioLink;

    // Bottom Panel
    private View mViewBottomPanel;
    private C2OpenSansBoldTextView mTvFragmentTitle;
    private AppCompatImageView mImgRadioLinkStatus;
    private C2OpenSansBoldTextView mTvRadioLinkStatus;
    private C2OpenSansBoldTextView mTvRadioNumber;
    private C2OpenSansBoldTextView mTvLastConnectionDateTime;
    private AppCompatImageView mImgSetting;
    private C2OpenSansBoldTextView mTvSetting;

    // Snackbar
    private View mViewSnackbar;

    //    private boolean mIsServiceRegistered;
//    private Intent mNetworkIntent;
    private BroadcastReceiver mRabbitMqBroadcastReceiver;
    private BroadcastReceiver mExcelBroadcastReceiver;
    private BroadcastReceiver mWaveRelayClientBroadcastReceiver;
    private BroadcastReceiver mSitRepFileSaveBroadcastReceiver;
    private BroadcastReceiver mMotionLogFileSaveBroadcastReceiver;
//    private BroadcastReceiver mUSBDetectionBroadcastReceiver;

    private IMQListener mIMQListener;
//    private MapView mMapView;

    private WaveRelayRadioAsyncTask mWaveRelayRadioAsyncTask;
//    private WaveRelayRadioClient mWaveRelayRadioClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainLayout = findViewById(R.id.layout_main_activity);

        observerSetup();
        initNetworkAndPopulateVideoStreamData();
        initSideMenuPanel();
        initBottomPanel();
        initSnackbar();

        // Pull data once from database
        pullDataFromExcelToDatabase();

        MainStatePagerAdapter mainStatePagerAdapter = new MainStatePagerAdapter(
                getSupportFragmentManager(), getApplication().getApplicationContext());
        mNoSwipeViewPager = findViewById(R.id.viewpager_main_nav);
        mNoSwipeViewPager.setAdapter(mainStatePagerAdapter);
        mNoSwipeViewPager.setPagingEnabled(false);
        mNoSwipeViewPager.setOffscreenPageLimit(MainNavigationConstants.SIDE_MENU_TAB_TOTAL_COUNT - 1);
    }

    /**
     * Initialises Wave Relay radio network, updates link status of local database and
     * populate video stream data
     */
    private void initNetworkAndPopulateVideoStreamData() {
        Bundle b = getIntent().getBundleExtra(MainNavigationConstants.WAVE_RELAY_RADIO_NO_BUNDLE_KEY);
        int radioId = b.getInt(MainNavigationConstants.WAVE_RELAY_RADIO_NO_KEY);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                radioId);

        getDeviceIpAddrAndRunWrRadioSocketWithVideoStreamDataPopulation(radioId);
    }

    /**
     * Initialises side panel UI which includes the following tabs fragments:
     * 1) Map Blueprint
     * 2) Video Stream
     * 3) Situation Report
     * 4) Timeline
     * 5) Task
     * 6) Radio Link Status
     */
    private void initSideMenuPanel() {
        mViewSideMenuPanel = findViewById(R.id.layout_main_side_menu_panel);

        // Tab Views
        RelativeLayout relativeLayoutTabMap = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_map_selector_status);
        RelativeLayout relativeLayoutTabVideoStream = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_video_stream_selector_status);
        RelativeLayout relativeLayoutTabReport = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_report_selector_status);
        RelativeLayout relativeLayoutTabTimeline = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_timeline_selector_status);
        RelativeLayout relativeLayoutTabTask = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_task_selector_status);
        RelativeLayout relativeLayoutTabRadioLink = mViewSideMenuPanel.
                findViewById(R.id.layout_tab_radio_link_selector_status);

        // Line within Tab View
        mLinearLayoutLineSelectorMap = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_map_line_selector);
        mLinearLayoutLineSelectorVideoStream = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_video_stream_line_selector);
        mLinearLayoutLineSelectorReport = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_report_line_selector);
        mLinearLayoutLineSelectorTimeline = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_timeline_line_selector);
        mLinearLayoutLineSelectorTask = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_task_line_selector);
        mLinearLayoutLineSelectorRadioLink = mViewSideMenuPanel.
                findViewById(R.id.linear_layout_radio_link_line_selector);

        // Image Views within Tab View
        mImgViewTabMap = mViewSideMenuPanel.findViewById(R.id.img_tab_map);
        mImgViewTabVideoStream = mViewSideMenuPanel.findViewById(R.id.img_tab_video_stream);
        mImgViewTabReport = mViewSideMenuPanel.findViewById(R.id.img_tab_report);
        mImgViewTabTimeline = mViewSideMenuPanel.findViewById(R.id.img_tab_timeline);
        mImgViewTabTask = mViewSideMenuPanel.findViewById(R.id.img_tab_task);
        mImgViewTabRadioLink = mViewSideMenuPanel.findViewById(R.id.img_tab_radio_link);

        // Tab Views OnClickListeners
        relativeLayoutTabMap.setOnClickListener(onMapTabClickListener);
        relativeLayoutTabVideoStream.setOnClickListener(onVideoStreamTabClickListener);
        relativeLayoutTabReport.setOnClickListener(onReportTabClickListener);
        relativeLayoutTabTimeline.setOnClickListener(onTimelineTabClickListener);
        relativeLayoutTabTask.setOnClickListener(onTaskTabClickListener);
        relativeLayoutTabRadioLink.setOnClickListener(onRadioLinkTabClickListener);

        // Map selected by default on start up
        removeLineSelector();
        setMapSelectedUI();
    }

    /**
     * Initialises bottom panel UI which includes the following:
     * 1) Fragment title
     * 2) Link status of current user
     * 3) Radio number
     * 4) Date & Time of last connection
     * 5) Callsign of current user
     * 6) Settings button
     */
    private void initBottomPanel() {
        mViewBottomPanel = findViewById(R.id.layout_main_bottom_panel);

        mTvFragmentTitle = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_fragment_title);

        mImgRadioLinkStatus = mViewBottomPanel.findViewById(R.id.img_bottom_panel_radio_link_status_icon);
        mTvRadioLinkStatus = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_radio_link_status);
        mTvRadioNumber = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_radio_number);
        mTvLastConnectionDateTime = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_last_connection_date_time);

        String lastConnectionDateTimeNoRecords = getString(R.string.radio_link_status_offline_since).
                concat(StringUtil.COLON).concat(StringUtil.SPACE).
                concat(getString(R.string.map_blueprint_personnel_link_status_no_records));
        mTvLastConnectionDateTime.setText(lastConnectionDateTimeNoRecords);

        C2OpenSansBoldTextView tvCallsign = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_callsign);
        LinearLayout layoutSetting = mViewBottomPanel.findViewById(R.id.layout_bottom_panel_settings);
        mImgSetting = mViewBottomPanel.findViewById(R.id.img_bottom_panel_settings_icon);
        mTvSetting = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_settings_text);

        setBottomPanelRadioLinkStatus(ERadioConnectionStatus.OFFLINE.toString());

        String selectedRadioNumber = SharedPreferenceUtil.getSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                0).toString();

        if (selectedRadioNumber == null) {
            mTvRadioNumber.setText(getString(R.string.btm_panel_radio).concat(StringUtil.COLON).
                    concat(StringUtil.SPACE).concat(StringUtil.N_A));
        } else {
            mTvRadioNumber.setText(getString(R.string.btm_panel_radio).concat(StringUtil.COLON).
                    concat(StringUtil.SPACE).concat(selectedRadioNumber));
        }

        tvCallsign.setText(SharedPreferenceUtil.getCurrentUserCallsignID());
        layoutSetting.setOnClickListener(onSettingsClickListener);
    }

    private void initSnackbar() {
        mViewSnackbar = getLayoutInflater().inflate(R.layout.layout_custom_snackbar, null);
    }

    public View getSnackbarView() {
        return mViewSnackbar;
    }

    private DebounceOnClickListener onMapTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

            while (true) {
                if (!popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID)) {
                    break;
                }
            }

        }
    };

    private DebounceOnClickListener onVideoStreamTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

            while (true) {
                if (!popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID)) {
                    break;
                }
            }

        }
    };

    private DebounceOnClickListener onReportTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

//            while (true) {
//                if (!resetChildFragmentBackStack(
//                        MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID)) {
//                    break;
//                }
//            }

        }
    };

    private DebounceOnClickListener onTimelineTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

            while (true) {
                if (!popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID)) {
                    break;
                }
            }

        }
    };

    private DebounceOnClickListener onTaskTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

//            while (true) {
//                if (!popChildFragmentBackStack(
//                        MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID)) {
//                    break;
//                }
//            }

        }
    };

    private DebounceOnClickListener onRadioLinkTabClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

            while (true) {
                if (!popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID)) {
                    break;
                }
            }

        }
    };

    private DebounceOnClickListener onSettingsClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID,
                    true);
            setSelectedTabUIComponents();

            while (true) {
                if (!popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID)) {
                    break;
                }
            }

        }
    };

    private void setSelectedTabUIComponents() {
        if (mNoSwipeViewPager != null && mTvFragmentTitle != null) {
            removeSelector();

            switch (mNoSwipeViewPager.getCurrentItem()) {
                case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
                    setMapSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID));
                    mTvFragmentTitle.setText(getString(R.string.map_page_title));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:
                    setVideoStreamSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:
                    setSitRepSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:
                    setTimelineSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID:
                    setTaskSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:
                    setRadioLinkSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID));
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID:
                    setSettingsSelectedUI();
                    mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                            getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID));
                    break;
            }
        }
    }

    private void removeSelector() {
        removeLineSelector();
        removeTintSelector();
    }

    private void removeLineSelector() {
        mLinearLayoutLineSelectorMap.setVisibility(View.GONE);
        mLinearLayoutLineSelectorVideoStream.setVisibility(View.GONE);
        mLinearLayoutLineSelectorReport.setVisibility(View.GONE);
        mLinearLayoutLineSelectorTimeline.setVisibility(View.GONE);
        mLinearLayoutLineSelectorTask.setVisibility(View.GONE);
        mLinearLayoutLineSelectorRadioLink.setVisibility(View.GONE);
    }

    private void removeTintSelector() {
        mImgViewTabMap.setColorFilter(null);
        mImgViewTabVideoStream.setColorFilter(null);
        mImgViewTabReport.setColorFilter(null);
        mImgViewTabTimeline.setColorFilter(null);
        mImgViewTabTask.setColorFilter(null);
        mImgViewTabRadioLink.setColorFilter(null);
        mImgSetting.setColorFilter(null);
        mTvSetting.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.primary_white));
    }

    private void setMapSelectedUI() {
        mLinearLayoutLineSelectorMap.setVisibility(View.VISIBLE);
        mImgViewTabMap.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setVideoStreamSelectedUI() {
        mLinearLayoutLineSelectorVideoStream.setVisibility(View.VISIBLE);
        mImgViewTabVideoStream.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setSitRepSelectedUI() {
        mLinearLayoutLineSelectorReport.setVisibility(View.VISIBLE);
        mImgViewTabReport.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setTimelineSelectedUI() {
        mLinearLayoutLineSelectorTimeline.setVisibility(View.VISIBLE);
        mImgViewTabTimeline.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setTaskSelectedUI() {
        mLinearLayoutLineSelectorTask.setVisibility(View.VISIBLE);
        mImgViewTabTask.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setRadioLinkSelectedUI() {
        mLinearLayoutLineSelectorRadioLink.setVisibility(View.VISIBLE);
        mImgViewTabRadioLink.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan), PorterDuff.Mode.SRC_ATOP);
    }

    private void setSettingsSelectedUI() {
        mImgSetting.setColorFilter(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
        mTvSetting.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.primary_highlight_cyan));
    }

//    /**
//     * Updates/resets radio information with userId account and broadcast updated model to other devices
//     */
//    private void saveAndBroadcastRadioInfo(WaveRelayRadioModel waveRelayRadioModel,
//                                           String userId, String phoneIpAddr) {
//        waveRelayRadioModel.setUserId(userId);
//        waveRelayRadioModel.setPhoneIpAddress(phoneIpAddr);
//
//        mWaveRelayRadioViewModel.updateWaveRelayRadio(waveRelayRadioModel);
//
//        // Send updated WaveRelay model to all other devices
//        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);
//
//        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_DEVICE_IP_ADDRESS,
//                phoneIpAddr);
//    }

    /**
     * Populate video stream data
     */
    private void populateVideoStreamData(String radioId, String radioIpAddress) {

        SingleObserver<List<VideoStreamModel>> singleObserverAllVideoStreams =
                new SingleObserver<List<VideoStreamModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<VideoStreamModel> videoStreamModelList) {
                        if (videoStreamModelList != null) {
                            Timber.i("onSuccess singleObserverAllVideoStreams, " +
                                    "populateVideoStreamData. " +
                                    "videoStreamModelList size: %d", videoStreamModelList.size());

                            String ownVideoStreamUrl = getString(R.string.video_stream_url_names_prefix).
                                    concat(radioIpAddress).
                                    concat(getString(R.string.video_stream_url_names_suffix));

                            String[] radioIpAddresses = MainApplication.getAppContext().getResources().
                                    getStringArray(R.array.login_radio_ip_addresses);

                            for (int i = 0; i < videoStreamModelList.size(); i++) {

                                VideoStreamModel videoStreamModel = videoStreamModelList.get(i);

                                if (EOwner.OWN.toString().
                                        equalsIgnoreCase(videoStreamModel.getOwner())) {

                                    videoStreamModel.setName(MainApplication.getAppContext().getResources().
                                            getString(R.string.video_stream_video_name_camera_radio).
                                            concat(StringUtil.SPACE).concat(radioId));
                                    videoStreamModel.setUrl(ownVideoStreamUrl);

                                } else {

                                    String otherVideoStreamUrl;

                                    // By default, home ('Own') video radio IP Address is 198.18.5.1 (radioIpAddresses[0]).
                                    // However, if current user's 'Camera-Radio Own' radio IP is NOT the default IP address,
                                    // replace default IP address with whichever IP addresses have been used
                                    // instead and set its name to 'Camera-Radio 1'.
                                    if (radioIpAddresses[i].equalsIgnoreCase(radioIpAddress)) {
                                        videoStreamModel.setName(MainApplication.getAppContext().getResources().
                                                getString(R.string.video_stream_video_name_camera_radio).
                                                concat(StringUtil.SPACE).concat("1"));

                                        otherVideoStreamUrl = MainApplication.getAppContext().getResources().
                                                getString(R.string.video_stream_url_names_prefix).
                                                concat(radioIpAddresses[0]).
                                                concat(getString(R.string.video_stream_url_names_suffix));
                                    } else {

                                        otherVideoStreamUrl = MainApplication.getAppContext().getResources().
                                                getString(R.string.video_stream_url_names_prefix).
                                                concat(radioIpAddresses[i]).
                                                concat(getString(R.string.video_stream_url_names_suffix));

                                        int ipAddressLastOctet = Integer.valueOf(radioIpAddresses[i].
                                                substring(radioIpAddresses[i].lastIndexOf(StringUtil.DOT) + 1));

                                        videoStreamModel.setName(MainApplication.getAppContext().getResources().
                                                getString(R.string.video_stream_video_name_camera_radio).
                                                concat(StringUtil.SPACE).concat(String.valueOf(ipAddressLastOctet)));
                                    }

                                    videoStreamModel.setUrl(otherVideoStreamUrl);
                                }

                                videoStreamModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());
                                mVideoStreamViewModel.updateVideoStream(videoStreamModel);
                            }

                        } else {
                            Timber.i("onSuccess singleObserverAllVideoStreams, " +
                                    "populateVideoStreamData. " +
                                    "videoStreamModelList is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverAllVideoStreams, " +
                                "populateVideoStreamData. " +
                                "Error Msg: %s", e.toString());
                    }
                };

        mVideoStreamViewModel.getAllVideoStreams(singleObserverAllVideoStreams);
    }

    /**
     * Obtain radio device IP address from local database and update model data with
     * userId and phone IP address
     *
     * @param radioId
     */
    private void getDeviceIpAddrAndRunWrRadioSocketWithVideoStreamDataPopulation(long radioId) {

        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                new SingleObserver<WaveRelayRadioModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                        if (waveRelayRadioModel != null) {
                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "getDeviceIpAddrAndRunWrRadioSocketWithVideoStreamDataPopulation. " +
                                    "waveRelayRadioModel: %s", waveRelayRadioModel);

//                            // Subscribe to RabbitMQ and JeroMQ
//                            subscribeToNetwork();

                            broadcastUserInfoUpdate();

//                            saveAndBroadcastRadioInfo(waveRelayRadioModel,
//                                    SharedPreferenceUtil.getCurrentUserCallsignID(),
//                                    NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

                            mWaveRelayRadioAsyncTask = new WaveRelayRadioAsyncTask();
                            mWaveRelayRadioAsyncTask.runWrRadioSocketConnection(
                                    waveRelayRadioModel.getRadioIpAddress());

                            populateVideoStreamData(String.valueOf(waveRelayRadioModel.getRadioId()),
                                    waveRelayRadioModel.getRadioIpAddress());

                        } else {
                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "getDeviceIpAddrAndRunWrRadioSocketWithVideoStreamDataPopulation. " +
                                    "waveRelayRadioModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverWaveRelayRadioByRadioNo, " +
                                "getDeviceIpAddrAndRunWrRadioSocketWithVideoStreamDataPopulation. " +
                                "Error Msg: %s", e.toString());
                    }
                };

        mWaveRelayRadioViewModel.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
    }

//    /**
//     * Resets User Id of Radio info and notifies (broadcasts update to) other devices
//     *
//     * @param radioId
//     */
//    private void
//    (long radioId) {
//
//        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
//                new SingleObserver<WaveRelayRadioModel>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
//                        if (waveRelayRadioModel != null) {
//
//                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, resetWrRadioUserIDAndBroadcastUpdate. waveRelayRadioModel: %s", waveRelayRadioModel);
//
//                            saveAndBroadcastRadioInfo(waveRelayRadioModel,
//                                    null, StringUtil.INVALID_STRING);
//
//                        } else {
//
//
//                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, resetWrRadioUserIDAndBroadcastUpdate. waveRelayRadioModel is null");
//
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                        Timber.e("onError singleObserverWaveRelayRadioByRadioNo,resetWrRadioUserIDAndBroadcastUpdate. Error Msg: %s ", e.toString());
//
//                    }
//                };
//
//        mWaveRelayRadioViewModel.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
//    }

    /**
     * Updates/resets user information with access token and broadcast updated model to other devices
     */
    private void saveAndBroadcastUserInfo(UserModel userModel) {
        userModel.setAccessToken(StringUtil.EMPTY_STRING);
        userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
        userModel.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
        userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

        mUserViewModel.updateUser(userModel);

        // Send updated User model to all other devices
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.ACCESS_TOKEN,
                StringUtil.EMPTY_STRING);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                ERadioConnectionStatus.OFFLINE.toString());
    }

    /**
     * Notifies (broadcasts update to) other devices of updated user info (access token)
     * to indicate successful user login
     */
    private void broadcastUserInfoUpdate() {

        SingleObserver<UserModel> singleObserverUserByUserId =
                new SingleObserver<UserModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(UserModel userModel) {
                        if (userModel != null) {

                            Timber.i("onSuccess singleObserverUserByUserId,broadcastUserInfoUpdate. userModel: %s", userModel);


                            // Send updated user model to all other devices
                            JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

                        } else {
                            Timber.i("onSuccess singleObserverUserByUserId ,broadcastUserInfoUpdate. userModel is null");


                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("onError singleObserverUserByUserId, broadcastUserInfoUpdate. Error Msg: %s ", e.toString());
                    }
                };

        mUserViewModel.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUserByUserId);
    }

    /**
     * Resets access token of User info and notifies (broadcasts update to) other devices
     */
    private void resetUserAccessTokenAndBroadcastUpdate() {

        SingleObserver<UserModel> singleObserverUserByUserId =
                new SingleObserver<UserModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(UserModel userModel) {
                        if (userModel != null) {
                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel: %s ", userModel);
                            saveAndBroadcastUserInfo(userModel);

                        } else {
                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. Error Msg: %s", e.toString());

                    }
                };

        mUserViewModel.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUserByUserId);
    }

//    /**
//     * RabbitMQ message listener for incoming message when connection is established
//     */
//    private void setupMQListener() {
//        if (mIMQListener == null) {
//            mIMQListener = new IMQListener() {
//                @Override
//                public void onNewMessage(String message) {
//                    Log.w("Debug", message);
//
//                    System.out.println("Received new rabbitMqMessage");
//                    System.out.println("JSONUtil.isJSONValid(message) is " + JSONUtil.isJSONValid(message));
////                String mqttMessageString = mqttMessage.toString();
//                    boolean isJSON = false;
//
//                    if (JSONUtil.isJSONValid(message)) {
//                        try {
//                            JSONObject mqttMessageJSON = new JSONObject(message);
//                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
//                            SharedPreferences.Editor editor = pref.edit();
//
//                            System.out.println("Received valid mqttMessage");
//
//                            switch (mqttMessageJSON.getString("key")) {
//
//                                case FragmentConstants.KEY_TASK_ADD:
//                                    String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
//                                    int totalNumberOfTasks = pref.getInt(totalNumberOfTasksKey, 0);
//                                    editor.putInt(totalNumberOfTasksKey, totalNumberOfTasks + 1);
//
//                                    String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(String.valueOf(totalNumberOfTasks));
//
//                                    editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), mqttMessageJSON.getInt("id"));
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), mqttMessageJSON.getString("assigner"));
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), mqttMessageJSON.getString("assignee"));
//                                    editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), R.drawable.default_soldier_icon);
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), mqttMessageJSON.getString("title"));
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), mqttMessageJSON.getString("description"));
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), mqttMessageJSON.getString("status"));
//                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), mqttMessageJSON.getString("date"));
//
//                                    editor.apply();
//
//                                    TaskFragment taskFragment = (TaskFragment) ((MainStatePagerAdapter)
//                                            mNoSwipeViewPager.getAdapter()).getPageReferenceMap().
//                                            get(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);
//
//                                    if (taskFragment != null) {
//
//                                        Timber.i("Task: Refresh Data");
//
////                                        taskFragment.refreshData();
//                                        taskFragment.addItemInRecycler();
//                                    }
//
//                                case FragmentConstants.KEY_SITREP_ADD:
//                                    String totalNumberOfSitRepKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER);
//                                    int totalNumberOfSitRep = pref.getInt(totalNumberOfSitRepKey, 0);
//                                    editor.putInt(totalNumberOfSitRepKey, totalNumberOfSitRep + 1);
//
//                                    System.out.println("main activity totalNumberOfSitRep + 1 is " + (totalNumberOfSitRep + 1));
//
//                                    String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(String.valueOf(totalNumberOfSitRep));
//                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), mqttMessageJSON.getInt("id"));
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), mqttMessageJSON.getString("reporter"));
//                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER_AVATAR_ID), R.drawable.default_soldier_icon);
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), mqttMessageJSON.getString("location"));
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), mqttMessageJSON.getString("activity"));
//                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), mqttMessageJSON.getInt("personnel_t"));
//                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), mqttMessageJSON.getInt("personnel_s"));
//                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), mqttMessageJSON.getInt("personnel_d"));
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), mqttMessageJSON.getString("next_coa"));
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), mqttMessageJSON.getString("request"));
//
//                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), mqttMessageJSON.getString("date"));
//
//                                    editor.apply();
//
//                                    SitRepFragment sitRepFragment = (SitRepFragment) ((MainStatePagerAdapter)
//                                            mNoSwipeViewPager.getAdapter()).getPageReferenceMap().
//                                            get(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
//
//                                    if (sitRepFragment != null) {
//                                        Timber.i("Sit Rep: Refresh Data");
//
////                                        sitRepFragment.refreshData();
//                                        sitRepFragment.addItemInRecycler();
//                                    }
//                            }
//
//                            isJSON = true;
//
//                        } catch (JSONException ex) {
//                            Timber.e("JSONException: %s", ex);
//
//                        }
//
//                    }
//                }
//            };
//
//            RabbitMQHelper.getInstance().addRabbitListener(mIMQListener);
//        }
//    }

    // ------------------------------ Get Main Activity UI methods ----------------------------- //

    public View getMainLayout() {
        return mMainLayout;
    }

    public View getMainSidePanel() {
        return mViewSideMenuPanel;
    }

    public View getMainBottomPanel() {
        return mViewBottomPanel;
    }

    public void setBottomPanelFragmentTitleText(String title) {
        mTvFragmentTitle.setText(title);
    }

    private void setBottomPanelRadioLinkStatus(String linkStatus) {
        if (ERadioConnectionStatus.OFFLINE.toString().equalsIgnoreCase(linkStatus)) {
            mImgRadioLinkStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.icon_offline, null));
            mTvRadioLinkStatus.setTextColor(ContextCompat.getColor(this, R.color.dull_red));
            mTvRadioNumber.setTextColor(ContextCompat.getColor(this, R.color.dull_red));
        } else {
            mImgRadioLinkStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.icon_online, null));
            mTvRadioLinkStatus.setTextColor(ContextCompat.getColor(this, R.color.dull_green));
            mTvRadioNumber.setTextColor(ContextCompat.getColor(this, R.color.dull_green));
        }

        mTvRadioLinkStatus.setText(linkStatus);
    }

//    public void setBottomPanelLastConnectionDateText(String date) {
//        mTvLastConnectionDate.setText(date);
//    }
//
//    public void setBottomPanelLastConnectionTimeText(String time) {
//        mTvLastConnectionTime.setText(time);
//    }

    public void setBottomPanelLastConnectionDateTimeText(String date, String time) {
        SpannableStringBuilder lastConnectionDateTimeStrBuilder = new SpannableStringBuilder();

        lastConnectionDateTimeStrBuilder.append(getString(R.string.btm_panel_since));
        lastConnectionDateTimeStrBuilder.append(StringUtil.COLON);
        lastConnectionDateTimeStrBuilder.append(StringUtil.SPACE);

        // If not null, displays date time in a similar format of 'Online since 10 Mar 2019, 10:00'
        if (date != null && time != null) {
            String dateTime = date.concat(StringUtil.COMMA).concat(StringUtil.SPACE).concat(time);

            SpannableString connectedDateTime = new SpannableString(dateTime);
            connectedDateTime.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                    R.color.primary_highlight_cyan, null)), 0, connectedDateTime.length(), 0);
            lastConnectionDateTimeStrBuilder.append(connectedDateTime);

        } else { // If null, displays in a similar format of 'Offline since: No records'
            SpannableString noRecords = new SpannableString(getString(
                    R.string.map_blueprint_personnel_link_status_no_records));
            noRecords.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                    R.color.primary_highlight_cyan, null)), 0, noRecords.length(), 0);
            lastConnectionDateTimeStrBuilder.append(noRecords);
        }

        mTvLastConnectionDateTime.setText(lastConnectionDateTimeStrBuilder,
                C2OpenSansBoldTextView.BufferType.SPANNABLE);
    }

//    public void closeWebSocketClient() {
////        mWaveRelayRadioClient = mWaveRelayRadioAsyncTask.getWaveRelayRadioClient();
////        if (mWaveRelayRadioClient != null) {
////            mWaveRelayRadioClient.closeWebSocketClient();
////        }
//
//        WaveRelayRadioClient.closeWebSocketClient();
//    }

    /**
     * Refresh Main Activity Bottom Panel UI with updated data of current user model of the following:
     * 1) Current Date and Time, if last known connection time is unknown
     * 2) Last known online or offline date and time
     */
    private void refreshBottomPanelUI(UserModel currentUserModel) {
        if (currentUserModel != null) {
//            // Extracts list of radio connection status from UserModel
//            List<UserModel> currentUserModelList = mPersonnelLinkStatusUserListItems.stream().
//                    filter(UserModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
//                            equalsIgnoreCase(UserModel.getUserId())).collect(Collectors.toList());
//
//            // There should only be ONE current user model
//            if (currentUserModelList.size() == 1) {
//                UserModel currentUserModel = currentUserModelList.get(0);

//                if (getActivity() instanceof MainActivity) {
//                    MainActivity mainActivity = ((MainActivity) getActivity());

            // Last known link status datetime
            String lastKnownLinkStatusDateTime = currentUserModel.getLastKnownConnectionDateTime();

            if (!lastKnownLinkStatusDateTime.equalsIgnoreCase(StringUtil.INVALID_STRING)) {
//                        if (ERadioConnectionStatus.OFFLINE.toString().
//                                equalsIgnoreCase(currentUserModel.getRadioFullConnectionStatus())) {
//                            String lastKnownLinkStatusDateInCustomStrFormat = DateTimeUtil.
//                                    dateToCustomDateStringFormat(DateTimeUtil.stringToDate(lastKnownLinkStatusDateTime));
//
//                            mainActivity.setBottomPanelLastConnectionDateText(lastKnownLinkStatusDateInCustomStrFormat);
//
//                        } else {
//                            String lastKnownLinkStatusTimeInCustomStrFormat = DateTimeUtil.
//                                    dateToCustomTimeStringFormat(DateTimeUtil.stringToDate(lastKnownLinkStatusDateTime));
//                            mainActivity.setBottomPanelLastConnectionTimeText(lastKnownLinkStatusTimeInCustomStrFormat);
//                        }

                String lastKnownLinkStatusDateInCustomStrFormat = DateTimeUtil.
                        dateToCustomDateStringFormat(DateTimeUtil.stringToDate(lastKnownLinkStatusDateTime));

                String lastKnownLinkStatusTimeInCustomStrFormat = DateTimeUtil.
                        dateToCustomTimeStringFormat(DateTimeUtil.stringToDate(lastKnownLinkStatusDateTime));

                setBottomPanelLastConnectionDateTimeText(lastKnownLinkStatusDateInCustomStrFormat,
                        lastKnownLinkStatusTimeInCustomStrFormat);

            } else {
//                        String currentDateTimeInZoneFormat = DateTimeUtil.getCurrentDateTime();
//
//                        String currentDateInCustomStrFormat = DateTimeUtil.dateToCustomDateStringFormat(
//                                DateTimeUtil.stringToDate(currentDateTimeInZoneFormat));
//                        String cuurentTimeInCustomStrFormat = DateTimeUtil.dateToCustomTimeStringFormat(
//                                DateTimeUtil.stringToDate(currentDateTimeInZoneFormat));
//
//                        mainActivity.setBottomPanelLastConnectionDateText(currentDateInCustomStrFormat);
//                        mainActivity.setBottomPanelLastConnectionTimeText(cuurentTimeInCustomStrFormat);

                setBottomPanelLastConnectionDateTimeText(null, null);
            }
//                }
//            }
        }
    }

    /**
     * Pull data from Excel to local database
     *
     */
    private void pullDataFromExcelToDatabase() {
        Timber.i("Pulling data from Excel to Database...");

        ExcelSpreadsheetRepository excelSpreadsheetRepository =
                new ExcelSpreadsheetRepository();
        excelSpreadsheetRepository.pullDataFromExcelToDatabase();
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mWaveRelayRadioViewModel = ViewModelProviders.of(this).get(WaveRelayRadioViewModel.class);
        mVideoStreamViewModel = ViewModelProviders.of(this).get(VideoStreamViewModel.class);

        String currentUserId = SharedPreferenceUtil.getCurrentUserCallsignID();

        /*
         * Refreshes bottom panel status UI whenever there is a change in user data (insert, update or delete)
         */
        mUserViewModel.getCurrentUserLiveData(currentUserId).observe(this, new Observer<UserModel>() {
            @Override
            public void onChanged(@Nullable UserModel userModel) {
                if (userModel.getRadioFullConnectionStatus().
                        equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())) {
                    setBottomPanelRadioLinkStatus(ERadioConnectionStatus.ONLINE.toString());
                } else {
                    setBottomPanelRadioLinkStatus(ERadioConnectionStatus.OFFLINE.toString());
                }

                refreshBottomPanelUI(userModel);
            }
        });
    }

    // ---------------------------------------- EventBus ---------------------------------------- //

    /**
     * Allows fragments within this Main activity to transfer (post) data objects across each other
     * The following object models are taken into consideration:
     * 1) SitRepModel
     * 2) TaskModel
     *
     * @param modelClass
     */
    public void postStickyModel(Object modelClass) {
        if (modelClass instanceof SitRepModel) {
            SitRepModel sitRepModel = (SitRepModel) modelClass;
            EventBus.getDefault().postSticky(sitRepModel);
        } else if (modelClass instanceof TaskModel) {
            TaskModel taskModel = (TaskModel) modelClass;
            EventBus.getDefault().postSticky(taskModel);
        }
    }

    /**
     * Allows fragments within this Main activity to receive data objects across each other
     * The following object models are taken into consideration:
     * 1) SitRepModel
     * 2) TaskModel
     *
     * @param modelClassSimpleName
     * @return
     */
    public Object getStickyModel(String modelClassSimpleName) {
        Object modelToUpdate = null;
        if (modelClassSimpleName.equalsIgnoreCase(SitRepModel.class.getSimpleName())) {
            modelToUpdate = EventBus.getDefault().getStickyEvent(SitRepModel.class);
        } else if (modelClassSimpleName.equalsIgnoreCase(TaskModel.class.getSimpleName())) {
            modelToUpdate = EventBus.getDefault().getStickyEvent(TaskModel.class);
        }

        return modelToUpdate;
    }

    /**
     * Allows fragments within this Main activity to receive AND remove data objects
     * across each other at the same time
     * The following object models are taken into consideration:
     * 1) SitRepModel
     * 2) TaskModel
     *
     * @param modelClass
     * @return
     */
    public Object removeStickyModel(Object modelClass) {
        Object modelToRemove = EventBus.getDefault().removeStickyEvent(modelClass.getClass());
        return modelToRemove;
    }

    // ---------------------------------------- Broadcast Receivers ---------------------------------------- //

//    /**
//     * Registers broadcast receiver for indication of successful RabbitMQ connection to
//     * initiate message listener
//     */
//    private void registerRabbitMQBroadcastReceiver() {
//        if (mRabbitMqBroadcastReceiver == null) {
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//
//            Timber.i("registerRabbitMQBroadcastReceiver");
//
//            mRabbitMqBroadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    if (RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
//                        if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
//
//                            Timber.i("RabbitMQConnectionStatus.CONNECTED");
//
////                            setupMQListener();
//                        }
//                    }
//                }
//            };
//
//            LocalBroadcastManager.getInstance(this).registerReceiver(mRabbitMqBroadcastReceiver, filter);
//        }
//    }

    /**
     * Registers broadcast receiver for display of respective snackbar Excel notification
     * of pushing and pulling data
     */
    private void registerExcelBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PULL_FAILED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULLED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULL_FAILED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PUSH_FAILED_INTENT_ACTION);

        mExcelBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_pull_from_excel_successful_message));
                } else if (UserSettingsFragment.EXCEL_DATA_PULL_FAILED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_pull_from_excel_failed_message));
                } else if (UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULLED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_ship_data_pull_from_excel_successful_message));
                } else if (UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULL_FAILED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_ship_data_pull_from_excel_failed_message));
                } else if (UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_push_to_excel_successful_message));
                } else if (UserSettingsFragment.EXCEL_DATA_PUSH_FAILED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_push_from_excel_failed_message));
                } else if (UserSettingsFragment.DATA_SYNCED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_sync_up_successful_message));
                } else if (UserSettingsFragment.LOGGED_OUT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_logout_successful_message));
                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mExcelBroadcastReceiver, filter);
    }

    /**
     * Registers broadcast receiver notification of successful connection of wave relay client
     */
    private void registerWaveRelayClientBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WaveRelayRadioClient.WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION);
        filter.addAction(WaveRelayRadioClient.WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION);

        mWaveRelayClientBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WaveRelayRadioClient.WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_wave_relay_client_connected_message));

                } else if (WaveRelayRadioClient.WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
//                    WaveRelayRadioClient.closeWebSocketClientAfterNotification();
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_wave_relay_client_disconnected_message));
                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mWaveRelayClientBroadcastReceiver, filter);
    }

    /**
     * Registers broadcast receiver notification after Sit Rep data into text / image files have been saved successfully
     */
    private void registerSitRepFileSaveBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileUtil.SIT_REP_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION);

        mSitRepFileSaveBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (FileUtil.SIT_REP_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {

                    String directoryPath = intent.getExtras().getString(FileUtil.DIRECTORY_PATH_KEY);

                    String fileSavedMessage = MainApplication.getAppContext().
                            getString(R.string.snackbar_sitrep_file_saved_successfully).
                            concat(System.lineSeparator()).concat(System.lineSeparator()).
                            concat(MainApplication.getAppContext().getString(R.string.directory)).
                            concat(StringUtil.COLON).concat(directoryPath);

                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            fileSavedMessage);

                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mSitRepFileSaveBroadcastReceiver, filter);
    }

    /**
     * Registers broadcast receiver notification after motion log data into text file has been saved successfully
     */
    private void registerMotionLogFileSaveBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileUtil.MOTION_LOG_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION);

        mMotionLogFileSaveBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (FileUtil.MOTION_LOG_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {

                    String directoryPath = intent.getExtras().getString(FileUtil.DIRECTORY_PATH_KEY);

                    String fileSavedMessage = MainApplication.getAppContext().
                            getString(R.string.snackbar_motion_log_file_saved_successfully).
                            concat(System.lineSeparator()).concat(System.lineSeparator()).
                            concat(MainApplication.getAppContext().getString(R.string.directory)).
                            concat(StringUtil.COLON).concat(directoryPath);

                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            fileSavedMessage);

                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMotionLogFileSaveBroadcastReceiver, filter);
    }

//    /**
//     * Registers broadcast receiver for USB detection
//     * of pushing and pulling data
//     */
//    private void registerUSBDetectionBroadcastReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(USBConnectionConstants.ACTION_POWER_CONNECTED);
//        filter.addAction(USBConnectionConstants.ACTION_POWER_DISCONNECTED);
//        filter.addAction(USBConnectionConstants.USB_STATE);
//
//        mUSBDetectionBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                Log.i(TAG, "action: " + action);
//
//                if (action != null) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        if (action.equals(USBConnectionConstants.USB_STATE)) {
//                            if (intent.getExtras() != null &&
//                                    intent.getExtras().getBoolean("connected")) {
//                                mIsUSBConnected = true;
//                                Toast.makeText(context, "USB Connected", Toast.LENGTH_SHORT).show();
//                            } else {
//                                mIsUSBConnected = false;
//                                Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    } else {
//                        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
//                            mIsUSBConnected = true;
//                            Toast.makeText(context, "USB Connected", Toast.LENGTH_SHORT).show();
//                        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
//                            mIsUSBConnected = false;
//                            Toast.makeText(context, "USB Disconnected", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//            }
//        };
//
//        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
//                mUSBDetectionBroadcastReceiver, filter);
//    }

    /**
     * Base transition method to be called for management of child fragments in their respective back stack.
     * Navigates to other fragments within each base child fragment (starting fragment of each tab)
     *
     * @param currentLayoutID
     * @param fromFragment
     * @param toFragment
     */
    public void navigateWithAnimatedTransitionToFragment(int currentLayoutID, Fragment fromFragment, Fragment toFragment) {
        FragmentManager fm = fromFragment.getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
        ft.replace(currentLayoutID, toFragment, toFragment.getClass().getSimpleName());
        ft.addToBackStack(toFragment.getClass().getSimpleName());
        ft.commit();
    }

    /**
     * Get View Pager; mainly used for getting currently displayed 'tab' item
     *
     * @return
     */
    public NoSwipeViewPager getViewPager() {
        return mNoSwipeViewPager;
    }

    /**
     * Get main View Pager Adapter for getting fragments
     *
     * @return
     */
    public MainStatePagerAdapter getViewPagerAdapter() {
        if (mNoSwipeViewPager.getAdapter() instanceof MainStatePagerAdapter) {
            return (MainStatePagerAdapter) mNoSwipeViewPager.getAdapter();
        }

        return null;
    }

    /**
     * Get View Pager; mainly used for getting currently displayed 'tab' item
     *
     * @return
     */
    public Fragment getCurrentFragment() {
        if (mNoSwipeViewPager != null) {
            return getViewPagerAdapter().getFragment(mNoSwipeViewPager.getCurrentItem());
        }

        return null;
    }

    /**
     * Get Base Child Fragment of each 'tab' used for Child Fragment Back stack management
     *
     * @param fragmentPosID
     * @return
     */
    public Fragment getBaseChildFragmentOfCurrentFragment(int fragmentPosID) {
        Fragment fragment = getViewPagerAdapter().getFragment(fragmentPosID);

        return fragment;
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     */
    public boolean popChildFragmentBackStack(int fragmentPosID) {
        Fragment baseChildFragment = getViewPagerAdapter().getFragment(fragmentPosID);

        if (baseChildFragment != null && baseChildFragment.getChildFragmentManager().
                getBackStackEntryCount() > 0) {

            baseChildFragment.getChildFragmentManager().popBackStack();
            return true;
        }

        return false;
    }

    /**
     * Request snackbar logout confirmation
     */
    private void queryConfirmLogout() {
        SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                getString(R.string.snackbar_settings_logout_message),
                MainActivity.this);
    }

    /**
     * Cleans up all resources of child fragments before returning to login activity
     */
    private void destroyAllBaseChildFragments() {
        if (getViewPagerAdapter() != null) {
            MapShipBlueprintFragment mapShipBlueprintFragment = ((MapShipBlueprintFragment)
                    getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID));

            if (mapShipBlueprintFragment != null) {
                mapShipBlueprintFragment.destroySelf();
            }

            VideoStreamFragment videoStreamFragment = ((VideoStreamFragment)
                    getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID));

            if (videoStreamFragment != null) {
                videoStreamFragment.destroySelf();
            }
        }
    }

    @Override
    public void onSnackbarActionClick() {
        finish();
    }

    @Override
    public void onBackPressed() {

        Timber.i("onBackPressed");

        if (getViewPagerAdapter() != null) {
            switch (mNoSwipeViewPager.getCurrentItem()) {
                case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
                    MapShipBlueprintFragment mapShipBlueprintFragment = ((MapShipBlueprintFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID));
                    if (mapShipBlueprintFragment != null && !mapShipBlueprintFragment.popBackStack())
                        queryConfirmLogout();
//                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:
                    VideoStreamFragment videoStreamFragment = ((VideoStreamFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID));
                    if (videoStreamFragment != null && !videoStreamFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:
                    SitRepFragment sitRepFragment = ((SitRepFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID));
                    if (sitRepFragment != null && !sitRepFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:
                    TimelineFragment timelineFragment = ((TimelineFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID));
                    if (timelineFragment != null && !timelineFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID:
                    TaskFragment taskFragment = ((TaskFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID));
                    if (taskFragment != null && !taskFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:
                    RadioLinkStatusFragment radioLinkStatusFragment = ((RadioLinkStatusFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID));
                    if (radioLinkStatusFragment != null && !radioLinkStatusFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID:
                    UserSettingsFragment userSettingsFragment = ((UserSettingsFragment)
                            getViewPagerAdapter().getFragment(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID));
                    if (userSettingsFragment != null && !userSettingsFragment.popBackStack())
                        queryConfirmLogout();
                    break;

                default:
                    queryConfirmLogout();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        registerRabbitMQBroadcastReceiver();
        registerExcelBroadcastReceiver();
        registerWaveRelayClientBroadcastReceiver();
        registerSitRepFileSaveBroadcastReceiver();
        registerMotionLogFileSaveBroadcastReceiver();
//        registerUSBDetectionBroadcastReceiver();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(USBDetectionBroadcastReceiver.RECEIVED_USB_UPDATE_ACTION);
//        mUSBDetectionBroadcastReceiver = new USBDetectionBroadcastReceiver();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mUSBDetectionBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Timber.i("onResume...");

        setSelectedTabUIComponents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Timber.i("onStop...");
    }

    // Executes resets on data models, network services etc upon user logout
    // or closing the app
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy");

        destroyAllBaseChildFragments();

        if (mRabbitMqBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRabbitMqBroadcastReceiver);
            mRabbitMqBroadcastReceiver = null;
        }

        if (mExcelBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mExcelBroadcastReceiver);
            mExcelBroadcastReceiver = null;
        }

        if (mWaveRelayClientBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mWaveRelayClientBroadcastReceiver);
            mWaveRelayClientBroadcastReceiver = null;
        }

        if (mSitRepFileSaveBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mSitRepFileSaveBroadcastReceiver);
            mSitRepFileSaveBroadcastReceiver = null;
        }

        if (mMotionLogFileSaveBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMotionLogFileSaveBroadcastReceiver);
            mMotionLogFileSaveBroadcastReceiver = null;
        }

        // Resets access token of user
        resetUserAccessTokenAndBroadcastUpdate();

        // Close any radio web sockets
        WaveRelayRadioClient.closeWebSocketClient();
    }
}
