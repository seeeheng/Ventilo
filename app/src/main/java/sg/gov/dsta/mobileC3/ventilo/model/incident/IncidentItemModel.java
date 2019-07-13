package sg.gov.dsta.mobileC3.ventilo.model.incident;

import android.graphics.drawable.Drawable;

import java.util.Date;

import lombok.Data;

@Data
public class IncidentItemModel {
    private String id;
    private String reporter;
    private Drawable reporterAvatar;
    private String title;
    private String description;
    private Date reportedDateTime;
}
