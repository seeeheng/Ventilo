package sg.gov.dsta.mobileC3.ventilo.activity.map;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.greenrobot.eventbus.EventBus;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.PageEvent;

public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("SettingsActivity onBackPressed");
        // Notify mapfragment of previous activity (SettingsActivity) that it was switched from
        EventBus.getDefault().post(PageEvent.getInstance().addPage(PageEvent.ACTIVITY_KEY, SettingsActivity.class.getSimpleName()));

        killmyself();
    }

    private void killmyself() {
        this.finishAndRemoveTask();
    }
}
