package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class activity_splash extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView quote;
    private TextView quote_sayer;
    private SharedPreferences shared;
    private CloudActivities cloudActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        cloudActivities = new CloudActivities();
        getSupportActionBar().hide(); // hide action bar
        mediaPlayer = MediaPlayer.create(activity_splash.this, R.raw.startup_sound);
        quote = this.findViewById(R.id.quote);
        quote_sayer = this.findViewById(R.id.quote_sayer);
        choose_random_quote();
        shared = getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);

        // if a user that was previously logged in but he didn't check the "stay logged in"
        // checkbox and when exiting the application he didn't logout,
        // we don't want to keep his details (uid. type, email. password) in sharedPreferences
        if( ! getSharedPreferences(SharedPreferencesKeys.GENERAL,Context.MODE_PRIVATE).getBoolean(SharedPreferencesKeys.LOGGED_IN, false))
            getSharedPreferences(SharedPreferencesKeys.GENERAL,Context.MODE_PRIVATE).edit().clear().apply();

        // *** FOR DEBUG PURPOSE ***
        Log.d("uid activity_splash", getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE));

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mediaPlayer.start();
                    sleep(2000);
                } catch (Exception e) {

                } finally {
                    // determine whether the user has
                    // logged in before with the "stay logged in" option checked
                    // attempt to re-login and direct him to the appropriate index
                    // otherwise redirect him to the login activity:
                    boolean isLoggedIn = shared.getBoolean(SharedPreferencesKeys.LOGGED_IN, false);
                    mediaPlayer.release();

                    if (isLoggedIn) {
                        cloudActivities.attemptRelogin(activity_splash.this);
                    } else {
                        startActivity(new Intent(new Intent(activity_splash.this, activity_login.class)));
                        finish();
                    }
                }
            }
        };
        thread.start();
        if (thread.isInterrupted()) {
            startActivity(new Intent(activity_splash.this, activity_login.class));
            finish();
        }
    }

    /**
     * Function will choose a random quote from the quote's array in res/values/strings.xml
     * and will display the quote and the quote sayer in the appropriate views
     */
    public void choose_random_quote() {
        final String[] quotes = getResources().getStringArray(R.array.quotes);
        Random rnd = new Random();
        String full_quote = quotes[rnd.nextInt(quotes.length)];
        quote.setText(full_quote.substring(0, full_quote.indexOf('-')));
        quote_sayer.setText(full_quote.substring(full_quote.indexOf('-')));
    }


}