package sg.gov.dsta.mobileC3.ventilo.model.bft;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

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
    @ColumnInfo(index = true)
    private String userId;
    private String xCoord;
    private String yCoord;
    private String altitude;
    private String bearing;
    private String type;          // For e.g. Fidgeting/Standing, Hazard, Deceased
    private String createdTime;

    // --- GETTER ---
    public long getId() { return id; }
    public String getUserId() { return userId; }
    public String getXCoord() { return xCoord; }
    public String getYCoord() { return yCoord; }
    public String getAltitude() { return altitude; }
    public String getBearing() { return bearing; }
    public String getType() { return type; }
    public String getCreatedTime() { return createdTime; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setXCoord(String xCoord) { this.xCoord = xCoord; }
    public void setYCoord(String yCoord) { this.yCoord = yCoord; }
    public void setAltitude(String altitude) { this.altitude = altitude; }
    public void setBearing(String bearing) { this.bearing = bearing; }
    public void setType(String type) { this.type = type; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }
}
