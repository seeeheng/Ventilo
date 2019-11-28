package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserSitRepJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class UserSitRepJoinRepository {

    private UserSitRepJoinDao mUserSitRepJoinDao;

    public UserSitRepJoinRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mUserSitRepJoinDao = db.userSitRepDao();
    }

    public synchronized void addUserSitRepJoin(UserSitRepJoinModel userSitRepJoinModel) {
        InsertAsyncTask task = new InsertAsyncTask(mUserSitRepJoinDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(userSitRepJoinModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public synchronized void querySitRepsForUser(String userId, SingleObserver<List<SitRepModel>> singleObserver) {
        Single<List<SitRepModel>> single = mUserSitRepJoinDao.querySitRepsForUser(userId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public synchronized void queryUsersForSitRep(long sitRepId, SingleObserver<List<UserModel>> singleObserver) {
        Single<List<UserModel>> single = mUserSitRepJoinDao.queryUsersForSitRep(sitRepId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    private static class InsertAsyncTask extends AsyncTask<UserSitRepJoinModel, Void, Void> {

        private UserSitRepJoinDao asyncUserSitRepJoinDao;

        InsertAsyncTask(UserSitRepJoinDao dao) {
            asyncUserSitRepJoinDao = dao;
        }

        @Override
        protected Void doInBackground(final UserSitRepJoinModel... userSitRepJoinModel) {
            asyncUserSitRepJoinDao.insertUserSitRepJoin(userSitRepJoinModel[0]);
            return null;
        }
    }
}