package sg.gov.dsta.mobileC3.ventilo.repository;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;

import sg.gov.dsta.mobileC3.ventilo.AsyncParallelTask;
import sg.gov.dsta.mobileC3.ventilo.activity.user.UserSettingsFragment;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.ExcelSpreadsheetUtil;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;

public class ExcelSpreadsheetRepository {

    private static final String TAG = ExcelSpreadsheetRepository.class.getSimpleName();

    public ExcelSpreadsheetRepository() {
    }

    public void pullDataFromExcelToDatabase() {
        PullDataFromExcelToDatabaseAsyncTask task =
                new PullDataFromExcelToDatabaseAsyncTask();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    public void pushDataToExcelFromDatabase() {
        PushDataToExcelFromDatabaseAsyncTask task =
                new PushDataToExcelFromDatabaseAsyncTask();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        };

        CustomThreadPoolManager.getInstance().addRunnable(runnable);
    }

    private class PullDataFromExcelToDatabaseAsyncTask extends AsyncTask<String, Void, Void> {

        PullDataFromExcelToDatabaseAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
            Log.i(TAG, "Background task - Pulling data from Excel to Database...");
            File excelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_RELATIVE_PATH, false);
            ExcelSpreadsheetUtil.readXlsWorkBookDataAndStoreIntoDatabase(excelFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            notifyExcelDataPulledBroadcastIntent();
            Log.i(TAG, "Excel file data stored into database");
        }
    }

    // TODO: Change code for this
    private class PushDataToExcelFromDatabaseAsyncTask extends AsyncTask<String, Void, Void> {

        PushDataToExcelFromDatabaseAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
            Log.i(TAG, "Background task - Pushing data to Excel from Database...");
            File excelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_RELATIVE_PATH, true);
            ExcelSpreadsheetUtil.readDatabaseAndStoreIntoXlsWorkBookData(excelFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            notifyExcelDataPushedBroadcastIntent();
            Log.i(TAG, "Excel file data stored into database");
        }
    }

    private void notifyExcelDataPulledBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyExcelDataPushedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }
}
