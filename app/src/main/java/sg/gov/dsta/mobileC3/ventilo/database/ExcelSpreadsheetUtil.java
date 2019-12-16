package sg.gov.dsta.mobileC3.ventilo.database;

import android.app.Application;
import android.os.Environment;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.repository.MapRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.FileUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SpinnerItemListDataBank;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.DataModelUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.map.EMapViewType;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class ExcelSpreadsheetUtil {

    public static final String EXCEL_FILE_RELATIVE_PATH = "Excel/Ventilo_Database.xlsx";
    public static final String EXCEL_FILE_SHIP_CONFIG_RELATIVE_PATH = "Excel/shipConfig.xlsx";

    private static final String TAG = ExcelSpreadsheetUtil.class.getSimpleName();

    private static final String CELL_TYPE_STRING = CellType.STRING.toString();
    private static final String CELL_TYPE_NUMBERIC = CellType.NUMERIC.toString();

    public ExcelSpreadsheetUtil() {
    }

    /* -------------------- Retrieve Excel Document -------------------- */

    /**
     * Gets Excel File by folder and file name
     *
     * @param folderName
     * @param fileName
     */
    public static File getExcelFile(String folderName, String fileName, boolean isForWritingData) {
        String relativeFilePath = "";
        if (folderName != null || !StringUtil.EMPTY_STRING.equalsIgnoreCase(folderName)) {
            relativeFilePath = fileName;
        } else {
            relativeFilePath = folderName.concat("/").concat(fileName);
        }

        return getExcelFile(relativeFilePath, isForWritingData);
    }

    /**
     * Gets Excel File by relative file path
     *
     * @param relativeFilePath
     * @return
     */
    public static File getExcelFile(String relativeFilePath, boolean isForWritingData) {
        File databaseFile;
        File directoryFileToCheck = null;

        File venDirectory = FileUtil.getFileDirectory(FileUtil.PUBLIC_DIRECTORY, FileUtil.SUB_DIRECTORY_VEN);
        String relativeFullPath = venDirectory.getName().concat(StringUtil.TRAILING_SLASH).concat(relativeFilePath);

        String[] directoriesAndFileName = StringUtil.removeTrailingSlashes(relativeFullPath);

//        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
//        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        // If SD card is NOT available
        if (Environment.getExternalStorageState() == null) {
//        if (isSDSupportedDevice && isSDPresent) {

            // Create new databaseFile object
//            databaseFile = new File(Environment.getDataDirectory()
//                    + "/" + relativeFilePath);

            directoryFileToCheck = Environment.getDataDirectory();

            for (int i = 0; i < directoriesAndFileName.length; i++) {

                if (i != directoriesAndFileName.length - 1) {

                    directoryFileToCheck = new File(directoryFileToCheck.getAbsolutePath().
                            concat(StringUtil.TRAILING_SLASH).concat(directoriesAndFileName[i]));

                    if (!directoryFileToCheck.exists()) {
                        directoryFileToCheck.mkdirs();
                    }

                }
            }

        } else { // If a SD card is available

            // Search for databaseFile on SD card
//            databaseFile = new File(Environment.getExternalStorageDirectory()
//                    + "/" + relativeFilePath);

            directoryFileToCheck = Environment.getExternalStorageDirectory();

            for (int i = 0; i < directoriesAndFileName.length; i++) {

                if (i != directoriesAndFileName.length - 1) {

                    directoryFileToCheck = new File(directoryFileToCheck.getAbsolutePath().
                            concat(StringUtil.TRAILING_SLASH).concat(directoriesAndFileName[i]));

                    if (!directoryFileToCheck.exists()) {
                        directoryFileToCheck.mkdirs();
                    }

                }
            }
        }

        databaseFile = new File(directoryFileToCheck, directoriesAndFileName[directoriesAndFileName.length - 1]);

        Timber.i("Checking if databaseFile: %s exists...", databaseFile.getAbsolutePath());

        if (!databaseFile.exists()) {

            Timber.i("Database file does not exist.");

            if (isForWritingData) {
                Timber.i("Creating new Database file...");

                try {
                    if (databaseFile.createNewFile()) {
                        Timber.i("New Database file created.");

                    }
                } catch (IOException e) {
                    Timber.e("Error creating new Database file - %s", e);

                }

            } else {
                return null;
            }

        } else {
            Timber.i("Database file absolute file path: %s", databaseFile.getAbsolutePath());

        }

        return databaseFile;
    }

    /* -------------------- Import/Read Data From Excel -------------------- */

    public static boolean readXlsWorkBookDataAndStoreIntoDatabase(String excelPath) {
        File excelFile = new File(excelPath);
        if (excelFile != null) {
            return readXlsWorkBookDataAndStoreIntoDatabase(excelFile);
        } else {
            return false;
        }
    }

    public static boolean readXlsWorkBookDataAndStoreIntoDatabase(File excelFile) {
        if (excelFile != null) {
            try {
                FileInputStream databaseFileInputStream = new FileInputStream(excelFile);
                return readXlsWorkBookDataAndStoreIntoDatabase(databaseFileInputStream);
            } catch (FileNotFoundException e) {

                Timber.i("Error locating xls file: %s", e);


            }
        }

        return false;
    }

    public static boolean readXlsWorkBookDataAndStoreIntoDatabase(FileInputStream databaseFileInputStream) {
        try {
            HSSFWorkbook workbook = new HSSFWorkbook(databaseFileInputStream);

            Timber.i("workbook.getNumberOfSheets(): %d", workbook.getNumberOfSheets());

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                HSSFSheet sheet = workbook.getSheetAt(i);
                storeDataModelsIntoDatabase(sheet);
            }

        } catch (IOException e) {
            Timber.e("Error in xls file stream: %s", e);

            return false;
        }

        return true;
    }

    public static boolean readXlsxWorkBookDataAndStoreIntoDatabase(String excelPath) {
        File excelFile = new File(excelPath);
        if (excelFile != null) {
            return readXlsxWorkBookDataAndStoreIntoDatabase(excelFile);
        } else {
            return false;
        }
    }

    public static boolean readXlsxWorkBookDataAndStoreIntoDatabase(File excelFile) {
        if (excelFile != null) {
            try {
                FileInputStream databaseFileInputStream = new FileInputStream(excelFile);
                return readXlsxWorkBookDataAndStoreIntoDatabase(databaseFileInputStream);
            } catch (FileNotFoundException e) {
                Timber.e("Error locating xlsx file: %s", e);

            }
        }

        return false;
    }

    public static boolean readXlsxWorkBookDataAndStoreIntoDatabase(
            FileInputStream databaseFileInputStream) {

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(databaseFileInputStream);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                storeDataModelsIntoDatabase(sheet);
            }

        } catch (IOException e) {
            Timber.e("Error in xlsx file stream: %s", e);

            return false;
        }

        return true;
    }

    /**
     * Extracts data from each sheet of the excel file. Each sheet is to represent a table.
     * Models will be created from these data which will then be stored into the database.
     *
     * @param sheet
     */
    private static synchronized void storeDataModelsIntoDatabase(Sheet sheet) {

        // Iterate through each row
        Iterator<Row> rowIterator = sheet.iterator();
        Row headerRow = null;
        Row contentRow;
        int totalNoOfHeaderDataColumn = 0;

        /**
         * Gets the total number of table data columns in sheet
         *
         * First header row containing column titles
         * E.g., id, password
         */
        if (rowIterator.hasNext()) {
            headerRow = rowIterator.next();

            // Iterate through all the columns
            Iterator<Cell> cellIterator = headerRow.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                if (CELL_TYPE_STRING.equalsIgnoreCase(cell.getCellTypeEnum().toString()) ||
                        CELL_TYPE_NUMBERIC.equalsIgnoreCase(cell.getCellTypeEnum().toString())) {
                    totalNoOfHeaderDataColumn++;
                }
            }
        }

        // Checks for the name of each sheet, then creates
        // corresponding data model and stores into database.
        switch (sheet.getSheetName()) {
            case DatabaseTableConstants.TABLE_USER:

                if (EAccessRight.CCT.toString().equalsIgnoreCase(
                        SharedPreferenceUtil.getCurrentUserAccessRight())) {

                    while (rowIterator.hasNext()) {
                        contentRow = rowIterator.next();

                        Timber.i("Checking User table...");
                        UserModel userModel = createUserModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                        if (userModel != null) {
                            UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());
                            DatabaseOperation.getInstance().insertUserIntoDatabase(userRepo, userModel);
                            JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(userModel);

                            Timber.i("storeDataModelsIntoDatabase - new User model inserted and broadcasted.");

                        }
                    }
                }

                break;

            case DatabaseTableConstants.TABLE_MAP_GA:

                MapRepository mapRepo = new MapRepository((Application) MainApplication.getAppContext());
                mapRepo.deleteAllMaps();

                while (rowIterator.hasNext()) {
                    contentRow = rowIterator.next();

                    MapModel mapModel = createMapModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                    if (mapModel != null) {
                        DatabaseOperation.getInstance().insertMapIntoDatabase(mapRepo, mapModel);
                        Timber.i("storeDataModelsIntoDatabase - new Map model inserted.");

                    }
                }

                break;

