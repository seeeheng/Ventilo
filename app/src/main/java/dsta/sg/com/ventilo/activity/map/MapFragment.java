package dsta.sg.com.ventilo.activity.map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.ArcGISMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dsta.sg.com.ventilo.PinView;
import dsta.sg.com.ventilo.R;
import dsta.sg.com.ventilo.model.Blueprint;
import dsta.sg.com.ventilo.model.Pin;
import dsta.sg.com.ventilo.util.BlueprintManager;
import dsta.sg.com.ventilo.util.DimensionUtil;
import dsta.sg.com.ventilo.util.DrawableUtil;
import dsta.sg.com.ventilo.util.MapDistanceConversionUtil;
import dsta.sg.com.ventilo.util.constant.VoiceCommands;
import sg.gov.dh.trackers.Coords;
import sg.gov.dh.trackers.Event;
import sg.gov.dh.trackers.NavisensLocalTracker;
import sg.gov.dh.trackers.TrackerListener;

public class MapFragment extends Fragment implements RecognitionListener {
    public static final int RESULT_SPEECH = 100;
    private static final String TAG = "MapFragment";

    // Map
    private MapView mArcGISMapView;
    private MapView mCesiumMapView;

    private static boolean mIsBFTTracking;
    private Button mBtnSpeak;
    private TextView mTvSpeakText;

    private String mResultText;

    // BFT Tracker
    private NavisensLocalTracker mBftCustomTracker;
    private PinView mPinView;
    private PointF mOffsetLocationPoint;
    private int mImageDisplayHeight;
    private int mImageDisplayLength;

//    private static boolean isDone;
//    private Map<Integer, Pin> pinMap;
//    private int x = 200;
//    private int y = 200;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootMapView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = rootMapView.findViewById(R.id.mapview_arcgis_map);
        mCesiumMapView = rootMapView.findViewById(R.id.mapview_cesium_map);
        mBtnSpeak = rootMapView.findViewById(R.id.btn_mic_speak);
        mTvSpeakText = rootMapView.findViewById(R.id.tv_mic_text);
        mBtnSpeak.setOnClickListener(micSpeakOnClickListener());

        // Map
//        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 34.056295, -117.195800, 16);
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 1.290270, 103.851959, 16);
        mMapView.setMap(map);
