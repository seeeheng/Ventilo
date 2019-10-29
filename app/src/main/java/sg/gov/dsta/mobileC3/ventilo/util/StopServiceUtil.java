package sg.gov.dsta.mobileC3.ventilo.util;

import android.app.Application;
import android.util.Log;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.network.NetworkService;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQClientPair;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQServerPair;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioClient;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class StopServiceUtil {

    private static final String TAG = StopServiceUtil.class.getSimpleName();

    public static void stopAllServices() {

        // Close network service upon application removal by user from recently used task list
        synchronized (StopServiceUtil.class) {

            // Resets access token of user
            resetUserAccessTokenAndBroadcastUpdate();

            // Close any radio web sockets
            WaveRelayRadioClient.closeWebSocketClient();

//            JeroMQClientPair.getInstance().stop();
//            JeroMQServerPair.getInstance().stop();
            JeroMQPublisher.getInstance().stop();
            JeroMQSubscriber.getInstance().stop();

            Timber.i("Stopped all JeroMQ connections.");

            MainApplication mainApplication = (MainApplication)
                    MainApplication.getAppContext();

//            if (NetworkService.mIsServiceRegistered) {
//                if (mainApplication != null) {
////                    NetworkService.deactivate();
//                    mainApplication.stopService(mainApplication.getNetworkServiceIntent());
//                    Timber.i("Stopped network service intent.");
//
//                    NetworkService.mIsServiceRegistered = false;
//                }
//            }
        }
    }

    /**
     * Updates/resets user information with access token and broadcast updated model to other devices
     */
    private static void saveAndBroadcastUserInfo(UserRepository userRepository, UserModel userModel) {
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

    private static void resetUserAccessTokenAndBroadcastUpdate() {
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
}
