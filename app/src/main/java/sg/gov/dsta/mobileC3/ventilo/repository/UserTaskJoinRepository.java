package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserTaskJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class UserTaskJoinRepository {

    private UserTaskJoinDao mUserTaskJoinDao;

    public UserTaskJoinRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mUserTaskJoinDao = db.userTaskDao();
    }

    public synchronized void addUserTaskJoin(UserTaskJoinModel userTaskJoinModel) {
        InsertAsyncTask task = new InsertAsyncTask(mUserTaskJoinDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(userTaskJoinModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public synchronized void queryTasksForUser(String userId, SingleObserver<List<TaskModel>> singleObserver) {
        Single<List<TaskModel>> single = mUserTaskJoinDao.queryTasksForUser(userId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public synchronized void queryUsersForTask(long taskId, SingleObserver<List<UserModel>> singleObserver) {
        Single<List<UserModel>> single = mUserTaskJoinDao.queryUsersForTask(taskId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    private static class InsertAsyncTask extends AsyncTask<UserTaskJoinModel, Void, Void> {

        private UserTaskJoinDao asyncUserTaskJoinDao;

        InsertAsyncTask(UserTaskJoinDao dao) {
            asyncUserTaskJoinDao = dao;
        }

        @Override
        protected Void doInBackground(final UserTaskJoinModel... userTaskJoinModel) {
            asyncUserTaskJoinDao.insertUserTaskJoin(userTaskJoinModel[0]);
            return null;
        }
    }
}
