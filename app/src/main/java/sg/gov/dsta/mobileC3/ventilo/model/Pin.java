package sg.gov.dsta.mobileC3.ventilo.model;

import android.graphics.PointF;

import lombok.Data;

@Data
public class Pin {
    private int pinID;
    private PointF pinInitialLocationPoint;
    private PointF pinCurrentLocationPoint;
    private float bearing;
}
