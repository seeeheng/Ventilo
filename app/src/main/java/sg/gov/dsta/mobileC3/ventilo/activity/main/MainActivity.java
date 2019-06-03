package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import sg.gov.dsta.mobileC3.ventilo.NoSwipeViewPager;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.login.LoginActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus.RadioLinkStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.task.TaskFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.user.UserSettingsFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.MqttHelper;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPubSubBrokerProxy;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.RabbitMQAsyncTask;
import sg.gov.dsta.mobileC3.ventilo.util.JSONUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // MQTT
    private static final String MQTT_TOPIC_TASK = "Task";
    private static final String MQTT_TOPIC_INCIDENT = "Incident";

//    public static Intent rabbitMQIntent;

    //    public static NetworkService networkService;
    public static boolean isServiceBound;
    //    private NetworkConnectivity mNetworkConnectivity;
    private MqttHelper mMqttHelper;

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
    private C2OpenSansBoldTextView mTvRadioLinkStatus;
    private C2OpenSansBoldTextView mTvLastConnectionDate;
    private C2OpenSansBoldTextView mTvLastConnectionTime;
    private C2OpenSansBoldTextView mTvCallsign;
    private LinearLayout mLayoutSetting;

    // Snackbar
    private View mViewSnackbar;

    //    private boolean mIsServiceRegistered;
    private BroadcastReceiver mRabbitMqBroadcastReceiver;
    private BroadcastReceiver mExcelBroadcastReceiver;

    private IMQListener mIMQListener;
//    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayoutMain = findViewById(R.id.layout_main_activity);

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

//        mBottomNavigationView = findViewById(R.id.btm_nav_view_main_nav);

//        NoSwipeViewPager.OnPageChangeListener viewPagerPageChangeListener =
//                setPageChangeListener(mBottomNavigationView);
//        mNoSwipeViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

//        BottomNavigationView.OnNavigationItemSelectedListener
//                navigationItemSelectedListener = setNavigationItemSelectedListener(mNoSwipeViewPager);
//        mBottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

