package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;

@Dao
public interface MapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createMap(MapModel mapModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void createMapGroup(MapModel... mapModelGroup);

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
