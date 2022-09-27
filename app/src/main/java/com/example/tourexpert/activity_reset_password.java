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

public class activity_reset_password extends AppCompatActivity implements View.OnClickListener {

    private AppGeneralActivities appGeneralActivities;
    private Intent intent;
    private TextInputEditText reset_email;
    private ProgressBar progressBar_reset;
    private Button btn_send_reset_password;
    private CloudActivities cloudActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region View's
        reset_email = this.findViewById(R.id.reset_email);
        progressBar_reset = this.findViewById(R.id.progressBar_reset);

        btn_send_reset_password = this.findViewById(R.id.btn_send_reset_password);
        btn_send_reset_password.setOnClickListener(this);
        // endregion

        // region ActionBar
        getSupportActionBar().setTitle("password reset");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_login
        intent = new Intent(activity_reset_password.this, activity_login.class);

        startActivity(intent);
        finish();
        return true;
    }

    @Override
    // local onClickListener handler
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.btn_send_reset_password:
                sendResetPasswordEmailRequest();
                break;
        }
    }

    private void sendResetPasswordEmailRequest() {
        String email = reset_email.getText().toString().trim();

        // email validation
        if (!appGeneralActivities.validateEmail(reset_email))
            return;

        cloudActivities.sendResetPasswordEmailRequest(activity_reset_password.this, email, progressBar_reset);
    }

}