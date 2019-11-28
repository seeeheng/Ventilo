package sg.gov.dsta.mobileC3.ventilo.database;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.annotation.NonNull;

import java.util.concurrent.Executors;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.BFTDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.MapDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.SitRepDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserSitRepJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserTaskJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.VideoStreamDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.WaveRelayRadioDao;
import sg.gov.dsta.mobileC3.ventilo.model.bft.BFTModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.map.MapModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.videoStream.EOwner;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

@Database(entities = {UserModel.class, MapModel.class, BFTModel.class, SitRepModel.class,
        TaskModel.class, UserSitRepJoinModel.class, UserTaskJoinModel.class,
        VideoStreamModel.class, WaveRelayRadioModel.class},
        version = 1, exportSchema = false)
public abstract class VentiloDatabase extends RoomDatabase {

    private static final String USERNAME_ONE = "111";
    private static final String USERNAME_TWO = "222";
    private static final String USERNAME_THREE = "333";
    private static final String USERNAME_FOUR = "444";
    private static final String USERNAME_FIVE = "555";
    private static final String USERNAME_SIX = "666";
    private static final String USERNAME_SEVEN = "777";
    private static final String USERNAME_EIGHT = "888";
    private static final String CCT_ONE_USERNAME = "999";
    private static final String CCT_TWO_USERNAME = "987";

    // --- SINGLETON ---
    private static volatile VentiloDatabase INSTANCE;

    // --- DAO ---
    public abstract UserDao userDao();

    public abstract MapDao mapDao();

    public abstract BFTDao bFTDao();

    public abstract SitRepDao sitRepDao();

    public abstract TaskDao taskDao();

    public abstract VideoStreamDao videoStreamDao();

    public abstract WaveRelayRadioDao waveRelayRadioDao();

    public abstract UserSitRepJoinDao userSitRepDao();

    public abstract UserTaskJoinDao userTaskDao();

