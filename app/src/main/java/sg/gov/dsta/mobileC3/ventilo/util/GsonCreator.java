package sg.gov.dsta.mobileC3.ventilo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonCreator {
    public static Gson createGson(){
        return new GsonBuilder().setDateFormat(DateTimeUtil.STANDARD_ISO_8601_DATE_TIME_FORMAT).
                serializeNulls().serializeSpecialFloatingPointValues().create();
    }
}
