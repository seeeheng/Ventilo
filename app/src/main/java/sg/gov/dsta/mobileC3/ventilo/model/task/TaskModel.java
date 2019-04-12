package sg.gov.dsta.mobileC3.ventilo.model.task;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Data
//@Entity(foreignKeys = @ForeignKey(entity = UserModel.class,
//        parentColumns = "userId",
//        childColumns = "userId"))
@Entity(tableName="Task")
public class TaskModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
//    private long userId;
    private String assignee;
    private String assignedBy;
    private String title;
    private String description;
    private String status; // NEW, IN PROGRESS, COMPLETE
    private String createdDateTime;

    // --- GETTER ---
    public long getId() { return id; }
//    public long getUserId() { return userId; }
    public String getAssignee() { return assignee; }
    public String getAssignedBy() { return assignedBy; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreatedDateTime() { return createdDateTime; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
//    public void setUserId(long userId) { this.userId = userId; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedDateTime(String createdDateTime) { this.createdDateTime = createdDateTime; }

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
