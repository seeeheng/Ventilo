package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;

public class WaveRelayRadioViewModel extends AndroidViewModel {

    private WaveRelayRadioRepository repository;

    public WaveRelayRadioViewModel(Application application) {
        super(application);
        repository = new WaveRelayRadioRepository(application);
    }

    public void getAllWaveRelayRadios(SingleObserver singleObserver) {
        repository.getAllWaveRelayRadios(singleObserver);
    }

    public void queryRadioByRadioId(long radioId, SingleObserver<WaveRelayRadioModel> singleObserver) {
        repository.queryRadioByRadioId(radioId, singleObserver);
    }

    public void insertWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        repository.insertWaveRelayRadio(waveRelayRadioModel);
    }

    public void updateWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        repository.updateWaveRelayRadio(waveRelayRadioModel);
    }

    public void deleteWaveRelayRadio(long waveRelayRadioId) {
        repository.deleteWaveRelayRadio(waveRelayRadioId);
    }
}
