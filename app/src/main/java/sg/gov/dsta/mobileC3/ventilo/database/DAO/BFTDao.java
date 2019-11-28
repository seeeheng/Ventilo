package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;

@Dao
public interface BFTDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBFTModel(BFTModel bFTModel);

    @Update
    void updateBFTModel(BFTModel bFTModel);

    @Query("DELETE FROM BFT WHERE id = :id")
    void deleteBFTModel(long id);

    @Query("SELECT * FROM BFT")
    LiveData<List<BFTModel>> getAllBFTsLiveData();

    @Query("SELECT * FROM BFT WHERE userId = :userId")
    LiveData<List<BFTModel>> getAllBFTsLiveDataForUser(String userId);

    @Query("SELECT * FROM BFT")
    List<BFTModel> getAllBFTs();

    @Query("SELECT * FROM BFT WHERE id = :id")
    Single<BFTModel> getBFTById(long id);

    @Query("SELECT * FROM BFT WHERE refId = :refId")
    Single<BFTModel> getBFTByRefId(long refId);

    @Query("SELECT * FROM BFT WHERE userId = :userId")
    Single<BFTModel> getBFTByUserId(String userId);

    @Query("SELECT * FROM BFT WHERE userId = :userId AND (type = 'Own' OR type = 'Own-Stale')")
    Single<List<BFTModel>> getBFTByUserIdAndOwnType(String userId);

    @Query("SELECT * FROM BFT WHERE userId = :userId AND type = :type")
    Single<List<BFTModel>> getBFTByUserIdAndType(String userId, String type);

    @Query("SELECT * FROM BFT WHERE userId = :userId AND createdDateTime = :createdDateTime")
    Single<BFTModel> getBFTByUserIdAndCreatedDateTime(String userId,
                                                      String createdDateTime);

//    @Query("SELECT * FROM BFT WHERE userId = :userId")
//    List<BFTModel> getAllBFTsForUser(String userId);

//    @Query("SELECT name FROM VideoStream WHERE userId = :userId")
//    LiveData<List<String>> getAllVideoStreamNamesLiveDataForUser(String userId);

//    @Query("SELECT url FROM VideoStream WHERE userId = :userId AND name = :name")
//    String getVideoStreamUrlForUserByName(String userId, String name);
}
