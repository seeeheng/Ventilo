package dsta.sg.com.ventilo;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.reactivex.Single;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TaskDaoTest {

    // FOR DATA
    private VentiloDatabase database;

    // DATA SET FOR TEST
    private static String USERNAME = "111";
    private static UserModel USER_DEMO = new UserModel(USERNAME, "", "", "Alpha", "Lead");
//    private static Single<UserModel> USER_DEMO_SINGLE = (Single<UserModel>) USER_DEMO;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void initDb() throws Exception {
        this.database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
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
        this.database.userDao().createUser(USER_DEMO);
        // TEST
        MutableLiveData<UserModel> userModel = new MutableLiveData<>();
        Single<UserModel> userModelSingle = this.database.userDao().getUserByUserId(USERNAME);
        userModel.setValue(userModelSingle.blockingGet());
        UserModel user = LiveDataTestUtil.getValue(userModel);
        assertTrue(user.getUserId().equals(USER_DEMO.getUserId()) && user.getUserId() == USERNAME);
    }
}
