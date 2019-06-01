package sg.gov.dsta.mobileC3.ventilo.database;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
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
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.repository.SitRepRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.TaskRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;
import sg.gov.dsta.mobileC3.ventilo.repository.VideoStreamRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.DataModelUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.task.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class ExcelSpreadsheetUtil {

    public static final String EXCEL_FILE_RELATIVE_PATH = "Ven/Database_Spreadsheet.xls";

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
        File databaseFile = null;

        // If no SD card is available
        if (Environment.getExternalStorageState() == null) {

            // Create new databaseFile object
            databaseFile = new File(Environment.getDataDirectory()
                    + "/" + relativeFilePath);

            // If a SD card is available
        } else {

            // Search for databaseFile on SD card
            databaseFile = new File(Environment.getExternalStorageDirectory()
                    + "/" + relativeFilePath);
        }

        if (!databaseFile.exists()) {
            Log.i(TAG, "Database file does not exist.");

            if (isForWritingData) {
                Log.i(TAG, "Creating new Database file...");
                try {
                    if (databaseFile.createNewFile()) {
                        Log.i(TAG, "New Database file created.");
                    }
                } catch (IOException e) {
                    Log.i(TAG, "Error creating new Database file - " + e);
                }

            } else {
                return null;
            }

        } else {
            Log.i(TAG, "Database file absolute file path: " + databaseFile.getAbsolutePath());
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
                Log.d(TAG, "Error locating xls file: " + e);
            }
        }

        return false;
    }

    public static boolean readXlsWorkBookDataAndStoreIntoDatabase(FileInputStream databaseFileInputStream) {
        try {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            HSSFWorkbook workbook = new HSSFWorkbook(databaseFileInputStream);
            Log.i(TAG, "workbook.getNumberOfSheets(): " + workbook.getNumberOfSheets());

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                HSSFSheet sheet = workbook.getSheetAt(i);
                storeDataModelsIntoDatabase(databaseOperation, sheet);
            }

        } catch (IOException e) {
            Log.d(TAG, "Error in xls file stream: " + e);
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
                Log.d(TAG, "Error locating xlsx file: " + e);
            }
        }

        return false;
    }

    public static boolean readXlsxWorkBookDataAndStoreIntoDatabase(
            FileInputStream databaseFileInputStream) {

        try {
            DatabaseOperation databaseOperation = new DatabaseOperation();
            XSSFWorkbook workbook = new XSSFWorkbook(databaseFileInputStream);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                storeDataModelsIntoDatabase(databaseOperation, sheet);
            }

        } catch (IOException e) {
            Log.d(TAG, "Error in xlsx file stream: " + e);
            return false;
        }

        return true;
    }

    /**
     * Extracts data from each sheet of the excel file. Each sheet is to represent a table.
     * Models will be created from these data which will then be stored into the database.
     *
     * @param databaseOperation
     * @param sheet
     */
    private static synchronized void storeDataModelsIntoDatabase(DatabaseOperation databaseOperation,
                                                                 Sheet sheet) {
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

                if (CELL_TYPE_STRING.equalsIgnoreCase(cell.getCellType().toString()) ||
                        CELL_TYPE_NUMBERIC.equalsIgnoreCase(cell.getCellType().toString())) {
                    totalNoOfHeaderDataColumn++;
                }
            }
        }

        // Checks for the name of each sheet, then creates
        // corresponding data model and stores into database.
        switch (sheet.getSheetName()) {
            case DatabaseTableConstants.TABLE_USER:

                while (rowIterator.hasNext()) {
                    contentRow = rowIterator.next();

                    UserModel userModel = createUserModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                    if (userModel != null) {
                        UserRepository userRepo = new
                                UserRepository((Application) MainApplication.getAppContext());
                        databaseOperation.insertUserIntoDatabase(userRepo, userModel);
                        Log.i(TAG, "storeDataModelsIntoDatabase new User inserted.");
                    }
                }
                break;

            case DatabaseTableConstants.TABLE_VIDEO_STREAM:

                while (rowIterator.hasNext()) {
                    contentRow = rowIterator.next();

                    VideoStreamModel videoStreamModel = createVideoStreamModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                    if (videoStreamModel != null) {
                        VideoStreamRepository videoStreamRepo = new
                                VideoStreamRepository((Application) MainApplication.getAppContext());
                        databaseOperation.insertVideoStreamIntoDatabase(videoStreamRepo, videoStreamModel);
                        Log.i(TAG, "storeDataModelsIntoDatabase new Video Stream inserted.");
                    }
                }
                break;

            case DatabaseTableConstants.TABLE_SIT_REP:

                while (rowIterator.hasNext()) {
                    contentRow = rowIterator.next();

                    SitRepModel sitRepModel = createSitRepModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                    if (sitRepModel != null) {
                        SitRepRepository sitRepRepo = new
                                SitRepRepository((Application) MainApplication.getAppContext());
                        databaseOperation.insertSitRepIntoDatabase(sitRepRepo, sitRepModel);
                        Log.i(TAG, "storeDataModelsIntoDatabase new Sit Rep inserted.");
                    }
                }
                break;

            case DatabaseTableConstants.TABLE_TASK:

                while (rowIterator.hasNext()) {
                    contentRow = rowIterator.next();

                    TaskModel taskModel = createTaskModelFromDataRow(headerRow, contentRow, totalNoOfHeaderDataColumn);

                    if (taskModel != null) {
                        TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
                        databaseOperation.insertTaskIntoDatabase(taskRepo, taskModel);
                        Log.i(TAG, "storeDataModelsIntoDatabase new Task inserted.");
                    }
                }
                break;

            default:
                Log.i(TAG, "Invalid sheet. Sheet name: " + sheet.getSheetName());
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
        if(row != null) {
            for(Cell cell: row) {
                if(dataFormatter.formatCellValue(cell).trim().length() > 0) {
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
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                }
            }
        }

        Log.i(TAG, "createUserModelFromDataRow, totalNoOfHeaderDataColumn: " + totalNoOfHeaderDataColumn);
        Log.i(TAG, "createUserModelFromDataRow, NO_OF_FIELDS_IN_USER_MODEL_FROM_EXCEL: " +
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
                Log.i(TAG, "User model, userId: " + dataFields.get(0).toString());
                Log.i(TAG, "User model, password: " + dataFields.get(1).toString());
                Log.i(TAG, "User model, team: " + dataFields.get(2).toString());
                Log.i(TAG, "User model, role: " + dataFields.get(3).toString());
                userModel = new UserModel(dataFields.get(0).toString());
                userModel.setPassword(dataFields.get(1).toString());
                userModel.setAccessToken(StringUtil.INVALID_STRING);
                userModel.setTeam(dataFields.get(2).toString());
                userModel.setRole(dataFields.get(3).toString());
                userModel.setRadioConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
                userModel.setLastKnownOnlineDateTime(StringUtil.INVALID_STRING);

            } else {
                Log.i(TAG, "Table data from Excel does not match User data model");
                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                    Log.i(TAG, "User data table, Excel header field [" + i + "]: " +
                            headerRow.getCell(i).getStringCellValue());
                }
            }

        } else {
            Log.i(TAG, "Number of table data fields from Excel does not " +
                    "match that of User data model");
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                Log.i(TAG, "User data table, Excel header field [" + i + "]: " +
                        headerRow.getCell(i).getStringCellValue());
            }
        }

        return userModel;
    }

    /**
     * Creates Video Stream model from each data row in sheet
     *
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static VideoStreamModel createVideoStreamModelFromDataRow(Row headerRow, Row contentRow,
                                                                      int totalNoOfHeaderDataColumn) {
        VideoStreamModel videoStreamModel = null;
        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return null;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    if (currentCell.getNumericCellValue() % 1 == 0) {
                        dataFields.put(i, (int) currentCell.getNumericCellValue());
                    } else {
                        dataFields.put(i, currentCell.getNumericCellValue());
                    }
                }
            }
        }

        Log.i(TAG, "createVideoStreamModelFromDataRow, totalNoOfHeaderDataColumn: " + totalNoOfHeaderDataColumn);
        Log.i(TAG, "createVideoStreamModelFromDataRow, NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL: " +
                DataModelUtil.NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL);

        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_VIDEO_STREAM_MODEL_FROM_EXCEL) {
            // Checks if all relevant Video Stream model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] videoStreamModelFields = DataModelUtil.getVideoStreamModelFieldsOfExcel();
            String[] videoStreamModelFieldNames = new String[videoStreamModelFields.length];

            for (int i = 0; i < videoStreamModelFields.length; i++) {
                videoStreamModelFieldNames[i] = videoStreamModelFields[i].getName();
            }

            // Compares to find any match in fields of header row with Video Stream model fields
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                isAllFieldsMatched = Arrays.stream(videoStreamModelFieldNames).anyMatch(
                        headerRow.getCell(i).getStringCellValue()::equalsIgnoreCase);
                Log.i(TAG, "createVideoStreamModelFromDataRow, " +
                        "isAllFieldsMatched[" + i + "]: " + isAllFieldsMatched);
            }

            for (int i = 0; i < videoStreamModelFieldNames.length; i++) {
                Log.i(TAG, "createVideoStreamModelFromDataRow, " +
                        "videoStreamModelFieldNames[" + i + "]: " + videoStreamModelFieldNames[i]);
            }

            if (isAllFieldsMatched) {
                Log.i(TAG, "Video Stream model, userId: " + dataFields.get(0).toString());
                Log.i(TAG, "Video Stream model, name: " + dataFields.get(1).toString());
                Log.i(TAG, "Video Stream model, url: " + dataFields.get(2).toString());

                videoStreamModel = new VideoStreamModel();
                videoStreamModel.setUserId(dataFields.get(0).toString());
                videoStreamModel.setName(dataFields.get(1).toString());
                videoStreamModel.setUrl(dataFields.get(2).toString());
                videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);

            } else {
                Log.i(TAG, "Table data from Excel does not match Video Stream data model");
                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                    Log.i(TAG, "Video Stream data table, Excel header field [" + i + "]: " +
                            headerRow.getCell(i).getStringCellValue());
                }
            }

        } else {
            Log.i(TAG, "Number of table data fields from Excel does not " +
                    "match that of Video Stream data model");
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                Log.i(TAG, "Video Stream data table, Excel header field [" + i + "]: " +
                        headerRow.getCell(i).getStringCellValue());
            }
        }

        return videoStreamModel;
    }

    /**
     * Creates Sit Rep model from each data row in sheet
     *
     * @param headerRow
     * @param contentRow
     * @param totalNoOfHeaderDataColumn
     * @return
     */
    private static SitRepModel createSitRepModelFromDataRow(Row headerRow, Row contentRow, int totalNoOfHeaderDataColumn) {
        SitRepModel sitRepModel = null;
        HashMap<Integer, Object> dataFields = new HashMap<>();

        // Checks if content row is actually empty
        if (isRowEmpty(contentRow)) {
            return null;
        }

        // Iterate through all columns based on header columns
        for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
            Cell currentCell = contentRow.getCell(i);

            if (currentCell != null) {
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellType().toString())) {
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

        Log.i(TAG, "createSitRepModelFromDataRow, totalNoOfHeaderDataColumn: " + totalNoOfHeaderDataColumn);
        Log.i(TAG, "createSitRepModelFromDataRow, NO_OF_FIELDS_IN_SIT_REP_MODEL_FROM_EXCEL: " +
                DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_FROM_EXCEL);

        if (dataFields.size() == DataModelUtil.NO_OF_FIELDS_IN_SIT_REP_MODEL_FROM_EXCEL) {
            // Checks if all relevant Sit Rep model fields match those of Excel header fields
            boolean isAllFieldsMatched = true;
            Field[] sitRepModelFields = DataModelUtil.getSitRepModelFieldsOfExcel();
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
                Log.i(TAG, "Sit Rep model, Reporter: " + dataFields.get(0).toString());
                Log.i(TAG, "Sit Rep model, Location: " + dataFields.get(2).toString());
                Log.i(TAG, "Sit Rep model, Activity: " + dataFields.get(3).toString());
                Log.i(TAG, "Sit Rep model, Personnel T: " + dataFields.get(4).toString());
                Log.i(TAG, "Sit Rep model, Personnel S: " + dataFields.get(5).toString());
                Log.i(TAG, "Sit Rep model, Personnel D: " + dataFields.get(6).toString());
                Log.i(TAG, "Sit Rep model, Next Course of Action: " + dataFields.get(7).toString());
                Log.i(TAG, "Sit Rep model, Request: " + dataFields.get(8).toString());

                sitRepModel = new SitRepModel();
                sitRepModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
                sitRepModel.setReporter(dataFields.get(0).toString());

                if (DrawableUtil.IsValidImage(dataFields.get(1).toString().getBytes())) {
                    sitRepModel.setSnappedPhoto(dataFields.get(1).toString().getBytes());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(2).toString())) {
                    sitRepModel.setLocation(StringUtil.EMPTY_STRING);
                } else {
                    sitRepModel.setLocation(dataFields.get(2).toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(3).toString())) {
                    sitRepModel.setActivity(StringUtil.EMPTY_STRING);
                } else {
                    sitRepModel.setActivity(dataFields.get(3).toString());
                }

                try {
                    sitRepModel.setPersonnelT(Integer.parseInt(dataFields.get(4).toString()));
                } catch (NumberFormatException e) {
                    Log.i(TAG, "Sit Rep model, Personnel T: \"" +
                            dataFields.get(4).toString() + "\" (" + e + ")");
                    return null;
                }

                try {
                    sitRepModel.setPersonnelS(Integer.parseInt(dataFields.get(5).toString()));
                } catch (NumberFormatException e) {
                    Log.i(TAG, "Sit Rep model, Personnel S: \"" +
                            dataFields.get(5).toString() + "\" (" + e + ")");
                    return null;
                }

                try {
                    sitRepModel.setPersonnelD(Integer.parseInt(dataFields.get(6).toString()));
                } catch (NumberFormatException e) {
                    Log.i(TAG, "Sit Rep model, Personnel D: \"" +
                            dataFields.get(6).toString() + "\" (" + e + ")");
                    return null;
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(7).toString())) {
                    sitRepModel.setNextCoa(StringUtil.EMPTY_STRING);
                } else {
                    sitRepModel.setNextCoa(dataFields.get(7).toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(8).toString())) {
                    sitRepModel.setRequest(StringUtil.EMPTY_STRING);
                } else {
                    sitRepModel.setRequest(dataFields.get(8).toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(9).toString())) {
                    sitRepModel.setOthers(StringUtil.EMPTY_STRING);
                } else {
                    sitRepModel.setOthers(dataFields.get(9).toString());
                }

                sitRepModel.setCreatedDateTime(DateTimeUtil.getCurrentTime());

            } else {
                Log.i(TAG, "Table data from Excel does not match Sit Rep data model");
                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                    Log.i(TAG, "Sit Rep data table, Excel header field [" + i + "]: " +
                            headerRow.getCell(i).getStringCellValue());
                }
            }

        } else {
            Log.i(TAG, "Number of table data fields from Excel does not " +
                    "match that of Sit Rep data model");
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                Log.i(TAG, "Sit Rep data table, Excel header field [" + i + "]: " +
                        headerRow.getCell(i).getStringCellValue());
            }
        }

        return sitRepModel;
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
                if (CELL_TYPE_STRING.equalsIgnoreCase(currentCell.getCellType().toString())) {
                    dataFields.put(i, currentCell.getStringCellValue());
                } else if (CELL_TYPE_NUMBERIC.equalsIgnoreCase(currentCell.getCellType().toString())) {
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

        Log.i(TAG, "createTaskModelFromDataRow, totalNoOfHeaderDataColumn: " + totalNoOfHeaderDataColumn);
        Log.i(TAG, "createTaskModelFromDataRow, NO_OF_FIELDS_IN_TASK_MODEL_FROM_EXCEL: " +
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
                Log.i(TAG, "Task model, phaseNo: " + dataFields.get(0).toString());
                Log.i(TAG, "Task model, adHocTaskPriority: " + dataFields.get(1).toString());
                Log.i(TAG, "Task model, assignedTo: " + dataFields.get(2).toString());
                Log.i(TAG, "Task model, assignedBy: " + dataFields.get(3).toString());
                Log.i(TAG, "Task model, title: " + dataFields.get(4).toString());
                Log.i(TAG, "Task model, description: " + dataFields.get(5).toString());

                taskModel = new TaskModel();
                taskModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
                taskModel.setPhaseNo(dataFields.get(0).toString());

                String adHocTaskPriority;
                if (dataFields.get(1).toString().equalsIgnoreCase(StringUtil.INVALID_STRING)) {
                    adHocTaskPriority = StringUtil.INVALID_STRING;
                } else if (dataFields.get(1).toString().equalsIgnoreCase(
                        EAdHocTaskPriority.HIGH.toString())) {
                    adHocTaskPriority = EAdHocTaskPriority.HIGH.toString();
                } else {
                    adHocTaskPriority = EAdHocTaskPriority.LOW.toString();
                }

                taskModel.setAdHocTaskPriority(adHocTaskPriority);

                String assignedTo = dataFields.get(2).toString();

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(2).toString())) {
                    taskModel.setAssignedTo(StringUtil.EMPTY_STRING);
                    taskModel.setStatus(StringUtil.EMPTY_STRING);
                } else {
                    String[] assignedToStrArray = StringUtil.removeCommasAndExtraSpaces(assignedTo);

                    StringBuilder status = new StringBuilder();
                    for (int i = 0; i < assignedToStrArray.length; i++) {
                        status.append(EStatus.NEW.toString());

                        if (i != assignedToStrArray.length - 1) {
                            status.append(StringUtil.COMMA);
                            status.append(StringUtil.SPACE);
                        }
                    }

                    taskModel.setAssignedTo(assignedTo);
                    taskModel.setStatus(status.toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(3).toString())) {
                    taskModel.setAssignedBy(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setAssignedBy(dataFields.get(3).toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(4).toString())) {
                    taskModel.setTitle(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setTitle(dataFields.get(4).toString());
                }

                if (StringUtil.INVALID_STRING.equalsIgnoreCase(dataFields.get(5).toString())) {
                    taskModel.setDescription(StringUtil.EMPTY_STRING);
                } else {
                    taskModel.setDescription(dataFields.get(5).toString());
                }

                taskModel.setCreatedDateTime(DateTimeUtil.getCurrentTime());
                taskModel.setCompletedDateTime(StringUtil.INVALID_STRING);

            } else {
                Log.i(TAG, "Table data from Excel does not match Task data model");
                for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                    Log.i(TAG, "Task data table, Excel header field [" + i + "]: " +
                            headerRow.getCell(i).getStringCellValue());
                }
            }

        } else {
            Log.i(TAG, "Number of table data fields from Excel does not " +
                    "match that of Task data model");
            for (int i = 0; i < totalNoOfHeaderDataColumn; i++) {
                Log.i(TAG, "Task data table, Excel header field [" + i + "]: " +
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
        DatabaseOperation databaseOperation = new DatabaseOperation();

        // Create corresponding sheets
        createUserSheet(wb, databaseOperation, excelFile);
        createVideoStreamSheet(wb, databaseOperation, excelFile);
        createSitRepSheet(wb, databaseOperation, excelFile);
        createTaskSheet(wb, databaseOperation, excelFile);

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

    public static void createUserSheet(HSSFWorkbook wb, DatabaseOperation databaseOperation,
                                       File excelFile) {

        UserRepository userRepo = new UserRepository((Application) MainApplication.getAppContext());

        // Get User data from database and populate Excel sheet
        SingleObserver<List<UserModel>> singleObserverGetAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                if (userModelList != null) {

                    // Cell style for header row
//                    CellStyle cs = wb.createCellStyle();
//                    cs.setAlignment(HorizontalAlignment.CENTER);
//                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
//
//        HSSFFont font = wb.createFont();
//        font.setBold(true);
//        font.setUnderline(HSSFFont.U_DOUBLE);
////        font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
//
//        cs.setFont(font);

                    // New Sheet
                    Sheet userSheet;
                    userSheet = wb.createSheet(DatabaseTableConstants.TABLE_USER);

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
//                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_PASSWORD);
//                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_TEAM);
//                    c.setCellStyle(cs);

                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.USER_HEADER_ROLE);
//                    c.setCellStyle(cs);

//        userSheet.setColumnWidth(0, (15 * 500));
//        userSheet.setColumnWidth(1, (15 * 500));
//        userSheet.setColumnWidth(2, (15 * 500));

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
                        Log.i(TAG, "Writing file into " + excelFile.getAbsolutePath());
//                            isSuccess = true;
                    } catch (IOException e) {
                        Log.w(TAG, "Error writing " + excelFile.getAbsolutePath(), e);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to save file", e);
                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {
                            Log.w(TAG, "Error closing databaseFileOutputStream - " + ex);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllUsers, createUserSheet. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.getAllUsersFromDatabase(userRepo, singleObserverGetAllUsers);
    }

    public static void createVideoStreamSheet(HSSFWorkbook wb, DatabaseOperation databaseOperation,
                                              File excelFile) {

        VideoStreamRepository videoStreamRepo = new VideoStreamRepository((Application) MainApplication.getAppContext());

        // Get Video Stream data from database and populate Excel sheet
        SingleObserver<List<VideoStreamModel>> singleObserverGetAllVideoStreams = new SingleObserver<List<VideoStreamModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<VideoStreamModel> videoStreamModelList) {
                if (videoStreamModelList != null) {

                    // Cell style for header row
//                    CellStyle cs = wb.createCellStyle();
//                    cs.setAlignment(HorizontalAlignment.CENTER);
//                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
//
//                    HSSFFont font = wb.createFont();
//                    font.setBold(true);
//                    font.setUnderline(HSSFFont.U_DOUBLE);
//                    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
//
//                    cs.setFont(font);

                    // New Sheet
                    Sheet videoStreamSheet;
                    videoStreamSheet = wb.createSheet(DatabaseTableConstants.TABLE_VIDEO_STREAM);

                    // Generate column headings
                    Row row = videoStreamSheet.createRow(0);
                    Cell c;

//                    Field[] videoStreamModelFields = DataModelUtil.getVideoStreamModelFieldsOfExcel();
//                    for (int i = 0; i < videoStreamModelFields.length; i++) {
//                        Field videoStreamModelField = videoStreamModelFields[i];
//                        c = row.createCell(i);
//                        c.setCellValue(videoStreamModelField.getName().trim());
////                        c.setCellStyle(cs);
//                    }
                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_USER_ID);
//                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_NAME);
//                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.VIDEO_STREAM_HEADER_URL);
//                    c.setCellStyle(cs);


//                    videoStreamSheet.setColumnWidth(0, (15 * 500));
//                    videoStreamSheet.setColumnWidth(1, (15 * 500));
//                    videoStreamSheet.setColumnWidth(2, (15 * 500));

                    for (int i = 0; i < videoStreamModelList.size(); i++) {
                        VideoStreamModel videoStreamModel = videoStreamModelList.get(i);
                        row = videoStreamSheet.createRow(i + 1);

                        c = row.createCell(0);
                        c.setCellValue(videoStreamModel.getUserId());
//            c.setCellStyle(cs);

                        c = row.createCell(1);
                        c.setCellValue(videoStreamModel.getName());
//            c.setCellStyle(cs);

                        c = row.createCell(2);
                        c.setCellValue(videoStreamModel.getUrl());
//            c.setCellStyle(cs);
                    }

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);
                        Log.i(TAG, "Writing file into " + excelFile.getAbsolutePath());
//                        isSuccess = true;
                    } catch (IOException e) {
                        Log.w(TAG, "Error writing " + excelFile.getAbsolutePath(), e);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to save file", e);
                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {
                            Log.w(TAG, "Error closing databaseFileOutputStream - " + ex);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllVideoStreams, createVideoStreamSheet. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.getAllVideoStreamsFromDatabase(videoStreamRepo, singleObserverGetAllVideoStreams);
    }

    public static void createSitRepSheet(HSSFWorkbook wb, DatabaseOperation databaseOperation,
                                         File excelFile) {

        SitRepRepository sitRepRepo = new SitRepRepository((Application) MainApplication.getAppContext());

        // Get Sit Rep data from database and populate Excel sheet
        SingleObserver<List<SitRepModel>> singleObserverGetAllSitReps = new SingleObserver<List<SitRepModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<SitRepModel> sitRepModelList) {
                if (sitRepModelList != null) {

                    // Cell style for header row
//                    CellStyle cs = wb.createCellStyle();
//                    cs.setAlignment(HorizontalAlignment.CENTER);
//                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
//
//                    HSSFFont font = wb.createFont();
//                    font.setBold(true);
//                    font.setUnderline(HSSFFont.U_DOUBLE);
//                    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
//
//                    cs.setFont(font);

                    // New Sheet
                    Sheet sitRepSheet;
                    sitRepSheet = wb.createSheet(DatabaseTableConstants.TABLE_SIT_REP);

                    // Generate column headings
                    Row row = sitRepSheet.createRow(0);
                    Cell c;

//                    Field[] sitRepModelFields = DataModelUtil.getSitRepModelFieldsOfExcel();
//                    for (int i = 0; i < sitRepModelFields.length; i++) {
//                        Field sitRepModelField = sitRepModelFields[i];
//                        c = row.createCell(i);
//                        c.setCellValue(sitRepModelField.getName().trim());
////                        c.setCellStyle(cs);
//                    }

                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_REPORTER);
//                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_SNAPPED_PHOTO);
//                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_LOCATION);
//                    c.setCellStyle(cs);

                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_ACTIVITY);
//                    c.setCellStyle(cs);

                    c = row.createCell(4);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_T);
//                    c.setCellStyle(cs);

                    c = row.createCell(5);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_S);
//                    c.setCellStyle(cs);

                    c = row.createCell(6);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_PERSONNEL_D);
//                    c.setCellStyle(cs);

                    c = row.createCell(7);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_NEXT_COA);
