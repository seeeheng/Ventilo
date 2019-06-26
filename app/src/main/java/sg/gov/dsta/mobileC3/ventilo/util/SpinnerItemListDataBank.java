package sg.gov.dsta.mobileC3.ventilo.util;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;

@Data
public class SpinnerItemListDataBank {

    private static SpinnerItemListDataBank instance;

    // Incident
    private String[] incidentTitleStrArray;

    // Task
    private String[] taskTitleStrArray;

    // Sit Rep
    private String[] locationStrArray;
    private String[] activityStrArray;
    private String[] nextCoaStrArray;
    private String[] requestStrArray;
    private String[] blueprintFloorStrArray;
    private String[] blueprintFloorHtmlLinkStrArray;

    private SpinnerItemListDataBank() {
        populateTaskTitles();
        populateLocationCodewords();
        populateActivities();
        populateNextCoa();
        populateRequests();
        populateBlueprintFloors();
        populateBlueprintFloorHtmlLinks();
    }

    public static SpinnerItemListDataBank getInstance() {
        if (instance == null) {
            instance = new SpinnerItemListDataBank();
        }
        return instance;
    }

    private void populateTaskTitles() {
        taskTitleStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.task_title_items);
    }

    private void populateLocationCodewords() {
        locationStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_location_items);
    }

    private void populateActivities() {
        activityStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_activity_items);
    }

    private void populateNextCoa() {
        nextCoaStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_next_coa_items);
    }

    private void populateRequests() {
        requestStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_request_items);
    }

    private void populateBlueprintFloors() {
        blueprintFloorStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.map_ship_blueprint_floor_items);
    }

    private void populateBlueprintFloorHtmlLinks() {
        blueprintFloorHtmlLinkStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.map_ship_blueprint_floor_html_link_items);
    }
}