//            case DatabaseTableConstants.TABLE_VIDEO_STREAM:
//
//                while (rowIterator.hasNext()) {
//                    contentRow = rowIterator.next();
//
//                    VideoStreamModel videoStreamModel = createVideoStreamModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);
//
//                    if (videoStreamModel != null) {
//                        VideoStreamRepository videoStreamRepo = new
//                                VideoStreamRepository((Application) MainApplication.getAppContext());
//                        DatabaseOperation.getInstance().insertVideoStreamIntoDatabase(videoStreamRepo, videoStreamModel);
//                        JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(videoStreamModel);
//
//                        Timber.i("storeDataModelsIntoDatabase new Video Stream inserted and broadcasted.");
//
//                    }
//                }
//                break;

//            case DatabaseTableConstants.TABLE_SIT_REP:
//
//                while (rowIterator.hasNext()) {
//                    contentRow = rowIterator.next();
//
//                    SitRepModel sitRepModel = createSitRepModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);
//
//                    if (sitRepModel != null) {
//                        SitRepRepository sitRepRepo = new
//                                SitRepRepository((Application) MainApplication.getAppContext());
//                        DatabaseOperation.getInstance().queryAndInsertSitRepIntoDatabase(sitRepRepo, sitRepModel);
//                        JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(sitRepModel);
//                        Timber.i("storeDataModelsIntoDatabase new Sit Rep inserted and broadcasted.");
//
//                    }
//                }
//                break;

            case DatabaseTableConstants.TABLE_SIT_REP_DROPDOWN_LIST:

                if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                        SharedPreferenceUtil.getCurrentUserAccessRight())) {

                    SpinnerItemListDataBank.getInstance().clearSitRepDropdownListData();

                    while (rowIterator.hasNext()) {
                        contentRow = rowIterator.next();

                        setSitRepDropdownListFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                        Timber.i("storeDataModelsIntoDatabase - new Sit Rep model inserted and broadcasted.");
                    }
                }

                break;

            case DatabaseTableConstants.TABLE_TASK:

                if (EAccessRight.CCT.toString().equalsIgnoreCase(
                        SharedPreferenceUtil.getCurrentUserAccessRight())) {

                    while (rowIterator.hasNext()) {
                        contentRow = rowIterator.next();

                        TaskModel taskModel = createTaskModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                        if (taskModel != null) {
                            TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
                            DatabaseOperation.getInstance().insertTaskIntoDatabaseAndBroadcast(taskRepo, taskModel);

                            Timber.i("storeDataModelsIntoDatabase - new Task model inserted and broadcasted.");
                        }
                    }
                }

                break;

            default:

                Timber.i("Invalid sheet. Sheet name:%s ", sheet.getSheetName());

        }
    }

    /**
     * Checks if row is fully empty
     *
     * @param row
     * @return
     */
    private static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();
        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    /**
     * Creates User model from each data row in sheet
     *
     * @param headerRow
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static UserModel createUserModelFromDataRow(Row headerRow, Row contentRow,
                                                        int totalNoOfHeaderDataColumn) {
        UserModel userModel = null;
        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return null;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                }
            }
        }

        Timber.i("createUserModelFromDataRow, totalNoOfHeaderDataColumn: %d ", totalNoOfHeaderDataColumn);

        Timber.i("createUserModelFromDataRow, NO_OF_FIELDS_IN_USER_MODEL_FROM_EXCEL: %d",
                DataModelUtil.NO_OF_FIELDS_IN_USER_MODEL_FROM_EXCEL);

        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_USER_MODEL_FROM_EXCEL) {
            // Checks if all relevant User model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] userModelFields = DataModelUtil.getUserModelFieldsOfExcel();
            String[] userModelFieldNames = new String[userModelFields.length];

            for (int i = 0; i < userModelFields.length; i++) {
                userModelFieldNames[i] = userModelFields[i].getName();
            }

            // Compares to find any match in fields of header row with User model fields
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                isAllFieldsMatched = Arrays.stream(userModelFieldNames).anyMatch(
                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
            }

            if (isAllFieldsMatched) {

                String userId = dataFields.get(0).toString();
                String password = dataFields.get(1).toString();
                String team = dataFields.get(2).toString();
                String role = dataFields.get(3).toString();

                Timber.i("User model, userId: %s", userId);
                Timber.i("User model, password: %s", password);
                Timber.i("User model, team: %s", team);
                Timber.i("User model, role: %s", role);

                userModel = new UserModel(userId);
                userModel.setPassword(password);
                userModel.setAccessToken(StringUtil.EMPTY_STRING);
                userModel.setTeam(team);
                userModel.setRole(role);
                userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
                userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                userModel.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
                userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

            } else {
                Timber.w("Table data from Excel does not match User data model");

                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                    Timber.w("User data table, Excel header field [" + i + "]: %s",
                            headerRow.getCell(i).getStringCellValue());

                }
            }

        } else {
            Timber.w("Number of table data fields from Excel does not " +
                    "match that of User data model");

            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                Timber.w("User data table, Excel header field [" + i + "]: %s",
                        headerRow.getCell(i).getStringCellValue());

            }
        }

        return userModel;
    }

    /**
     * Creates Map model from each data row in sheet
     *
     * @param headerRow
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static MapModel createMapModelFromDataRow(Row headerRow, Row contentRow,
                                                        int totalNoOfHeaderDataColumn) {
        MapModel mapModel = null;
        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return null;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                }
            }
        }

        Timber.i("createMapModelFromDataRow, totalNoOfHeaderDataColumn: %d ", totalNoOfHeaderDataColumn);

        Timber.i("createMapModelFromDataRow, NO_OF_FIELDS_IN_MAP_MODEL_FROM_EXCEL: %d",
                DataModelUtil.NO_OF_FIELDS_IN_MAP_MODEL_FROM_EXCEL);

        if ((dataFields.size() - 4) == DataModelUtil.NO_OF_FIELDS_IN_MAP_MODEL_FROM_EXCEL) {
            // Checks if all relevant Map model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] mapModelFields = DataModelUtil.getMapModelFieldsOfExcel();
            String[] mapModelFieldNames = new String[mapModelFields.length];

            for (int i = 0; i < mapModelFields.length; i++) {
                mapModelFieldNames[i] = mapModelFields[i].getName();
            }

            // Compares to find any match in fields of header row with Map model fields
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                if (headerRow.getCell(i) != null) {
                    isAllFieldsMatched = Arrays.stream(mapModelFieldNames).anyMatch(
                            headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
                }

            }

            if (isAllFieldsMatched) {

                // dataFields.get(0) is an empty header
                String shipName = dataFields.get(1).toString();
                String gaScale = dataFields.get(2).toString();
                String deckName = dataFields.get(3).toString();
                String deckType = dataFields.get(4).toString();
                String pixelWidth = dataFields.get(5).toString();
                String pixelHeight = dataFields.get(6).toString();
                String floorAltitudeInPixel = dataFields.get(7).toString();

                String upperRightYInPixel = dataFields.get(8).toString();
                String lowerLeftYInPixel = dataFields.get(9).toString();
                String lowerLeftXInPixel = dataFields.get(10).toString();
                String upperRightXInPixel = dataFields.get(11).toString();
//                String heightAboveFrameLineInCm = dataFields.get(8).toString();
//                String heightBelowFrameLineInCm = dataFields.get(9).toString();

                Timber.i("Map model, deckName: %s", deckName);
                Timber.i("Map model, deckType: %s", deckType);
                Timber.i("Map model, gaScale: %s", gaScale);
                Timber.i("Map model, floorAltitudeInPixel: %s", floorAltitudeInPixel);
                Timber.i("Map model, pixelWidth: %s", pixelWidth);
                Timber.i("Map model, lowerLeftXInPixel: %s", lowerLeftXInPixel);
                Timber.i("Map model, lowerLeftYInPixel: %s", lowerLeftYInPixel);
                Timber.i("Map model, upperRightXInPixel: %s", upperRightXInPixel);
                Timber.i("Map model, upperRightXInPixel: %s", upperRightYInPixel);
//                Timber.i("Map model, heightAboveFrameLineInCm: %s", heightAboveFrameLineInCm);
//                Timber.i("Map model, heightBelowFrameLineInCm: %s", heightBelowFrameLineInCm);

                mapModel = new MapModel(deckName);
                mapModel.setGaScale(gaScale);

                if (deckType.toLowerCase().equalsIgnoreCase(EMapViewType.DECK.toString().toLowerCase())) {
                    mapModel.setViewType(EMapViewType.DECK.toString());

                } else if (deckType.toLowerCase().equalsIgnoreCase(EMapViewType.SIDE.toString().toLowerCase())) {
                    mapModel.setViewType(EMapViewType.SIDE.toString());

                } else if (deckType.toLowerCase().equalsIgnoreCase(EMapViewType.FRONT.toString().toLowerCase())) {
                    mapModel.setViewType(EMapViewType.FRONT.toString());

                } else {
                    mapModel.setViewType(StringUtil.INVALID_STRING);
                }

//                String floorAltitudeInPixel = String.valueOf(DimensionUtil.
//                        convertPixelToCm(Float.valueOf(floorAltitudeInPixel)));
//                Double floorAltitudeInPixel = DimensionUtil.
//                        convertPixelToCm(Float.valueOf(floorAltitudeInPixel));
//                Double onePixelToMetres = (floorAltitudeInPixel / Double.valueOf(floorAltitudeInPixel)) * Double.valueOf(gaScale) / 100;
//                String floorAltitudeInMetres = String.valueOf(floorAltitudeInPixel * onePixelToMetres);

                String lowerLeftXInCm = String.valueOf(DimensionUtil.
                        convertPixelToCm(Float.valueOf(lowerLeftXInPixel)));

                String lowerLeftYInCm = String.valueOf(DimensionUtil.
                        convertPixelToCm(Float.valueOf(lowerLeftYInPixel)));

                String upperRightXInCm = String.valueOf(DimensionUtil.
                        convertPixelToCm(Float.valueOf(upperRightXInPixel)));

                String upperRightYInCm = String.valueOf(DimensionUtil.
                        convertPixelToCm(Float.valueOf(upperRightYInPixel)));

//                String lowerLeftXInMetres = String.valueOf(Float.valueOf(lowerLeftX) / 100);
//                String lowerLeftYInMetres = String.valueOf(Float.valueOf(lowerLeftY) / 100);
//                String upperRightXInMetres = String.valueOf(Float.valueOf(upperRightX) / 100);
//                String upperRightYInMetres = String.valueOf(Float.valueOf(upperRightY) / 100);

                mapModel.setFloorAltitudeInPixel(floorAltitudeInPixel);
                mapModel.setPixelWidth(pixelWidth);
                mapModel.setLowerLeftX(lowerLeftXInCm);
                mapModel.setLowerLeftY(lowerLeftYInCm);
                mapModel.setUpperRightX(upperRightXInCm);
                mapModel.setUpperRightY(upperRightYInCm);
//                mapModel.setHeightAboveFrameLineInCm(heightAboveFrameLineInCm);
//                mapModel.setHeightBelowFrameLineInCm(heightBelowFrameLineInCm);

            } else {
                Timber.w("Table data from Excel does not match Map data model");

                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                    if (headerRow.getCell(i) != null) {
                        Timber.w("Map data table, Excel header field [" + i + "]: %s",
                                headerRow.getCell(i).getStringCellValue());
                    }
                }
            }

        } else {
            Timber.w("Number of table data fields from Excel does not " +
                    "match that of Map data model");

            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                if (headerRow.getCell(i) != null) {
                    Timber.w("Map data table, Excel header field [" + i + "]: %s",
                            headerRow.getCell(i).getStringCellValue());
                }

            }
        }

        return mapModel;
    }

//    /**
//     * Creates Video Stream model from each data row in sheet
//     *
//     * @param contentRow
//     * @param totalNoOfHeaderDataColumn
//     * @return
//     */
//    private static VideoStreamModel createVideoStreamModelFromDataRow(Row headerRow, Row contentRow,
//                                                                      int totalNoOfHeaderDataColumn) {
//        VideoStreamModel videoStreamModel = null;
//        HashMap<Integer, Object> dataFields = new HashMap<>();
//
//        // Checks if content row is actually empty
//        if (isRowEmpty(contentRow)) {
//            return null;
//        }
//
//        // Iterate through all columns based on header columns
//        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//            Cell currentCell = contentRow.getCell(i);
//
//            if (currentCell != null) {
//                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellType().toString())) {
//                    dataFields.put(i, currentCell.getStringCellValue());
//                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellType().toString())) {
//                    if (currentCell.getNumericCellValue() % 1 == 0) {
//                        dataFields.put(i, (int) currentCell.getNumericCellValue());
//                    } else {
//                        dataFields.put(i, currentCell.getNumericCellValue());
//                    }
//                }
//            }
//        }
//
//        Timber.i("createVideoStreamModelFromDataRow, totalNoOfHeaderDataColumn: %d" , totalNoOfHeaderDataColumn);
//
//        Timber.i("createVideoStreamModelFromDataRow, NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL: %d" ,
//                DataModelUtil.NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL);
//
//
//
//        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL) {
//            // Checks if all relevant Video Stream model fields match those of Excel header fields
//            boolean isAllFieldsMatched = true;
//            Field[] videoStreamModelFields = DataModelUtil.getVideoStreamModelFieldsOfExcel();
//            String[] videoStreamModelFieldNames = new String[videoStreamModelFields.length];
//
//            for (int i = 0; i < videoStreamModelFields.length; i++) {
//                videoStreamModelFieldNames[i] = videoStreamModelFields[i].getName();
//            }
//
//            // Compares to find any match in fields of header row with Video Stream model fields
//            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//                isAllFieldsMatched = Arrays.stream(videoStreamModelFieldNames).anyMatch(
//                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
//
//                Timber.i("createVideoStreamModelFromDataRow, " +
//                        "isAllFieldsMatched[" + i + "]: %b" , isAllFieldsMatched);
//
//            }
//
//            for (int i = 0; i < videoStreamModelFieldNames.length; i++) {
//
//                Timber.i("createVideoStreamModelFromDataRow, " +
//                        "videoStreamModelFieldNames[" + i + "]: %s" , videoStreamModelFieldNames[i]);
//
//            }
//
//            if (isAllFieldsMatched) {
//                Timber.i("Video Stream model, userId: %s" , dataFields.get(0).toString());
//
//                Timber.i("Video Stream model, name: %s" , dataFields.get(1).toString());
//
//                Timber.i("Video Stream model, url: %s" , dataFields.get(2).toString());
//
//
//
//                videoStreamModel = new VideoStreamModel();
//                videoStreamModel.setUserId(dataFields.get(0).toString());
//                videoStreamModel.setName(dataFields.get(1).toString());
//                videoStreamModel.setUrl(dataFields.get(2).toString());
//                videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);
//
//            } else {
//                Timber.i("Table data from Excel does not match Video Stream data model");
//
//                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//
//                    Timber.i("Video Stream data table, Excel header field [" + i + "]: %s" ,
//                            headerRow.getCell(i).getStringCellValue());
//
//                }
//            }
//
//        } else {
//
//            Timber.i("Number of table data fields from Excel does not " +
//                    "match that of Video Stream data model");
//
//
//            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//
//                Timber.i("Video Stream data table, Excel header field [" + i + "]: %s" ,
//                        headerRow.getCell(i).getStringCellValue());
//
//
//            }
//        }
//
//        return videoStreamModel;
//    }

