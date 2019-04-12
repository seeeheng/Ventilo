package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.estimote.coresdk.cloud.model.User;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private UserRepository repository;
    private LiveData<List<UserModel>> mAllUsers;
//    private MutableLiveData<UserModel> mUserByAccessTokenOrUserId;

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        mAllUsers = repository.getAllUsers();
//        mUserByAccessTokenOrUserId = repository.getUserByAccessTokenOrUserId();
    }

    public LiveData<List<UserModel>> getAllUsers() {
        return mAllUsers;
    }

//    public MutableLiveData<UserModel> getUserByAccessTokenOrUserId() {
//        return mUserByAccessTokenOrUserId;
//    }

    public void queryUserByUserId(String userId, SingleObserver<UserModel> singleObserver) {
        repository.queryUserByUserId(userId, singleObserver);
    }

    public void queryUserByAccessToken(String accessToken, SingleObserver<UserModel> singleObserver) {
        repository.queryUserByAccessToken(accessToken, singleObserver);
    }

    public void addUser(UserModel userModel) {
        repository.addUser(userModel);
    }

    public void updateUser(UserModel userModel) {
        repository.updateUser(userModel);
    }

    public void deleteUser(String userId) {
        repository.deleteUser(userId);
    }
}
