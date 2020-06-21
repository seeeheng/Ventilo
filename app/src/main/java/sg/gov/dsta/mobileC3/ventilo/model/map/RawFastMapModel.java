package sg.gov.dsta.mobileC3.ventilo.model.map;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName="Map")
public class RawFastMapModel {
    @PrimaryKey
    @NonNull
    private String time;
    private String id;
    private String map_file;
    private String level;
    private byte[] mapImage;
    private String origin_x;
    private String origin_y;
    private String origin_z;
    private String resolution;
    private String width;
    private String height;
    private String is_fused;
    private String fused_id;

    public RawFastMapModel(@NonNull String map_file) {
        this.map_file = map_file;
    }

    // --- GETTER ---
    @NonNull
    public String getTimeStamp() { return time; }
    public String getId() { return id; }
    public String getDeckName() { return map_file; }
    public String getLevel() { return level; }
    public byte[] getMapImage() { return mapImage; }
    public String getOriginX() { return origin_x; }
    public String getOriginY() { return origin_y; }
    public String getOriginZ() { return origin_z; }
    public String getGaScale() { return resolution; }
    public String getWidth() { return width; }
    public String getHeight() { return height; }
    public String getIsFused() { return is_fused; }
    public String getFusedId() { return fused_id; }
//    public String getHeightAboveFrameLineInCm() { return heightAboveFrameLineInCm; }
//    public String getHeightBelowFrameLineInCm() { return heightBelowFrameLineInCm; }

    // --- SETTER ---
    public void setTimeStamp(String time) { this.time = time; }
    public void setId(String id) { this.id = id; }
    public void setDeckName(String map_file) { this.map_file = map_file; }
    public void setLevel(String level) { this.level = level; }
    public void setMapImage(byte[] mapImage) { this.mapImage = mapImage; }
    public void setOriginX(String origin_x) { this.origin_x = origin_x; }
    public void setOriginY(String origin_y) { this.origin_y = origin_y; }
    public void setOriginZ(String origin_z) { this.origin_z = origin_z; }
    public void setGaScale(String resolution) { this.resolution = resolution; }
    public void setWidth(String width) { this.width = width; }
    public void setHeight(String height) { this.height = height; }
    public void setIsFused(String is_fused) { this.is_fused = is_fused; }
    public void setFusedId(String fused_id) { this.fused_id = fused_id; }
//    public void setHeightAboveFrameLineInCm(String heightAboveFrameLineInCm) {
//        this.heightAboveFrameLineInCm = heightAboveFrameLineInCm; }
//    public void setHeightBelowFrameLineInCm(String heightBelowFrameLineInCm) {
//        this.heightBelowFrameLineInCm = heightBelowFrameLineInCm; }
}
