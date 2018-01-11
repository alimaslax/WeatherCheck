package edu.umb.cs410.weathercheck.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import edu.umb.cs410.weathercheck.R;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CompareActivity extends Activity {

    @Bind(R.id.apix) TextView apixLabel;
    @Bind(R.id.open) TextView openLabel;
    @Bind(R.id.yahoo) TextView yahooLabel;
    @Bind(R.id.dark) TextView darkLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare);

        ButterKnife.bind(this);


        Intent intent = getIntent();
        HashMap<String, Integer> temps = (HashMap<String, Integer>) intent.getSerializableExtra(MainActivity.COMPARE_TEMPS);
        try {
            darkLabel.setText(temps.get("dark").toString()+ "     Dark Sky");
            yahooLabel.setText(temps.get("yahoo").toString()+ "     Yahoo");
            apixLabel.setText(temps.get("apix").toString()+ "     Apix");
            openLabel.setText(temps.get("open").toString()+ "     Open");
        }
        catch(Exception e)
        {
            Toast.makeText(this,"Network Error",Toast.LENGTH_LONG);
        }



    }

}
