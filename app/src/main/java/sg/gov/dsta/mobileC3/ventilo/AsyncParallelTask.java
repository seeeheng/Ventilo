package sg.gov.dsta.mobileC3.ventilo;

import android.os.AsyncTask;
import android.os.Build;
import androidx.annotation.Nullable;

public class AsyncParallelTask {

//    public static void executeTask(AsyncTask task) {
//        executeTask(task, null);
//    }

    public static void executeTask(AsyncTask task, @Nullable Object... params) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // Android 4.4 (API 19) and above
//            // Parallel AsyncTasks are possible, with the thread-pool size dependent on device
//            // hardware
//            if (params == null) {
//                task.execute();
//            } else {
//                task.execute(params);
//            }
//
//        } else

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // Android 3.0 to
            // Android 4.3
            // Parallel AsyncTasks are not possible unless using executeOnExecutor
            if (params == null) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        } else { // Below Android 3.0
            // Parallel AsyncTasks are possible, with fixed thread-pool size
            if (params == null) {
                task.execute();
            } else {
                task.execute(params);
            }
        }
    }
}
