package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.map.BFTLocalPreferences;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.bft.RawBFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.map.RawFastMapModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.map.EMapViewType;
import sg.gov.dsta.mobileC3.ventilo.util.enums.sitRep.EReportType;
import timber.log.Timber;

public class FileUtil {

    public static final String SIT_REP_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION =
            "Sit Rep Text File Content Saved Successfully";
    public static final String MOTION_LOG_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION =
            "Motion Log Text File Content Saved Successfully";
    public static final String DIRECTORY_PATH_KEY = "Directory Path";
    public static final File PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory();
    public static final String SUB_DIRECTORY_VEN = "Ven";
//    private static final File PUBLIC_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

    private static final String LEVEL_2_SUB_DIRECTORY_LOCATION_LOG = "Location Log";
    private static final String LEVEL_2_SUB_DIRECTORY_MAP_IMAGES = "Map Images";
    private static final String LEVEL_2_SUB_DIRECTORY_MAP_HTML = "Map Html";
    private static final String LEVEL_2_SUB_DIRECTORY_MAP_JSON = "Map Json";
    private static final String LEVEL_2_SUB_DIRECTORY_BFT_POS = "BFT Position";
    private static final String LEVEL_2_SUB_DIRECTORY_MOTION_LOG = "Motion Log";
    private static final String LEVEL_2_SUB_DIRECTORY_JS_TO_COPY = "js";
    private static final String LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY = "leaflet";
    private static final String LEVEL_2_SUB_DIRECTORY_JS_TO_PASTE = "js";
    private static final String LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_PASTE = "Leaflet";
    private static final String TEXT_FILE_SUFFIX = ".txt";
    private static final String PNG_FILE_SUFFIX = ".png";
    private static final String HTML_FILE_SUFFIX = ".html";
    private static final String SIT_REP = "SitRep";
    private static final String LOCATION_LOG = "Location_Log";
    private static final String MOTION_LOG = "Motion_Log";

    private static final String JSON_HEADER_POSITION = "position";

    private static final String DECK_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-deck-template.html";
    private static final String SIDE_PROFILE_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-side-profile-template.html";
    private static final String FRONT_PROFILE_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-front-profile-template.html";
//    private static final String SIT_REP_IMAGE = "SitRepImage";

    private static final String FASTMAP_DECK_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-deck-fastmap-template.html";

    private static File[] mapImageFiles;

    /**
     * Get specific file directory
     *
     * @param mainDirectoryFile
     * @param subDirectory
     * @return
     */
    public static File getFileDirectory(File mainDirectoryFile, String subDirectory) {
        File directory = new File(mainDirectoryFile, subDirectory);

        if (!directory.exists()) {
            Timber.i("%s directory not found, creating new folder...", subDirectory);
            directory.mkdirs();

        } else {
            Timber.i("%s directory found!", subDirectory);
        }

        return directory;
    }

    private static File[] getAllFilesInFolder(File fileDirectory) {
        return sortFileByName(fileDirectory.listFiles());
    }

