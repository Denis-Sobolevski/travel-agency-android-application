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
import android.util.Log;
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

public class activity_hotels extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private Intent paypal_intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private SharedPreferences shared_general;
    private SharedPreferences shared_userChoice;
    private HotelAdapter hotelAdapter;

    // region custom ActionBar
    private ViewGroup activity_hotels_action_bar;
    private ImageView go_back;
    private SearchView search_bar;

    private RadioButton search_by_star_rating;
    private RadioButton search_by_price;

    private FloatingActionButton add_new_hotel;
    private String fieldToSearchBy;
    // endregion custom ActionBar

    private String user_type;

    private RecyclerView hotel_recyclerView;
    private static FirebaseRecyclerOptions<Hotel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);

        cloudActivities = new CloudActivities();
        appGeneralActivities = new AppGeneralActivities();
        shared_general = getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        shared_userChoice = getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        // region PAYPAL SERVICE INTENT
        paypal_intent = new Intent(activity_hotels.this, PayPalService.class);
        paypal_intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalClientIDConfigClass.paypalConfig);
        startService(paypal_intent);

        // endregion PAYPAL SERVICE INTENT

        // save the current activity in a utility class so we can access
        // it on the hotelsAdapter:
        SavedActivity.setActivity(activity_hotels.this);

        //region SharedPreferencesKeys
        // the user type maybe: user or employee
        user_type = shared_general.getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);
        //endregion SharedPreferencesKeys

        // region RadioButtons
        search_by_star_rating = this.findViewById(R.id.search_by_star_rating);
        search_by_star_rating.setOnClickListener(this);

        search_by_price = this.findViewById(R.id.search_by_price);
        search_by_price.setOnClickListener(this);
        // endregion RadioButtons

        // region RecyclerView:
        hotel_recyclerView = this.findViewById(R.id.hotel_recyclerView);
        hotel_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = cloudActivities.optionsQueryToDisplayAllHotels(activity_hotels.this);
        // if options was null, it means we have connectivity issues (internet or database), we cannot
        // use options that is null
        if (options != null) {
            hotelAdapter = new HotelAdapter(options);
            hotelAdapter.startListening();
            hotel_recyclerView.setAdapter(hotelAdapter);
        }

        // endregion RecyclerView:

        // region ActionBar
        // hide the default ActionBar and initialize our custom ActionBar with searchView
        getSupportActionBar().hide();
        activity_hotels_action_bar = this.findViewById(R.id.hotels_action_bar);

        go_back = activity_hotels_action_bar.findViewById(R.id.go_back);
        go_back.setOnClickListener(this);

        search_bar = activity_hotels_action_bar.findViewById(R.id.search_bar);
        search_bar.setIconifiedByDefault(false);
        // endregion ActionBar

        // region FloatingActionButton
        // only if the user type is employee, make this button visible
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            add_new_hotel = this.findViewById(R.id.add_new_hotel);
            add_new_hotel.setVisibility(View.VISIBLE);
            add_new_hotel.setOnClickListener(this);
        }
        // endregion FloatingActionButton

        // set the default search option by star rating:
        fieldToSearchBy = "stars";
        search_bar.setInputType(InputType.TYPE_CLASS_NUMBER);
        // set the query:
        search_bar_query();
    }

    // region PAYPAL PAYMENT

    public static Hotel hotel = null;
    private static int amount = 0;
    private static int days = 0;
    public static String arrivalDate = null;
    public static String departureDate = null;

    public void hotelPurchaseMethod(String arriveDate, String departDate,int daysOfRent, int amountOfRooms, Hotel model) {
        hotel = model;
        amount = amountOfRooms;
        days = daysOfRent;
        arrivalDate = arriveDate;
        departureDate = departDate;

        double price = amountOfRooms * days * hotel.getPrice();

        // initialize the paypal intent:
        PayPalPayment payment = new PayPalPayment(new BigDecimal(price), "USD",
                amountOfRooms + " rooms for hotel: " + model.getName()+"\n"+"type: " + model.getType() , PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(activity_hotels.this, PaymentActivity.class);
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

                // 1. update the hotel room's available amount
                String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                String category = DataBaseCollectionsKeys.HOTELS;
                String productKey = hotel.getKey();
                // hotel / flight / attraction - all the Products have in common is availableAmount
                cloudActivities.updateProductAmount(activity_hotels.this, cityKey, category, productKey, amount);

                // 2. add a statistic's object to the city, later used for statistical reports:
                double price = amount * days * hotel.getPrice();
                long time_now = Calendar.getInstance().getTimeInMillis();
                Statistic statistic = new Statistic(DataBaseCollectionsKeys.HOTELS, time_now, amount, price);
                cloudActivities.appendStatistic(activity_hotels.this, statistic, cityKey);

                // 3. add a purchase history for a user:
                HotelPurchase hotelPurchase = (HotelPurchase) cloudActivities.addPurchaseHistoryToUser(activity_hotels.this, time_now,
                        amount, price, hotel);
                Log.d("hotelPurchase: ", hotelPurchase.toString());

                // 4. make a pdf reception for the user:
                try {
                    PDFMaker pdfMaker = new PDFMaker();
                    pdfMaker.createPDFreceipt(activity_hotels.this, hotelPurchase);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                // purchase was not successful:
                appGeneralActivities.displayAlertDialog(activity_hotels.this, "payment failed",
                        "something went wrong, if you believe you've been charged, please contact customer support", R.drawable.warning);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // stop the paypal service on application closure:
        stopService(new Intent(activity_hotels.this, PayPalService.class));
        super.onDestroy();
    }

    // endregion PAYPAL PAYMENT

    private void search_bar_query() {
        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // DO NOTHING
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // case insensitive search:
                if(newText.isEmpty()) {
                    options = cloudActivities.optionsQueryToDisplayAllHotels(activity_hotels.this);
                } else {
                    options = cloudActivities.displayHotelsByQuery(activity_hotels.this, newText, fieldToSearchBy);
                }

                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    hotelAdapter = new HotelAdapter(options);
                    hotelAdapter.startListening();
                    hotel_recyclerView.setAdapter(hotelAdapter);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch(v.getId()) {
            case R.id.add_new_hotel:
                intent = new Intent(activity_hotels.this, activity_add_hotel.class);
                startActivity(intent);
                finish();
                break;
            case R.id.go_back:
                intent = new Intent(activity_hotels.this, activity_categories.class);
                startActivity(intent);
                finish();
                break;
            case R.id.search_by_star_rating:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_NUMBER);
                fieldToSearchBy = "stars";
                search_bar.setQueryHint("search by star rating");
                options = cloudActivities.displayHotelsByQuery(activity_hotels.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    hotelAdapter = new HotelAdapter(options);
                    hotelAdapter.startListening();
                    hotel_recyclerView.setAdapter(hotelAdapter);
                }
                break;
            case R.id.search_by_price:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_NUMBER);
                fieldToSearchBy = "price";
                search_bar.setQueryHint("search by maximum price");
                options = cloudActivities.displayHotelsByQuery(activity_hotels.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    hotelAdapter = new HotelAdapter(options);
                    hotelAdapter.startListening();
                    hotel_recyclerView.setAdapter(hotelAdapter);
                }
                break;
        }
    }


}