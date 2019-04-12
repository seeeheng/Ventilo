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

//    @Query("INSERT INTO Task DEFAULT VALUES")
    @Insert
    Long insertTaskModel(TaskModel task);

    @Update
    void updateTaskModel(TaskModel task);

    @Query("DELETE FROM Task WHERE id = :taskId")
    void deleteTaskModel(long taskId);

    @Query("SELECT * FROM Task")
    LiveData<List<TaskModel>> getAllTasks();
}
