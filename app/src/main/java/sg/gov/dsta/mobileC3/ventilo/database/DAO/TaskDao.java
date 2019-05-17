package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM Task WHERE id = :taskId")
    Single<TaskModel> queryTaskById(long taskId);

    @Query("SELECT * FROM Task WHERE refId = :taskId")
    Single<TaskModel> queryTaskByRefId(long taskId);

    @Insert
    Long insertTaskModel(TaskModel task);

    @Update
    void updateTaskModel(TaskModel task);

    @Query("UPDATE Task SET assignedTo = :assignedTo, assignedBy = :assignedBy," +
            "title = :title, description = :description, status = :status," +
            "createdDateTime = :createdDateTime WHERE refId = :refId")
    void updateTaskModelByRefId(String assignedTo, String assignedBy, String title,
                                String description, String status, String createdDateTime,
                                long refId);

    @Query("DELETE FROM Task WHERE id = :taskId")
    void deleteTaskModel(long taskId);

    @Query("DELETE FROM Task WHERE refId = :taskRefId")
    void deleteTaskModelByRefId(long taskRefId);

    @Query("SELECT * FROM Task")
    LiveData<List<TaskModel>> getAllTasksLiveData();

    @Query("SELECT * FROM Task")
    List<TaskModel> getAllTasks();
}
