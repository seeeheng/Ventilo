package sg.gov.dsta.mobileC3.ventilo.model.user;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import lombok.Data;

@Data
@Entity(tableName="User")
//        ,
//        foreignKeys = @ForeignKey(entity = TaskModel.class,
//        parentColumns = "Id",
//        childColumns = "userId"))
public class UserModel {
    @PrimaryKey
    @NonNull
    private String userId;                  // 3-digit callsign. For e.g., 111, 112
    private String password;
    private String accessToken;
    private String team;                    // For e.g., 1, Alpha
    private String role;                    // For e.g., CCT, Lead, Member
    private String radioConnectionStatus;
    private String lastKnownOnlineDateTime;

    public UserModel(String userId) {
        this.userId = userId;
    }

    // --- GETTER ---
    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getAccessToken() { return accessToken; }
    public String getTeam() { return team; }
    public String getRole() { return role; }
    public String getRadioConnectionStatus() { return radioConnectionStatus; }
    public String getLastKnownOnlineDateTime() { return lastKnownOnlineDateTime; }

    // --- SETTER ---
    public void setUserId(String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setTeam(String team) { this.team = team; }
    public void setRole(String role) { this.role = role; }
    public void setRadioConnectionStatus(String radioConnectionStatus) {
        this.radioConnectionStatus = radioConnectionStatus; }
    public void setLastKnownOnlineDateTime(String lastKnownOnlineDateTime) {
        this.lastKnownOnlineDateTime = lastKnownOnlineDateTime; }
}
