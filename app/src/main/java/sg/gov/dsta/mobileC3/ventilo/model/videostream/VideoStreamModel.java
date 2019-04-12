package sg.gov.dsta.mobileC3.ventilo.model.videostream;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.drawable.Drawable;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Data
@Entity(foreignKeys = @ForeignKey(entity = UserModel.class,
        parentColumns = "userId",
        childColumns = "userId"))
public class VideoStreamModel {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId;
    private String name;
    private String url;
    private String iconType;

    // --- GETTER ---
    public long getId() { return id; }
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getIconType() { return iconType; }

    // --- SETTER ---
    public void setId(long id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setUrl(String url) { this.url = url; }
    public void setIconType(String iconType) { this.iconType = iconType; }
}
