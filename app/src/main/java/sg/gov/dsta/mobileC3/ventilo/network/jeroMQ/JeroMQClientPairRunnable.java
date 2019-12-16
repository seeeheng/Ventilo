package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DatabaseOperation;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import timber.log.Timber;

public class JeroMQClientPairRunnable implements Runnable {

    private static final String TAG = JeroMQClientPairRunnable.class.getSimpleName();

    public static final String DATA_SYNC_SUCCESSFUL_INTENT_ACTION = "Data Sync Successful";
    public static final String DATA_SYNC_FAILED_INTENT_ACTION = "Data Sync Failed";

    private List<Integer> mSocketMissingHeartbeatList;
    private List<String> mSocketReceivedHeartbeatTimeList;
    private ZContext mZContext;
    private ZMQ.Poller mPoller;
    private boolean mIsPollerToBeClosed;
    private CloseSocketsListener mCloseSocketsListener;

    protected JeroMQClientPairRunnable(ZContext zContext, ZMQ.Poller poller,
                                       CloseSocketsListener closeSocketsListener) {
        mZContext = zContext;
        mZContext.setLinger(0);
        mZContext.setRcvHWM(0);
        mZContext.setSndHWM(0);

        mPoller = poller;
        mCloseSocketsListener = closeSocketsListener;

        mSocketMissingHeartbeatList = new ArrayList<>();
        mSocketReceivedHeartbeatTimeList = new ArrayList<>();

        for (int i = 0; i < mPoller.getSize(); i++) {
            mSocketMissingHeartbeatList.add(0);
            mSocketReceivedHeartbeatTimeList.add(DateTimeUtil.getCurrentDateTime());
        }
    }

