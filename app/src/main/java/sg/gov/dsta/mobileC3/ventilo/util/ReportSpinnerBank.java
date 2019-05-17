package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;

@Data
public class ReportSpinnerBank {

    private static ReportSpinnerBank instance;

    // Incident
    private String[] incidentTitleList;

    // Task
    private String[] taskTitleList;

    // Sit Rep
    private String[] locationList;
    private String[] activityList;
    private String[] nextCoaList;
    private String[] requestList;

    private ReportSpinnerBank() {
        populateTaskTitles();
        populateLocationCodewords();
        populateActivities();
        populateNextCoa();
        populateRequests();
    }

    public static ReportSpinnerBank getInstance() {
        if (instance == null) {
            instance = new ReportSpinnerBank();
        }
        return instance;
    }

    private void populateTaskTitles() {
        taskTitleList = MainApplication.getAppContext().getResources().
                getStringArray(R.array.task_title_items);
    }

    private void populateLocationCodewords() {
        locationList = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_location_items);
    }

    private void populateActivities() {
        activityList = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_activity_items);
    }

    private void populateNextCoa() {
        nextCoaList = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_next_coa_items);
    }

    private void populateRequests() {
        requestList = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_request_items);
    }
}
