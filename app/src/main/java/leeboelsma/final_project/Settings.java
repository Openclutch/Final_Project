package leeboelsma.final_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.CheckBox;

/**
 * Created by lee on 2017-04-20.
 */

public class Settings extends PreferenceActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    public void onStop(){
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        //debug = (CheckBox)findViewById(R.id.debugMode);
        editor.putBoolean("debug", true);
        editor.commit();
    }
}
