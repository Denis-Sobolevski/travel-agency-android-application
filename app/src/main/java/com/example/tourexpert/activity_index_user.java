package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class activity_index_user extends AppCompatActivity implements View.OnClickListener {

    private CardView logout, profile, browse_locations, purchase_history;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private Intent intent;
    private TextView greet_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index_user);

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


        browse_locations = this.findViewById(R.id.browse_locations);
        browse_locations.setOnClickListener(this);

        purchase_history = this.findViewById(R.id.purchase_history);
        purchase_history.setOnClickListener(this);

        greet_user = this.findViewById(R.id.greet_user);
        // endregion View's

        // we want to greet the user by welcome:
        cloudActivities.greetUser(activity_index_user.this, greet_user);
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.logout:
                cloudActivities.logout(activity_index_user.this);
                break;
            case R.id.profile:
                intent = new Intent(activity_index_user.this, activity_user_profile.class);
                // because we go from the user index page to the profile page
                // we will send the current user type
                // so we can display default user avatar the next page:
                intent.putExtra(SharedPreferencesKeys.TYPE, UserTypeKeys.USER);
                startActivity(intent);
                finish();
                break;
            case R.id.browse_locations:
                intent = new Intent(activity_index_user.this, activity_cities.class);
                startActivity(intent);
                finish();
                break;
            case R.id.purchase_history:
                intent = new Intent(activity_index_user.this, activity_purchase_history.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}