package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;
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

    @Query("SELECT * FROM User WHERE userId = :userId")
    LiveData<UserModel> getCurrentUserLiveData(String userId);

    @Query("SELECT * FROM User")
    List<UserModel> getAllUsers();

    @Query("SELECT * FROM User WHERE userId = :userId")
    Single<UserModel> getUserByUserId(String userId);

    @Query("SELECT * FROM User WHERE accessToken = :accessToken")
    Single<UserModel> getUserByAccessToken(String accessToken);

    @Query("SELECT * FROM User WHERE userId = :userId")
    Single<UserModel> getUserByUserIP(String userId);
}
