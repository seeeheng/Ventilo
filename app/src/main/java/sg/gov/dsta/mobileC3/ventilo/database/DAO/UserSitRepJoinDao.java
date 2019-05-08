package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Dao
public interface UserSitRepJoinDao {

    @Query("SELECT SitRep.* FROM SitRep\n"+
            "INNER JOIN UserSitRepJoin ON SitRep.id=UserSitRepJoin.sitRepId\n"+
            "WHERE UserSitRepJoin.userId=:userId")
    Single<List<SitRepModel>> querySitRepsForUser(String userId);

    @Query("SELECT User.* FROM User\n"+
            "INNER JOIN UserSitRepJoin ON User.userId=UserSitRepJoin.userId\n"+
            "WHERE UserSitRepJoin.sitRepId=:sitRepId")
    Single<List<UserModel>> queryUsersForSitRep(long sitRepId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserSitRepJoin(UserSitRepJoinModel userSitRepJoinModel);

//    @Update
//    long updateUserTaskJoin(UserTaskJoinModel userTaskJoin);
//
//    @Query("DELETE FROM UserTaskJoinModel WHERE taskId = :taskId")
//    int deleteUsersForTask(long taskId);
}
