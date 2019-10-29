package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio.WaveRelayRadioSocketClient;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class JeroMQSubscriberRunnable implements Runnable {

    private static final String TAG = JeroMQSubscriberRunnable.class.getSimpleName();

    //    private List<Socket> mSocketList;
    // <Identity ID, HeartbeartCount>
    private Map<Integer, Integer> mSocketMissingHeartbeatMap;
    private Map<Integer, String> mSocketReceivedHeartbeatTimeMap;
    private ZContext mZContext;
    private ZMQ.Poller mPoller;
    private boolean mIsPollerToBeClosed;
    private CloseSocketsListener mCloseSocketsListener;
//    private int mSocketIdentityCount;

    protected JeroMQSubscriberRunnable(ZContext zContext, List<Socket> socketList,
                                       ZMQ.Poller poller,
                                       CloseSocketsListener closeSocketsListener) {
        mZContext = zContext;
        mZContext.setLinger(0);
        mZContext.setRcvHWM(0);
        mZContext.setSndHWM(0);

//        mSocketList = socketList;
        mPoller = poller;
        mCloseSocketsListener = closeSocketsListener;

        mSocketMissingHeartbeatMap = new HashMap<>();
        mSocketReceivedHeartbeatTimeMap = new HashMap<>();
//        mSocketIdentityCount = mPoller.getSize();

        for (int i = 0; i < mPoller.getSize(); i++) {
            BigInteger identity = new BigInteger(mPoller.getSocket(i).getIdentity());
            Log.i(TAG, "identity to be inserted: " + identity.intValue());

            mSocketMissingHeartbeatMap.put(identity.intValue(), 0);
            mSocketReceivedHeartbeatTimeMap.put(identity.intValue(), DateTimeUtil.getCurrentDateTime());
        }
    }

    @Override
    public void run() {
        subscribeTopics(mPoller);

        while (!Thread.currentThread().isInterrupted()) {

            if (mIsPollerToBeClosed && mPoller != null) {
                mPoller.close();

//                if (mCloseSocketsListener != null) {
//                    mCloseSocketsListener.closeSockets();
//                }

                closeSockets(mPoller);
                break;
            }

            if (mPoller != null) {

                mPoller.poll(JeroMQParent.POLL_TIMEOUT_IN_MILLISEC);

                for (int i = 0; i < mPoller.getSize(); i++) {

//                    if (mPoller.pollin(i)) {
//                        storeMessageInDatabase(mPoller.getItem(i).getSocket());
//    //                    Timber.i("Received message: %s", message);
//                    }

                    Socket currentSocket = mPoller.getSocket(i);
                    BigInteger identity = new BigInteger(currentSocket.getIdentity());

                    String lastEndpoint = currentSocket.getLastEndpoint();
                    int socketIdentityCount = Integer.valueOf(lastEndpoint.
                            substring(lastEndpoint.lastIndexOf(StringUtil.DOT) + 1,
                                    lastEndpoint.lastIndexOf(StringUtil.COLON)));

//                    String lastEndpointIpAddress = lastEndpoint.substring(lastEndpoint.
//                                    indexOf(StringUtil.TRAILING_SLASH) + 2,
//                            lastEndpoint.lastIndexOf(StringUtil.COLON));

//                    Log.i(TAG, "lastEndpointIpAddress: " + lastEndpointIpAddress);

                    if (mPoller.pollin(i)) {
                        storeMessageInDatabase(currentSocket);
//                        updateWaveRelayDatabaseOfOtherUserId(lastEndpointIpAddress, true);

                        Log.i(TAG, "identity to be reset: " + identity.intValue());
                        mSocketMissingHeartbeatMap.put(identity.intValue(), 0);
                        mSocketReceivedHeartbeatTimeMap.put(identity.intValue(), DateTimeUtil.getCurrentDateTime());

                    } else {    // If socket is unreadable, the following sequence will take place:
                        // 1) Check if last heartbeat time with added interval (2 seconds) exceeds current time
                        // 2) If it does not exceed, ignore. Else, do the following:
                        //      a) Check if number of missing heartbeat for socket exceeds threshold
                        //      b) If it does not exceed, increment corresponding missing heartbeat by 1.
                        //         And set last heartbeat time to current time.
                        //         Else, close current socket, recreate and connect a new one. Unregister the old one
                        //         from the poller and register the new one. Reset missing heartbeat and last heartbeat
                        //         time.

//                        String heartBeatIntervalCurrentTime = DateTimeUtil.
//                                addMilliSecondsToZonedDateTime(mSocketReceivedHeartbeatTimeMap.get(i),
//                                        JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                        String heartBeatIntervalCurrentTime = DateTimeUtil.
                                addMilliSecondsToZonedDateTime(mSocketReceivedHeartbeatTimeMap.get(identity.intValue()),
                                        JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);

                        String currentTime = DateTimeUtil.getCurrentDateTime();
                        if (currentTime.
                                compareTo(heartBeatIntervalCurrentTime) > 0) {

//                            int currentSocketMissingHeartbeat = mSocketMissingHeartbeatMap.get(i);
                            int currentSocketMissingHeartbeat = mSocketMissingHeartbeatMap.get(identity.intValue());


                            if (currentSocketMissingHeartbeat >= JeroMQParent.
                                    MISSING_ZERO_MQ_HEARTBEAT_CONNECTION_THRESHOLD) {

                                Timber.i("Disconnecting from endpoint: %s...", lastEndpoint);

                                // Unregister and disconnect/destroy current socket
                                mPoller.unregister(currentSocket);
//                                currentSocket.disconnect(lastEndpoint);
//                                currentSocket.close();
                                mZContext.destroySocket(currentSocket);

//                                updateWaveRelayDatabaseOfOtherUserId(lastEndpointIpAddress, false);

                                Timber.i("Reconnecting with endpoint: %s...", lastEndpoint);

                                // Create new socket
                                Socket socket = mZContext.createSocket(SocketType.SUB);

                                BigInteger newIdentity = BigInteger.valueOf(socketIdentityCount);
                                socket.setIdentity(newIdentity.toByteArray());
//                                mSocketIdentityCount++;

                                socket.setMaxMsgSize(-1);
                                socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                                socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                                socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                                socket.setLinger(0);
                                socket.setRcvHWM(0);
                                socket.setSndHWM(0);
                                socket.setImmediate(true);
                                socket.setTCPKeepAlive(1);
                                socket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                                socket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                                socket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                                socket.setSendTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);
                                socket.setReceiveTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);
                                socket.connect(lastEndpoint);

                                subscribeTopicsForSocket(socket);

                                // Set socket list with newly created one
//                            mSocketList.set(i, socket);
                                mPoller.register(socket, ZMQ.Poller.POLLIN);

                                // Reset socket heartbeat to 0
//                                mSocketMissingHeartbeatMap.set(i, 0);
                                Log.i(TAG, "newIdentity to be reset: " + newIdentity.intValue());
                                mSocketMissingHeartbeatMap.put(newIdentity.intValue(), 0);
                                mSocketReceivedHeartbeatTimeMap.put(newIdentity.intValue(), DateTimeUtil.getCurrentDateTime());

                            } else {

                                currentSocketMissingHeartbeat++;
                                Timber.i("Current missing heartbeat of %s: %s...",
                                        lastEndpoint, currentSocketMissingHeartbeat);

//                                mSocketMissingHeartbeatMap.set(i, currentSocketMissingHeartbeat);
                                Log.i(TAG, "identity to be incremented: " + identity.intValue());
                                mSocketMissingHeartbeatMap.put(identity.intValue(), currentSocketMissingHeartbeat);
                                mSocketReceivedHeartbeatTimeMap.put(identity.intValue(), DateTimeUtil.getCurrentDateTime());
                            }


                            Timber.i("Socket Endpoint (%s)'s missing heartbeat: %d",
                                    lastEndpoint, currentSocketMissingHeartbeat);
                        } else {
//                            Timber.i("currentTime: %s", currentTime);
//                            Timber.i("heartBeatIntervalCurrentTime: %s", heartBeatIntervalCurrentTime);
                        }
                    }
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

            socket.setMaxMsgSize(-1);
            socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
            socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
            socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
            socket.setLinger(0);
            socket.setRcvHWM(0);
            socket.setSndHWM(0);
            socket.setImmediate(true);
            socket.setTCPKeepAlive(1);
            socket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
            socket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
            socket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
            socket.setSendTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);
            socket.setReceiveTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);

            message = socket.recvStr();

            if (message != null) {
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
            BFTRepository bftRepo = new BFTRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            BFTModel bftModel = gson.fromJson(jsonMsg, BFTModel.class);

            if (!SharedPreferenceUtil.getCurrentUserCallsignID().equalsIgnoreCase(bftModel.getUserId())) {
                switch (messageTopicAction) {
                    case JeroMQPublisher.TOPIC_INSERT:
                        Timber.i("storeBftMessage insert");

                        DatabaseOperation.getInstance().insertBftIntoDatabase(bftRepo, bftModel);
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
            WaveRelayRadioRepository waveRelayRadioRepository = new
                    WaveRelayRadioRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            WaveRelayRadioModel waveRelayRadioModel = gson.fromJson(jsonMsg,
                    WaveRelayRadioModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeRadioMessage insert");
                    DatabaseOperation.getInstance().insertRadioIntoDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeRadioMessage update");

                    if (waveRelayRadioModel.getUserId() != null &&
                            !SharedPreferenceUtil.getCurrentUserCallsignID().
                                    equalsIgnoreCase(waveRelayRadioModel.getUserId())) {
                        DatabaseOperation.getInstance().updateRadioInDatabase(waveRelayRadioRepository,
                                waveRelayRadioModel);
                    }

                    break;

                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeRadioMessage delete");
                    DatabaseOperation.getInstance().deleteRadioInDatabase(waveRelayRadioRepository,
                            waveRelayRadioModel.getRadioId());
                    break;
            }
        }
    }

    /**
     * Handles incoming WaveRelayRadioModel model for Synchronisation
     *
     * @param waveRelayRadioRepository
     * @param waveRelayRadioModel
     */
    private void handleRadioDataSync(WaveRelayRadioRepository waveRelayRadioRepository,
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

                    DatabaseOperation.getInstance().insertRadioIntoDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                } else {
                    Timber.i("Updating WaveRelay Radio model from synchronisation...");
                    DatabaseOperation.getInstance().updateRadioInDatabase(waveRelayRadioRepository, waveRelayRadioModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverSyncRadio, handleRadioDataSync.Error Msg: %s", e.toString());

            }
        };

        DatabaseOperation.getInstance().queryRadioByRadioIdInDatabase(waveRelayRadioRepository,
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
            UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            UserModel userModel = gson.fromJson(jsonMsg, UserModel.class);

            if (!SharedPreferenceUtil.getCurrentUserCallsignID().equalsIgnoreCase(userModel.getUserId())) {
                switch (messageTopicAction) {
                    case JeroMQPublisher.TOPIC_INSERT:
                        Timber.i("storeUserMessage insert");

                        DatabaseOperation.getInstance().insertUserIntoDatabase(userRepo, userModel);
                        break;

                    case JeroMQPublisher.TOPIC_UPDATE:
                        Timber.i("storeUserMessage update");

                        DatabaseOperation.getInstance().updateUserInDatabase(userRepo, userModel);
                        break;

                    case JeroMQPublisher.TOPIC_DELETE:
                        Timber.i("storeUserMessage delete");

                        DatabaseOperation.getInstance().deleteUserInDatabase(userRepo, userModel.getUserId());
                        break;
                }
            }
        }
    }

    /**
     * Handles incoming User model for Synchronisation
     *
     * @param userRepo
     * @param userModel
     */
    private void handleUserDataSync(UserRepository userRepo,
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
                    DatabaseOperation.getInstance().insertUserIntoDatabase(userRepo, userModel);
                } else {
                    Timber.i("Updating User model from synchronisation...");
                    DatabaseOperation.getInstance().updateUserInDatabase(userRepo, userModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverSyncUser, handleUserDataSync. Error Msg: %s", e.toString());


            }
        };

        DatabaseOperation.getInstance().queryUserByUserIdInDatabase(userRepo, userModel.getUserId(), singleObserverSyncUser);
    }

    /**
     * Stores/updates/deletes/synchronises incoming Sit Rep JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeSitRepMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeSitRepMessage jsonMsg:%s ", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Timber.i("storeSitRepMessage sync");

                    handleSitRepDataSync(sitRepRepo, sitRepModel);
                    break;

                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeSitRepMessage insert");

                    DatabaseOperation.getInstance().queryAndInsertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                    break;

                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeSitRepMessage update");

                    DatabaseOperation.getInstance().updateSitRepInDatabase(sitRepRepo, sitRepModel);
                    break;

                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeSitRepMessage delete");

                    DatabaseOperation.getInstance().deleteSitRepInDatabase(sitRepRepo, sitRepModel.getId());
                    break;
            }
        }
    }

    /**
     * Create new Sit Rep model and insert into database
     *
     * @param sitRepRepo
     * @param sitRepModel
     * @return
     */
    private void insertNewSitRepModelForSync(SitRepRepository sitRepRepo,
                                             SitRepModel sitRepModel) {

        SitRepModel newSitRepModel = new SitRepModel();
        newSitRepModel.setRefId(sitRepModel.getRefId());
        newSitRepModel.setReporter(sitRepModel.getReporter());
        newSitRepModel.setSnappedPhoto(sitRepModel.getSnappedPhoto());
        newSitRepModel.setReportType(sitRepModel.getReportType());

        newSitRepModel.setLocation(sitRepModel.getLocation());
        newSitRepModel.setActivity(sitRepModel.getActivity());
        newSitRepModel.setPersonnelT(sitRepModel.getPersonnelT());
        newSitRepModel.setPersonnelS(sitRepModel.getPersonnelS());
        newSitRepModel.setPersonnelD(sitRepModel.getPersonnelD());
        newSitRepModel.setNextCoa(sitRepModel.getNextCoa());
        newSitRepModel.setRequest(sitRepModel.getRequest());
        newSitRepModel.setOthers(sitRepModel.getOthers());

        newSitRepModel.setVesselType(sitRepModel.getVesselType());
        newSitRepModel.setVesselName(sitRepModel.getVesselName());
        newSitRepModel.setLpoc(sitRepModel.getLpoc());
        newSitRepModel.setNpoc(sitRepModel.getNpoc());
        newSitRepModel.setLastVisitToSg(sitRepModel.getLastVisitToSg());
        newSitRepModel.setVesselLastBoarded(sitRepModel.getVesselLastBoarded());
        newSitRepModel.setCargo(sitRepModel.getCargo());
        newSitRepModel.setPurposeOfCall(sitRepModel.getPurposeOfCall());
        newSitRepModel.setDuration(sitRepModel.getDuration());
        newSitRepModel.setCurrentCrew(sitRepModel.getCurrentCrew());
        newSitRepModel.setCurrentMaster(sitRepModel.getCurrentMaster());
        newSitRepModel.setCurrentCe(sitRepModel.getCurrentCe());
        newSitRepModel.setQueries(sitRepModel.getQueries());

        newSitRepModel.setCreatedDateTime(sitRepModel.getCreatedDateTime());
        newSitRepModel.setLastUpdatedDateTime(sitRepModel.getLastUpdatedDateTime());
        newSitRepModel.setIsValid(sitRepModel.getIsValid());

        DatabaseOperation.getInstance().queryAndInsertSitRepIntoDatabase(sitRepRepo, newSitRepModel);

    }

    /**
     * Compares current matched Sit Rep model with newly received Sit Rep model for data synchronisation
     *
     * @param currentMatchedSitRepModel
     * @param newSitRepModel
     * @return
     */
    private SitRepModel compareSitRepModelModelForSync(SitRepModel currentMatchedSitRepModel, SitRepModel newSitRepModel) {

        if (EIsValid.NO.toString().equalsIgnoreCase(newSitRepModel.getIsValid())) {
            currentMatchedSitRepModel.setIsValid(EIsValid.NO.toString());

        } else {

            // If existing model's lastUpdatedDateTime is less (outdated) than the new model's,
            // update current model's main data with new model's main data.
            if ((StringUtil.INVALID_STRING.equalsIgnoreCase(currentMatchedSitRepModel.
                    getLastUpdatedDateTime()) && !StringUtil.INVALID_STRING.equalsIgnoreCase(newSitRepModel.
                    getLastUpdatedDateTime())) ||
                    !StringUtil.INVALID_STRING.equalsIgnoreCase(newSitRepModel.
                            getLastUpdatedDateTime()) && currentMatchedSitRepModel.getLastUpdatedDateTime().
                            compareTo(newSitRepModel.getLastUpdatedDateTime()) <= -1) {

                currentMatchedSitRepModel.setReporter(newSitRepModel.getReporter());
                currentMatchedSitRepModel.setSnappedPhoto(newSitRepModel.getSnappedPhoto());
                currentMatchedSitRepModel.setReportType(newSitRepModel.getReportType());

                currentMatchedSitRepModel.setLocation(newSitRepModel.getLocation());
                currentMatchedSitRepModel.setActivity(newSitRepModel.getActivity());
                currentMatchedSitRepModel.setPersonnelT(newSitRepModel.getPersonnelT());
                currentMatchedSitRepModel.setPersonnelS(newSitRepModel.getPersonnelS());
                currentMatchedSitRepModel.setPersonnelD(newSitRepModel.getPersonnelD());
                currentMatchedSitRepModel.setNextCoa(newSitRepModel.getNextCoa());
                currentMatchedSitRepModel.setRequest(newSitRepModel.getRequest());
                currentMatchedSitRepModel.setOthers(newSitRepModel.getOthers());

                currentMatchedSitRepModel.setVesselType(newSitRepModel.getVesselType());
                currentMatchedSitRepModel.setVesselName(newSitRepModel.getVesselName());
                currentMatchedSitRepModel.setLpoc(newSitRepModel.getLpoc());
                currentMatchedSitRepModel.setNpoc(newSitRepModel.getNpoc());
                currentMatchedSitRepModel.setLastVisitToSg(newSitRepModel.getLastVisitToSg());
                currentMatchedSitRepModel.setVesselLastBoarded(newSitRepModel.getVesselLastBoarded());
                currentMatchedSitRepModel.setCargo(newSitRepModel.getCargo());
                currentMatchedSitRepModel.setPurposeOfCall(newSitRepModel.getPurposeOfCall());
                currentMatchedSitRepModel.setDuration(newSitRepModel.getDuration());
                currentMatchedSitRepModel.setCurrentCrew(newSitRepModel.getCurrentCrew());
                currentMatchedSitRepModel.setCurrentMaster(newSitRepModel.getCurrentMaster());
                currentMatchedSitRepModel.setCurrentCe(newSitRepModel.getCurrentCe());
                currentMatchedSitRepModel.setQueries(newSitRepModel.getQueries());

                currentMatchedSitRepModel.setLastUpdatedDateTime(newSitRepModel.getLastUpdatedDateTime());
                currentMatchedSitRepModel.setCreatedDateTime(newSitRepModel.getCreatedDateTime());
                currentMatchedSitRepModel.setIsValid(newSitRepModel.getIsValid());

            }
        }

        return currentMatchedSitRepModel;
    }

    /**
     * Handles incoming Sit Rep model for Synchronisation
     *
     * @param sitRepRepo
     * @param sitRepModel
     */
    private void handleSitRepDataSync(SitRepRepository sitRepRepo,
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
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(SitRepModel matchedSitRepModel) {
                if (matchedSitRepModel == null) {
                    Timber.i("Inserting new Sit Rep model from synchronisation...");

                    insertNewSitRepModelForSync(sitRepRepo, sitRepModel);

                } else {
                    Timber.i("Updating Sit Rep model from synchronisation...");

                    SitRepModel sitRepModelToSync = compareSitRepModelModelForSync(matchedSitRepModel, sitRepModel);
                    DatabaseOperation.getInstance().updateSitRepInDatabase(sitRepRepo, sitRepModelToSync);

                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.i("onError singleObserverSyncSitRep, syncSitRepInDatabase. Error Msg: %s", e.toString());
                Timber.i("Inserting new Sit Rep model from synchronisation...");

                insertNewSitRepModelForSync(sitRepRepo, sitRepModel);
            }
        };

        DatabaseOperation.getInstance().querySitRepByCreatedDateTimeInDatabase(sitRepRepo,
                sitRepModel.getCreatedDateTime(), singleObserverSyncSitRep);
    }

    /**
     * Stores/updates/deletes/synchronises incoming Task JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private synchronized void storeTaskMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeTaskMessage jsonMsg: %s", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            TaskModel taskModel = gson.fromJson(jsonMsg, TaskModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Timber.i("storeTaskMessage sync");

                    handleTaskDataSync(taskRepo, taskModel);
                    break;

                case JeroMQPublisher.TOPIC_INSERT:
                    Timber.i("storeTaskMessage insert");

                    DatabaseOperation.getInstance().queryAndInsertTaskIntoDatabase(taskRepo, taskModel);
                    break;

                case JeroMQPublisher.TOPIC_UPDATE:
                    Timber.i("storeTaskMessage update");

                    DatabaseOperation.getInstance().updateTaskInDatabase(taskRepo, taskModel);
                    break;

                case JeroMQPublisher.TOPIC_DELETE:
                    Timber.i("storeTaskMessage delete");

                    DatabaseOperation.getInstance().deleteTaskInDatabase(taskRepo, taskModel.getId());
                    break;
            }
        }
    }

    /**
     * Create new Task model and insert into database
     *
     * @param taskRepo
     * @param taskModel
     * @return
     */
    private void insertNewTaskModelForSync(TaskRepository taskRepo,
                                           TaskModel taskModel) {

        TaskModel newTaskModel = new TaskModel();
        newTaskModel.setRefId(taskModel.getId());
        newTaskModel.setPhaseNo(taskModel.getPhaseNo());
        newTaskModel.setAdHocTaskPriority(taskModel.getAdHocTaskPriority());
        newTaskModel.setAssignedTo(taskModel.getAssignedTo());
        newTaskModel.setAssignedBy(taskModel.getAssignedBy());
        newTaskModel.setTitle(taskModel.getTitle());
        newTaskModel.setDescription(taskModel.getDescription());
        newTaskModel.setStatus(taskModel.getStatus());
        newTaskModel.setCreatedDateTime(taskModel.getCreatedDateTime());
        newTaskModel.setCompletedDateTime(taskModel.getCompletedDateTime());
        newTaskModel.setLastUpdatedStatusDateTime(taskModel.getLastUpdatedStatusDateTime());
        newTaskModel.setLastUpdatedMainDateTime(taskModel.getLastUpdatedMainDateTime());
        newTaskModel.setIsValid(taskModel.getIsValid());

        DatabaseOperation.getInstance().queryAndInsertTaskIntoDatabase(taskRepo, newTaskModel);

    }

    /**
     * Compares current matched task model with newly received task model for data synchronisation
     *
     * @param currentMatchedTaskModel
     * @param newTaskModel
     * @return
     */
    private TaskModel compareTaskModelForSync(TaskModel currentMatchedTaskModel, TaskModel newTaskModel) {

        if (EIsValid.NO.toString().equalsIgnoreCase(newTaskModel.getIsValid())) {
            currentMatchedTaskModel.setIsValid(EIsValid.NO.toString());

        } else {

//            Timber.i("currentMatchedTaskModel.getLastUpdatedStatusDateTime(): %s",
//                    currentMatchedTaskModel.getLastUpdatedStatusDateTime());
//
//            Timber.i("newTaskModel.getLastUpdatedStatusDateTime(): %s",
//                    newTaskModel.getLastUpdatedStatusDateTime());
//
//            Timber.i("currentMatchedTaskModel.getLastUpdatedMainDateTime(): %s",
//                    currentMatchedTaskModel.getLastUpdatedMainDateTime());
//
//            Timber.i("newTaskModel.getLastUpdatedMainDateTime(): %s",
//                    newTaskModel.getLastUpdatedMainDateTime());

            // If existing model's lastUpdatedMainDateTime is less (outdated) than the new model's,
            // update current model's main data with new model's main data.
            // Only CCT has the access control to change these information.
            // Main data includes the following:
            // 1) Phase No
            // 2) AdHocTaskPriority
            // 3) AssignedTo
            // 4) AssignedBy
            // 5) Title
            // 6) Description
            // 7) LastUpdatedMainDateTime
            if ((StringUtil.INVALID_STRING.equalsIgnoreCase(currentMatchedTaskModel.
                    getLastUpdatedMainDateTime()) && !StringUtil.INVALID_STRING.equalsIgnoreCase(newTaskModel.
                    getLastUpdatedMainDateTime())) ||
                    !StringUtil.INVALID_STRING.equalsIgnoreCase(newTaskModel.
                            getLastUpdatedMainDateTime()) && currentMatchedTaskModel.getLastUpdatedMainDateTime().
                            compareTo(newTaskModel.getLastUpdatedMainDateTime()) < 1) {

                currentMatchedTaskModel.setPhaseNo(newTaskModel.getPhaseNo());
                currentMatchedTaskModel.setAdHocTaskPriority(newTaskModel.getAdHocTaskPriority());
                currentMatchedTaskModel.setAssignedTo(newTaskModel.getAssignedTo());
                currentMatchedTaskModel.setAssignedBy(newTaskModel.getAssignedBy());
                currentMatchedTaskModel.setTitle(newTaskModel.getTitle());
                currentMatchedTaskModel.setDescription(newTaskModel.getDescription());
                currentMatchedTaskModel.setLastUpdatedMainDateTime(newTaskModel.getLastUpdatedMainDateTime());

                currentMatchedTaskModel.setStatus(newTaskModel.getStatus());
                currentMatchedTaskModel.setCompletedDateTime(newTaskModel.getCompletedDateTime());
                currentMatchedTaskModel.setLastUpdatedStatusDateTime(newTaskModel.getLastUpdatedStatusDateTime());

            } else {    // If task model has not been updated by CCT, get latest task status from Team Leads.
                // Number of assignees (Assign To personnel) will be the same, so there will be no issue in direct comparison
                // For e.g. Item 1 of currentMatchedTaskModel will ALWAYS correspond with item 1 of newTaskModel

                String[] currentModelStatusGroup = StringUtil.removeCommasAndExtraSpaces(currentMatchedTaskModel.getStatus());
                String[] currentModelCompletedDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(currentMatchedTaskModel.getCompletedDateTime());
                String[] currentModelLastUpdatedStatusDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(currentMatchedTaskModel.getLastUpdatedStatusDateTime());

                String[] newModelStatusGroup = StringUtil.removeCommasAndExtraSpaces(newTaskModel.getStatus());
                String[] newModelCompletedDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(newTaskModel.getCompletedDateTime());
                String[] newModelLastUpdatedStatusDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(newTaskModel.getLastUpdatedStatusDateTime());

//                // For all task models, there will at least be ONE lastUpdatedStatusDateTimeGroup
//                // Step 1: compare current and new model to find out the most recent update
//                // The model with the most recent update
//                if () {
//
//                }
//
//                if (currentModelLastUpdatedStatusDateTimeGroup.length < newModelLastUpdatedStatusDateTimeGroup.length) {
//
//
//                } else if (currentModelLastUpdatedStatusDateTimeGroup.length > newModelLastUpdatedStatusDateTimeGroup.length)  {
//
//                }
//

                for (int i = 0; i < currentModelLastUpdatedStatusDateTimeGroup.length; i++) {

                    // If existing model's lastUpdatedStatusDateTime is less (outdated) than the new model's,
                    // update current model's data with new model's data
                    if ((StringUtil.INVALID_STRING.equalsIgnoreCase(currentModelLastUpdatedStatusDateTimeGroup[i]) &&
                            !StringUtil.INVALID_STRING.equalsIgnoreCase(newModelLastUpdatedStatusDateTimeGroup[i]))
                            ||
                            !StringUtil.INVALID_STRING.equalsIgnoreCase(newModelLastUpdatedStatusDateTimeGroup[i]) &&
                                    currentModelLastUpdatedStatusDateTimeGroup[i].
                                            compareTo(newModelLastUpdatedStatusDateTimeGroup[i]) <= -1) {

                        currentModelStatusGroup[i] = newModelStatusGroup[i];
                        currentModelCompletedDateTimeGroup[i] = newModelCompletedDateTimeGroup[i];
                        currentModelLastUpdatedStatusDateTimeGroup[i] = newModelLastUpdatedStatusDateTimeGroup[i];

                    }
                }

                String status = String.join(StringUtil.COMMA, currentModelStatusGroup);
                String completedDateTime = String.join(StringUtil.COMMA, currentModelCompletedDateTimeGroup);
                String lastUpdatedStatusDateTime = String.join(StringUtil.COMMA, currentModelLastUpdatedStatusDateTimeGroup);

                currentMatchedTaskModel.setStatus(status);
                currentMatchedTaskModel.setCompletedDateTime(completedDateTime);
                currentMatchedTaskModel.setLastUpdatedStatusDateTime(lastUpdatedStatusDateTime);

            }
        }

        return currentMatchedTaskModel;
    }

    /**
     * Handles incoming Task model for Synchronisation
     *
     * @param taskRepo
     * @param taskModel
     */
    private void handleTaskDataSync(TaskRepository taskRepo,
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
                    Timber.i("Inserting new Task model from synchronisation...");

                    insertNewTaskModelForSync(taskRepo, taskModel);

                } else {
                    Timber.i("Updating Task model from synchronisation...");

                    TaskModel taskModelToSync = compareTaskModelForSync(matchedTaskModel, taskModel);
                    DatabaseOperation.getInstance().updateTaskInDatabase(taskRepo, taskModelToSync);

                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.i("onError singleObserverSyncTask, syncTaskInDatabase. Error Msg: %s", e.toString());
                Timber.i("Inserting new Task model from synchronisation...");

                insertNewTaskModelForSync(taskRepo, taskModel);
            }
        };

        DatabaseOperation.getInstance().queryTaskByCreatedDateTimeInDatabase(taskRepo,
                taskModel.getCreatedDateTime(), singleObserverSyncTask);
    }

    /**
     * Broadcasts wave relay radio detail of user to other devices
     */
    private synchronized void broadcastWaveRelayRadioDetailsOfUser(String userId, boolean isConnected) {
        WaveRelayRadioRepository waveRelayRadioRepository = new WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadio = new SingleObserver<WaveRelayRadioModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                if (waveRelayRadioModel != null) {
                    Log.d(TAG, "onSuccess singleObserverWaveRelayRadio, " +
                            "broadcastWaveRelayRadioDetailsOfUser. " +
                            "UserId: " + waveRelayRadioModel.getUserId());

                    if (!SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(waveRelayRadioModel.getUserId()) && !isConnected) {

                        waveRelayRadioModel.setUserId(null);
                        waveRelayRadioModel.setPhoneIpAddress(StringUtil.INVALID_STRING);
                        waveRelayRadioModel.setSignalToNoiseRatio(StringUtil.N_A);
                        waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
                    }

                    JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(waveRelayRadioModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverWaveRelayRadio, " +
                        "broadcastWaveRelayRadioDetailsOfUser. " +
                        "Error Msg: " + e.toString());
            }
        };

        waveRelayRadioRepository.queryRadioByUserId(userId, singleObserverWaveRelayRadio);
    }

    /**
     * Updates and broadcast radio connection status of specific user in local database
     */
    private synchronized void updateAndBroadcastUserRadioConnectionStatus(String userId, boolean isConnected) {
        UserRepository userRepository = new UserRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverUser, " +
                        "updateAndBroadcastUserRadioConnectionStatus. " +
                        "UserId: " + userModel.getUserId());

                boolean isSecondConnectionCheckMet;
                String currentUserId = userModel.getUserId();

                // Check missing heart beat connection of other devices
                if (userModel.getMissingHeartBeatCount() >=
                        WaveRelayRadioSocketClient.MISSING_HEARTBEAT_CONNECTION_THRESHOLD &&
                        !SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(currentUserId)) {

                    isSecondConnectionCheckMet = false;
                    broadcastWaveRelayRadioDetailsOfUser(currentUserId, false);

                } else {
                    isSecondConnectionCheckMet = true;

                }

                if (isConnected && isSecondConnectionCheckMet) {

                    // Update last connected time only if user was previously offline
                    if (!ERadioConnectionStatus.ONLINE.toString().
                            equalsIgnoreCase(userModel.getRadioFullConnectionStatus()) ||
                            StringUtil.INVALID_STRING.
                                    equalsIgnoreCase(userModel.getLastKnownConnectionDateTime())) {

                        userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
                    }

                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.CONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.ONLINE.toString());
                    userModel.setMissingHeartBeatCount(0);

                    // For own device, simply reset missing heart beat to 0
                    if (SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(currentUserId)) {

//                        userModel.setMissingHeartBeatCount(0);
                        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                                ERadioConnectionStatus.ONLINE.toString());

                    }

