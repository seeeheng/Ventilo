package sg.gov.dsta.mobileC3.ventilo.model.join;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

import static android.arch.persistence.room.ForeignKey.CASCADE;

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