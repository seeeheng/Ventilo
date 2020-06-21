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
public class RawBFTModel {

    private long id;
    private String time;
    private String x;
    private String y;
    private String z;
    private String level;
    private String heading;
    private String fused_id;
    private String is_fused;

    // --- GETTER ---
    public long getId() { return id; }
    public String getTime() { return time; }
    public String getX() { return x; }
    public String getY() { return y; }
    public String getZ() { return z; }
    public String getLevel() { return level; }
    public String getHeading() { return heading; }
    public String getFusedId() { return fused_id; }
    public String getIsFused() { return is_fused; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setTime(String time) { this.time = time; }
    public void setX(String x) { this.x = x; }
    public void setY(String y) { this.y = y; }
    public void setZ(String z) { this.z = z; }
    public void setLevel(String level) { this.level = level; }
    public void setHeading(String heading) { this.heading = heading; }
    public void setFusedId(String fused_id) { this.fused_id = fused_id; }
    public void setIsFused(String is_fused) { this.is_fused = is_fused; }
}