    @Override
    public void run() {
//        subscribeTopics(mPoller);

        Timber.i("JeroMQClientPairRunnable running in progress..");
//        subscribeTopics(mSocketList);

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
//
//                    if (mPoller.pollin(i)) {
//                        storeMessageInDatabase(mPoller.getItem(i).getSocket());
//                    }

//                    Timber.i("Looking into poller item %d...", i);

                    Socket currentSocket = mPoller.getSocket(i);

                    if (mPoller.pollin(i)) {
                        storeMessageInDatabase(currentSocket);
                        mSocketMissingHeartbeatList.set(i, 0);
                        mSocketReceivedHeartbeatTimeList.set(i, DateTimeUtil.getCurrentDateTime());

                    } else {    // If socket is unreadable, the following sequence will take place:
                        // 1) Check if last heartbeat time with added interval (2 seconds) exceeds current time
                        // 2) If it does not exceed, ignore. Else, do the following:
                        //      a) Check if number of missing heartbeat for socket exceeds threshold
                        //      b) If it does not exceed, increment corresponding missing heartbeat by 1.
                        //         And set last heartbeat time to current time.
                        //         Else, close current socket, recreate and connect a new one. Unregister the old one
                        //         from the poller and register the new one. Reset missing heartbeat and last heartbeat
                        //         time.

                        if (currentSocket != null) {

                            String heartBeatIntervalCurrentTime = DateTimeUtil.
                                    addMilliSecondsToZonedDateTime(mSocketReceivedHeartbeatTimeList.get(i),
                                            JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);

//                            Timber.i("DateTimeUtil.getCurrentDateTime(): %s...", DateTimeUtil.getCurrentDateTime());
//                            Timber.i("heartBeatIntervalCurrentTime: %s...", heartBeatIntervalCurrentTime);
//                            Timber.i("DateTimeUtil.getCurrentDateTime().compareTo(heartBeatIntervalCurrentTime): %s...",
//                                    DateTimeUtil.getCurrentDateTime().
//                                            compareTo(heartBeatIntervalCurrentTime));

                            int currentSocketMissingHeartbeat = mSocketMissingHeartbeatList.get(i);
                            String lastEndpoint = currentSocket.getLastEndpoint();

                            if (DateTimeUtil.getCurrentDateTime().
                                    compareTo(heartBeatIntervalCurrentTime) > 0) {

                                if (currentSocketMissingHeartbeat >= JeroMQParent.
                                        MISSING_ZERO_MQ_HEARTBEAT_CONNECTION_THRESHOLD) {

                                    Timber.i("Disconnecting from endpoint: %s...", lastEndpoint);

                                    // Unregister and disconnect/destroy current socket
                                    mPoller.unregister(currentSocket);
                                    mZContext.destroySocket(currentSocket);

                                    Timber.i("Reconnecting with endpoint: %s...", lastEndpoint);

                                    // Create new socket
                                    Socket socket = mZContext.createSocket(SocketType.SUB);
                                    socket.setMaxMsgSize(-1);
                                    socket.setMsgAllocationHeapThreshold(0);

                                    socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
                                    socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
                                    socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
                                    socket.setLinger(0);
                                    socket.setRcvHWM(0);
                                    socket.setSndHWM(0);
                                    socket.setImmediate(true);
                                    socket.setTCPKeepAlive(0);
                                    socket.setTCPKeepAliveCount(JeroMQParent.TCP_KEEP_ALIVE_COUNT);
                                    socket.setTCPKeepAliveIdle(JeroMQParent.TCP_KEEP_ALIVE_IDLE_IN_MILLISEC);
                                    socket.setTCPKeepAliveInterval(JeroMQParent.TCP_KEEP_ALIVE_INTERVAL_IN_MILLISEC);
                                    socket.setSendTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);
                                    socket.setReceiveTimeOut(JeroMQParent.SOCKET_TIMEOUT_IN_MILLISEC);
                                    socket.connect(lastEndpoint);

//                                subscribeTopicsForSocket(socket);

                                    // Set socket list with newly created one
//                            mSocketList.set(i, socket);
                                    mPoller.register(socket, ZMQ.Poller.POLLIN);

                                    // Reset socket heartbeat to 0
                                    mSocketMissingHeartbeatList.set(i, 0);

                                } else {

                                    currentSocketMissingHeartbeat++;
                                    Timber.i("Current missing heartbeat of %s: %s...",
                                            lastEndpoint, currentSocketMissingHeartbeat);

                                    mSocketMissingHeartbeatList.set(i, currentSocketMissingHeartbeat);

                                }

                                mSocketReceivedHeartbeatTimeList.set(i, DateTimeUtil.getCurrentDateTime());

                                Timber.i("Socket Endpoint (%s)'s missing heartbeat: %d",
                                        lastEndpoint, currentSocketMissingHeartbeat);
                            }

                        }

//                        else {
//                            Timber.i("currentSocket is null");
//
//                        }
                    }
                }
            }
        }

        Timber.i("Req poller closed.");
    }

    protected void closePoller() {
        mIsPollerToBeClosed = true;
    }

    protected void addPollerItem(Socket socket) {
        if (mPoller != null) {
            mPoller.register(socket, ZMQ.Poller.POLLIN);
            mSocketMissingHeartbeatList.add(0);
            mSocketReceivedHeartbeatTimeList.add(DateTimeUtil.getCurrentDateTime());
        }
    }

    private synchronized void storeMessageInDatabase(Socket socket) {
        Intent broadcastIntent = new Intent();
        String message = "";

        try {

            socket.setMaxMsgSize(-1);
            socket.setMsgAllocationHeapThreshold(0);

            socket.setHeartbeatIvl(JeroMQParent.HEARTBEAT_INTERVAL_IN_MILLISEC);
            socket.setHeartbeatTimeout(JeroMQParent.HEARTBEAT_TIMEOUT_IN_MILLISEC);
            socket.setHeartbeatTtl(JeroMQParent.HEARTBEAT_TTL_IN_MILLISEC);
            socket.setLinger(0);
            socket.setRcvHWM(0);
            socket.setSndHWM(0);
            socket.setImmediate(true);
            socket.setTCPKeepAlive(0);
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

                // For e.g. TOPIC-USER, TOPIC-RADIO, TOPIC-BFT, TOPIC-SITREP, TOPIC-TASK
                String messageMainTopic = messageTopicParts[0].
                        concat(StringUtil.HYPHEN).concat(messageTopicParts[1]);

                String messageTopicAction = "";

                // For e.g. SYNC
                messageTopicAction = messageTopicParts[2];

                switch (messageMainTopic) {
                    case JeroMQPublisher.TOPIC_PREFIX_SITREP:
                        storeSitRepMessage(messageContent, messageTopicAction);
                        break;
                    case JeroMQPublisher.TOPIC_PREFIX_TASK:
                        storeTaskMessage(messageContent, messageTopicAction);
                        break;
                    default:
                        break;
                }

                broadcastIntent.setAction(DATA_SYNC_SUCCESSFUL_INTENT_ACTION);
                LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);

                try {
                    Thread.sleep(CustomThreadPoolManager.THREAD_SLEEP_DURATION_NONE); // milliseconds
                } catch (InterruptedException e) {
                    Timber.i("Client pair socket thread interrupted.");
                }
            }

        } catch (ZMQException e) {
            Timber.i("Socket exception - %s", e);
//            Timber.i("Closing client pair socket..");
//
//            socket.close();
//
//            Timber.i("Client pair socket closed..");

            broadcastIntent.setAction(DATA_SYNC_FAILED_INTENT_ACTION);
            LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Synchronises incoming Sit Rep JSON messages from CCT into local database
     *
     * @param jsonMsg
     */
    private void storeSitRepMessage(String jsonMsg, String messageTopicAction) {
        Timber.i("storeSitRepMessage jsonMsg: %s ", jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Timber.i("storeSitRepMessage sync");

                    handleSitRepDataSync(sitRepRepo, sitRepModel);
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
     * Synchronises incoming Task JSON messages from CCT into local database
     *
     * @param jsonMsg
     */
    private void storeTaskMessage(String jsonMsg, String messageTopicAction) {
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
//                case JeroMQPublisher.TOPIC_INSERT:
//                    Timber.i("storeTaskMessage insert");
//
//                    databaseOperation.queryAndInsertTaskIntoDatabase(taskRepo, taskModel);
//                    break;
//                case JeroMQPublisher.TOPIC_UPDATE:
//                    Timber.i("storeTaskMessage update");
//
//                    databaseOperation.updateTaskInDatabase(taskRepo, taskModel);
//                    break;
//                case JeroMQPublisher.TOPIC_DELETE:
//                    Timber.i("storeTaskMessage delete");
//
//                    databaseOperation.deleteTaskInDatabase(taskRepo, taskModel.getId());
//                    break;
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
                            compareTo(newTaskModel.getLastUpdatedMainDateTime()) <= -1) {

                currentMatchedTaskModel.setPhaseNo(newTaskModel.getPhaseNo());
                currentMatchedTaskModel.setAdHocTaskPriority(newTaskModel.getAdHocTaskPriority());
                currentMatchedTaskModel.setAssignedTo(newTaskModel.getPhaseNo());
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

//    /**
//     * Subscribe socket list from poller to all relevant topics
//     *
//     * @param poller
//     */
//    private void subscribeTopics(ZMQ.Poller poller) {
//        for (int i = 0; i < poller.getSize(); i++) {
//            Socket socket = poller.getItem(i).getSocket();
//            subscribeTopicsForSocket(socket);
//        }
//    }
//
//    /**
//     * Subscribe socket to all relevant topics
//     *
//     * @param socket
//     */
//    private void subscribeTopicsForSocket(Socket socket) {
////        socket.subscribe("".getBytes());
//        socket.subscribe(JeroMQParent.TOPIC_PREFIX_SITREP.getBytes());
//        socket.subscribe(JeroMQParent.TOPIC_PREFIX_TASK.getBytes());
//    }

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
