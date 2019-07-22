package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

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
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.WaveRelayRadioViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioAsyncTask;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioClient;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioSocketClient;
import sg.gov.dsta.mobileC3.ventilo.util.JSONUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.USBConnectionConstants;
import sg.gov.dsta.mobileC3.ventilo.util.network.NetworkUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;

public class MainActivity extends AppCompatActivity implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // MQTT
    private static final String MQTT_TOPIC_TASK = "Task";
    private static final String MQTT_TOPIC_INCIDENT = "Incident";

    // View Models
    private UserViewModel mUserViewModel;
    private WaveRelayRadioViewModel mWaveRelayRadioViewModel;

    // Main
    private RelativeLayout mLayoutMain;

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

    private RelativeLayout mRelativeLayoutTabMap;
    private RelativeLayout mRelativeLayoutTabVideoStream;
    private RelativeLayout mRelativeLayoutTabReport;
    private RelativeLayout mRelativeLayoutTabTimeline;
    private RelativeLayout mRelativeLayoutTabTask;
    private RelativeLayout mRelativeLayoutTabRadioLink;

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
    private C2OpenSansBoldTextView mTvLastConnectionDate;
    private C2OpenSansBoldTextView mTvLastConnectionTime;
    private C2OpenSansBoldTextView mTvCallsign;
    private LinearLayout mLayoutSetting;
    private AppCompatImageView mImgSetting;
    private C2OpenSansBoldTextView mTvSetting;

    // Snackbar
    private View mViewSnackbar;

    //    private boolean mIsServiceRegistered;
//    private Intent mNetworkIntent;
    private BroadcastReceiver mRabbitMqBroadcastReceiver;
    private BroadcastReceiver mExcelBroadcastReceiver;
    private BroadcastReceiver mWaveRelayClientBroadcastReceiver;
//    private BroadcastReceiver mUSBDetectionBroadcastReceiver;

    private IMQListener mIMQListener;
//    private MapView mMapView;

    private WaveRelayRadioAsyncTask mWaveRelayRadioAsyncTask;
    private WaveRelayRadioClient mWaveRelayRadioClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutMain = findViewById(R.id.layout_main_activity);

//        DimensionUtil.convertPixelToDps(20);

//        setBroadcastReceivers();

        observerSetup();
        initNetwork();
        initSideMenuPanel();
        initBottomPanel();
        initSnackbar();
//        ownUserId = 10;
//        ownUserId = Long.parseLong(getIntent().getStringExtra("USER_ID"));

        MainStatePagerAdapter mainStatePagerAdapter = new MainStatePagerAdapter(
                getSupportFragmentManager(), getApplication().getApplicationContext());
        mNoSwipeViewPager = findViewById(R.id.viewpager_main_nav);
        mNoSwipeViewPager.setAdapter(mainStatePagerAdapter);
        mNoSwipeViewPager.setPagingEnabled(false);
    }

