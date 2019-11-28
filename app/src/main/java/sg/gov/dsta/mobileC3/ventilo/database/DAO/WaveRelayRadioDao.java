package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;

@Dao
public interface WaveRelayRadioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWaveRelayRadioModel(WaveRelayRadioModel waveRelayRadioModel);

    @Update
    void updateWaveRelayRadioModel(WaveRelayRadioModel waveRelayRadioModel);

    @Query("DELETE FROM WaveRelayRadio WHERE radioId = :radioId")
    void deleteWaveRelayRadioModel(long radioId);

    @Query("SELECT * FROM WaveRelayRadio")
    List<WaveRelayRadioModel> getAllWaveRelayRadios();

    @Query("SELECT * FROM WaveRelayRadio WHERE radioId = :radioId")
    Single<WaveRelayRadioModel> getRadioByRadioId(long radioId);

    @Query("SELECT * FROM WaveRelayRadio WHERE userId = :userId")
    Single<WaveRelayRadioModel> getRadioByUserId(String userId);

    @Query("SELECT * FROM WaveRelayRadio WHERE radioIpAddress = :radioIpAddress")
    Single<WaveRelayRadioModel> getRadioByRadioIPAddress(String radioIpAddress);
}
