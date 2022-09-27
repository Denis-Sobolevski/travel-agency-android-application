package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import java.time.LocalDate;
import java.util.ArrayList;

public class activity_reports extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;
    SharedPreferences userChoice;
    String city_key;
    private final int MIN_YEAR = 1900;

    private ArrayList<Statistic> data;

    private EditText year_of_data;
    private Button btn_acquire_data;
    private Button btn_generate_report;
    private View ProgressBar_fetch_data;
    private View reports_menu;

    private RadioButton statistics_by_month, statistics_by_percentage;

    private int report_choice = 1; // the default report choice
    private final int REPORT_BY_MONTHS = 1;
    private final int REPORT_BY_PERCENTAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
        userChoice = getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
        city_key = userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);

        // region VIEWS
        year_of_data = this.findViewById(R.id.year_of_data);

        btn_acquire_data = this.findViewById(R.id.btn_acquire_data);
        btn_acquire_data.setOnClickListener(this);

        btn_generate_report = this.findViewById(R.id.btn_generate_report);
        btn_generate_report.setOnClickListener(this);

        ProgressBar_fetch_data = this.findViewById(R.id.ProgressBar_fetch_data);
        reports_menu = this.findViewById(R.id.reports_menu);

        statistics_by_month = this.findViewById(R.id.statistics_by_month);
        statistics_by_month.setOnClickListener(this);

        statistics_by_percentage = this.findViewById(R.id.statistics_by_percentage);
        statistics_by_percentage.setOnClickListener(this);

        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle("acquire report");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_cities
        intent = new Intent(activity_reports.this, activity_cities.class);
        startActivity(intent);
        finish();
        return true;
    }

    /**
     * this function read the year from the user
     * and tries to fetch data corresponding to this year:
     */
    private void acquireData() {

        if (this.year_of_data.getText().toString().isEmpty()) {
            this.year_of_data.setError("enter a year");
            this.year_of_data.requestFocus();
            return;
        }

        int year = 0;
        int current_year = LocalDate.now().getYear();

        try {
            year = Integer.parseInt(this.year_of_data.getText().toString());

            if (year > current_year || year < this.MIN_YEAR)
                throw new Exception();

        } catch (Exception e) {
            this.year_of_data.setError("given year is invalid");
            this.year_of_data.requestFocus();
            return;
        }

        if (appGeneralActivities.isConnectedToInternet(this)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                ProgressBar_fetch_data.setVisibility(View.VISIBLE);

                cloudActivities.fetchStatisticData(year, DataBaseCollectionsKeys.STATISTICS + "/" + city_key, new DataFetchListener() {
                    @Override
                    public void onSuccessfulDataFetch(ArrayList<Statistic> statistics) {
                        ProgressBar_fetch_data.setVisibility(View.INVISIBLE);

                        // there is no data corresponding to year given by the Admin,
                        // notify the Admin and rollback
                        if(statistics.isEmpty()) {
                            reports_menu.setVisibility(View.GONE);
                            btn_generate_report.setVisibility(View.GONE);
                            appGeneralActivities.displayAlertDialog(activity_reports.this, "no statistical data was found", "there is no data associated with the given year, please try another year", R.drawable.warning);
                            return;
                        }

                        // send the data we fetched to the ReportMaker:
                        ReportMaker.data = statistics;
                        Log.d("my data: ", ReportMaker.data.toString());
                        reports_menu.setVisibility(View.VISIBLE);
                        btn_generate_report.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(String message) {
                        // data fetch failed to be executed, notify the user:
                        ProgressBar_fetch_data.setVisibility(View.INVISIBLE);
                        btn_generate_report.setVisibility(View.GONE);
                        appGeneralActivities.displayAlertDialog(activity_reports.this, "unexpected error", "data fetch failed because of the following exception:\n'" + message + "'", R.drawable.warning);
                    }
                });
            } else {
                // database is unavailable:
                appGeneralActivities.displayAlertDialog(this, "database unavailable", "database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no network was found:
            appGeneralActivities.displayAlertDialog(this, "no network connection", "no network connection was found, please try again later", R.drawable.error);
        }
    }

    /**
     *
     */
    private void display_report() {
        Intent intent = new Intent(activity_reports.this, activity_report_display.class);
        intent.putExtra("report_choice", this.report_choice);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.btn_acquire_data:
                this.acquireData();
                break;
            case R.id.btn_generate_report:
                display_report();
                break;
            case R.id.statistics_by_month:
                report_choice = this.REPORT_BY_MONTHS;
                break;
            case R.id.statistics_by_percentage:
                report_choice = this.REPORT_BY_PERCENTAGE;
                break;
        }
    }
}