//        mMapView.resume();
//        mMapView.setZ(2);
//        mMapView.setVisibility(View.VISIBLE);
//        mMapView.bringToFront();




        mPinView = rootMapView.findViewById(R.id.img_map_main);

        BlueprintManager.getInstance().initBlueprintList(getContext());

        for (Blueprint blueprint : BlueprintManager.getInstance().getBlueprintList()) {
//            Integer resID = entry.getKey();
//            String resName = entry.getValue();

            Integer resID = blueprint.getResID();
            String resName = blueprint.getResName();

            if ("map_heli_deck_116_5m_x_27_3m_height_45p5_length_68_to_225".equalsIgnoreCase(resName)) {
                mPinView.setImage(ImageSource.resource(resID));
//                mPinView.setImageID(resID);
                BlueprintManager.getInstance().setDisplayedBlueprint(blueprint);
//                mPinView.storeBlueprintDetails(resName, resID);

//                BitmapFactory.Options dimensions = new BitmapFactory.Options();
//                dimensions.inJustDecodeBounds = true;
//                BitmapFactory.decodeResource(getResources(), resID, dimensions);
//                int height = dimensions.outHeight;
//                int width = dimensions.outWidth;


//                System.out.println("height before is " + height);
//                System.out.println("width before is " + width);
//                    mImageDisplayHeight = mPinView.getScaledImagePixelFromBlueprint(height);
//                    mImageDisplayLength = mPinView.getScaledImagePixelFromBlueprint(width);

                // Checks if image length is larger than screen width; adjust image size accordingly
//                int screenWidth = DimensionUtil.getScreenWidth(getActivity());
//                if (width > screenWidth) {
//                    double scaleFitFactor = (double) screenWidth / (double) width;
//                    height = (int) Math.round(scaleFitFactor * height);
//                    width = screenWidth;
//                }
//
//                mPinView.getLayoutParams().height = height;
//                mPinView.getLayoutParams().width = width;

//                System.out.println("mPinView.getHeight() is " + mPinView.getHeight());
//                System.out.println("mPinView.getWidth() is " + mPinView.getWidth());


//                mImageDisplayHeight = mPinView.getScaledImagePixelFromBlueprint(height);
//                mImageDisplayLength = mPinView.getScaledImagePixelFromBlueprint(width);

//                System.out.println("mPinView.getMeasuredHeight() is " + mPinView.getMeasuredHeight());
//                System.out.println("mPinView.getMeasuredWidth() is " + mPinView.getMeasuredWidth());

//                System.out.println("height is " + height);
//                System.out.println("width is " + width);

//                mImageDisplayHeight = mPinView.getScaledImagePixelFromBlueprint(height);
//                mImageDisplayLength = mPinView.getScaledImagePixelFromBlueprint(width);

//                System.out.println("mImageDisplayHeight is " + mImageDisplayHeight);
//                System.out.println("mImageDisplayLength is " + mImageDisplayLength);
//                Pin ownPin = new Pin();
//                ownPin.setPinID(1);
//                ownPin.setBearing(90.0f);
//                float centerX = mImageDisplayLength / 2;
//                float centerY = mImageDisplayHeight / 2;
//
//                System.out.println("centerX is " + centerX);
//                System.out.println("centerY is " + centerY);
//
//                setOffsetLocationPoint(0f, 0f);
//                ownPin.setPinInitialLocationPoint(new PointF(centerX, centerY));
//                ownPin.setPinCurrentLocationPoint(new PointF(centerX, centerY));
////        pin1.setPinLocationPoint(new PointF(2151.0f, 1554.0f));
//
//                mPinView.initOwnLocation(ownPin.getPinID(), ownPin);

                Pin pin2 = new Pin();
                pin2.setPinID(2);
                pin2.setBearing(180.0f);
                pin2.setPinInitialLocationPoint(new PointF(300.0f, 300.0f));
                pin2.setPinCurrentLocationPoint(new PointF(300.0f, 300.0f));

                mPinView.addPin(pin2.getPinID(), pin2);
            }
        }

        return rootMapView;
    }

    private View.OnClickListener micSpeakOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateMicrophone();
            }
        };
    }

    private void activateMicrophone() {
        SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speech.setRecognitionListener(this);

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getActivity().getPackageName());

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

//        if (speech.g)
        speech.startListening(recognizerIntent);

