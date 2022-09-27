package com.example.tourexpert;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class activity_add_flight extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Intent intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;

    // inputs from the user:
    private TextInputEditText flight_source, flight_destination, flight_date, ticket_price, available_amount, flight_class;
    private ProgressBar progressBar_add_flight;
    private Button btn_add_flight;

    // LocalDateTime parameters:
    private int savedYear = 0, savedMonth = 0, savedDay = 0, savedHour = 0, savedMinutes = 0;

    private DatePickerDialog datePickerDialog;
    private DatePicker datePicker;
    private TimePickerDialog timePickerDialog;

    private LocalDateTime dateOfFlight; // representing the flight date
    private DateTimeFormatter dateTimeFormatter; // formatting the date for the employee by: "dd/MM/yyyy 'at' hh:mm am/pm"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region VIEWS:
        flight_source = this.findViewById(R.id.flight_source);
        flight_destination = this.findViewById(R.id.flight_destination);
        ticket_price = this.findViewById(R.id.ticket_price);
        available_amount = this.findViewById(R.id.available_amount);
        flight_class = this.findViewById(R.id.flight_class);

        progressBar_add_flight = this.findViewById(R.id.progressBar_add_attraction);

        flight_date = this.findViewById(R.id.flight_date);
        flight_date.setInputType(InputType.TYPE_NULL);
        flight_date.setTextIsSelectable(false);
        flight_date.setOnClickListener(this);

        btn_add_flight = this.findViewById(R.id.btn_add_flight);
        btn_add_flight.setOnClickListener(this);
        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle("add new flight");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion ActionBar

        // region Date:
        // initialize a DatePickerDialog View dialog:
        datePickerDialog = new DatePickerDialog(activity_add_flight.this, android.R.style.Theme_DeviceDefault_Dialog, this::onDateSet,
                0, 0, 0);

        // initialize a TimePickerDialog View dialog:
        timePickerDialog = new TimePickerDialog(activity_add_flight.this, this::onTimeSet,
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

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_flights and the user type is employee
        intent = new Intent(activity_add_flight.this, activity_flights.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flight_date:
                // the employee tries to choose a date and time for the flight departure:
                datePickerDialog.show();
                break;
            case R.id.btn_add_flight:
                appGeneralActivities.click(this);
                addFlight();
                break;
        }
    }

    private void addFlight() {
        String flightSource = flight_source.getText().toString().trim().toLowerCase();
        String flightDestination = flight_destination.getText().toString().trim().toLowerCase();
        String flightClass = flight_class.getText().toString().trim();
        int ticketPrice = 0;
        int availableAmount = 0;

        // region flight_source validation:
        if (!appGeneralActivities.validateName(flight_source, "source credentials", 4)) {
            return;
        }
        // endregion flight_source validation

        // region flight_destination validation:
        if (!appGeneralActivities.validateName(flight_destination, "destination credentials", 4)) {
            return;
        }
        // endregion flight_destination validation

        // region flight_class validation:
        if (!appGeneralActivities.validateName(flight_class, "flight class", 2)) {
            return;
        }
        // endregion flight_class validation

        // region date and time validations:
        // employee didn't choose date
        if (dateOfFlight == null) {
            flight_date.setError("please choose the date");
            flight_date.requestFocus();
            return;
        }

        // if the Employee chosen date is today, and the time is behind the current time, show an error and rollback the process
        if (dateOfFlight.isBefore(LocalDateTime.ofInstant(Calendar.getInstance().toInstant(), ZoneId.systemDefault()))) {
            flight_date.setError("invalid hour and minutes, please pick valid time");
            flight_date.setText("");
            flight_date.requestFocus();
            return;
        } else {
            // no error, this time the employee choose a valid hour and minutes:
            flight_date.setError(null);
        }

        // convert the date to long value and save it, so we can used it when we add a flight object:
        ZonedDateTime zonedDateTime = ZonedDateTime.of(dateOfFlight, ZoneId.systemDefault());
        long dateOfFlight_long = zonedDateTime.toInstant().toEpochMilli();
        // *** FOR DEBUG ***
        Log.d("test : dateOfFlight_long:", dateOfFlight_long + "");

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
        // endregion validate available_amount
        Flight flight = new Flight(null, ticketPrice, availableAmount, flightSource, flightDestination, dateOfFlight_long, flightClass);

        cloudActivities.addProduct(activity_add_flight.this, flight, progressBar_add_flight);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        // employee chooses the year, month and day of the flight:
        this.savedYear = year;
        this.savedMonth = month + 1; // months in DatePicker are received as: 0-11 so we +1
        this.savedDay = dayOfMonth;

        // employee done choosing date, show the time picker dialog:
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // employee chooses the hour and minute of the flight:
        this.savedHour = hourOfDay; // 0-23
        this.savedMinutes = minute;

        // initialize the dateOfFlight:
        dateOfFlight = LocalDateTime.of(savedYear, savedMonth, savedDay, savedHour, savedMinutes);

        // show the employee the date he chosen (human readable):
        String dateOfFlight_to_readAble = dateOfFlight.format(dateTimeFormatter);
        flight_date.setText(dateOfFlight_to_readAble);
    }
}