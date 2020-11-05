package sg.gov.dh.trackers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;
import com.navisens.motiondnaapi.MotionDna;
import com.navisens.motiondnaapi.MotionDnaSDK;
import com.navisens.motiondnaapi.MotionDnaSDKListener;

import java.util.HashMap;
import java.util.Map;

import sg.gov.dh.utils.Coords;

import static com.navisens.motiondnaapi.MotionDna.ErrorCode.ERROR_AUTHENTICATION_FAILED;
import static com.navisens.motiondnaapi.MotionDna.ErrorCode.ERROR_PERMISSIONS;
import static com.navisens.motiondnaapi.MotionDna.ErrorCode.ERROR_SDK_EXPIRED;
import static com.navisens.motiondnaapi.MotionDna.ErrorCode.ERROR_SENSOR_MISSING;
import static com.navisens.motiondnaapi.MotionDna.ErrorCode.ERROR_SENSOR_TIMING;

/**
 * The NAVISENS implementation of the Tracker Interface and NAVISENS's own API interface<br>
 * Note that this requires 'com.navisens:motiondnaapi:1.4.2' as dependancy in Gradle script.
 * Note: This only support local coordinate system
 * @see Tracker
 */

public class NavisensLocalTracker implements MotionDnaSDKListener, Tracker {
    //Somehow need to think of a way to make these 2 variables eternal
    double currentHeight=0.0;
    double mapHeight=0.0;
    double currentOffset=0.0;
    double currentX = 0.0;
    double currentY = 0.0;
    double currentZ = 0.0;
    double currentBearing = 0.0;

    /**
     * Do not change this.
     */
    private static final String DEVELOPER_KEY = "zcNcv8YYUYgvhgNrMZZzPNEQbcV9nAxlHhNPA9i9yNQyOmnBiveny6ZVJs6Hgsnr";

    /**
     * For logging purposes only
     */
    private static final String TAG = "NAVISENS_LOCAL_TRACKER";

    /**
     * Will not accept GPS updates if the radial distance error of the GPS update is more than this value in metres
     */
    private static float ACCEPTABLE_ERROR_RADIAL_DISTANCE_METRES=(float)1.0;

    /**
     * Update rate of the NAVISENS tracker in ms.
     */
    private static final double UPDATERATE_MS= 500.00;

    /**
     * Activity passed by the parent class (E.g. Map Provider's LocationDataSource)
     */
    private Activity context=null;

    public void setActive(boolean active) {
        isActive = active;
    }
    private boolean isActive = false;

    /**
     * Navisens core API that the Navisens Tracker depends on
     */
    MotionDnaSDK motionDnaApp;

    /**
     * The listener which would be assigned when setTrackerListener(TrackerListener listener) is called by parent class(E.g. Map Provider's LocationDataSource)
     */
    private TrackerListener listener;

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * This listener is used to trigger parent class<br>
     * {@inheritDoc}
     * @param listener
     */
    public void setTrackerListener(TrackerListener listener) {
        Log.d(TAG,"Setting up listener");
        this.listener = listener;
    }

