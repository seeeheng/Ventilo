package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.UserTaskJoinRepository;

public class UserTaskJoinViewModel extends AndroidViewModel {

    private UserTaskJoinRepository repository;

    public UserTaskJoinViewModel(Application application) {
        super(application);
        repository = new UserTaskJoinRepository(application);
    }

    public void addUserTaskJoin(UserTaskJoinModel userTaskJoinModel) {
        repository.addUserTaskJoin(userTaskJoinModel);
    }

    public void queryTasksForUser(String userId, SingleObserver<List<TaskModel>> singleObserver) {
        repository.queryTasksForUser(userId, singleObserver);
    }

    public void queryUsersForTask(long taskId, SingleObserver<List<UserModel>> singleObserver) {
        repository.queryUsersForTask(taskId, singleObserver);
    }
}
