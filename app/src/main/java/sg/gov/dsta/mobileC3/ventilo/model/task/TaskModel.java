package sg.gov.dsta.mobileC3.ventilo.model.task;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.text.TextUtils;

import java.util.Comparator;

import lombok.Data;

@Data
//@Entity(foreignKeys = @ForeignKey(entity = UserModel.class,
//        parentColumns = "userId",
//        childColumns = "userId"))
@Entity(tableName = "Task")
public class TaskModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long refId;
    private String phaseNo;
    private String adHocTaskPriority;
    private String assignedTo;
    private String assignedBy;
    private String title;
    private String description;
    private String status; // NEW, IN PROGRESS, COMPLETE
    private String createdDateTime;
    private String completedDateTime;
    private String lastUpdatedStatusDateTime;
    private String lastUpdatedMainDateTime;
    private String isValid;

    // --- GETTER ---
    public long getId() { return id; }
    public long getRefId() { return refId; }
    public String getPhaseNo() { return phaseNo; }
    public String getAdHocTaskPriority() { return adHocTaskPriority; }
//    public long getUserId() { return userId; }
    public String getAssignedTo() { return assignedTo; }
    public String getAssignedBy() { return assignedBy; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCreatedDateTime() { return createdDateTime; }
    public String getCompletedDateTime() { return completedDateTime; }
    public String getLastUpdatedStatusDateTime() { return lastUpdatedStatusDateTime; }
    public String getLastUpdatedMainDateTime() { return lastUpdatedMainDateTime; }
    public String getIsValid() { return isValid; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setRefId(long refId) { this.refId = refId; }
//    public void setUserId(long userId) { this.userId = userId; }
    public void setPhaseNo(String phaseNo) { this.phaseNo = phaseNo; }
    public void setAdHocTaskPriority(String adHocTaskPriority) { this.adHocTaskPriority = adHocTaskPriority; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedDateTime(String createdDateTime) { this.createdDateTime = createdDateTime; }
    public void setCompletedDateTime(String completedDateTime) { this.completedDateTime = completedDateTime; }
    public void setLastUpdatedStatusDateTime(String lastUpdatedStatusDateTime) { this.lastUpdatedStatusDateTime = lastUpdatedStatusDateTime; }
    public void setLastUpdatedMainDateTime(String lastUpdatedMainDateTime) { this.lastUpdatedMainDateTime = lastUpdatedMainDateTime; }
    public void setIsValid(String isValid) { this.isValid = isValid; }

    public static Comparator<TaskModel> getPhaseNoComparator(){
        return new Comparator<TaskModel>() {

            @Override
            public int compare(TaskModel taskModelOne, TaskModel taskModelTwo) {
                if (TextUtils.isDigitsOnly(taskModelOne.phaseNo) && TextUtils.isDigitsOnly(taskModelTwo.phaseNo)) {
                    return Integer.valueOf(taskModelOne.phaseNo) - Integer.valueOf(taskModelTwo.phaseNo);
                }

                return -1;
            }
        };
    }
}
