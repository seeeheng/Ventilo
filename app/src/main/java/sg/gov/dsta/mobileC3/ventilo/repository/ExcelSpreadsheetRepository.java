package sg.gov.dsta.mobileC3.ventilo.repository;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import sg.gov.dsta.mobileC3.ventilo.database.ExcelSpreadsheetUtil;

public class ExcelSpreadsheetRepository {

    private static final String TAG = ExcelSpreadsheetRepository.class.getSimpleName();

    public ExcelSpreadsheetRepository() {
    }

    public void pullDataFromExcelToDatabase() {
        PullDataFromExcelToDatabaseAsyncTask task =
                new PullDataFromExcelToDatabaseAsyncTask();
        task.execute();
    }

    public void pushDataToExcelFromDatabase() {
        PushDataToExcelFromDatabaseAsyncTask task =
                new PushDataToExcelFromDatabaseAsyncTask();
        task.execute();
    }

    private static class PullDataFromExcelToDatabaseAsyncTask extends AsyncTask<String, Void, Void> {

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
            Log.i(TAG, "Excel file data stored into database");
        }
    }

    // TODO: Change code for this
    private static class PushDataToExcelFromDatabaseAsyncTask extends AsyncTask<String, Void, Void> {

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
            Log.i(TAG, "Excel file data stored into database");
        }
    }
}