//    private void setBroadcastReceivers() {
//        //Reference: http://www.codepool.biz/how-to-monitor-usb-events-on-android.html
//
//        mUsbReceiver = new BroadcastReceiver() {
//
//            String usbStateChangeAction = "android.hardware.usb.action.USB_STATE";
//
//            public void onReceive(Context context, Intent intent) {
////                String action = intent.getAction();
////                Log.d(LOG_TAG, "Received Broadcast: "+action);
////                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
////
////                    updateUSBstatus();
////                    Log.d(LOG_TAG, "USB Connected..");
////                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
////                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
////                    if (device != null) {
////                        updateUSBstatus();
////                    }
////                    Log.d(LOG_TAG, "USB Disconnected..");
////                }
//
//                String action = intent.getAction();
//                Log.d(TAG, "Received Broadcast: " + action);
//                if (action.equalsIgnoreCase(usbStateChangeAction)) { //Check if change in USB state
//                    if (intent.getExtras().getBoolean("connected")) {
//                        // USB was connected
//                        Toast.makeText(getApplicationContext(), "USB CONNECTED", Toast.LENGTH_LONG).show();
//                    } else {
//                        // USB was disconnected
//                        Toast.makeText(getApplicationContext(), "USB DISCONNECTED", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        };
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
//        registerReceiver(mUsbReceiver , filter);
//        Log.d(TAG, "mUsbReceiver Registered");
//    }

    private void initNetwork() {
        Bundle b = getIntent().getBundleExtra(MainNavigationConstants.WAVE_RELAY_RADIO_NO_BUNDLE_KEY);
        int radioId = b.getInt(MainNavigationConstants.WAVE_RELAY_RADIO_NO_KEY);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                radioId);

        getDeviceIpAddrAndRunWrRadioSocket(radioId);
    }

    private void initSideMenuPanel() {
        mViewSideMenuPanel = findViewById(R.id.layout_main_side_menu_panel);

        // Tab Views
        mRelativeLayoutTabMap = mViewSideMenuPanel.findViewById(R.id.layout_tab_map_selector_status);
        mRelativeLayoutTabVideoStream = mViewSideMenuPanel.findViewById(R.id.layout_tab_video_stream_selector_status);
        mRelativeLayoutTabReport = mViewSideMenuPanel.findViewById(R.id.layout_tab_report_selector_status);
        mRelativeLayoutTabTimeline = mViewSideMenuPanel.findViewById(R.id.layout_tab_timeline_selector_status);
        mRelativeLayoutTabTask = mViewSideMenuPanel.findViewById(R.id.layout_tab_task_selector_status);
        mRelativeLayoutTabRadioLink = mViewSideMenuPanel.findViewById(R.id.layout_tab_radio_link_selector_status);

        // Line within Tab View
        mLinearLayoutLineSelectorMap = mViewSideMenuPanel.findViewById(R.id.linear_layout_map_line_selector);
        mLinearLayoutLineSelectorVideoStream = mViewSideMenuPanel.findViewById(R.id.linear_layout_video_stream_line_selector);
        mLinearLayoutLineSelectorReport = mViewSideMenuPanel.findViewById(R.id.linear_layout_report_line_selector);
        mLinearLayoutLineSelectorTimeline = mViewSideMenuPanel.findViewById(R.id.linear_layout_timeline_line_selector);
        mLinearLayoutLineSelectorTask = mViewSideMenuPanel.findViewById(R.id.linear_layout_task_line_selector);
        mLinearLayoutLineSelectorRadioLink = mViewSideMenuPanel.findViewById(R.id.linear_layout_radio_link_line_selector);

        // Image Views within Tab View
        mImgViewTabMap = mViewSideMenuPanel.findViewById(R.id.img_tab_map);
        mImgViewTabVideoStream = mViewSideMenuPanel.findViewById(R.id.img_tab_video_stream);
        mImgViewTabReport = mViewSideMenuPanel.findViewById(R.id.img_tab_report);
        mImgViewTabTimeline = mViewSideMenuPanel.findViewById(R.id.img_tab_timeline);
        mImgViewTabTask = mViewSideMenuPanel.findViewById(R.id.img_tab_task);
        mImgViewTabRadioLink = mViewSideMenuPanel.findViewById(R.id.img_tab_radio_link);

        // Tab Views OnClickListeners
        mRelativeLayoutTabMap.setOnClickListener(onMapTabClickListener);
        mRelativeLayoutTabVideoStream.setOnClickListener(onVideoStreamTabClickListener);
        mRelativeLayoutTabReport.setOnClickListener(onReportTabClickListener);
        mRelativeLayoutTabTimeline.setOnClickListener(onTimelineTabClickListener);
        mRelativeLayoutTabTask.setOnClickListener(onTaskTabClickListener);
        mRelativeLayoutTabRadioLink.setOnClickListener(onRadioLinkTabClickListener);

        // Map selected by default on start up
        removeLineSelector();
        setMapSelectedUI();
    }

    private void initBottomPanel() {
        mViewBottomPanel = findViewById(R.id.layout_main_bottom_panel);

        mTvFragmentTitle = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_fragment_title);
        mImgRadioLinkStatus = mViewBottomPanel.findViewById(R.id.img_bottom_panel_radio_link_status_icon);
        mTvRadioLinkStatus = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_radio_link_status);
        mTvLastConnectionDate = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_last_connection_date);
        mTvLastConnectionTime = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_last_connection_time);
        mTvCallsign = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_callsign);
        mLayoutSetting = mViewBottomPanel.findViewById(R.id.layout_bottom_panel_settings);
        mImgSetting = mViewBottomPanel.findViewById(R.id.img_bottom_panel_settings_icon);
        mTvSetting = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_settings_text);

        setBottomPanelRadioLinkStatus(ERadioConnectionStatus.OFFLINE.toString());
        mTvCallsign.setText(SharedPreferenceUtil.getCurrentUserCallsignID());
        mLayoutSetting.setOnClickListener(onSettingsClickListener);
    }

    private void initSnackbar() {
        mViewSnackbar = getLayoutInflater().inflate(R.layout.layout_custom_snackbar, null);
    }

    public View getSnackbarView() {
        return mViewSnackbar;
    }

    private OnClickListener onMapTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onVideoStreamTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onReportTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onTimelineTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onTaskTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onRadioLinkTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
        }
    };

    private OnClickListener onSettingsClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID,
                    true);
            setSelectedTabUIComponents();
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

    /**
     * Updates/resets radio information with userId account and broadcast updated model to other devices
     */
    private void saveAndBroadcastRadioInfo(WaveRelayRadioModel waveRelayRadioModel,
                                           String userId, String phoneIpAddr) {
        waveRelayRadioModel.setUserId(userId);
        waveRelayRadioModel.setPhoneIpAddress(phoneIpAddr);

        mWaveRelayRadioViewModel.updateWaveRelayRadio(waveRelayRadioModel);

        // Send updated WaveRelay model to all other devices
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_DEVICE_IP_ADDRESS,
                phoneIpAddr);
    }

    /**
     * Obtain radio device IP address from local database and update model data with
     * userId and phone IP address
     *
     * @param radioId
     */
    private void getDeviceIpAddrAndRunWrRadioSocket(long radioId) {

        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                new SingleObserver<WaveRelayRadioModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                        if (waveRelayRadioModel != null) {
                            Log.d(TAG, "onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "getDeviceIpAddrAndRunWrRadioSocket. " +
                                    "waveRelayRadioModel: " + waveRelayRadioModel);

//                            // Subscribe to RabbitMQ and JeroMQ
//                            subscribeToNetwork();

                            broadcastUserInfoUpdate();

                            saveAndBroadcastRadioInfo(waveRelayRadioModel,
                                    SharedPreferenceUtil.getCurrentUserCallsignID(),
                                    NetworkUtil.getOwnIPAddressThroughWiFiOrEthernet(true));

                            mWaveRelayRadioAsyncTask = new WaveRelayRadioAsyncTask();
                            mWaveRelayRadioAsyncTask.runWrRadioSocketConnection(
                                    waveRelayRadioModel.getRadioIpAddress());

                        } else {
                            Log.d(TAG, "onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "getDeviceIpAddrAndRunWrRadioSocket. " +
                                    "waveRelayRadioModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverWaveRelayRadioByRadioNo, " +
                                "getDeviceIpAddrAndRunWrRadioSocket. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mWaveRelayRadioViewModel.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
    }

    /**
     * Resets User Id of Radio info and notifies (broadcasts update to) other devices
     *
     * @param radioId
     */
    private void resetWrRadioUserIDAndBroadcastUpdate(long radioId) {

        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                new SingleObserver<WaveRelayRadioModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                        if (waveRelayRadioModel != null) {
                            Log.d(TAG, "onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "resetWrRadioUserIDAndBroadcastUpdate. " +
                                    "waveRelayRadioModel: " + waveRelayRadioModel);

                            saveAndBroadcastRadioInfo(waveRelayRadioModel,
                                    null, StringUtil.INVALID_STRING);

                        } else {
                            Log.d(TAG, "onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "resetWrRadioUserIDAndBroadcastUpdate. " +
                                    "waveRelayRadioModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverWaveRelayRadioByRadioNo, " +
                                "resetWrRadioUserIDAndBroadcastUpdate. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mWaveRelayRadioViewModel.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
    }

    /**
     * Updates/resets user information with access token and broadcast updated model to other devices
     */
    private void saveAndBroadcastUserInfo(UserModel userModel, String accessToken) {
        userModel.setAccessToken(accessToken);

        mUserViewModel.updateUser(userModel);

        // Send updated User model to all other devices
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.ACCESS_TOKEN,
                accessToken);
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
                            Log.d(TAG, "onSuccess singleObserverUserByUserId, " +
                                    "broadcastUserInfoUpdate. " +
                                    "userModel: " + userModel);

                            // Send updated user model to all other devices
                            JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

                        } else {
                            Log.d(TAG, "onSuccess singleObserverUserByUserId, " +
                                    "broadcastUserInfoUpdate. " +
                                    "userModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverUserByUserId, " +
                                "broadcastUserInfoUpdate. " +
                                "Error Msg: " + e.toString());
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
                            Log.d(TAG, "onSuccess singleObserverUserByUserId, " +
                                    "resetUserAccessTokenAndBroadcastUpdate. " +
                                    "userModel: " + userModel);

                            saveAndBroadcastUserInfo(userModel, StringUtil.EMPTY_STRING);

                        } else {
                            Log.d(TAG, "onSuccess singleObserverUserByUserId, " +
                                    "resetUserAccessTokenAndBroadcastUpdate. " +
                                    "userModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverUserByUserId, " +
                                "resetUserAccessTokenAndBroadcastUpdate. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mUserViewModel.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUserByUserId);
    }

    /**
     * RabbitMQ message listener for incoming message when connection is established
     */
    private void setupMQListener() {
        if (mIMQListener == null) {
            mIMQListener = new IMQListener() {
                @Override
                public void onNewMessage(String message) {
                    Log.w("Debug", message);

                    System.out.println("Received new rabbitMqMessage");
                    System.out.println("JSONUtil.isJSONValid(message) is " + JSONUtil.isJSONValid(message));
//                String mqttMessageString = mqttMessage.toString();
                    boolean isJSON = false;

                    if (JSONUtil.isJSONValid(message)) {
                        try {
                            JSONObject mqttMessageJSON = new JSONObject(message);
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
                            SharedPreferences.Editor editor = pref.edit();

                            System.out.println("Received valid mqttMessage");

                            switch (mqttMessageJSON.getString("key")) {

                                case FragmentConstants.KEY_TASK_ADD:
                                    String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
                                    int totalNumberOfTasks = pref.getInt(totalNumberOfTasksKey, 0);
                                    editor.putInt(totalNumberOfTasksKey, totalNumberOfTasks + 1);

                                    String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(String.valueOf(totalNumberOfTasks));

                                    editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), mqttMessageJSON.getInt("id"));
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), mqttMessageJSON.getString("assigner"));
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), mqttMessageJSON.getString("assignee"));
                                    editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), R.drawable.default_soldier_icon);
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), mqttMessageJSON.getString("title"));
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), mqttMessageJSON.getString("description"));
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), mqttMessageJSON.getString("status"));
                                    editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), mqttMessageJSON.getString("date"));

                                    editor.apply();

                                    TaskFragment taskFragment = (TaskFragment) ((MainStatePagerAdapter)
                                            mNoSwipeViewPager.getAdapter()).getPageReferenceMap().
                                            get(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);

                                    if (taskFragment != null) {
                                        Log.d(TAG, "Task: Refresh Data");
//                                        taskFragment.refreshData();
                                        taskFragment.addItemInRecycler();
                                    }

                                case FragmentConstants.KEY_SITREP_ADD:
                                    String totalNumberOfSitRepKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER);
                                    int totalNumberOfSitRep = pref.getInt(totalNumberOfSitRepKey, 0);
                                    editor.putInt(totalNumberOfSitRepKey, totalNumberOfSitRep + 1);

                                    System.out.println("main activity totalNumberOfSitRep + 1 is " + (totalNumberOfSitRep + 1));

                                    String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(String.valueOf(totalNumberOfSitRep));
                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), mqttMessageJSON.getInt("id"));
                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), mqttMessageJSON.getString("reporter"));
                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER_AVATAR_ID), R.drawable.default_soldier_icon);
                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), mqttMessageJSON.getString("location"));
                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), mqttMessageJSON.getString("activity"));
                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), mqttMessageJSON.getInt("personnel_t"));
                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), mqttMessageJSON.getInt("personnel_s"));
                                    editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), mqttMessageJSON.getInt("personnel_d"));
                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), mqttMessageJSON.getString("next_coa"));
                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), mqttMessageJSON.getString("request"));

                                    editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                                            concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), mqttMessageJSON.getString("date"));

                                    editor.apply();

                                    SitRepFragment sitRepFragment = (SitRepFragment) ((MainStatePagerAdapter)
                                            mNoSwipeViewPager.getAdapter()).getPageReferenceMap().
                                            get(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);

                                    if (sitRepFragment != null) {
                                        Log.d(TAG, "Sit Rep: Refresh Data");
//                                        sitRepFragment.refreshData();
                                        sitRepFragment.addItemInRecycler();
                                    }
                            }

                            isJSON = true;

                        } catch (JSONException ex) {
                            Log.d(TAG, "JSONException: " + ex);
                        }

                    }
                }
            };

            RabbitMQHelper.getInstance().addRabbitListener(mIMQListener);
        }
    }

    // ------------------------------ Get Main Activity UI methods ----------------------------- //

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
        } else {
            mImgRadioLinkStatus.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.icon_online, null));
            mTvRadioLinkStatus.setTextColor(ContextCompat.getColor(this, R.color.dull_green));
        }

        mTvRadioLinkStatus.setText(linkStatus);
    }

    public void setBottomPanelLastConnectionDateText(String date) {
        mTvLastConnectionDate.setText(date);
    }

    public void setBottomPanelLastConnectionTimeText(String time) {
        mTvLastConnectionTime.setText(time);
    }

    public void closeWebSocketClient() {
        if (mWaveRelayRadioClient != null) {
            mWaveRelayRadioClient.closeWebSocketClient();
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mWaveRelayRadioViewModel = ViewModelProviders.of(this).get(WaveRelayRadioViewModel.class);

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

    /**
     * Registers broadcast receiver for indication of successful RabbitMQ connection to
     * initiate message listener
     */
    private void registerRabbitMQBroadcastReceiver() {
        if (mRabbitMqBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
            Log.i(TAG, "registerRabbitMQBroadcastReceiver");

            mRabbitMqBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                        if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
                            Log.i(TAG, "RabbitMQConnectionStatus.CONNECTED");
                            setupMQListener();
                        }
                    }
                }
            };

            LocalBroadcastManager.getInstance(this).registerReceiver(mRabbitMqBroadcastReceiver, filter);
        }
    }

    /**
     * Registers broadcast receiver for display of respective snackbar Excel notification
     * of pushing and pulling data
     */
    private void registerExcelBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION);

        mExcelBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_pull_from_excel_successful_message));
                } else if (UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_push_to_excel_successful_message));
                } else if (UserSettingsFragment.DATA_SYNCED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_sync_up_successful_message));
                } else if (UserSettingsFragment.LOGGED_OUT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
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
        filter.addAction(WaveRelayRadioSocketClient.WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION);
        filter.addAction(WaveRelayRadioSocketClient.WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION);

        mWaveRelayClientBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WaveRelayRadioSocketClient.WAVE_RELAY_CLIENT_CONNECTED_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_wave_relay_client_connected_message));
                    mWaveRelayRadioClient = mWaveRelayRadioAsyncTask.getWaveRelayRadioClient();

                } else if (WaveRelayRadioSocketClient.WAVE_RELAY_CLIENT_DISCONNECTED_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_wave_relay_client_disconnected_message));
                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mWaveRelayClientBroadcastReceiver, filter);
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
     *
     */
    private void queryConfirmLogout() {
        SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                getString(R.string.snackbar_settings_logout_message),
                MainActivity.this);
    }

    /**
     * Cleans up all resources of child fragments before returning to login activity
     *
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
        Log.i(TAG, "onBackPressed");

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
        registerRabbitMQBroadcastReceiver();
        registerExcelBroadcastReceiver();
        registerWaveRelayClientBroadcastReceiver();
//        registerUSBDetectionBroadcastReceiver();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(USBDetectionBroadcastReceiver.RECEIVED_USB_UPDATE_ACTION);
//        mUSBDetectionBroadcastReceiver = new USBDetectionBroadcastReceiver();
//        LocalBroadcastManager.getInstance(this).registerReceiver(mUSBDetectionBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume...");
        setSelectedTabUIComponents();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop...");
    }

    // Executes resets on data models, network services etc upon user logout
    // or closing the app
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");

        destroyAllBaseChildFragments();

        if (mRabbitMqBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRabbitMqBroadcastReceiver);
            mRabbitMqBroadcastReceiver = null;
        }

        if (mExcelBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mExcelBroadcastReceiver);
            mExcelBroadcastReceiver = null;
        }

//        if (mUSBDetectionBroadcastReceiver != null) {
//            LocalBroadcastManager.getInstance(this).unregisterReceiver(mUSBDetectionBroadcastReceiver);
//            mUSBDetectionBroadcastReceiver = null;
//        }

        // Resets Wave Relay radio info of user
        Object userRadioId = SharedPreferenceUtil.getSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
                0);

        if (userRadioId != null) {
            if (userRadioId instanceof Integer) {
                Log.i(TAG, "Resetting User Id of radio info...");
                resetWrRadioUserIDAndBroadcastUpdate((int) userRadioId);
            }
        }

        // Resets access token of user
        resetUserAccessTokenAndBroadcastUpdate();

        // Close any radio web sockets
        closeWebSocketClient();

//        /* Close service properly, if not already closed.
//         */
//        synchronized (MainActivity.class) {
//            if (NetworkService.mIsServiceRegistered) {
//
//                if (getApplication() instanceof MainApplication) {
//                    stopService(((MainApplication) getApplication()).getNetworkIntent());
//                    Log.i(TAG, "Stopped network intent service.");
//
//                    NetworkService.mIsServiceRegistered = false;
//                }
//
////                stopService(mNetworkIntent);
////                Log.i(TAG, "Stopped network intent service.");
////
////                NetworkService.mIsServiceRegistered = false;
//            }
//
//            JeroMQPublisher.getInstance().stop();
//            JeroMQSubscriber.getInstance().stop();
//
//            Log.i(TAG, "Stopped all JeroMQ connections.");
//        }
    }
}