//        mDataReceived = findViewById(R.id.dataReceived);
//        mBtnPublish = findViewById(R.id.btn_mqtt_publish);
//
//        mBtnPublish.setVisibility(View.GONE);
//        mBtnPublish.setOnClickListener(publishOnClickListener());
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
        mTvRadioLinkStatus = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_radio_link_status);
        mTvLastConnectionDate = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_last_connection_date);
        mTvLastConnectionTime = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_last_connection_time);
        mTvCallsign = mViewBottomPanel.findViewById(R.id.tv_bottom_panel_callsign);
        mLayoutSetting = mViewBottomPanel.findViewById(R.id.layout_bottom_panel_settings);

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
            removeSelector();
            setMapSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID));
        }
    };

    private OnClickListener onVideoStreamTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();
            setVideoStreamSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID));
        }
    };

    private OnClickListener onReportTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();
            setReportSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID));
        }
    };

    private OnClickListener onTimelineTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();
            setTimelineSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID));
        }
    };

    private OnClickListener onTaskTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();
            setTaskSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID));
        }
    };

    private OnClickListener onRadioLinkTabClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();
            setRadioLinkSelectedUI();
            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID));
        }
    };

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
    }

    private void setMapSelectedUI() {
        mLinearLayoutLineSelectorMap.setVisibility(View.VISIBLE);
        mImgViewTabMap.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setVideoStreamSelectedUI() {
        mLinearLayoutLineSelectorVideoStream.setVisibility(View.VISIBLE);
        mImgViewTabVideoStream.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setReportSelectedUI() {
        mLinearLayoutLineSelectorReport.setVisibility(View.VISIBLE);
        mImgViewTabReport.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setTimelineSelectedUI() {
        mLinearLayoutLineSelectorTimeline.setVisibility(View.VISIBLE);
        mImgViewTabTimeline.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setTaskSelectedUI() {
        mLinearLayoutLineSelectorTask.setVisibility(View.VISIBLE);
        mImgViewTabTask.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private void setRadioLinkSelectedUI() {
        mLinearLayoutLineSelectorRadioLink.setVisibility(View.VISIBLE);
        mImgViewTabRadioLink.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);
    }

    private OnClickListener onSettingsClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeSelector();

            mNoSwipeViewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID,
                    true);
            mTvFragmentTitle.setText(mNoSwipeViewPager.getAdapter().
                    getPageTitle(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID));

            mTvFragmentTitle.setText(getString(R.string.settings_page_title));
        }
    };

//    private NoSwipeViewPager.OnPageChangeListener setPageChangeListener(
//            final BottomNavigationView bottomNavigationView) {
//
//        return new NoSwipeViewPager.OnPageChangeListener() {
//            public void onPageScrollStateChanged(int state) {
//            }
//
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            public void onPageSelected(int position) {
//                switch (position) {
//                    case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
//                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_map);
//                        return;
//
//                    case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:
//                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_video_stream);
//                        return;
//
//                    case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:
//                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_report);
//                        return;
//
//                    case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:
//                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_timeline);
//                        return;
//
//                    case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:
//                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_radio_link);
//                        return;
//
//                    default:
//                        return;
//                }
//            }
//        };
//    }

//    private BottomNavigationView.OnNavigationItemSelectedListener setNavigationItemSelectedListener(
//            final NoSwipeViewPager viewPager) {
//
//        return new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.btn_nav_action_map:
//                        viewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
//                                true);
//                        return true;
//
//                    case R.id.btn_nav_action_video_stream:
//                        viewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
//                                true);
//                        return true;
//
//                    case R.id.btn_nav_action_report:
//                        viewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
//                                true);
//                        return true;
//
//                    case R.id.btn_nav_action_timeline:
//                        viewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
//                                true);
//                        return true;
//
//                    case R.id.btn_nav_action_radio_link:
//                        viewPager.setCurrentItem(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
//                                true);
//                        return true;
//
//                    default:
//                        return false;
//                }
//            }
//        };
//    }

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

    public View getMainSidePanel() {
        return mViewSideMenuPanel;
    }

    public View getMainBottomPanel() {
        return mViewBottomPanel;
    }

    public void setBottomPanelFragmentTitleText(String title) {
        mTvFragmentTitle.setText(title);
    }

    public void setBottomPanelRadioLinkStatusText(String linkStatus) {
        mTvRadioLinkStatus.setText(linkStatus);
    }

    public void setBottomPanelLastConnectionDateText(String date) {
        mTvLastConnectionDate.setText(date);
    }

    public void setBottomPanelLastConnectionTimeText(String time) {
        mTvLastConnectionTime.setText(time);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
//    private ServiceConnection rabbitMQServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            NetworkServiceBinder binder = (NetworkServiceBinder) service;
//            mNetworkService = binder.getService();
////            mNetworkService.deactivate();
////            unbindService(this);
////            System.out.println("Connected");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            mNetworkService.deactivate();
////            System.out.println("Disconnected");
////            Toast.makeText(getApplicationContext(), "Service Disconnected", Toast.LENGTH_LONG).show();
//        }
//    };

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == REQUEST_TAKE_PHOTO) {
//            Toast.makeText(getActivity().getApplicationContext(), "Photo Captured; photo uri is " +
//                    data.getExtras().getBundle(MediaStore.EXTRA_OUTPUT), Toast.LENGTH_LONG);
//            mLinearLayoutCameraBtn.setVisibility(View.GONE);
//            mLinearLayoutSendToBtn.setVisibility(View.VISIBLE);
//            mRelLayoutEditFabs.setVisibility(View.VISIBLE);
//        }
//    }

    public void postStickyModel(Object modelClass) {
        if (modelClass instanceof SitRepModel) {
            SitRepModel sitRepModel = (SitRepModel) modelClass;
            EventBus.getDefault().postSticky(sitRepModel);
        } else if (modelClass instanceof TaskModel) {
            TaskModel taskModel = (TaskModel) modelClass;
            EventBus.getDefault().postSticky(taskModel);
        }
    }

    public Object getStickyModel(String modelClassSimpleName) {
        Object modelToUpdate = null;
        if (modelClassSimpleName.equalsIgnoreCase(SitRepModel.class.getSimpleName())) {
            modelToUpdate = EventBus.getDefault().getStickyEvent(SitRepModel.class);
        } else if (modelClassSimpleName.equalsIgnoreCase(TaskModel.class.getSimpleName())) {
            modelToUpdate = EventBus.getDefault().getStickyEvent(TaskModel.class);
        }

        return modelToUpdate;
    }

    public Object removeStickyModel(Object modelClass) {
        Object modelToRemove =  EventBus.getDefault().removeStickyEvent(modelClass.getClass());
        return modelToRemove;
    }

    private void registerMqttBroadcastReceiver() {
        if (mRabbitMqBroadcastReceiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//        filter.addAction(MqttHelper.MQTT_CONNECT_INTENT_ACTION);
            Log.i(TAG, "registerMqttBroadcastReceiver");
            mRabbitMqBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
//                if (MqttHelper.MQTT_CONNECT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
//                    if (mMqttHelper == null) {
//                        mMqttHelper = MqttHelper.getInstance();
//                        setMqttCallback();
//                    }
//                }

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

    private void registerExcelBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION);
        filter.addAction(UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION);

        mExcelBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            getString(R.string.snackbar_settings_pull_from_excel_successful_message));
                } else if (UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            getString(R.string.snackbar_settings_push_to_excel_successful_message));
                } else if (UserSettingsFragment.DATA_SYNCED_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            getString(R.string.snackbar_settings_sync_up_successful_message));
                } else if (UserSettingsFragment.LOGGED_OUT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mLayoutMain, getSnackbarView(),
                            getString(R.string.snackbar_settings_logout_successful_message));
                }
            }
        };

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mExcelBroadcastReceiver, filter);
    }

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
     * @return
     */
    public MainStatePagerAdapter getViewPagerAdapter() {
        if (mNoSwipeViewPager.getAdapter() instanceof MainStatePagerAdapter) {
            return (MainStatePagerAdapter) mNoSwipeViewPager.getAdapter();
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

        if (baseChildFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
            baseChildFragment.getChildFragmentManager().popBackStack();
            return true;
        }

        return false;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
//    public static ServiceConnection rabbitMQServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // Bind to LocalService, cast the IBinder and get LocalService instance
//            NetworkServiceBinder binder = (NetworkServiceBinder) service;
//            networkService = binder.getService();
////            isServiceBound = true;
//            Log.i(TAG, "Network service activated.");
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.i(TAG, "Network service deactivating...");
//            networkService.deactivate();
////            isServiceBound = false;
//            Log.i(TAG, "Network service deactivated.");
//        }
//    };

//    private void subscribeToRabbitMQ() {
//        rabbitMQIntent = new Intent(this, NetworkService.class);
//        startService(rabbitMQIntent);
//        bindService(rabbitMQIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
//    }
    @Override
    public void onBackPressed() {
        System.out.println("Main Activity onBackPressed");
//        int count = getSupportFragmentManager().getBackStackEntryCount();
//        if (count == 0) {
//            // Go to login page
//            super.onBackPressed();
//            this.finish();
//        } else {
//
//
//            String taggedName = getSupportFragmentManager().getBackStackEntryAt(
//                    getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
//
//            Log.d(TAG, "returned fragment matches" + taggedName);
//
//            if (getSupportFragmentManager().findFragmentByTag(taggedName) instanceof MapShipBlueprintFragment) {
////                mBottomNavigationView.setVisibility(View.VISIBLE);
//
////                System.out.println("getSupportFragmentManager().getBackStackEntryCount() is " + getSupportFragmentManager().getBackStackEntryCount());
////                String newTaggedName = getSupportFragmentManager().getBackStackEntryAt(
////                        getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
////                String newTaggedName2 = getSupportFragmentManager().getBackStackEntryAt(
////                        getSupportFragmentManager().getBackStackEntryCount() - 3).getName();
////                System.out.println("newTaggedName is " + newTaggedName);
////                System.out.println("newTaggedName 2 is " + newTaggedName2);
////                System.out.println("MapFragment.class.getSimpleName() is " + MapFragment.class.getSimpleName());
////
////                System.out.println("getSupportFragmentManager().findFragmentByTag(\"android:switcher:\" + R.id.viewpager_main_nav + \":\" + 1) is " + getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager_main_nav + ":" + 1));
////                MapFragment returnedFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager_main_nav + ":" + 1);
////                if (0 == mNoSwipeViewPager.getCurrentItem() && null != returnedFragment) {
////                    ((MapFragment) returnedFragment).onVisible();
////                }
//
//                // Notify mapfragment of previous fragment (MapShipBlueprintFragment) that it was switched from
//                EventBus.getDefault().post(PageEvent.getInstance().addPage(PageEvent.FRAGMENT_KEY, MapShipBlueprintFragment.class.getSimpleName()));
//
//
//
//
////                for (int i = 0; i < backStackCount; i++) {
////                    if (MapFragment.class.getSimpleName().equalsIgnoreCase(
////                            getSupportFragmentManager().getBackStackEntryAt(i).getName())) {
////
//////                        MapFragment returnedFragment = (MapFragment) getSupportFragmentManager().
//////                                findFragmentByTag(MapFragment.class.getSimpleName());
////                        returnedFragment.onVisible();
////                    }
////                }
//            }
//
//            getSupportFragmentManager().popBackStack();
//        }

        if (mNoSwipeViewPager.getAdapter() instanceof MainStatePagerAdapter) {
            switch (mNoSwipeViewPager.getCurrentItem()) {
                case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
                    MapShipBlueprintFragment mapShipBlueprintFragment = ((MapShipBlueprintFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID));
                    if (!mapShipBlueprintFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:
                    VideoStreamFragment videoStreamFragment = ((VideoStreamFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID));
                    if (!videoStreamFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:
                    SitRepFragment sitRepFragment = ((SitRepFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID));
                    if (!sitRepFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:
                    TimelineFragment timelineFragment = ((TimelineFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID));
                    if (!timelineFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID:
                    TaskFragment taskFragment = ((TaskFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID));
                    if (!taskFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:
                    RadioLinkStatusFragment radioLinkStatusFragment = ((RadioLinkStatusFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID));
                    if (!radioLinkStatusFragment.popBackStack())
                        super.onBackPressed();
                    break;

                case MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID:
                    UserSettingsFragment userSettingsFragment = ((UserSettingsFragment)
                            ((MainStatePagerAdapter) mNoSwipeViewPager.getAdapter()).
                                    getFragment(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID));
                    if (!userSettingsFragment.popBackStack())
                        super.onBackPressed();
                    break;

                default:
                    super.onBackPressed();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mqttIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mqttIntent);
//        mIsServiceRegistered = getApplicationContext().bindService(mqttIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
        registerMqttBroadcastReceiver();
        registerExcelBroadcastReceiver();
//        subscribeToMQTT();


//        JeroMQAsyncTask jeroMQAsyncTask = new JeroMQAsyncTask();
//        jeroMQAsyncTask.runJeroMQ();


//        subscribeToRabbitMQ();
//                    RabbitMQAsyncTask rabbitMQAsyncTask = new RabbitMQAsyncTask();
//                    rabbitMQAsyncTask.runRabbitMQ();


//        subscribeToRabbitMQ();
    }

//    private void subscribeToMQTT() {
//        mMqttIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mMqttIntent);
//        mIsServiceRegistered = getApplicationContext().bindService(mMqttIntent, rabbitMQServiceConnection, Context.BIND_AUTO_CREATE);
//        registerMqttBroadcastReceiver();
//    }

    @Override
    protected void onStop() {
        super.onStop();
//
        Log.i(TAG, "Main Activity onStop...");
//        System.out.println("Service not registered");
//
//        if (mIsServiceRegistered) {
//            System.out.println("Service registered");
//            getApplicationContext().unbindService(rabbitMQServiceConnection);
//        }

//        JeroMQPubSubBrokerProxy.getInstance().stop();
//        JeroMQPublisher.getInstance().stop();
//        JeroMQSubscriber.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying Main Activity...");

        if (mRabbitMqBroadcastReceiver != null) {
            mRabbitMqBroadcastReceiver = null;
        }

        if (mExcelBroadcastReceiver != null) {
            mExcelBroadcastReceiver = null;
        }

        /* Close service properly. Currently, the service is not destroyed, only the mqtt connection and
         * connection status are closed.
         */
        synchronized (MainActivity.class) {
            if (RabbitMQAsyncTask.mIsServiceRegistered) {
                Log.i(TAG, "Destroying RabbitMQ...");

                RabbitMQAsyncTask.stopRabbitMQ();
                Log.i(TAG, "Stopped RabbitMQ Async Task.");

                stopService(MainApplication.rabbitMQIntent);
                Log.i(TAG, "Stopped RabbitMQ Intent Service.");

                MainApplication.networkService.stopSelf();
                Log.i(TAG, "Stop RabbitMQ Network Self.");

                getApplicationContext().unbindService(MainApplication.rabbitMQServiceConnection);
                Log.i(TAG, "Unbinded RabbitMQ Service.");

                JeroMQPublisher.getInstance().stop();
                JeroMQSubscriber.getInstance().stop();

                Log.i(TAG, "Stopped all JeroMQ connections.");
                RabbitMQAsyncTask.mIsServiceRegistered = false;

                JeroMQPubSubBrokerProxy.getInstance().stop();
            }
        }
    }

//    public interface OnInitMQTTListener {
//        public void onInitMQTT(MqttHelper mqttHelper);
//    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        getApplicationContext().unbindService(rabbitMQServiceConnection);
//    }
}
