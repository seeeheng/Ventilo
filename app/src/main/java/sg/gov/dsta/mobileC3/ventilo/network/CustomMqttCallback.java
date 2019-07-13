package sg.gov.dsta.mobileC3.ventilo.network;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CustomMqttCallback implements MqttCallback {

    private NetworkService service;
    public CustomMqttCallback(NetworkService service) {
        this.service = service;
    }

    public CustomMqttCallback() {

    }


    @Override
    public void connectionLost(Throwable throwable) {

    }

    //When notification message is received from the server, this method will be called
    //Depending on the message type, the corresponding notification will be created and sent to the user
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

//        SharedPreferences sharedPrefs = service.getApplicationContext().getSharedPreferences(NotificationsActivity.NOTIFICATION_PREF, Context.MODE_MULTI_PROCESS);
//        int num = sharedPrefs.getInt(NotificationsActivity.NOTIFICATION_NUM, 0);
//        num++;
//        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
//        prefsEditor.putInt(NotificationsActivity.NOTIFICATION_NUM, num);
//        prefsEditor.commit();
//
//        Gson gson = GsonCreator.createGson();
//        NotificationMessage msg = gson.fromJson(mqttMessage.toString(), NotificationMessage.class);
//        Context context = service.getApplicationContext();


//        switch (msg.getMessageType()) {
//            case LIKE_POST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_LIKE_POST, true)){
//                    LikeNotificationCreator likeNotificationCreator = new LikeNotificationCreator(context);
//                    likeNotificationCreator.createNotification(msg);
//                }
//                break;
//            case LIKE_REQUEST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_LIKE_REQUEST, true)){
//                    LikeNotificationCreator likeNotificationCreator = new LikeNotificationCreator(context);
//                    likeNotificationCreator.createNotification(msg);
//                }
//                break;
//            case FOLLOWING_POST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_FOLLOW_POST, true)) {
//                    FollowingNotificationCreator followingNotificationCreator = new FollowingNotificationCreator(context);
//                    followingNotificationCreator.createNotification(msg);
//                }
//                break;
//            case FOLLOWING_REQUEST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_FOLLOW_REQUEST, true)) {
//                    FollowingNotificationCreator followingNotificationCreator = new FollowingNotificationCreator(context);
//                    followingNotificationCreator.createNotification(msg);
//                }
//                break;
//            case IN_REQUEST_RANGE:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_IN_RANGE, true)) {
//                    InRequestRangeNotificationCreator inRequestRangeNotificationCreator = new InRequestRangeNotificationCreator(context);
//                    inRequestRangeNotificationCreator.createNotification(msg);
//                }
//                break;
//            case GROUP_POST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_GROUP_POST, true)) {
//                    GroupNotificationCreator groupNotificationCreator = new GroupNotificationCreator(context);
//                    groupNotificationCreator.createNotification(msg);
//                }
//                break;
//            case GROUP_REQUEST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_GROUP_REQUEST, true)) {
//                    GroupNotificationCreator groupNotificationCreator = new GroupNotificationCreator(context);
//                    groupNotificationCreator.createNotification(msg);
//                }
//                break;
//            case COMMENT_POST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_COMMENT_POST, true)) {
//                    CommentNotificationCreator commentNotificationCreator = new CommentNotificationCreator(context);
//                    commentNotificationCreator.createNotification(msg);
//                }
//                break;
//            case COMMENT_REQUEST:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_COMMENT_REQUEST, true)) {
//                    CommentNotificationCreator commentNotificationCreator = new CommentNotificationCreator(context);
//                    commentNotificationCreator.createNotification(msg);
//                }
//                break;
//            case RESPONSE:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_RESPONSE, true)) {
//                    ResponseNotificationCreator responseNotificationCreator = new ResponseNotificationCreator(context);
//                    responseNotificationCreator.createNotification(msg);
//                }
//                break;
//            case GROUP_INVITATION:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_GROUP_INVITE, true)) {
//                    GroupInviteNotificationCreator groupInviteNotificationCreator = new GroupInviteNotificationCreator(context);
//                    groupInviteNotificationCreator.createNotification(msg);
//                }
//                break;
//            case THANK:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_THANK, true)) {
//                    ThankNotificationCreator thankNotificationCreator = new ThankNotificationCreator(context);
//                    thankNotificationCreator.createNotification(msg);
//                }
//                break;
//            case FOLLOW_YOU:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_FOLLOW_YOU, true)) {
//                    FollowYouNotificationCreator followYouNotificationCreator = new FollowYouNotificationCreator(context);
//                    followYouNotificationCreator.createNotification(msg);
//                }
//                break;
//            case RANK:
//                if (sharedPrefs.getBoolean(NotificationsActivity.NOTIFICATION_RANK, true)) {
//                    RankNotificationCreator rankNotificationCreator = new RankNotificationCreator(context);
//                    rankNotificationCreator.createNotification(msg);
//                }
//                break;
//            default:
//        }


//        Intent intent = new Intent(context, ProfileActivity.class);
//        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
////        android.app.Notification n  = new android.app.Notification.Builder(context)
////                .setContentTitle("SharedSense")
////                .setContentText("@lohver needs your help. Tap to view.")
////                .setSmallIcon(R.drawable.btn_small_appbar)
////                .setContentIntent(pIntent)
////                .setSubText("testy testy test")
////                .setAutoCancel(true).build();
//

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
