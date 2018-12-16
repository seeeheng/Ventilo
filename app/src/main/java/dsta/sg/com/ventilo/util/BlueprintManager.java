package dsta.sg.com.ventilo.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dsta.sg.com.ventilo.R;
import dsta.sg.com.ventilo.model.Blueprint;

public class BlueprintManager {

    private static final BlueprintManager INSTANCE = new BlueprintManager();
    private ArrayList<Blueprint> blueprintList;
    private Blueprint blueprintOnDisplay;

    public BlueprintManager() {
        init();
    }

    public static BlueprintManager getInstance() {
        return INSTANCE;
    }

    private void init() {
        blueprintList = new ArrayList<>();
        blueprintOnDisplay = new Blueprint();
    }

    public ArrayList<Blueprint> getBlueprintList() {
        return blueprintList;
    }

    public Blueprint getDisplayedBlueprint() {
        return blueprintOnDisplay;
    }

    public void setDisplayedBlueprint(Blueprint blueprint) {
        blueprintOnDisplay = blueprint;
    }

    public void initBlueprintList(Context context) {
        // Get all map images in drawable folder and store with corresponding blueprint information
        ArrayList<String> resNameArray = DrawableUtil.getResNameList("map");

        for (String resName : resNameArray) {
            Blueprint blueprint = new Blueprint();

            int resID = DrawableUtil.getResId(resName, R.drawable.class);
            blueprint.setResName(resName);
            blueprint.setResID(resID);

            String[] blueprintInfo = MapDistanceConversionUtil.decodeMapResourceName(context, resName, resID);
            blueprint.setName(blueprintInfo[0].toString());
            blueprint.setHeightInMetres(Float.valueOf(blueprintInfo[1]));
            blueprint.setLengthInMetres(Float.valueOf(blueprintInfo[2]));
            blueprint.setHeightInPixels(Integer.valueOf(blueprintInfo[3]));
            blueprint.setLengthInPixels(Integer.valueOf(blueprintInfo[4]));
            blueprintList.add(blueprint);
        }
    }
}
