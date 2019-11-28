package sg.gov.dsta.mobileC3.ventilo.model.sitrep;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(tableName = "SitRep")
public class SitRepModel {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private long refId;
    private String reporter;
    private byte[] snappedPhoto;
    private String reportType;

    // Mission
    private String location;
    private String activity;
    private int personnelT;
    private int personnelS;
    private int personnelD;
    private String nextCoa;
    private String request;
    private String others;

    // Inspection
    private String vesselType;
    private String vesselName;
    private String lpoc;
    private String npoc;
    private String lastVisitToSg;
    private String vesselLastBoarded;
    private String cargo;
    private String purposeOfCall;
    private String duration;
    private String currentCrew;
    private String currentMaster;
    private String currentCe;
    private String queries;

    // Both
    private String createdDateTime;
    private String lastUpdatedDateTime;
    private String isValid;

    // --- GETTER ---
    public long getId() { return id; }
    public long getRefId() { return refId; }
    public String getReporter() { return reporter; }
    public byte[] getSnappedPhoto() { return snappedPhoto; }
    public String getReportType() { return reportType; }

    public String getLocation() { return location; }
    public String getActivity() { return activity; }
    public int getPersonnelT() { return personnelT; }
    public int getPersonnelS() { return personnelS; }
    public int getPersonnelD() { return personnelD; }
    public String getNextCoa() { return nextCoa; }
    public String getRequest() { return request; }
    public String getOthers() { return others; }

    public String getVesselType() { return vesselType; }
    public String getVesselName() { return vesselName; }
    public String getLpoc() { return lpoc; }
    public String getNpoc() { return npoc; }
    public String getLastVisitToSg() { return lastVisitToSg; }
    public String getVesselLastBoarded() { return vesselLastBoarded; }
    public String getCargo() { return cargo; }
    public String getPurposeOfCall() { return purposeOfCall; }
    public String getDuration() { return duration; }
    public String getCurrentCrew() { return currentCrew; }
    public String getCurrentMaster() { return currentMaster; }
    public String getCurrentCe() { return currentCe; }
    public String getQueries() { return queries; }

    public String getCreatedDateTime() { return createdDateTime; }
    public String getLastUpdatedDateTime() { return lastUpdatedDateTime; }
    public String getIsValid() { return isValid; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setRefId(long refId) { this.refId = refId; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public void setSnappedPhoto(byte[] snappedPhoto) { this.snappedPhoto = snappedPhoto; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public void setLocation(String location) { this.location = location; }
    public void setActivity(String activity) { this.activity = activity; }
    public void setPersonnelT(int personnelT) { this.personnelT = personnelT; }
    public void setPersonnelS(int personnelS) { this.personnelS = personnelS; }
    public void setPersonnelD(int personnelD) { this.personnelD = personnelD; }
    public void setNextCoa(String nextCoa) { this.nextCoa = nextCoa; }
    public void setRequest(String request) { this.request = request; }
    public void setOthers(String others) { this.others = others; }

    public void setVesselType(String vesselType) { this.vesselType = vesselType; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }
    public void setLpoc(String lpoc) { this.lpoc = lpoc; }
    public void setNpoc(String npoc) { this.npoc = npoc; }
    public void setLastVisitToSg(String lastVisitToSg) { this.lastVisitToSg = lastVisitToSg; }
    public void setVesselLastBoarded(String vesselLastBoarded) { this.vesselLastBoarded = vesselLastBoarded; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public void setPurposeOfCall(String purposeOfCall) { this.purposeOfCall = purposeOfCall; }
    public void setDuration(String duration) { this.duration = duration; }
    public void setCurrentCrew(String currentCrew) { this.currentCrew = currentCrew; }
    public void setCurrentMaster(String currentMaster) { this.currentMaster = currentMaster; }
    public void setCurrentCe(String currentCe) { this.currentCe = currentCe; }
    public void setQueries(String queries) { this.queries = queries; }

    public void setCreatedDateTime(String createdDateTime) { this.createdDateTime = createdDateTime; }
    public void setLastUpdatedDateTime(String lastUpdatedDateTime) { this.lastUpdatedDateTime = lastUpdatedDateTime; }
    public void setIsValid(String isValid) { this.isValid = isValid; }
}
