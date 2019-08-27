package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class TaskRepository {

    private LiveData<List<TaskModel>> mAllTasksLiveData;

    private TaskDao mTaskDao;

    public TaskRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mTaskDao = db.taskDao();
        mAllTasksLiveData = mTaskDao.getAllTasksLiveData();
    }

    /**
     * Obtains all Task models Live data from database
     *
     * @return
     */
    public LiveData<List<TaskModel>> getAllTasksLiveData() {
        return mAllTasksLiveData;
    }


    /**
     * Obtains all Task models from database
     *
     * @param singleObserver
     */
    public void getAllTasks(SingleObserver singleObserver) {
        QueryAllTasksAsyncTask task = new
                QueryAllTasksAsyncTask(mTaskDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

//    /**
//     * Obtain Task based on Id with Id from local database
//     *
//     * @param taskId
//     * @param singleObserver
//     */
//    public void queryTaskById(long taskId, SingleObserver<TaskModel> singleObserver) {
//        Single<TaskModel> single = mTaskDao.queryTaskById(taskId);
//        single.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(singleObserver);
//    }

    /**
     * Obtain Task based on Ref Id with Id from local database
     *
     * @param taskId
     * @param singleObserver
     */
    public void queryTaskByRefId(long taskId, SingleObserver<TaskModel> singleObserver) {
        Single<TaskModel> single = mTaskDao.queryTaskByRefId(taskId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Obtain Task based on created date and time from local database
     *
     * @param createdDateTime
     * @param singleObserver
     */
    public void queryTaskByCreatedDateTime(String createdDateTime, SingleObserver<TaskModel> singleObserver) {
        Single<TaskModel> single = mTaskDao.queryTaskByCreatedDateTime(createdDateTime);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Insert Task model in local database with updates to Observer
     * @param taskModel
     * @param singleObserver
     */
    public void insertTaskWithObserver(TaskModel taskModel, SingleObserver singleObserver) {
        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mTaskDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Insert Task model into local database
     * @param taskModel
     */
    public void insertTask(TaskModel taskModel) {
        InsertAsyncTask task = new InsertAsyncTask(mTaskDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Update Task model by Id with Id in local database
     * @param taskModel
     */
    public void updateTask(TaskModel taskModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mTaskDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Update Task model by Ref Id with Id in local database
     * @param taskModel
     */
    public void updateTaskByRefId(TaskModel taskModel) {
        UpdateByRefIdAsyncTask task = new UpdateByRefIdAsyncTask(mTaskDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Delete Task model by Id with Id from local database
     * @param taskId
     */
    public void deleteTask(long taskId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mTaskDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Delete Task model by Ref Id with Id from local database
     * @param taskId
     */
    public void deleteTaskByRefId(long taskId) {
        DeleteByRefIdAsyncTask task = new DeleteByRefIdAsyncTask(mTaskDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(taskId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Query all Task models in database
     *
     */
    private static class QueryAllTasksAsyncTask extends AsyncTask<String, Void, Void> {

        private TaskDao asyncTaskDao;
        private SingleObserver asyncSingleObserver;

        QueryAllTasksAsyncTask(TaskDao dao, SingleObserver singleObserver) {
            asyncTaskDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<TaskModel> allTasksList =
                    asyncTaskDao.getAllTasks();

            if (allTasksList == null) {
                allTasksList = new ArrayList<>();
            }

            Observable<List<TaskModel>> observableAllTasks =
                    Observable.just(allTasksList);
            Single<List<TaskModel>> singleAllTasks =
                    Single.fromObservable(observableAllTasks);

            singleAllTasks.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    /**
     * Execute insertion of Task model into database as a background task, and sends
     * Task Id to subscribed Observer object
     */
    private static class InsertWithObserverAsyncTask extends AsyncTask<TaskModel, Void, Void> {

        private TaskDao asyncTaskDao;
        private SingleObserver asyncSingleObserver;

        InsertWithObserverAsyncTask(TaskDao dao, SingleObserver singleObserver) {
            asyncTaskDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final TaskModel... taskModel) {
            // Converts type Long to Observable, then to Single for RxJava use
            Long insertedTaskId = asyncTaskDao.insertTaskModel(taskModel[0]);
            Observable<Long> observableInsertedTaskId = Observable.just(insertedTaskId);
            Single<Long> singleInsertedTaskId = Single.fromObservable(observableInsertedTaskId);

            singleInsertedTaskId.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    /**
     * Execute insertion of Task model into database as a background task
     */
    private static class InsertAsyncTask extends AsyncTask<TaskModel, Void, Void> {

        private TaskDao asyncTaskDao;

        InsertAsyncTask(TaskDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TaskModel... taskModel) {
            asyncTaskDao.insertTaskModel(taskModel[0]);
            return null;
        }
    }

    /**
     * Execute update of Task model in database as a background task
     */
    private static class UpdateAsyncTask extends AsyncTask<TaskModel, Void, Void> {

        private TaskDao asyncTaskDao;

        UpdateAsyncTask(TaskDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TaskModel... taskModel) {
            asyncTaskDao.updateTaskModel(taskModel[0]);
            return null;
        }
    }

    /**
     * Execute update of Task model by Ref Id in database as a background task
     */
    private static class UpdateByRefIdAsyncTask extends AsyncTask<TaskModel, Void, Void> {

        private TaskDao asyncTaskDao;

        UpdateByRefIdAsyncTask(TaskDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final TaskModel... taskModel) {
            TaskModel taskModelToUpdate = taskModel[0];
            asyncTaskDao.updateTaskModelByRefId(taskModelToUpdate.getId(), taskModelToUpdate.getPhaseNo(),
                    taskModelToUpdate.getAdHocTaskPriority(),taskModelToUpdate.getAssignedTo(),
                    taskModelToUpdate.getAssignedBy(), taskModelToUpdate.getTitle(),
                    taskModelToUpdate.getDescription(), taskModelToUpdate.getStatus(),
                    taskModelToUpdate.getCreatedDateTime(), taskModelToUpdate.getCompletedDateTime(),
                    taskModelToUpdate.getLastUpdatedStatusDateTime(), taskModelToUpdate.getLastUpdatedMainDateTime(),
                    taskModelToUpdate.getIsValid());

            return null;
        }
    }

    /**
     * Execute deletion of Task model from database as a background task
     */
    private static class DeleteAsyncTask extends AsyncTask<Long, Void, Void> {

        private TaskDao asyncTaskDao;

        DeleteAsyncTask(TaskDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... taskId) {
            asyncTaskDao.deleteTaskModel(taskId[0]);
            return null;
        }
    }

    /**
     * Execute deletion of Task model by Ref Id from database as a background task
     */
    private static class DeleteByRefIdAsyncTask extends AsyncTask<Long, Void, Void> {

        private TaskDao asyncTaskDao;

        DeleteByRefIdAsyncTask(TaskDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... taskRefId) {
            asyncTaskDao.deleteTaskModelByRefId(taskRefId[0]);
            return null;
        }
    }
}
