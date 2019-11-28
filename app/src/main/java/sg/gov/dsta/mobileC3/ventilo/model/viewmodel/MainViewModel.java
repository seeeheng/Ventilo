package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;

import sg.gov.dsta.mobileC3.ventilo.repository.MainRepository;

public class MainViewModel extends AndroidViewModel {

    private MainRepository repository;

    public MainViewModel(Application application) {
        super(application);
        repository = new MainRepository(application);
    }

    public void clearAllData() {
        repository.clearAllData();
    }
}
