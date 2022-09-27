package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class activity_login extends AppCompatActivity implements View.OnClickListener {

    private TextView register_here;
    private TextView reset_password;
    private Intent intent;
    private AppGeneralActivities appGeneralActivities;
    private TextInputEditText login_email;
    private TextInputEditText login_password;
    private CheckBox checkBox_stayLoggedIn;
    private Button btn_login;
    private ProgressBar progressBar_login;
    private CloudActivities cloudActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // *** FOR DEBUG PURPOSE ***
        Log.d("uid activity_login", getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE));

        // region ActionBar
        getSupportActionBar().hide(); // hide action bar
        // endregion

        // region View's
        register_here = this.findViewById(R.id.register_here);
        register_here.setOnClickListener(this);

        login_email = this.findViewById(R.id.login_email);
        login_password = this.findViewById(R.id.login_password);

        btn_login = this.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);

        progressBar_login = this.findViewById(R.id.progressBar_login);

        reset_password = this.findViewById(R.id.reset_password);
        reset_password.setOnClickListener(this);

        checkBox_stayLoggedIn = this.findViewById(R.id.checkBox_stayLoggedIn);
        checkBox_stayLoggedIn.setOnClickListener(this);
        // endregion

    }

    @Override
    // local onClickListener handler
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.register_here:
                intent = new Intent(activity_login.this, activity_register_user.class);
//              because we redirect from the login page to the register activity
//              The created user type will be a "user"
                intent.putExtra(SharedPreferencesKeys.TYPE, UserTypeKeys.USER);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.reset_password:
                startActivity(new Intent(activity_login.this, activity_reset_password.class));
                finish();
                break;
        }
    }

    private void login() {

        String email = login_email.getText().toString().trim();
        String password = login_password.getText().toString().trim();
        boolean stayLoggedIn = checkBox_stayLoggedIn.isChecked();

        // email validation
        if (!appGeneralActivities.validateEmail(login_email))
            return;

        // password validation
        if (!appGeneralActivities.validatePassword(login_password))
            return;

        cloudActivities.login(activity_login.this, email, password, progressBar_login, stayLoggedIn);
    }

}


