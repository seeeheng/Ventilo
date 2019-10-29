package sg.gov.dsta.mobileC3.ventilo.network.waveRelayRadio;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQSubscriber;
import timber.log.Timber;

public class WaveRelayRadioJobService extends JobService {

    private static final int WAVE_RELAY_RADIO_JOB_ID = 1;
    private static final int CHECK_RADIO_CONNECTION_INTERVAL_IN_MILLISEC = 3000;
    private static final int CHECK_RADIO_CONNECTION_FLEX_INTERVAL_IN_MILLISEC = 2000;

    private static volatile WaveRelayRadioJobService instance;
    private static JobScheduler mScheduler;
    private static boolean mIsSchedulerRunning;
    private static WaveRelayRadioConnectionListener mWaveRelayRadioConnectionListener;

    /**
     * Creates/gets singleton class instance
     *
     * @return
     */
    public static WaveRelayRadioJobService getInstance() {
        // Double check locking pattern (check if instance is null twice)
        if (instance == null) {
            synchronized (WaveRelayRadioJobService.class) {
                if (instance == null) {
                    instance = new WaveRelayRadioJobService();
                }
            }
        }

        return instance;
    }

    public WaveRelayRadioJobService() {}

//    public WaveRelayRadioJobService (WaveRelayRadioConnectionListener waveRelayRadioConnectionListener) {
//        mWaveRelayRadioConnectionListener = waveRelayRadioConnectionListener;
//    }

    protected void setConnectionListener(WaveRelayRadioConnectionListener waveRelayRadioConnectionListener) {
        mWaveRelayRadioConnectionListener = waveRelayRadioConnectionListener;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Timber.i("onStartJob");

        if (mWaveRelayRadioConnectionListener != null) {
            Timber.i("runCheckConnection");
            mWaveRelayRadioConnectionListener.runCheckConnection();
        }

        // Reschedule the Service before calling job finished
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            scheduleRefresh();

        jobFinished(params, false);
        stopSelf();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public void scheduleRefresh() {
        ComponentName componentName = new ComponentName(MainApplication.getAppContext(),
                WaveRelayRadioJobService.class);
        JobInfo.Builder jobBuilder =
                new JobInfo.Builder(WAVE_RELAY_RADIO_JOB_ID, componentName);

//        jobBuilder.setPersisted(true);

        /* For Android N and Upper Versions */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            jobBuilder.setMinimumLatency(CHECK_RADIO_CONNECTION_FLEX_INTERVAL_IN_MILLISEC) // Time interval
                    .setOverrideDeadline(CHECK_RADIO_CONNECTION_INTERVAL_IN_MILLISEC);
        } else {
            jobBuilder.setPeriodic(CHECK_RADIO_CONNECTION_INTERVAL_IN_MILLISEC,
                    CHECK_RADIO_CONNECTION_FLEX_INTERVAL_IN_MILLISEC);
        }

        JobInfo info = jobBuilder.build();

        mScheduler = (JobScheduler) MainApplication.getAppContext().
                getSystemService(JOB_SCHEDULER_SERVICE);

        int resultCode = mScheduler.schedule(info);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.i("Job Scheduled");
            mIsSchedulerRunning = true;
        } else {
            Timber.i("Job Scheduling fail");
        }
    }

    public static boolean isSchedulerRunning() {
        return mIsSchedulerRunning;
    }

    public static void stopScheduler() {
        List<JobInfo> jobInfoList = mScheduler.getAllPendingJobs();

        if (jobInfoList != null && jobInfoList.size() > 0) {
            Timber.i("Stopping Wave Relay Job Service Scheduler...");
            mScheduler.cancelAll();
            mIsSchedulerRunning = false;
        }
    }

    public interface WaveRelayRadioConnectionListener {
        void runCheckConnection();
    }

}
