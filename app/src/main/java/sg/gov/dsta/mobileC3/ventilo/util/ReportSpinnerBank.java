package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.R;

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

    private ReportSpinnerBank(Context context) {
        populateIncidentTitles(context);
        populateTaskTitles(context);
        populateLocationCodewords(context);
        populateActivities(context);
        populateNextCoa(context);
        populateRequests(context);
    }

    public static ReportSpinnerBank getInstance(Context context) {
        if (instance == null) {
            instance = new ReportSpinnerBank(context);
        }
        return instance;
    }

    private void populateIncidentTitles(Context context) {
        incidentTitleList = context.getResources().getStringArray(R.array.incident_title_items);
    }

    private void populateTaskTitles(Context context) {
        taskTitleList = context.getResources().getStringArray(R.array.task_title_items);
    }

    private void populateLocationCodewords(Context context) {
        locationList = context.getResources().getStringArray(R.array.sitrep_location_items);
    }

    private void populateActivities(Context context) {
        activityList = context.getResources().getStringArray(R.array.sitrep_activity_items);
    }

    private void populateNextCoa(Context context) {
        nextCoaList = context.getResources().getStringArray(R.array.sitrep_next_coa_items);
    }

    private void populateRequests(Context context) {
        requestList = context.getResources().getStringArray(R.array.sitrep_request_items);
    }
}
