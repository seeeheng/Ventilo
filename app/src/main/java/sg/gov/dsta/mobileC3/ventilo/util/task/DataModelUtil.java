package sg.gov.dsta.mobileC3.ventilo.util.task;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final int NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL = getNoOfFieldsInVideoStreamModelOfExcel();
    public static final int NO_OF_FIELDS_IN_SIT_REP_MODEL_FROM_EXCEL = getNoOfFieldsInSitRepModelOfExcel();
    public static final int NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL = getNoOfFieldsInTaskModelOfExcel();

    public static final String USER_ID_FIELD = "userId";

    // Excluded Fields
    private static final String SERIAL_VERSION_UID_FIELD = "serialVersionUID";
    private static final String CHANGE_FIELD = "$change";
    private static final String ID_FIELD = "id";
    private static final String REF_ID_FIELD = "refId";
    private static final String ACCESS_TOKEN_FIELD = "accessToken";
    private static final String ICON_TYPE_FIELD = "iconType";
    private static final String STATUS_FIELD = "status";
    private static final String CREATED_DATE_TIME_FIELD = "createdDateTime";


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
        List<Field> userModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD) &&
                        !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
                collect(Collectors.toList());

        Field[] userModelFieldsArray = new Field[userModelFieldsList.size()];
        userModelFieldsList.toArray(userModelFieldsArray);

        return userModelFieldsArray;
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
        List<Field> videoStreamModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD) &&
                        !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
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
        List<Field> sitRepModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD) &&
                        !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
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
        List<Field> taskModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(SERIAL_VERSION_UID_FIELD) &&
                        !f.getName().equalsIgnoreCase(CHANGE_FIELD)).
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
        List<Field> videoStreamModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(ID_FIELD)).
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
        List<Field> sitRepModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(ID_FIELD)).
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
        List<Field> userModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(ACCESS_TOKEN_FIELD)).
                collect(Collectors.toList());

        Field[] userModelFieldsArray = new Field[userModelFieldsList.size()];
        userModelFieldsList.toArray(userModelFieldsArray);

        return userModelFieldsArray;
    }

    /**
     * Filters off fields NOT required of Excel.
     * From Video Stream model
     *
     * @return
     */
    public static Field[] getVideoStreamModelFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getVideoStreamModelFieldsWithoutID());
        List<Field> videoStreamModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(ICON_TYPE_FIELD)).
                collect(Collectors.toList());

        Field[] videoStreamModelFieldsArray = new Field[videoStreamModelFieldsList.size()];
        videoStreamModelFieldsList.toArray(videoStreamModelFieldsArray);

        return videoStreamModelFieldsArray;
    }

    /**
     * Filters off fields NOT required of Excel.
     * From Sit Rep model
     *
     * @return
     */
    public static Field[] getSitRepModelFieldsOfExcel() {
        Stream<Field> streamFields = Arrays.stream(getSitRepModelFieldsWithoutID());
        List<Field> sitRepModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(REF_ID_FIELD) &&
                !f.getName().equalsIgnoreCase(CREATED_DATE_TIME_FIELD)).
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
        List<Field> taskModelFieldsList = streamFields.filter(f ->
                !f.getName().equalsIgnoreCase(REF_ID_FIELD) &&
                        !f.getName().equalsIgnoreCase(STATUS_FIELD) &&
                        !f.getName().equalsIgnoreCase(CREATED_DATE_TIME_FIELD)).
                collect(Collectors.toList());

        Field[] taskModelFieldsArray = new Field[taskModelFieldsList.size()];
        taskModelFieldsList.toArray(taskModelFieldsArray);

        return taskModelFieldsArray;
    }

    private static int getNoOfFieldsInUserModelOfExcel() {
        return getUserModelFieldsOfExcel().length;
    }

    private static int getNoOfFieldsInVideoStreamModelOfExcel() {
        return getVideoStreamModelFieldsOfExcel().length;
    }

    private static int getNoOfFieldsInSitRepModelOfExcel() {
        return getSitRepModelFieldsOfExcel().length;
    }

    private static int getNoOfFieldsInTaskModelOfExcel() {
        return getTaskModelFieldsOfExcel().length;
    }
}
