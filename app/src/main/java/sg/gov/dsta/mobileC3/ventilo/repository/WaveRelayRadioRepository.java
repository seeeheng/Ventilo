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
import sg.gov.dsta.mobileC3.ventilo.database.DAO.WaveRelayRadioDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;

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
    public void getAllWaveRelayRadios(SingleObserver singleObserver) {
        QueryAllWaveRelayRadiosAsyncTask task = new
                QueryAllWaveRelayRadiosAsyncTask(mWaveRelayRadioDao, singleObserver);
        task.execute();
    }

    /**
     * Obtain WaveRelay Radio model based on Radio Id (Number) from local database
     *
     * @param radioId
     * @param singleObserver
     */
    public void queryRadioByRadioId(long radioId, SingleObserver<WaveRelayRadioModel> singleObserver) {
        Single<WaveRelayRadioModel> single = mWaveRelayRadioDao.getRadioByRadioId(radioId);
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
    public void queryRadioByRadioIPAddress(String radioIPAddress, SingleObserver<WaveRelayRadioModel> singleObserver) {
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
    public void insertWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        InsertAsyncTask task = new InsertAsyncTask(mWaveRelayRadioDao);
        task.execute(waveRelayRadioModel);
    }

    /**
     * Update WaveRelay Radio model by Id in local database
     *
     * @param waveRelayRadioModel
     */
    public void updateWaveRelayRadio(WaveRelayRadioModel waveRelayRadioModel) {
        UpdateAsyncTask task = new UpdateAsyncTask(mWaveRelayRadioDao);
        task.execute(waveRelayRadioModel);
    }

    /**
     * Delete WaveRelay Radio model based on Radio Id (Number) from local database
     *
     * @param radioId
     */
    public void deleteWaveRelayRadio(long radioId) {
        DeleteAsyncTask task = new DeleteAsyncTask(mWaveRelayRadioDao);
        task.execute(radioId);
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
