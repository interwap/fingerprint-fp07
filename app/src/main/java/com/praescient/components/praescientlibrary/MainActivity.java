package com.praescient.components.praescientlibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.praescient.components.enrolment_fp07.EnrolFP07;
import com.praescient.components.fingerprint_fp07.Fp07;

public class MainActivity extends AppCompatActivity implements Fp07.MatchListener {

    private Context context = MainActivity.this;
    private android.app.FragmentManager fragmentManager;
    private Fp07 popup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getFragmentManager();


        final Display display = getWindowManager().getDefaultDisplay();

        final String template = "AwFmIY4A+f6ABoACgAKAAoACgAKAAoACgAKAAoACgAKAAoACgAKAAgAAAAAAAAAAAAAAAAAAAAA1\n" +
                "i9NeX4wQXmkQq94Wl1beNBlUPkQkUv5frOi+PDDT3iWxqt4ss6reM7iUfiM7Kv5KO5NeFLyV/j2+\n" +
                "6Z4bv2r+GYiX/z4KJ78fC5ZfPBHonysiVP86qamfZB5nPDujE3wypSocPy0T3GXD57xqHtH9K7xU\n" +
                "fWS7ElprvJDybEDROmc+53IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMBZiGMAP/+gAKAAoACgAKAAoACgAKAAoACgAKA\n" +
                "AoACgAKAAoACgAIAAAAAAAAAAAAAAAAAAAAATpQoficZVv5EmxP+bS+o3kA6FJ4vvCt+IL2WnlW9\n" +
                "095vvpL+KEBrPkjAqh4XC8H/b4yQNxsgrH86pBS/VSTS30mraf8xiNkUSyUTfEInKhxJstScM7Mr\n" +
                "PCkKmJ1Fi9KdTYvn/S+Nlp06NGtdOD2UvVacEzJSnSmyTS9UWhMwAtIYMRfTAAAAAAAAAAAAAAAA\n" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popup = Fp07.newInstance(context, "FingerprintFPO5 Biometrics");

                popup.setTextSize(20);
                popup.show(fragmentManager, "biometric_fragment");


                popup.match(template);



                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                       // .setAction("Action", null).show();

                //Toast.makeText(context, "Display: " + display.getWidth() + " " + display.getHeight(), Toast.LENGTH_LONG).show();
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void isMatch(boolean match) {



        if(match){
            popup.dismiss();
            Toast.makeText(context, "correct", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "incorrect", Toast.LENGTH_LONG).show();
        }
    }
}
