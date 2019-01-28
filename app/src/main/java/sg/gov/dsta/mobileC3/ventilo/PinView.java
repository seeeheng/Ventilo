package sg.gov.dsta.mobileC3.ventilo;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.HashMap;
import java.util.Map;

import sg.gov.dsta.mobileC3.ventilo.model.Blueprint;
import sg.gov.dsta.mobileC3.ventilo.model.Pin;
import sg.gov.dsta.mobileC3.ventilo.util.BlueprintManager;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.MapDistanceConversionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MyInfo;

public class PinView extends SubsamplingScaleImageView {

    private final int densityScalingFactor = getResources().getDisplayMetrics().densityDpi / 160;

    private final Paint paint = new Paint();
    private final PointF vPin = new PointF();
//    private static boolean mIsFirstTimeDraw = true;
//    private Blueprint mBlueprint;

    private Map<Integer, Pin> mPinMap;
    //    private PointF mOwnLocationPoint;
    private PointF sPin;
    private Bitmap pin;
    private float mScaledWidth;
    private float mScaledHeight;
    private float mCenterX;
    private float mCenterY;
    private static boolean isFirstOwnMarkerCreated = true;

    public PinView(Context context) {
        this(context, null);
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

//    public void setPin(PointF sPin) {
//        this.sPin = sPin;
//        initialise();
//        invalidate();
//    }

//    public void setPinMap(Map<Integer, Pin> pinMap) {
//        this.mPinMap = pinMap;
//        initialise();
//        invalidate();
//    }

//    public Blueprint setImageID() {
//        return mBlueprint;
//    }

    public void setOwnPinBearing(float bearing) {
        if (mPinMap != null) {
            for (Map.Entry<Integer, Pin> entry : mPinMap.entrySet()) {
                if (entry.getKey() == MyInfo.ID) {
                    entry.getValue().setBearing(bearing);
                    invalidate();
                }
            }
        }
    }

    public void setOwnPinCoordinates(PointF ownOffsetPoint) {
        if (mPinMap != null) {
            for (Map.Entry<Integer, Pin> entry : mPinMap.entrySet()) {
                if (entry.getKey() == MyInfo.ID) {
                    float initialOwnLocationPointX = entry.getValue().getPinInitialLocationPoint().x;
                    float initialOwnLocationPointY = entry.getValue().getPinInitialLocationPoint().y;
                    PointF newOwnLocationPoint = entry.getValue().getPinCurrentLocationPoint();
                    newOwnLocationPoint.set(initialOwnLocationPointX + ownOffsetPoint.x,
                            initialOwnLocationPointY + ownOffsetPoint.y);
                    entry.getValue().setPinCurrentLocationPoint(newOwnLocationPoint);
                    invalidate();
                }
            }
        }
    }

//    public void setOwnLocationPoint(float centerX, float centerY) {
//        if (mOwnLocationPoint == null) {
//            mOwnLocationPoint = new PointF(centerX, centerY);
//        } else {
//            mOwnLocationPoint.set(centerX, centerY);
//        }
//
//        invalidate();
//    }

    private void initialise() {
        float density = getResources().getDisplayMetrics().densityDpi;
        System.out.println("density is " + density);
        pin = BitmapFactory.decodeResource(this.getResources(), R.drawable.blue_marker);
        mScaledWidth = (density / 420f) * pin.getWidth();
        mScaledHeight = (density / 420f) * pin.getHeight();
        pin = Bitmap.createScaledBitmap(pin, (int) mScaledWidth, (int) mScaledHeight, true);

//        if (mBlueprint == null) {
//            mBlueprint = new Blueprint();
//        }
//        initOwnLocation();
    }

    public void initOwnLocation(int pinID, Pin pin) {
        addPin(pinID, pin);
    }

//    public int getDensityScalingFactor() {
//        return densityScalingFactor;
//    }

    public void addPin(int pinID, Pin pin) {
        getPinMap().put(pinID, pin);
        invalidate();
    }

    public Map<Integer, Pin> getPinMap() {
        if (mPinMap == null) {
            mPinMap = new HashMap<>();
        }

        return mPinMap;
    }

//    public float getPixelsPerMetreFromBlueprint(int scaledImgLengthByPixels, float imgLengthByMetres) {
//        return MapDistanceConversionUtil.getPixelsPerMetre(scaledImgLengthByPixels,
//                imgLengthByMetres);
//    }

    public int getScaledImagePixelFromBlueprint(int unscaledImgLengthByPixels) {
        return MapDistanceConversionUtil.getScaledImagePixels(unscaledImgLengthByPixels,
                densityScalingFactor);
    }

//    public void storeBlueprintDetails(String blueprintResourceName) {
//        String[] blueprintInfo = MapDistanceConversionUtil.decodeMapResourceName(blueprintResourceName);
//        mBlueprint.setResName(blueprintInfo[0].toString());
//        mBlueprint.setHeightInMetres(Float.valueOf(blueprintInfo[1]));
//        mBlueprint.setLengthInMetres(Float.valueOf(blueprintInfo[2]));
//    }
//
//    public Blueprint getBlueprint() {
//        return mBlueprint;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }

        if (isFirstOwnMarkerCreated) {
//            System.out.println("mPinView.getMeasuredHeight() is " + getMeasuredHeight());
//            System.out.println("mPinView.getMeasuredWidth() is " + getMeasuredWidth());

            Pin ownPin = new Pin();
            ownPin.setPinID(1);
            ownPin.setBearing(90.0f);


//            BitmapFactory.Options dimensions = new BitmapFactory.Options();
//            dimensions.inJustDecodeBounds = true;
//            BitmapFactory.decodeResource(getResources(), resID, dimensions);
//            int height = dimensions.outHeight;
//            int width = dimensions.outWidth;

            Blueprint blueprintOnDisplay = BlueprintManager.getInstance().getDisplayedBlueprint();
            int blueprintPixelLength = blueprintOnDisplay.getLengthInPixels();
            int blueprintPixelHeight = blueprintOnDisplay.getHeightInPixels();

//            float centerX = getScaledImagePixelFromBlueprint(getMeasuredWidth() / 2);
//            float centerY = getScaledImagePixelFromBlueprint(getMeasuredHeight() / 2);

            int lengthToBeScaled;
            int heightToBeScaled;

            float centerX;

            int screenWidth = DimensionUtil.getScreenWidth();
            if (blueprintPixelLength > getMeasuredWidth()) {
                double scaleFitFactor = (double) getMeasuredWidth() / (double) screenWidth;
                lengthToBeScaled = Math.round(blueprintPixelLength * (float) scaleFitFactor);
                centerX = lengthToBeScaled / 2;
//                lengthToBeScaled = getMeasuredWidth();
            } else {
                lengthToBeScaled = blueprintPixelLength;
                centerX = getScaledImagePixelFromBlueprint( lengthToBeScaled / 2);
            }

            if (blueprintPixelHeight > getMeasuredHeight()) {
                heightToBeScaled = getMeasuredHeight();
            } else {
                heightToBeScaled = blueprintPixelHeight;
            }

            System.out.println("heightToBeScaled is " + heightToBeScaled);
            System.out.println("lengthToBeScaled is " + lengthToBeScaled);

//            float centerX = lengthToBeScaled / 2;

//            float centerX = getScaledImagePixelFromBlueprint( lengthToBeScaled / 2);
            float centerY = getScaledImagePixelFromBlueprint(heightToBeScaled / 2);

            System.out.println("getWidth is " + getWidth());
            System.out.println("getHeight is " + getHeight());

            System.out.println("blueprintPixelHeight is " + blueprintPixelHeight);
            System.out.println("blueprintPixelLength is " + blueprintPixelLength);

            System.out.println("getMeasuredWidth is " + getMeasuredWidth());
            System.out.println("getMeasuredHeight is " + getMeasuredHeight());


            float endX = getScaledImagePixelFromBlueprint(getMeasuredWidth());
            float endY = getScaledImagePixelFromBlueprint(getMeasuredHeight());

            System.out.println("getScaledImagePixelFromBlueprint(getMeasuredWidth) is " + endX);
            System.out.println("getScaledImagePixelFromBlueprint(getMeasuredHeight) is " + endY);

            System.out.println("centerX is " + centerX);
            System.out.println("centerY is " + centerY);

//            setOffsetLocationPoint(0f, 0f);
            ownPin.setPinInitialLocationPoint(new PointF(centerX, centerY));
            ownPin.setPinCurrentLocationPoint(new PointF(centerX, centerY));
//        pin1.setPinLocationPoint(new PointF(2151.0f, 1554.0f));

            initOwnLocation(ownPin.getPinID(), ownPin);

            isFirstOwnMarkerCreated = false;
        }

//        if (mIsFirstTimeDraw) {
//
//            mIsFirstTimeDraw = false;
//        }

        paint.setAntiAlias(true);

        for (Map.Entry<Integer, Pin> entry : mPinMap.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());

            sPin = entry.getValue().getPinCurrentLocationPoint();
            float bearing = entry.getValue().getBearing();

            if (sPin != null && pin != null) {
                sourceToViewCoord(sPin, vPin);

                System.out.println("pin.getWidth() is " + pin.getWidth());
                System.out.println("pin.getHeight() is " + pin.getHeight());

                // Coordinates vX and vY are top left of pin
                float vX = vPin.x - (pin.getWidth() / 2);
                float vY = vPin.y - (pin.getHeight() / 2);

                Matrix matrix = new Matrix();
//            float imageCenterX = vX + pin.getWidth() / 2;
//            float imageCenterY = vY + pin.getHeight() / 2;

                System.out.println("vX is " + vX);
                System.out.println("vY is " + vY);

                mCenterX = vX + (mScaledWidth) / 2;
                mCenterY = vY + (mScaledHeight) / 2;
                System.out.println("pin x center is " + mCenterX);
                System.out.println("pin y center is " + mCenterY);

                matrix.reset();
                /** Translate keeps the pin in place relative to image that is set upon,
                 * dependent on zoom and pan scale */
                matrix.setTranslate(vX, vY);
                matrix.postRotate(bearing, mCenterX, mCenterY);

                canvas.drawBitmap(pin, matrix, paint);

//            canvas.drawBitmap(pin, vX, vY, paint);
            }
        }
    }
}
