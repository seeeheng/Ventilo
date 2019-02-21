package sg.gov.dsta.mobileC3.ventilo.util.constant;

import java.util.Calendar;
import java.util.Date;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class SharedPreferenceConstants {
//    public static final String TITLE = "VentiloPref";
    public static final int DEFAULT_INT = 0;
    public static final String DEFAULT_STRING = "N.A.";
    public static final String SEPARATOR = "_";

    // Standard Format is (Team of User)_(Callsign of User)_(Main Header)_(Task Number)_(Sub Header)
    // E.g. 1_Alpha_Task_1_Title
    public static final String TEAM_NUMBER = "1";
    public static final String CALLSIGN_USER = "Alpha";

    public static final String INITIALS = TEAM_NUMBER.concat(SEPARATOR).concat(CALLSIGN_USER);

    // Task
    // Format for getting total number is (Team of User)_(Callsign of User)_(Total Number of Tasks)
    public static final String TASK_TOTAL_NUMBER = "Number of Tasks";
    public static final String HEADER_TASK = ReportFragmentConstants.KEY_TASK;
    public static final String SUB_HEADER_TASK_ID = "Id";
    public static final String SUB_HEADER_TASK_ASSIGNER = "Assigner";
    public static final String SUB_HEADER_TASK_ASSIGNEE = "Assignee";
    public static final String SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID = "Assignee Avatar Id";
    public static final String SUB_HEADER_TASK_TITLE = "Title";
    public static final String SUB_HEADER_TASK_DESCRIPTION = "Description";
    public static final String SUB_HEADER_TASK_STATUS = "Status";
    public static final String SUB_HEADER_TASK_DATE = "Date";

    // Incident
    public static final String HEADER_INCIDENT = ReportFragmentConstants.KEY_INCIDENT;
    public static final String SUB_HEADER_INCIDENT_TITLE = "Title";
    public static final String SUB_HEADER_INCIDENT_DESCRIPTION = "Description";

    // Sitrep
    public static final String HEADER_SITREP = ReportFragmentConstants.KEY_SITREP_ACTIVITY;
}