//    /**
//     * Creates Sit Rep model from each data row in sheet
//     *
//     * @param headerRow
//     * @param contentRow
//     * @param totalNoOfHeaderDataColumn
//     * @return
//     */
//    private static SitRepModel createSitRepModelFromDataRow(Row headerRow, Row contentRow, int totalNoOfHeaderDataColumn) {
//        SitRepModel sitRepModel = null;
//        HashMap<Integer, Object> dataFields = new HashMap<>();
//
//        // Checks if content row is actually empty
//        if (isRowEmpty(contentRow)) {
//            return null;
//        }
//
//        // Iterate through all columns based on header columns
//        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//            Cell currentCell = contentRow.getCell(i);
//
//            if (currentCell != null) {
//                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
//                    dataFields.put(i, currentCell.getStringCellValue());
//                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
//                    if (currentCell.getNumericCellValue() % 1 == 0) {
//                        dataFields.put(i, (int) currentCell.getNumericCellValue());
//                    } else {
//                        dataFields.put(i, currentCell.getNumericCellValue());
//                    }
//                } else {
//                    dataFields.put(i, StringUtil.INVALID_STRING);
//                }
//            } else {
//                dataFields.put(i, StringUtil.INVALID_STRING);
//            }
//        }
//
//        Timber.i("createSitRepModelFromDataRow, totalNoOfHeaderDataColumn: %d", totalNoOfHeaderDataColumn);
//
//        Timber.i("createSitRepModelFromDataRow, NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL: %d",
//                DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL);
//
//
//        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL) {
//            // Checks if all relevant Sit Rep model fields match those of Excel header fields
//            boolean isAllFieldsMatched = true;
//            Field[] sitRepModelFields = DataModelUtil.getSitRepModelDropdownListFieldsOfExcel();
//            String[] sitRepModelFieldNames = new String[sitRepModelFields.length];
//
//            for (int i = 0; i < sitRepModelFields.length; i++) {
//                sitRepModelFieldNames[i] = sitRepModelFields[i].getName();
//            }
//
//            // Compares to find any match in fields of header row with Sit Rep model fields
//            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//                isAllFieldsMatched = Arrays.stream(sitRepModelFieldNames).anyMatch(
//                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
//            }
//
//            if (isAllFieldsMatched) {
//
//                Timber.i("Sit Rep model, Reporter: %s", dataFields.get(0).toString());
//                Timber.i("Sit Rep model, Location: %s", dataFields.get(2).toString());
//                Timber.i("Sit Rep model, Activity: %s", dataFields.get(3).toString());
//                Timber.i("Sit Rep model, Personnel T: %s", dataFields.get(4).toString());
//                Timber.i("Sit Rep model, Personnel S: %s", dataFields.get(5).toString());
//                Timber.i("Sit Rep model, Personnel D: %s", dataFields.get(6).toString());
//                Timber.i("Sit Rep model, Next Course of Action: %s", dataFields.get(7).toString());
//                Timber.i("Sit Rep model, Request: %s", dataFields.get(8).toString());
//
//
//                sitRepModel = new SitRepModel();
//                sitRepModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
//                sitRepModel.setReporter(dataFields.get(0).toString());
//
//                if (DrawableUtil.IsValidImage(dataFields.get(1).toString().getBytes())) {
//                    sitRepModel.setSnappedPhoto(dataFields.get(1).toString().getBytes());
//                }
//
//                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(2).toString())) {
//                    sitRepModel.setLocation(StringUtil.EMPTY_STRING);
//                } else {
//                    sitRepModel.setLocation(dataFields.get(2).toString());
//                }
//
//                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(3).toString())) {
//                    sitRepModel.setActivity(StringUtil.EMPTY_STRING);
//                } else {
//                    sitRepModel.setActivity(dataFields.get(3).toString());
//                }
//
//                try {
//                    sitRepModel.setPersonnelT(Integer.parseInt(dataFields.get(4).toString()));
//                } catch (NumberFormatException e) {
//
//                    Timber.e("Sit Rep model, Personnel T: %s %s ", dataFields.get(4).toString(), e);
//
//
//                    return null;
//                }
//
//                try {
//                    sitRepModel.setPersonnelS(Integer.parseInt(dataFields.get(5).toString()));
//                } catch (NumberFormatException e) {
//
//                    Timber.e("Sit Rep model, Personnel S: %s %s ", dataFields.get(5).toString(), e);
//
//
//                    return null;
//                }
//
//                try {
//                    sitRepModel.setPersonnelD(Integer.parseInt(dataFields.get(6).toString()));
//                } catch (NumberFormatException e) {
//
//                    Timber.e("Sit Rep model, Personnel D: %s %s ", dataFields.get(6).toString(), e);
//
//                    /*
//                    Log.i(TAG, "Sit Rep model, Personnel D: \"" +
//                            dataFields.get(6).toString() + "\" (" + e + ")");*/
//                    return null;
//                }
//
//                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(7).toString())) {
//                    sitRepModel.setNextCoa(StringUtil.EMPTY_STRING);
//                } else {
//                    sitRepModel.setNextCoa(dataFields.get(7).toString());
//                }
//
//                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(8).toString())) {
//                    sitRepModel.setRequest(StringUtil.EMPTY_STRING);
//                } else {
//                    sitRepModel.setRequest(dataFields.get(8).toString());
//                }
//
//                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(9).toString())) {
//                    sitRepModel.setOthers(StringUtil.EMPTY_STRING);
//                } else {
//                    sitRepModel.setOthers(dataFields.get(9).toString());
//                }
//
//                sitRepModel.setCreatedDateTime(DateTimeUtil.getCurrentDateTime());
//
//            } else {
//                Timber.i("Table data from Excel does not match Sit Rep data model");
//
//                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//
//                    Timber.i("Sit Rep data table, Excel header field [" + i + "]: %s",
//                            headerRow.getCell(i).getStringCellValue());
//
//
//                }
//            }
//
//        } else {
//            Timber.i("Number of table data fields from Excel does not match that of Sit Rep data model");
//
//
//            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
//
//                Timber.i("Sit Rep data table, Excel header field [" + i + "]: %s",
//                        headerRow.getCell(i).getStringCellValue());
//
//            }
//        }
//
//        return sitRepModel;
//    }

    /**
     * Creates Sit Rep dropdown list data from each data row in sheet
     *
     * @param headerRow
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static void setSitRepDropdownListFromDataRow(Row headerRow, Row contentRow, int totalNoOfHeaderDataColumn) {

        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                } else {
                    dataFields.put(i, StringUtil.EMPTY_STRING);
                }
            } else {
                dataFields.put(i, StringUtil.EMPTY_STRING);
            }
        }

        Timber.d("setSitRepDropdownListFromDataRow, totalNoOfHeaderDataColumn: %d", totalNoOfHeaderDataColumn);

        Timber.d("setSitRepDropdownListFromDataRow, NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL: %d",
                DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL);


        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_DROPDOWN_LIST_FROM_EXCEL) {
            // Checks if all relevant Sit Rep model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] sitRepModelFields = DataModelUtil.getSitRepModelDropdownListFieldsOfExcel();
            String[] sitRepModelFieldNames = new String[sitRepModelFields.length];

            for (int i = 0; i < sitRepModelFields.length; i++) {
                sitRepModelFieldNames[i] = sitRepModelFields[i].getName();
            }

            // Compares to find any match in fields of header row with Sit Rep model fields
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                isAllFieldsMatched = Arrays.stream(sitRepModelFieldNames).anyMatch(
                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
            }

            if (isAllFieldsMatched) {

                String location = dataFields.get(0).toString();
                String activity = dataFields.get(1).toString();
                String nextCoa = dataFields.get(2).toString();
                String request = dataFields.get(3).toString();

                if (location != null && !StringUtil.EMPTY_STRING.equalsIgnoreCase(location)) {
                    SpinnerItemListDataBank.getInstance().addItemToLocationList(location);
                    Timber.i("Sit Rep model, Location: %s", location);
                }

                if (activity != null && !StringUtil.EMPTY_STRING.equalsIgnoreCase(activity)) {
                    SpinnerItemListDataBank.getInstance().addItemToActivityList(activity);
                    Timber.i("Sit Rep model, Activity: %s", activity);
                }

                if (nextCoa != null && !StringUtil.EMPTY_STRING.equalsIgnoreCase(nextCoa)) {
                    SpinnerItemListDataBank.getInstance().addItemToNextCoaList(nextCoa);
                    Timber.i("Sit Rep model, Next Coa: %s", nextCoa);
                }

                if (request != null && !StringUtil.EMPTY_STRING.equalsIgnoreCase(request)) {
                    SpinnerItemListDataBank.getInstance().addItemToRequestList(request);
                    Timber.i("Sit Rep model, Request: %s", request);
                }

//                Timber.i("Sit Rep model, Reporter: %s", dataFields.get(0).toString());
//                Timber.i("Sit Rep model, Location: %s", dataFields.get(2).toString());
//                Timber.i("Sit Rep model, Activity: %s", dataFields.get(3).toString());
//                Timber.i("Sit Rep model, Personnel T: %s", dataFields.get(4).toString());
//                Timber.i("Sit Rep model, Personnel S: %s", dataFields.get(5).toString());
//                Timber.i("Sit Rep model, Personnel D: %s", dataFields.get(6).toString());
//                Timber.i("Sit Rep model, Next Course of Action: %s", dataFields.get(7).toString());
//                Timber.i("Sit Rep model, Request: %s", dataFields.get(8).toString());

            } else {
                Timber.i("Table data from Excel does not match Sit Rep data model");

                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                    Timber.i("Sit Rep data table, Excel header field [" + i + "]: %s",
                            headerRow.getCell(i).getStringCellValue());

                }
            }

        } else {
            Timber.i("Number of table data fields from Excel does not match that of Sit Rep data model");

            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                Timber.i("Sit Rep data table, Excel header field [" + i + "]: %s",
                        headerRow.getCell(i).getStringCellValue());

            }
        }
    }

    /**
     * Creates Task model from each data row in sheet
     *
     * @param headerRow
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static TaskModel createTaskModelFromDataRow(Row headerRow, Row contentRow, int totalNoOfHeaderDataColumn) {
        TaskModel taskModel = null;
        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return null;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellTypeEnum().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                } else {
                    dataFields.put(i, StringUtil.INVALID_STRING);
                }
            } else {
                dataFields.put(i, StringUtil.INVALID_STRING);
            }
        }

        Timber.i("createTaskModelFromDataRow, totalNoOfHeaderDataColumn: %d", totalNoOfHeaderDataColumn);

        Timber.i("createTaskModelFromDataRow, NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL: %d",
                DataModelUtil.NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL);

        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL) {
            // Checks if all relevant Task model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] taskModelFields = DataModelUtil.getTaskModelFieldsOfExcel();
            String[] taskModelFieldNames = new String[taskModelFields.length];

            for (int i = 0; i < taskModelFields.length; i++) {
                taskModelFieldNames[i] = taskModelFields[i].getName();
            }

            // Compares to find any match in fields of header row with Task model fields
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                isAllFieldsMatched = Arrays.stream(taskModelFieldNames).anyMatch(
                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
            }

            if (isAllFieldsMatched) {

                String phaseNo = dataFields.get(0).toString();
                String adHocTaskPriority = dataFields.get(1).toString();
                String assignedTo = dataFields.get(2).toString();
                String assignedBy = dataFields.get(3).toString();
                String title = dataFields.get(4).toString();
                String description = dataFields.get(5).toString();

                Timber.i("Task model, phaseNo: %s", phaseNo);
                Timber.i("Task model, adHocTaskPriority: %s", adHocTaskPriority);
                Timber.i("Task model, assignedTo: %s", assignedTo);
                Timber.i("Task model, assignedBy: %s", assignedBy);
                Timber.i("Task model, title: %s", title);
                Timber.i("Task model, description: %s", description);

                taskModel = new TaskModel();
                taskModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
                taskModel.setPhaseNo(dataFields.get(0).toString());

                if (adHocTaskPriority.equalsIgnoreCase(StringUtil.INVALID_STRING)) {
                    adHocTaskPriority = StringUtil.INVALID_STRING;
                } else if (adHocTaskPriority.equalsIgnoreCase(
                        EAdHocTaskPriority.HIGH.toString())) {
                    adHocTaskPriority = EAdHocTaskPriority.HIGH.toString();
                } else {
                    adHocTaskPriority = EAdHocTaskPriority.LOW.toString();
                }

                taskModel.setAdHocTaskPriority(adHocTaskPriority);
                taskModel.setAssignedTo(assignedTo);

                String[] assignedToStrArray = StringUtil.removeCommasAndExtraSpaces(assignedTo);

                // Status and CompletedDateTime fields
                StringBuilder status = new StringBuilder();
                StringBuilder completedDateTime = new StringBuilder();
                StringBuilder lastUpdatedStatusDateTime = new StringBuilder();

                for (int i = 0; i < assignedToStrArray.length; i++) {

                    if (StringUtil.INVALID_STRING.equalsIgnoreCase(assignedToStrArray[i])) {
                        status.append(StringUtil.INVALID_STRING);
                        completedDateTime.append(StringUtil.INVALID_STRING);
                        lastUpdatedStatusDateTime.append(StringUtil.INVALID_STRING);

                    } else {

                        status.append(EStatus.NEW.toString());
                        completedDateTime.append(StringUtil.INVALID_STRING);
                        lastUpdatedStatusDateTime.append(StringUtil.INVALID_STRING);

                        if (i != assignedToStrArray.length - 1) {
                            status.append(StringUtil.COMMA);
                            status.append(StringUtil.SPACE);
                            completedDateTime.append(StringUtil.COMMA);
                            lastUpdatedStatusDateTime.append(StringUtil.SPACE);
                        }
                    }
                }

                taskModel.setStatus(status.toString());
                taskModel.setCompletedDateTime(completedDateTime.toString());
                taskModel.setLastUpdatedStatusDateTime(lastUpdatedStatusDateTime.toString());

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(assignedBy)) {
                    taskModel.setAssignedBy(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setAssignedBy(assignedBy);
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(title)) {
                    taskModel.setTitle(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setTitle(title);
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(description)) {
                    taskModel.setDescription(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setDescription(description);
                }

                taskModel.setCreatedDateTime(DateTimeUtil.getCurrentDateTime());
                taskModel.setLastUpdatedMainDateTime(StringUtil.INVALID_STRING);
                taskModel.setIsValid(EIsValid.YES.toString());

            } else {

                Timber.w("Table data from Excel does not match Task data model");

                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {

                    Timber.w("Task data table, Excel header field [" + i + "]: %s",
                            headerRow.getCell(i).getStringCellValue());

                }
            }

        } else {
            Timber.w("Number of table data fields from Excel does not " +
                    "match that of Task data model");

            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                Timber.w("Task data table, Excel header field [" + i + "]: %s",
                        headerRow.getCell(i).getStringCellValue());

            }
        }

        return taskModel;
    }

    /* -------------------- Export/Write Data To Excel -------------------- */

    public static boolean readDatabaseAndStoreIntoXlsWorkBookData(String excelPath) {
        File excelFile = new File(excelPath);
        return readDatabaseAndStoreIntoXlsWorkBookData(excelFile);
    }

