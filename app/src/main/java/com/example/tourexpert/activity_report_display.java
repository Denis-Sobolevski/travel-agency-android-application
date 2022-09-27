package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.anychart.AnyChartView;

public class activity_report_display extends AppCompatActivity {

    AppGeneralActivities appGeneralActivities;
    Intent intent;
    private int report_choice;

    AnyChartView graph;
    ProgressBar progressBar_loading_graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_display);

        appGeneralActivities = new AppGeneralActivities();

        // region VIEWS
        graph = this.findViewById(R.id.graph);
        graph.setProgressBar(this.findViewById(R.id.progressBar_loading_graph));
        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle("report");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion

        report_choice = getIntent().getExtras().getInt("report_choice");

        load_graph();
    }

    public void load_graph() {
        switch(this.report_choice) {
            case 1: ReportMaker.generate_report_by_months(this.graph); break;
            case 2: ReportMaker.generate_report_by_percentage(this.graph); break;
        }

        graph.setVisibility(View.VISIBLE);
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_cities
        intent = new Intent(activity_report_display.this, activity_reports.class);
        startActivity(intent);
        finish();
        return true;
    }
}