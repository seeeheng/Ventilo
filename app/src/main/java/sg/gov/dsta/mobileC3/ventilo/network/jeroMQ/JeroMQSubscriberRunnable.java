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
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserSitRepJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;
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

            String messageTopic = ValidationUtil.getFirstWord(message);
            String messageContent = ValidationUtil.removeFirstWord(message);
            String[] messageTopicParts = messageTopic.split(StringUtil.HYPHEN);
            String messageMainTopic = messageTopicParts[0].
                    concat(StringUtil.HYPHEN).concat(messageTopicParts[1]);
            String messageTopicAction = messageTopicParts[2];

            switch (messageMainTopic) {
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
    }

    /**
     * Stores/updates/deletes incoming Sit Rep JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeSitRepMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeSitRepMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            SitRepModel sitRepModel = gson.fromJson(jsonMsg, SitRepModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_INSERT:
                    insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    updateSitRepInDatabase(sitRepRepo, sitRepModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    deleteSitRepInDatabase(sitRepRepo, sitRepModel.getRefId());
                    break;
            }
        }
    }

    private void insertSitRepIntoDatabase(SitRepRepository sitRepRepo, SitRepModel sitRepModel) {
        // Get sitRepId after adding Sit Rep model into database
        // Use newly generated sitRepId to create UserSitRepJoin entry in composite table
        SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Long sitRepId) {
                Log.d(TAG, "onSuccess singleObserverAddSitRep, " +
                        "insertSitRepIntoDatabase. " +
                        "SitRepId: " + sitRepId);

                UserSitRepJoinRepository userSitRepJoinRepository = new
                        UserSitRepJoinRepository((Application) MainApplication.getAppContext());

                // Reporter is used as userId for UserSitRepJoin composite table in local database
                // Create row for UserSitRepJoin with userId and sitRepId
                String sitRepReporterGroups = sitRepModel.getReporter();
                String[] sitRepReporterGroupsArray = sitRepReporterGroups.split(",");
                for (int i = 0; i < sitRepReporterGroupsArray.length; i++) {
                    UserSitRepJoinModel userSitRepJoinModel = new
                            UserSitRepJoinModel(sitRepReporterGroupsArray[i].trim(), sitRepId);
                    userSitRepJoinRepository.addUserSitRepJoin(userSitRepJoinModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddSitRep, insertSitRepIntoDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        sitRepRepo.addSitRep(sitRepModel, singleObserverAddSitRep);
    }

    private void updateSitRepInDatabase(SitRepRepository sitRepRepo, SitRepModel sitRepModel) {
        sitRepRepo.updateSitRepByRefId(sitRepModel);
    }

    private void deleteSitRepInDatabase(SitRepRepository sitRepRepo, long sitRepId) {
        sitRepRepo.deleteSitRepByRefId(sitRepId);
    }

    /**
     * Stores/updates/deletes incoming Task JSON messages from other devices into local database
     *
     * @param jsonMsg
     */
    private void storeTaskMessage(String jsonMsg, String messageTopicAction) {
        Log.i(TAG, "storeTaskMessage jsonMsg: " + jsonMsg);

        if (MainApplication.getAppContext() instanceof Application) {
            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
            Gson gson = GsonCreator.createGson();
            TaskModel taskModel = gson.fromJson(jsonMsg, TaskModel.class);

            switch (messageTopicAction) {
                case JeroMQPublisher.TOPIC_INSERT:
                    insertTaskIntoDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_UPDATE:
                    updateTaskInDatabase(taskRepo, taskModel);
                    break;
                case JeroMQPublisher.TOPIC_DELETE:
                    deleteTaskInDatabase(taskRepo, taskModel.getRefId());
                    break;
            }
        }
    }

    private void insertTaskIntoDatabase(TaskRepository taskRepo, TaskModel taskModel) {
        // Get taskId after adding Task model into database
        // Use newly generated taskId to create UserTaskJoin entry in composite table
        SingleObserver<Long> singleObserverAddTask = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(Long taskId) {
                Log.d(TAG, "onSuccess singleObserverAddTask, " +
                        "insertTaskIntoDatabase. " +
                        "TaskId: " + taskId);

                UserTaskJoinRepository userTaskJoinRepository = new
                        UserTaskJoinRepository((Application) MainApplication.getAppContext());

                // AssignedTo is used as userId(s) for UserTaskJoin composite table in local database
                // Create row for UserTaskJoin with userId and taskId
                String taskAssignedToGroups = taskModel.getAssignedTo();
                String[] taskAssignedToGroupsArray = taskAssignedToGroups.split(",");
                for (int i = 0; i < taskAssignedToGroupsArray.length; i++) {
                    UserTaskJoinModel userTaskJoinModel = new
                            UserTaskJoinModel(taskAssignedToGroupsArray[i].trim(), taskId);
                    userTaskJoinRepository.addUserTaskJoin(userTaskJoinModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddTask, insertTaskIntoDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        taskRepo.addTask(taskModel, singleObserverAddTask);
    }

    private void updateTaskInDatabase(TaskRepository taskRepo, TaskModel taskModel) {
        taskRepo.updateTaskByRefId(taskModel);
    }

    private void deleteTaskInDatabase(TaskRepository taskRepo, long taskRefId) {
        taskRepo.deleteTaskByRefId(taskRefId);
    }

    /**
     * Subscribe socket to all relevant topics
     *
     * @param socket
     */
    private void subscribeTopics(ZMQ.Socket socket) {
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_BFT.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_SITREP.getBytes());
        socket.subscribe(JeroMQPublisher.TOPIC_PREFIX_TASK.getBytes());
    }
}
