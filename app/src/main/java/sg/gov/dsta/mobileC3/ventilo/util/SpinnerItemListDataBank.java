package sg.gov.dsta.mobileC3.ventilo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    // Sit Rep (Mission)
    private List<String> locationList;
    private List<String> activityList;
    private List<String> nextCoaList;
    private List<String> requestList;
    private String[] locationStrArray;
    private String[] activityStrArray;
    private String[] nextCoaStrArray;
    private String[] requestStrArray;

    // Sit Rep (Inspection)
    private String[] vesselTypeStrArray;
    private String[] vesselNameStrArray;
    private String[] lpocStrArray;
    private String[] npocStrArray;
    private String[] cargoStrArray;

    // Map Blueprint
    private String[] blueprintFloorStrArray;
    private String[] blueprintFloorHtmlLinkStrArray;
    private boolean isLocalBlueprintDirectory;

    // Map Blueprint Test Log (motion label)
    private List<String> motionLabelList;
    private String[] motionLabelStrArray;

    private SpinnerItemListDataBank() {

        // Task
        populateTaskTitles();

        // Sit Rep Mission
        locationList = new ArrayList<>();
        locationList.add(MainApplication.getAppContext().
                getResources().getString(R.string.sitrep_select_location));

        activityList = new ArrayList<>();
        activityList.add(MainApplication.getAppContext().
                getResources().getString(R.string.sitrep_select_activity));

        nextCoaList = new ArrayList<>();
        nextCoaList.add(MainApplication.getAppContext().
                getResources().getString(R.string.sitrep_select_next_coa));

        requestList = new ArrayList<>();
        requestList.add(MainApplication.getAppContext().
                getResources().getString(R.string.sitrep_select_request));

        populateLocationCodewords();
        populateActivities();
        populateNextCoa();
        populateRequests();

        // Sit Rep Inspection
        populateVesselType();
        populateVesselName();
        populateLpoc();
        populateNpoc();
        populateCargo();

        // Map Blueprint
        populateBlueprintFloors();
        populateBlueprintFloorHtmlLinks();

        // Map Blueprint Test Log
        motionLabelList = new ArrayList<>();

        populateMotionLabels();
    }

    public static SpinnerItemListDataBank getInstance() {
        if (instance == null) {
            instance = new SpinnerItemListDataBank();
        }
        return instance;
    }

    /** -------------------- Task -------------------- **/

    private void populateTaskTitles() {
        taskTitleStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.task_title_items);
    }

    /** -------------------- Sit Rep Mission -------------------- **/

    public void clearSitRepDropdownListData() {
        if (locationList != null) {
            locationList.clear();
            locationList.add(MainApplication.getAppContext().
                    getResources().getString(R.string.sitrep_select_location));
        }

        if (activityList != null) {
            activityList.clear();
            activityList.add(MainApplication.getAppContext().
                    getResources().getString(R.string.sitrep_select_activity));
        }

        if (nextCoaList != null) {
            nextCoaList.clear();
            nextCoaList.add(MainApplication.getAppContext().
                    getResources().getString(R.string.sitrep_select_next_coa));
        }

        if (requestList != null) {
            requestList.clear();
            requestList.add(MainApplication.getAppContext().
                    getResources().getString(R.string.sitrep_select_request));
        }
    }

    public void addItemToLocationList(String location) {
        if (locationList != null) {
            locationList.add(location);
            locationStrArray = locationList.toArray(new String[0]);
        }
    }

    public void addItemToActivityList(String activity) {
        if (activityList != null) {
            activityList.add(activity);
            activityStrArray = activityList.toArray(new String[0]);
        }
    }

    public void addItemToNextCoaList(String nextCoa) {
        if (nextCoaList != null) {
            nextCoaList.add(nextCoa);
            nextCoaStrArray = nextCoaList.toArray(new String[0]);
        }
    }

    public void addItemToRequestList(String request) {
        if (requestList != null) {
            requestList.add(request);
            requestStrArray = requestList.toArray(new String[0]);
        }
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

    /** -------------------- Sit Rep Inspection -------------------- **/
    private void populateVesselType() {
        vesselTypeStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_vessel_type_items);
    }

    private void populateVesselName() {
        vesselNameStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_vessel_name_items);
    }

    private void populateLpoc() {
        lpocStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_lpoc_items);
    }

    private void populateNpoc() {
        npocStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_npoc_items);
    }

    private void populateCargo() {
        cargoStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.sitrep_cargo_items);
    }

    /** -------------------- Map Blueprint -------------------- **/
    public void repopulateBlueprintDetails() {
        populateBlueprintFloors();
        populateBlueprintFloorHtmlLinks();
    }

    private void populateBlueprintFloors() {
        blueprintFloorStrArray = FileUtil.getAllFileNamesWithoutExtensionInMapBlueprintHtmlFolder();

        if (blueprintFloorStrArray.length == 0) {
            blueprintFloorStrArray = MainApplication.getAppContext().getResources().
                    getStringArray(R.array.map_ship_blueprint_floor_items);
            isLocalBlueprintDirectory = true;

        } else {
            isLocalBlueprintDirectory = false;

        }

    }

    private void populateBlueprintFloorHtmlLinks() {
        blueprintFloorHtmlLinkStrArray = FileUtil.getAllFileNamesInMapBlueprintHtmlFolder();

        if (blueprintFloorHtmlLinkStrArray == null || blueprintFloorHtmlLinkStrArray.length == 0) {
            blueprintFloorHtmlLinkStrArray = MainApplication.getAppContext().getResources().
                    getStringArray(R.array.map_ship_blueprint_floor_html_link_items);
            isLocalBlueprintDirectory = true;

        } else {
            isLocalBlueprintDirectory = false;

        }

    }

    /** -------------------- Map Blueprint Motion Label -------------------- **/
    private void populateMotionLabels() {
        motionLabelStrArray = MainApplication.getAppContext().getResources().
                getStringArray(R.array.map_blueprint_test_log_motion_label_items);

        List<String> motionLabelStrList = new ArrayList<>(Arrays.asList(motionLabelStrArray));
        motionLabelList.addAll(motionLabelStrList);
    }

    public void addItemToMotionLabelList(String motionLabel) {
        if (motionLabelList != null) {
            motionLabelList.add(motionLabel);
            motionLabelStrArray = motionLabelList.toArray(new String[0]);
        }
    }
}
