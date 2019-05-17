package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private UserRepository repository;
    private LiveData<List<UserModel>> mAllUsersLiveData;

    public UserViewModel(Application application) {
        super(application);
        repository = new UserRepository(application);
        mAllUsersLiveData = repository.getAllUsersLiveData();
    }

    public LiveData<List<UserModel>> getAllUsersLiveData() {
        return mAllUsersLiveData;
    }

    public void getAllUsers(SingleObserver<UserModel> singleObserver) {
        repository.getAllUsers(singleObserver);
    }

    public void queryUserByUserId(String userId, SingleObserver<UserModel> singleObserver) {
        repository.queryUserByUserId(userId, singleObserver);
    }

    public void queryUserByAccessToken(String accessToken, SingleObserver<UserModel> singleObserver) {
        repository.queryUserByAccessToken(accessToken, singleObserver);
    }

    public void insertUser(UserModel userModel) {
        repository.insertUser(userModel);
    }

    public void updateUser(UserModel userModel) {
        repository.updateUser(userModel);
    }

    public void deleteUser(String userId) {
        repository.deleteUser(userId);
    }
}
