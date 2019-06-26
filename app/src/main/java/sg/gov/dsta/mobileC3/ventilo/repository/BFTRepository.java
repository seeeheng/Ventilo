package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.BFTDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;

public class BFTRepository {

    private BFTDao mBFTDao;

    public BFTRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mBFTDao = db.bFTDao();
    }

    public LiveData<List<BFTModel>> getAllBFTsLiveDataForUser(String userId) {
        return mBFTDao.getAllBFTsLiveDataForUser(userId);
    }

//    public LiveData<List<String>> getAllVideoStreamNamesLiveDataForUser(String userId) {
//        return mBFTDao.getAllVideoStreamNamesLiveDataForUser(userId);
//    }

    public void getAllBFTs(SingleObserver singleObserver) {
        QueryAllBFTsAsyncTask task = new
                QueryAllBFTsAsyncTask(mBFTDao, singleObserver);
        task.execute();
    }

//    public void getAllBFTsLiveDataForUser(
//            String userId, SingleObserver singleObserver) {
//        QueryAllBFTsForUserAsyncTask task = new
//                QueryAllBFTsForUserAsyncTask(mBFTDao, singleObserver);
//        task.execute(userId);
//    }

//    public void getVideoStreamUrlForUserByName(
//            String userId, String videoName, SingleObserver singleObserver) {
//        String[] paramsForGetUrl = {userId, videoName};
//        QueryVideoStreamForUserByNameAsyncTask task = new
//                QueryVideoStreamForUserByNameAsyncTask(mBFTDao, singleObserver);
//        task.execute(paramsForGetUrl);
//    }

//    public void insertVideoStreamWithObserver(VideoStreamModel videoStreamModel,
//                                              SingleObserver singleObserver) {
//        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mBFTDao, singleObserver);
//        task.execute(videoStreamModel);
//    }

    public void insertBFT(BFTModel bFTModel) {
        InsertAsyncTask task = new InsertAsyncTask(mBFTDao);
        task.execute(bFTModel);
    }

    public void updateBFT(BFTModel bFTModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mBFTDao);
        task.execute(bFTModel);
    }

    public void deleteBFT(long bFTId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mBFTDao);
        task.execute(bFTId);
    }

    private static class QueryAllBFTsAsyncTask extends AsyncTask<String, Void, Void> {

        private BFTDao asyncBFTDao;
        private SingleObserver asyncSingleObserver;

        QueryAllBFTsAsyncTask(BFTDao dao, SingleObserver singleObserver) {
            asyncBFTDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<BFTModel> allBFTsList =
                    asyncBFTDao.getAllBFTs();

            if (allBFTsList == null) {
                allBFTsList = new ArrayList<>();
            }

            Observable<List<BFTModel>> observableAllBFTs =
                    Observable.just(allBFTsList);
            Single<List<BFTModel>> singleAllBFTs =
                    Single.fromObservable(observableAllBFTs);

            singleAllBFTs.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

//    private static class QueryAllBFTsForUserAsyncTask extends
//            AsyncTask<String, Void, Void> {
//
//        private BFTDao asyncBFTDao;
//        private SingleObserver asyncSingleObserver;
//
//        QueryAllBFTsForUserAsyncTask(BFTDao dao, SingleObserver singleObserver) {
//            asyncBFTDao = dao;
//            asyncSingleObserver = singleObserver;
//        }
//
//        @Override
//        protected Void doInBackground(final String... userId) {
//            // Converts type Long to Observable, then to Single for RxJava use
//            List<BFTModel> allBFTsForUser =
//                    asyncBFTDao.getAllBFTsForUser(userId[0]);
//
//            if (allBFTsForUser == null) {
//                allBFTsForUser = new ArrayList<>();
//            }
//
//            Observable<List<BFTModel>> observableAllBFTForUser =
//                    Observable.just(allBFTsForUser);
//            Single<List<BFTModel>> singleAllBFTForUser =
//                    Single.fromObservable(observableAllBFTForUser);
//
//            singleAllBFTForUser.subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(asyncSingleObserver);
//
//            return null;
//        }
//    }

//    private static class QueryVideoStreamForUserByNameAsyncTask extends
//            AsyncTask<String, Void, Void> {
//
//        private VideoStreamDao asyncBFTDao;
//        private SingleObserver asyncSingleObserver;
//
//        QueryVideoStreamForUserByNameAsyncTask(VideoStreamDao dao, SingleObserver singleObserver) {
//            asyncBFTDao = dao;
//            asyncSingleObserver = singleObserver;
//        }
//
//        @Override
//        protected Void doInBackground(final String... paramForGetUrl) {
//            // Converts type Long to Observable, then to Single for RxJava use
//            String videoStreamUrl = asyncBFTDao.
//                    getVideoStreamUrlForUserByName(paramForGetUrl[0], paramForGetUrl[1]);
//
//            if (videoStreamUrl != null) {
//                Observable<String> observableVideoStreamUrl =
//                        Observable.just(videoStreamUrl);
//                Single<String> singleVideoStreamUrl =
//                        Single.fromObservable(observableVideoStreamUrl);
//
//                singleVideoStreamUrl.subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(asyncSingleObserver);
//            }
//
//            return null;
//        }
//    }

//    private static class InsertWithObserverAsyncTask extends AsyncTask<BFTModel, Void, Void> {
//
//        private BFTDao asyncBFTDao;
//        private SingleObserver asyncSingleObserver;
//
//        InsertWithObserverAsyncTask(BFTDao dao, SingleObserver singleObserver) {
//            asyncBFTDao = dao;
//            asyncSingleObserver = singleObserver;
//        }
//
//        @Override
//        protected Void doInBackground(final BFTModel... bFTModel) {
//            // Converts type Long to Observable, then to Single for RxJava use
//            Long insertedBFTId = asyncBFTDao.
//                    insertBFTModel(bFTModel[0]);
//            Observable<Long> observableInsertedBFTId =
//                    Observable.just(insertedBFTId);
//            Single<Long> singleInsertedBFTId =
//                    Single.fromObservable(observableInsertedBFTId);
//
//            singleInsertedBFTId.subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(asyncSingleObserver);
//
//            return null;
//        }
//    }

    private static class InsertAsyncTask extends AsyncTask<BFTModel, Void, Void> {

        private BFTDao asyncBFTDao;

        InsertAsyncTask(BFTDao dao) {
            asyncBFTDao = dao;
        }

        @Override
        protected Void doInBackground(final BFTModel... bFTModel) {
            asyncBFTDao.insertBFTModel(bFTModel[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<BFTModel, Void, Void> {

        private BFTDao asyncBFTDao;

        UpdateAsyncTask(BFTDao dao) {
            asyncBFTDao = dao;
        }

        @Override
        protected Void doInBackground(final BFTModel... bFTModel) {
            asyncBFTDao.updateBFTModel(bFTModel[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Long, Void, Void> {

        private BFTDao asyncBFTDao;

        DeleteAsyncTask(BFTDao dao) {
            asyncBFTDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... bFTId) {
            asyncBFTDao.deleteBFTModel(bFTId[0]);
            return null;
        }
    }
}
