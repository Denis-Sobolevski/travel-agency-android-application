package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class activity_index_employee extends AppCompatActivity implements View.OnClickListener {

    private CardView logout, profile, manage_products;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private Intent intent;
    private TextView greet_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_employee);

        cloudActivities = new CloudActivities();
        appGeneralActivities = new AppGeneralActivities();

        // region ActionBar
        getSupportActionBar().hide(); // hide action bar
        // endregion

        // region View's
        logout = this.findViewById(R.id.logout);
        logout.setOnClickListener(this);

        profile = this.findViewById(R.id.profile);
        profile.setOnClickListener(this);

        manage_products = this.findViewById(R.id.manage_products);
        manage_products.setOnClickListener(this);

        greet_user = this.findViewById(R.id.greet_user);
        // endregion View's

        // we want to greet the user by welcome:
        cloudActivities.greetUser(activity_index_employee.this, greet_user);
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.logout:
                cloudActivities.logout(activity_index_employee.this);
                break;
            case R.id.profile:
                intent = new Intent(activity_index_employee.this, activity_user_profile.class);
                // because we go from the employee index page to the profile page
                // we will send the current user type
                // so we can display default user avatar the next page:
                intent.putExtra(SharedPreferencesKeys.TYPE, UserTypeKeys.EMPLOYEE);
                startActivity(intent);
                finish();
                break;
            case R.id.manage_products:
                intent = new Intent(activity_index_employee.this, activity_cities.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}