//    public static boolean readDatabaseAndStoreIntoXlsWorkBookData(File excelFile) {
//        try {
//            return readDatabaseAndStoreIntoXlsWorkBookData(excelFile);
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "Error locating xls file: " + e);
//        }
//
//        return false;
//    }

    public static boolean readDatabaseAndStoreIntoXlsWorkBookData(File excelFile) {
        boolean isSuccess = false;

        //New Workbook
        HSSFWorkbook wb = new HSSFWorkbook();

        // Create corresponding sheets
        createUserSheetForXls(wb, excelFile);
//        createVideoStreamSheet(wb, excelFile);
//        createSitRepSheet(wb, excelFile);
        createTaskSheetForXls(wb, excelFile);

//        try {
//            wb.write(databaseFileOutputStream);
//            Log.i(TAG, "Writing file into " + excelFile.getAbsolutePath());
//            isSuccess = true;
//        } catch (IOException e) {
//            Log.w(TAG, "Error writing " + excelFile.getAbsolutePath(), e);
//        } catch (Exception e) {
//            Log.w(TAG, "Failed to save file", e);
//        } finally {
//            try {
//                if (null != databaseFileOutputStream)
//                    databaseFileOutputStream.close();
//            } catch (Exception ex) {
//                Log.w(TAG, "Error closing databaseFileOutputStream - " + ex);
//            }
//        }

        return isSuccess;
    }

    public static boolean readDatabaseAndStoreIntoXlsxWorkBookData(String excelPath) {
        File excelFile = new File(excelPath);
        return readDatabaseAndStoreIntoXlsxWorkBookData(excelFile);
    }

    public static boolean readDatabaseAndStoreIntoXlsxWorkBookData(File excelFile) {
        boolean isSuccess = false;

        //New Workbook
        XSSFWorkbook wb = new XSSFWorkbook();

        // Create corresponding sheets
        createUserSheetForXlsx(wb, excelFile);
//        createVideoStreamSheet(wb, excelFile);
//        createSitRepSheet(wb, excelFile);
        createTaskSheetForXlsx(wb, excelFile);

        return isSuccess;
    }

    public static void createUserSheetForXls(HSSFWorkbook wb, File excelFile) {

        UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());

        // Get User data from database and populate Excel sheet
        SingleObserver<List<UserModel>> singleObserverGetAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {

                if (userModelList != null) {

                    // New Sheet
                    Sheet userSheet;
                    userSheet = wb.createSheet(DatabaseTableConstants.TABLE_USER);

                    // Generate column headings
                    Row row = userSheet.createRow(0);
                    Cell c;

                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_USER_ID);
//                    c.setCellStyle(style);
//                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_PASSWORD);
//                    c.setCellStyle(style);
//                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_TEAM);
//                    c.setCellStyle(style);
//                    c.setCellStyle(cs);

                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_ROLE);
