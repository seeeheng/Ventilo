package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;

@Dao
public interface VideoStreamDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertVideoStreamModel(VideoStreamModel videoStreamModel);

    @Update
    void updateVideoStreamModel(VideoStreamModel videoStreamModel);

    @Query("DELETE FROM VideoStream WHERE id = :videoStreamId")
    void deleteVideoStreamModel(long videoStreamId);

    @Query("SELECT * FROM VideoStream WHERE userId = :userId")
    LiveData<List<VideoStreamModel>> getAllVideoStreamsLiveDataForUser(String userId);

    @Query("SELECT * FROM VideoStream")
    List<VideoStreamModel> getAllVideoStreams();

    @Query("SELECT * FROM VideoStream WHERE userId = :userId")
    List<VideoStreamModel> getAllVideoStreamsForUser(String userId);

    @Query("SELECT name FROM VideoStream WHERE userId = :userId")
    LiveData<List<String>> getAllVideoStreamNamesLiveDataForUser(String userId);

    @Query("SELECT url FROM VideoStream WHERE userId = :userId AND name = :name")
    String getVideoStreamUrlForUserByName(String userId, String name);
}
