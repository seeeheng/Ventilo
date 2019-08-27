package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.app.Application;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

import java.util.Collections;
import java.util.Comparator;
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
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import timber.log.Timber;

public class JeroMQClientPairRunnable implements Runnable {

    private static final String TAG = JeroMQClientPairRunnable.class.getSimpleName();

    public static final String DATA_SYNC_SUCCESSFUL_INTENT_ACTION = "Data Sync Successful";
    public static final String DATA_SYNC_FAILED_INTENT_ACTION = "Data Sync Failed";

    private List<Socket> mSocketList;
    private ZMQ.Poller mPoller;
    private boolean mIsPollerToBeClosed;
    private CloseSocketsListener mCloseSocketsListener;

    protected JeroMQClientPairRunnable(List<Socket> socketList, ZMQ.Poller poller,
                                       CloseSocketsListener closeSocketsListener) {
        mSocketList = socketList;
        mPoller = poller;
        mCloseSocketsListener = closeSocketsListener;
    }

    @Override
    public void run() {
        Timber.i("JeroMQClientPairRunnable running in progress..");
//        subscribeTopics(mSocketList);

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

        Timber.i("Req poller closed.");
    }

    protected void closePoller() {
        mIsPollerToBeClosed = true;
    }

    private void storeMessageInDatabase(Socket socket) {
        Intent broadcastIntent = new Intent();
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
            DatabaseOperation databaseOperation = new DatabaseOperation();
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Timber.i("storeSitRepMessage sync");

                    handleSitRepDataSync(databaseOperation, sitRepRepo, sitRepModel);
                    break;
            }
        }
    }

    /**
     * Create new Sit Rep model and insert into database
     *
     * @param databaseOperation
     * @param sitRepRepo
     * @param sitRepModel
     * @return
     */
    private void insertNewSitRepModelForSync(DatabaseOperation databaseOperation,
                                             SitRepRepository sitRepRepo,
                                             SitRepModel sitRepModel) {

        SitRepModel newSitRepModel = new SitRepModel();
        newSitRepModel.setRefId(sitRepModel.getRefId());
        newSitRepModel.setReporter(sitRepModel.getReporter());
        newSitRepModel.setSnappedPhoto(sitRepModel.getSnappedPhoto());
        newSitRepModel.setLocation(sitRepModel.getLocation());
        newSitRepModel.setActivity(sitRepModel.getActivity());
        newSitRepModel.setPersonnelT(sitRepModel.getPersonnelT());
        newSitRepModel.setPersonnelS(sitRepModel.getPersonnelS());
        newSitRepModel.setPersonnelD(sitRepModel.getPersonnelD());
        newSitRepModel.setNextCoa(sitRepModel.getNextCoa());
        newSitRepModel.setRequest(sitRepModel.getRequest());
        newSitRepModel.setOthers(sitRepModel.getOthers());
        newSitRepModel.setCreatedDateTime(sitRepModel.getCreatedDateTime());
        newSitRepModel.setLastUpdatedDateTime(sitRepModel.getLastUpdatedDateTime());
        newSitRepModel.setIsValid(sitRepModel.getIsValid());

        databaseOperation.insertSitRepIntoDatabase(sitRepRepo, newSitRepModel);

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
                currentMatchedSitRepModel.setLocation(newSitRepModel.getLocation());
                currentMatchedSitRepModel.setActivity(newSitRepModel.getActivity());
                currentMatchedSitRepModel.setPersonnelT(newSitRepModel.getPersonnelT());
                currentMatchedSitRepModel.setPersonnelS(newSitRepModel.getPersonnelS());
                currentMatchedSitRepModel.setPersonnelD(newSitRepModel.getPersonnelD());
                currentMatchedSitRepModel.setNextCoa(newSitRepModel.getNextCoa());
                currentMatchedSitRepModel.setRequest(newSitRepModel.getRequest());
                currentMatchedSitRepModel.setOthers(newSitRepModel.getOthers());
                currentMatchedSitRepModel.setLastUpdatedDateTime(newSitRepModel.getLastUpdatedDateTime());

            }
        }

        return currentMatchedSitRepModel;
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
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(SitRepModel matchedSitRepModel) {
                if (matchedSitRepModel == null) {
                    Timber.i("Inserting new Sit Rep model from synchronisation...");

                    insertNewSitRepModelForSync(databaseOperation, sitRepRepo, sitRepModel);

                } else {
                    Timber.i("Updating Sit Rep model from synchronisation...");

                    SitRepModel sitRepModelToSync = compareSitRepModelModelForSync(matchedSitRepModel, sitRepModel);
                    databaseOperation.updateSitRepInDatabase(sitRepRepo, sitRepModel);

                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.i("onError singleObserverSyncSitRep, syncSitRepInDatabase. Error Msg: %s", e.toString());
                Timber.i("Inserting new Sit Rep model from synchronisation...");

                insertNewSitRepModelForSync(databaseOperation, sitRepRepo, sitRepModel);
            }
        };

        databaseOperation.querySitRepByCreatedDateTimeInDatabase(sitRepRepo,
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
            DatabaseOperation databaseOperation = new DatabaseOperation();
            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            TaskModel taskModel = gson.fromJson(jsonMsg, TaskModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_SYNC:
                    Timber.i("storeTaskMessage sync");

                    handleTaskDataSync(databaseOperation, taskRepo, taskModel);
                    break;
//                case JeroMQPublisher.TOPIC_INSERT:
//                    Timber.i("storeTaskMessage insert");
//
//                    databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
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
     * @param databaseOperation
     * @param taskRepo
     * @param taskModel
     * @return
     */
    private void insertNewTaskModelForSync(DatabaseOperation databaseOperation,
                                           TaskRepository taskRepo,
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

        databaseOperation.insertTaskIntoDatabase(taskRepo, newTaskModel);

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
     * @param databaseOperation
     * @param taskRepo
     * @param taskModel
     */
    private void handleTaskDataSync(DatabaseOperation databaseOperation, TaskRepository taskRepo,
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

                    insertNewTaskModelForSync(databaseOperation, taskRepo, taskModel);

                } else {
                    Timber.i("Updating Task model from synchronisation...");

                    TaskModel taskModelToSync = compareTaskModelForSync(matchedTaskModel, taskModel);
                    databaseOperation.updateTaskInDatabase(taskRepo, taskModelToSync);

                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.i("onError singleObserverSyncTask, syncTaskInDatabase. Error Msg: %s", e.toString());
                Timber.i("Inserting new Task model from synchronisation...");

                insertNewTaskModelForSync(databaseOperation, taskRepo, taskModel);
            }
        };

        databaseOperation.queryTaskByCreatedDateTimeInDatabase(taskRepo,
                taskModel.getCreatedDateTime(), singleObserverSyncTask);
    }

    /**
     * Subscribe socket list to all relevant topics
     *
     * @param socketList
     */
    private void subscribeTopics(List<Socket> socketList) {
        for (int i = 0; i < mSocketList.size(); i++) {
            Socket socket = socketList.get(i);
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_SITREP.getBytes());
            socket.subscribe(JeroMQParent.TOPIC_PREFIX_TASK.getBytes());
        }
    }

    public interface CloseSocketsListener {
        void closeSockets();
    }
}
