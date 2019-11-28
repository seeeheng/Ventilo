package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.MapDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class MapRepository {

    private LiveData<List<MapModel>> mAllMapLiveData;

    private MapDao mMapDao;

    public MapRepository(Application application) {
        VentiloDatabase db = VentiloDatabase.getInstance(application);
        mMapDao = db.mapDao();
        mAllMapLiveData = mMapDao.getAllMapLiveData();
    }

    public synchronized void deleteAllMaps() {
        mMapDao.deleteAllMaps();
    }

    public synchronized LiveData<List<MapModel>> getAllMapLiveData() {
        return mAllMapLiveData;
    }

    /**
     * Obtains all Map models from database
     *
     * @param singleObserver
     */
    public synchronized void getAllMaps(SingleObserver singleObserver) {
        QueryAllMapAsyncTask task = new
                QueryAllMapAsyncTask(mMapDao, singleObserver);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public synchronized void insertMap(MapModel mapModel) {
        InsertAsyncTask task = new InsertAsyncTask(mMapDao);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(mapModel);
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    /**
     * Query all Map models in database
     *
     */
    private static class QueryAllMapAsyncTask extends AsyncTask<String, Void, Void> {

        private MapDao asyncMapDao;
        private SingleObserver asyncSingleObserver;

        QueryAllMapAsyncTask(MapDao dao, SingleObserver singleObserver) {
            asyncMapDao = dao;
            asyncSingleObserver = singleObserver;
        }

        @Override
        protected Void doInBackground(final String... param) {
            // Converts type Long to Observable, then to Single for RxJava use
            List<MapModel> allMapList =
                    asyncMapDao.getAllMaps();

            if (allMapList == null) {
                allMapList = new ArrayList<>();
            }

            Observable<List<MapModel>> observableAllMaps =
                    Observable.just(allMapList);
            Single<List<MapModel>> singleAllUsers =
                    Single.fromObservable(observableAllMaps);

            singleAllUsers.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(asyncSingleObserver);

            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<MapModel, Void, Void> {

        private MapDao asyncTaskDao;

        InsertAsyncTask(MapDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MapModel... mapModel) {
            asyncTaskDao.createMap(mapModel[0]);
            return null;
        }
    }

}
