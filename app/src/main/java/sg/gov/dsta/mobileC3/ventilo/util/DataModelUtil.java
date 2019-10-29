package sg.gov.dsta.mobileC3.ventilo.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;

public class DataModelUtil {

    // All fields
    public static final int NO_OF_FIELDS_IN_USER_MODEL = getNoOfFieldsInUserModel();
    public static final int NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL = getNoOfFieldsInVideoStreamModel();
    public static final int NO_OF_FIELDS_IN_SIT_REP_MODEL = getNoOfFieldsInSitRepModel();
    public static final int NO_OF_FIELDS_IN_TASK_MODEL = getNoOfFieldsInTaskModel();

    // All fields without Id
    public static final int NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_WITHOUT_ID = getNoOfFieldsInVideoStreamModelWithoutId();
    public static final int NO_OF_FIELDS_IN_SIT_REP_MODEL_WITHOUT_ID = getNoOfFieldsInSitRepModelWithoutId();
    public static final int NO_OF_FIELDS_IN_TASK_MODEL_WITHOUT_ID = getNoOfFieldsInTaskModelWithoutId();

    // Fields From Excel
    public static final int NO_OF_FIELDS_IN_USER_MODEL_FROM_EXCEL = getNoOfFieldsInUserModelOfExcel();
    public static final int NO_OF_FIELDS_IN_MAP_MODEL_FROM_EXCEL = getNoOfFieldsInMapModelOfExcel();
    //    public static final int NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL = getNoOfFieldsInVideoStreamModelOfExcel();
    public static final int NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL = getNoOfFieldsInSitRepModelDropdownListOfExcel();
    public static final int NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL = getNoOfFieldsInTaskModelOfExcel();

    public static final String USER_ID_FIELD = "userId";

    /**
     * -------------------- Excluded Fields --------------------
     **/
    private static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
    private static final String CHANGE_FIELD = "$change";
    private static final String ID_FIELD = "id";
    private static final String REF_ID_FIELD = "refId";

    // User specific
    private static final String ACCESS_TOKEN_FIELD = "accessToken";
    private static final String PHONE_TO_RADIO_CONNECTION_STATUS_FIELD = "phoneToRadioConnectionStatus";
    private static final String RADIO_TO_NETWORK_CONNECTION_STATUS_FIELD = "radioToNetworkConnectionStatus";
    private static final String RADIO_FULL_CONNECTION_STATUS_FIELD = "radioFullConnectionStatus";
    private static final String LAST_KNOWN_CONNECTION_DATE_TIME_FIELD = "lastKnownConnectionDateTime";
    private static final String MISSING_HEARTBEAT_COUNT_FIELD = "missingHeartBeatCount";

    // Map specific
    private static final String VIEW_TYPE_FIELD = "viewType";

    // Sit Rep specific
    private static final String REPORTER_FIELD = "reporter";
    private static final String SNAPPED_PHOTO_FIELD = "snappedPhoto";
    private static final String REPORT_TYPE_FIELD = "reportType";
    private static final String PERSONNEL_T_FIELD = "personnelT";
    private static final String PERSONNEL_S_FIELD = "personnelS";
    private static final String PERSONNEL_D_FIELD = "personnelD";
    private static final String OTHERS_FIELD = "others";

    private static final String VESSEL_TYPE_FIELD = "vesselType";
    private static final String VESSEL_NAME_FIELD = "vesselName";
    private static final String LPOC_FIELD = "lpoc";
    private static final String NPOC_FIELD = "npoc";
    private static final String LAST_VISIT_TO_SG_FIELD = "lastVisitToSg";
    private static final String VESSEL_LAST_BOARDED_FIELD = "vesselLastBoarded";
    private static final String CARGO_FIELD = "cargo";
    private static final String PURPOSE_OF_CALL_FIELD = "purposeOfCall";
    private static final String DURATION_FIELD = "duration";
    private static final String CURRENT_CREW_FIELD = "currentCrew";
    private static final String CURRENT_MASTER_FIELD = "currentMaster";
    private static final String CURRENT_CE_FIELD = "currentCe";
    private static final String QUERIES_FIELD = "queries";
    private static final String LAST_UPDATED_DATE_TIME_FIELD = "lastUpdatedDateTime";

