package dsta.sg.com.ventilo.activity.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import dsta.sg.com.ventilo.NoSwipeViewPager;
import dsta.sg.com.ventilo.R;
import dsta.sg.com.ventilo.helper.MqttHelper;
import dsta.sg.com.ventilo.network.NetworkConnectivity;
import dsta.sg.com.ventilo.network.NetworkService;
import dsta.sg.com.ventilo.network.NetworkServiceBinder;
import dsta.sg.com.ventilo.util.constant.MainNavigationConstants;

public class MainActivity extends AppCompatActivity {

    private Intent mqttIntent;

    NetworkService mNetworkService;
    NetworkConnectivity mNetworkConnectivity;
    MqttHelper mqttHelper;
    TextView dataReceived;
    Button mBtnPublish;
    private boolean isServiceRegistered;




//    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ownUserId = 10;
//        ownUserId = Long.parseLong(getIntent().getStringExtra("USER_ID"));

        MainStatePagerAdapter mainStatePagerAdapter = new MainStatePagerAdapter(
                getSupportFragmentManager());
        final NoSwipeViewPager viewPager = findViewById(R.id.viewpager_main_nav);
        viewPager.setAdapter(mainStatePagerAdapter);
        viewPager.setPagingEnabled(false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.btm_nav_view_main_nav);

        NoSwipeViewPager.OnPageChangeListener viewPagerPageChangeListener =
                setPageChangeListener(bottomNavigationView);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        BottomNavigationView.OnNavigationItemSelectedListener
                navigationItemSelectedListener = setNavigationItemSelectedListener(viewPager);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        dataReceived = findViewById(R.id.dataReceived);
        mBtnPublish = findViewById(R.id.btn_mqtt_publish);

        mBtnPublish.setVisibility(View.GONE);
//        startMqtt();

        mBtnPublish.setOnClickListener(publishOnClickListener());
    }

    private NoSwipeViewPager.OnPageChangeListener setPageChangeListener(
            final BottomNavigationView bottomNavigationView) {

        return new NoSwipeViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                switch (position) {
                    case MainNavigationConstants.BTM_NAV_MENU_MAP_POSITION_ID:
                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_map);
                        return;

                    case MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID:
                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_timeline);
                        return;

                    case MainNavigationConstants.BTM_NAV_MENU_REPORT_POSITION_ID:
                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_report);
                        return;

                    case MainNavigationConstants.BTM_NAV_MENU_NOTIFICATION_POSITION_ID:
                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_notification);
                        return;

                    case MainNavigationConstants.BTM_NAV_MENU_STREAM_POSITION_ID:
                        bottomNavigationView.setSelectedItemId(R.id.btn_nav_action_stream);
                        return;

                    default:
                        return;
                }
            }
        };
    }

    private BottomNavigationView.OnNavigationItemSelectedListener setNavigationItemSelectedListener(
            final NoSwipeViewPager viewPager) {

        return new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.btn_nav_action_map:
                        viewPager.setCurrentItem(MainNavigationConstants.BTM_NAV_MENU_MAP_POSITION_ID,
                                true);
                        return true;

                    case R.id.btn_nav_action_timeline:
                        viewPager.setCurrentItem(MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID,
                                true);
                        return true;

                    case R.id.btn_nav_action_report:
                        viewPager.setCurrentItem(MainNavigationConstants.BTM_NAV_MENU_REPORT_POSITION_ID,
                                true);
                        return true;

                    case R.id.btn_nav_action_notification:
                        viewPager.setCurrentItem(MainNavigationConstants.BTM_NAV_MENU_NOTIFICATION_POSITION_ID,
                                true);
                        return true;

                    case R.id.btn_nav_action_stream:
                        viewPager.setCurrentItem(MainNavigationConstants.BTM_NAV_MENU_STREAM_POSITION_ID,
                                true);
                        return true;

                    default:
                        return false;
                }
            }
        };
    }

    private View.OnClickListener publishOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mqttHelper != null) {
                    mqttHelper.publishMessage();
                }
            }
        };
    }

    public void setMqttCallback() {
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

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
//                dataReceived.setText(mqttMessage.toString());
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//            }
//        });
//    }

    /** Defines callbacks for service binding, passed to bindService() */
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

    private void registerBroadcastReceiverForMqtt() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MqttHelper.MQTT_CONNECT_INTENT_ACTION);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MqttHelper.MQTT_CONNECT_INTENT_ACTION.equalsIgnoreCase(intent.getAction())) {
                    if (mqttHelper == null) {
                        mqttHelper = MqttHelper.getInstance();
                        setMqttCallback();
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            // Go to login page
            super.onBackPressed();
        } else {
//        FragmentManager.BackStackEntry backStackEntryAt = getSupportFragmentManager().getBackStackEntryAt(count-1);
//        String name = backStackEntryAt.getName();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mqttIntent = new Intent(getApplicationContext(), NetworkService.class);
        startService(mqttIntent);
        isServiceRegistered = getApplicationContext().bindService(mqttIntent, mMqttServiceConnection, Context.BIND_AUTO_CREATE);
        registerBroadcastReceiverForMqtt();


    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        System.out.println("Service not registered");
//
//        if (isServiceRegistered) {
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
        if (isServiceRegistered) {
            getApplicationContext().unbindService(mMqttServiceConnection);
        }
    }



//    @Override
//    protected void onPause() {
//        super.onPause();
//
////        getApplicationContext().unbindService(mMqttServiceConnection);
//    }
}
