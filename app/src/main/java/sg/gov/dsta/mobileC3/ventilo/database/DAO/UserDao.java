package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createUser(UserModel user);

    @Update
    void updateUserModel(UserModel user);

    @Query("DELETE FROM User WHERE userId = :userId")
    void deleteUserModel(String userId);

    @Query("SELECT * FROM User")
    LiveData<List<UserModel>> getAllUsersLiveData();

    @Query("SELECT * FROM User")
    List<UserModel> getAllUsers();

    @Query("SELECT * FROM User WHERE userId = :userId")
    Single<UserModel> getUserByUserId(String userId);

    @Query("SELECT * FROM User WHERE accessToken = :accessToken")
    Single<UserModel> getUserByAccessToken(String accessToken);
}
