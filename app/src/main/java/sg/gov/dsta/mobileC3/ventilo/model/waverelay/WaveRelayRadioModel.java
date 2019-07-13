package sg.gov.dsta.mobileC3.ventilo.model.waverelay;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

@Data
@Entity(tableName = "WaveRelayRadio",
        foreignKeys = @ForeignKey(entity = UserModel.class,
                parentColumns = "userId",
                childColumns = "userId"))
public class WaveRelayRadioModel {

    @PrimaryKey
    private int radioId;
    @ColumnInfo(index = true)
    private String userId;
    private String radioIpAddress;
    private String phoneIpAddress;

    // --- GETTER ---
    public int getRadioId() { return radioId; }
    public String getUserId() { return userId; }
    public String getRadioIpAddress() { return radioIpAddress; }
    public String getPhoneIpAddress() { return phoneIpAddress; }

    // --- SETTER ---
    public void setRadioId(int radioId) { this.radioId = radioId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRadioIpAddress(String radioIpAddress) { this.radioIpAddress = radioIpAddress; }
    public void setPhoneIpAddress(String phoneIpAddress) { this.phoneIpAddress = phoneIpAddress; }
}
