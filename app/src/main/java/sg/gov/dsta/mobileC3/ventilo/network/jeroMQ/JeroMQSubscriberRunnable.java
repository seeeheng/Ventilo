package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.BftEvent;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class JeroMQSubscriberRunnable implements Runnable {

    private static final String TAG = JeroMQSubscriberRunnable.class.getSimpleName();

    private List<Socket> mSocketList;
    private ZMQ.Poller mPoller;
    private boolean mIsPollerToBeClosed;
    private CloseSocketsListener mCloseSocketsListener;

    protected JeroMQSubscriberRunnable(List<Socket> socketList, ZMQ.Poller poller,
                                       CloseSocketsListener closeSocketsListener) {
        mSocketList = socketList;
        mPoller = poller;
        mCloseSocketsListener = closeSocketsListener;
    }

    @Override
    public void run() {
        subscribeTopics(mSocketList);

        while (!Thread.currentThread().interrupted()) {
            Timber.i("Listening for messages...");

            if (mIsPollerToBeClosed && mPoller != null) {
                mPoller.close();

                if (mCloseSocketsListener != null) {
                    mCloseSocketsListener.closeSockets();
                }
                break;
            }

            mPoller.poll();

            for (int i = 0; i < mSocketList.size(); i++) {
                if (mPoller.pollin(i)) {
                    storeMessageInDatabase(mSocketList.get(i));
//                    Timber.i("Received message: %s", message);
                }
            }
        }

        Timber.i("Subscriber poller closed.");
    }

    protected void closePoller() {
        mIsPollerToBeClosed = true;
    }

    private synchronized void storeMessageInDatabase(Socket socket) {
        String message = "";
        try {
            message = socket.recvStr();

            Timber.i("Received message: %s", message);

            String messageTopic = StringUtil.getFirstWord(message);
            String messageContent = StringUtil.removeFirstWord(message);
            String[] messageTopicParts = messageTopic.split(StringUtil.HYPHEN);

            // For e.g. PREFIX-USER, PREFIX-RADIO, PREFIX-BFT, PREFIX-SITREP, PREFIX-TASK
            String messageMainTopic = messageTopicParts[0].
                    concat(StringUtil.HYPHEN).concat(messageTopicParts[1]);

//            String messageTopicAction = "";

            // For e.g. SYNC, INSERT, UPDATE, DELETE
            // Only BFT message does not have message topic action
//            if (!messageTopic.equalsIgnoreCase(JeroMQPublisher.TOPIC_PREFIX_BFT)) {
//                messageTopicAction = messageTopicParts[2];
//            }
            String messageTopicAction = messageTopicParts[2];

            switch (messageMainTopic) {
                case JeroMQPublisher.TOPIC_PREFIX_USER:
                    storeUserMessage(messageContent, messageTopicAction);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_RADIO:
                    storeRadioMessage(messageContent, messageTopicAction);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_BFT:
                    storeBftMessage(messageContent, messageTopicAction);
//                    updateBftWithMessage(message);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_SITREP:
                    storeSitRepMessage(messageContent, messageTopicAction);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_TASK:
                    storeTaskMessage(messageContent, messageTopicAction);
                    break;
                default:
                    break;
            }

            try {
                Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE); //milliseconds

            } catch (InterruptedException e) {
                Timber.i("Sub socket thread interrupted.");

            }

        } catch (ZMQException e) {
            Timber.i("Socket exception - %s", e);
//            Timber.i("Closing sub socket..");
//
//            socket.close();
//
//            Timber.i("Sub socket closed..");
        }
    }

    /**
     * Sends incoming BFT messages from other devices to listener in Map Ship Blueprint fragment
     *
     * @param jsonMsg
     * @param messageTopicAction
     */
    private synchronized void storeBftMessage(String jsonMsg, String messageTopicAction) {
//        Timber.i("updateBftWithMessage bftMsg: %s", bftMsg);
//
//        EventBus.getDefault().post(BftEvent.getInstance().setBftMessage(bftMsg));

        Timber.i("storeBftMessage jsonMsg: %s", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            BFTRepository bftRepo = new BFTRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            BFTModel bftModel = gson.fromJson(jsonMsg, BFTModel.class);

            if (!SharedPreferenceUtil.getCurrentUserCallsignID().equalsIgnoreCase(bftModel.getUserId())) {
                switch (messageTopicAction) {
                    case JeroMQPublisher.TOPIC_INSERT:
                        Timber.i("storeUserMessage insert");

                        databaseOperation.insertBftIntoDatabase(bftRepo, bftModel);
                        break;

//                    case JeroMQPublisher.TOPIC_UPDATE:
//                        Timber.i("storeUserMessage update");
//
//                        databaseOperation.updateUserInDatabase(bftRepo, bftModel);
//                        break;
//
//                    case JeroMQPublisher.TOPIC_DELETE:
//                        Timber.i("storeUserMessage delete");
//
//                        databaseOperation.deleteUserInDatabase(bftRepo, userModel.getUserId());
//                        break;
                }
            }
        }
    }

    /**
     * Stores/updates/deletes/synchronises incoming WaveRelay Radio JSON messages
     * from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeRadioMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeRadioMessage jsonMsg: %s", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            WaveRelayRadioRepository waveRelayRadioRepository = new
                    WaveRelayRadioRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            WaveRelayRadioModel waveRelayRadioModel = gson.fromJson(jsonMsg,
                    WaveRelayRadioModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeRadioMessage insert");
                    databaseOperation.insertRadioIntoDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeRadioMessage update");

                    if (waveRelayRadioModel.getUserId() != null &&
                            !SharedPreferenceUtil.getCurrentUserCallsignID().
                                    equalsIgnoreCase(waveRelayRadioModel.getUserId())) {
                        databaseOperation.updateRadioInDatabase(waveRelayRadioRepository,
                                waveRelayRadioModel);
                    }

                    break;

                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeRadioMessage delete");
                    databaseOperation.deleteRadioInDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel.getRadioId());
                    break;
            }
        }
    }

    /**
     * Handles incoming WaveRelayRadioModel model for Synchronisation
     *
     * @param databaseOperation
     * @param waveRelayRadioRepository
     * @param waveRelayRadioModel
     */
    private void handleRadioDataSync(DatabaseOperation databaseOperation,
                                     WaveRelayRadioRepository waveRelayRadioRepository,
                                     WaveRelayRadioModel waveRelayRadioModel) {
        /**
         * Query for WaveRelayRadio model from database with id of received WaveRelayRadio model.
         *
         * If model exists in database, result will return a valid User model
         * Else, result will return NULL.
         *
         * If result returns a valid WaveRelayRadio model, update returned WaveRelayRadio model with
         * received WaveRelayRadio model
         * Else, insert received WaveRelayRadio model into database.
         */
        SingleObserver<WaveRelayRadioModel> singleObserverSyncRadio = new SingleObserver<WaveRelayRadioModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(WaveRelayRadioModel matchedWaveRelayRadioModel) {
                if (matchedWaveRelayRadioModel == null) {
                    Timber.i("Inserting new WaveRelay Radio model from synchronisation...");

                    databaseOperation.insertRadioIntoDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                } else {
                    Timber.i("Updating WaveRelay Radio model from synchronisation...");
                    databaseOperation.updateRadioInDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverSyncRadio, handleRadioDataSync.Error Msg: %s", e.toString());

            }
        };

        databaseOperation.queryRadioByRadioIdInDatabase(waveRelayRadioRepository,
                waveRelayRadioModel.getRadioId(), singleObserverSyncRadio);
    }

    /**
     * Stores/updates/deletes/synchronises incoming User JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeUserMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeUserMessage jsonMsg: %s", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            UserModel userModel = gson.fromJson(jsonMsg, UserModel.class);

            if (!SharedPreferenceUtil.getCurrentUserCallsignID().equalsIgnoreCase(userModel.getUserId())) {
                switch (messageTopicAction) {
                    case JeroMQPublisher.TOPIC_INSERT:
                        Timber.i("storeUserMessage insert");

                        databaseOperation.insertUserIntoDatabase(userRepo, userModel);
                        break;

                    case JeroMQPublisher.TOPIC_UPDATE:
                        Timber.i("storeUserMessage update");

                        databaseOperation.updateUserInDatabase(userRepo, userModel);
                        break;

                    case JeroMQPublisher.TOPIC_DELETE:
                        Timber.i("storeUserMessage delete");

                        databaseOperation.deleteUserInDatabase(userRepo, userModel.getUserId());
                        break;
                }
            }
        }
    }

    /**
     * Handles incoming User model for Synchronisation
     *
     * @param databaseOperation
     * @param userRepo
     * @param userModel
     */
    private void handleUserDataSync(DatabaseOperation databaseOperation, UserRepository userRepo,
                                    UserModel userModel) {
        /**
         * Query for User model from database with id of received User model.
         *
         * If model exists in database, result will return a valid User model
         * Else, result will return NULL.
         *
         * If result returns a valid User model, update returned User model with received User model
         * Else, insert received User model into database.
         */
        SingleObserver<UserModel> singleObserverSyncUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(UserModel matchedUserModel) {
                if (matchedUserModel == null) {
                    Timber.i("Inserting new User model from synchronisation...");
                    databaseOperation.insertUserIntoDatabase(userRepo, userModel);
                } else {
                    Timber.i("Updating User model from synchronisation...");
                    databaseOperation.updateUserInDatabase(userRepo, userModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverSyncUser, handleUserDataSync. Error Msg: %s", e.toString());


            }
        };

        databaseOperation.queryUserByUserIdInDatabase(userRepo, userModel.getUserId(), singleObserverSyncUser);
    }

    /**
     * Stores/updates/deletes/synchronises incoming Sit Rep JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeSitRepMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeSitRepMessage jsonMsg:%s ", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
//                case JeroMQPublisher.TOPIC_SYNC:
//                    Timber.i("storeSitRepMessage sync");
//
//                    handleSitRepDataSync(databaseOperation, sitRepRepo, sitRepModel);
//                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeSitRepMessage insert");

                    databaseOperation.insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeSitRepMessage update");

                    databaseOperation.updateSitRepInDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeSitRepMessage delete");

                    databaseOperation.deleteSitRepInDatabase(sitRepRepo, sitRepModel.getId());
                    break;
            }
        }
    }

//    /**
//     * Handles incoming Sit Rep model for Synchronisation
//     *
//     * @param databaseOperation
//     * @param sitRepRepo
//     * @param sitRepModel
//     */
//    private void handleSitRepDataSync(DatabaseOperation databaseOperation, SitRepRepository sitRepRepo,
//                                      SitRepModel sitRepModel) {
//        /**
//         * Query for Sit Rep (SR) model from database with id of received SR model.
//         *
//         * If model exists in database, result will return a valid SR model
//         * Else, result will return NULL.
//         *
//         * If result returns a valid SR model, update returned SR model with received SR model
//         * Else, insert received SR model into database.
//         */
//        SingleObserver<SitRepModel> singleObserverSyncSitRep = new SingleObserver<SitRepModel>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onSuccess(SitRepModel matchedSitRepModel) {
//                if (matchedSitRepModel == null) {
//                    Timber.i("Inserting new Sit Rep model from synchronisation...");
//
//                    databaseOperation.insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
//                } else {
//                    Timber.i("Updating Sit Rep model from synchronisation...");
//
//                    databaseOperation.updateSitRepInDatabase(sitRepRepo, sitRepModel);
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.i("onError singleObserverSyncSitRep, syncSitRepInDatabase. Error Msg: %s" , e.toString());
//            }
//        };
//
//        databaseOperation.querySitRepByCreatedDateTimeInDatabase(sitRepRepo,
//                sitRepModel.getCreatedDateTime(), singleObserverSyncSitRep);
//    }

    /**
     * Stores/updates/deletes/synchronises incoming Task JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeTaskMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeTaskMessage jsonMsg: %s", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            TaskModel taskModel = gson.fromJson(jsonMsg, TaskModel.class);

            switch (messageTopicAction) {
//                case JeroMQPublisher.TOPIC_SYNC:
//                    Timber.i("storeTaskMessage sync");
//
//                    handleTaskRepDataSync(databaseOperation, taskRepo, taskModel);
//                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeTaskMessage insert");

                    databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeTaskMessage update");

                    databaseOperation.updateTaskInDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeTaskMessage delete");

                    databaseOperation.deleteTaskInDatabase(taskRepo, taskModel.getId());
                    break;
            }
        }
    }

//    /**
//     * Handles incoming Task model for Synchronisation
//     *
//     * @param databaseOperation
//     * @param taskRepo
//     * @param taskModel
//     */
//    private void handleTaskRepDataSync(DatabaseOperation databaseOperation, TaskRepository taskRepo,
//                                       TaskModel taskModel) {
//        /**
//         * Query for Task model from database with id of received Task model.
//         *
//         * If model exists in database, result will return a valid Task model
//         * Else, result will return NULL.
//         *
//         * If result returns a valid SR model, update returned SR model with received SR model
//         * Else, insert received SR model into database.
//         */
//        SingleObserver<TaskModel> singleObserverSyncTask = new SingleObserver<TaskModel>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onSuccess(TaskModel matchedTaskModel) {
//                if (matchedTaskModel == null) {
//                    Timber.i("Inserting new Task model from synchronisation...");
//
//                    databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
//                } else {
//                    Timber.i("Updating Task model from synchronisation...");
//
//                    databaseOperation.updateTaskInDatabase(taskRepo, taskModel);
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.i("onError singleObserverSyncTask, syncTaskInDatabase. Error Msg: %s" , e.toString());
//            }
//        };
//
//        databaseOperation.queryTaskByCreatedDateTimeInDatabase(taskRepo,
//                taskModel.getCreatedDateTime(), singleObserverSyncTask);
//    }

    /**
     * Subscribe socket list to all relevant topics
     *
     * @param socketList
     */
    private void subscribeTopics(List<Socket> socketList) {
        for (int i = 0; i < mSocketList.size(); i++) {
            Socket socket = socketList.get(i);
//            socket.subscribe("".getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_USER.getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_RADIO.getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_BFT.getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_SITREP.getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_TASK.getBytes());
        }
    }

    public interface CloseSocketsListener {
        void closeSockets();
    }
}
