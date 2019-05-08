package sg.gov.dsta.mobileC3.ventilo.model.join;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "UserTaskJoin",
        primaryKeys = {"userId", "taskId"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserModel.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = CASCADE),
                @ForeignKey(
                        entity = TaskModel.class,
                        parentColumns = "id",
                        childColumns = "taskId",
                        onDelete = CASCADE)
        },
        indices = {
                @Index(value = "userId"),
                @Index(value = "taskId")
        }
)
public class UserTaskJoinModel {
    @NonNull
    public String userId;
    public long taskId;

    public UserTaskJoinModel(String userId, long taskId) {
        this.userId = userId;
        this.taskId = taskId;
    }
}