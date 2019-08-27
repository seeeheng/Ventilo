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
import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class UserRepository {

    private LiveData<List<UserModel>> mAllUsersLiveData;

    private UserDao mUserDao;

    public UserRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mUserDao = db.userDao();
        mAllUsersLiveData = mUserDao.getAllUsersLiveData();
    }

    public LiveData<List<UserModel>> getAllUsersLiveData() {
        return mAllUsersLiveData;
    }

    public LiveData<UserModel> getCurrentUserLiveData(String userId) {
        return mUserDao.getCurrentUserLiveData(userId);
    }

    /**
     * Obtains all User models from database
     *
     * @param singleObserver
     */
    public void getAllUsers(SingleObserver singleObserver) {
        QueryAllUsersAsyncTask task = new
                QueryAllUsersAsyncTask(mUserDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void queryUserByAccessToken(String accessToken, SingleObserver<UserModel> singleObserver) {
        Single<UserModel> single = mUserDao.getUserByAccessToken(accessToken);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public void queryUserByUserId(String userId, SingleObserver<UserModel> singleObserver) {
        Single<UserModel> single = mUserDao.getUserByUserId(userId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public void insertUser(UserModel userModel) {
        InsertAsyncTask task = new InsertAsyncTask(mUserDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(userModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void updateUser(UserModel userModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mUserDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(userModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void deleteUser(String userId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mUserDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(userId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Query all User models in database
     *
     */
    private static class QueryAllUsersAsyncTask extends AsyncTask<String, Void, Void> {

        private UserDao asyncUserDao;
        private SingleObserver asyncSingleObserver;

        QueryAllUsersAsyncTask(UserDao dao, SingleObserver singleObserver) {
            asyncUserDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<UserModel> allUsersList =
                    asyncUserDao.getAllUsers();

            if (allUsersList == null) {
                allUsersList = new ArrayList<>();
            }

            Observable<List<UserModel>> observableAllUsers =
                    Observable.just(allUsersList);
            Single<List<UserModel>> singleAllUsers =
                    Single.fromObservable(observableAllUsers);

            singleAllUsers.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<UserModel, Void, Void> {

        private UserDao asyncTaskDao;

        InsertAsyncTask(UserDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final UserModel... userModel) {
            asyncTaskDao.createUser(userModel[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<UserModel, Void, Void> {

        private UserDao asyncUserDao;

        UpdateAsyncTask(UserDao dao) {
            asyncUserDao = dao;
        }

        @Override
        protected Void doInBackground(final UserModel... userModel) {
            asyncUserDao.updateUserModel(userModel[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<String, Void, Void> {

        private UserDao asyncUserDao;

        DeleteAsyncTask(UserDao dao) {
            asyncUserDao = dao;
        }

        @Override
        protected Void doInBackground(final String... userId) {
            asyncUserDao.deleteUserModel(userId[0]);
            return null;
        }
    }
}
