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
import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.SitRepDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class SitRepRepository {

    private LiveData<List<SitRepModel>> mAllSitRepsLiveData;

    private SitRepDao mSitRepDao;

    public SitRepRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mSitRepDao = db.sitRepDao();
        mAllSitRepsLiveData = mSitRepDao.getAllSitRepsLiveData();
    }

    /**
     * Obtains all Sit Rep models from database
     *
     * @return
     */
    public LiveData<List<SitRepModel>> getAllSitRepsLiveData() {
        return mAllSitRepsLiveData;
    }

    /**
     * Obtains all Sit Rep models from database
     *
     * @param singleObserver
     */
    public void getAllSitReps(SingleObserver singleObserver) {
        QueryAllSitRepsAsyncTask task = new
                QueryAllSitRepsAsyncTask(mSitRepDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

//    /**
//     * Obtain Sit Rep based on Id with Id from local database
//     *
//     * @param sitRepId
//     * @param singleObserver
//     */
//    public void querySitRepById(long sitRepId, SingleObserver<SitRepModel> singleObserver) {
//        Single<SitRepModel> single = mSitRepDao.querySitRepById(sitRepId);
//        single.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(singleObserver);
//    }

    /**
     * Obtain Sit Rep based on Ref Id with Id from local database
     *
     * @param sitRepId
     * @param singleObserver
     */
    public void querySitRepByRefId(long sitRepId, SingleObserver<SitRepModel> singleObserver) {
        Single<SitRepModel> single = mSitRepDao.querySitRepByRefId(sitRepId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Obtain Sit Rep based on created date and time from local database
     *
     * @param createdDateTime
     * @param singleObserver
     */
    public void querySitRepByCreatedDateTime(String createdDateTime, SingleObserver<SitRepModel> singleObserver) {
        Single<SitRepModel> single = mSitRepDao.querySitRepByCreatedDateTime(createdDateTime);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Insert Sit Rep model in local database with updates to Observer
     * @param sitRepModel
     * @param singleObserver
     */
    public void insertSitRepWithObserver(SitRepModel sitRepModel, SingleObserver singleObserver) {
        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mSitRepDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Insert Sit Rep model into local database
     *
     * @param sitRepModel
     */
    public void insertSitRep(SitRepModel sitRepModel) {
        InsertAsyncTask task = new InsertAsyncTask(mSitRepDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);

//        AsyncParallelTask.executeTask(task, sitRepModel);
    }

    /**
     * Update Sit Rep model by Id with Id in local database
     *
     * @param sitRepModel
     */
    public void updateSitRep(SitRepModel sitRepModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mSitRepDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Update Sit Rep model by Ref Id with Id in local database
     *
     * @param sitRepModel
     */
    public void updateSitRepByRefId(SitRepModel sitRepModel) {
        UpdateByRefIdAsyncTask task = new UpdateByRefIdAsyncTask(mSitRepDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Delete Sit Rep model by Id with Id from local database
     *
     * @param sitRepId
     */
    public void deleteSitRep(long sitRepId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mSitRepDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Delete Sit Rep model by Ref Id with Id from local database
     *
     * @param sitRepId
     */
    public void deleteSitRepByRefId(long sitRepId) {
        DeleteByRefIdAsyncTask task = new DeleteByRefIdAsyncTask(mSitRepDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(sitRepId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Query all Sit Rep models in database
     *
     */
    private static class QueryAllSitRepsAsyncTask extends AsyncTask<String, Void, Void> {

        private SitRepDao asyncSitRepDao;
        private SingleObserver asyncSingleObserver;

        QueryAllSitRepsAsyncTask(SitRepDao dao, SingleObserver singleObserver) {
            asyncSitRepDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<SitRepModel> allSitRepsList =
                    asyncSitRepDao.getAllSitReps();

            if (allSitRepsList == null) {
                allSitRepsList = new ArrayList<>();
            }

            Observable<List<SitRepModel>> observableAllSitReps =
                    Observable.just(allSitRepsList);
            Single<List<SitRepModel>> singleAllSitReps =
                    Single.fromObservable(observableAllSitReps);

            singleAllSitReps.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    /**
     * Execute insertion of Sit Rep model into database as a background task, and sends
     * Task Id to subscribed Observer object
     */
    private static class InsertWithObserverAsyncTask extends AsyncTask<SitRepModel, Void, Void> {

        private SitRepDao asyncSitRepDao;
        private SingleObserver asyncSingleObserver;

        InsertWithObserverAsyncTask(SitRepDao dao, SingleObserver singleObserver) {
            asyncSitRepDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final SitRepModel... sitRepModel) {
            // Converts type Long to Observable, then to Single for RxJava use
            Long insertedSitRepId = asyncSitRepDao.insertSitRepModel(sitRepModel[0]);
            Observable<Long> observableInsertedSitRepId = Observable.just(insertedSitRepId);
            Single<Long> singleInsertedSitRepId = Single.fromObservable(observableInsertedSitRepId);

            singleInsertedSitRepId.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    /**
     * Execute insertion of Sit Rep model into database as a background task
     */
    private static class InsertAsyncTask extends AsyncTask<SitRepModel, Void, Void> {

        private SitRepDao asyncSitRepDao;

        InsertAsyncTask(SitRepDao dao) {
            asyncSitRepDao = dao;
        }

        @Override
        protected Void doInBackground(final SitRepModel... sitRepModel) {
            asyncSitRepDao.insertSitRepModel(sitRepModel[0]);
            return null;
        }
    }

    /**
     * Execute update of Sit Rep model in database as a background task
     */
    private static class UpdateAsyncTask extends AsyncTask<SitRepModel, Void, Void> {

        private SitRepDao asyncSitRepDao;

        UpdateAsyncTask(SitRepDao dao) {
            asyncSitRepDao = dao;
        }

        @Override
        protected Void doInBackground(final SitRepModel... sitRepModel) {
            asyncSitRepDao.updateSitRepModel(sitRepModel[0]);
            return null;
        }
    }

    /**
     * Execute update of Sit Rep model by Ref Id in database as a background task
     */
    private static class UpdateByRefIdAsyncTask extends AsyncTask<SitRepModel, Void, Void> {

        private SitRepDao asyncSitRepDao;

        UpdateByRefIdAsyncTask(SitRepDao dao) {
            asyncSitRepDao = dao;
        }

        @Override
        protected Void doInBackground(final SitRepModel... sitRepModel) {
            SitRepModel sitRepModelToUpdate = sitRepModel[0];
            asyncSitRepDao.updateSitRepModelByRefId(sitRepModelToUpdate.getId(),
                    sitRepModelToUpdate.getReporter(), sitRepModelToUpdate.getSnappedPhoto(),
                    sitRepModelToUpdate.getLocation(), sitRepModelToUpdate.getActivity(),
                    sitRepModelToUpdate.getPersonnelT(), sitRepModelToUpdate.getPersonnelS(),
                    sitRepModelToUpdate.getPersonnelD(), sitRepModelToUpdate.getNextCoa(),
                    sitRepModelToUpdate.getRequest(), sitRepModelToUpdate.getOthers(),
                    sitRepModelToUpdate.getCreatedDateTime(), sitRepModelToUpdate.getLastUpdatedDateTime(),
                    sitRepModelToUpdate.getIsValid());

            return null;
        }
    }

    /**
     * Execute deletion of Sit Rep model from database as a background task
     */
    private static class DeleteAsyncTask extends AsyncTask<Long, Void, Void> {

        private SitRepDao asyncSitRepDao;

        DeleteAsyncTask(SitRepDao dao) {
            asyncSitRepDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... sitRepId) {
            asyncSitRepDao.deleteSitRepModel(sitRepId[0]);
            return null;
        }
    }

    /**
     * Execute deletion of Sit Rep model by Ref Id from database as a background task
     */
    private static class DeleteByRefIdAsyncTask extends AsyncTask<Long, Void, Void> {

        private SitRepDao asyncSitRepDao;

        DeleteByRefIdAsyncTask(SitRepDao dao) {
            asyncSitRepDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... sitRepRefId) {
            asyncSitRepDao.deleteSitRepModelByRefId(sitRepRefId[0]);
            return null;
        }
    }
}
