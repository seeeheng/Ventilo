package sg.gov.dsta.mobileC3.ventilo.database;

import android.app.Application;
import android.util.Log;

import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserSitRepJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.VideoStreamRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import timber.log.Timber;

public class DatabaseOperation {

    private static final String TAG = DatabaseOperation.class.getSimpleName();

    public DatabaseOperation() {
    }

    /* ---------------------------------------- User ---------------------------------------- */

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
     * Obtain User by userId from database
     *
     * @param userRepo
     * @param userId
     * @param singleObserver
     */
    public void queryUserByUserIdInDatabase(UserRepository userRepo, String userId,
                                             SingleObserver<UserModel> singleObserver) {
        userRepo.queryUserByUserId(userId, singleObserver);
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

    /* ---------------------------------------- Radio ---------------------------------------- */

    /**
     * Retrieve all WaveRelay Radios from database
     *
     * @param waveRelayRadioRepo
     * @param singleObserver
     */
    public void getAllRadiosFromDatabase(WaveRelayRadioRepository waveRelayRadioRepo,
                                        SingleObserver<List<WaveRelayRadioModel>> singleObserver) {
        waveRelayRadioRepo.getAllWaveRelayRadios(singleObserver);
    }

    /**
     * Obtain WaveRelay Radio by userId from database
     *
     * @param waveRelayRadioRepo
     * @param radioId
     * @param singleObserver
     */
    public void queryRadioByRadioIdInDatabase(WaveRelayRadioRepository waveRelayRadioRepo, long radioId,
                                            SingleObserver<WaveRelayRadioModel> singleObserver) {
        waveRelayRadioRepo.queryRadioByRadioId(radioId, singleObserver);
    }

    /**
     * Insertion of WaveRelay Radio model into database
     *
     * @param waveRelayRadioRepo
     * @param waveRelayRadioModel
     */
    public void insertRadioIntoDatabase(WaveRelayRadioRepository waveRelayRadioRepo,
                                        WaveRelayRadioModel waveRelayRadioModel) {
        waveRelayRadioRepo.insertWaveRelayRadio(waveRelayRadioModel);
    }

    /**
     * Update of WaveRelay Radio model in database
     *
     * @param waveRelayRadioRepo
     * @param waveRelayRadioModel
     */
    public void updateRadioInDatabase(WaveRelayRadioRepository waveRelayRadioRepo,
                                     WaveRelayRadioModel waveRelayRadioModel) {
        waveRelayRadioRepo.updateWaveRelayRadio(waveRelayRadioModel);
    }

    /**
     * Deletion of WaveRelay Radio model in database
     *
     * @param waveRelayRadioRepo
     * @param radioId
     */
    public void deleteRadioInDatabase(WaveRelayRadioRepository waveRelayRadioRepo, long radioId) {
        waveRelayRadioRepo.deleteWaveRelayRadio(radioId);
    }

    /* ---------------------------------------- BFT ---------------------------------------- */

    /**
     * Insertion of BFT model into database
     *
     * @param bFTRepo
     * @param bFTModel
     */
    public void insertBFTIntoDatabase(BFTRepository bFTRepo, BFTModel bFTModel) {
        bFTRepo.insertBFT(bFTModel);
    }

    /* ---------------------------------------- Video Stream ---------------------------------------- */

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

    /* ---------------------------------------- Sit Rep ---------------------------------------- */

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
                Timber.i("onSuccess singleObserverAddSitRep, insertSitRepIntoDatabase. SitRepId: %ld", sitRepId);



                UserSitRepJoinRepository userSitRepJoinRepository = new
                        UserSitRepJoinRepository((Application) MainApplication.getAppContext());

                // Reporter is used as userId for UserSitRepJoin composite table in local database
                // Create row for UserSitRepJoin with userId and sitRepId
                String sitRepReporterGroups = sitRepModel.getReporter();
                String[] sitRepReporterGroupsArray = StringUtil.
                        removeCommasAndExtraSpaces(sitRepReporterGroups);
                for (String sitRepReporter : sitRepReporterGroupsArray) {
                    UserSitRepJoinModel userSitRepJoinModel = new
                            UserSitRepJoinModel(sitRepReporter.trim(), sitRepId);
                    userSitRepJoinRepository.addUserSitRepJoin(userSitRepJoinModel);
                }
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverAddSitRep, insertSitRepIntoDatabase. Error Msg: %s" , e.toString());

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

        // If RefId is local refId, it means that received sitRepModel is from original creator
        // i.e.  Team Alpha Lead originally creates a sit rep, and updates own sit rep info thereafter
        if (sitRepModel.getRefId() == DatabaseTableConstants.LOCAL_REF_ID) {
            sitRepRepo.updateSitRepByRefId(sitRepModel);
        } else {    // Else received updated sitRepModel may not be from original creator,
                    // but from a Team Lead who updated his sit rep details and sends this update to 2 users:
                    // 1) CCT
                    // 2) Other Team Leads
                    //
                    // Hence, to handle each user recipient, the following is to be done:
                    // 1) For CCT - check if received RefId sitRepModel is
                    // the same as a TaskId in his own sit rep list database, and update his own sitRepModel
                    // 2) For other Team Leads - received RefId sitRepModel is
                    // the same as a RefId in his own task list database, and update this sitRepModel

            SingleObserver<List<SitRepModel>> singleObserverGetAllSitReps = new SingleObserver<List<SitRepModel>>() {
                @Override
                public void onSubscribe(Disposable d) {}

                @Override
                public void onSuccess(List<SitRepModel> sitRepModelList) {

                    Timber.i("onSuccess singleObserverGetAllSitReps, updateSitRepInDatabase. sitRepModelList.size(): %d" , sitRepModelList.size());


                    // Extracts a list of all Ref Id from the list of all SitRepModel objects
                    // Finds a match between receiving model's RefId and own model's id from local database
                    boolean isMatchedIdFound = sitRepModelList.stream().map(
                            SitRepModel -> SitRepModel.getId()).anyMatch(id -> id == sitRepModel.getRefId());

                    // Extracts a list of all Ref Id from the list of all SitRepModel objects
                    // Finds a match between receiving model's RefId and own model's RefId from local database
                    boolean isMatchedRefIdFound = sitRepModelList.stream().map(
                            SitRepModel -> SitRepModel.getRefId()).anyMatch(refId -> refId == sitRepModel.getRefId());

                    // Set received task model's id to its own refId
                    // For both CCT and Team Lead recipients for update purpose
                    sitRepModel.setId(sitRepModel.getRefId());

                    if (isMatchedIdFound) {
                        // Set refId to be local refId (-1) as current user is original creator
                        sitRepModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
                        sitRepRepo.updateSitRep(sitRepModel);
                    } else if (isMatchedRefIdFound) {
                        // Update based on RefId with updated received id
                        sitRepRepo.updateSitRepByRefId(sitRepModel);
                    }
                }

                @Override
                public void onError(Throwable e) {

                    Timber.e("onError singleObserverGetAllSitReps, updateSitRepInDatabase. Error Msg: %s" , e.toString());

                }
            };

            sitRepRepo.getAllSitReps(singleObserverGetAllSitReps);
        }

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

    /* ---------------------------------------- Task ---------------------------------------- */

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

                Timber.i("onSuccess singleObserverAddTask, insertTaskIntoDatabase. TaskId: %ld" , taskId);


                UserTaskJoinRepository userTaskJoinRepository = new
                        UserTaskJoinRepository((Application) MainApplication.getAppContext());

                // AssignedTo is used as userId(s) for UserTaskJoin composite table in local database
                // Create row for UserTaskJoin with userId and taskId
                String assignedToGroup = taskModel.getAssignedTo();
                String[] assignedToGroupsArray = StringUtil.removeCommasAndExtraSpaces(assignedToGroup);
                for (int i = 0; i < assignedToGroupsArray.length; i++) {
                    UserTaskJoinModel userTaskJoinModel = new
                            UserTaskJoinModel(assignedToGroupsArray[i].trim(), taskId);
                    userTaskJoinRepository.addUserTaskJoin(userTaskJoinModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAddTask, insertTaskIntoDatabase. Error Msg: %s" , e.toString());

            }
        };

        taskRepo.addTask(taskModel, singleObserverAddTask);
    }

