package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;

@Dao
public interface SitRepDao {

//    @Query("SELECT * FROM SitRep WHERE id = :sitRepId")
//    Single<SitRepModel> querySitRepById(long sitRepId);

    @Query("SELECT * FROM SitRep WHERE refId = :sitRefId")
    Single<SitRepModel> querySitRepByRefId(long sitRefId);

    @Query("SELECT * FROM SitRep WHERE createdDateTime = :createdDateTime")
    Single<SitRepModel> querySitRepByCreatedDateTime(String createdDateTime);

    @Insert
    Long insertSitRepModel(SitRepModel sitRep);

    @Update
    void updateSitRepModel(SitRepModel sitRep);

    @Query("UPDATE SitRep SET reporter = :reporter, snappedPhoto = :snappedPhoto, " +
            "location = :location, activity = :activity, personnelT = :personnelT, " +
            "personnelS = :personnelS, personnelD = :personnelD, nextCoa = :nextCoa, " +
            "request = :request, others = :others, createdDateTime = :reportedDateTime, " +
            "lastUpdatedDateTime = :lastUpdatedDateTime, isValid = :isValid " +
            " WHERE refId = :id")
    void updateSitRepModelByRefId(long id, String reporter, byte[] snappedPhoto, String location,
                                  String activity, int personnelT, int personnelS,
                                  int personnelD, String nextCoa, String request,
                                  String others, String reportedDateTime,
                                  String lastUpdatedDateTime, String isValid);

    @Query("UPDATE SitRep SET isValid = \"No\" WHERE id = :sitRepId")
    void deleteSitRepModel(long sitRepId);

    @Query("UPDATE SitRep SET isValid = \"No\" WHERE refId = :sitRepRefId")
    void deleteSitRepModelByRefId(long sitRepRefId);

//    @Query("DELETE FROM SitRep WHERE id = :sitRepId")
//    void deleteSitRepModel(long sitRepId);

//    @Query("DELETE FROM SitRep WHERE refId = :sitRepRefId")
//    void deleteSitRepModelByRefId(long sitRepRefId);

    @Query("SELECT * FROM SitRep")
    LiveData<List<SitRepModel>> getAllSitRepsLiveData();

    @Query("SELECT * FROM SitRep")
    List<SitRepModel> getAllSitReps();
}
