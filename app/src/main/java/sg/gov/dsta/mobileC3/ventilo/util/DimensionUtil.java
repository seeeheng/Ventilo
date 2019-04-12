package sg.gov.dsta.mobileC3.ventilo.util;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DimensionUtil {

    public static int getScreenWidth() {
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

//        return displayMetrics.widthPixels;
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeightWithoutNavBar() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenHeightWithoutNavAndStatusBar() {
        return Resources.getSystem().getDisplayMetrics().heightPixels - getStatusBarHeight();
    }

    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Resources.getSystem().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int convertDpsToPixel(int dps) {
//        return  (int) (dps * Resources.getSystem().getDisplayMetrics().density + 0.5f);
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps,
                Resources.getSystem().getDisplayMetrics()) + 0.5f);
    }

    public static int convertPixelToDps(float pixel) {
        return  (int) (pixel / Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
