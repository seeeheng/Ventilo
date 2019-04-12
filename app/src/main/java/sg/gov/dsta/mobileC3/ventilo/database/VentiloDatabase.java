package sg.gov.dsta.mobileC3.ventilo.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;

import sg.gov.dsta.mobileC3.ventilo.database.DAO.TaskDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.UserTaskJoinDao;
import sg.gov.dsta.mobileC3.ventilo.database.DAO.VideoStreamDao;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoin;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.repository.UserRepository;

@Database(entities = {UserModel.class, TaskModel.class, UserTaskJoin.class, VideoStreamModel.class},
        version = 1, exportSchema = false)
public abstract class VentiloDatabase extends RoomDatabase {

    // --- SINGLETON ---
    private static volatile VentiloDatabase INSTANCE;

    // --- DAO ---
    public abstract TaskDao taskDao();
    public abstract VideoStreamDao videoStreamDao();
    public abstract UserDao userDao();
    public abstract UserTaskJoinDao userTaskDao();

    // --- INSTANCE ---
    public static VentiloDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            System.out.println("VentiloDatabase getInstance");
            synchronized (VentiloDatabase.class) {
                if (INSTANCE == null) {
                    System.out.println("VentiloDatabase getInstance inner ");
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VentiloDatabase.class, "VentiloDatabase.db")
                            .addCallback(prepopulateDatabase())
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ---

    private static Callback prepopulateDatabase(){
        return new Callback() {

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
}