//                    c.setCellStyle(style);
//                    c.setCellStyle(cs);

                    for (int i = 0; i < userModelList.size(); i++) {
                        UserModel userModel = userModelList.get(i);
                        row = userSheet.createRow(i + 1);

                        c = row.createCell(0);
                        c.setCellValue(userModel.getUserId());
//            c.setCellStyle(cs);

                        c = row.createCell(1);
                        c.setCellValue(userModel.getPassword());
//            c.setCellStyle(cs);

                        c = row.createCell(2);
                        c.setCellValue(userModel.getTeam());
//            c.setCellStyle(cs);

                        c = row.createCell(3);
                        c.setCellValue(userModel.getRole());
//            c.setCellStyle(cs);
                    }

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);

                        Timber.i("Writing file into %s", excelFile.getAbsolutePath());

//                            isSuccess = true;
                    } catch (IOException e) {
                        Timber.e("Error writing %s %s", excelFile.getAbsolutePath(), e);


                    } catch (Exception e) {
                        Timber.e("Failed to save file %s ", e);


                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {

                            Timber.e("Error closing databaseFileOutputStream - %s", ex);

                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllUsers, createUserSheet. Error Msg: %s", e.toString());

            }
        };

        DatabaseOperation.getInstance().getAllUsersFromDatabase(userRepo, singleObserverGetAllUsers);
    }

    public static void createUserSheetForXlsx(XSSFWorkbook wb, File excelFile) {

        UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());

        // Get User data from database and populate Excel sheet
        SingleObserver<List<UserModel>> singleObserverGetAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {

                if (userModelList != null) {

                    // New Sheet
                    Sheet userSheet;
                    userSheet = wb.createSheet(DatabaseTableConstants.TABLE_USER);

                    // Column width is measured by 1/256th of a character
                    int userColumnWidthInUnits;
                    int passwordColumnWidthInUnits;
                    int teamColumnWidthInUnits;
                    int roleColumnWidthInUnits;

                    // Cell style for header row
                    XSSFCellStyle headerStyle = wb.createCellStyle();
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);
                    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    // Cell style for data row
                    XSSFCellStyle dataStyle = wb.createCellStyle();
                    dataStyle.setAlignment(HorizontalAlignment.CENTER);
                    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    XSSFFont font = wb.createFont();
                    font.setBold(true);
                    font.setUnderline(HSSFFont.U_DOUBLE);

                    headerStyle.setFont(font);

                    // Generate column headings
                    Row row = userSheet.createRow(0);
                    Cell c;

//                    Field[] userModelFields = DataModelUtil.getUserModelFieldsOfExcel();
//                    for (int i = 0; i < userModelFields.length; i++) {
//                        Field userModelField = userModelFields[i];
//                        c = row.createCell(i);
//                        c.setCellValue(userModelField.getName().trim());
////            c.setCellStyle(cs);
//                    }

                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_USER_ID);
                    c.setCellStyle(headerStyle);
                    userColumnWidthInUnits = DatabaseTableConstants.USER_HEADER_USER_ID.length();

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_PASSWORD);
                    c.setCellStyle(headerStyle);
                    passwordColumnWidthInUnits = DatabaseTableConstants.USER_HEADER_PASSWORD.length();

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_TEAM);
                    c.setCellStyle(headerStyle);
                    teamColumnWidthInUnits = DatabaseTableConstants.USER_HEADER_TEAM.length();

                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_ROLE);
                    c.setCellStyle(headerStyle);
                    roleColumnWidthInUnits = DatabaseTableConstants.USER_HEADER_ROLE.length();

                    for (int i = 0; i < userModelList.size(); i++) {
                        UserModel userModel = userModelList.get(i);
                        row = userSheet.createRow(i + 1);

                        String userId = userModel.getUserId();
                        String password = userModel.getPassword();
                        String team = userModel.getTeam();
                        String role = userModel.getRole();

                        c = row.createCell(0);
                        c.setCellValue(userId);
                        c.setCellStyle(dataStyle);
                        if (userId.length() > userColumnWidthInUnits) {
                            userColumnWidthInUnits = userId.length();
                        }

                        c = row.createCell(1);
                        c.setCellValue(password);
                        c.setCellStyle(dataStyle);
                        if (password.length() > passwordColumnWidthInUnits) {
                            passwordColumnWidthInUnits = password.length();
                        }

                        c = row.createCell(2);
                        c.setCellValue(team);
                        c.setCellStyle(dataStyle);
                        if (team.length() > teamColumnWidthInUnits) {
                            teamColumnWidthInUnits = team.length();
                        }

                        c = row.createCell(3);
                        c.setCellValue(role);
                        c.setCellStyle(dataStyle);
                        if (role.length() > roleColumnWidthInUnits) {
                            roleColumnWidthInUnits = role.length();
                        }
                    }

                    // Add 5 character spacing at the start and end of the data, so it does not look cluttered
                    userSheet.setColumnWidth(0, ((userColumnWidthInUnits + 10) * 256));
                    userSheet.setColumnWidth(1, ((passwordColumnWidthInUnits + 10) * 256));
                    userSheet.setColumnWidth(2, ((teamColumnWidthInUnits + 10) * 256));
                    userSheet.setColumnWidth(3, ((roleColumnWidthInUnits + 10) * 256));

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);

                        Timber.i("Writing file into %s", excelFile.getAbsolutePath());

                    } catch (IOException e) {
                        Timber.e("Error writing %s %s", excelFile.getAbsolutePath(), e);


                    } catch (Exception e) {
                        Timber.e("Failed to save file %s ", e);


                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {

                            Timber.e("Error closing databaseFileOutputStream - %s", ex);

                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverGetAllUsers, createUserSheet. Error Msg: %s", e.toString());

            }
        };

        DatabaseOperation.getInstance().getAllUsersFromDatabase(userRepo, singleObserverGetAllUsers);
    }

