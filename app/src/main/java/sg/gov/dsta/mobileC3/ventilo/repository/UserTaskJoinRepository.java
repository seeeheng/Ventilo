package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserTaskJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoin;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

public class UserTaskJoinRepository {

    private UserTaskJoinDao mUserTaskJoinDao;

    public UserTaskJoinRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mUserTaskJoinDao = db.userTaskDao();
    }

    public void addUserTaskJoin(UserTaskJoin userTaskJoin) {
        InsertAsyncTask task = new InsertAsyncTask(mUserTaskJoinDao);
        task.execute(userTaskJoin);
    }

    public void queryTasksForUser(String userId, SingleObserver<List<TaskModel>> singleObserver) {
        Single<List<TaskModel>> single = mUserTaskJoinDao.queryTasksForUser(userId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public void queryUsersForTask(long taskId, SingleObserver<List<UserModel>> singleObserver) {
        Single<List<UserModel>> single = mUserTaskJoinDao.queryUsersForTask(taskId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    private static class InsertAsyncTask extends AsyncTask<UserTaskJoin, Void, Void> {

        private UserTaskJoinDao asyncUserTaskJoinDao;

        InsertAsyncTask(UserTaskJoinDao dao) {
            asyncUserTaskJoinDao = dao;
        }

        @Override
        protected Void doInBackground(final UserTaskJoin... userTaskJoin) {
            asyncUserTaskJoinDao.insertUserTaskJoin(userTaskJoin[0]);
            return null;
        }
    }
}
