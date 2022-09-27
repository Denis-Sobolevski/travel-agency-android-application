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

public class activity_register_user extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private String user_type;
    private TextInputEditText register_email,
            register_first_name,
            register_last_name,
            register_phone,
            register_password;
    private ProgressBar progressBar_register;
    private Button register_user;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        cloudActivities = new CloudActivities();
        appGeneralActivities = new AppGeneralActivities();

        // region Intent's
        intent = getIntent();
        // user_type can either be user or employee, depends from which activity we came through
        // user - if a user tries to register through the login activity -> register user activity
        // employee - if the admin tries to register a user through admin index -> register user activity
        user_type = intent.getExtras().getString(SharedPreferencesKeys.TYPE);
        // endregion

        // region ActionBar
        getSupportActionBar().setTitle(user_type + " registration");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion

        // region View's
        register_email = this.findViewById(R.id.register_email);
        register_first_name = this.findViewById(R.id.register_first_name);
        register_last_name = this.findViewById(R.id.register_last_name);
        register_phone = this.findViewById(R.id.register_phone);
        register_password = this.findViewById(R.id.register_password);
        register_user = this.findViewById(R.id.register_user);
        register_user.setOnClickListener(this);
        progressBar_register = this.findViewById(R.id.progressBar_register);

        // endregion

    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_login, a user tried to register himself:
        if (user_type.equals(UserTypeKeys.USER))
            intent = new Intent(activity_register_user.this, activity_login.class);
        // the previous activity was the index of the admin, he tried to register a new employee:
        if (user_type.equals(UserTypeKeys.EMPLOYEE))
            intent = new Intent(activity_register_user.this, activity_index_admin.class);

        startActivity(intent);
        finish();
        return true;
    }

    @Override
    // local onClickListener handler
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.register_user:
                // we call our method to register the user,
                // if this method is called from the admin's GUI
                // the user_type will be "admin"
                // else if this method is called from the register_here activity
                // the user_type wil be "user"
                register_user();
                break;
        }
    }

    private void register_user() {
        // get the user's input from the editText's
        String email = register_email.getText().toString().trim().toLowerCase();
        String firstName = register_first_name.getText().toString().trim().toLowerCase();
        String lastName = register_last_name.getText().toString().trim().toLowerCase();
        String phone = register_phone.getText().toString().trim();
        String password = register_password.getText().toString().trim();

        // region validate the user's input
        /* validate the user's input before registration attempt */

        // email validation
        if (!appGeneralActivities.validateEmail(register_email))
            return;

        // firstName validation
        if (!appGeneralActivities.validateName(register_first_name, "first name", 2))
            return;

        // lastName validation
        if (!appGeneralActivities.validateName(register_last_name, "last name", 2))
            return;

        // phone validation
        if (!appGeneralActivities.validatePhone(register_phone))
            return;

        // password validation
        if (!appGeneralActivities.validatePassword(register_password))
            return;
        // endregion

        // concat a country code to the phone, like +972 is for israel
        phone = appGeneralActivities.getCountryDialCode(activity_register_user.this) + "-" + phone;

        cloudActivities.registerUser(
                activity_register_user.this,
                email,
                firstName,
                lastName,
                phone,
                password,
                progressBar_register,
                user_type);
    }

}