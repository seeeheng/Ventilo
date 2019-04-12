package sg.gov.dsta.mobileC3.ventilo.repository;

import android.app.Application;
import android.os.AsyncTask;

import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;

public class MainRepository {

    VentiloDatabase mDb;

    public MainRepository(Application application) {
        mDb = VentiloDatabase.getInstance(application);
    }

    public void clearAllData() {
        MainRepository.ClearAllDataAsyncTask task =
                new MainRepository.ClearAllDataAsyncTask(mDb);
        task.execute();
    }

    private static class ClearAllDataAsyncTask extends AsyncTask<String, Void, Void> {

        private VentiloDatabase asyncDb;

        ClearAllDataAsyncTask(VentiloDatabase db) {
            asyncDb = db;
        }

        @Override
        protected Void doInBackground(final String... param) {
            asyncDb.clearAllTables();
            return null;
        }
    }
}