    /**
     * Insertion of TaskModel into database and broadcasts to other devices
     *
     * @param taskModel
     */
    public void insertTaskIntoDatabaseAndBroadcast(TaskRepository taskRepo, TaskModel taskModel) {

        SingleObserver<Long> singleObserverAddTask = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long taskId) {
                Timber.i("onSuccess singleObserverAddTask, insertTaskIntoDatabaseAndBroadcast. TaskId: %ld" , taskId);


                taskModel.setRefId(taskId);
                taskRepo.updateTask(taskModel);

                UserTaskJoinRepository userTaskJoinRepository = new
                        UserTaskJoinRepository((Application) MainApplication.getAppContext());

                // AssignedTo is used as userId(s) for UserTaskJoin composite table in local database
                // Create row for UserTaskJoin with userId and taskId
                String assignedToGroup = taskModel.getAssignedTo();
                String[] assignedToGroupsArray = StringUtil.removeCommasAndExtraSpaces(assignedToGroup);
                for (int i = 0; i < assignedToGroupsArray.length; i++) {
                    UserTaskJoinModel userTaskJoinModel = new
                            UserTaskJoinModel(assignedToGroupsArray[i].trim(), taskId);
                    userTaskJoinRepository.addUserTaskJoin(userTaskJoinModel);
                }

                // Send newly created Task model to all other devices
                JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(taskModel);
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverAddTask, insertTaskIntoDatabaseAndBroadcast. Error Msg: %s" , e.toString());

            }
        };

        taskRepo.insertTaskWithObserver(taskModel, singleObserverAddTask);
    }

    /**
     * Update of TaskModel in database
     *
     * @param taskRepo
     * @param taskModel
     */
    public void updateTaskInDatabase(TaskRepository taskRepo, TaskModel taskModel) {
        // If RefId is local refId, it means that received taskModel is from original creator
        // i.e. CCT originally creates a task, and updates his own task info thereafter
        if (taskModel.getRefId() == DatabaseTableConstants.LOCAL_REF_ID) {
            taskRepo.updateTaskByRefId(taskModel);
        } else {    // Else received taskModel may not be from original creator,
                    // but from a Team Lead who updated his task status and sends this update to 2 users:
                    // 1) CCT (for viewing in Tasks and Timeline)
                    // 2) Other Team Leads (for viewing in Timeline)
                    //
                    // Hence, to handle each user recipient, the following is to be done:
                    // 1) For CCT (original creator) - check if received RefId taskModel is
                    // the same as a TaskId in his own task list database, and update his own task model
                    // 2) For other Team Leads (NOT original creator) - received RefId taskModel is
                    // the same as a RefId in his own task list database, and update this task model

            SingleObserver<List<TaskModel>> singleObserverGetAllTasks = new SingleObserver<List<TaskModel>>() {
                @Override
                public void onSubscribe(Disposable d) {}

                @Override
                public void onSuccess(List<TaskModel> taskModelList) {

                    Timber.i("onSuccess singleObserverGetAllTasks, updateTaskInDatabase. taskModelList.size(): %d" , taskModelList.size());


                    // Extracts a list of all Ref Id from the list of all TaskModel objects
                    // Finds a match between receiving model's RefId and own model's id from local database
                    boolean isMatchedIdFound = taskModelList.stream().map(
                            TaskModel -> TaskModel.getId()).anyMatch(id -> id == taskModel.getRefId());

//                    List<Long> matchedIdList = taskIdList.stream().
//                            filter(id -> id == taskModel.getId()).
//                            collect(Collectors.toList());

//                    boolean isMatchedRefIdFound = taskRefIdList.stream().anyMatch(refId -> refId == taskModel.getRefId());

                    // Extracts a list of all Ref Id from the list of all TaskModel objects
                    // Finds a match between receiving model's RefId and own model's RefId from local database
                    boolean isMatchedRefIdFound = taskModelList.stream().map(
                            TaskModel -> TaskModel.getRefId()).anyMatch(refId -> refId == taskModel.getRefId());

//                    boolean isMatchedRefIdFound = taskRefIdList.stream().anyMatch(refId -> refId == taskModel.getRefId());

                    // Set received task model's id to its own refId
                    // For both CCT and Team Lead recipients
                    taskModel.setId(taskModel.getRefId());

                    // For CCT
                    if (isMatchedIdFound) {
                        // Set refId to be local refId (-1) as current user is original creator
                        taskModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
                        taskRepo.updateTask(taskModel);
                    } else if (isMatchedRefIdFound) { // For Team Leads
                        // Update based on RefId with updated received id
                        taskRepo.updateTaskByRefId(taskModel);
                    }
                }

                @Override
                public void onError(Throwable e) {

                    Timber.e("onError singleObserverGetAllTasks, updateTaskInDatabase. Error Msg: %s" , e.toString());


                }
            };

            taskRepo.getAllTasks(singleObserverGetAllTasks);
        }
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