//                    c.setCellStyle(cs);

                    c = row.createCell(8);
                    c.setCellValue(DatabaseTableConstants.SITREP_HEADER_REQUEST);
//                    c.setCellStyle(cs);

//                    sitRepSheet.setColumnWidth(0, (15 * 500));
//                    sitRepSheet.setColumnWidth(1, (15 * 500));
//                    sitRepSheet.setColumnWidth(2, (15 * 500));

                    for (int i = 0; i < sitRepModelList.size(); i++) {
                        SitRepModel sitRepModel = sitRepModelList.get(i);
                        row = sitRepSheet.createRow(i + 1);

                        c = row.createCell(0);
                        c.setCellValue(sitRepModel.getReporter());
//                        c.setCellStyle(cs);

                        c = row.createCell(1);
                        if (sitRepModel.getSnappedPhoto() != null) {
                            c.setCellValue(sitRepModel.getSnappedPhoto().toString());
                        } else {
                            c.setCellValue(StringUtil.INVALID_STRING);
                        }
//                        c.setCellStyle(cs);

                        c = row.createCell(2);
                        c.setCellValue(sitRepModel.getLocation());
//                        c.setCellStyle(cs);

                        c = row.createCell(3);
                        c.setCellValue(sitRepModel.getActivity());
//                        c.setCellStyle(cs);

                        c = row.createCell(4);
                        c.setCellValue(sitRepModel.getPersonnelT());
//                        c.setCellStyle(cs);

                        c = row.createCell(5);
                        c.setCellValue(sitRepModel.getPersonnelS());
//                        c.setCellStyle(cs);

                        c = row.createCell(6);
                        c.setCellValue(sitRepModel.getPersonnelD());
//                        c.setCellStyle(cs);

                        c = row.createCell(7);
                        c.setCellValue(sitRepModel.getNextCoa());
//                        c.setCellStyle(cs);

                        c = row.createCell(8);
                        c.setCellValue(sitRepModel.getRequest());
//                        c.setCellStyle(cs);
                    }

                    FileOutputStream databaseFileOutputStream = null;
                    try {
                        databaseFileOutputStream = new FileOutputStream(excelFile);
                        wb.write(databaseFileOutputStream);
                        Log.i(TAG, "Writing file into " + excelFile.getAbsolutePath());
//                        isSuccess = true;
                    } catch (IOException e) {
                        Log.w(TAG, "Error writing " + excelFile.getAbsolutePath(), e);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to save file", e);
                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {
                            Log.w(TAG, "Error closing databaseFileOutputStream - " + ex);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllSitReps, createSitRepSheet. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.getAllSitRepsFromDatabase(sitRepRepo, singleObserverGetAllSitReps);
    }

    public static void createTaskSheet(HSSFWorkbook wb, DatabaseOperation databaseOperation,
                                       File excelFile) {

        TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());

        // Get Task data from database and populate Excel sheet
        SingleObserver<List<TaskModel>> singleObserverGetAllTasks = new SingleObserver<List<TaskModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(List<TaskModel> taskModelList) {
                if (taskModelList != null) {

                    // Cell style for header row
//                    CellStyle cs = wb.createCellStyle();
//                    cs.setAlignment(HorizontalAlignment.CENTER);
//                    cs.setVerticalAlignment(VerticalAlignment.CENTER);
//
//                    HSSFFont font = wb.createFont();
//                    font.setBold(true);
//                    font.setUnderline(HSSFFont.U_DOUBLE);
//                    font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
//
//                    cs.setFont(font);

                    // New Sheet
                    Sheet taskSheet;
                    taskSheet = wb.createSheet(DatabaseTableConstants.TABLE_TASK);

                    // Generate column headings
                    Row row = taskSheet.createRow(0 );
                    Cell c;

//                    Field[] taskModelFields = DataModelUtil.getTaskModelFieldsOfExcel();
//                    for (int i = 0; i < taskModelFields.length; i++) {
//                        Field taskModelField = taskModelFields[i];
//                        c = row.createCell(i);
//                        c.setCellValue(taskModelField.getName().trim());
////                        c.setCellStyle(cs);
//                    }

                    c = row.createCell(0);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_TO);
//                    c.setCellStyle(cs);

                    c = row.createCell(1);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_ASSIGNED_BY);
//                    c.setCellStyle(cs);

                    c = row.createCell(2);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_TITLE);
//                    c.setCellStyle(cs);

                    c = row.createCell(3);
                    c.setCellValue(DatabaseTableConstants.TASK_HEADER_DESCRIPTION);
//                    c.setCellStyle(cs);

//                    taskSheet.setColumnWidth(0, (15 * 500));
//                    taskSheet.setColumnWidth(1, (15 * 500));
//                    taskSheet.setColumnWidth(2, (15 * 500));

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
                        Log.i(TAG, "Writing file into " + excelFile.getAbsolutePath());
//                        isSuccess = true;
                    } catch (IOException e) {
                        Log.w(TAG, "Error writing " + excelFile.getAbsolutePath(), e);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to save file", e);
                    } finally {
                        try {
                            if (databaseFileOutputStream != null)
                                databaseFileOutputStream.close();
                        } catch (Exception ex) {
                            Log.w(TAG, "Error closing databaseFileOutputStream - " + ex);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverGetAllTasks, createTaskSheet. " +
                        "Error Msg: " + e.toString());
            }
        };

        databaseOperation.getAllTasksFromDatabase(taskRepo, singleObserverGetAllTasks);
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
