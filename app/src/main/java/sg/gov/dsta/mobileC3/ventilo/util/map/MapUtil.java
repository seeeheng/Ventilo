package sg.gov.dsta.mobileC3.ventilo.util.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

public class MapUtil {

    // Conversion of lat lon to x and y coordinate
    public static Point fromWgs84(double lon, double lat) {
        double x = lon * 111319.49079327358D;
        double y = Math.log(Math.max(0.0D, Math.tan(lat * 0.008726646259971648D + 0.7853981633974483D))) * 6378137.000000001D;
        return new Point(x, y);
    }

    public static LatLng toWgs84(double x, double y) {
        double lon = x * 8.983152841195214E-6D;
        if (lon < -180.0D) {
            lon = -180.0D;
        } else if (lon > 180.0D) {
            lon = 180.0D;
        }

        double lat = 114.59155902616465D * (Math.atan(Math.exp(y * 1.567855942887398E-7D)) - 0.7853981633974483D);
        return new LatLng(lat, lon);
    }

}
