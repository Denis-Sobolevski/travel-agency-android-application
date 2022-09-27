package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class activity_categories extends AppCompatActivity implements View.OnClickListener {

    AppGeneralActivities appGeneralActivities;
    Intent intent;
    SharedPreferences shared;
    SharedPreferences.Editor editor;

    LinearLayout category_attractions, category_flights, category_hotels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        appGeneralActivities = new AppGeneralActivities();

        // region SharedPreferencesKeys
        shared = getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
        editor = shared.edit();
        // endregion SharedPreferencesKeys

        // region Views
        category_attractions = this.findViewById(R.id.category_attractions);
        category_attractions.setOnClickListener(this);

        category_flights = this.findViewById(R.id.category_flights);
        category_flights.setOnClickListener(this);

        category_hotels = this.findViewById(R.id.category_hotels);
        category_hotels.setOnClickListener(this);

        // endregion Views

        // region ActionBar
        getSupportActionBar().setTitle(shared.getString(SharedPreferencesKeys.CITY_NAME, SharedPreferencesKeys.DEFAULT_VALUE) + " Categories");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion ActionBar

    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_login
        intent = new Intent(activity_categories.this, activity_cities.class);

        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        // user / employee choose a category:
        switch (v.getId()) {
            case R.id.category_flights:
                // user / employee choose to go to flights:
                intent = new Intent(activity_categories.this, activity_flights.class);
                editor.putString(SharedPreferencesKeys.CATEGORY, DataBaseCollectionsKeys.FLIGHTS);
                editor.apply();
                startActivity(intent);
                finish();
                break;
            case R.id.category_attractions:
                // user / employee choose to go to attractions:
                intent = new Intent(activity_categories.this, activity_attractions.class);
                editor.putString(SharedPreferencesKeys.CATEGORY, DataBaseCollectionsKeys.ATTRACTIONS);
                editor.apply();
                startActivity(intent);
                finish();
                break;
            case R.id.category_hotels:
                // user / employee choose to go to hotels:
                intent = new Intent(activity_categories.this, activity_hotels.class);
                editor.putString(SharedPreferencesKeys.CATEGORY, DataBaseCollectionsKeys.HOTELS);
                editor.apply();
                startActivity(intent);
                finish();
                break;
        }

    }
}