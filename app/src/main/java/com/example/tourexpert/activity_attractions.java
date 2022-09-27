package com.example.tourexpert;

import androidx.annotation.NonNull;
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
import android.view.MenuItem;
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

public class activity_attractions extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private Intent paypal_intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private SharedPreferences shared_userChoice;
    private SharedPreferences shared_general;
    private AttractionAdapter attractionAdapter;

    // region custom ActionBar
    private ViewGroup activity_attractions_action_bar;
    private ImageView go_back;
    private SearchView search_bar;

    private RadioButton search_by_type;
    private RadioButton search_by_price;

    private String fieldToSearchBy;
    // endregion custom ActionBar

    private FloatingActionButton add_new_attraction;
    private String user_type;

    private RecyclerView attraction_recyclerView;
    private static FirebaseRecyclerOptions<Attraction> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attractions);

        cloudActivities = new CloudActivities();
        appGeneralActivities = new AppGeneralActivities();
        shared_general = getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        shared_userChoice = getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        // region PAYPAL SERVICE INTENT
        paypal_intent = new Intent(activity_attractions.this, PayPalService.class);
        paypal_intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalClientIDConfigClass.paypalConfig);
        startService(paypal_intent);

        // endregion PAYPAL SERVICE INTENT

        // save the current activity in a utility class so we can access
        // it on the attractionAdapter:
        SavedActivity.setActivity(activity_attractions.this);

        //region SharedPreferencesKeys
        // the user type maybe: user or employee
        user_type = shared_general.getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);
        //endregion SharedPreferencesKeys

        // region RadioButtons
        search_by_type = this.findViewById(R.id.search_by_type);
        search_by_type.setOnClickListener(this);

        search_by_price = this.findViewById(R.id.search_by_price);
        search_by_price.setOnClickListener(this);
        // endregion RadioButtons

        // region RecyclerView:
        attraction_recyclerView = this.findViewById(R.id.attraction_recyclerView);
        attraction_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = cloudActivities.optionsQueryToDisplayAllAttractions(activity_attractions.this);
        // if options was null, it means we have connectivity issues (internet or database), we cannot
        // use options that is null
        if (options != null) {
            attractionAdapter = new AttractionAdapter(options);
            attractionAdapter.startListening();
            attraction_recyclerView.setAdapter(attractionAdapter);
        }

        // endregion RecyclerView:

        // region ActionBar
        // hide the default ActionBar and initialize our custom ActionBar with searchView
        getSupportActionBar().hide();
        activity_attractions_action_bar = this.findViewById(R.id.attractions_action_bar);

        go_back = activity_attractions_action_bar.findViewById(R.id.go_back);
        go_back.setOnClickListener(this);

        search_bar = activity_attractions_action_bar.findViewById(R.id.search_bar);
        search_bar.setIconifiedByDefault(false);
        // endregion ActionBar

        // region FloatingActionButton
        // only if the user type is employee, make this button visible
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            add_new_attraction = this.findViewById(R.id.add_new_attraction);
            add_new_attraction.setVisibility(View.VISIBLE);
            add_new_attraction.setOnClickListener(this);
        }
        // endregion FloatingActionButton

        // set the default search option by type:
        fieldToSearchBy = SharedPreferencesKeys.TYPE;
        // set the query:
        search_bar_query();
    }

    // region PAYPAL PAYMENT

    // those static fields are used to identify which attraction
    // and what amount was bought
    private static Attraction attraction = null;
    private static int amount = 0;

    public void attractionPurchaseMethod(int amountOfTickets, Attraction model) {
        attraction = model;
        amount = amountOfTickets;

        // calculate the purchase price:
        double purchase_price = amountOfTickets * model.getPrice();

        // initialize the paypal intent:
        PayPalPayment payment = new PayPalPayment(new BigDecimal(purchase_price), "USD",
                amountOfTickets + " tickets for attraction: " + model.getKey(), PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(activity_attractions.this, PaymentActivity.class);
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

                // 1. update the attraction available amount ( subtract the amount the user purchased ):
                String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                String category = DataBaseCollectionsKeys.ATTRACTIONS;
                String productKey = attraction.getKey();
                // hotel / flight / attraction - all have in common is availableAmount
                cloudActivities.updateProductAmount(activity_attractions.this, cityKey, category, productKey, amount);

                // 2. add a statistic's object to the city, later used for statistical reports:
                double price = amount * attraction.getPrice();
                long time_now = Calendar.getInstance().getTimeInMillis();
                Statistic statistic = new Statistic(DataBaseCollectionsKeys.ATTRACTIONS, time_now, amount, price);
                cloudActivities.appendStatistic(activity_attractions.this, statistic, cityKey);

                // 3. add a purchase history for a user:
                AttractionPurchase attractionPurchase = (AttractionPurchase) cloudActivities.addPurchaseHistoryToUser(activity_attractions.this, time_now, amount, price, attraction);

                // 4. make a pdf reception for the user:
                try {
                    PDFMaker pdfMaker = new PDFMaker();
                    pdfMaker.createPDFreceipt(activity_attractions.this, attractionPurchase);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // payment was successful, notify the user:
                appGeneralActivities.displayAlertDialog(activity_attractions.this, "purchase successful",
                        "purchase is successful, your reception is at your documents folder, and has been added to your attraction's purchase history", R.drawable.information);

            } else {
                // purchase was not successful:
                appGeneralActivities.displayAlertDialog(activity_attractions.this, "payment failed",
                        "something went wrong, if you believe you've been charged, please contact customer support", R.drawable.warning);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // stop the paypal service on application closure:
        stopService(new Intent(activity_attractions.this, PayPalService.class));
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
                if(newText.isEmpty()) {
                    options = cloudActivities.optionsQueryToDisplayAllAttractions(activity_attractions.this);
                } else {
                    options = cloudActivities.displayAttractionsByQuery(activity_attractions.this, newText, fieldToSearchBy);
                }
                // if options was null, it means there was connectivity issues
                if (options != null) {
                    attractionAdapter = new AttractionAdapter(options);
                    attractionAdapter.startListening();
                    attraction_recyclerView.setAdapter(attractionAdapter);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.add_new_attraction:
                intent = new Intent(activity_attractions.this, activity_add_attraction.class);
                startActivity(intent);
                finish();
                break;

            case R.id.go_back:
                intent = new Intent(activity_attractions.this, activity_categories.class);
                startActivity(intent);
                finish();
                break;

            case R.id.search_by_type:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_TEXT);
                fieldToSearchBy = SharedPreferencesKeys.TYPE;
                search_bar.setQueryHint("search by type");
                options = cloudActivities.displayAttractionsByQuery(activity_attractions.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    attractionAdapter = new AttractionAdapter(options);
                    attractionAdapter.startListening();
                    attraction_recyclerView.setAdapter(attractionAdapter);
                }
                break;

            case R.id.search_by_price:
                search_bar.setQuery("", false);
                search_bar.setInputType(InputType.TYPE_CLASS_NUMBER);
                fieldToSearchBy = "price";
                search_bar.setQueryHint("search by maximum price");
                options = cloudActivities.displayAttractionsByQuery(activity_attractions.this, search_bar.getQuery().toString(), fieldToSearchBy);

                if (options != null) {
                    attractionAdapter = new AttractionAdapter(options);
                    attractionAdapter.startListening();
                    attraction_recyclerView.setAdapter(attractionAdapter);
                }
                break;
        }
    }
}