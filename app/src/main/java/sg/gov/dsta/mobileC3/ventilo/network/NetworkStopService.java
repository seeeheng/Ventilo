package sg.gov.dsta.mobileC3.ventilo.network;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQServerPair;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioClient;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class NetworkStopService extends Service {

    private static final String TAG = NetworkStopService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Stop service started.");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stop service destroyed.");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(TAG, "onTaskRemoved");

        // Close network service upon application removal by user from recently used task list
        synchronized (NetworkStopService.class) {
//            NetworkService.deactivate();

            if (NetworkService.mIsServiceRegistered) {
                if (getApplication() instanceof MainApplication) {
//                    NetworkService.deactivate();
                    stopService(((MainApplication) getApplication()).getNetworkIntent());
                    Log.i(TAG, "Stopped network intent service.");

                    NetworkService.mIsServiceRegistered = false;
                }
            }

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
//            WaveRelayRadioClient.stopWaveRelayRadioJobService();
            WaveRelayRadioClient.closeWebSocketClient();

            // Resets access token of user
            resetUserAccessTokenAndBroadcastUpdate();

            JeroMQPublisher.getInstance().stop();
            JeroMQSubscriber.getInstance().stop();
            JeroMQServerPair.getInstance().stop();

            Log.i(TAG, "Stopped all JeroMQ connections.");
        }

        stopSelf();
    }

    /**
     * Updates/resets user information with access token and broadcast updated model to other devices
     */
    private void saveAndBroadcastUserInfo(UserRepository userRepository, UserModel userModel) {
        userModel.setAccessToken(StringUtil.EMPTY_STRING);
        userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
        userModel.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
        userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

        userRepository.updateUser(userModel);

        // Send updated User model to all other devices
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.ACCESS_TOKEN,
                StringUtil.EMPTY_STRING);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                ERadioConnectionStatus.OFFLINE.toString());
    }

    private void resetUserAccessTokenAndBroadcastUpdate() {
        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());

        SingleObserver<UserModel> singleObserverUserByUserId =
                new SingleObserver<UserModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(UserModel userModel) {
                        if (userModel != null) {
                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel: %s " ,userModel);
                            saveAndBroadcastUserInfo(userRepository, userModel);

                        } else {
                            Timber.i("onSuccess singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. userModel is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverUserByUserId, resetUserAccessTokenAndBroadcastUpdate. Error Msg: %s" , e.toString());

                    }
                };

        userRepository.queryUserByUserId(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverUserByUserId);
    }

    /**
     * Updates/resets radio information with userId account and broadcast updated model to other devices
     */
    private void saveAndBroadcastRadioInfo(WaveRelayRadioRepository waveRelayRadioRepository,
                                           WaveRelayRadioModel waveRelayRadioModel,
                                           String userId, String phoneIpAddr) {
        waveRelayRadioModel.setUserId(userId);
        waveRelayRadioModel.setPhoneIpAddress(phoneIpAddr);

        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);

        // Send updated WaveRelay model to all other devices
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_DEVICE_IP_ADDRESS,
                phoneIpAddr);
    }

    /**
     * Resets User Id of Radio info and notifies (broadcasts update to) other devices
     *
     * @param radioId
     */
    private void resetWrRadioUserIDAndBroadcastUpdate(long radioId) {
        WaveRelayRadioRepository waveRelayRadioRepository = new WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                new SingleObserver<WaveRelayRadioModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                        if (waveRelayRadioModel != null) {

                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, resetWrRadioUserIDAndBroadcastUpdate. waveRelayRadioModel: %s" , waveRelayRadioModel);

                            saveAndBroadcastRadioInfo(waveRelayRadioRepository, waveRelayRadioModel,
                                    null, StringUtil.INVALID_STRING);

                        } else {


                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, resetWrRadioUserIDAndBroadcastUpdate. waveRelayRadioModel is null");

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverWaveRelayRadioByRadioNo,resetWrRadioUserIDAndBroadcastUpdate. Error Msg: %s " , e.toString());

                    }
                };

        waveRelayRadioRepository.queryRadioByRadioId(radioId, singleObserverWaveRelayRadioByRadioNo);
    }
}
