package studio.bachelor.muninn;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

/**
 * Created by BACHELOR on 2016/03/14.
 */
public class SettingActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.setting, target);
    }

    @Override
    protected boolean isValidFragment(String fragment_name) {
        boolean is_valid =
                ColorSettings.class.getName().equals(fragment_name) |
                        SizeSettings.class.getName().equals(fragment_name) |
                        ServerSettings.class.getName().equals(fragment_name);
        return is_valid;
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class ColorSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.color);
        }
    }

    public static class SizeSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.size);
        }
    }

    public static class ServerSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.server);
        }
    }
}