    private static File[] sortFileByName(File[] fileArray) {

        if (fileArray != null) {
            Arrays.sort(fileArray, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    int n1 = extractNumber(file1.getName());
                    int n2 = extractNumber(file2.getName());
                    return n1 - n2;
                }

                private int extractNumber(String name) {
                    int i = 0;

                    try {
                        int s = name.indexOf(StringUtil.UNDERSCORE) + 1;
                        int e = name.lastIndexOf(StringUtil.DOT);
                        String number = name.substring(s, e);
                        i = Integer.parseInt(number);

                    } catch (Exception e) {
                        i = 0; // if filename does not match the format
                        // then default to 0
                    }
                    return i;
                }
            });
        }

        return fileArray;
    }

    private static JSONObject[] sortFastMapJsonFileByDateTimeField(JSONObject[] JsonObjectArray) {

        Arrays.sort(JsonObjectArray, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObj1, JSONObject jsonObj2) {

                try {
                    JSONObject pos1JsonObj = jsonObj1.getJSONObject(JSON_HEADER_POSITION);
                    JSONObject pos2JsonObj = jsonObj2.getJSONObject(JSON_HEADER_POSITION);

                    if (pos1JsonObj.has("time") && pos2JsonObj.has("time")) {

                        try {
                            int n1 = pos1JsonObj.getInt("time");
                            int n2 = pos2JsonObj.getInt("time");

                            return n1 - n2;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    Timber.d("JSON Object does not contain \"position\" field");
                }

                return -1;
            }
        });

        return JsonObjectArray;
    }


    private static String[] getAllFolderNamesInFolder(File fileDirectory) {
        File[] folders = getAllFilesInFolder(fileDirectory);

        ArrayList<String> availableFolderList = new ArrayList<>();

        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    availableFolderList.add(folder.getName().trim());
                }
            }
        }

        String[] availableFoldersArray = new String[availableFolderList.size()];
        availableFoldersArray = availableFolderList.toArray(availableFoldersArray);

        return availableFoldersArray;
    }

    private static String[] getAllFileNamesInFolder(File fileDirectory) {
        return fileDirectory.list();
    }

    private static String[] getAllFileNamesExcludingFolderInFolder(File fileDirectory) {
        File[] folders = getAllFilesInFolder(fileDirectory);

        ArrayList<String> availableFileList = new ArrayList<>();

        if (folders != null) {
            for (File file : folders) {
                if (!file.isDirectory()) {
                    availableFileList.add(file.getName().trim());
                }
            }
        }

        String[] availableFilesArray = new String[availableFileList.size()];
        availableFilesArray = availableFileList.toArray(availableFilesArray);
        availableFilesArray = sortFileName(availableFilesArray);

        return availableFilesArray;
    }

    private static String[] sortFileName(String[] fileNameArray) {

        if (fileNameArray != null) {
            Arrays.sort(fileNameArray, new Comparator<String>() {
                @Override
                public int compare(String fileName1, String fileName2) {
                    int n1 = extractNumber(fileName1);
                    int n2 = extractNumber(fileName2);
                    return n1 - n2;
                }

                private int extractNumber(String name) {
                    int i = 0;

                    try {
                        int s = name.indexOf(StringUtil.UNDERSCORE) + 1;
                        int e = name.lastIndexOf(StringUtil.DOT);
                        String number = name.substring(s, e);
                        i = Integer.parseInt(number);

                    } catch (Exception e) {
                        i = 0; // if filename does not match the format
                        // then default to 0
                    }
                    return i;
                }
            });
        }

        return fileNameArray;
    }

    private static JSONObject convertFileToJsonObject(File file) {
        StringBuilder posJson = new StringBuilder();
        JSONObject posJsonObj = null;

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                posJson.append(line);
                posJson.append('\n');
            }

            br.close();

            new JsonParser().parse(posJson.toString());
            posJsonObj = new JSONObject(posJson.toString());

        } catch (IOException | JsonParseException | JSONException e) {
            e.printStackTrace();
        }

        return posJsonObj;
    }

    private static List<JSONObject> getAllJsonObjFromFilesInFolder(File filePathDirectory) {

        File[] files = getAllFilesInFolder(filePathDirectory);

        List<JSONObject> jsonObjList = new ArrayList<>();

        if (files != null) {
            for (File file : files) {

                JSONObject jsonObj = convertFileToJsonObject(file);

                if (jsonObj != null) {
                    jsonObjList.add(jsonObj);
                }
            }
        }

        JSONObject[] jsonObjArray = new JSONObject[jsonObjList.size()];
        jsonObjArray = jsonObjList.toArray(jsonObjArray);
        jsonObjArray = sortFastMapJsonFileByDateTimeField(jsonObjArray);

        jsonObjList = Arrays.asList(jsonObjArray);


        return jsonObjList;

    }

    // **************************************** Map Blueprint **************************************** //

    // Extract file names without extension of Ven/Map Html folder in internal storage
    public static String[] getAllFileNamesWithoutExtensionInMapBlueprintHtmlFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);

        File[] listOfMapBlueprintHtmlFiles = getAllFilesInFolder(mapBlueprintHtmlDirectory);

        List<String> mapBlueprintHtmlFileNameList = new ArrayList<>();

        if (listOfMapBlueprintHtmlFiles != null) {
            for (File mapBlueprintHtmlFile : listOfMapBlueprintHtmlFiles) {
                if (mapBlueprintHtmlFile.getName().contains(HTML_FILE_SUFFIX)) {
                    mapBlueprintHtmlFileNameList.add(FilenameUtils.removeExtension(mapBlueprintHtmlFile.getName().toLowerCase()));
                }
            }
        }

        return mapBlueprintHtmlFileNameList.toArray(new String[0]);
    }

    // Extract file names without extension of Ven/Map Html/<User ID> folder in internal storage
    public static String[] getAllFileNamesWithoutExtensionInMapBlueprintHtmlFolder(String userId) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);
        File userIdDirectory = getFileDirectory(mapBlueprintHtmlDirectory, userId);

        File[] listOfMapBlueprintHtmlFiles = getAllFilesInFolder(userIdDirectory);

        List<String> mapBlueprintHtmlFileNameList = new ArrayList<>();

        if (listOfMapBlueprintHtmlFiles != null) {
            for (File mapBlueprintHtmlFile : listOfMapBlueprintHtmlFiles) {
                mapBlueprintHtmlFileNameList.add(FilenameUtils.removeExtension(mapBlueprintHtmlFile.getName().toLowerCase()));
            }
        }

        return mapBlueprintHtmlFileNameList.toArray(new String[0]);
    }

    // Extract folder names of Ven/Map Html/<User ID> folder in internal storage
    public static String[] getAllFolderNamesInMapBlueprintHtmlFolder(String userId) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);
        File userIdDirectory = getFileDirectory(mapBlueprintHtmlDirectory, userId);

        return getAllFolderNamesInFolder(userIdDirectory);
    }

    // Extract file names of Ven/Map Html folder in internal storage
    public static String[] getAllFileNamesInMapBlueprintHtmlFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);

        return getAllFileNamesExcludingFolderInFolder(mapBlueprintHtmlDirectory);
    }

    // Extract file names of Ven/Map Html/<User ID> folder in internal storage
    public static String[] getAllMapBlueprintHtmlFilesInFolder(String userId) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);
        File userIdDirectory = getFileDirectory(mapBlueprintHtmlDirectory, userId);

        return getAllFileNamesExcludingFolderInFolder(userIdDirectory);
    }

    // Extract file names of Ven/Map Html/<User ID> folder in internal storage
    public static String[] getAllMapBlueprintHtmlFilesInFolder(String userId, String floorLevel) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);
        File userIdDirectory = getFileDirectory(mapBlueprintHtmlDirectory, userId);
        File userMapBlueprintHtmlDirectory = getFileDirectory(userIdDirectory, floorLevel);

        return getAllFileNamesExcludingFolderInFolder(userMapBlueprintHtmlDirectory);
    }

    // Get files of map images of directory
    public static File[] getAllFilesInMapBlueprintImagesFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintImagesDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_IMAGES);

        return getAllFilesInFolder(mapBlueprintImagesDirectory);
    }

    // Get map image files in corresponding user ID directory
    public static File[] getAllMapImageFilesInFolder(String userId) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintImagesDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_IMAGES);
        File userIdDirectory = getFileDirectory(mapBlueprintImagesDirectory, userId);

        File[] mapImageFileArray = new File[0];

        try (Stream<Path> paths = Files.walk(Paths.get(userIdDirectory.getAbsolutePath()))) {
            List<File> mapImageFileList = paths.filter(Files::isRegularFile).map(x -> x.toFile()).collect(Collectors.toList());

            mapImageFileArray = new File[mapImageFileList.size()];
            mapImageFileList.toArray(mapImageFileArray);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapImageFileArray;
    }

    /**
     * Transfer all files from assets sub folder to Ven corresponding sub folder
     */
    private static void transferAllFilesInAssetsSubFolderToExternalFolder(String subDirectoryNameToCopy, String subDirectoryNameToPaste) {
        try {
            String[] filenamesInAssets = MainApplication.getAppContext().
                    getAssets().list(subDirectoryNameToCopy);

            if (filenamesInAssets != null) {

                for (int i = 0; i < filenamesInAssets.length; i++) {

                    String filenameInAssets = filenamesInAssets[i];
                    String filePathInAssets = subDirectoryNameToCopy.concat(StringUtil.TRAILING_SLASH).concat(filenameInAssets);

                    InputStream fileIS = MainApplication.getAppContext().
                            getAssets().open(filePathInAssets);

                    // Create image to html files
                    File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
                    File subDirectory = getFileDirectory(venDirectory, subDirectoryNameToPaste);

                    File jsFile = new File(subDirectory, filenameInAssets);

                    try (OutputStream outputStream = new FileOutputStream(jsFile, false)) {

                        // Copy file contents of existing folder to newly created file in external storage
                        IOUtils.copy(fileIS, outputStream);

                    } catch (FileNotFoundException e) {
                        // handle exception here
                    } catch (IOException e) {
                        // handle exception here
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Save Map image into corresponding file directory
     *
     * @param mapModel
     * @param mapImage
     */
    public static void saveMapImageIntoFileDirectory(MapModel mapModel, byte[] mapImage) {

        Timber.i("Storing Map image into corresponding file directory");

        if (mapImage != null &&
                DrawableUtil.IsValidImage(mapImage)) {

            File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
            File mapImageDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_IMAGES);
            File mapImageFile = new File(mapImageDirectory, mapModel.getDeckName());

            try {
                // Save image file
                FileOutputStream out = new FileOutputStream(mapImageFile);

                Bitmap mapImageBitmap = DrawableUtil.getBitmapFromBytes(mapImage);
                mapImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();

                Timber.i("Map image file saved");

                notifySitRepTextFileSavedBroadcastIntent(mapImageFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("Error writing Map image into file...");

            }
        }
    }

    // Get file path of map html directory (Ven/Html Images)
    public static String getMapBlueprintImagesFilePath() {

        StringBuilder filePathStrBuilder = new StringBuilder();

        filePathStrBuilder.append(PUBLIC_DIRECTORY.getPath());
        filePathStrBuilder.append(StringUtil.TRAILING_SLASH);
        filePathStrBuilder.append(SUB_DIRECTORY_VEN);
        filePathStrBuilder.append(StringUtil.TRAILING_SLASH);
        filePathStrBuilder.append(LEVEL_2_SUB_DIRECTORY_MAP_HTML);

        return filePathStrBuilder.toString();
    }

//    private void createHtmlFilesFromMapBlueprintImages(File[] imageFiles) {
//
//    }

    // **************************************** BFT Position **************************************** //

    /**
     * Extract file names without extension of Ven/BFT Position folder in internal storage
     *
     * @return
     */
    public static String[] getAllFolderNamesInBftPosFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File bftPosDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_BFT_POS);

        return getAllFolderNamesInFolder(bftPosDirectory);
    }

    /**
     * Extract converted JSON Objects from files in corresponding FastMap BFT Position Call Sign folder in internal storage
     *
     * @param folderName
     * @return
     */
    public static List<JSONObject> getJsonObjListInBftPosCallSignFolder(String folderName) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File bftPosDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_BFT_POS);
        File bftPosCallSignDirectory = getFileDirectory(bftPosDirectory, folderName);

        return getAllJsonObjFromFilesInFolder(bftPosCallSignDirectory);
    }

    /**
     * Extract converted RawBFTModel list from JSON object list in designated folder
     *
     * @param folderName
     * @return
     */
    public static List<RawBFTModel> getRawBftModelListFromJsonObjListInFolder(String folderName) {

        List<JSONObject> rawBftJsonObjList = getJsonObjListInBftPosCallSignFolder(folderName);

        List<RawBFTModel> rawBftModelList = new ArrayList<>();

        for (JSONObject jsonObj : rawBftJsonObjList) {

            RawBFTModel rawBftModel = getRawBftModelFromJsonObj(jsonObj);
            rawBftModelList.add(rawBftModel);
        }

        return rawBftModelList;
    }

    /**
     * Extract raw BFT models from JSON Object
     *
     * @param jsonObj
     * @return
     */
    public static RawBFTModel getRawBftModelFromJsonObj(JSONObject jsonObj) {
        try {
            JSONObject posJsonObj = jsonObj.getJSONObject(JSON_HEADER_POSITION);

            Gson gson = GsonCreator.createGson();

            RawBFTModel rawBftModel = gson.fromJson(posJsonObj.toString(), RawBFTModel.class);
            return rawBftModel;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Extract converted JSON Objects from files in corresponding FastMap Map folder in internal storage
     *
     * @return
     */
    public static List<JSONObject> getJsonObjListInMapJsonFolder(String userIdFolderName, String levelFolderName) {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapJsonDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_JSON);
        File userIdDirectory = getFileDirectory(mapJsonDirectory, userIdFolderName);
        File levelDirectory = getFileDirectory(userIdDirectory, levelFolderName);
//        File mapDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_FASTMAP_MAP);

        return getAllJsonObjFromFilesInFolder(levelDirectory);
    }

    /**
     * Extract converted RawBFTModel list from JSON object list in designated folder
     *
     * @return
     */
    public static List<RawFastMapModel> getRawMapModelListFromJsonObjListInFolder(String userIdFolderName, String levelFolderName) {

        List<JSONObject> rawMapJsonObjList = getJsonObjListInMapJsonFolder(userIdFolderName, levelFolderName);

        List<RawFastMapModel> rawMapModelList = new ArrayList<>();

        for (JSONObject jsonObj : rawMapJsonObjList) {

            RawFastMapModel rawMapModel = getRawMapModelFromJsonObj(jsonObj);
            rawMapModelList.add(rawMapModel);
        }

//        RawFastMapModel[] rawMapModelArray = new RawFastMapModel[rawMapModelList.size()];
//        rawMapModelList.toArray(rawMapModelArray);
//        rawMapModelList = sortRawMapModelByDeckName(rawMapModelArray);

        return rawMapModelList;
    }

    private static List<RawFastMapModel> sortRawMapModelByDeckName(RawFastMapModel[] rawMapModelArray) {
        Arrays.sort(rawMapModelArray, new Comparator<RawFastMapModel>() {
            @Override
            public int compare(RawFastMapModel rawFastMapModel1, RawFastMapModel rawFastMapModel2) {
                int n1 = extractNumber(rawFastMapModel1.getDeckName());
                int n2 = extractNumber(rawFastMapModel2.getDeckName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;

                try {
                    int s = name.indexOf(StringUtil.UNDERSCORE) + 1;
                    int e = name.lastIndexOf(StringUtil.DOT);
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);

                } catch (Exception e) {
                    i = 0; // if filename does not match the format
                    // then default to 0
                }
                return i;
            }
        });

        return new ArrayList<>(Arrays.asList(rawMapModelArray));
    }

    /**
     * Extract raw Fastmap Map models from JSON Object
     *
     * @param jsonObj
     * @return
     */
    public static RawFastMapModel getRawMapModelFromJsonObj(JSONObject jsonObj) {
        try {
            JSONObject mapJsonObj = jsonObj.getJSONObject(JSON_HEADER_POSITION);

            Gson gson = GsonCreator.createGson();

            System.out.println("mapJsonObj: " + mapJsonObj);

            RawFastMapModel rawMapModel = gson.fromJson(mapJsonObj.toString(), RawFastMapModel.class);
            return rawMapModel;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    // **************************************** Location Logs **************************************** //

    public static File getLocationLogParentFileDirectory() {

        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);

        File locationLogDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_LOCATION_LOG);

        String currentDayMonthYearDate = DateTimeUtil.
                dateToCustomDateStringFormat(DateTimeUtil.
                        stringToDate(DateTimeUtil.getCurrentDateTime()));

        File locationLogDirectoryByDate = getFileDirectory(locationLogDirectory, currentDayMonthYearDate);

        return locationLogDirectoryByDate;
    }

    /**
     * Get location log file name in parent file directory
     *
     * @return
     */
    public static String getLocationLogFileName() {
        // Text file name
        StringBuilder textFileNameBuilder = new StringBuilder();
        String currentDateInFileFormat = DateTimeUtil.dateToCustomDateFileStringFormat(
                DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));

        textFileNameBuilder.append(LOCATION_LOG);
        textFileNameBuilder.append(StringUtil.UNDERSCORE);
        textFileNameBuilder.append(currentDateInFileFormat);
        textFileNameBuilder.append(TEXT_FILE_SUFFIX);

        return textFileNameBuilder.toString();
    }

//    /**
//     * Save location data log into file directory
//     *
//     * @param locationLog
//     */
//    public static void saveLocationLogIntoFile(String locationLog) {
//
//        Timber.i("Finding location data log directory for file storage...");
//
//        if (locationLog != null) {
//            File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
//
//            File locationLogDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_LOCATION_LOG);
//
//            String currentDayMonthYearDate = DateTimeUtil.
//                    dateToCustomDateStringFormat(DateTimeUtil.
//                            stringToDate(DateTimeUtil.getCurrentDateTime()));
//
//            File locationLogDirectoryByDate = getFileDirectory(locationLogDirectory, currentDayMonthYearDate);
//
//            Timber.i("Checking / Creating file name...");
//
//            // Text file name
//            StringBuilder textFileNameBuilder = new StringBuilder();
//            String currentDateInFileFormat = DateTimeUtil.dateToCustomDateFileStringFormat(
//                    DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));
//
//            textFileNameBuilder.append(LOCATION_LOG);
//            textFileNameBuilder.append(StringUtil.UNDERSCORE);
//            textFileNameBuilder.append(currentDateInFileFormat);
//            textFileNameBuilder.append(TEXT_FILE_SUFFIX);
//
//            File locationLogTextFile = new File(locationLogDirectoryByDate, textFileNameBuilder.toString());
//
//            try {
//
//
//                FileWriter writer;
//
//                // Save text file
//                if (!locationLogTextFile.exists()) {
//
//                    writer = new FileWriter(locationLogTextFile);
//
//                } else {
//
//                    writer = new FileWriter(locationLogTextFile, true);
//                    writer.append(System.lineSeparator());
//                    writer.append(System.lineSeparator());
//                    writer.append(StringUtil.HYPHEN_DIVIDER);
//                    writer.append(System.lineSeparator());
//                    writer.append(System.lineSeparator());
//
//                }
//
//                Timber.i("Writing location data log content into text file...");
//
//                String currentDateTimeFormat = DateTimeUtil.dateToCustomDateTimeStringFormat(
//                        DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));
//
//                writer.append(currentDateTimeFormat);
//                writer.append(System.lineSeparator());
//                writer.append(System.lineSeparator());
//
//                String newFileHeader = MainApplication.getAppContext().
//                        getString(R.string.map_blueprint_test_log_new_file_created);
//
//                if (!locationLog.contains(newFileHeader)) {
//                    writer.append(newFileHeader);
//                    writer.append(System.lineSeparator());
//                }
//
//                writer.append(locationLog);
//
//                writer.flush();
//                writer.close();
//
//                Timber.i("Location log file saved!");
//
//                notifyMotionLogTextFileSavedBroadcastIntent(locationLogDirectoryByDate.getAbsolutePath());
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Timber.d("Error writing location log into file...");
//
//            }
//
//        } else {
//            Timber.d("Location log is null. Unable to write into file...");
//        }
//    }

    // **************************************** Sit Rep **************************************** //

    /**
     * Save Sit Rep into file directory with provided Sit Rep
     *
     * @param sitRepModel
     */
    public static void saveSitRepIntoFile(SitRepModel sitRepModel) {
//        String sitRepPath = PUBLIC_DIRECTORY.getAbsolutePath().
//                concat(StringUtil.TRAILING_SLASH).concat(FragmentConstants.KEY_SITREP);
        Timber.i("Finding Sit Rep directory for file storage...");

        if (sitRepModel != null) {
            File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);

            File sitRepDirectory = getFileDirectory(venDirectory, FragmentConstants.KEY_SITREP);

            String currentDayMonthYearDate = DateTimeUtil.
                    dateToCustomDateStringFormat(DateTimeUtil.
                            stringToDate(DateTimeUtil.getCurrentDateTime()));

            File sitRepDirectoryByDate = getFileDirectory(sitRepDirectory, currentDayMonthYearDate);

            File sitRepDateDirectoryByUserId = getFileDirectory(sitRepDirectoryByDate, sitRepModel.getReporter());

            Timber.i("Creating file name...");

            // Text file name
            StringBuilder textFileNameBuilder = new StringBuilder();
            String currentTimeInFileFormat = DateTimeUtil.dateToCustomDateTimeFileStringFormat(
                    DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));

            textFileNameBuilder.append(sitRepModel.getReporter());
            textFileNameBuilder.append(SIT_REP);
            textFileNameBuilder.append(StringUtil.UNDERSCORE);
            textFileNameBuilder.append(currentTimeInFileFormat);
            textFileNameBuilder.append(TEXT_FILE_SUFFIX);

            File sitRepTextFile = new File(sitRepDateDirectoryByUserId, textFileNameBuilder.toString());

            // Image file name
            StringBuilder imageFileNameBuilder = new StringBuilder();

            imageFileNameBuilder.append(sitRepModel.getReporter());
            imageFileNameBuilder.append(SIT_REP);
            imageFileNameBuilder.append(StringUtil.UNDERSCORE);
            imageFileNameBuilder.append(currentTimeInFileFormat);
            imageFileNameBuilder.append(PNG_FILE_SUFFIX);

            File sitRepImageFile = new File(sitRepDateDirectoryByUserId, imageFileNameBuilder.toString());

            try {

                // Save text file
                FileWriter writer = new FileWriter(sitRepTextFile);

                String sitRepContent = createSitRepInTextFile(sitRepModel);

                Timber.i("Writing Sit Rep content into text file...");
                writer.append(sitRepContent);

                writer.flush();
                writer.close();

                if (sitRepModel.getSnappedPhoto() != null &&
                        DrawableUtil.IsValidImage(sitRepModel.getSnappedPhoto())) {
                    // Save image file
                    FileOutputStream out = new FileOutputStream(sitRepImageFile);

                    Bitmap snappedPhotoBitmap = DrawableUtil.getBitmapFromBytes(sitRepModel.getSnappedPhoto());
                    snappedPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                }

                Timber.i("Sit Rep file(s) saved!");

                notifySitRepTextFileSavedBroadcastIntent(sitRepDateDirectoryByUserId.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("Error writing Sit Rep into file...");

            }

        } else {
            Timber.d("Sit Rep is null. Unable to write into file...");
        }
    }

    /**
     * Creates Sit Rep content for text file from Sit Rep Model
     *
     * @param sitRepModel
     * @return
     */
    private static String createSitRepInTextFile(SitRepModel sitRepModel) {
        Timber.i("Creating Sit Rep text file content...");

        StringBuilder stringBuilder = new StringBuilder();

        // Header
        String headerContent = sitRepModel.getReporter().concat(StringUtil.SPACE).
                concat(FragmentConstants.KEY_SITREP).concat(System.lineSeparator()).
                concat(System.lineSeparator());

        // For 'Mission' type Sit Rep
        if (EReportType.MISSION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {

            // Location
            String locationContent = MainApplication.getAppContext().getString(R.string.sitrep_location).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getLocation()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Activity
            String activityContent = MainApplication.getAppContext().getString(R.string.sitrep_activity).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getActivity()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Threat Personnel Count
            String TContent = MainApplication.getAppContext().getString(R.string.sitrep_T).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(String.valueOf(sitRepModel.getPersonnelT())).
                    concat(System.lineSeparator());

            // Suspect Personnel Count
            String SContent = MainApplication.getAppContext().getString(R.string.sitrep_S).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(String.valueOf(sitRepModel.getPersonnelS())).
                    concat(System.lineSeparator());

            // Dead Personnel Count
            String DContent = MainApplication.getAppContext().getString(R.string.sitrep_D).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(String.valueOf(sitRepModel.getPersonnelD())).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Next Course of Action
            String nextCoaContent = MainApplication.getAppContext().getString(R.string.sitrep_next_coa).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getNextCoa()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Request
            String requestContent = MainApplication.getAppContext().getString(R.string.sitrep_request).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getRequest()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Others
            String others = StringUtil.N_A;

            if (sitRepModel.getOthers() != null) {
                others = sitRepModel.getOthers();
            }

            String othersContent = MainApplication.getAppContext().getString(R.string.sitrep_others).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).concat(others);

            stringBuilder.append(headerContent);
            stringBuilder.append(locationContent);
            stringBuilder.append(activityContent);
            stringBuilder.append(TContent);
            stringBuilder.append(SContent);
            stringBuilder.append(DContent);
            stringBuilder.append(nextCoaContent);
            stringBuilder.append(requestContent);
            stringBuilder.append(othersContent);

        } else if (EReportType.INSPECTION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {
            // For 'Inspection' type Sit Rep

            // Vessel Type
            String vesselTypeContent = MainApplication.getAppContext().getString(R.string.sitrep_vessel_type).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getVesselType()).
                    concat(System.lineSeparator());

            // Vessel Name
            String vesselNameContent = MainApplication.getAppContext().getString(R.string.sitrep_vessel_name).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getVesselName()).
                    concat(System.lineSeparator());

            // LPOC
            String lpocContent = MainApplication.getAppContext().getString(R.string.sitrep_lpoc).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getLpoc()).
                    concat(System.lineSeparator());

            // NPOC
            String npocContent = MainApplication.getAppContext().getString(R.string.sitrep_npoc).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getNpoc()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Last Visit to SG
            String lastVisitToSgContent = MainApplication.getAppContext().getString(R.string.sitrep_last_visit_to_sg).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getLastVisitToSg()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Vessel Last Boarded
            String vesselLastBoardedContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_vessel_last_boarded).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getVesselLastBoarded()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Cargo
            String cargoContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_cargo).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getCargo()).
                    concat(System.lineSeparator()).concat(System.lineSeparator());

            // Purpose of Call
            String purposeOfCallContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_purpose_of_call).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getPurposeOfCall()).
                    concat(System.lineSeparator());

            // Duration
            String durationContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_duration).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getDuration()).
                    concat(System.lineSeparator());

            // Current Crew
            String currentCrewContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_current_crew).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getCurrentCrew()).
                    concat(System.lineSeparator());

            // Current Master
            String currentMasterContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_current_master).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getCurrentMaster()).
                    concat(System.lineSeparator());

            // Current CE
            String currentCeContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_current_ce).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getCurrentCe()).
                    concat(System.lineSeparator());

            // Queries
            String queriesContent = MainApplication.getAppContext().
                    getString(R.string.sitrep_queries).
                    concat(StringUtil.COLON).concat(StringUtil.SPACE).
                    concat(sitRepModel.getQueries());

            stringBuilder.append(vesselTypeContent);
            stringBuilder.append(vesselNameContent);
            stringBuilder.append(lpocContent);
            stringBuilder.append(npocContent);
            stringBuilder.append(lastVisitToSgContent);
            stringBuilder.append(vesselLastBoardedContent);
            stringBuilder.append(cargoContent);
            stringBuilder.append(purposeOfCallContent);
            stringBuilder.append(durationContent);
            stringBuilder.append(currentCrewContent);
            stringBuilder.append(currentMasterContent);
            stringBuilder.append(currentCeContent);
            stringBuilder.append(queriesContent);

        }

        return stringBuilder.toString();
    }

    /**
     * Notify Sit Rep broadcast listeners of file save status with provided file path
     */
    private static synchronized void notifySitRepTextFileSavedBroadcastIntent(String fileAbsolutePath) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(DIRECTORY_PATH_KEY, fileAbsolutePath);

        broadcastIntent.setAction(SIT_REP_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }


    // **************************************** Test Log **************************************** //

    /**
     * Save Motion Log into file directory
     *
     * @param motionLog
     */
    public static void saveMotionLogIntoFile(String motionLog) {

        Timber.i("Finding motion log directory for file storage...");

        if (motionLog != null) {
            File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);

            File motionLogDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MOTION_LOG);

            String currentDayMonthYearDate = DateTimeUtil.
                    dateToCustomDateStringFormat(DateTimeUtil.
                            stringToDate(DateTimeUtil.getCurrentDateTime()));

            File motionLogDirectoryByDate = getFileDirectory(motionLogDirectory, currentDayMonthYearDate);

            Timber.i("Checking / Creating file name...");

            // Text file name
            StringBuilder textFileNameBuilder = new StringBuilder();
            String currentDateInFileFormat = DateTimeUtil.dateToCustomDateFileStringFormat(
                    DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));


            textFileNameBuilder.append(MOTION_LOG);
            textFileNameBuilder.append(StringUtil.UNDERSCORE);
            textFileNameBuilder.append(currentDateInFileFormat);
            textFileNameBuilder.append(TEXT_FILE_SUFFIX);

            File motionLogTextFile = new File(motionLogDirectoryByDate, textFileNameBuilder.toString());

            try {


                FileWriter writer;

                // Save text file
                if (!motionLogTextFile.exists()) {

                    writer = new FileWriter(motionLogTextFile);

                } else {

                    writer = new FileWriter(motionLogTextFile, true);
                    writer.append(System.lineSeparator());
                    writer.append(System.lineSeparator());
                    writer.append(StringUtil.HYPHEN_DIVIDER);
                    writer.append(System.lineSeparator());
                    writer.append(System.lineSeparator());

                }

                Timber.i("Writing motion log content into text file...");

                String currentDateTimeFormat = DateTimeUtil.dateToCustomDateTimeStringFormat(
                        DateTimeUtil.stringToDate(DateTimeUtil.getCurrentDateTime()));

                writer.append(currentDateTimeFormat);
                writer.append(System.lineSeparator());
                writer.append(System.lineSeparator());

                String newFileHeader = MainApplication.getAppContext().
                        getString(R.string.map_blueprint_test_log_new_file_created);

                if (!motionLog.contains(newFileHeader)) {
                    writer.append(newFileHeader);
                    writer.append(System.lineSeparator());
                }

                writer.append(motionLog);

                writer.flush();
                writer.close();

                Timber.i("Motion log file saved!");

                notifyMotionLogTextFileSavedBroadcastIntent(motionLogDirectoryByDate.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                Timber.d("Error writing motion log into file...");

            }

        } else {
            Timber.d("Motion log is null. Unable to write into file...");
        }
    }

    /**
     * Notify motion log broadcast listener of file save status with provided file path
     */
    private static synchronized void notifyMotionLogTextFileSavedBroadcastIntent(String fileAbsolutePath) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(DIRECTORY_PATH_KEY, fileAbsolutePath);

        broadcastIntent.setAction(MOTION_LOG_FILE_SAVED_SUCCESSFULLY_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

    /**
     * Get Bit map from file filePath
     *
     * @param filePath
     * @return
     */
    public static Bitmap getBitmapFromFile(String filePath) {
        try {
            Bitmap bitmap = null;
            File f = new File(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getBitmapFromInputStream(InputStream inputStream) {
        try {
            Bitmap bitmap = null;
//            File f = new File(filePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create relevant html files from each map blueprint images
     */
    public static void createHtmlFilesFromImagesUsingAssetsTemplate(List<MapModel> mapModelList) {
        File[] mapImageFiles = getAllFilesInMapBlueprintImagesFolder();

        createHtmlFilesFromImagesUsingAssetsTemplate(mapModelList, mapImageFiles);
    }

    /**
     * Create relevant html files from each map blueprint images
     */
    public static void createHtmlFilesFromImagesUsingAssetsTemplate(List<MapModel> mapModelList, File[] mapImageFiles) {

//        File[] mapImageFiles = getAllFilesInMapBlueprintImagesFolder();
//        File[] mapImageFiles = getAllMapImageFilesInFolder();

        boolean isHtmlCreated = false;

        // Iterate through all map image files in 'Map Images' folder
        for (File mapImageFile : mapImageFiles) {

            if (mapImageFile.isFile()) {

                // Iterate through all map models in database to find a match in name and extract all relevant details
                for (MapModel mapModel : mapModelList) {

                    String mapImageFileNameWithoutExtension = FilenameUtils.removeExtension(mapImageFile.getName().toLowerCase());
                    String[] mapNameGroup = StringUtil.removeUnderscores(mapImageFileNameWithoutExtension);
                    String mapDeckName = mapNameGroup[1]; // 0 - Ship Name, 1 - Deck Name

                    Timber.i("mapDeckName: %s", mapDeckName);
                    Timber.i("mapModel.getDeckName(): %s", mapModel.getDeckName());

                    if (mapModel.getDeckName().equalsIgnoreCase(mapDeckName)) {

                        InputStream htmlTemplateIS = null;

                        if (mapModel.getViewType().toLowerCase().
                                equalsIgnoreCase(EMapViewType.DECK.toString().toLowerCase())) {

                            try {
                                htmlTemplateIS = MainApplication.getAppContext().
                                        getAssets().open(DECK_TEMPLATE_FILE_DIRECTORY);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (mapModel.getViewType().toLowerCase().
                                equalsIgnoreCase(EMapViewType.SIDE.toString().toLowerCase())) {

                            try {
                                htmlTemplateIS = MainApplication.getAppContext().
                                        getAssets().open(SIDE_PROFILE_TEMPLATE_FILE_DIRECTORY);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (mapModel.getViewType().toLowerCase().
                                equalsIgnoreCase(EMapViewType.FRONT.toString().toLowerCase())) {

                            try {
                                htmlTemplateIS = MainApplication.getAppContext().
                                        getAssets().open(FRONT_PROFILE_TEMPLATE_FILE_DIRECTORY);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        if (htmlTemplateIS != null) {

                            isHtmlCreated = true;

                            // Create image to html files
                            File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
                            File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);

                            String htmlLeafletFileName = LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY.concat(StringUtil.HYPHEN).
                                    concat(mapImageFileNameWithoutExtension).concat(HTML_FILE_SUFFIX);

                            File htmlLeafletFile = new File(mapBlueprintHtmlDirectory, htmlLeafletFileName);
                            createHtmlStringFromTemplate(mapModel, htmlTemplateIS, mapImageFile, htmlLeafletFile);

                            break;

                        }

                    }
                }

            }
        }

        // If new html file is created, it means at least a blueprint image file is available
        // If so, transfer all required javascript files from assets folder to external 'js' folder
        if (isHtmlCreated) {

            FileUtil.transferAllFilesInAssetsSubFolderToExternalFolder(LEVEL_2_SUB_DIRECTORY_JS_TO_COPY,
                    LEVEL_2_SUB_DIRECTORY_JS_TO_PASTE);
            FileUtil.transferAllFilesInAssetsSubFolderToExternalFolder(LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY,
                    LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_PASTE);

        }

    }

    private static void createHtmlStringFromTemplate(MapModel mapModel, InputStream htmlTemplateIS,
                                                     File mapImageFile, File htmlLeafletFile) {

        try (OutputStream outputStream = new FileOutputStream(htmlLeafletFile, false)) {

            String htmlString = null;

            // Copy contents of html template to newly created deck html file
            IOUtils.copy(htmlTemplateIS, outputStream);

            htmlString = FileUtils.readFileToString(htmlLeafletFile);

            String gaScale = mapModel.getGaScale(); // 300


//            Bitmap bitmap = FileUtil.getBitmapFromFile(mapImageFile.getAbsolutePath());
            String pxWidth = mapModel.getPixelWidth(); // String.valueOf(bitmap.getWidth());
//            String pxCmWidth = "41.45";
            String pxCmWidth = String.valueOf(DimensionUtil.
                    convertPixelToCm(Float.valueOf(pxWidth)));


            //            String lowestHeight = mapModel.getFloorAltitudeInPixel(); // String.valueOf((-1.45 * 300 * 142 / 105) / 100)
//            String highestHeight = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInPixel()) +
//                    DimensionUtil.AVERAGE_HUMAN_HEIGHT_IN_CM);

//            String averageHeightInMetres = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInPixel()) / 100);
            String averageHeightInMetres = String.valueOf(BFTLocalPreferences.getMetresFromPixels(Double.valueOf(mapModel.getFloorAltitudeInPixel())));
//            String floorAltitudeInPixel = mapModel.getFloorAltitudeInPixel();
//            Double onePixelToMetres = (Double.valueOf(floorAltitudeInPixel) / Double.valueOf(pxWidth)) * Double.valueOf(gaScale) / 100;
//            String averageHeightInMetres = String.valueOf(Double.valueOf(floorAltitudeInPixel) * onePixelToMetres);


            String imageOverlayFilePath = StringUtil.SINGLE_QUOTATION.concat(StringUtil.BACK_ONE_LEVEL_DIRECTORY).
                    concat(StringUtil.TRAILING_SLASH).concat(LEVEL_2_SUB_DIRECTORY_MAP_IMAGES).
                    concat(StringUtil.TRAILING_SLASH).concat(mapImageFile.getName()).
                    concat(StringUtil.SINGLE_QUOTATION); // "'../Map Images/BW_Paris_A_Deck.png'"
            String lowerLeftXInCm = mapModel.getLowerLeftX(); // "-4"
            String lowerLeftYInCm = mapModel.getLowerLeftY(); // "-16.97"
            String upperRightXInCm = mapModel.getUpperRightX(); // "37.45"
            String upperRightYInCm = mapModel.getUpperRightY(); // "19.16"

//            htmlString = htmlString.replace("$lowestHeight", lowestHeight);
//            htmlString = htmlString.replace("$highestHeight", highestHeight);
            htmlString = htmlString.replace("$averageHeightInMetres", averageHeightInMetres);
            htmlString = htmlString.replace("$gaScale", gaScale);
            htmlString = htmlString.replace("$pxWidth", pxWidth);
            htmlString = htmlString.replace("$pxCmWidth", pxCmWidth);
            htmlString = htmlString.replace("$imageOverlayFilePath", imageOverlayFilePath);
            htmlString = htmlString.replace("$lowerLeftXInCm", lowerLeftXInCm);
            htmlString = htmlString.replace("$lowerLeftYInCm", lowerLeftYInCm);
            htmlString = htmlString.replace("$upperRightXInCm", upperRightXInCm);
            htmlString = htmlString.replace("$upperRightYInCm", upperRightYInCm);

            FileUtils.writeStringToFile(htmlLeafletFile, htmlString);

        } catch (FileNotFoundException e) {
            // handle exception here
        } catch (IOException e) {
            // handle exception here
        }
    }

    // ********************** FastMap ********************** //

    /**
     * Create relevant html files from each map blueprint images
     */
    public static void createHtmlFilesFromImagesUsingAssetsTemplate(String userId, List<MapModel> mapModelList) {
        if (mapImageFiles == null) {
            mapImageFiles = getAllMapImageFilesInFolder(userId);
        } else {
            System.out.println("mapImageFiles is NOT null");
        }

        createHtmlFilesFromFastMapImagesUsingAssetsTemplate(userId, mapModelList, mapImageFiles);
    }

    /**
     * Create relevant html files from each FastMap map blueprint images
     */
    public static void createHtmlFilesFromFastMapImagesUsingAssetsTemplate(String userId, List<MapModel> mapModelList, File[] mapImageFiles) {

//        File[] mapImageFiles = getAllFilesInMapBlueprintImagesFolder();
//        File[] mapImageFiles = getAllMapImageFilesInFolder();

        boolean isHtmlCreated = false;

        // Iterate through all map models in database to find a match in name and extract all relevant details
        for (MapModel mapModel : mapModelList) {

            // Iterate through all map image files in 'Map Images' folder
            for (File mapImageFile : mapImageFiles) {

                if (mapImageFile.isFile()) {

                    String mapImageFileNameWithoutExtension = FilenameUtils.removeExtension(mapImageFile.getName().toLowerCase());
                    String[] mapNameGroup = StringUtil.removeUnderscores(mapImageFileNameWithoutExtension);

                    if (mapNameGroup.length == 3) {
                        String mapShipNameWithTrooperId = mapNameGroup[0]; // 0 - Ship Name with trooper ID, 1 - Level, 2 - Map index
                        String mapLevel = mapNameGroup[1]; // 0 - Ship Name with trooper ID, 1 - Level, 2 - Map index
                        String mapIndex = mapNameGroup[2]; // 0 - Ship Name with trooper ID, 1 - Level, 2 - Map index
                        String mapShipNameWithTrooperIdAndLevel = mapShipNameWithTrooperId.concat(StringUtil.UNDERSCORE).concat(mapLevel);

                        Timber.i("mapIndex: %s", mapIndex);
                        Timber.i("mapModel.getDeckNameWithIndex(): %s", mapModel.getDeckNameWithIndex());
                        Timber.i("mapShipNameWithTrooperIdAndLevel: %s", mapShipNameWithTrooperIdAndLevel);

                        if (mapModel.getDeckNameWithIndex().equalsIgnoreCase(mapImageFileNameWithoutExtension)) {

                            InputStream htmlTemplateIS = null;

                            if (mapModel.getViewType().toLowerCase().
                                    equalsIgnoreCase(EMapViewType.DECK.toString().toLowerCase())) {

                                try {
//                                htmlTemplateIS = MainApplication.getAppContext().
//                                        getAssets().open(DECK_TEMPLATE_FILE_DIRECTORY);

                                    htmlTemplateIS = MainApplication.getAppContext().
                                            getAssets().open(FASTMAP_DECK_TEMPLATE_FILE_DIRECTORY);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else if (mapModel.getViewType().toLowerCase().
                                    equalsIgnoreCase(EMapViewType.SIDE.toString().toLowerCase())) {

                                try {
                                    htmlTemplateIS = MainApplication.getAppContext().
                                            getAssets().open(SIDE_PROFILE_TEMPLATE_FILE_DIRECTORY);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else if (mapModel.getViewType().toLowerCase().
                                    equalsIgnoreCase(EMapViewType.FRONT.toString().toLowerCase())) {

                                try {
                                    htmlTemplateIS = MainApplication.getAppContext().
                                            getAssets().open(FRONT_PROFILE_TEMPLATE_FILE_DIRECTORY);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            if (htmlTemplateIS != null) {

                                isHtmlCreated = true;

                                // Create image to html files
                                File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
                                File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);
                                File userIdDirectory = getFileDirectory(mapBlueprintHtmlDirectory, userId);
                                File userMapBlueprintHtmlDirectory = getFileDirectory(userIdDirectory, mapShipNameWithTrooperIdAndLevel);

                                deleteAllFilesInFolder(userMapBlueprintHtmlDirectory);

                                String htmlLeafletFileName = mapImageFileNameWithoutExtension.concat(HTML_FILE_SUFFIX);

//                            String htmlLeafletFileName = LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY.concat(StringUtil.HYPHEN).
//                                    concat(mapDeckName).concat(HTML_FILE_SUFFIX);

                                File htmlLeafletFile = new File(userMapBlueprintHtmlDirectory, htmlLeafletFileName);
                                createHtmlStringOfFastMapFromTemplate(userId, mapShipNameWithTrooperIdAndLevel, mapModel, htmlTemplateIS, mapImageFile, htmlLeafletFile);

                            }

                            break;

                        }
                    }
                }

            }
        }

        // If new html file is created, it means at least a blueprint image file is available
        // If so, transfer all required javascript files from assets folder to external 'js' folder
        if (isHtmlCreated) {

            FileUtil.transferAllFilesInAssetsSubFolderToExternalFolder(LEVEL_2_SUB_DIRECTORY_JS_TO_COPY,
                    LEVEL_2_SUB_DIRECTORY_JS_TO_PASTE);
            FileUtil.transferAllFilesInAssetsSubFolderToExternalFolder(LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY,
                    LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_PASTE);

        }

    }

    private static void createHtmlStringOfFastMapFromTemplate(String userId, String mapShipNameWithTrooperIdAndLevel,
                                                              MapModel mapModel, InputStream htmlTemplateIS, File mapImageFile, File htmlLeafletFile) {

        try (OutputStream outputStream = new FileOutputStream(htmlLeafletFile, false)) {

            String htmlString = null;

            // Copy contents of html template to newly created deck html file
            IOUtils.copy(htmlTemplateIS, outputStream);

            htmlString = FileUtils.readFileToString(htmlLeafletFile);

            String gaScale = mapModel.getGaScale(); // 300


//            Bitmap bitmap = FileUtil.getBitmapFromFile(mapImageFile.getAbsolutePath());
            String pxWidth = mapModel.getPixelWidth(); // String.valueOf(bitmap.getWidth());
            String pxHeight = mapModel.getPixelHeight();
//            String pxCmWidth = "41.45";
//            String pxCmWidth = String.valueOf(DimensionUtil.
//                    convertPixelToCm(Float.valueOf(pxWidth)));


            //            String lowestHeight = mapModel.getFloorAltitudeInPixel(); // String.valueOf((-1.45 * 300 * 142 / 105) / 100)
//            String highestHeight = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInPixel()) +
//                    DimensionUtil.AVERAGE_HUMAN_HEIGHT_IN_CM);

//            String averageHeightInMetres = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInPixel()) / 100);
            String averageHeightInMetres = String.valueOf(BFTLocalPreferences.getMetresFromPixels(Double.valueOf(mapModel.getFloorAltitudeInPixel())));
//            String floorAltitudeInPixel = mapModel.getFloorAltitudeInPixel();
//            Double onePixelToMetres = (Double.valueOf(floorAltitudeInPixel) / Double.valueOf(pxWidth)) * Double.valueOf(gaScale) / 100;
//            String averageHeightInMetres = String.valueOf(Double.valueOf(floorAltitudeInPixel) * onePixelToMetres);
            String level = mapModel.getLevel();

            String imageOverlayFilePath = StringUtil.SINGLE_QUOTATION.concat(StringUtil.BACK_ONE_LEVEL_DIRECTORY).
                    concat(StringUtil.TRAILING_SLASH).concat(StringUtil.BACK_ONE_LEVEL_DIRECTORY).
                    concat(StringUtil.TRAILING_SLASH).concat(StringUtil.BACK_ONE_LEVEL_DIRECTORY).
                    concat(StringUtil.TRAILING_SLASH).concat(LEVEL_2_SUB_DIRECTORY_MAP_IMAGES).
                    concat(StringUtil.TRAILING_SLASH).concat(userId).
                    concat(StringUtil.TRAILING_SLASH).concat(mapShipNameWithTrooperIdAndLevel).
                    concat(StringUtil.TRAILING_SLASH).concat(mapImageFile.getName()).
                    concat(StringUtil.SINGLE_QUOTATION); // "'../../Map Images/<userId>/BW_Paris_A_Deck.png'"
            String lowerLeftX = mapModel.getLowerLeftX(); // "-4"
            String lowerLeftY = mapModel.getLowerLeftY(); // "-16.97"
            String upperRightX = mapModel.getUpperRightX(); // "37.45"
            String upperRightY = mapModel.getUpperRightY(); // "19.16"

            String initialZoom;
            if (Double.valueOf(pxHeight) > MapLeafletUtil.PIXEL_PER_ZOOM_LEVEL) {
                initialZoom = String.valueOf(-1 * Math.sqrt(Double.valueOf(pxHeight) / MapLeafletUtil.PIXEL_PER_ZOOM_LEVEL) + 1);
            } else {
                initialZoom = String.valueOf(Math.sqrt(MapLeafletUtil.PIXEL_PER_ZOOM_LEVEL / (Double.valueOf(pxHeight))) - 1);
            }

            String initialX;
            if (Double.valueOf(pxWidth) > MapLeafletUtil.PIXEL_PER_ZOOM_LEVEL) {
                initialX = String.valueOf(0.01 * Double.valueOf(pxWidth));
            } else {
                initialX = String.valueOf(0.015 * Double.valueOf(pxWidth));
            }

            String initialY;
            if (Double.valueOf(pxHeight) > MapLeafletUtil.PIXEL_PER_ZOOM_LEVEL) {
                initialY = String.valueOf(0.01 * Double.valueOf(pxHeight));
            } else {
                initialY = String.valueOf(0.015 * Double.valueOf(pxHeight));
            }

            System.out.println("$averageHeightInMetres: " + averageHeightInMetres);
            System.out.println("$level: " + level);
            System.out.println("$gaScale: " + gaScale);
            System.out.println("$pxWidth: " + pxWidth);
            System.out.println("$pxHeight: " + pxHeight);
            System.out.println("$imageOverlayFilePath: " + imageOverlayFilePath);
            System.out.println("$lowerLeftX: " + lowerLeftX);
            System.out.println("$lowerLeftY: " + lowerLeftY);
            System.out.println("$upperRightX: " + upperRightX);
            System.out.println("$upperRightY: " + upperRightY);
            System.out.println("$initialZoom: " + initialZoom);
            System.out.println("$initialX: " + initialX);
            System.out.println("$initialY: " + initialY);

//            htmlString = htmlString.replace("$lowestHeight", lowestHeight);
//            htmlString = htmlString.replace("$highestHeight", highestHeight);
            htmlString = htmlString.replace("$averageHeightInMetres", averageHeightInMetres);
            htmlString = htmlString.replace("$level", level);
            htmlString = htmlString.replace("$gaScale", gaScale);
            htmlString = htmlString.replace("$pxWidth", pxWidth);
            htmlString = htmlString.replace("$pxHeight", pxHeight);
//            htmlString = htmlString.replace("$pxCmWidth", pxCmWidth);
            htmlString = htmlString.replace("$imageOverlayFilePath", imageOverlayFilePath);
            htmlString = htmlString.replace("$lowerLeftX", lowerLeftX);
            htmlString = htmlString.replace("$lowerLeftY", lowerLeftY);
            htmlString = htmlString.replace("$upperRightX", upperRightX);
            htmlString = htmlString.replace("$upperRightY", upperRightY);
            htmlString = htmlString.replace("$initialZoom", initialZoom);
            htmlString = htmlString.replace("$initialX", initialX);
            htmlString = htmlString.replace("$initialY", initialY);

            FileUtils.writeStringToFile(htmlLeafletFile, htmlString);

        } catch (FileNotFoundException e) {
            // handle exception here
        } catch (IOException e) {
            // handle exception here
        }
    }


    private static void deleteAllFilesInFolder(File folder) {
        if (folder.isDirectory())
        {
            File[] children = folder.listFiles();
            for (File child : children) {
                boolean isChildDeleted = child.delete();
            }
        }
    }

    public static double getWidthInCmOfImageFile(Bitmap bitmap) {
//        return bitmap.getScaledHeight(bitmap.getDensity());
        return DimensionUtil.convertPixelToCm(bitmap.getWidth());
    }
}
