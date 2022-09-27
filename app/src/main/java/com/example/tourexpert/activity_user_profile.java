package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class activity_user_profile extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private AppGeneralActivities appGeneralActivities;
    private String user_type;
    private CloudActivities cloudActivities;
    private ImageView user_portrait;
    private TextView name_for_display,
            email_for_display;
    private TextInputEditText first_name_for_update,
            last_name_for_update,
            phone_for_update;
    private Button update_user_details;
    private ProgressBar progressBar_update_user_details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region View's
        user_portrait = findViewById(R.id.user_portrait);
        name_for_display = findViewById(R.id.name_for_display);
        email_for_display = findViewById(R.id.email_for_display);
        first_name_for_update = findViewById(R.id.first_name_for_update);
        last_name_for_update = findViewById(R.id.last_name_for_update);
        phone_for_update = findViewById(R.id.phone_for_update);

        update_user_details = findViewById(R.id.update_user_details);
        update_user_details.setOnClickListener(this);

        progressBar_update_user_details = findViewById(R.id.progressBar_update_user_details);
        // endregion

        // region intent
        intent = getIntent();
        // we determine which user type has entered this current activity:
        user_type = intent.getExtras().getString(SharedPreferencesKeys.TYPE);
        // endregion intent

        // region ActionBar
        getSupportActionBar().setTitle(user_type + " profile");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion

        // insert the default portrait determined by the current user_type:
        insert_default_user_portrait();

        // insert the current user details onto the text fields:
        cloudActivities.displayUserDetails(
                activity_user_profile.this,
                name_for_display,
                email_for_display,
                first_name_for_update,
                last_name_for_update,
                phone_for_update
        );

        // the user might flip their phone to landscape
        // mode while having trouble with connectivity (internet or database)
        // we don't want the name_for_display and email_for_display
        // to reset and not show the user full name and email:
        if (savedInstanceState != null) {
            name_for_display.setText(savedInstanceState.getString("name_for_display"));
            email_for_display.setText(savedInstanceState.getString("email_for_display"));
        }

    }

    // used to restore the: name_for_display
    // and email_for_display TextViews after
    // the user flipped their phone and the user does
    // not have internet or database access:
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name_for_display", name_for_display.getText().toString());
        outState.putString("email_for_display", email_for_display.getText().toString());
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            // the user has chosen to update his details
            // with the ones that are currently displayed
            // at the EditTexts:
            case R.id.update_user_details:
                updateUserDetails();
                break;
        }
    }

    private void updateUserDetails() {
        // get the details from the user input:
        String firstName = first_name_for_update.getText().toString().trim().toLowerCase();
        String lastName = last_name_for_update.getText().toString().trim().toLowerCase();
        String phone = phone_for_update.getText().toString().trim();

        // firstName validation
        if (!appGeneralActivities.validateName(first_name_for_update, "first name", 2))
            return;

        // lastName validation
        if (!appGeneralActivities.validateName(last_name_for_update, "last name", 2))
            return;

        // phone validation
        if (!appGeneralActivities.validatePhone(phone_for_update))
            return;

        // concat a country code to the phone, like +972 is for israel
        phone = appGeneralActivities.getCountryDialCode(activity_user_profile.this) + "-" + phone;

        cloudActivities.updateUserDetails(
                activity_user_profile.this,
                firstName,
                lastName,
                phone,
                progressBar_update_user_details
        );
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    // ***** this method also respond to all the click listeners
    // even the regular buttons !!! *****
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // if the View that was clicked is not the
        // button of the update user details:
        if (item.getItemId() != R.id.update_user_details) {
            appGeneralActivities.click(this);
            // the current user is an admin:
            if (user_type.equals(UserTypeKeys.ADMIN))
                intent = new Intent(activity_user_profile.this, activity_index_admin.class);
            // the current user is an employee:
            if (user_type.equals(UserTypeKeys.EMPLOYEE))
                intent = new Intent(activity_user_profile.this, activity_index_employee.class);
            // the current user is an user:
            if (user_type.equals(UserTypeKeys.USER))
                intent = new Intent(activity_user_profile.this, activity_index_user.class);

            startActivity(intent);
            finish();
        }
        return true;
    }

    // will determine and insert the user avatar based on the current user type:
    private void insert_default_user_portrait() {
        int portrait = 0;
        switch (user_type) {
            case UserTypeKeys.ADMIN:
                portrait = R.drawable.profile_admin;
                break;
            case UserTypeKeys.EMPLOYEE:
                portrait = R.drawable.profile_employee;
                break;
            case UserTypeKeys.USER:
                portrait = R.drawable.profile_user;
                break;
        }
        user_portrait.setImageResource(portrait);
    }

}