    /**
     * This will release the Navisens API<br>
     * {@inheritDoc}
     */
    @Override
    public void deactivate() {
        try {
            motionDnaApp.stop();
        } catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }
        setActive(false);
    }

    /**
     * Disable regular GPS updates, and manually set a set of given coordinates to the NAVISENS API.<br>
     * {@inheritDoc}
     * @param coords
     */
    @Override
    public void setManualLocation(Coords coords) {

        double x = convertLeftToRightX(coords.getX(),coords.getY());
        double y = convertLeftToRightY(coords.getX(),coords.getY());
        double z = coords.getAltitude();
        this.mapHeight=z;
        this.currentOffset=0;
        
        double heading = convertLeftToRightHeading(coords.getBearing());

        motionDnaApp.setCartesianPosition(x,y);
        motionDnaApp.setCartesianHeading(heading);
    }

    @Override
    public void setGPSLocation() {}

    /**
     * This feature is unnecessary in Navisens<br>
     * {@inheritDoc}
     */
    @Override
    public void setup() {
        Log.i(TAG, "This feature is unnecessary in Navisens");
    }

    @Override
    public void stopGPSUpdate() {}

    @Override
    public void startGPSUpdate() {}

    // Conversion functions tidied up.
    private double convertLeftToRightX(double x, double y) { return y; }
    private double convertLeftToRightY(double x, double y) { return -1*x; }
    private double convertLeftToRightHeading(double bearing) { return (bearing*-1); }
    private double convertRightToLeftY(double righthandx, double righthandy) { return righthandx; }
    private double convertRightToLeftX(double righthandx, double righthandy) { return (righthandy*-1); }
    private double convertRightToLeftHeading(double righthandlocalHeading) { return righthandlocalHeading*-1; }

    /**
     * WARNING: NAVISENS uses the Right Hand Coordinate System. Meaning
     *                        X
     *                        ^
     *                        |
     *                        |
     *                        |
     *                        |
     *                        |
     * Y <---------------------
     * From X=0,Y=0,Deg=0, Walk straight: X increase
     * From X=0,Y=0,Deg=0, Turn right then walk straight: Degree=-90, Y decreases
     * From X=0,Y=0,Deg=0, Turn left then walk straight: Degree=90, Y increases
     * From X=0,Y=0,Deg=0, Turn around then walk straight: Degree=180/-180, X decreases
     * Not to worry, this class turns that into the more conventional Left hand Coordinate System for user consumption.
     *
     * This constructor will be replaced with NavisensLocalTracker(Activity context, boolean useLastKnownLoc)
     * The constructor accepts a Activity class. This is needed to provide backdoor access to the UI. (E.g. Toast)<br>
     * Constructor will always get a initial location update via last known GPS, if any.<br>
     * It will then initialise the NAVISENS API core. After constructor completes, NAVISENS should be providing regular location updates.
     * @param context
     */
    public NavisensLocalTracker(Activity context){
        Log.d(TAG,"Initialising Navisens Integrator");
        this.listener = null;
        this.context=context;
        Log.d(TAG,"Checking for strict mode");
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            Log.d(TAG,"Enabling strict mode access");
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Log.d(TAG,"Loading Navisens");
        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("model","dsta");
        configuration.put("gps",true);
        configuration.put("callback",UPDATERATE_MS);
        configuration.put("logging",true);

        motionDnaApp = new MotionDnaSDK(context,this);
        motionDnaApp.start(DEVELOPER_KEY,configuration);

        Log.d(TAG,"DEVELOPER_KEY: " + DEVELOPER_KEY);
        Log.i(TAG,"Running Navisens SDK version " + MotionDnaSDK.SDKVersion() + " on " + motionDnaApp.getDeviceModel());
        Log.d(TAG,"Setting update rate to " + UPDATERATE_MS + " ms");

        // motionDnaApp.setGnssOrGpsStatusEnabled(true);
        // motionDnaApp.setExternalPositioningState(MotionDna.ExternalPositioningState.HIGH_ACCURACY);
    }

    /**
     * This is called whenever NAVISENS API core receives a new location update.<br>
     * NAVISENS provides 2 types of locations, a global lat long if its initialised with a lat long, and a local XY that starts from 0,0.<br>
     * Call listener.onNewCoords to pass the coordinates to the parent class
     * {@inheritDoc}
     * @param motionDna Not to worry about this, handled internally by NAVISENS
     * @see TrackerListener
     */
    @Override
    public void receiveMotionDna(MotionDna motionDna) {
        Log.d(TAG,"Received update from Navisens Tracker");
        MotionDna.Location loc = motionDna.getLocation();
        String motionType=motionDna.getClassifiers().get("motion").prediction.label;

        MotionDna.CartesianLocation localLocation= motionDna.getLocation().cartesian;
        double righthandx = localLocation.x;
        double righthandy = localLocation.y;
        double z = localLocation.z;
        double righthandlocalHeading = motionDna.getLocation().global.heading;

        double x=convertRightToLeftX(righthandx,righthandy);
        double y=convertRightToLeftY(righthandx, righthandy);
        double localHeading = convertRightToLeftHeading(righthandlocalHeading);
        this.currentX=x;
        this.currentY=y;
        this.currentBearing=localHeading;
        this.currentZ=this.mapHeight;
        performTrackingHeightOffsetAdjustment(z);
        Log.i(TAG,"X:"+x + " Y:"+y + " Z:"+z + " Heading:" + localHeading + " EstimatedMotion:" + motionType);
        // TODO: Nav 2.0.1 appears to have removed accuracy + tracking of status. Confirm, and if this is not the case, add it back in.
        listener.onNewCoords(new Coords(z,z,this.mapHeight,localHeading,(float)0.0,(float)0.0, (float)0.0, x, y, motionType));
    }

    private void performTrackingHeightOffsetAdjustment(double z) {
        this.currentOffset=z-this.currentHeight;
        this.mapHeight=this.mapHeight+this.currentOffset;
        this.currentHeight=z;
    }

    /**
     * Enable regular GPS updates<br>
     * {@inheritDoc}
     */
    public Coords getCurrentXYZLocation()
    {
        Coords coord = new Coords(0.0, 0.0, this.currentZ, this.currentBearing, 0,0, 0, this.currentX, this.currentY, "");
        return coord;
    }

    /**
     * Starts new motion DNA and creates new binary log file
     *
     */
    public void startMotionDnaAndCreateNewTestLogFile() {
        motionDnaApp = null;
        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("model","dsta");
        configuration.put("gps",true);
        configuration.put("callback",UPDATERATE_MS);
        configuration.put("logging",true);

        motionDnaApp = new MotionDnaSDK(this.context,this);
        motionDnaApp.start(DEVELOPER_KEY, configuration);

//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_SENSOR_TIMING,
//                "Error: Sensor Timing");
//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_AUTHENTICATION_FAILED,
//                "Error: Authentication Failed");
//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_SENSOR_MISSING,
//                "Error: Sensor Missing");
//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_SDK_EXPIRED,
//                "Error: SDK Expired");
//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_WRONG_FLOOR_INPUT,
//                "Error: Wrong Floor Input");
//        motionDnaApp.reportError(MotionDna.ErrorCode.ERROR_PERMISSIONS,
//                "Error: Permissions");
    }

    @Override
    public void reportStatus(MotionDnaSDK.Status status, String s) {
        switch (status) {
            case AuthenticationFailure:
                System.out.println("Error: Authentication Failed " + s);
                break;
            case AuthenticationSuccess:
                System.out.println("Status: Authentication Successful " + s);
                break;
            case ExpiredSDK:
                System.out.println("Status: SDK expired " + s);
                break;
            case PermissionsFailure:
                System.out.println("Status: permissions not granted " + s);
                break;
            case MissingSensor:
                System.out.println("Status: sensor missing " + s);
                break;
            case SensorTimingIssue:
                System.out.println("Status: sensor timing " + s);
                break;
            case Configuration:
                System.out.println("Status: configuration " + s);
                break;
            case None:
                System.out.println("Status: None " + s);
                break;
            default:
                System.out.println("Status: Unknown " + s);
        }
    }

    /**
     * Appends test log header to existing motion DNA binary log file
     *
     * @param logHeader
     */
    public void appendHeaderToExistingTestLogFile(String logHeader) {
        if (motionDnaApp != null) {
            motionDnaApp.log(logHeader);
        }
    }

}
