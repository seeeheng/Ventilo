package sg.gov.dsta.mobileC3.ventilo.util.constant;

public class DatabaseTableConstants {

    // Table names
    public static final String TABLE_USER = "User";
    public static final String TABLE_VIDEO_STREAM = "VideoStream";
    public static final String TABLE_SIT_REP = "SitRep";
    public static final String TABLE_TASK = "Task";


    /** -------------------- Excel Table Header Names -------------------- **/

    // User
    public static final String USER_HEADER_USER_ID = "userId";
    public static final String USER_HEADER_PASSWORD = "password";
    public static final String USER_HEADER_TEAM = "team";
    public static final String USER_HEADER_ROLE = "role";

    // Video Stream
    public static final String VIDEO_STREAM_HEADER_USER_ID = "userId";
    public static final String VIDEO_STREAM_HEADER_NAME = "name";
    public static final String VIDEO_STREAM_HEADER_URL = "url";

    // Sit Rep
    public static final String SITREP_HEADER_REPORTER = "reporter";
    public static final String SITREP_HEADER_SNAPPED_PHOTO = "snappedPhoto";
    public static final String SITREP_HEADER_LOCATION = "location";
    public static final String SITREP_HEADER_ACTIVITY = "activity";
    public static final String SITREP_HEADER_PERSONNEL_T = "personnelT";
    public static final String SITREP_HEADER_PERSONNEL_S = "personnelS";
    public static final String SITREP_HEADER_PERSONNEL_D = "personnelD";
    public static final String SITREP_HEADER_NEXT_COA = "nextCoa";
    public static final String SITREP_HEADER_REQUEST = "request";

    // Task
    public static final String TASK_HEADER_ASSIGNED_TO = "assignedTo";
    public static final String TASK_HEADER_ASSIGNED_BY = "assignedBy";
    public static final String TASK_HEADER_TITLE = "title";
    public static final String TASK_HEADER_DESCRIPTION = "description";
}
