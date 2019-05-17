package sg.gov.dsta.mobileC3.ventilo.worker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.ExcelSpreadsheetUtil;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPubSubBrokerProxy;

public class NetworkWorker extends Worker {

    private static final String TAG = NetworkWorker.class.getSimpleName();

    private static RabbitMQHelper rabbitMQHelper;

    public NetworkWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        rabbitMQHelper = RabbitMQHelper.getInstance();
        rabbitMQHelper.connectionStatus = RabbitMQHelper.RabbitMQConnectionStatus.INITIAL;
    }

    private void notifyRabbitMQConnectedBroadcastIntent() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(RabbitMQHelper.RABBITMQ_CONNECT_INTENT_ACTION);
//        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    private void initJeroMQ() {
        JeroMQPubSubBrokerProxy.getInstance().start();
//        JeroMQPublisher.getInstance().start();
//        JeroMQSubscriber.getInstance().start();
        Log.i(TAG, "JeroMQ initialised");
    }

    private void extractDataFromExcelFile() {
        File excelFile = ExcelSpreadsheetUtil.getExcelFile(ExcelSpreadsheetUtil.EXCEL_FILE_RELATIVE_PATH, false);
        ExcelSpreadsheetUtil.readXlsWorkBookDataAndStoreIntoDatabase(excelFile);
        Log.i(TAG, "Excel file data stored into database");
    }

    @NonNull
    @Override
    public Result doWork() {

        if (rabbitMQHelper != null) {
            System.out.println("rabbitMQHelper not null");
            boolean isRabbitMQConnected = rabbitMQHelper.startRabbitMQWithDefaultSetting();
            if (isRabbitMQConnected) {
                System.out.println("rabbitMQHelper notify");
                notifyRabbitMQConnectedBroadcastIntent();
            }
        }

//        initJeroMQ();
//        extractDataFromExcelFile();

        return Result.success();
    }
}
