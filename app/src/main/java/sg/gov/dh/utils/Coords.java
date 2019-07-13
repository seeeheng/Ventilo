package sg.gov.dh.utils;

import javax.xml.parsers.FactoryConfigurationError;

/**
 * This class is meant to allow 3rd party trackers to encapsulate their representation of Location into something that parent classes (E.g. Map Provider's LocationDataSource class) can view.
 */

public class Coords {


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    private String action="";

    private boolean local= false;
    /**
     * Local X displacement, if available
     */
    private double x=0;

    /**
     * Local Y displacement, if available
     */
    private double y=0;


    /**
     * Latitude (WGS84)
     */
    private double latitude=0;

    /**
     * Longitude (WGS84)
     */
    private double longitude=0;

    /**
     * In metres. Default=0
     */
    private double altitude=0;

    /**
     * The +/- error of the bearing in degrees. Default=0<br>
     * Be mindful that the Map provider may use this differently.
     */
    private float bear_accuracy=1;


    /**
     * The +/- error of the horizonal coordinate in metres. Default=0<br>
     * Be mindful that the Map provider may use this differently.
     */
    private float hori_accuracy=1;

    /**
     * The +/- error of the vertical coordinate in metres. Default=0<br>
     * Be mindful that the Map provider may use this differently.
     */
    private float vert_accuracy=1;

    /**
     * In degrees. Default=0
     */
    private double bearing=0;

    /**
     *
     * @return
     */
    public float getBear_accuracy() {
        return bear_accuracy;
    }

    /**
     *
     * @param bear_accuracy
     */
    public void setBear_accuracy(float bear_accuracy) {
        this.bear_accuracy = bear_accuracy;
    }

    /**
     *
     * @return
     */
    public float getHori_accuracy() {
        return hori_accuracy;
    }

    /**
     *
     * @param hori_accuracy
     */
    public void setHori_accuracy(float hori_accuracy) {
        this.hori_accuracy = hori_accuracy;
    }

    /**
     *
     * @return
     */
    public float getVert_accuracy() {
        return vert_accuracy;
    }

    /**
     *
     * @param vert_accuracy
     */
    public void setVert_accuracy(float vert_accuracy) {
        this.vert_accuracy = vert_accuracy;
    }

    /**
     *
     * @return
     */
    public double getBearing() {
        return bearing;
    }

    /**
     *
     * @param bearing
     */
    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    /**
     * There's only one constructor which demands all the input. If there's no input from the 3rd party tracker, simply leave it at zero.
     * @param latitude
     * @param longitude
     * @param altitude
     * @param bearing
     * @param hori_accuracy
     * @param vert_accuracy
     * @param bear_accuracy
     */


    public Coords(double latitude, double longitude, double altitude, double bearing, float hori_accuracy,float vert_accuracy, float bear_accuracy) {
        this.latitude=latitude;
        this.longitude=longitude;
        this.altitude=altitude;
        this.bearing=bearing;
        this.hori_accuracy=hori_accuracy;
        this.vert_accuracy=vert_accuracy;
        this.bear_accuracy=bear_accuracy;
    }

    public Coords(double latitude, double longitude, double altitude, double bearing, float hori_accuracy,float vert_accuracy, float bear_accuracy, double x, double y, String motionType) {
        this.latitude=latitude;
        this.longitude=longitude;
        this.altitude=altitude;
        this.bearing=bearing;
        this.hori_accuracy=hori_accuracy;
        this.vert_accuracy=vert_accuracy;
        this.bear_accuracy=bear_accuracy;
        this.setX(x);
        this.setY(y);
        this.setAction(motionType);
    }

    public Coords(double latitude, double longitude, double altitude, double bearing, float hori_accuracy,float vert_accuracy, float bear_accuracy, double x, double y, boolean isLocal, String motionType) {
        this.latitude=latitude;
        this.longitude=longitude;
        this.altitude=altitude;
        this.bearing=bearing;
        this.hori_accuracy=hori_accuracy;
        this.vert_accuracy=vert_accuracy;
        this.bear_accuracy=bear_accuracy;
        this.setX(x);
        this.setY(y);
        this.setLocal(isLocal);
        this.setAction(motionType);
    }

    /**
     *
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     *
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     *
     * @param altitude
     */
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }


    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }
}
