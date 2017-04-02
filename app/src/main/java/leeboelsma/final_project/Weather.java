package leeboelsma.final_project;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class Weather extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the description of your menu items in the menu
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
