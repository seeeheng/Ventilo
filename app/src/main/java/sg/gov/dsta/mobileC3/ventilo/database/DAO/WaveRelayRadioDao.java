package sg.gov.dsta.mobileC3.ventilo.database.DAO;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
}
