package sg.gov.dsta.mobileC3.ventilo.repository;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;

import sg.gov.dsta.mobileC3.ventilo.activity.user.UserSettingsFragment;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.ExcelSpreadsheetUtil;
import sg.gov.dsta.mobileC3.ventilo.thread.CustomThreadPoolManager;
import timber.log.Timber;

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

        File excelFile;
        File shipConfigExcelFile;
        PullDataFromExcelToDatabaseAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
            Timber.i(TAG, "Pulling data from Excel to Database...");

            // For Task and User data
            excelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_RELATIVE_PATH, false);
//            ExcelSpreadsheetUtil.readXlsWorkBookDataAndStoreIntoDatabase(excelFile);

            if (excelFile != null) {
                ExcelSpreadsheetUtil.readXlsxWorkBookDataAndStoreIntoDatabase(excelFile);
            }

            // For BFT data
//            shipConfigExcelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_SHIP_CONFIG_RELATIVE_PATH, false);
////            ExcelSpreadsheetUtil.readXlsWorkBookDataAndStoreIntoDatabase(excelFile);
//
//            if (shipConfigExcelFile != null) {
//                ExcelSpreadsheetUtil.readXlsxWorkBookDataAndStoreIntoDatabase(shipConfigExcelFile);
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);

            if (excelFile != null) {
                notifyExcelDataPulledBroadcastIntent();
                Timber.i(TAG, "Excel file - Task and User data stored into database");

            }  else {
                notifyExcelDataPullFailedBroadcastIntent();

            }

//            if (shipConfigExcelFile != null) {
//                notifyShipConfigExcelDataPulledBroadcastIntent();
//                Timber.i(TAG, "Excel file - Ship Config data stored into database");
//
//            } else {
//                notifyShipConfigExcelDataPullFailedBroadcastIntent();
//
//            }
        }
    }

    private class PushDataToExcelFromDatabaseAsyncTask extends AsyncTask<String, Void, Void> {

        PushDataToExcelFromDatabaseAsyncTask() {}

        @Override
        protected Void doInBackground(final String... param) {
            Timber.i(TAG, "Background task - Pushing data to Excel from Database...");
            File excelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_RELATIVE_PATH, true);
//            ExcelSpreadsheetUtil.readDatabaseAndStoreIntoXlsWorkBookData(excelFile);
            ExcelSpreadsheetUtil.readDatabaseAndStoreIntoXlsxWorkBookData(excelFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            notifyExcelDataPushedBroadcastIntent();
            Timber.i(TAG, "Excel file data stored into database");
        }
    }

    private void notifyExcelDataPulledBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PULLED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyShipConfigExcelDataPulledBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULLED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyExcelDataPullFailedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PULL_FAILED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyShipConfigExcelDataPullFailedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_SHIP_CONFIG_PULL_FAILED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyExcelDataPushedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PUSHED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void notifyExcelDataPushFailedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UserSettingsFragment.EXCEL_DATA_PUSH_FAILED_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }
}
