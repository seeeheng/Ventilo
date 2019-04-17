package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import sg.gov.dsta.mobileC3.ventilo.NoSwipeViewPager;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.task.TaskFragment;
import sg.gov.dsta.mobileC3.ventilo.helper.MqttHelper;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkServiceBinder;
import sg.gov.dsta.mobileC3.ventilo.network.rabbitmq.IMQListener;
import sg.gov.dsta.mobileC3.ventilo.util.JSONUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // MQTT
    private static final String MQTT_TOPIC_TASK = "Task";
    private static final String MQTT_TOPIC_INCIDENT = "Incident";

    private Intent mMqttIntent;

    private NetworkService mNetworkService;
    //    private NetworkConnectivity mNetworkConnectivity;
    private MqttHelper mMqttHelper;

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
    private C2OpenSansSemiBoldTextView mTvFragmentTitle;
    private C2OpenSansSemiBoldTextView mTvRadioLinkStatus;
    private C2OpenSansSemiBoldTextView mTvLastConnectionDate;
    private C2OpenSansSemiBoldTextView mTvLastConnectionTime;

    private boolean mIsServiceRegistered;

    private IMQListener mIMQListener;
//    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSideMenuPanel();
        initBottomPanel();
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
        mRelativeLayoutTabMap = findViewById(R.id.layout_tab_map_selector_status);
        mRelativeLayoutTabVideoStream = findViewById(R.id.layout_tab_video_stream_selector_status);
        mRelativeLayoutTabReport = findViewById(R.id.layout_tab_report_selector_status);
        mRelativeLayoutTabTimeline = findViewById(R.id.layout_tab_timeline_selector_status);
        mRelativeLayoutTabTask = findViewById(R.id.layout_tab_task_selector_status);
        mRelativeLayoutTabRadioLink = findViewById(R.id.layout_tab_radio_link_selector_status);

        // Line within Tab View
        mLinearLayoutLineSelectorMap = findViewById(R.id.linear_layout_map_line_selector);
        mLinearLayoutLineSelectorVideoStream = findViewById(R.id.linear_layout_video_stream_line_selector);
        mLinearLayoutLineSelectorReport = findViewById(R.id.linear_layout_report_line_selector);
        mLinearLayoutLineSelectorTimeline = findViewById(R.id.linear_layout_timeline_line_selector);
        mLinearLayoutLineSelectorTask = findViewById(R.id.linear_layout_task_line_selector);
        mLinearLayoutLineSelectorRadioLink = findViewById(R.id.linear_layout_radio_link_line_selector);

        // Image Views within Tab View
        mImgViewTabMap = findViewById(R.id.img_tab_map);
        mImgViewTabVideoStream = findViewById(R.id.img_tab_video_stream);
        mImgViewTabReport = findViewById(R.id.img_tab_report);
        mImgViewTabTimeline = findViewById(R.id.img_tab_timeline);
        mImgViewTabTask = findViewById(R.id.img_tab_task);
        mImgViewTabRadioLink = findViewById(R.id.img_tab_radio_link);

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

        mTvFragmentTitle = findViewById(R.id.tv_bottom_panel_fragment_title);
        mTvRadioLinkStatus = findViewById(R.id.tv_bottom_panel_radio_link_status);
        mTvLastConnectionDate = findViewById(R.id.tv_bottom_panel_last_connection_date);
        mTvLastConnectionTime = findViewById(R.id.tv_bottom_panel_last_connection_time);
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
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setVideoStreamSelectedUI() {
        mLinearLayoutLineSelectorVideoStream.setVisibility(View.VISIBLE);
        mImgViewTabVideoStream.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setReportSelectedUI() {
        mLinearLayoutLineSelectorReport.setVisibility(View.VISIBLE);
        mImgViewTabReport.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setTimelineSelectedUI() {
        mLinearLayoutLineSelectorTimeline.setVisibility(View.VISIBLE);
        mImgViewTabTimeline.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setTaskSelectedUI() {
        mLinearLayoutLineSelectorTask.setVisibility(View.VISIBLE);
        mImgViewTabTask.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void setRadioLinkSelectedUI() {
        mLinearLayoutLineSelectorRadioLink.setVisibility(View.VISIBLE);
        mImgViewTabRadioLink.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primary_highlight_cyan),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

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

                                    TaskFragment taskFragment = (TaskFragment) MainStatePagerAdapter.getPageReferenceMap().
                                            get(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);

                                    if (taskFragment != null) {
                                        Log.d(TAG, "Task: Refresh Data");
                                        taskFragment.refreshData();
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

                                    SitRepFragment sitRepFragment = (SitRepFragment) MainStatePagerAdapter.getPageReferenceMap().
                                            get(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);

                                    if (sitRepFragment != null) {
                                        Log.d(TAG, "Sit Rep: Refresh Data");
                                        sitRepFragment.refreshData();
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

    public void setMqttCallback() {
        mMqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());

                System.out.println("Received new mqttMessage");
                String mqttMessageString = mqttMessage.toString();
                boolean isJSON = false;

                if (JSONUtil.isJSONValid(mqttMessageString)) {
                    try {
                        JSONObject mqttMessageJSON = new JSONObject(mqttMessageString);
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

                                TaskFragment taskFragment = (TaskFragment) MainStatePagerAdapter.getPageReferenceMap().
                                        get(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);

                                if (taskFragment != null) {
                                    Log.d(TAG, "Task: Refresh Data");
                                    taskFragment.refreshData();
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

                                SitRepFragment sitRepFragment = (SitRepFragment) MainStatePagerAdapter.getPageReferenceMap().
                                        get(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);

                                if (sitRepFragment != null) {
                                    Log.d(TAG, "Sit Rep: Refresh Data");
                                    sitRepFragment.refreshData();
                                    sitRepFragment.addItemInRecycler();
                                }
                        }

                        isJSON = true;

                    } catch (JSONException ex) {
                        Log.d(TAG, "JSONException: " + ex);
                    }

                }
                //
//                checkMqttTaskTopic(topic, mqttMessage.toString());
//                checkMqttIncidentTopic(topic, mqttMessage.toString());

//                mDataReceived.setText(mqttMessage.toString());

//                if (mqttMessage.toString().contains("PAYLOAD")) {
//                    MapFragment.isTrackAllies = true;
//                    MapFragment.coordString = mqttMessage.toString();
//                    System.out.println("message arrived is " + mqttMessage.toString());
//                }

//                if (!isJSON) {
//                    if (mqttMessage.toString().contains("publish")) {
//                        MapFragment.isTrackAllies = true;
//                        MapFragment.coordString = mqttMessage.toString();
//                        System.out.println("message arrived is " + mqttMessage.toString());
//                    }
//
//
//                    if (mqttMessage.toString().contains("Coords:")) {
//                        System.out.println("GOT THE STRING --> " + mqttMessage.toString());
//                        MapFragment.isTrackAllies = true;
//                        MapFragment.coordString = mqttMessage.toString();
//                    }
//                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

//    private void checkMqttTaskTopic(String topic, String message) {
//        if (MQTT_TOPIC_TASK.equalsIgnoreCase(topic)) {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//            SharedPreferences.Editor editor = pref.edit();
//
//            String subHeaderTaskKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE);
//            editor.putString(subHeaderTaskKey, message);
//        }
//    }
//
//    private void checkMqttIncidentTopic(String topic, String message) {
//        if (MQTT_TOPIC_INCIDENT.equalsIgnoreCase(topic)) {
//
//        }
//    }

//    private void startMqtt() {
//        mqttHelper = new MqttHelper(getApplicationContext());
//        mqttHelper.setCallback(new MqttCallbackExtended() {
//            @Override
//            public void connectComplete(boolean b, String s) {
//
//            }
//
//            @Override
//            public void connectionLost(Throwable throwable) {
//
//            }
//
//            @Override
//            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
//                Log.w("Debug", mqttMessage.toString());
//                mDataReceived.setText(mqttMessage.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//            }
//        });
//    }

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
    private ServiceConnection mMqttServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NetworkServiceBinder binder = (NetworkServiceBinder) service;
            mNetworkService = binder.getService();
//            mNetworkService.deactivate();
//            unbindService(this);
//            System.out.println("Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mNetworkService.deactivate();
//            System.out.println("Disconnected");
//            Toast.makeText(getApplicationContext(), "Service Disconnected", Toast.LENGTH_LONG).show();
        }
    };

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

    private void registerMqttBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//        filter.addAction(MqttHelper.MQTT_CONNECT_INTENT_ACTION);
        System.out.println("registerMqttBroadcastReceiver");
        BroadcastReceiver receiver = new BroadcastReceiver() {
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
                        System.out.println("RabbitMQConnectionStatus.CONNECTED");
                        setupMQListener();
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onBackPressed() {
        System.out.println("Main Activity onBackPressed");
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            // Go to login page
            super.onBackPressed();
        } else {
            String taggedName = getSupportFragmentManager().getBackStackEntryAt(
                    getSupportFragmentManager().getBackStackEntryCount() - 1).getName();

            Log.d(TAG, "returned fragment matches" + taggedName);

            if (getSupportFragmentManager().findFragmentByTag(taggedName) instanceof MapShipBlueprintFragment) {
//                mBottomNavigationView.setVisibility(View.VISIBLE);

//                System.out.println("getSupportFragmentManager().getBackStackEntryCount() is " + getSupportFragmentManager().getBackStackEntryCount());
//                String newTaggedName = getSupportFragmentManager().getBackStackEntryAt(
//                        getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
//                String newTaggedName2 = getSupportFragmentManager().getBackStackEntryAt(
//                        getSupportFragmentManager().getBackStackEntryCount() - 3).getName();
//                System.out.println("newTaggedName is " + newTaggedName);
//                System.out.println("newTaggedName 2 is " + newTaggedName2);
//                System.out.println("MapFragment.class.getSimpleName() is " + MapFragment.class.getSimpleName());
//
//                System.out.println("getSupportFragmentManager().findFragmentByTag(\"android:switcher:\" + R.id.viewpager_main_nav + \":\" + 1) is " + getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager_main_nav + ":" + 1));
//                MapFragment returnedFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager_main_nav + ":" + 1);
//                if (0 == mNoSwipeViewPager.getCurrentItem() && null != returnedFragment) {
//                    ((MapFragment) returnedFragment).onVisible();
//                }

                // Notify mapfragment of previous fragment (MapShipBlueprintFragment) that it was switched from
                EventBus.getDefault().post(PageEvent.getInstance().addPage(PageEvent.FRAGMENT_KEY, MapShipBlueprintFragment.class.getSimpleName()));

//                for (int i = 0; i < backStackCount; i++) {
//                    if (MapFragment.class.getSimpleName().equalsIgnoreCase(
//                            getSupportFragmentManager().getBackStackEntryAt(i).getName())) {
//
////                        MapFragment returnedFragment = (MapFragment) getSupportFragmentManager().
////                                findFragmentByTag(MapFragment.class.getSimpleName());
//                        returnedFragment.onVisible();
//                    }
//                }
            } else if (getSupportFragmentManager().findFragmentByTag(taggedName) instanceof TaskFragment) {
                TaskFragment returnedFragment = (TaskFragment) getSupportFragmentManager().findFragmentByTag(taggedName);
                returnedFragment.refreshData();
            }

//            String fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
//            Log.d(TAG, "fragmentTag is " + fragmentTag);
//            if (TaskFragment.class.getSimpleName().equalsIgnoreCase(fragmentTag)) {
//
//                TaskFragment returnedFragment = (TaskFragment) fragmentManager.findFragmentByTag(fragmentTag);
//                returnedFragment.refreshUI();
//            }

            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mqttIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mqttIntent);
//        mIsServiceRegistered = getApplicationContext().bindService(mqttIntent, mMqttServiceConnection, Context.BIND_AUTO_CREATE);
//        registerMqttBroadcastReceiver();


//        subscribeToMQTT();
        subscribeToRabbitMQ();
    }

//    private void subscribeToMQTT() {
//        mMqttIntent = new Intent(getApplicationContext(), NetworkService.class);
//        startService(mMqttIntent);
//        mIsServiceRegistered = getApplicationContext().bindService(mMqttIntent, mMqttServiceConnection, Context.BIND_AUTO_CREATE);
//        registerMqttBroadcastReceiver();
//    }

    private void subscribeToRabbitMQ() {
        mMqttIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(mMqttIntent);
        mIsServiceRegistered = getApplicationContext().bindService(mMqttIntent, mMqttServiceConnection, Context.BIND_AUTO_CREATE);
        registerMqttBroadcastReceiver();

//        new RabbitMQTask().execute("");
//        RabbitMQHelper.getInstance(this.getApplicationContext()).startRabbitMQWithDefaultSetting();
//        if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
//            setupMQListener();
//        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        System.out.println("Service not registered");
//
//        if (mIsServiceRegistered) {
//            System.out.println("Service registered");
//            getApplicationContext().unbindService(mMqttServiceConnection);
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Close service properly. Currently, the service is not destroyed, only the mqtt connection and
         * connection status are closed.
         */
        if (mIsServiceRegistered) {
//            stopService(mMqttIntent);
            getApplicationContext().unbindService(mMqttServiceConnection);
            mIsServiceRegistered = false;
        }
    }

//    private class RabbitMQTask extends AsyncTask<String, Void, String> {
//
//        protected String doInBackground(String... urls) {
//            RabbitMQHelper.getInstance().startRabbitMQWithDefaultSetting();
//            if (RabbitMQHelper.connectionStatus == RabbitMQHelper.RabbitMQConnectionStatus.CONNECTED) {
//                setupMQListener();
//            }
//            return "";
//        }
//
//        protected void onPostExecute(String result) {
//            // TODO: check this.exception
//            // TODO: do something with the result
//        }
//    }

//    public interface OnInitMQTTListener {
//        public void onInitMQTT(MqttHelper mqttHelper);
//    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        getApplicationContext().unbindService(mMqttServiceConnection);
//    }
}
