package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;

public class TaskViewModel extends AndroidViewModel {

    private TaskRepository repository;
    private LiveData<List<TaskModel>> mAllTasksLiveData;

    public TaskViewModel(Application application) {
        super(application);
        repository = new TaskRepository(application);
        mAllTasksLiveData = repository.getAllTasksLiveData();
    }

    public LiveData<List<TaskModel>> getAllTasksLiveData() {
        return mAllTasksLiveData;
    }

    public void getAllTasks(SingleObserver<List<TaskModel>> singleObserver) {
        repository.getAllTasks(singleObserver);
    }

    public void insertTaskWithObserver(TaskModel taskModel, SingleObserver singleObserver) {
        repository.insertTaskWithObserver(taskModel, singleObserver);
    }

    public void insertTask(TaskModel taskModel) {
        repository.insertTask(taskModel);
    }

    public void updateTask(TaskModel taskModel) {
        repository.updateTask(taskModel);
    }

    public void updateTaskByRefId(TaskModel taskModel) {
        repository.updateTaskByRefId(taskModel);
    }

    public void deleteTask(long taskId) {
        repository.deleteTask(taskId);
    }

    public void deleteTaskByRefId(long taskRefId) {
        repository.deleteTaskByRefId(taskRefId);
    }
}
