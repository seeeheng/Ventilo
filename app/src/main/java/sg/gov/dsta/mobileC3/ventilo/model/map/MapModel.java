package sg.gov.dsta.mobileC3.ventilo.model.map;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import lombok.Data;

@Data
@Entity(tableName="Map")
public class MapModel {
    @PrimaryKey
    @NonNull
    private String deckName;
    private String viewType;
    private String gaScale;
    private String floorAltitudeInCm;
    private String pixelWidth;
    private String lowerLeftX;
    private String lowerLeftY;
    private String upperRightX;
    private String upperRightY;
//    private String heightAboveFrameLineInCm;
//    private String heightBelowFrameLineInCm;

    public MapModel(@NonNull String deckName) {
        this.deckName = deckName;
    }

    // --- GETTER ---
    @NonNull
    public String getDeckName() { return deckName; }
    public String getViewType() { return viewType; }
    public String getGaScale() { return gaScale; }
    public String getFloorAltitudeInCm() { return floorAltitudeInCm; }
    public String getPixelWidth() { return pixelWidth; }
    public String getLowerLeftX() { return lowerLeftX; }
    public String getLowerLeftY() { return lowerLeftY; }
    public String getUpperRightX() { return upperRightX; }
    public String getUpperRightY() { return upperRightY; }
//    public String getHeightAboveFrameLineInCm() { return heightAboveFrameLineInCm; }
//    public String getHeightBelowFrameLineInCm() { return heightBelowFrameLineInCm; }

    // --- SETTER ---
    public void setDeckName(String deckName) { this.deckName = deckName; }
    public void setViewType(String viewType) { this.viewType = viewType; }
    public void setGaScale(String gaScale) { this.gaScale = gaScale; }
    public void setFloorAltitudeInCm(String floorAltitudeInCm) { this.floorAltitudeInCm = floorAltitudeInCm; }
    public void setPixelWidth(String pixelWidth) { this.pixelWidth = pixelWidth; }
    public void setLowerLeftX(String lowerLeftX) { this.lowerLeftX = lowerLeftX; }
    public void setLowerLeftY(String lowerLeftY) { this.lowerLeftY = lowerLeftY; }
    public void setUpperRightX(String upperRightX) { this.upperRightX = upperRightX; }
    public void setUpperRightY(String upperRightY) { this.upperRightY = upperRightY; }
//    public void setHeightAboveFrameLineInCm(String heightAboveFrameLineInCm) {
//        this.heightAboveFrameLineInCm = heightAboveFrameLineInCm; }
//    public void setHeightBelowFrameLineInCm(String heightBelowFrameLineInCm) {
//        this.heightBelowFrameLineInCm = heightBelowFrameLineInCm; }
}
