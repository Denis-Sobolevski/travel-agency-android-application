package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.firebase.ui.database.FirebaseRecyclerOptions;

public class activity_purchase_history extends AppCompatActivity implements View.OnClickListener {

    Intent intent;
    CloudActivities cloudActivities;
    AppGeneralActivities appGeneralActivities;
    RecyclerView purchases_RecyclerView;

    RadioButton show_flight_purchases, show_attraction_purchases, show_hotel_purchases;

    FlightPurchaseAdapter flightPurchaseAdapter;
    FirebaseRecyclerOptions<FlightPurchase> flightPurchaseFirebaseRecyclerOptions;

    AttractionPurchaseAdapter attractionPurchaseAdapter;
    FirebaseRecyclerOptions<AttractionPurchase> attractionPurchaseFirebaseRecyclerOptions;

    HotelPurchaseAdapter hotelPurchaseAdapter;
    FirebaseRecyclerOptions<HotelPurchase> hotelPurchaseFirebaseRecyclerOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        SavedActivity.setActivity(activity_purchase_history.this);

        // region VIEWS
        show_flight_purchases = this.findViewById(R.id.show_flight_purchases);
        show_flight_purchases.setOnClickListener(this);

        show_attraction_purchases = this.findViewById(R.id.show_attraction_purchases);
        show_attraction_purchases.setOnClickListener(this);

        show_hotel_purchases = this.findViewById(R.id.show_hotel_purchases);
        show_hotel_purchases.setOnClickListener(this);
        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle("Purchase history");
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion

        // region RecyclerView:
        purchases_RecyclerView = this.findViewById(R.id.purchases_recyclerView);
        purchases_RecyclerView.setLayoutManager(new LinearLayoutManager(this));


        flightPurchaseFirebaseRecyclerOptions = cloudActivities.displayAllFlightPurchases(activity_purchase_history.this);

        // if its not null, we can show the data:
        if (flightPurchaseFirebaseRecyclerOptions != null) {
            flightPurchaseAdapter = new FlightPurchaseAdapter(flightPurchaseFirebaseRecyclerOptions);
            flightPurchaseAdapter.startListening();
            purchases_RecyclerView.setAdapter(flightPurchaseAdapter);
        }
        // endregion RecyclerView:

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // the user has clicked the go back arrow button:
        appGeneralActivities.click(this);
        intent = new Intent(activity_purchase_history.this, activity_index_user.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.show_flight_purchases:
                flightPurchaseFirebaseRecyclerOptions = cloudActivities.displayAllFlightPurchases(activity_purchase_history.this);
                // if its not null, we can show the data:
                if (flightPurchaseFirebaseRecyclerOptions != null) {
                    flightPurchaseAdapter = new FlightPurchaseAdapter(flightPurchaseFirebaseRecyclerOptions);
                    flightPurchaseAdapter.startListening();
                    purchases_RecyclerView.setAdapter(flightPurchaseAdapter);
                }
                break;
            case R.id.show_attraction_purchases:
                attractionPurchaseFirebaseRecyclerOptions = cloudActivities.displayAllAttractionPurchases(activity_purchase_history.this);
                // if its not null, we can show the data:
                if (attractionPurchaseFirebaseRecyclerOptions != null) {
                    attractionPurchaseAdapter = new AttractionPurchaseAdapter(attractionPurchaseFirebaseRecyclerOptions);
                    attractionPurchaseAdapter.startListening();
                    purchases_RecyclerView.setAdapter(attractionPurchaseAdapter);
                }
                break;
            case R.id.show_hotel_purchases:
                hotelPurchaseFirebaseRecyclerOptions = cloudActivities.displayAllHotelPurchases(activity_purchase_history.this);
                // if its not null, we can show the data:
                if (hotelPurchaseFirebaseRecyclerOptions != null) {
                    hotelPurchaseAdapter = new HotelPurchaseAdapter(hotelPurchaseFirebaseRecyclerOptions);
                    hotelPurchaseAdapter.startListening();
                    purchases_RecyclerView.setAdapter(hotelPurchaseAdapter);
                }
                break;
        }
    }
}