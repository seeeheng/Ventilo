package sg.gov.dsta.mobileC3.ventilo.model.bft;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Data
@Entity(tableName = "BFT",
        foreignKeys = @ForeignKey(entity = UserModel.class,
        parentColumns = "userId",
        childColumns = "userId"))
public class BFTModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long refId;
    @ColumnInfo(index = true)
    private String userId;
    private String xCoord;
    private String yCoord;
    private String altitude;
    private String level;

    private String bearing;
    private String action;              // For e.g. Fidgeting, Standing, BeaconDrop
    private String type;                // For e.g. Hazard, Deceased
    private String createdDateTime;
    private int missingHeartBeatCount;

    // --- GETTER ---
    public long getId() { return id; }
    public long getRefId() { return refId; }
    public String getUserId() { return userId; }
    public String getXCoord() { return xCoord; }
    public String getYCoord() { return yCoord; }
    public String getAltitude() { return altitude; }
    public String getLevel() { return level; }
    public String getBearing() { return bearing; }
    public String getAction() { return action; }
    public String getType() { return type; }
    public String getCreatedDateTime() { return createdDateTime; }
    public int getMissingHeartBeatCount() { return missingHeartBeatCount; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setRefId(long refId) { this.refId = refId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setXCoord(String xCoord) { this.xCoord = xCoord; }
    public void setYCoord(String yCoord) { this.yCoord = yCoord; }
    public void setAltitude(String altitude) { this.altitude = altitude; }
    public void setLevel(String level) { this.level = level; }
    public void setBearing(String bearing) { this.bearing = bearing; }
    public void setAction(String action) { this.action = action; }
    public void setType(String type) { this.type = type; }
    public void setCreatedDateTime(String createdDateTime) { this.createdDateTime = createdDateTime; }
    public void setMissingHeartBeatCount(int missingHeartBeatCount) {
        this.missingHeartBeatCount = missingHeartBeatCount; }
}
