package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import android.util.Log;

import com.google.gson.Gson;

import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import timber.log.Timber;

public class JeroMQBroadcastOperation {

    private static final String TAG = JeroMQBroadcastOperation.class.getSimpleName();

    /**
     * Broadcasts insertion of data to connected devices in the network
     *
     * @param model
     */
    public static void broadcastDataInsertionOverSocket(Object model) {
        Timber.i("broadcastDataInsertionOverSocket");

        Gson gson = GsonCreator.createGson();
        String modelJson = gson.toJson(model);

        if (model instanceof UserModel) {
            JeroMQPublisher.getInstance().sendUserMessage(modelJson, JeroMQPublisher.TOPIC_INSERT);
        } else if (model instanceof WaveRelayRadioModel) {
            JeroMQPublisher.getInstance().sendWaveRelayRadioMessage(modelJson, JeroMQPublisher.TOPIC_INSERT);
        } else if (model instanceof BFTModel) {
            JeroMQPublisher.getInstance().sendBFTMessage(modelJson, JeroMQPublisher.TOPIC_INSERT);
        } else if (model instanceof SitRepModel) {
            JeroMQPublisher.getInstance().sendSitRepMessage(modelJson, JeroMQPublisher.TOPIC_INSERT);
        } else if (model instanceof TaskModel) {
            JeroMQPublisher.getInstance().sendTaskMessage(modelJson, JeroMQPublisher.TOPIC_INSERT);
        }
    }

    /**
     * Broadcasts update of data to connected devices in the network
     *
     * @param model
     */
    public static void broadcastDataUpdateOverSocket(Object model) {
        Timber.i("broadcastDataUpdateOverSocket");

        Gson gson = GsonCreator.createGson();
        String modelJson = gson.toJson(model);

        if (model instanceof UserModel) {
            JeroMQPublisher.getInstance().sendUserMessage(modelJson, JeroMQPublisher.TOPIC_UPDATE);
        } else if (model instanceof WaveRelayRadioModel) {
            JeroMQPublisher.getInstance().sendWaveRelayRadioMessage(modelJson, JeroMQPublisher.TOPIC_UPDATE);
        } else if (model instanceof SitRepModel) {
            JeroMQPublisher.getInstance().sendSitRepMessage(modelJson, JeroMQPublisher.TOPIC_UPDATE);
        } else if (model instanceof TaskModel) {
            JeroMQPublisher.getInstance().sendTaskMessage(modelJson, JeroMQPublisher.TOPIC_UPDATE);
        }
    }

    /**
     * Broadcasts deletion of data to connected devices in the network
     *
     * @param model
     */
    public static void broadcastDataDeletionOverSocket(Object model) {
        Timber.i("broadcastDataDeletionOverSocket");

        Gson gson = GsonCreator.createGson();
        String modelJson = gson.toJson(model);

        if (model instanceof UserModel) {
            JeroMQPublisher.getInstance().sendUserMessage(modelJson, JeroMQPublisher.TOPIC_DELETE);
        } else if (model instanceof SitRepModel) {
            JeroMQPublisher.getInstance().sendSitRepMessage(modelJson, JeroMQPublisher.TOPIC_DELETE);
        } else if (model instanceof TaskModel) {
            JeroMQPublisher.getInstance().sendTaskMessage(modelJson, JeroMQPublisher.TOPIC_DELETE);
        }
    }

//    /**
//     * Unicast synchronisation of data to connected device in the network
//     *
//     * @param model
//     */
//    public static void UnicastDataSyncOverSocket(Object model, String targetPhoneIPAddress) {
//        Timber.i("UnicastDataSyncOverSocket");
//
//        Gson gson = GsonCreator.createGson();
//        String modelJson = gson.toJson(model);
//
//        if (model instanceof SitRepModel) {
//            JeroMQClientPair.getInstance().sendSitRepMessage(modelJson, JeroMQPublisher.TOPIC_SYNC);
//        } else if (model instanceof TaskModel) {
//            JeroMQClientPair.getInstance().sendTaskMessage(modelJson, JeroMQPublisher.TOPIC_SYNC);
//        }
//    }

//    /**
//     * Broadcasts OWN force BFT data to connected devices in the network
//     *
//     * @param message
//     */
//    public static void broadcastBftOwnForceDataOverSocket(String message) {
//        Timber.i("broadcastBftOwnForceDataOverSocket");
//
//        JeroMQPublisher.getInstance().sendBFTMessage(message, JeroMQParent.TOPIC_OWN_FORCE);
//    }
//
//    /**
//     * Broadcasts other types BFT data to connected devices in the network
//     *
//     * @param message
//     */
//    public static void broadcastBftOthersDataOverSocket(String message) {
//        Timber.i("broadcastBftOthersDataOverSocket");
//
//        JeroMQPublisher.getInstance().sendBFTMessage(message, JeroMQParent.TOPIC_OTHERS);
//    }
}
