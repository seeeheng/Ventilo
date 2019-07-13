package sg.gov.dsta.mobileC3.ventilo.util;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

    public static void setDimensions(View view, int width, int height, int weight, ViewGroup viewGroup) {
        if (view.getLayoutParams() == null) {
            if (viewGroup instanceof LinearLayout) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height, weight);
                view.setLayoutParams(lp);
            }
        } else if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) view.getLayoutParams();
            p.width = width;
            p.height = height;
            p.weight = weight;
        }

        view.requestLayout();
    }

    public static void setDimensions(View view, int width, int height, ViewGroup viewGroup) {
        if (view.getLayoutParams() == null) {
            if (viewGroup instanceof LinearLayout) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
                view.setLayoutParams(lp);
            } else if (viewGroup instanceof RelativeLayout) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
                view.setLayoutParams(lp);
            } else if (viewGroup instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
                view.setLayoutParams(lp);
            }
        } else if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.width = width;
            p.height = height;
        }

        view.requestLayout();
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public static void setPaddings(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            view.setPadding(left, top, right, bottom);
            view.requestLayout();
        }
    }
}
