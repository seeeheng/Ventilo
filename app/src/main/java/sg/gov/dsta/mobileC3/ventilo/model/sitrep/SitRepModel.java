package sg.gov.dsta.mobileC3.ventilo.model.sitrep;

import android.graphics.drawable.Drawable;

import java.util.Date;

import lombok.Data;

@Data
public class SitRepModel {
    private int id;
    private String reporter;
    private Drawable reporterAvatar;
    private String location;
    private String activity;
    private int personnelT;
    private int personnelS;
    private int personnelD;
    private String nextCOA;
    private String request;
    private Date reportedDateTime;
}
