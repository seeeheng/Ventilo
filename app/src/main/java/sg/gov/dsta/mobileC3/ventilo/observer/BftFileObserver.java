package sg.gov.dsta.mobileC3.ventilo.observer;

import android.os.FileObserver;

import androidx.annotation.Nullable;

import timber.log.Timber;

public abstract class BftFileObserver extends FileObserver {

    private static final String TAG = "BftFileObserver";

    private String folderPath;

    public abstract void onCreate();
    public abstract void onModify();

    public BftFileObserver(String folderPath) {
        super(folderPath);
        this.folderPath = folderPath;
    }

    @Override
    public void onEvent(int event, @Nullable String file) {

        if (event == FileObserver.CREATE && !file.equals(".probe")) {
            Timber.d(TAG + "File created [%s] ", folderPath + file);
            onCreate();
        }

        if (event == FileObserver.MODIFY && !file.equals(".probe")) {
            Timber.d(TAG + "File modified [%s] ", folderPath + file);
            onModify();
        }
    }
}