//                    else {    // For other devices, increment missing heart beat count by 1.
//                                // Their heart beat counts will be updated to 0 by their respective devices when
//                                // they broadcast their own connection with heartbeat as 0.
//
//                        if (userModel.getMissingHeartBeatCount() != Integer.valueOf(StringUtil.INVALID_STRING)) {
//                            userModel.setMissingHeartBeatCount(userModel.getMissingHeartBeatCount() + 1);
//                        } else {
//                            userModel.setMissingHeartBeatCount(0);
//                        }
//                    }

                } else {

                    // Update last connected time only if user was previously online
                    if (!ERadioConnectionStatus.OFFLINE.toString().
                            equalsIgnoreCase(userModel.getRadioFullConnectionStatus())) {
                        userModel.setLastKnownConnectionDateTime(DateTimeUtil.getCurrentDateTime());
                    }

                    userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                    userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                    userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

                    if (SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(currentUserId)) {

                        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                                ERadioConnectionStatus.OFFLINE.toString());
                    }
                }

                userRepository.updateUser(userModel);

                // Send updated User data to other connected devices
                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(userModel);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "updateAndBroadcastUserRadioConnectionStatus. " +
                        "Error Msg: " + e.toString());
            }
        };

        userRepository.queryUserByUserId(userId, singleObserverUser);
    }

    /**
     * Updates Wave Relay database table of connected user and remove User Id those who are not connected
     * excluding current user (as this method is called with neighbours' IP addresses as input argument).
     * Radio IP address list contains those who are connected to the network
     */
    private synchronized void updateWaveRelayDatabaseOfOtherUserId(String ipAddress, boolean isConnected) {
        WaveRelayRadioRepository waveRelayRadioRepository = new
                WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadio = new
                SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
                        Timber.i("onSuccess singleObserverAllWaveRelayRadio, updateWaveRelayDatabaseOfOtherUserIds. waveRelayRadioModel.size(): %d", waveRelayRadioModelList.size());

                        // Obtain specific Wave Relay model from database
                        List<WaveRelayRadioModel> waveRelaySpecificRadioModelList = waveRelayRadioModelList.stream().
                                filter(waveRelayRadioModel -> ipAddress.equalsIgnoreCase(
                                        waveRelayRadioModel.getPhoneIpAddress())).collect(Collectors.toList());

                        if (waveRelaySpecificRadioModelList.size() == 1) {

                            WaveRelayRadioModel waveRelayRadioModel = waveRelaySpecificRadioModelList.get(0);

                            if (isConnected) {
                                String userId = waveRelayRadioModel.getUserId();
                                if (userId != null) {
                                    updateAndBroadcastUserRadioConnectionStatus(userId, true);
//                                updateVideoStreamUrlOfUser(String.valueOf(waveRelayRadioModel.getRadioId()),
//                                        waveRelayRadioModel.getRadioIpAddress());
                                }
                            } else {
                                // Set user connection to offline in User Table, if user was previously online
                                String userId = waveRelayRadioModel.getUserId();
                                if (userId != null && !userId.equalsIgnoreCase(SharedPreferenceUtil.
                                        getCurrentUserCallsignID())) {

                                    updateAndBroadcastUserRadioConnectionStatus(userId, false);
                                    waveRelayRadioModel.setUserId(null);
                                    waveRelayRadioRepository.updateWaveRelayRadio(waveRelayRadioModel);
                                }
                            }
//                            queryAndUpdateUserOfRadioIPAddress(waveRelayRadioModel.getRadioIpAddress(),
//                                    false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverAllWaveRelayRadio, " +
                                "updateWaveRelayDatabaseOfOtherUserIds. " +
                                "Error Msg: " + e.toString());
                    }
                };

        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverAllWaveRelayRadio);
    }

    /**
     * Subscribe socket list from poller to all relevant topics
     *
     * @param poller
     */
    private void subscribeTopics(ZMQ.Poller poller) {
        for (int i = 0; i < poller.getSize(); i++) {
            Socket socket = poller.getItem(i).getSocket();
            subscribeTopicsForSocket(socket);
        }
    }

    /**
     * Subscribe socket to all relevant topics
     *
     * @param socket
     */
    private void subscribeTopicsForSocket(Socket socket) {
//        socket.subscribe("".getBytes());
        socket.subscribe(JeroMQParent.TOPIC_PREFIX_USER.getBytes());
        socket.subscribe(JeroMQParent.TOPIC_PREFIX_RADIO.getBytes());
        socket.subscribe(JeroMQParent.TOPIC_PREFIX_BFT.getBytes());
        socket.subscribe(JeroMQParent.TOPIC_PREFIX_SITREP.getBytes());
        socket.subscribe(JeroMQParent.TOPIC_PREFIX_TASK.getBytes());
    }

    private void closeSockets(ZMQ.Poller poller) {
        if (mZContext != null) {
//            for (int i = 0; i < poller.getSize(); i++) {
//                Socket socket = poller.getItem(i).getSocket();
//
////                if (mClientSubEndpointList != null &&
////                        mClientSubEndpointList.get(i) != null) {
//                socket.disconnect(poller.getItem(i).getSocket().getLastEndpoint());
//                Timber.i("Client SUB socket disconnected %d", i);
//
//                socket.close();
//                Timber.i("Client SUB socket closed %d", i);
////                }
//
//                mZContext.destroySocket(socket);
//                Timber.i("Client SUB socket destroyed %d", i);
//            }
        }

        if (mZContext != null && !mZContext.isClosed()) {
            Timber.i("Destroying ZContext.");

            mZContext.destroy();

            Timber.i("ZContext destroyed.");
        }
    }

    public interface CloseSocketsListener {
        void closeSockets();
    }
}
