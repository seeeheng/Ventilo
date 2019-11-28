package sg.gov.dsta.mobileC3.ventilo.model.join;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

import static androidx.room.ForeignKey.CASCADE;

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