    // Task specific
    private static final String ICON_TYPE_FIELD = "iconType";
    private static final String STATUS_FIELD = "status";
    private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";
    private static final String COMPLETED_DATE_TIME_FIELD = "completedDateTime";
    private static final String LAST_UPDATED_STATUS_DATE_TIME_FIELD = "lastUpdatedStatusDateTime";
    private static final String LAST_UPDATED_MAIN_DATE_TIME_FIELD = "lastUpdatedMainDateTime";
    private static final String IS_VALID_FIELD = "isValid";

    /** -------------------- All Fields -------------------- **/

    /**
     * DeclaredFields method includes unwanted variable fields;
     * This method filters off serial version UID and $change variables from DeclaredFields()
     * From User model
     *
     * @return
     */
    public static Field[] getUserModelFields() {
        Stream<Field> streamFields = Arrays.stream(UserModel.class.getDeclaredFields());
        List<Field> userModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD)
                && !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] userModelFieldsArray = new Field[userModelFieldsList.size()];
        userModelFieldsList.toArray(userModelFieldsArray);

        return userModelFieldsArray;
    }

    /**
     * DeclaredFields method includes unwanted variable fields;
     * This method filters off serial version UID and $change variables from DeclaredFields()
     * From Map model
     *
     * @return
     */
    public static Field[] getMapModelFields() {
        Stream<Field> streamFields = Arrays.stream(MapModel.class.getDeclaredFields());
        List<Field> mapModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD)
                && !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] mapModelFieldsArray = new Field[mapModelFieldsList.size()];
        mapModelFieldsList.toArray(mapModelFieldsArray);

        return mapModelFieldsArray;
    }

    /**
     * DeclaredFields method includes unwanted variable fields;
     * This method filters off serial version UID and $change variables from DeclaredFields()
     * From Video Stream model
     *
     * @return
     */
    public static Field[] getVideoStreamModelFields() {
        Stream<Field> streamFields = Arrays.stream(VideoStreamModel.class.getDeclaredFields());
        List<Field> videoStreamModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD)
                && !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] videoStreamModelFieldsArray = new Field[videoStreamModelFieldsList.size()];
        videoStreamModelFieldsList.toArray(videoStreamModelFieldsArray);

        return videoStreamModelFieldsArray;
    }

    /**
     * DeclaredFields method includes unwanted variable fields;
     * This method filters off serial version UID and $change variables from DeclaredFields()
     * From Sit Rep model
     *
     * @return
     */
    public static Field[] getSitRepModelFields() {
        Stream<Field> streamFields = Arrays.stream(SitRepModel.class.getDeclaredFields());
        List<Field> sitRepModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD)
                && !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] sitRepModelFieldsArray = new Field[sitRepModelFieldsList.size()];
        sitRepModelFieldsList.toArray(sitRepModelFieldsArray);

        return sitRepModelFieldsArray;
    }

    /**
     * DeclaredFields method includes unwanted variable fields;
     * This method filters off serial version UID and $change variables from DeclaredFields()
     * From Task model
     *
     * @return
     */
    public static Field[] getTaskModelFields() {
        Stream<Field> streamFields = Arrays.stream(TaskModel.class.getDeclaredFields());
        List<Field> taskModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD)
                && !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] taskModelFieldsArray = new Field[taskModelFieldsList.size()];
        taskModelFieldsList.toArray(taskModelFieldsArray);

        return taskModelFieldsArray;
    }

    private static int getNoOfFieldsInUserModel() {
        return getUserModelFields().length;
    }

    private static int getNoOfFieldsInVideoStreamModel() {
        return getVideoStreamModelFields().length;
    }

    private static int getNoOfFieldsInSitRepModel() {
        return getSitRepModelFields().length;
    }

    private static int getNoOfFieldsInTaskModel() {
        return getTaskModelFields().length;
    }


    /** -------------------- All Fields Except Id -------------------- **/

    /**
     * Filters off additional 'id' field which is an auto-generated field.
     * From Video Stream model
     *
     * @return
     */
    public static Field[] getVideoStreamModelFieldsWithoutID() {
        Stream<Field> streamFields = Arrays.stream(getVideoStreamModelFields());
        List<Field> videoStreamModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(ID_FIELD)).
                collect(Collectors.toList());

        Field[] videoStreamModelFieldsArray = new Field[videoStreamModelFieldsList.size()];
        videoStreamModelFieldsList.toArray(videoStreamModelFieldsArray);

        return videoStreamModelFieldsArray;
    }

    /**
     * Filters off additional 'id' field which is an auto-generated field.
     * From Sit Rep model
     *
     * @return
     */
    public static Field[] getSitRepModelFieldsWithoutID() {
        Stream<Field> streamFields = Arrays.stream(getSitRepModelFields());
        List<Field> sitRepModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(ID_FIELD)).
                collect(Collectors.toList());

        Field[] sitRepModelFieldsArray = new Field[sitRepModelFieldsList.size()];
        sitRepModelFieldsList.toArray(sitRepModelFieldsArray);

        return sitRepModelFieldsArray;
    }

    /**
     * Filters off additional 'id' field which is an auto-generated field.
     * From Task model
     *
     * @return
     */
    public static Field[] getTaskModelFieldsWithoutID() {
        Stream<Field> streamFields = Arrays.stream(getTaskModelFields());
        List<Field> taskModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(ID_FIELD)).
                collect(Collectors.toList());

        Field[] taskModelFieldsArray = new Field[taskModelFieldsList.size()];
        taskModelFieldsList.toArray(taskModelFieldsArray);

        return taskModelFieldsArray;
    }

    private static int getNoOfFieldsInVideoStreamModelWithoutId() {
        return getVideoStreamModelFieldsWithoutID().length;
    }

    private static int getNoOfFieldsInSitRepModelWithoutId() {
        return getSitRepModelFieldsWithoutID().length;
    }

    private static int getNoOfFieldsInTaskModelWithoutId() {
        return getTaskModelFieldsWithoutID().length;
    }


    /** -------------------- Fields From Excel -------------------- **/

    /**
     * Filters off fields NOT required of Excel.
     * From User model
     *
     * @return
     */
    public static Field[] getUserModelFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getUserModelFields());
        List<Field> userModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(ACCESS_TOKEN_FIELD)
                && !f.getName().equalsIgnoreCase(PHONE_TO_RADIO_CONNECTION_STATUS_FIELD)
                && !f.getName().equalsIgnoreCase(RADIO_TO_NETWORK_CONNECTION_STATUS_FIELD)
                && !f.getName().equalsIgnoreCase(RADIO_FULL_CONNECTION_STATUS_FIELD)
                && !f.getName().equalsIgnoreCase(LAST_KNOWN_CONNECTION_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(MISSING_HEARTBEAT_COUNT_FIELD)).
                collect(Collectors.toList());

        Field[] userModelFieldsArray = new Field[userModelFieldsList.size()];
        userModelFieldsList.toArray(userModelFieldsArray);

        return userModelFieldsArray;
    }

    /**
     * Filters off fields NOT required of Excel.
     * From Map model
     *
     * @return
     */
    public static Field[] getMapModelFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getMapModelFields());
        List<Field> mapModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(VIEW_TYPE_FIELD)).
                collect(Collectors.toList());

        Field[] mapModelFieldsArray = new Field[mapModelFieldsList.size()];
        mapModelFieldsList.toArray(mapModelFieldsArray);

        return mapModelFieldsArray;
    }

