package sg.gov.dsta.mobileC3.ventilo.model.videostream;

import android.graphics.drawable.Drawable;

import lombok.Data;

@Data
public class VideoStreamItemModel {
    private int id;
    private String name;
    private String url;
    private String iconType;
}
