package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;

public class activity_add_city extends AppCompatActivity implements View.OnClickListener {

    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private Intent intent;

    private TextInputEditText city_name,
            country_name;

    private ProgressBar progressBar_add_city;
    private Button btn_add_city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region Views
        city_name = this.findViewById(R.id.city_name);
        country_name = this.findViewById(R.id.country_name);
        progressBar_add_city = this.findViewById(R.id.progressBar_add_city);

        btn_add_city = this.findViewById(R.id.btn_add_city);
        btn_add_city.setOnClickListener(this);
        // endregion Views

        // region ActionBar
        getSupportActionBar().setTitle("add new city");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_cities and the user type is employee
        intent = new Intent(activity_add_city.this, activity_cities.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.btn_add_city:
                // the employee tries to add a city:
                addCity();
                break;
        }
    }

    private void addCity() {
        String cityName = city_name.getText().toString().trim().toLowerCase();
        String countryName = country_name.getText().toString().trim().toLowerCase();

        // cityName validation:
        // shortest city name is 1 character, yup i was surprised to
        if (!appGeneralActivities.validateName(city_name, "city name", 1))
            return;

        // countryName validation:
        // shortest country name is 4 characters
        if (!appGeneralActivities.validateName(country_name, "country name", 4))
            return;

        cloudActivities.addCity(activity_add_city.this, cityName, countryName, progressBar_add_city);
    }
}