//    /**
//     * Filters off fields NOT required of Excel.
//     * From Video Stream model
//     *
//     * @return
//     */
//    public static Field[] getVideoStreamModelFieldsOfExcel() {
//        Stream<Field> streamFields = Arrays.stream(getVideoStreamModelFieldsWithoutID());
//        List<Field> videoStreamModelFieldsList = streamFields.filter(f ->
//                !f.getName().equalsIgnoreCase(ICON_TYPE_FIELD)).
//                collect(Collectors.toList());
//
//        Field[] videoStreamModelFieldsArray = new Field[videoStreamModelFieldsList.size()];
//        videoStreamModelFieldsList.toArray(videoStreamModelFieldsArray);
//
//        return videoStreamModelFieldsArray;
//    }

    /**
     * Filters off fields NOT required of Excel.
     * From Sit Rep model
     *
     * @return
     */
    public static Field[] getSitRepModelDropdownListFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getSitRepModelFieldsWithoutID());
        List<Field> sitRepModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(REF_ID_FIELD)
                && !f.getName().equalsIgnoreCase(REPORTER_FIELD)
                && !f.getName().equalsIgnoreCase(SNAPPED_PHOTO_FIELD)
                && !f.getName().equalsIgnoreCase(REPORT_TYPE_FIELD)
                && !f.getName().equalsIgnoreCase(PERSONNEL_T_FIELD)
                && !f.getName().equalsIgnoreCase(PERSONNEL_S_FIELD)
                && !f.getName().equalsIgnoreCase(PERSONNEL_D_FIELD)
                && !f.getName().equalsIgnoreCase(OTHERS_FIELD)
                && !f.getName().equalsIgnoreCase(VESSEL_TYPE_FIELD)
                && !f.getName().equalsIgnoreCase(VESSEL_NAME_FIELD)
                && !f.getName().equalsIgnoreCase(LPOC_FIELD)
                && !f.getName().equalsIgnoreCase(NPOC_FIELD)
                && !f.getName().equalsIgnoreCase(LAST_VISIT_TO_SG_FIELD)
                && !f.getName().equalsIgnoreCase(VESSEL_LAST_BOARDED_FIELD)
                && !f.getName().equalsIgnoreCase(CARGO_FIELD)
                && !f.getName().equalsIgnoreCase(PURPOSE_OF_CALL_FIELD)
                && !f.getName().equalsIgnoreCase(DURATION_FIELD)
                && !f.getName().equalsIgnoreCase(CURRENT_CREW_FIELD)
                && !f.getName().equalsIgnoreCase(CURRENT_MASTER_FIELD)
                && !f.getName().equalsIgnoreCase(CURRENT_CE_FIELD)
                && !f.getName().equalsIgnoreCase(QUERIES_FIELD)
                && !f.getName().equalsIgnoreCase(CREATED_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(LAST_UPDATED_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(IS_VALID_FIELD)).
                collect(Collectors.toList());

        Field[] sitRepModelFieldsArray = new Field[sitRepModelFieldsList.size()];
        sitRepModelFieldsList.toArray(sitRepModelFieldsArray);

        return sitRepModelFieldsArray;
    }

    /**
     * Filters off fields NOT required of Excel.
     * From Task model
     *
     * @return
     */
    public static Field[] getTaskModelFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getTaskModelFieldsWithoutID());
        List<Field> taskModelFieldsList = streamFields.filter(f
                -> !f.getName().equalsIgnoreCase(REF_ID_FIELD)
                && !f.getName().equalsIgnoreCase(STATUS_FIELD)
                && !f.getName().equalsIgnoreCase(CREATED_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(COMPLETED_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(LAST_UPDATED_STATUS_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(LAST_UPDATED_MAIN_DATE_TIME_FIELD)
                && !f.getName().equalsIgnoreCase(IS_VALID_FIELD)).
                collect(Collectors.toList());

        Field[] taskModelFieldsArray = new Field[taskModelFieldsList.size()];
        taskModelFieldsList.toArray(taskModelFieldsArray);

        return taskModelFieldsArray;
    }

    private static int getNoOfFieldsInUserModelOfExcel() {
        return getUserModelFieldsOfExcel().length;
    }

    private static int getNoOfFieldsInMapModelOfExcel() {
        return getMapModelFieldsOfExcel().length;
    }

//    private static int getNoOfFieldsInVideoStreamModelOfExcel() {
//        return getVideoStreamModelFieldsOfExcel().length;
//    }

    private static int getNoOfFieldsInSitRepModelDropdownListOfExcel() {
        return getSitRepModelDropdownListFieldsOfExcel().length;
    }

    private static int getNoOfFieldsInTaskModelOfExcel() {
        return getTaskModelFieldsOfExcel().length;
    }
}
