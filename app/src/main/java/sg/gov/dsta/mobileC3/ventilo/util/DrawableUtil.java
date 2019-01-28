package sg.gov.dsta.mobileC3.ventilo.util;

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
}
