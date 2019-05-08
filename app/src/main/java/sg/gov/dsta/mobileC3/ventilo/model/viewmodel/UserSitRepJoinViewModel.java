package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.UserSitRepJoinRepository;

public class UserSitRepJoinViewModel extends AndroidViewModel {

    private UserSitRepJoinRepository repository;

    public UserSitRepJoinViewModel(Application application) {
        super(application);
        repository = new UserSitRepJoinRepository(application);
    }

    public void addUserSitRepJoin(UserSitRepJoinModel userSitRepJoinModel) {
        repository.addUserSitRepJoin(userSitRepJoinModel);
    }

    public void querySitRepsForUser(String userId, SingleObserver<List<SitRepModel>> singleObserver) {
        repository.querySitRepsForUser(userId, singleObserver);
    }

    public void queryUsersForSitRep(long sitRepId, SingleObserver<List<UserModel>> singleObserver) {
        repository.queryUsersForSitRep(sitRepId, singleObserver);
    }
}
