package sg.gov.dsta.mobileC3.ventilo.util.constant;

public class SharedPreferenceConstants {
//    public static final String TITLE = "VentiloPref";
    public static final int DEFAULT_INT = 0;
    public static final String DEFAULT_STRING = "";
    public static final String SEPARATOR = "_";

    // Standard Format is (Team of User)_(Callsign of User)_(Main Header)_(Task Number)_(Sub Header)
    // E.g. 1_Alpha_Task_1_Title
    public static final String TEAM_NUMBER = "1";
    public static String CALLSIGN_USER = "Call Sign";
    public static String DEFAULT_CALLSIGN_USER = "A11";

    public static String INITIALS = TEAM_NUMBER.concat(SEPARATOR).concat(DEFAULT_CALLSIGN_USER);

    // Task
    // Format for getting total number is (Team of User)_(Callsign of User)_(Total Number of Tasks)
    public static final String HEADER_TASK = FragmentConstants.KEY_TASK;
    public static final String TASK_TOTAL_NUMBER = "Number of Tasks";
    public static final String SUB_HEADER_TASK_ID = "Task ID";
    public static final String SUB_HEADER_TASK_ASSIGNER = "Task Assigner";
    public static final String SUB_HEADER_TASK_ASSIGNEE = "Task Assignee";
    public static final String SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID = "Task Assignee Avatar ID";
    public static final String SUB_HEADER_TASK_TITLE = "Task Title";
    public static final String SUB_HEADER_TASK_DESCRIPTION = "Task Description";
    public static final String SUB_HEADER_TASK_STATUS = "Task Status";
    public static final String SUB_HEADER_TASK_DATE = "Task Date";

    // Incident
    public static final String HEADER_INCIDENT = FragmentConstants.KEY_INCIDENT;
    public static final String SUB_HEADER_INCIDENT_TITLE = "Title";
    public static final String SUB_HEADER_INCIDENT_DESCRIPTION = "Description";

    // Sitrep
    public static final String HEADER_SITREP = FragmentConstants.KEY_SITREP;
    public static final String SITREP_TOTAL_NUMBER = "Number of Sit Reps";
    public static final String SUB_HEADER_SITREP_ID = "Sit Rep ID";
    public static final String SUB_HEADER_SITREP_REPORTER = "Sit Rep Reporter";
    public static final String SUB_HEADER_SITREP_REPORTER_AVATAR_ID = "Sit Rep Reporter Avatar ID";
    public static final String SUB_HEADER_SITREP_LOCATION = "Sit Rep Location";
    public static final String SUB_HEADER_SITREP_ACTIVITY = "Sit Rep Activity";
    public static final String SUB_HEADER_SITREP_PERSONNEL_T = "Sit Rep Personnel T";
    public static final String SUB_HEADER_SITREP_PERSONNEL_S = "Sit Rep Personnel S";
    public static final String SUB_HEADER_SITREP_PERSONNEL_D = "Sit Rep Personnel D";
    public static final String SUB_HEADER_SITREP_NEXT_COA = "Sit Rep Next COA";
    public static final String SUB_HEADER_SITREP_REQUEST = "Sit Rep Request";
    public static final String SUB_HEADER_SITREP_DATE = "Sit Rep Date";

    // Video
    public static final String HEADER_VIDEO_STREAM = "Video Stream";
    public static final String VIDEO_STREAM_TOTAL_NUMBER = "Number of Video Streams";
    public static final String SUB_HEADER_VIDEO_STREAM_ID = "Video Stream ID";
    public static final String SUB_HEADER_VIDEO_STREAM_NAME = "Video Stream Name";
    public static final String SUB_HEADER_VIDEO_STREAM_URL = "Video Stream URL";
    public static final String SUB_HEADER_VIDEO_STREAM_ICON_TYPE = "Video Stream Icon ID";

    public static final String SUB_HEADER_FIRST_VIDEO_URL = "Video First URL";
    public static final String SUB_HEADER_SECOND_VIDEO_URL = "Video Second URL";
}
