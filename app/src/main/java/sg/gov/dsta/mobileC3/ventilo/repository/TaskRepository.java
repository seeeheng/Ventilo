package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;

public class TaskRepository {

    private LiveData<List<TaskModel>> mAllTasks;

    private TaskDao mTaskDao;

    public TaskRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mTaskDao = db.taskDao();
        mAllTasks = mTaskDao.getAllTasks();
    }

    /**
     * Obtains all Task models from database
     * @return
     */
    public LiveData<List<TaskModel>> getAllTasks() {
        return mAllTasks;
    }

    /**
     * Insert Task model in local database with updates to Observer
     * @param taskModel
     * @param singleObserver
     */
    public void addTask(TaskModel taskModel, SingleObserver singleObserver) {
        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mTaskDao, singleObserver);
        task.execute(taskModel);
    }

    /**
     * Insert Task model into local database
     * @param taskModel
     */
    public void insertTask(TaskModel taskModel) {
        InsertAsyncTask task = new InsertAsyncTask(mTaskDao);
        task.execute(taskModel);
    }

    /**
     * Update Task model in local database
     * @param taskModel
     */
    public void updateTask(TaskModel taskModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mTaskDao);
        task.execute(taskModel);
    }

    /**
     * Update Task model by Ref Id in local database
     * @param taskModel
     */
    public void updateTaskByRefId(TaskModel taskModel) {
        UpdateByRefIdAsyncTask task = new UpdateByRefIdAsyncTask(mTaskDao);
        task.execute(taskModel);
    }

    /**
     * Delete Task model from local database
     * @param taskId
     */
    public void deleteTask(long taskId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mTaskDao);
        task.execute(taskId);
    }

    /**
     * Delete Task model by Ref Id from local database
     * @param taskRefId
     */
    public void deleteTaskByRefId(long taskRefId) {
        DeleteByRefIdAsyncTask task = new DeleteByRefIdAsyncTask(mTaskDao);
        task.execute(taskRefId);
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
            asyncTaskDao.updateTaskModelByRefId(taskModelToUpdate.getAssignedTo(),
                    taskModelToUpdate.getAssignedBy(), taskModelToUpdate.getTitle(),
                    taskModelToUpdate.getDescription(), taskModelToUpdate.getStatus(),
                    taskModelToUpdate.getCreatedDateTime(), taskModelToUpdate.getRefId());

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