//        try {
//
//            startActivityForResult(recognizerIntent, RESULT_SPEECH);
//        } catch (ActivityNotFoundException a) {
//
//            Toast.makeText(getActivity().getApplicationContext(),
//                    "Opps! Your device doesn’t support Speech to Text", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int errorCode) {

        switch (errorCode) {

            case SpeechRecognizer.ERROR_AUDIO:

                mResultText = "Error - audio";

                break;

            case SpeechRecognizer.ERROR_CLIENT:

                mResultText = "Error - client";

                break;

            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

                mResultText = "Error - insufficient permissions";

                break;

            case SpeechRecognizer.ERROR_NETWORK:

                mResultText = "Error - network";

                break;

            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:

                mResultText = "Error - network timeout";

                break;

            case SpeechRecognizer.ERROR_NO_MATCH:

                mResultText = "Error - no match";

                break;

            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

                mResultText = "Error - recogniser busy";

                break;

            case SpeechRecognizer.ERROR_SERVER:

                mResultText = "Error - server";

                break;

            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:

                mResultText = "Error - speech timeout";

                break;

            default:

                mResultText = "Error - do not understand";

                break;
        }

        mTvSpeakText.setText(mResultText);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
    }

    @Override
    public void onPartialResults(Bundle arg0) {
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        mTvSpeakText.setText("text is " + VoiceCommands.getPredictedWords(matches));
        Log.d("SPEAK", matches.toString());
    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case RESULT_SPEECH: {
//                if (resultCode == RESULT_OK && null != data) {
//                    ArrayList<String> text = data
//                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    mTvSpeakText.setText("text is " + text);
//                    Log.d("ACTIVITY SPEAK", "activity speaking");
//                }
//
//                break;
//            }
//        }
//    }


    private void setOffsetLocationPoint(float centerX, float centerY) {
        if (mOffsetLocationPoint == null) {
            mOffsetLocationPoint = new PointF(centerX, centerY);
        } else {
            mOffsetLocationPoint.set(centerX, centerY);
        }
    }

    // Note that this NavisensLocalTracker class can only register XYZ offsets in metres, NOT lat/long.
    private void startBFTTracking() {
        if (!mIsBFTTracking) {
//            NavisensCore core = new NavisensCore(MOTIONDNA_KEY, this);
//            NavisensMaps maps = core.init(NavisensMaps.class)
//                    .useLocalOnly()
//                    .showPath().hideMarkers();

            mBftCustomTracker = new NavisensLocalTracker(getActivity());
            mBftCustomTracker.setActive(true);
            mBftCustomTracker.setTrackerListener(new TrackerListener() {
                @Override
                public void onNewCoords(Coords coords) {
                    //You receive the coordinates here, you can do whatever you want with it.
                    Log.d(TAG, "X:" + coords.getX());
                    Log.d(TAG, "Y:" + coords.getY());
                    Log.d(TAG, "Z:" + coords.getAltitude());
                    Log.d(TAG, "bearing:" + coords.getBearing());
                    Log.d(TAG, "Action:" + coords.getAction());

//                    pinView.setPinBearing((float) coords.getBearing());
//                    pinView.setPin(new PointF(x, y));

                    BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInPixels();
                    float pixelPerMetreScaleFactor = MapDistanceConversionUtil.getScaledImagePixelsPerMetre(
                            BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInPixels(),
                            BlueprintManager.getInstance().getDisplayedBlueprint().getLengthInMetres());

                    mPinView.setOwnPinBearing((float) coords.getBearing());
                    setOffsetLocationPoint(((float) coords.getX() * pixelPerMetreScaleFactor),
                            ((float) -coords.getY() * pixelPerMetreScaleFactor));
                    mPinView.setOwnPinCoordinates(mOffsetLocationPoint);

//                    mTvSpeakText.setText("x is " + coords.getX());
//                    Matrix matrix = new Matrix();
//                    float imageCenterX = pinView.getX() + pinView.getWidth()  / 2;
//                    float imageCenterY = pinView.getY() + pinView.getHeight() / 2;
//                    matrix.setRotate(coords.getBearing(), imageCenterX, imageCenterY);
//                    yourCanvas.drawBitmap(yourBitmap, matrix, null);
//                    pinView.draw();

//                    y += 10;

//                bftCustomTracker.deactivate();
//                updateMap(coords);
//                sendCoords(coords);
                }

                @Override
                public void onNewEvent(Event event) {
                    //This is deprecated in current version. Replaced by coords.getAction().
                }
            });

            mIsBFTTracking = true;
        }
    }

    private void stopBFTTracking() {
        if (mIsBFTTracking && mBftCustomTracker != null && mBftCustomTracker.isActive()) {
            mBftCustomTracker.deactivate();
            mBftCustomTracker = null;
            mIsBFTTracking = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Call MapView.pause to suspend map rendering while the activity is paused, which can save battery usage.
//        if (mMapView != null) {
//            mMapView.pause();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Call MapView.resume to resume map rendering when the activity returns to the foreground.
        if (mMapView != null) {
            mMapView.resume();
        }

//        startBFTTracking();
    }

    @Override
    public void onStop() {
        super.onStop();
//        stopBFTTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (mMapView != null) {
////            mMapView.dispose();
//        }
    }
}
