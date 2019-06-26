package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserSitRepJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;

public class JeroMQSubscriberRunnable implements Runnable {

    private static final String TAG = JeroMQSubscriberRunnable.class.getSimpleName();

    private Socket mSocket;

    protected JeroMQSubscriberRunnable(Socket socket) {
        mSocket = socket;
    }

    @Override
    public void run() {
        subscribeTopics(mSocket);

//        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
        while (!Thread.currentThread().interrupted()) {
            Log.i(TAG, "Listening for messages...");

            String message = "";
            try {
                message = mSocket.recvStr();
            } catch (ZMQException e)  {
                Log.d(TAG, "Socket exception - " + e);
                Log.d(TAG, "Closing sub socket..");
                mSocket.close();
                Log.d(TAG, "Sub socket closed..");
                break;
            }

            String messageTopic = StringUtil.getFirstWord(message);
            String messageContent = StringUtil.removeFirstWord(message);
            String[] messageTopicParts = messageTopic.split(StringUtil.HYPHEN);

            // For e.g. PREFIX-USER, PREFIX-RADIO, PREFIX-BFT, PREFIX-SITREP, PREFIX-TASK
            String messageMainTopic = messageTopicParts[0].
                    concat(StringUtil.HYPHEN).concat(messageTopicParts[1]);

            // For e.g. SYNC, INSERT, UPDATE, DELETE
            String messageTopicAction = messageTopicParts[2];

            switch (messageMainTopic) {
                case JeroMQPublisher.TOPIC_PREFIX_USER:
                    storeUserMessage(messageContent, messageTopicAction);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_RADIO:
                    storeRadioMessage(messageContent, messageTopicAction);
                    break;
                case JeroMQPublisher.TOPIC_PREFIX_BFT:
                    storeBFTMessage(messageContent);
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

            Log.i(TAG, "Received message: " + message);

            try {
                Thread.sleep(1000); //milliseconds
            } catch (InterruptedException e) {
                break;
            }
        }
//            }
//        }, 0, RUNNABLE_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }

    /**
     * Store incoming BFT JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeBFTMessage(String jsonMsg) {
        Log.i(TAG, "storeBFTMessage jsonMsg: " + jsonMsg);

//        case JeroMQPublisher.TOPIC_PREFIX_BFT_SYNC:
//        databaseOperation.insertBFTIntoDatabase(sitRepRepo, sitRepModel);
//        break;
    }

    /**
     * Stores/updates/deletes/synchronises incoming WaveRelay Radio JSON messages
     * from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeRadioMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeRadioMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            WaveRelayRadioRepository waveRelayRadioRepository = new
                    WaveRelayRadioRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            WaveRelayRadioModel waveRelayRadioModel = gson.fromJson(jsonMsg,
                    WaveRelayRadioModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Log.i(TAG, "storeRadioMessage sync");
                    handleRadioDataSync(databaseOperation, waveRelayRadioRepository,
                            waveRelayRadioModel);
                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Log.i(TAG, "storeRadioMessage insert");
                    databaseOperation.insertRadioIntoDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Log.i(TAG, "storeRadioMessage update");
                    databaseOperation.updateRadioInDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Log.i(TAG, "storeRadioMessage delete");
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
            public void onSubscribe(Disposable d) {}

            @Override
            public void onSuccess(WaveRelayRadioModel matchedWaveRelayRadioModel) {
                if (matchedWaveRelayRadioModel == null) {
                    Log.d(TAG, "Inserting new WaveRelay Radio model from synchronisation...");
                    databaseOperation.insertRadioIntoDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                } else {
                    Log.d(TAG, "Updating WaveRelay Radio model from synchronisation...");
                    databaseOperation.updateRadioInDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverSyncRadio, handleRadioDataSync. " +
                        "Error Msg: " + e.toString());
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
    private void storeUserMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeUserMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            UserModel userModel = gson.fromJson(jsonMsg, UserModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Log.i(TAG, "storeUserMessage sync");
                    handleUserDataSync(databaseOperation, userRepo, userModel);
                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Log.i(TAG, "storeUserMessage insert");
                    databaseOperation.insertUserIntoDatabase(userRepo, userModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Log.i(TAG, "storeUserMessage update");
                    databaseOperation.updateUserInDatabase(userRepo, userModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Log.i(TAG, "storeUserMessage delete");
                    databaseOperation.deleteUserInDatabase(userRepo, userModel.getUserId());
                    break;
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
            public void onSubscribe(Disposable d) {}

            @Override
            public void onSuccess(UserModel matchedUserModel) {
                if (matchedUserModel == null) {
                    Log.d(TAG, "Inserting new User model from synchronisation...");
                    databaseOperation.insertUserIntoDatabase(userRepo, userModel);
                } else {
                    Log.d(TAG, "Updating User model from synchronisation...");
                    databaseOperation.updateUserInDatabase(userRepo, userModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverSyncUser, handleUserDataSync. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.queryUserByUserIdInDatabase(userRepo, userModel.getUserId(), singleObserverSyncUser);
    }

    /**
     * Stores/updates/deletes/synchronises incoming Sit Rep JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeSitRepMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeSitRepMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Log.i(TAG, "storeSitRepMessage sync");
                    handleSitRepDataSync(databaseOperation, sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Log.i(TAG, "storeSitRepMessage insert");
                    databaseOperation.insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Log.i(TAG, "storeSitRepMessage update");
                    databaseOperation.updateSitRepInDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Log.i(TAG, "storeSitRepMessage delete");
                    databaseOperation.deleteSitRepInDatabase(sitRepRepo, sitRepModel.getRefId());
                    break;
            }
        }
    }

    /**
     * Handles incoming Sit Rep model for Synchronisation
     *
     * @param databaseOperation
     * @param sitRepRepo
     * @param sitRepModel
     */
    private void handleSitRepDataSync(DatabaseOperation databaseOperation, SitRepRepository sitRepRepo,
                                      SitRepModel sitRepModel) {
        /**
         * Query for Sit Rep (SR) model from database with id of received SR model.
         *
         * If model exists in database, result will return a valid SR model
         * Else, result will return NULL.
         *
         * If result returns a valid SR model, update returned SR model with received SR model
         * Else, insert received SR model into database.
         */
        SingleObserver<SitRepModel> singleObserverSyncSitRep = new SingleObserver<SitRepModel>() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onSuccess(SitRepModel matchedSitRepModel) {
                if (matchedSitRepModel == null) {
                    Log.d(TAG, "Inserting new Sit Rep model from synchronisation...");
                    databaseOperation.insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                } else {
                    Log.d(TAG, "Updating Sit Rep model from synchronisation...");
                    databaseOperation.updateSitRepInDatabase(sitRepRepo, sitRepModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverSyncSitRep, syncSitRepInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.querySitRepByRefIdInDatabase(sitRepRepo, sitRepModel.getId(), singleObserverSyncSitRep);
    }

    /**
     * Stores/updates/deletes/synchronises incoming Task JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeTaskMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeTaskMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            TaskModel taskModel = gson.fromJson(jsonMsg, TaskModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Log.i(TAG, "storeTaskMessage sync");
                    handleTaskRepDataSync(databaseOperation, taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_INSERT:
                    Log.i(TAG, "storeTaskMessage insert");
                    databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Log.i(TAG, "storeTaskMessage update");
                    databaseOperation.updateTaskInDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    Log.i(TAG, "storeTaskMessage delete");
                    databaseOperation.deleteTaskInDatabase(taskRepo, taskModel.getRefId());
                    break;
            }
        }
    }

    /**
     * Handles incoming Task model for Synchronisation
     *
     * @param databaseOperation
     * @param taskRepo
     * @param taskModel
     */
    private void handleTaskRepDataSync(DatabaseOperation databaseOperation, TaskRepository taskRepo,
                                       TaskModel taskModel) {
        /**
         * Query for Task model from database with id of received Task model.
         *
         * If model exists in database, result will return a valid Task model
         * Else, result will return NULL.
         *
         * If result returns a valid SR model, update returned SR model with received SR model
         * Else, insert received SR model into database.
         */
        SingleObserver<TaskModel> singleObserverSyncTask = new SingleObserver<TaskModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(TaskModel matchedTaskModel) {
                if (matchedTaskModel == null) {
                    Log.d(TAG, "Inserting new Task model from synchronisation...");
                    databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
                } else {
                    Log.d(TAG, "Updating Task model from synchronisation...");
                    databaseOperation.updateTaskInDatabase(taskRepo, taskModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverSyncTask, syncTaskInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.queryTaskByRefIdInDatabase(taskRepo, taskModel.getId(), singleObserverSyncTask);
    }

    /**
     * Subscribe socket to all relevant topics
     *
     * @param socket
     */
    private void subscribeTopics(ZMQ.Socket socket) {
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_BFT.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_USER.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_RADIO.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_SITREP.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_TASK.getBytes());
    }
}
