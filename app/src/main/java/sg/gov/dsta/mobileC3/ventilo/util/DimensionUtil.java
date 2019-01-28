package sg.gov.dsta.mobileC3.ventilo.util;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;

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
}
