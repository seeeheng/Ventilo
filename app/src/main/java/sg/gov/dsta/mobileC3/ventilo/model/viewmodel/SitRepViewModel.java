package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;

/**
 * Sit Rep view model class provides an interface to execute background tasks
 */
public class SitRepViewModel extends AndroidViewModel {

    private SitRepRepository repository;
    private LiveData<List<SitRepModel>> mAllSitRepsLiveData;

    public SitRepViewModel(Application application) {
        super(application);
        repository = new SitRepRepository(application);
        mAllSitRepsLiveData = repository.getAllSitRepsLiveData();
    }

    public LiveData<List<SitRepModel>> getAllSitRepsLiveData() {
        return mAllSitRepsLiveData;
    }

    public void getAllSitReps(SingleObserver<SitRepModel> singleObserver) {
        repository.getAllSitReps(singleObserver);
    }

    public void insertSitRepWithObserver(SitRepModel sitRepModel, SingleObserver singleObserver) {
        repository.insertSitRepWithObserver(sitRepModel, singleObserver);
    }

    public void insertSitRep(SitRepModel sitRepModel) {
        repository.insertSitRep(sitRepModel);
    }

    public void querySitRepBySitRepId(long sitRepId, SingleObserver<SitRepModel> singleObserver) {
        repository.querySitRepById(sitRepId, singleObserver);
    }

    public void updateSitRep(SitRepModel sitRepModel) {
        repository.updateSitRep(sitRepModel);
    }

    public void updateSitRepByRefId(SitRepModel sitRepModel) {
        repository.updateSitRepByRefId(sitRepModel);
    }

    public void deleteSitRep(long sitRepId) {
        repository.deleteSitRep(sitRepId);
    }

    public void deleteSitRepByRefId(long sitRepRefId) {
        repository.deleteSitRepByRefId(sitRepRefId);
    }
}
