package sg.gov.dsta.mobileC3.ventilo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.lang.reflect.Field;
import java.util.ArrayList;

import sg.gov.dsta.mobileC3.ventilo.R;

public class DrawableUtil {

    public static ArrayList<String> getResNameList(String stringToExtract) {
        ArrayList<String> resNameArray = new ArrayList<>();
        Field[] fieldsID = R.drawable.class.getFields();

        for (Field f : fieldsID) {
            String[] mapResourceNameArray = f.getName().split("_");
            if (stringToExtract.equalsIgnoreCase(mapResourceNameArray[0])) {
                resNameArray.add(f.getName());
            }
        }

        return resNameArray;
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

//    public static ArrayList<String> getResNameListFromRaw(String stringToExtract) {
//        ArrayList<String> resNameArray = new ArrayList<>();
//        Field[] fieldsID = R.raw.class.getFields();
//
//        for (Field f : fieldsID) {
//            String[] mapResourceNameArray = f.getName().split("_");
//            if (stringToExtract.equalsIgnoreCase(mapResourceNameArray[0])) {
//                resNameArray.add(f.getName());
//            }
//        }
//
//        return resNameArray;
//    }

    public static boolean areDrawablesIdentical(Drawable drawableA, Drawable drawableB) {
        Drawable.ConstantState stateA = drawableA.getConstantState();
        Drawable.ConstantState stateB = drawableB.getConstantState();
        // If the constant state is identical, they are using the same drawable resource.
        // However, the opposite is not necessarily true.
        return (stateA != null && stateB != null && stateA.equals(stateB))
                || getBitmap(drawableA).sameAs(getBitmap(drawableB));
    }

    public static Bitmap getBitmap(Drawable drawable) {
        Bitmap result;
        if (drawable instanceof BitmapDrawable) {
            result = ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            // Some drawables have no intrinsic width - e.g. solid colours.
            if (width <= 0) {
                width = 1;
            }
            if (height <= 0) {
                height = 1;
            }

            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return result;
    }

    public static boolean IsValidImage(byte[] bytes) {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        if (bmp == null) {
            return false;
        }

        return true;
    }
}
