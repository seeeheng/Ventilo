package sg.gov.dsta.mobileC3.ventilo.model.join;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "UserSitRepJoin",
        primaryKeys = {"userId", "sitRepId"},
        foreignKeys = {
                @ForeignKey(
                        entity = UserModel.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = CASCADE),
                @ForeignKey(
                        entity = SitRepModel.class,
                        parentColumns = "id",
                        childColumns = "sitRepId",
                        onDelete = CASCADE)
        },
        indices = {
                @Index(value = "userId"),
                @Index(value = "sitRepId")
        }
)
public class UserSitRepJoinModel {
    @NonNull
    public String userId;
    public long sitRepId;

    public UserSitRepJoinModel(String userId, long sitRepId) {
        this.userId = userId;
        this.sitRepId = sitRepId;
    }
}