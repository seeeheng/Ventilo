package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Range;

import org.apache.commons.codec.binary.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapDistanceConversionUtil {
    private static final String TAG = "MapDistConversionUtil";

    private static final float ZERO_POINT_SIX_METRES_PER_UNIT = 0.6f;   // 600MM
    private static final float ZERO_POINT_SEVEN_METRES_PER_UNIT = 0.7f; // 700MM
    private static final float ZERO_POINT_EIGHT_METRES_PER_UNIT = 0.8f; // 800MM

//    private static final float METRES_PER_DENSITY_PIXEL_FOR_PLATFORM_DECK_LENGTH_800MM
//            = ZERO_POINT_EIGHT_METRES_PER_UNIT * 38 / 717;
//
//    private static final float METRES_PER_DENSITY_PIXEL_FOR_PLATFORM_DECK_HEIGHT_600MM
//            = ZERO_POINT_SIX_METRES_PER_UNIT / 518;

    private static final Map<Range<Integer>, Float> MAP_TO_ACTUAL_DIST_MAPPING = createTranslationMap();

    private static Map<Range<Integer>, Float> createTranslationMap() {
        Map<Range<Integer>, Float> map = new HashMap<Range<Integer>, Float>();
        map.put(Range.create(-10, 12), ZERO_POINT_SIX_METRES_PER_UNIT);
        map.put(Range.create(12, 164), ZERO_POINT_EIGHT_METRES_PER_UNIT);
        map.put(Range.create(164, 195), ZERO_POINT_SEVEN_METRES_PER_UNIT);
        map.put(Range.create(195, 225), ZERO_POINT_SIX_METRES_PER_UNIT);

        return map;
    }

    public static float convertMapToActualDistOf6M(float mapDistValue) {
        float actualDist = 0.0f;
        for (Float convertedValue : MAP_TO_ACTUAL_DIST_MAPPING.values()) {
            if (convertedValue == ZERO_POINT_SIX_METRES_PER_UNIT) {
                actualDist = mapDistValue * convertedValue;
            }
        }

        return actualDist;
    }

    public static float convertMapToActualDistOf7M(float mapDistValue) {
        float actualDist = 0.0f;
        for (Float convertedValue : MAP_TO_ACTUAL_DIST_MAPPING.values()) {
            if (convertedValue == ZERO_POINT_SEVEN_METRES_PER_UNIT) {
                actualDist = mapDistValue * convertedValue;
            }
        }

        return actualDist;
    }

    public static float convertMapToActualDistOf8M(float mapDistValue) {
        float actualDist = 0.0f;
        for (Float convertedValue : MAP_TO_ACTUAL_DIST_MAPPING.values()) {
            if (convertedValue == ZERO_POINT_EIGHT_METRES_PER_UNIT) {
                actualDist = mapDistValue * convertedValue;
            }
        }

        return actualDist;
    }

    /**
     * Extracts and assumes the first 3 sets of String value with format "(Starting Int Value)_(to)_(Ending Int Value)"
     * Extracts and assumes the last 3 sets of String value with format "(Starting Int Value)_(to)_(Ending Int Value)"
     * For example, "10_to_80" or "n10_to_70" where 'n' annotates that the corresponding value is negative, in this case, -10
     * Returns the following float[3]:
     * float[0] is the name of the blueprint
     * float[1] is the height of the blueprint
     * float[2] is the width of the blueprint
     **/
    public static String[] decodeMapResourceName(Context context, String mapResourceName, int mapResourceID) {
        String name = "";
        float actualHeightDistInMetres = 0.0f;
        float actualLengthDistInMetres = 0.0f;

        String[] mapResourceNameArray = mapResourceName.split("_");
        name = mapResourceNameArray[1].concat(" ").concat(mapResourceNameArray[2]);

        if (mapResourceNameArray.length >= 5) {

            actualHeightDistInMetres = convertStrMapHeightToActualDistInMetres(
                    mapResourceNameArray[mapResourceNameArray.length - 5]);

            String startingRange;

            // Changes 'n' to '-'. For e.g., "n10" to "-10"
            if (mapResourceNameArray[mapResourceNameArray.length - 3].contains("n")) {

                startingRange = "-".concat(mapResourceNameArray[mapResourceNameArray.length - 3].substring(1));
                System.out.println("if startingRange is " + Integer.valueOf(startingRange));
            } else {
                startingRange = mapResourceNameArray[mapResourceNameArray.length - 3];
                System.out.println("else startingRange is " + startingRange);
            }

            Integer[] actualDistRangeArray = new Integer[]{Integer.valueOf(startingRange),
                    Integer.valueOf(mapResourceNameArray[mapResourceNameArray.length - 1])};

            Range<Integer> actualDistRange = Range.create(actualDistRangeArray[0], actualDistRangeArray[1]);

            for (Map.Entry<Range<Integer>, Float> entry : MAP_TO_ACTUAL_DIST_MAPPING.entrySet()) {

                Range<Integer> keyRange = entry.getKey();
                Float unitValue = entry.getValue();

                try {
                    Range<Integer> intersectedRange = actualDistRange.intersect(keyRange);

                    actualLengthDistInMetres += (intersectedRange.getUpper() - intersectedRange.getLower()) * unitValue;

//                        if (actualDistRange.getLower() < keyRange.getLower()) {
//                            if (actualDistRange.getUpper() >= keyRange.getUpper()) {
//
//                                actualLengthDistInMetres += (keyRange.getUpper() - keyRange.getLower()) * unitValue;
//                                System.out.println("1 actualDistRange.getUpper() >= keyRange.getUpper() is " + actualLengthDistInMetres);
//                            } else {
//                                actualLengthDistInMetres += (actualDistRange.getUpper() - keyRange.getLower()) * unitValue;
//                                System.out.println("else 1 is " + actualLengthDistInMetres);
//                            }
//                        } else {
//                            if (actualDistRange.getUpper() >= keyRange.getUpper()) {
//                                actualLengthDistInMetres += (keyRange.getUpper() - actualDistRange.getLower()) * unitValue;
//                                System.out.println("2 actualDistRange.getUpper() >= keyRange.getUpper() is " + actualLengthDistInMetres);
//                            } else {
//                                actualLengthDistInMetres += (actualDistRange.getUpper() - actualDistRange.getLower()) * unitValue;
//                                System.out.println("else 2 is " + actualLengthDistInMetres);
//                            }
//                        }
                } catch (NullPointerException e) {
                    Log.d(TAG, "NullPointerException - Compared Range is Null");
                } catch (IllegalArgumentException e) {
                    Log.d(TAG, "IllegalArgumentException - Both ranges do not intersect");
                }
//                }
            }
        }

        BitmapFactory.Options dimensions = new BitmapFactory.Options();
        dimensions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), mapResourceID, dimensions);
        int actualHeightDistInPixels = dimensions.outHeight;
        int actualLengthDistInPixels = dimensions.outWidth;

        String[] actualDistParam = new String[]{name, String.valueOf(actualHeightDistInMetres),
                String.valueOf(actualLengthDistInMetres), String.valueOf(actualHeightDistInPixels),
                String.valueOf(actualLengthDistInPixels)};

        System.out.println("actualHeightDistInMetres is " + actualHeightDistInMetres);
        System.out.println("actualLengthDistInMetres is " + actualLengthDistInMetres);

        return actualDistParam;
    }

    public static float convertStrMapHeightToActualDistInMetres(String mapHeight) {
        Float actualHeightInMetres = 0.0f;

        // if height string does not contain all digits,
        // then it means it has the letter 'p' that represents the start of decimal places
        if (!mapHeight.matches("[0-9]+")) {
            mapHeight = mapHeight.replace('p', '.');
        }

        actualHeightInMetres = Float.valueOf(mapHeight) * ZERO_POINT_SIX_METRES_PER_UNIT;

        return actualHeightInMetres;
    }

    public static float getScaledImagePixelsPerMetre(int totalLengthByPixels, float totalLengthByMetres) {
        return totalLengthByPixels / totalLengthByMetres;
    }

    public static int getScaledImagePixels(int unscaledImagePixels, int densityScalingFactor) {
        return unscaledImagePixels * densityScalingFactor;
    }
//
//    public static float convertPixelsToMetres(float pixelsToConvert) {
//
//    }
}