    // --- INSTANCE ---
    public static VentiloDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (VentiloDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VentiloDatabase.class, "VentiloDatabase.db")
                            .addCallback(prepopulateDatabase())
                            .build();

                    Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            INSTANCE.clearAllTables();
                            INSTANCE.populateDatabaseOnAppStart();
                        }
                    });
                }
            }
        }

        return INSTANCE;
    }

    private static Callback prepopulateDatabase() {
        return new Callback() {

            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
//                System.out.println("Database opened");
//                ContentValues firstUser = new ContentValues();
//                firstUser.put("userId", "888");
//                firstUser.put("password", "password1");
//                firstUser.put("accessToken", "");
//                firstUser.put("team", "Alpha");
//                firstUser.put("role", "Member");
//
//                ContentValues secondUser = new ContentValues();
//                secondUser.put("userId", "999");
//                secondUser.put("password", "password2");
//                secondUser.put("accessToken", "");
//                secondUser.put("team", "Alpha");
//                secondUser.put("role", "Member");
//
//                db.insert("User", OnConflictStrategy.REPLACE, firstUser);
//                db.insert("User", OnConflictStrategy.REPLACE, secondUser);

//                ContentValues contentValues = new ContentValues();
//                contentValues.put("id", 1);
//                contentValues.put("username", "Philippe");
//                contentValues.put("urlPicture", "https://oc-user.imgix.net/users/avatars/15175844164713_frame_523.jpg?auto=compress,format&q=80&h=100&dpr=2");
//
//                db.insert("User", OnConflictStrategy.IGNORE, contentValues);
            }
        };
    }

    private void populateDatabaseOnAppStart() {

        // Users
        UserModel userOne = createUserModel(USERNAME_ONE, USERNAME_ONE, StringUtil.EMPTY_STRING,
                "Alpha, Bravo", EAccessRight.TEAM_LEAD.toString());
        UserModel userTwo = createUserModel(USERNAME_TWO, USERNAME_TWO, StringUtil.EMPTY_STRING,
                "Alpha", EAccessRight.TEAM_LEAD.toString());
        UserModel userThree = createUserModel(USERNAME_THREE, USERNAME_THREE, StringUtil.EMPTY_STRING,
                "Alpha", EAccessRight.TEAM_LEAD.toString());
        UserModel userFour = createUserModel(USERNAME_FOUR, USERNAME_FOUR, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie, Delta, Echo, Foxtrot", EAccessRight.TEAM_LEAD.toString());
        UserModel userFive = createUserModel(USERNAME_FIVE, USERNAME_FIVE, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie, Delta, Echo, Foxtrot", EAccessRight.TEAM_LEAD.toString());
        UserModel userSix = createUserModel(USERNAME_SIX, USERNAME_SIX, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie", EAccessRight.TEAM_LEAD.toString());
        UserModel userSeven = createUserModel(USERNAME_SEVEN, USERNAME_SEVEN, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie", EAccessRight.TEAM_LEAD.toString());
        UserModel userEight = createUserModel(USERNAME_EIGHT, USERNAME_EIGHT, StringUtil.EMPTY_STRING,
                "Alpha, Bravo", EAccessRight.TEAM_LEAD.toString());
        UserModel userNine = createUserModel(CCT_ONE_USERNAME, CCT_ONE_USERNAME, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie, Delta, Echo, Foxtrot", EAccessRight.CCT.toString());
        UserModel userTen = createUserModel(CCT_TWO_USERNAME, CCT_TWO_USERNAME, StringUtil.EMPTY_STRING,
                "Alpha, Bravo, Charlie, Delta, Echo, Foxtrot", EAccessRight.CCT.toString());

        userDao().createUser(userOne);
        userDao().createUser(userTwo);
        userDao().createUser(userThree);
        userDao().createUser(userFour);
        userDao().createUser(userFive);
        userDao().createUser(userSix);
        userDao().createUser(userSeven);
        userDao().createUser(userEight);
        userDao().createUser(userNine);
        userDao().createUser(userTen);

        // User Radio/Phone IP Addresses
        String[] radioIpAddresses = MainApplication.getAppContext().getResources().
                getStringArray(R.array.login_radio_ip_addresses);
//        String[] phoneIpAddresses = MainApplication.getAppContext().getResources().
//                getStringArray(R.array.login_user_mobile_ip_addresses);

        // Wave Relay Radios
        WaveRelayRadioModel radioOne = createWaveRelayRadioModel(1, null,
                radioIpAddresses[0], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioTwo = createWaveRelayRadioModel(2, null,
                radioIpAddresses[1], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioThree = createWaveRelayRadioModel(3, null,
                radioIpAddresses[2], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioFour = createWaveRelayRadioModel(4, null,
                radioIpAddresses[3], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioFive = createWaveRelayRadioModel(5, null,
                radioIpAddresses[4], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSix = createWaveRelayRadioModel(6, null,
                radioIpAddresses[5], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSeven = createWaveRelayRadioModel(7, null,
                radioIpAddresses[6], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioEight = createWaveRelayRadioModel(8, null,
                radioIpAddresses[7], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioNine = createWaveRelayRadioModel(9, null,
                radioIpAddresses[8], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioTen = createWaveRelayRadioModel(10, null,
                radioIpAddresses[9], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSixtyFive = createWaveRelayRadioModel(65, null,
                radioIpAddresses[10], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSixtySix = createWaveRelayRadioModel(66, null,
                radioIpAddresses[11], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSixtySeven = createWaveRelayRadioModel(67, null,
                radioIpAddresses[12], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSixtyEight = createWaveRelayRadioModel(68, null,
                radioIpAddresses[13], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);
        WaveRelayRadioModel radioSixtyNine = createWaveRelayRadioModel(69, null,
                radioIpAddresses[14], StringUtil.INVALID_STRING, StringUtil.INVALID_STRING);

        SharedPreferenceUtil.setSharedPreference(SharedPreferenceConstants.USER_RADIO_LINK_STATUS,
                ERadioConnectionStatus.OFFLINE.toString());

        waveRelayRadioDao().insertWaveRelayRadioModel(radioOne);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioTwo);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioThree);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioFour);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioFive);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSix);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSeven);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioEight);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioNine);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioTen);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSixtyFive);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSixtySix);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSixtySeven);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSixtyEight);
        waveRelayRadioDao().insertWaveRelayRadioModel(radioSixtyNine);

        // Video Streams; Add 'Camera-Radio' label to radio number for radio name
        VideoStreamModel videoOne = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(EOwner.OWN.toString()),
                EOwner.OWN.toString());
        VideoStreamModel videoTwo = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("2"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoThree = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("3"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoFour = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("4"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoFive = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("5"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSix = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("6"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSeven = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("7"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoEight = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("8"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoNine = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("9"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoTen = createVideoStreamModel(MainApplication.getAppContext().getResources().
                getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("10"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSixtyFive = createVideoStreamModel(MainApplication.getAppContext().getResources().
                        getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("65"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSixtySix = createVideoStreamModel(MainApplication.getAppContext().getResources().
                        getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("66"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSixtySeven = createVideoStreamModel(MainApplication.getAppContext().getResources().
                        getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("67"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSixtyEight = createVideoStreamModel(MainApplication.getAppContext().getResources().
                        getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("68"),
                EOwner.OTHERS.toString());
        VideoStreamModel videoSixtyNine = createVideoStreamModel(MainApplication.getAppContext().getResources().
                        getString(R.string.video_stream_video_name_camera_radio).concat(StringUtil.SPACE).concat("69"),
                EOwner.OTHERS.toString());

        videoStreamDao().insertVideoStreamModel(videoOne);
        videoStreamDao().insertVideoStreamModel(videoTwo);
        videoStreamDao().insertVideoStreamModel(videoThree);
        videoStreamDao().insertVideoStreamModel(videoFour);
        videoStreamDao().insertVideoStreamModel(videoFive);
        videoStreamDao().insertVideoStreamModel(videoSix);
        videoStreamDao().insertVideoStreamModel(videoSeven);
        videoStreamDao().insertVideoStreamModel(videoEight);
        videoStreamDao().insertVideoStreamModel(videoNine);
        videoStreamDao().insertVideoStreamModel(videoTen);
        videoStreamDao().insertVideoStreamModel(videoSixtyFive);
        videoStreamDao().insertVideoStreamModel(videoSixtySix);
        videoStreamDao().insertVideoStreamModel(videoSixtySeven);
        videoStreamDao().insertVideoStreamModel(videoSixtyEight);
        videoStreamDao().insertVideoStreamModel(videoSixtyNine);

    }

    private static UserModel createUserModel(String userId, String password, String accessToken, String team,
                                             String role) {

        UserModel userModel = new UserModel(userId);
        userModel.setPassword(password);
        userModel.setAccessToken(accessToken);
        userModel.setTeam(team);
        userModel.setRole(role);
        userModel.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        userModel.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
        userModel.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
        userModel.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));

        return userModel;
    }

    private static WaveRelayRadioModel createWaveRelayRadioModel(int radioId, String userId,
                                                                 String radioIpAddress, String phoneIpAddress,
                                                                 String snr) {

        WaveRelayRadioModel waveRelayRadioModel = new WaveRelayRadioModel();
        waveRelayRadioModel.setRadioId(radioId);
        waveRelayRadioModel.setUserId(userId);
        waveRelayRadioModel.setRadioIpAddress(radioIpAddress);
        waveRelayRadioModel.setPhoneIpAddress(phoneIpAddress);
        waveRelayRadioModel.setSignalToNoiseRatio(snr);

        return waveRelayRadioModel;
    }

    private static VideoStreamModel createVideoStreamModel(String name, String owner) {

        VideoStreamModel videoStreamModel = new VideoStreamModel();
        videoStreamModel.setUserId(USERNAME_ONE);
        videoStreamModel.setName(name);
        videoStreamModel.setUrl(SharedPreferenceConstants.DEFAULT_STRING);
        videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);
        videoStreamModel.setOwner(owner);

        return videoStreamModel;
    }
}
