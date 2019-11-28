package sg.gov.dsta.mobileC3.ventilo.util.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidx.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.gov.dsta.mobileC3.ventilo.R;

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

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = marker.findViewById(R.id.circle_img_view_vessel_marker_icon);
        markerImage.setImageResource(resource);
        TextView tvName = marker.findViewById(R.id.tv_marker_vessel_name);
        tvName.setText(name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

}
