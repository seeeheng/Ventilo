package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Dao
public interface UserTaskJoinDao {

    @Query("SELECT Task.* FROM Task\n"+
            "INNER JOIN UserTaskJoin ON Task.id=UserTaskJoin.taskId\n"+
            "WHERE UserTaskJoin.userId=:userId")
    Single<List<TaskModel>> queryTasksForUser(String userId);

    @Query("SELECT User.* FROM User\n"+
            "INNER JOIN UserTaskJoin ON User.userId=UserTaskJoin.userId\n"+
            "WHERE UserTaskJoin.taskId=:taskId")
    Single<List<UserModel>> queryUsersForTask(long taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserTaskJoin(UserTaskJoinModel userTaskJoinModel);

//    @Update
//    long updateUserTaskJoin(UserTaskJoinModel userTaskJoin);
//
//    @Query("DELETE FROM UserTaskJoinModel WHERE taskId = :taskId")
//    int deleteUsersForTask(long taskId);
}
