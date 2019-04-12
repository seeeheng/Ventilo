package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;

public class TaskRepository {
//    private MutableLiveData<List<TaskModel>> mTaskResults =
//            new MutableLiveData<>();
    private LiveData<List<TaskModel>> mAllTasks;

    private TaskDao mTaskDao;

    public TaskRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mTaskDao = db.taskDao();
        mAllTasks = mTaskDao.getAllTasks();
    }

    public LiveData<List<TaskModel>> getAllTasks() {
        return mAllTasks;
    }

    public void addTask(TaskModel taskModel, SingleObserver singleObserver) {
        InsertAsyncTask task = new InsertAsyncTask(mTaskDao, singleObserver);
        task.execute(taskModel);
    }

    public void updateTask(TaskModel taskModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mTaskDao);
        task.execute(taskModel);
    }

    public void deleteTask(long taskId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mTaskDao);
        task.execute(taskId);
    }

    private static class InsertAsyncTask extends AsyncTask<TaskModel, Void, Void> {

        private TaskDao asyncTaskDao;
        private SingleObserver asyncSingleObserver;

        InsertAsyncTask(TaskDao dao, SingleObserver singleObserver) {
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
}
