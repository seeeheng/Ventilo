package sg.gov.dsta.mobileC3.ventilo.database;

import android.app.Application;
import android.util.Log;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserSitRepJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.VideoStreamRepository;

public class DatabaseOperation {

    private static final String TAG = DatabaseOperation.class.getSimpleName();

    public DatabaseOperation() {
    }

    /* -------------------- User -------------------- */

    /**
     * Retrieve all Users from database
     *
     * @param userRepo
     * @param singleObserver
     */
    public void getAllUsersFromDatabase(UserRepository userRepo,
                                        SingleObserver<List<UserModel>> singleObserver) {
        userRepo.getAllUsers(singleObserver);
    }

    /**
     * Insertion of User model into database
     *
     * @param userRepo
     * @param userModel
     */
    public void insertUserIntoDatabase(UserRepository userRepo, UserModel userModel) {
        userRepo.insertUser(userModel);
    }

    /**
     * Update of User model in database
     *
     * @param userRepo
     * @param userModel
     */
    public void updateUserInDatabase(UserRepository userRepo, UserModel userModel) {
        userRepo.updateUser(userModel);
    }

    /**
     * Deletion of User model in database
     *
     * @param userRepo
     * @param userId
     */
    public void deleteUserInDatabase(UserRepository userRepo, String userId) {
        userRepo.deleteUser(userId);
    }

    /* -------------------- Video Stream -------------------- */

    /**
     * Retrieve all Video Stream from database
     *
     * @param videoStreamRepo
     * @param singleObserver
     */
    public void getAllVideoStreamsFromDatabase(VideoStreamRepository videoStreamRepo,
                                               SingleObserver<List<VideoStreamModel>> singleObserver) {
        videoStreamRepo.getAllVideoStreams(singleObserver);
    }

    /**
     * Insertion of Video Stream model into database
     *
     * @param videoStreamRepo
     * @param videoStreamModel
     */
    public void insertVideoStreamIntoDatabase(VideoStreamRepository videoStreamRepo,
                                              VideoStreamModel videoStreamModel) {
        videoStreamRepo.insertVideoStream(videoStreamModel);
    }

    /**
     * Update of Video Stream model in database
     *
     * @param videoStreamRepo
     * @param videoStreamModel
     */
    public void updateVideoStreamInDatabase(VideoStreamRepository videoStreamRepo,
                                            VideoStreamModel videoStreamModel) {
        videoStreamRepo.updateVideoStream(videoStreamModel);
    }

    /**
     * Deletion of Video Stream model in database
     *
     * @param videoStreamRepo
     * @param videoStreamId
     */
    public void deleteVideoStreamInDatabase(VideoStreamRepository videoStreamRepo,
                                            long videoStreamId) {
        videoStreamRepo.deleteVideoStream(videoStreamId);
    }

    /* -------------------- Sit Rep -------------------- */

    /**
     * Retrieve all Sit Reps from database
     *
     * @param sitRepRepo
     * @param singleObserver
     */
    public void getAllSitRepsFromDatabase(SitRepRepository sitRepRepo,
                                          SingleObserver<List<SitRepModel>> singleObserver) {
        sitRepRepo.getAllSitReps(singleObserver);
    }

    /**
     * Obtain Sit Rep based on Ref Id with Id from database
     *
     * @param sitRepRepo
     * @param sitRepId
     * @param singleObserver
     */
    public void querySitRepByRefIdInDatabase(SitRepRepository sitRepRepo, long sitRepId,
                                             SingleObserver<SitRepModel> singleObserver) {
        sitRepRepo.querySitRepByRefId(sitRepId, singleObserver);
    }

    /**
     * Insertion of SitRepModel into database
     *
     * @param sitRepRepo
     * @param sitRepModel
     */
    public void insertSitRepIntoDatabase(SitRepRepository sitRepRepo, SitRepModel sitRepModel) {
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
                for (String sitRepReporter : sitRepReporterGroupsArray) {
                    UserSitRepJoinModel userSitRepJoinModel = new
                            UserSitRepJoinModel(sitRepReporter.trim(), sitRepId);
                    userSitRepJoinRepository.addUserSitRepJoin(userSitRepJoinModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddSitRep, insertSitRepIntoDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        sitRepRepo.insertSitRepWithObserver(sitRepModel, singleObserverAddSitRep);
    }

    /**
     * Update of SitRepModel in database
     *
     * @param sitRepRepo
     * @param sitRepModel
     */
    public void updateSitRepInDatabase(SitRepRepository sitRepRepo, SitRepModel sitRepModel) {
        sitRepRepo.updateSitRepByRefId(sitRepModel);
    }

    /**
     * Deletion of SitRepModel in database
     *
     * @param sitRepRepo
     * @param sitRepId
     */
    public void deleteSitRepInDatabase(SitRepRepository sitRepRepo, long sitRepId) {
        sitRepRepo.deleteSitRepByRefId(sitRepId);
    }

    /* -------------------- Task -------------------- */

    /**
     * Retrieve all Tasks from database
     *
     * @param taskRepo
     * @param singleObserver
     */
    public void getAllTasksFromDatabase(TaskRepository taskRepo,
                                          SingleObserver<List<TaskModel>> singleObserver) {
        taskRepo.getAllTasks(singleObserver);
    }

    /**
     * Obtain Task based on Ref Id with Id from database
     *
     * @param taskRepo
     * @param taskId
     * @param singleObserver
     */
    public void queryTaskByRefIdInDatabase(TaskRepository taskRepo, long taskId,
                                           SingleObserver<TaskModel> singleObserver) {
        taskRepo.queryTaskByRefId(taskId, singleObserver);
    }

    /**
     * Insertion of TaskModel into database
     *
     * @param taskRepo
     * @param taskModel
     */
    public void insertTaskIntoDatabase(TaskRepository taskRepo, TaskModel taskModel) {
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

    /**
     * Update of TaskModel in database
     *
     * @param taskRepo
     * @param taskModel
     */
    public void updateTaskInDatabase(TaskRepository taskRepo, TaskModel taskModel) {
        taskRepo.updateTaskByRefId(taskModel);
    }

    /**
     * Deletion of TaskModel in database
     *
     * @param taskRepo
     * @param taskId
     */
    public void deleteTaskInDatabase(TaskRepository taskRepo, long taskId) {
        taskRepo.deleteTaskByRefId(taskId);
    }
}
