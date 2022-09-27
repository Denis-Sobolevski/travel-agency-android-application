package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class activity_index_admin extends AppCompatActivity implements View.OnClickListener {

    private CardView logout, profile, register_employee, reports, manage_users;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private Intent intent;
    private TextView greet_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_admin);

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

        register_employee = this.findViewById(R.id.register_employee);
        register_employee.setOnClickListener(this);

        reports = this.findViewById(R.id.reports);
        reports.setOnClickListener(this);

        manage_users = this.findViewById(R.id.manage_users);
        manage_users.setOnClickListener(this);

        greet_user = this.findViewById(R.id.greet_user);
        // endregion

        // we want to greet the user by welcome:
        cloudActivities.greetUser(activity_index_admin.this, greet_user);


    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.logout:
                cloudActivities.logout(activity_index_admin.this);
                break;
            case R.id.register_employee:
                intent = new Intent(activity_index_admin.this, activity_register_user.class);
                // the admin is interested in creating an employee account
                intent.putExtra(SharedPreferencesKeys.TYPE, UserTypeKeys.EMPLOYEE);
                startActivity(intent);
                finish();
                break;
            case R.id.profile:
                intent = new Intent(activity_index_admin.this, activity_user_profile.class);
                // because we go from the admin index page to the profile page
                // we will send the current user type
                // so we can display default user avatar the next page:
                intent.putExtra(SharedPreferencesKeys.TYPE, UserTypeKeys.ADMIN);
                startActivity(intent);
                finish();
                break;
            case R.id.reports:
                intent = new Intent(activity_index_admin.this, activity_cities.class);
                startActivity(intent);
                finish();
                break;
            case R.id.manage_users:
                // redirect the admin to the manage users activity:
                startActivity(new Intent(activity_index_admin.this, activity_manage_users.class));
                finish();
                break;
        }
    }

}