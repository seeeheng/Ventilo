package sg.gov.dsta.mobileC3.ventilo.model.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import io.reactivex.SingleObserver;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.repository.BFTRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.VideoStreamRepository;

public class BFTViewModel extends AndroidViewModel {

    private BFTRepository repository;

    public BFTViewModel(Application application) {
        super(application);
        repository = new BFTRepository(application);
    }

    public void getAllBFTs(SingleObserver singleObserver) {
        repository.getAllBFTs(singleObserver);
    }

    public LiveData<List<BFTModel>> getAllBFTsLiveDataForUser(String userId) {
        return repository.getAllBFTsLiveDataForUser(userId);
    }

//
//    public LiveData<List<String>> getAllVideoStreamNamesLiveDataForUser(String userId) {
//        return repository.getAllVideoStreamNamesLiveDataForUser(userId);
//    }
//
//    public void getAllVideoStreamsForUser(String userId, SingleObserver singleObserver) {
//        repository.getAllVideoStreamsLiveDataForUser(userId, singleObserver);
//    }
//
//    public void getVideoStreamUrlForUserByName(String userId, String videoName,
//                                             SingleObserver singleObserver) {
//        repository.getVideoStreamUrlForUserByName(userId, videoName, singleObserver);
//    }

    public void insertBFT(BFTModel bFTModel) {
        repository.insertBFT(bFTModel);
    }

//    public void insertVideoStream(VideoStreamModel videoStreamModel,
//                                  SingleObserver singleObserver) {
//        repository.insertVideoStreamWithObserver(videoStreamModel, singleObserver);
//    }

    public void updateBFT(BFTModel bFTModel) {
        repository.updateBFT(bFTModel);
    }

    public void deleteBFT(long bFTId) {
        repository.deleteBFT(bFTId);
    }
}
