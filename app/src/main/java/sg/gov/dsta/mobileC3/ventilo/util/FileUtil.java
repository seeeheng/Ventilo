package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.map.EMapViewType;
import sg.gov.dsta.mobileC3.ventilo.util.enums.sitRep.EReportType;
import timber.log.Timber;

public class FileUtil {

    public static final String FILE_SAVED_SUCCESSFULLY_INTENT_ACTION =
            "Sit Rep Text File Content Saved Successfully";
    public static final String DIRECTORY_PATH_KEY = "Directory Path";
    public static final File PUBLIC_DIRECTORY = Environment.getExternalStorageDirectory();
    public static final String SUB_DIRECTORY_VEN = "Ven";
//    private static final File PUBLIC_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

    private static final String LEVEL_2_SUB_DIRECTORY_MAP_IMAGES = "Map Images";
    private static final String LEVEL_2_SUB_DIRECTORY_MAP_HTML = "Map Html";
    private static final String LEVEL_2_SUB_DIRECTORY_JS_TO_COPY = "js";
    private static final String LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_COPY = "leaflet";
    private static final String LEVEL_2_SUB_DIRECTORY_JS_TO_PASTE = "js";
    private static final String LEVEL_2_SUB_DIRECTORY_LEAFLET_TO_PASTE = "Leaflet";
    private static final String TEXT_FILE_SUFFIX = ".txt";
    private static final String PNG_FILE_SUFFIX = ".png";
    private static final String HTML_FILE_SUFFIX = ".html";
    private static final String SIT_REP = "SitRep";

    private static final String DECK_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-deck-template.html";
    private static final String SIDE_PROFILE_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-side-profile-template.html";
    private static final String FRONT_PROFILE_TEMPLATE_FILE_DIRECTORY = "ship/leaflet-front-profile-template.html";
//    private static final String SIT_REP_IMAGE = "SitRepImage";

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

    // Extract file names without extension of Ven/Map Html folder in internal storage
    public static String[] getAllFileNamesWithoutExtensionInMapBlueprintHtmlFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);

        File[] listOfMapBlueprintHtmlFiles = getAllFilesInFolder(mapBlueprintHtmlDirectory);

        List<String> mapBlueprintHtmlFileNameList = new ArrayList<>();

        if (listOfMapBlueprintHtmlFiles != null) {
            for (File mapBlueprintHtmlFile : listOfMapBlueprintHtmlFiles) {
                mapBlueprintHtmlFileNameList.add(FilenameUtils.removeExtension(mapBlueprintHtmlFile.getName().toLowerCase()));
            }
        }

        return mapBlueprintHtmlFileNameList.toArray(new String[0]);
    }

    // Extract file names of Ven/Map Html folder in internal storage
    public static String[] getAllFileNamesInMapBlueprintHtmlFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintHtmlDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_HTML);

        return getAllFileNamesInFolder(mapBlueprintHtmlDirectory);
    }

    // Get files of map images of directory
    public static File[] getAllFilesInMapBlueprintImagesFolder() {
        File venDirectory = getFileDirectory(PUBLIC_DIRECTORY, SUB_DIRECTORY_VEN);
        File mapBlueprintImagesDirectory = getFileDirectory(venDirectory, LEVEL_2_SUB_DIRECTORY_MAP_IMAGES);

        return getAllFilesInFolder(mapBlueprintImagesDirectory);
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

    private static File[] getAllFilesInFolder(File fileDirectory) {
        return fileDirectory.listFiles();
    }

    private static String[] getAllFileNamesInFolder(File fileDirectory) {
        return fileDirectory.list();
    }

//    private void createHtmlFilesFromMapBlueprintImages(File[] imageFiles) {
//
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
     *
     */
    private static synchronized void notifySitRepTextFileSavedBroadcastIntent(String fileAbsolutePath) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.putExtra(DIRECTORY_PATH_KEY, fileAbsolutePath);

        broadcastIntent.setAction(FILE_SAVED_SUCCESSFULLY_INTENT_ACTION);
        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).sendBroadcast(broadcastIntent);
    }

//    public Bitmap getBitmapFromImageFile() {
//
//        getBitmapFromFile();
//    }

    /**
     * Get Bit map from file filePath
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
     *
     */
    public static void createHtmlFilesFromImagesUsingAssetsTemplate(List<MapModel> mapModelList) {

        File[] mapImageFiles = getAllFilesInMapBlueprintImagesFolder();
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
//            String lowestHeight = mapModel.getFloorAltitudeInCm(); // String.valueOf((-1.45 * 300 * 142 / 105) / 100)
//            String highestHeight = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInCm()) +
//                    DimensionUtil.AVERAGE_HUMAN_HEIGHT_IN_CM);

//            String averageHeightInMetres = String.valueOf(Float.valueOf(mapModel.getFloorAltitudeInCm()) / 100);
            String averageHeightInMetres = mapModel.getFloorAltitudeInCm();

//            Bitmap bitmap = FileUtil.getBitmapFromFile(mapImageFile.getAbsolutePath());
            String pxWidth = mapModel.getPixelWidth(); // String.valueOf(bitmap.getWidth());
//            String pxCmWidth = "41.45";
            String pxCmWidth = String.valueOf(DimensionUtil.
                    convertPixelToCm(Float.valueOf(pxWidth)));

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

    public static double getWidthInCmOfImageFile(Bitmap bitmap) {
//        return bitmap.getScaledHeight(bitmap.getDensity());
        return DimensionUtil.convertPixelToCm(bitmap.getWidth());
    }
}
