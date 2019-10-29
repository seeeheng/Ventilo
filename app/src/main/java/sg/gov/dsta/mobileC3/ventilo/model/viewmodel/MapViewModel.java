package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.repository.MapRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;

public class MapViewModel extends AndroidViewModel {

    private MapRepository repository;
    private LiveData<List<MapModel>> mAllMapLiveData;

    public MapViewModel(Application application) {
        super(application);
        repository = new MapRepository(application);
        mAllMapLiveData = repository.getAllMapLiveData();
    }

    public LiveData<List<MapModel>> getAllMapLiveData() {
        return mAllMapLiveData;
    }

    public void getAllMaps(SingleObserver<List<MapModel>> singleObserver) {
        repository.getAllMaps(singleObserver);
    }

    public void insertMap(MapModel mapModel) {
        repository.insertMap(mapModel);
    }

    public void deleteAllMaps() {
        repository.deleteAllMaps();
    }
}
