package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

public class UserRepository {

    private LiveData<List<UserModel>> mAllUsers;

    private UserDao mUserDao;

    public UserRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mUserDao = db.userDao();
        mAllUsers = mUserDao.getAllUsers();
    }

    public LiveData<List<UserModel>> getAllUsers() {
        return mAllUsers;
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

    public void addUser(UserModel userModel) {
        InsertAsyncTask task = new InsertAsyncTask(mUserDao);
        task.execute(userModel);
    }

    public void updateUser(UserModel userModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mUserDao);
        task.execute(userModel);
    }

    public void deleteUser(String userId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mUserDao);
        task.execute(userId);
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
