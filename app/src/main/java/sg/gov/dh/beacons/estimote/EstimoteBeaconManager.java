package sg.gov.dh.beacons.estimote;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.NfcF;
import android.util.Log;
import android.widget.Toast;

import com.estimote.coresdk.recognition.utils.DeviceId;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import sg.gov.dh.beacons.BeaconObject;
import sg.gov.dh.beacons.BeaconListener;
import sg.gov.dh.beacons.BeaconManagerInterface;



public class EstimoteBeaconManager implements BeaconManagerInterface {

    //Following will support Estimote's NFC features
    PendingIntent pendingIntent;
    IntentFilter ndef;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    private NfcAdapter nfcAdapter;
    private Intent NfcIntent;

    //Following will support Estimote's general features.
    private ProximityObserver.Handler proximityObserverHandler;
    EstimoteCloudCredentials cloudCredentials =null;
    String APPID;
    String APPTOKEN;
    private BeaconListener listener;
    double minDist;
    Activity context = null;
    String TAG = "EstimoteBeaconManager";

    public EstimoteBeaconManager(Activity context){
        this.setParentContext(context);
    }

    private void setParentContext(Activity context) {
        this.context=context;
    }

    @Override
    public void setAppId(String id) {
        APPID=id;
    }

    @Override
    public void setAppToken(String token) {
        APPTOKEN=token;
    }

    @Override
    public void setDistActivate(double dist) {
        this.minDist=dist;
    }

    @Override
    public String getBeaconIdbByNFC(NdefMessage nfcMsg) {
        DeviceId beaconId = findBeaconId(nfcMsg);
        return beaconId.toString().substring(1,4);

    }


    public void disableForegroundDispatch() {
        if (nfcAdapter!=null){
            nfcAdapter.disableForegroundDispatch(this.context);
        }
    }


    public void enableForegroundDispatch() {
        nfcAdapter.enableForegroundDispatch(this.context, pendingIntent, null, null);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setBeaconListener(BeaconListener listener) {
        this.listener = listener;

    }

    @Override
    public void deactivate() {
        proximityObserverHandler.stop();
    }

    @Override
    public void setup() {
        cloudCredentials = new EstimoteCloudCredentials(APPID, APPTOKEN);
        setupEstimoteRequirements();
        setupNFC();
        setupProximity();
    }

    private void setupEstimoteRequirements() {
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(context,
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                Log.d(TAG, "requirements fulfilled");
                                Toast.makeText(context.getApplicationContext(), "Estimote requirements satisfied",Toast.LENGTH_LONG);
                                return null;
                            }
                        },
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Log.e(TAG, "requirements missing: " + requirements);
                                return null;
                            }
                        },
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e(TAG, "requirements error: " + throwable);
                                return null;
                            }
                        });
    }

    private void setupProximity() {
        ProximityObserver proximityObserver = new ProximityObserverBuilder(context.getApplicationContext(), cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.e(TAG, "proximity observer error: " + throwable);
                        Toast.makeText(context.getApplicationContext(),"BLUETOOTH ERROR, try disabling and re-enable BT, then restart",Toast.LENGTH_LONG);
                        return null;
                    }
                })
                .withLowLatencyPowerMode()
                .withEstimoteSecureMonitoringDisabled()
                .withTelemetryReportingDisabled()
                .build();

        Log.d(TAG, "Setting up proximity zone for a range of " + minDist + " metres");
        ProximityZone zone = new ProximityZoneBuilder()
                .forTag(APPID)
                .inCustomRange(minDist)
//                .onExit(new Function1<ProximityZoneContext, Unit>() {
//                    @Override
//                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
//                        String title = proximityZoneContext.getAttachments().get(APPID+"/title");
//                        Log.d(TAG,"On Exit beacons == " + title);
//                        if (title == null) {
//                            title = "unknown";
//                        }
//                        listener.onNewUpdate(new BeaconObject(title));
//                        return null;
//                    }
//                })
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        String title = proximityZoneContext.getAttachments().get(APPID+"/title");
                        String subtitle = proximityZoneContext.getDeviceId().substring(0,3);
                        Log.d(TAG,"On Enter beacons == " + title + "/" + subtitle);
                        if (title == null) {
                            title = "unknown";
                        }
                        listener.onNewUpdate(new BeaconObject(subtitle));
                        return null;
                    }
                })
                .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
                    @Override
                    public Unit invoke(Set<? extends ProximityZoneContext> contexts) {
                        List<EstimoteProximityContent> nearbyContent = new ArrayList<>(contexts.size());
                        Log.d(TAG,"On Context Change beacons == " + contexts.size());
                        for (ProximityZoneContext proximityContext : contexts) {
                            String title = proximityContext.getAttachments().get(APPID+"/title");
                            if (title == null) {
                                title = "unknown";
                            }
//                            String subtitle = EstimoteUtils.getShortIdentifier(proximityContext.getDeviceId());
                            String subtitle = proximityContext.getDeviceId().substring(0,3);
                            Log.d(TAG,"On Context Change " + title+"="+subtitle);
                            listener.onNewUpdate(new BeaconObject(subtitle));
//                            nearbyContent.add(new EstimoteProximityContent(title, subtitle));
//                            listener.onNewUpdate(new BeaconObject(subtitle));
                        }


                        return null;
                    }
                })
                .build();

        proximityObserverHandler = proximityObserver.startObserving(zone);

    }

    private void setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this.context);
        pendingIntent = PendingIntent.getActivity(
                this.context, 0, new Intent(this.context, this.context.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray= new IntentFilter[] {ndef, };
        techListsArray = new String[][] { new String[] { NfcF.class.getName() } };
        NfcIntent = new Intent(this.context.getApplicationContext(), this.context.getClass());
    }


    private static DeviceId findBeaconId(NdefMessage msg) {
        NdefRecord[] records = msg.getRecords();
        for (NdefRecord record : records) {
            if (record.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE) {
                String type = new String(record.getType(), Charset.forName("ascii"));
                if ("estimote.com:id".equals(type)) {
                    return DeviceId.fromBytes(record.getPayload());
                }
            }
        }
        return null;
    }

}
