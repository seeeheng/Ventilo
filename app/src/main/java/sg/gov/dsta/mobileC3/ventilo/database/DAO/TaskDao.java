package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;

@Dao
public interface TaskDao {

//    @Query("SELECT * FROM Task WHERE id = :taskId")
//    Single<TaskModel> queryTaskById(long taskId);

    @Query("SELECT * FROM Task WHERE refId = :taskId")
    Single<TaskModel> queryTaskByRefId(long taskId);

    @Query("SELECT * FROM Task WHERE createdDateTime = :createdDateTime")
    Single<TaskModel> queryTaskByCreatedDateTime(String createdDateTime);

    @Insert
    Long insertTaskModel(TaskModel task);

    @Update
    void updateTaskModel(TaskModel task);

    @Query("UPDATE Task SET phaseNo = :phaseNo, adHocTaskPriority = :adHocTaskPriority, " +
            "assignedTo = :assignedTo, assignedBy = :assignedBy," +
            "title = :title, description = :description, status = :status," +
            "createdDateTime = :createdDateTime, completedDateTime = :completedDateTime, " +
            "lastUpdatedStatusDateTime = :lastUpdatedStatusDateTime, " +
            "lastUpdatedMainDateTime = :lastUpdatedMainDateTime, isValid = :isValid " +
            "WHERE refId = :id")
    void updateTaskModelByRefId(long id, String phaseNo, String adHocTaskPriority, String assignedTo,
                                String assignedBy, String title, String description, String status,
                                String createdDateTime, String completedDateTime, String lastUpdatedStatusDateTime,
                                String lastUpdatedMainDateTime, String isValid);

    @Query("UPDATE Task SET isValid = \"No\" WHERE id = :taskId")
    void deleteTaskModel(long taskId);

    @Query("UPDATE Task SET isValid = \"No\" WHERE refId = :taskRefId")
    void deleteTaskModelByRefId(long taskRefId);

//    @Query("DELETE FROM Task WHERE id = :taskId")
//    void deleteTaskModel(long taskId);

//    @Query("DELETE FROM Task WHERE refId = :taskRefId")
//    void deleteTaskModelByRefId(long taskRefId);

    @Query("SELECT * FROM Task")
    LiveData<List<TaskModel>> getAllTasksLiveData();

    @Query("SELECT * FROM Task")
    List<TaskModel> getAllTasks();
}
