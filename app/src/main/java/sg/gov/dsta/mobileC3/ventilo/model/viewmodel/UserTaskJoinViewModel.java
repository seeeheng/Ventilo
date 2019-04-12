package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoin;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;

public class UserTaskJoinViewModel extends AndroidViewModel {

    private UserTaskJoinRepository repository;

    public UserTaskJoinViewModel(Application application) {
        super(application);
        repository = new UserTaskJoinRepository(application);
    }

    public void addUserTaskJoin(UserTaskJoin userTaskJoin) {
        repository.addUserTaskJoin(userTaskJoin);
    }

    public void queryTasksForUser(String userId, SingleObserver<List<TaskModel>> singleObserver) {
        repository.queryTasksForUser(userId, singleObserver);
    }

    public void queryUsersForTask(long taskId, SingleObserver<List<UserModel>> singleObserver) {
        repository.queryUsersForTask(taskId, singleObserver);
    }
}
