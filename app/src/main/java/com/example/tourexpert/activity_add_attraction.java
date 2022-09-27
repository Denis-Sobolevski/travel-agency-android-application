package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class activity_add_attraction extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Intent intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;

    // inputs from user:
    private TextInputEditText attraction_type, attraction_date,
            ticket_price, available_amount, attraction_description;
    private ProgressBar progressBar_add_attraction;
    private Button btn_add_attraction;

    // LocalDateTime parameters:
    private int savedYear = 0, savedMonth = 0, savedDay = 0, savedHour = 0, savedMinutes = 0;

    private DatePickerDialog datePickerDialog;
    private DatePicker datePicker;
    private TimePickerDialog timePickerDialog;

    private LocalDateTime dateOfAttraction; // representing the attraction date
    private DateTimeFormatter dateTimeFormatter; // formatting the date for the employee by: "dd/MM/yyyy 'at' hh:mm am/pm"

    public Uri imageUri;
    private ImageView attraction_image;

    private static final String ACTIONBAR_TITLE = "add new attraction";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_attraction);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region VIEWS:
        attraction_type = this.findViewById(R.id.attraction_type);
        attraction_date = this.findViewById(R.id.attraction_date);
        ticket_price = this.findViewById(R.id.ticket_price);
        available_amount = this.findViewById(R.id.available_amount);
        attraction_description = this.findViewById(R.id.attraction_description);

        progressBar_add_attraction = this.findViewById(R.id.progressBar_add_attraction);

        attraction_date = this.findViewById(R.id.attraction_date);
        attraction_date.setInputType(InputType.TYPE_NULL);
        attraction_date.setTextIsSelectable(false);
        attraction_date.setOnClickListener(this);

        attraction_image = this.findViewById(R.id.attraction_image);
        attraction_image.setOnClickListener(this);

        btn_add_attraction = this.findViewById(R.id.btn_add_attraction);
        btn_add_attraction.setOnClickListener(this);
        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle(this.ACTIONBAR_TITLE);
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion ActionBar

        // region Date:
        // initialize a DatePickerDialog View dialog:
        datePickerDialog = new DatePickerDialog(activity_add_attraction.this, android.R.style.Theme_DeviceDefault_Dialog, this::onDateSet,
                0, 0, 0);

        // initialize a TimePickerDialog View dialog:
        timePickerDialog = new TimePickerDialog(activity_add_attraction.this, this::onTimeSet,
                0, 0, true);

        // get the DatePicker View and set its minimum possible date to today:
        datePicker = datePickerDialog.getDatePicker();
        Calendar currentTime = Calendar.getInstance();
        datePicker.setMinDate(currentTime.getTimeInMillis()); // get the current time, convert it to long (current time in milli seconds)

        // get the TimePicker and set its minimum possible hour and minutes:
        timePickerDialog.updateTime(currentTime.get(Calendar.HOUR), currentTime.get(Calendar.MINUTE));

        // initialize the date formatter, will be used to display the date to the employee by "dd/MM/yyyy 'at' hh:mm am/pm"
        dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' hh:mm a");

        // endregion Date
    }

    private void chooseAttractionPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the user chose a picture
        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            attraction_image.setImageURI(imageUri);
        }
    }

    private void addAttraction() {
        String type = attraction_type.getText().toString().trim().toLowerCase();
        int ticketPrice = 0;
        int availableAmount = 0;
        String description = attraction_description.getText().toString().trim().toLowerCase();

        // region type validation:
        if (!appGeneralActivities.validateName(attraction_type, "type of attraction", 2)) {
            return;
        }
        // endregion type validation

        // region date and time validations:
        // employee didn't choose date
        if (dateOfAttraction == null) {
            attraction_date.setError("please choose the date");
            attraction_date.requestFocus();
            return;
        }

        // if the Employee chosen date is today, and the time is behind the current time, show an error and rollback the process
        if (dateOfAttraction.isBefore(LocalDateTime.ofInstant(Calendar.getInstance().toInstant(), ZoneId.systemDefault()))) {
            attraction_date.setError("invalid hour and minutes, please pick valid time");
            attraction_date.setText("");
            attraction_date.requestFocus();
            return;
        } else {
            // no error, this time the employee choose a valid hour and minutes:
            attraction_date.setError(null);
        }

        // convert the date to long value and save it, so we can used it when we add a Attraction object:
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateOfAttraction, ZoneId.systemDefault());
        long dateOfAttraction_long = zonedDateTime.toInstant().toEpochMilli();

        // endregion date and time validation

        // region validate ticket_price:
        try {
            ticketPrice = Integer.parseInt(ticket_price.getText().toString().trim().toLowerCase());

            if (ticketPrice < 1)
                throw new Exception();
        } catch (Exception e) {
            ticket_price.setError("please enter a positive price for a ticket");
            ticket_price.requestFocus();
            return;
        }
        // endregion validate ticket_price

        // region validate available_amount:
        try {
            availableAmount = Integer.parseInt(available_amount.getText().toString().trim().toLowerCase());

            if (availableAmount < 1)
                throw new Exception();
        } catch (Exception e) {
            available_amount.setError("please enter a valid amount of tickets available");
            available_amount.requestFocus();
            return;
        }

        // check if an image was chosen:
        if(imageUri == null) {
            appGeneralActivities.displayAlertDialog(this, "image not found", "please choose an image for this hotel", R.drawable.warning);
            return;
        }

        // endregion validate available_amount
        Attraction attraction = new Attraction(null, ticketPrice, availableAmount, type, dateOfAttraction_long, description, null);

        cloudActivities.addProduct(activity_add_attraction.this, attraction, progressBar_add_attraction);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // employee chooses the year, month and day of the attraction:
        this.savedYear = year;
        this.savedMonth = month + 1; // months in DatePicker are received as: 0-11 so we +1
        this.savedDay = dayOfMonth;

        // employee done choosing date, show the time picker dialog:
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // employee chooses the hour and minute of the attraction:
        this.savedHour = hourOfDay; // 0-23
        this.savedMinutes = minute;

        // initialize the dateOfAttraction:
        dateOfAttraction = LocalDateTime.of(savedYear, savedMonth, savedDay, savedHour, savedMinutes);

        // show the employee the date he chosen (human readable):
        String dateOfAttraction_to_readAble = dateOfAttraction.format(dateTimeFormatter);
        attraction_date.setText(dateOfAttraction_to_readAble);
    }


    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_attractions and the user type is employee
        intent = new Intent(activity_add_attraction.this, activity_attractions.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch(v.getId()) {
            case R.id.attraction_date:
                // the employee tries to choose a date and time for the attraction:
                datePickerDialog.show();
                break;
            case R.id.btn_add_attraction:
                // employee tries to add an attraction
                addAttraction();
                break;
            case R.id.attraction_image:
                chooseAttractionPicture();
                break;
        }
    }
}