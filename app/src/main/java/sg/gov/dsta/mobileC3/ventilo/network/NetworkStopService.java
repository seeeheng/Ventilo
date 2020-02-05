package sg.gov.dsta.mobileC3.ventilo.network;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.util.Log;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.StopServiceUtil;

public class NetworkStopService extends Service {

    private static final String TAG = NetworkStopService.class.getSimpleName();
    private static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "1";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

//            Notification.Builder builder = new Notification.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID)
//                    .setContentTitle(getString(R.string.notification_header))
//                    .setContentText(getString(R.string.notification_service_ended))
//                    .setChannelId(DEFAULT_NOTIFICATION_CHANNEL_ID)
//                    .setAutoCancel(true);
//
//            Notification notification = builder.build();
//            startForeground(1, notification);

            startMyOwnForeground();

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, DEFAULT_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_header))
                    .setContentText(getString(R.string.notification_service_ended))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        Log.i(TAG, "Network stop service started.");

        return START_NOT_STICKY;
    }

    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.default_soldier_icon)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stop service destroyed.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved");

        // Close network service upon application removal by user from recently used task list
        synchronized (NetworkStopService.class) {
//            NetworkService.deactivate();

//            if (NetworkService.mIsServiceRegistered) {
//                if (getApplication() instanceof MainApplication) {
////                    NetworkService.deactivate();
//                    stopService(((MainApplication) getApplication()).getNetworkServiceIntent());
//                    Log.i(TAG, "Stopped network intent service.");
//
//                    NetworkService.mIsServiceRegistered = false;
//                }
//            }

//            // Resets Wave Relay radio info of user
//            Object userRadioId = SharedPreferenceUtil.getSharedPreference(SharedPreferenceConstants.USER_RADIO_NO,
//                    0);
//
//            if (userRadioId != null) {
//                if (userRadioId instanceof Integer) {
//                    Timber.i("Resetting User Id of radio info...");
//
//                    resetWrRadioUserIDAndBroadcastUpdate((int) userRadioId);
//                }
//            }

            // Close any radio web sockets
////            WaveRelayRadioClient.stopWaveRelayRadioJobService();
//            WaveRelayRadioClient.closeWebSocketClient();
//
//            // Resets access token of user
//            resetUserAccessTokenAndBroadcastUpdate();
//
//            JeroMQPublisher.getInstance().stop();
//            JeroMQSubscriber.getInstance().stop();
//            JeroMQClientPair.getInstance().stop();
//            JeroMQServerPair.getInstance().stop();
//
//            Log.i(TAG, "Stopped all JeroMQ connections.");

            StopServiceUtil.stopAllServices();
        }

        this.stopSelf();
    }
//
//    /**
//     * Updates/resets user information with access token and broadcast updated model to other devices
//     */
//    private void saveAndBroadcastUserInfo(UserRepository userRepository, UserModel userModel) {
//        userModel.setAccessToken(StringUtil.EMPTY_STRING);
//        userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
//        userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
//        userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
//        userModel.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
//        userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));
//
//        userRepository.updateUser(userModel);
//
//        // Send updated User model to all other devices
//        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);
//
//        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.ACCESS_TOKEN,
//                StringUtil.EMPTY_STRING);
//
//        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
//                ERadioConnectionStatus.OFFLINE.toString());
//    }
//
//    private void resetUserAccessTokenAndBroadcastUpdate() {
//        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());
//
//        SingleObserver<UserModel> singleObserverUserByUserId =
//                new SingleObserver<UserModel>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        // add it to a CompositeDisposable
//                    }
//
//                    @Override
//                    public void onSuccess(UserModel userModel) {
//                        if (userModel != null) {
//                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel: %s " ,userModel);
//                            saveAndBroadcastUserInfo(userRepository, userModel);
//
//                        } else {
//                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel is null");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                        Timber.e("onError singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. Error Msg: %s" , e.toString());
//
//                    }
//                };
//
//        userRepository.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
//                singleObserverUserByUserId);
//    }
//
//    /**
//     * Updates/resets radio information with userId account and broadcast updated model to other devices
//     */
//    private void saveAndBroadcastRadioInfo(WaveRelayRadioRepository waveRelayRadioRepository,
//                                           WaveRelayRadioModel waveRelayRadioModel,
//                                           String userId, String phoneIpAddr) {
//        waveRelayRadioModel.setUserId(userId);
//        waveRelayRadioModel.setPhoneIpAddress(phoneIpAddr);
//
//        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
//
//        // Send updated WaveRelay model to all other devices
//        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);
//
//        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_DEVICE_IP_ADDRESS,
//                phoneIpAddr);
//    }
//
//    /**
//     * Resets User Id of Radio info and notifies (broadcasts update to) other devices
//     *
//     * @param radioId
//     */
//    private void resetWrRadioUserIDAndBroadcastUpdate(long radioId) {
//        WaveRelayRadioRepository waveRelayRadioRepository = new WaveRelayRadioRepository((Application) MainApplication.getAppContext());
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
//                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, resetWrRadioUserIDAndBroadcastUpdate. waveRelayRadioModel: %s" , waveRelayRadioModel);
//
//                            saveAndBroadcastRadioInfo(waveRelayRadioRepository, waveRelayRadioModel,
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
//                        Timber.e("onError singleObserverWaveRelayRadioByRadioNo,resetWrRadioUserIDAndBroadcastUpdate. Error Msg: %s " , e.toString());
//
//                    }
//                };
//
//        waveRelayRadioRepository.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
//    }
}
