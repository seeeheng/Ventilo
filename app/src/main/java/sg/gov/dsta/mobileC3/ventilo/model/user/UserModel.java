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
    private String userId;                          // 3-digit callsign. For e.g., 111, 112
    private String password;
    private String accessToken;
    private String team;                            // For e.g., 1, Alpha
    private String role;                            // For e.g., CCT, Lead, Member
    private String phoneToRadioConnectionStatus;    // For e.g., Connected / Disconnected
    private String radioToNetworkConnectionStatus;  // For e.g., Connected / Disconnected
    private String radioFullConnectionStatus;       // For e.g., Online / Offline
    private String lastKnownOnlineDateTime;

    public UserModel(@NonNull String userId) {
        this.userId = userId;
    }

    // --- GETTER ---
    @NonNull
    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getAccessToken() { return accessToken; }
    public String getTeam() { return team; }
    public String getRole() { return role; }
    public String getPhoneToRadioConnectionStatus() { return phoneToRadioConnectionStatus; }
    public String getRadioToNetworkConnectionStatus() { return radioToNetworkConnectionStatus; }
    public String getRadioFullConnectionStatus() { return radioFullConnectionStatus; }
    public String getLastKnownOnlineDateTime() { return lastKnownOnlineDateTime; }

    // --- SETTER ---
    public void setUserId(@NonNull String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setTeam(String team) { this.team = team; }
    public void setRole(String role) { this.role = role; }
    public void setPhoneToRadioConnectionStatus(String phoneToRadioConnectionStatus) {
        this.phoneToRadioConnectionStatus = phoneToRadioConnectionStatus; }
    public void setRadioToNetworkConnectionStatus(String radioToNetworkConnectionStatus) {
        this.radioToNetworkConnectionStatus = radioToNetworkConnectionStatus; }
    public void setRadioFullConnectionStatus(String radioFullConnectionStatus) {
        this.radioFullConnectionStatus = radioFullConnectionStatus; }
    public void setLastKnownOnlineDateTime(String lastKnownOnlineDateTime) {
        this.lastKnownOnlineDateTime = lastKnownOnlineDateTime; }
}
