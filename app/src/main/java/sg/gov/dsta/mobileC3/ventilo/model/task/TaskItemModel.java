package sg.gov.dsta.mobileC3.ventilo.model.task;

import android.graphics.drawable.Drawable;

import java.util.Date;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

@Data
public class TaskItemModel {
    private int id;
    private String assignee;
    private Drawable assigneeAvatar;
    private String assigner;
    private String title;
    private String description;
    private EStatus status; // NEW, IN PROGRESS, DONE
    private Date createdDateTime;

//    private String id;
//    private String createdBy; // ic
//    private Date createdDate;
//    private Date modifiedDate;
//
//    private String title;
//    private String detail;
////    private GeoJsonPoint location;
//    private String wsname;
//    private ETopicType type;
//
//
//    private List<String> receiver; //ic
//    private EStatus status; // New, Doing, Done
//    private Date DueDate;
//    private String refid;	// ref id of incident or issue or task
//
//    //private List<String> assignees; //  ic of accountuser
////    private List<CheckList> checklist;
//    private List<Comment> comments;
//    private EStatus levelOfIncident; // HML
}
