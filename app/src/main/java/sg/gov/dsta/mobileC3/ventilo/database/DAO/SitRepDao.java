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

    @Insert
    Long insertSitRepModel(SitRepModel sitRep);

    @Query("SELECT * FROM SitRep WHERE id = :sitRepId")
    Single<SitRepModel> querySitRepBySitRepId(long sitRepId);

    @Update
    void updateSitRepModel(SitRepModel sitRep);

    @Query("UPDATE SitRep SET reporter = :reporter, snappedPhoto = :snappedPhoto," +
            "location = :location, activity = :activity, personnelT = :personnelT," +
            "personnelS = :personnelS, personnelD = :personnelD, nextCoa = :nextCoa," +
            "request = :request, reportedDateTime = :reportedDateTime WHERE refId = :refId")
    void updateSitRepModelByRefId(String reporter, byte[] snappedPhoto, String location,
                                  String activity, int personnelT, int personnelS,
                                  int personnelD, String nextCoa, String request,
                                  String reportedDateTime, long refId);

    @Query("DELETE FROM SitRep WHERE id = :sitRepId")
    void deleteSitRepModel(long sitRepId);

    @Query("DELETE FROM SitRep WHERE refId = :sitRepRefId")
    void deleteSitRepModelByRefId(long sitRepRefId);

    @Query("SELECT * FROM SitRep")
    LiveData<List<SitRepModel>> getAllSitReps();
}