//    public static void createVideoStreamSheet(HSSFWorkbook wb, File excelFile) {
//
//        VideoStreamRepository videoStreamRepo = new VideoStreamRepository((Application) MainApplication.getAppContext());
//
//        // Get Video Stream data from database and populate Excel sheet
//        SingleObserver<List<VideoStreamModel>> singleObserverGetAllVideoStreams = new SingleObserver<List<VideoStreamModel>>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onSuccess(List<VideoStreamModel> videoStreamModelList) {
//                if (videoStreamModelList != null) {
//
//                    // Cell style for header row
////                    CellStyle cs = wb.createCellStyle();
////                    cs.setAlignment(HorizontalAlignment.CENTER);
////                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
////
////                    HSSFFont font = wb.createFont();
////                    font.setBold(true);
////                    font.setUnderline(HSSFFont.U_DOUBLE);
////                    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
////
////                    cs.setFont(font);
//
//                    // New Sheet
//                    Sheet videoStreamSheet;
//                    videoStreamSheet = wb.createSheet(DatabaseTableConstants.TABLE_VIDEO_STREAM);
//
//                    // Generate column headings
//                    Row row = videoStreamSheet.createRow(0);
//                    Cell c;
//
////                    Field[] videoStreamModelFields = DataModelUtil.getVideoStreamModelFieldsOfExcel();
////                    for (int i = 0; i < videoStreamModelFields.length; i++) {
////                        Field videoStreamModelField = videoStreamModelFields[i];
////                        c = row.createCell(i);
////                        c.setCellValue(videoStreamModelField.getName().trim());
//////                        c.setCellStyle(cs);
////                    }
//                    c = row.createCell(0);
//                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_USER_ID);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(1);
//                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_NAME);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(2);
//                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_URL);
////                    c.setCellStyle(cs);
//
//
////                    videoStreamSheet.setColumnWidth(0, (15 * 500));
////                    videoStreamSheet.setColumnWidth(1, (15 * 500));
////                    videoStreamSheet.setColumnWidth(2, (15 * 500));
//
//                    for (int i = 0; i < videoStreamModelList.size(); i++) {
//                        VideoStreamModel videoStreamModel = videoStreamModelList.get(i);
//                        row = videoStreamSheet.createRow(i + 1);
//
//                        c = row.createCell(0);
//                        c.setCellValue(videoStreamModel.getUserId());
////            c.setCellStyle(cs);
//
//                        c = row.createCell(1);
//                        c.setCellValue(videoStreamModel.getName());
////            c.setCellStyle(cs);
//
//                        c = row.createCell(2);
//                        c.setCellValue(videoStreamModel.getUrl());
////            c.setCellStyle(cs);
//                    }
//
//                    FileOutputStream databaseFileOutputStream = null;
//                    try {
//                        databaseFileOutputStream = new FileOutputStream(excelFile);
//                        wb.write(databaseFileOutputStream);
//
//                        Timber.i("Writing file into %s" , excelFile.getAbsolutePath());
//
//
////                        isSuccess = true;
//                    } catch (IOException e) {
//
//                        Timber.e("Error writing %s %s" , excelFile.getAbsolutePath(), e);
//
//
//                    } catch (Exception e) {
//
//                        Timber.e("Failed to save file %s", e);
//
//                    } finally {
//                        try {
//                            if (databaseFileOutputStream != null)
//                                databaseFileOutputStream.close();
//                        } catch (Exception ex) {
//
//                            Timber.e("Error closing databaseFileOutputStream - %s" , ex);
//
//
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//                Timber.e("onError singleObserverGetAllVideoStreams, createVideoStreamSheet. Error Msg: %s" , e.toString());
//
//
//            }
//        };
//
//        DatabaseOperation.getInstance().getAllVideoStreamsFromDatabase(videoStreamRepo, singleObserverGetAllVideoStreams);
//    }

