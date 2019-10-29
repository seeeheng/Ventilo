package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;

@Dao
public interface MapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createMap(MapModel user);

//    @Update
//    void updateUserModel(UserModel user);
//
//    @Query("DELETE FROM User WHERE userId = :userId")
//    void deleteUserModel(String userId);

    @Query("DELETE FROM Map")
    void deleteAllMaps();

    @Query("SELECT * FROM Map")
    LiveData<List<MapModel>> getAllMapLiveData();

//    @Query("SELECT * FROM User WHERE userId = :userId")
//    LiveData<UserModel> getCurrentUserLiveData(String userId);
//
    @Query("SELECT * FROM Map")
    List<MapModel> getAllMaps();
//
//    @Query("SELECT * FROM User WHERE userId = :userId")
//    Single<UserModel> getUserByUserId(String userId);
//
//    @Query("SELECT * FROM User WHERE accessToken = :accessToken")
//    Single<UserModel> getUserByAccessToken(String accessToken);
//
//    @Query("SELECT * FROM User WHERE userId = :userId")
//    Single<UserModel> getUserByUserIP(String userId);
}
