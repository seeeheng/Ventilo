package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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

    @Query("SELECT * FROM BFT WHERE userId = :userId AND type = :type")
    Single<List<BFTModel>> getBFTByUserIdAndType(String userId, String type);

    @Query("SELECT * FROM BFT WHERE userId = :userId AND type = :type " +
            "AND createdDateTime = :createdDateTime")
    Single<List<BFTModel>> getBFTByUserIdAndTypeAndCreatedDateTime(String userId,
                                                                   String type,
                                                                   String createdDateTime);

//    @Query("SELECT * FROM BFT WHERE userId = :userId")
//    List<BFTModel> getAllBFTsForUser(String userId);

//    @Query("SELECT name FROM VideoStream WHERE userId = :userId")
//    LiveData<List<String>> getAllVideoStreamNamesLiveDataForUser(String userId);

//    @Query("SELECT url FROM VideoStream WHERE userId = :userId AND name = :name")
//    String getVideoStreamUrlForUserByName(String userId, String name);
}