//    public static void createSitRepSheet(HSSFWorkbook wb, File excelFile) {
//
//        SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());
//
//        // Get Sit Rep data from database and populate Excel sheet
//        SingleObserver<List<SitRepModel>> singleObserverGetAllSitReps = new SingleObserver<List<SitRepModel>>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onSuccess(List<SitRepModel> sitRepModelList) {
//                if (sitRepModelList != null) {
//
//                    // Cell style for header row
////                    CellStyle cs = wb.createCellStyle();
////                    cs.setAlignment(HorizontalAlignment.CENTER);
////                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
////
////                    HSSFFont font = wb.createFont();
////                    font.setBold(true);
////                    font.setUnderline(HSSFFont.U_DOUBLE);
////                    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
////
////                    cs.setFont(font);
//
//                    // New Sheet
//                    Sheet sitRepSheet;
//                    sitRepSheet = wb.createSheet(DatabaseTableConstants.TABLE_SIT_REP);
//
//                    // Generate column headings
//                    Row row = sitRepSheet.createRow(0);
//                    Cell c;
//
////                    Field[] sitRepModelFields = DataModelUtil.getSitRepModelDropdownListFieldsOfExcel();
////                    for (int i = 0; i < sitRepModelFields.length; i++) {
////                        Field sitRepModelField = sitRepModelFields[i];
////                        c = row.createCell(i);
////                        c.setCellValue(sitRepModelField.getName().trim());
//////                        c.setCellStyle(cs);
////                    }
//
//                    c = row.createCell(0);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_REPORTER);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(1);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_SNAPPED_PHOTO);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(2);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_LOCATION);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(3);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_ACTIVITY);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(4);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_T);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(5);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_S);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(6);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_D);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(7);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_NEXT_COA);
////                    c.setCellStyle(cs);
//
//                    c = row.createCell(8);
//                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_REQUEST);
////                    c.setCellStyle(cs);
//
////                    sitRepSheet.setColumnWidth(0, (15 * 500));
////                    sitRepSheet.setColumnWidth(1, (15 * 500));
////                    sitRepSheet.setColumnWidth(2, (15 * 500));
//
//                    for (int i = 0; i < sitRepModelList.size(); i++) {
//                        SitRepModel sitRepModel = sitRepModelList.get(i);
//                        row = sitRepSheet.createRow(i + 1);
//
//                        c = row.createCell(0);
//                        c.setCellValue(sitRepModel.getReporter());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(1);
//                        if (sitRepModel.getSnappedPhoto() != null) {
//                            c.setCellValue(sitRepModel.getSnappedPhoto().toString());
//                        } else {
//                            c.setCellValue(StringUtil.INVALID_STRING);
//                        }
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(2);
//                        c.setCellValue(sitRepModel.getLocation());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(3);
//                        c.setCellValue(sitRepModel.getActivity());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(4);
//                        c.setCellValue(sitRepModel.getPersonnelT());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(5);
//                        c.setCellValue(sitRepModel.getPersonnelS());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(6);
//                        c.setCellValue(sitRepModel.getPersonnelD());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(7);
//                        c.setCellValue(sitRepModel.getNextCoa());
////                        c.setCellStyle(cs);
//
//                        c = row.createCell(8);
//                        c.setCellValue(sitRepModel.getRequest());
////                        c.setCellStyle(cs);
//                    }
//
//                    FileOutputStream databaseFileOutputStream = null;
//                    try {
//                        databaseFileOutputStream = new FileOutputStream(excelFile);
//                        wb.write(databaseFileOutputStream);
//
//                        Timber.i("Writing file into %s", excelFile.getAbsolutePath());
//
//
////                        isSuccess = true;
//                    } catch (IOException e) {
//
//                        Timber.e("Error writing %s %s", excelFile.getAbsolutePath(), e);
//
//                    } catch (Exception e) {
//
//                        Timber.e("Failed to save file %s", e);
//
//                    } finally {
//                        try {
//                            if (databaseFileOutputStream != null)
//                                databaseFileOutputStream.close();
//                        } catch (Exception ex) {
//
//                            Timber.e("Error closing databaseFileOutputStream - %s", ex);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//                Timber.e("onError singleObserverGetAllSitReps, createSitRepSheet. Error Msg: %s", e.toString());
//
//            }
//        };
//
//        DatabaseOperation.getInstance().getAllSitRepsFromDatabase(sitRepRepo, singleObserverGetAllSitReps);
//    }

    public static void createTaskSheetForXls(HSSFWorkbook wb, File excelFile) {

        TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());

        // Get Task data from database and populate Excel sheet
        SingleObserver<List<TaskModel>> singleObserverGetAllTasks = new SingleObserver<List<TaskModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<TaskModel> taskModelList) {
                if (taskModelList != null) {

                    // New Sheet
                    Sheet taskSheet;
                    taskSheet = wb.createSheet(DatabaseTableConstants.TABLE_TASK);

                    // Generate column headings
                    Row row = taskSheet.createRow(0);
                    Cell c;

                    // phaseNo
                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_PHASE_NO);
//                    c.setCellStyle(cs);

                    // adHocTaskPriority
                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_AD_HOC_TASK_PRIORITY);
//                    c.setCellStyle(cs);

                    // assignedTo
                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_TO);
//                    c.setCellStyle(cs);

                    // assignedBy
                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_BY);
//                    c.setCellStyle(cs);

                    // title
                    c = row.createCell(4);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_TITLE);
//                    c.setCellStyle(cs);

                    // description
                    c = row.createCell(5);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_DESCRIPTION);
//                    c.setCellStyle(cs);

                    for (int i = 0; i < taskModelList.size(); i++) {
                        TaskModel taskModel = taskModelList.get(i);
                        row = taskSheet.createRow(i + 1);

                        c = row.createCell(0);
                        c.setCellValue(taskModel.getPhaseNo());

                        c = row.createCell(1);
                        c.setCellValue(taskModel.getAdHocTaskPriority());

                        c = row.createCell(2);
                        c.setCellValue(taskModel.getAssignedTo());
//                        c.setCellStyle(cs);

                        c = row.createCell(3);
                        c.setCellValue(taskModel.getAssignedBy());
//                        c.setCellStyle(cs);

                        c = row.createCell(4);
                        c.setCellValue(taskModel.getTitle());
//                        c.setCellStyle(cs);

                        c = row.createCell(5);
                        c.setCellValue(taskModel.getDescription());
//                        c.setCellStyle(cs);
                    }

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);

                        Timber.i("Writing file into %s", excelFile.getAbsolutePath());


