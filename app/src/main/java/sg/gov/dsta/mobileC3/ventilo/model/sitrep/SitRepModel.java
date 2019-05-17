package sg.gov.dsta.mobileC3.ventilo.model.sitrep;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "SitRep")
public class SitRepModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long refId;
    private String reporter;
    private byte[] snappedPhoto;
    private String location;
    private String activity;
    private int personnelT;
    private int personnelS;
    private int personnelD;
    private String nextCoa;
    private String request;
    private String createdDateTime;

    // --- GETTER ---
    public long getId() { return id; }
    public long getRefId() { return refId; }
    public String getReporter() { return reporter; }
    public byte[] getSnappedPhoto() { return snappedPhoto; }
    public String getLocation() { return location; }
    public String getActivity() { return activity; }
    public int getPersonnelT() { return personnelT; }
    public int getPersonnelS() { return personnelS; }
    public int getPersonnelD() { return personnelD; }
    public String getNextCoa() { return nextCoa; }
    public String getRequest() { return request; }
    public String getCreatedDateTime() { return createdDateTime; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setRefId(long refId) { this.refId = refId; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public void setSnappedPhoto(byte[] snappedPhoto) { this.snappedPhoto = snappedPhoto; }
    public void setLocation(String location) { this.location = location; }
    public void setActivity(String activity) { this.activity = activity; }
    public void setPersonnelT(int personnelT) { this.personnelT = personnelT; }
    public void setPersonnelS(int personnelS) { this.personnelS = personnelS; }
    public void setPersonnelD(int personnelD) { this.personnelD = personnelD; }
    public void setNextCoa(String nextCoa) { this.nextCoa = nextCoa; }
    public void setRequest(String request) { this.request = request; }
    public void setCreatedDateTime(String createdDateTime) { this.createdDateTime = createdDateTime; }
}
