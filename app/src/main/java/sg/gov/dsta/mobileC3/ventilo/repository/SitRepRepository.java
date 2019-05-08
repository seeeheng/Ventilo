package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Query;
import android.os.AsyncTask;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.SitRepDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;

public class SitRepRepository {

    private LiveData<List<SitRepModel>> mAllSitReps;

    private SitRepDao mSitRepDao;

    public SitRepRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mSitRepDao = db.sitRepDao();
        mAllSitReps = mSitRepDao.getAllSitReps();
    }

    /**
     * Obtains all Sit Rep models from database
     * @return
     */
    public LiveData<List<SitRepModel>> getAllSitReps() {
        return mAllSitReps;
    }

    /**
     * Insert Sit Rep model in local database with updates to Observer
     * @param sitRepModel
     * @param singleObserver
     */
    public void addSitRep(SitRepModel sitRepModel, SingleObserver singleObserver) {
        InsertWithObserverAsyncTask task = new InsertWithObserverAsyncTask(mSitRepDao, singleObserver);
        task.execute(sitRepModel);
    }

    /**
     * Insert Sit Rep model into local database
     * @param sitRepModel
     */
    public void insertSitRep(SitRepModel sitRepModel) {
        InsertAsyncTask task = new InsertAsyncTask(mSitRepDao);
        task.execute(sitRepModel);
    }

    /**
     * Obtain Sit Rep based on Sit Rep Id from local database
     * @param sitRepId
     * @param singleObserver
     */
    public void querySitRepBySitRepId(long sitRepId, SingleObserver<SitRepModel> singleObserver) {
        Single<SitRepModel> single = mSitRepDao.querySitRepBySitRepId(sitRepId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Update Sit Rep model in local database
     * @param sitRepModel
     */
    public void updateSitRep(SitRepModel sitRepModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mSitRepDao);
        task.execute(sitRepModel);
    }

    /**
     * Update Sit Rep model by Ref Id in local database
     * @param sitRepModel
     */
    public void updateSitRepByRefId(SitRepModel sitRepModel) {
        UpdateByRefIdAsyncTask task = new UpdateByRefIdAsyncTask(mSitRepDao);
        task.execute(sitRepModel);
    }

    /**
     * Delete Sit Rep model from local database
     * @param sitRepId
     */
    public void deleteSitRep(long sitRepId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mSitRepDao);
        task.execute(sitRepId);
    }

    /**
     * Delete Sit Rep model By Ref Id from local database
     * @param sitRepRefId
     */
    public void deleteSitRepByRefId(long sitRepRefId) {
        DeleteByRefIdAsyncTask task = new DeleteByRefIdAsyncTask(mSitRepDao);
        task.execute(sitRepRefId);
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
            asyncSitRepDao.updateSitRepModelByRefId(sitRepModelToUpdate.getReporter(),
                    sitRepModelToUpdate.getSnappedPhoto(), sitRepModelToUpdate.getLocation(),
                    sitRepModelToUpdate.getActivity(), sitRepModelToUpdate.getPersonnelT(),
                    sitRepModelToUpdate.getPersonnelS(), sitRepModelToUpdate.getPersonnelD(),
                    sitRepModelToUpdate.getNextCoa(), sitRepModelToUpdate.getRequest(),
                    sitRepModelToUpdate.getReportedDateTime(), sitRepModelToUpdate.getRefId());

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