//                        isSuccess = true;
                    } catch (IOException e) {
                        Timber.e("Error writing %s %s", excelFile.getAbsolutePath(), e);


                    } catch (Exception e) {
                        Timber.e("Failed to save file %s", e);

                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {

                            Timber.e("Error closing databaseFileOutputStream - %s", ex);

                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverGetAllTasks, createTaskSheet. Error Msg: %s", e.toString());

            }
        };

        DatabaseOperation.getInstance().getAllTasksFromDatabase(taskRepo, singleObserverGetAllTasks);
    }

    public static void createTaskSheetForXlsx(XSSFWorkbook wb, File excelFile) {

        TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());

        // Get Task data from database and populate Excel sheet
        SingleObserver<List<TaskModel>> singleObserverGetAllTasks = new SingleObserver<List<TaskModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<TaskModel> taskModelList) {
                if (taskModelList != null) {

                    // New Sheet
                    Sheet taskSheet;
                    taskSheet = wb.createSheet(DatabaseTableConstants.TABLE_TASK);

                    // Column width is measured by 1/256th of a character
                    int phaseNoColumnWidthInUnits;
                    int adHocTaskPriorityColumnWidthInUnits;
                    int assignedToColumnWidthInUnits;
                    int assignedByColumnWidthInUnits;
                    int titleColumnWidthInUnits;
                    int descriptionColumnWidthInUnits;

                    // Cell style for header row
                    XSSFCellStyle headerStyle = wb.createCellStyle();
                    headerStyle.setAlignment(HorizontalAlignment.CENTER);
                    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    // Cell style for data row
                    XSSFCellStyle dataStyle = wb.createCellStyle();
                    dataStyle.setAlignment(HorizontalAlignment.CENTER);
                    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                    XSSFFont font = wb.createFont();
                    font.setBold(true);
                    font.setUnderline(HSSFFont.U_DOUBLE);

                    headerStyle.setFont(font);

                    // Generate column headings
                    Row row = taskSheet.createRow(0);
                    Cell c;

                    // phaseNo
                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_PHASE_NO);
                    c.setCellStyle(headerStyle);
                    phaseNoColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_PHASE_NO.length();

                    // adHocTaskPriority
                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_AD_HOC_TASK_PRIORITY);
                    c.setCellStyle(headerStyle);
                    adHocTaskPriorityColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_AD_HOC_TASK_PRIORITY.length();

                    // assignedTo
                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_TO);
                    c.setCellStyle(headerStyle);
                    assignedToColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_ASSIGNED_TO.length();

                    // assignedBy
                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_BY);
                    c.setCellStyle(headerStyle);
                    assignedByColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_ASSIGNED_BY.length();

                    // title
                    c = row.createCell(4);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_TITLE);
                    c.setCellStyle(headerStyle);
                    titleColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_TITLE.length();

                    // description
                    c = row.createCell(5);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_DESCRIPTION);
                    c.setCellStyle(headerStyle);
                    descriptionColumnWidthInUnits = DatabaseTableConstants.TASK_HEADER_DESCRIPTION.length();

                    for (int i = 0; i < taskModelList.size(); i++) {
                        TaskModel taskModel = taskModelList.get(i);
                        row = taskSheet.createRow(i + 1);

                        String phaseNo = taskModel.getPhaseNo();
                        String adHocTaskPriority = taskModel.getAdHocTaskPriority();
                        String assignedTo = taskModel.getAssignedTo();
                        String assignedBy = taskModel.getAssignedBy();
                        String title = taskModel.getTitle();
                        String description = taskModel.getDescription();

                        c = row.createCell(0);
                        c.setCellValue(phaseNo);
                        c.setCellStyle(dataStyle);
                        if (phaseNo.length() > phaseNoColumnWidthInUnits) {
                            phaseNoColumnWidthInUnits = phaseNo.length();
                        }

                        c = row.createCell(1);
                        c.setCellValue(adHocTaskPriority);
                        c.setCellStyle(dataStyle);
                        if (adHocTaskPriority.length() > adHocTaskPriorityColumnWidthInUnits) {
                            adHocTaskPriorityColumnWidthInUnits = adHocTaskPriority.length();
                        }

                        c = row.createCell(2);
                        c.setCellValue(assignedTo);
                        c.setCellStyle(dataStyle);
                        if (assignedTo.length() > assignedToColumnWidthInUnits) {
                            assignedToColumnWidthInUnits = assignedTo.length();
                        }

                        c = row.createCell(3);
                        c.setCellValue(assignedBy);
                        c.setCellStyle(dataStyle);
                        if (assignedBy.length() > assignedByColumnWidthInUnits) {
                            assignedByColumnWidthInUnits = assignedBy.length();
                        }

                        c = row.createCell(4);
                        c.setCellValue(title);
                        c.setCellStyle(dataStyle);
                        if (title.length() > titleColumnWidthInUnits) {
                            titleColumnWidthInUnits = title.length();
                        }

                        c = row.createCell(5);
                        c.setCellValue(description);
                        c.setCellStyle(dataStyle);
                        if (description.length() > descriptionColumnWidthInUnits) {
                            descriptionColumnWidthInUnits = description.length();
                        }
                    }

                    // Add 5 character spacing at the start and end of the data, so it does not look cluttered
                    taskSheet.setColumnWidth(0, ((phaseNoColumnWidthInUnits + 10) * 256));
                    taskSheet.setColumnWidth(1, ((adHocTaskPriorityColumnWidthInUnits + 10) * 256));
                    taskSheet.setColumnWidth(2, ((assignedToColumnWidthInUnits + 10) * 256));
                    taskSheet.setColumnWidth(3, ((assignedByColumnWidthInUnits + 10) * 256));
                    taskSheet.setColumnWidth(4, ((titleColumnWidthInUnits + 10) * 256));
                    taskSheet.setColumnWidth(5, ((descriptionColumnWidthInUnits + 10) * 256));

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);

                        Timber.i("Writing file into %s", excelFile.getAbsolutePath());

                    } catch (IOException e) {
                        Timber.e("Error writing %s %s", excelFile.getAbsolutePath(), e);

                    } catch (Exception e) {
                        Timber.e("Failed to save file %s", e);

                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {
                            Timber.e("Error closing databaseFileOutputStream - %s", ex);

                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverGetAllTasks, createTaskSheet. Error Msg: %s", e.toString());

            }
        };

        DatabaseOperation.getInstance().getAllTasksFromDatabase(taskRepo, singleObserverGetAllTasks);
    }

//    public static void readDatabaseAndStoreIntoXlsxWorkBookData(String excelPath) {
//        File excelFile = new File(excelPath);
//        readDatabaseAndStoreIntoXlsxWorkBookData(excelFile);
//    }
//
//    public static void readDatabaseAndStoreIntoXlsxWorkBookData(File excelFile) {
//        try {
//            FileInputStream databaseFileInputStream = new FileInputStream(excelFile);
//            readDatabaseAndStoreIntoXlsxWorkBookData(databaseFileInputStream);
//        } catch (FileNotFoundException e) {
//            Log.d(TAG, "Error locating xlsx file: " + e);
//        }
//    }
//
//    public static void readDatabaseAndStoreIntoXlsxWorkBookData(FileInputStream databaseFileInputStream) {
//
//        try {
//            DatabaseOperation databaseOperation = new DatabaseOperation();
//            XSSFWorkbook workbook = new XSSFWorkbook(databaseFileInputStream);
//
//            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//                XSSFSheet sheet = workbook.getSheetAt(i);
//                storeDataModelsIntoDatabase(databaseOperation, sheet);
//            }
//
//        } catch (IOException e) {
//            Log.d(TAG, "Error in xlsx file stream: " + e);
//        }
//    }

//    public static boolean isExternalStorageReadOnly() {
//        String extStorageState = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
//            return true;
//        }
//        return false;
//    }
//
//    public static boolean isExternalStorageAvailable() {
//        String extStorageState = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
//            return true;
//        }
//        return false;
//    }
}
