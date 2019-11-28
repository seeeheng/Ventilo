package dsta.sg.com.ventilo;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {

    // FOR DATA
    private VentiloDatabase database;

    // DATA SET FOR TEST
    private static String USERNAME = "111";
//    private static UserModel USER_DEMO = new UserModel(USERNAME, "", "", "Alpha", "Lead");
    private static UserModel USER_DEMO = new UserModel(USERNAME);

//    private static Single<UserModel> USER_DEMO_SINGLE = (Single<UserModel>) USER_DEMO;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() throws Exception {
        this.database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().getContext(),
                VentiloDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        database.close();
    }

    @Test
    public void insertAndGetUser() throws InterruptedException {
        // BEFORE : Adding a new user
        USER_DEMO.setPassword(USERNAME);
        USER_DEMO.setAccessToken(StringUtil.EMPTY_STRING);
        USER_DEMO.setTeam("Alpha, Bravo");
        USER_DEMO.setRole(EAccessRight.TEAM_LEAD.toString());
        USER_DEMO.setPhoneToRadioConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        USER_DEMO.setRadioToNetworkConnectionStatus(ERadioConnectionStatus.DISCONNECTED.toString());
        USER_DEMO.setRadioFullConnectionStatus(ERadioConnectionStatus.OFFLINE.toString());
        USER_DEMO.setLastKnownConnectionDateTime(StringUtil.INVALID_STRING);
        USER_DEMO.setMissingHeartBeatCount(Integer.valueOf(StringUtil.INVALID_STRING));
        this.database.userDao().createUser(USER_DEMO);

        // TEST
        MutableLiveData<UserModel> userModel = new MutableLiveData<>();
        Single<UserModel> userModelSingle = this.database.userDao().getUserByUserId(USERNAME);
        userModel.setValue(userModelSingle.blockingGet());
        UserModel user = LiveDataTestUtil.getValue(userModel);
        assertTrue(user.getUserId().equals(USER_DEMO.getUserId()) && user.getUserId() == USERNAME);
    }
}
