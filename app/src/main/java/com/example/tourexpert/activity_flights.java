package com.example.tourexpert;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;

public class activity_flights extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private Intent paypal_intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private FlightAdapter flightAdapter;

    // region custom ActionBar
    private ViewGroup activity_flights_action_bar;
    private ImageView go_back;
    private SearchView search_bar;

    private RadioButton search_by_destination;
    private RadioButton search_by_price;

    private String fieldToSearchBy;
    // endregion custom ActionBar

    private FloatingActionButton add_new_flight;
    private String user_type;
    private SharedPreferences shared_general;
    private SharedPreferences shared_userChoice;

    private RecyclerView flight_recyclerView;
    private static FirebaseRecyclerOptions<Flight> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flights);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
        shared_general = getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        shared_userChoice = getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        // region PAYPAL SERVICE INTENT
        paypal_intent = new Intent(activity_flights.this, PayPalService.class);
        paypal_intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalClientIDConfigClass.paypalConfig);
        startService(paypal_intent);

        // endregion PAYPAL SERVICE INTENT

        // save the current activity in a utility class so we can access
        // it on the flightAdapter:
        SavedActivity.setActivity(activity_flights.this);

        //region SharedPreferencesKeys
        // the user type maybe: user or employee
        user_type = shared_general.getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);
        //endregion SharedPreferencesKeys

        // region RadioButtons
        search_by_destination = this.findViewById(R.id.search_by_destination);
        search_by_destination.setOnClickListener(this);

        search_by_price = this.findViewById(R.id.search_by_price);
        search_by_price.setOnClickListener(this);
        // endregion RadioButtons

        // region RecyclerView:
        flight_recyclerView = this.findViewById(R.id.flight_recyclerView);
        flight_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = cloudActivities.optionsQueryToDisplayAllFlights(activity_flights.this);
        // if options was null, it means we have connectivity issues (internet or database), we cannot
        // use options that is null
        if (options != null) {
            flightAdapter = new FlightAdapter(options);
            flightAdapter.startListening();
            flight_recyclerView.setAdapter(flightAdapter);
        }

        // endregion RecyclerView:

        //region ActionBar
        // hide the default ActionBar and initialize our custom ActionBar with searchView
        getSupportActionBar().hide();
        activity_flights_action_bar = this.findViewById(R.id.flights_action_bar);

        go_back = activity_flights_action_bar.findViewById(R.id.go_back);
        go_back.setOnClickListener(this);

        search_bar = activity_flights_action_bar.findViewById(R.id.search_bar);
        search_bar.setIconifiedByDefault(false);
        //endregion ActionBar

        // region FloatingActionButton
        // only if the user type is employee, make this button visible
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            add_new_flight = this.findViewById(R.id.add_new_flight);
            add_new_flight.setVisibility(View.VISIBLE);
            add_new_flight.setOnClickListener(this);
        }
        // endregion FloatingActionButton

        // set the default search option by destination:
        fieldToSearchBy = "destination";
        // set the query:
        search_bar_query();
    }

    // region PAYPAL PAYMENT

    // those static fields are used to identify which flight
    // and what amount was bought
    private static Flight flight = null;
    private static int amount = 0;

    public void flightPurchaseMethod(int amountOfTickets, Flight model) {
        flight = model;
        amount = amountOfTickets;

        // calculate the purchase price:
        double purchase_price = amountOfTickets * model.getPrice();

        // initialize the paypal intent:
        PayPalPayment payment = new PayPalPayment(new BigDecimal(purchase_price), "USD",
                amountOfTickets + " tickets for flight: " + model.getKey(), PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(activity_flights.this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalClientIDConfigClass.paypalConfig);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PayPalClientIDConfigClass.PAYPAL_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PayPalClientIDConfigClass.PAYPAL_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // purchase was successful
                // now do the following operations:

                // 1. update the flight available amount ( subtract the amount the user purchased )
                String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                String category = DataBaseCollectionsKeys.FLIGHTS;
                String productKey = flight.getKey();
                // hotel / flight / attraction - all have in common is availableAmount
                cloudActivities.updateProductAmount(activity_flights.this, cityKey, category, productKey, amount);

                // 2. add a statistic's object to the city, later used for statistical reports:
                double price = amount * flight.getPrice();
                long time_now = Calendar.getInstance().getTimeInMillis();
                Statistic statistic = new Statistic(DataBaseCollectionsKeys.FLIGHTS, time_now, amount, price);
                cloudActivities.appendStatistic(activity_flights.this, statistic, cityKey);

                // 3. add a purchase history for a user:
                FlightPurchase flightPurchase = (FlightPurchase) cloudActivities.addPurchaseHistoryToUser(activity_flights.this, time_now, amount, price, flight);

                // 4. make a pdf reception for the user:
                try {
                    PDFMaker pdfMaker = new PDFMaker();
                    pdfMaker.createPDFreceipt(activity_flights.this, flightPurchase);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // payment was successful, notify the user:
                appGeneralActivities.displayAlertDialog(activity_flights.this, "purchase successful",
                        "purchase is successful, your reception is at your documents folder, and has been added to your flight's purchase history", R.drawable.information);

            } else {
                // purchase was not successful:
                appGeneralActivities.displayAlertDialog(activity_flights.this, "payment failed",
                        "something went wrong, if you believe you've been charged, please contact customer support", R.drawable.warning);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // stop the paypal service on application closure:
        stopService(new Intent(activity_flights.this, PayPalService.class));
        super.onDestroy();
    }

    // endregion PAYPAL PAYMENT

    public void search_bar_query() {
        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // DO NOTHING
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // case-insensitive search:
                if(newText.isEmpty()) {
                    options = cloudActivities.optionsQueryToDisplayAllFlights(activity_flights.this);
                } else {
                    options = cloudActivities.displayFlightsByQuery(activity_flights.this, newText, fieldToSearchBy);
                }
                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    flightAdapter = new FlightAdapter(options);
                    flightAdapter.startListening();
                    flight_recyclerView.setAdapter(flightAdapter);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            // employee has chosen to add a flight
            case R.id.add_new_flight:
                intent = new Intent(activity_flights.this, activity_add_flight.class);
                startActivity(intent);
                finish();
                break;
            case R.id.go_back:
                intent = new Intent(activity_flights.this, activity_categories.class);
                startActivity(intent);
                finish();
                break;

            case R.id.search_by_destination:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_TEXT);
                fieldToSearchBy = "destination";
                search_bar.setQueryHint("search by destination");
                options = cloudActivities.displayFlightsByQuery(activity_flights.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    flightAdapter = new FlightAdapter(options);
                    flightAdapter.startListening();
                    flight_recyclerView.setAdapter(flightAdapter);
                }
                break;

            case R.id.search_by_price:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_NUMBER);
                fieldToSearchBy = "price";
                search_bar.setQueryHint("search by maximum price");
                options = cloudActivities.displayFlightsByQuery(activity_flights.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    flightAdapter = new FlightAdapter(options);
                    flightAdapter.startListening();
                    flight_recyclerView.setAdapter(flightAdapter);
                }
                break;
        }
    }

}