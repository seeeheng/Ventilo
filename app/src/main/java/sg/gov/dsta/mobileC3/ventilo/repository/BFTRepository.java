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
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class BFTRepository {

    private BFTDao mBFTDao;

    public BFTRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mBFTDao = db.bFTDao();
    }

    public LiveData<List<BFTModel>> getAllBFTsLiveData() {
        return mBFTDao.getAllBFTsLiveData();
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

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Get BFT model by Id from database
     *
     * @param id
     * @param singleObserver
     */
    public void queryBFTById(long id, SingleObserver<BFTModel> singleObserver) {
        Single<BFTModel> single = mBFTDao.getBFTById(id);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Get BFT model by Ref Id from database
     *
     * @param refId
     * @param singleObserver
     */
    public void queryBFTByRefId(long refId, SingleObserver<BFTModel> singleObserver) {
        Single<BFTModel> single = mBFTDao.getBFTByRefId(refId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Get Own Type BFT model from database
     *
     * @param userId
     * @param type
     * @param singleObserver
     */
    public void queryBFTByUserIdAndType(String userId, String type,
                                        SingleObserver<List<BFTModel>> singleObserver) {
        Single<List<BFTModel>> single = mBFTDao.getBFTByUserIdAndType(userId, type);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Get existing BFT model from database based on userId, type and created date & time
     *
     * @param userId
     * @param type
     * @param createdDateTime
     * @param singleObserver
     */
    public void queryBFTByUserIdAndTypeAndCreatedDateTime(String userId, String type,
                                                          String createdDateTime,
                                                          SingleObserver<List<BFTModel>> singleObserver) {
        Single<List<BFTModel>> single = mBFTDao.getBFTByUserIdAndTypeAndCreatedDateTime(userId,
                type, createdDateTime);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    public void insertBFT(BFTModel bFTModel) {
        InsertAsyncTask task = new InsertAsyncTask(mBFTDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(bFTModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void insertBFTWithObserver(BFTModel bFTModel, SingleObserver singleObserver) {
        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mBFTDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(bFTModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void updateBFT(BFTModel bFTModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mBFTDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(bFTModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void deleteBFT(long bFTId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mBFTDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(bFTId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
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

//    private static class InsertAsyncTask extends AsyncTask<BFTModel, Void, Void> {
//
//        private BFTDao asyncBFTDao;
//
//        InsertAsyncTask(BFTDao dao) {
//            asyncBFTDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(final BFTModel... bFTModel) {
//            asyncBFTDao.insertBFTModel(bFTModel[0]);
//            return null;
//        }
//    }

    /**
     * Execute insertion of Bft model into database as a background task
     */
    private static class InsertAsyncTask extends AsyncTask<BFTModel, Void, Void> {

        private BFTDao asyncBFTDao;

        InsertAsyncTask(BFTDao dao) {
            asyncBFTDao = dao;
        }

        @Override
        protected Void doInBackground(final BFTModel... bftModel) {
            asyncBFTDao.insertBFTModel(bftModel[0]);
            return null;
        }
    }

    /**
     * Execute insertion of Bft model into database as a background task, and sends
     * id to subscribed Observer object
     */
    private static class InsertWithObserverAsyncTask extends AsyncTask<BFTModel, Void, Void> {

        private BFTDao asyncBFTDao;
        private SingleObserver asyncSingleObserver;

        InsertWithObserverAsyncTask(BFTDao dao, SingleObserver singleObserver) {
            asyncBFTDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final BFTModel... bftModel) {
            // Converts type Long to Observable, then to Single for RxJava use
            Long insertedBftId = asyncBFTDao.insertBFTModel(bftModel[0]);
            Observable<Long> observableInsertedBftId = Observable.just(insertedBftId);
            Single<Long> singleInsertedBftId = Single.fromObservable(observableInsertedBftId);

            singleInsertedBftId.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

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
