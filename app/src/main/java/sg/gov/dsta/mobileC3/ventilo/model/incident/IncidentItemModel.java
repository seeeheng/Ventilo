package sg.gov.dsta.mobileC3.ventilo.model.incident;

import android.graphics.drawable.Drawable;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

@Data
public class IncidentItemModel {
    private String id;
    private String reporter;
    private Drawable reporterAvatar;
    private String title;
    private String description;
}
