package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import sg.gov.dsta.mobileC3.ventilo.repository.MainRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;

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
