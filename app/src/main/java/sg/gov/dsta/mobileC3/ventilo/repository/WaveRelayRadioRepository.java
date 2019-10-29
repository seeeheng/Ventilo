package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.WaveRelayRadioDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class WaveRelayRadioRepository {

    private WaveRelayRadioDao mWaveRelayRadioDao;

    public WaveRelayRadioRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mWaveRelayRadioDao = db.waveRelayRadioDao();
    }

    /**
     * Obtains all WaveRelay Radio models from database
     *
     * @param singleObserver
     */
    public synchronized void getAllWaveRelayRadios(SingleObserver singleObserver) {
        QueryAllWaveRelayRadiosAsyncTask task = new
                QueryAllWaveRelayRadiosAsyncTask(mWaveRelayRadioDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Obtain WaveRelay Radio model based on Radio Id (Number) from local database
     *
     * @param radioId
     * @param singleObserver
     */
    public synchronized void queryRadioByRadioId(long radioId, SingleObserver<WaveRelayRadioModel> singleObserver) {
        Single<WaveRelayRadioModel> single = mWaveRelayRadioDao.getRadioByRadioId(radioId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Obtain WaveRelay Radio model based on User Id from local database
     *
     * @param userId
     * @param singleObserver
     */
    public synchronized void queryRadioByUserId(String userId, SingleObserver<WaveRelayRadioModel> singleObserver) {
        Single<WaveRelayRadioModel> single = mWaveRelayRadioDao.getRadioByUserId(userId);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Obtain WaveRelay Radio model based on Radio IP Address from local database
     *
     * @param radioIPAddress
     * @param singleObserver
     */
    public synchronized void queryRadioByRadioIPAddress(String radioIPAddress, SingleObserver<WaveRelayRadioModel> singleObserver) {
        Single<WaveRelayRadioModel> single = mWaveRelayRadioDao.getRadioByRadioIPAddress(radioIPAddress);
        single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(singleObserver);
    }

    /**
     * Insert WaveRelay Radio model into local database
     *
     * @param waveRelayRadioModel
     */
    public synchronized void insertWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        InsertAsyncTask task = new InsertAsyncTask(mWaveRelayRadioDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(waveRelayRadioModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Update WaveRelay Radio model by Id in local database
     *
     * @param waveRelayRadioModel
     */
    public synchronized void updateWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mWaveRelayRadioDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(waveRelayRadioModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Delete WaveRelay Radio model based on Radio Id (Number) from local database
     *
     * @param radioId
     */
    public synchronized void deleteWaveRelayRadio(long radioId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mWaveRelayRadioDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(radioId);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    private static class QueryAllWaveRelayRadiosAsyncTask extends
            AsyncTask<String, Void, Void> {

        private WaveRelayRadioDao asyncWaveRelayRadioDao;
        private SingleObserver asyncSingleObserver;

        QueryAllWaveRelayRadiosAsyncTask(WaveRelayRadioDao dao, SingleObserver singleObserver) {
            asyncWaveRelayRadioDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<WaveRelayRadioModel> allWaveRelayRadiosList =
                    asyncWaveRelayRadioDao.getAllWaveRelayRadios();

            if (allWaveRelayRadiosList == null) {
                allWaveRelayRadiosList = new ArrayList<>();
            }

            Observable<List<WaveRelayRadioModel>> observableAllWaveRelayRadios =
                    Observable.just(allWaveRelayRadiosList);
            Single<List<WaveRelayRadioModel>> singleAllWaveRelayRadios =
                    Single.fromObservable(observableAllWaveRelayRadios);

            singleAllWaveRelayRadios.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<WaveRelayRadioModel, Void, Void> {

        private WaveRelayRadioDao asyncWaveRelayRadioDao;

        InsertAsyncTask(WaveRelayRadioDao dao) {
            asyncWaveRelayRadioDao = dao;
        }

        @Override
        protected Void doInBackground(final WaveRelayRadioModel... waveRelayRadioModel) {
            asyncWaveRelayRadioDao.insertWaveRelayRadioModel(waveRelayRadioModel[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<WaveRelayRadioModel, Void, Void> {

        private WaveRelayRadioDao asyncWaveRelayRadioDao;

        UpdateAsyncTask(WaveRelayRadioDao dao) {
            asyncWaveRelayRadioDao = dao;
        }

        @Override
        protected Void doInBackground(final WaveRelayRadioModel... waveRelayRadioModel) {
            asyncWaveRelayRadioDao.updateWaveRelayRadioModel(waveRelayRadioModel[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Long, Void, Void> {

        private WaveRelayRadioDao asyncWaveRelayRadioDao;

        DeleteAsyncTask(WaveRelayRadioDao dao) {
            asyncWaveRelayRadioDao = dao;
        }

        @Override
        protected Void doInBackground(final Long... radioId) {
            asyncWaveRelayRadioDao.deleteWaveRelayRadioModel(radioId[0]);
            return null;
        }
    }
}
