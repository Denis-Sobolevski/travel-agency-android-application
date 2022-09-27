package com.example.tourexpert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

public class activity_add_hotel extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;

    private EditText hotel_name, room_type, price_for_night, available_amount, hotel_room_description;
    private RatingBar hotel_stars;
    private ImageView room_image;
    private ProgressBar progressBar_add_hotel;
    private Button btn_add_hotel;


    public Uri imageUri;

    private final int SUCCESS_CODE = 1;
    private final int MIN_LENGTH = 2;
    private final String HOTEL_NAME = "hotel name";
    private final int MIN_STAR_COUNT = 1;
    private final String STAR_COUNT_ERR_MESSAGE = "please choose at least 1 star for an hotel";
    private final String PRICE_ERR_MESSAGE = "please enter a positive price for a night at this room";
    private final String ROOM_AVAILABLE_ERR_MESSAGE = "please enter a valid amount of rooms available";
    private final String ACTIONBAR_TITLE = "add new hotel";
    private final String IMAGE_NOT_FOUND_ERR_TITLE = "image not found";
    private final String IMAGE_NOT_FOUND_ERR_MESSAGE = "please choose an image for this hotel";
    private final String DESC_EMPTY_ERR_MESSAGE = "please enter a description";
    private final int MIN_PRICE = 1;
    private final int MIN_AVAILABLE_AMOUNT = 1;
    private final String ROOM_TYPE = "room type";
    private final String INTENT_TYPE = "image/*";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hotel);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region VIEWS
        hotel_name = this.findViewById(R.id.hotel_name);
        room_type = this.findViewById(R.id.room_type);
        price_for_night = this.findViewById(R.id.price_for_night);
        available_amount = this.findViewById(R.id.available_amount);
        hotel_room_description = this.findViewById(R.id.hotel_room_description);
        hotel_stars = this.findViewById(R.id.hotel_stars);

        room_image = this.findViewById(R.id.room_image);
        room_image.setOnClickListener(this);

        progressBar_add_hotel = this.findViewById(R.id.progressBar_add_hotel);
        btn_add_hotel = this.findViewById(R.id.btn_add_hotel);
        btn_add_hotel.setOnClickListener(this);
        // endregion VIEWS

        // region ActionBar
        getSupportActionBar().setTitle(this.ACTIONBAR_TITLE);
        // implements an arrow on the ActionBar which will operate as an MenuItem inside the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // endregion ActionBar
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.room_image:
                chooseRoomPicture();
                break;
            case R.id.btn_add_hotel:
                addHotel();
                break;
        }
    }

    private void addHotel() {
        String name = hotel_name.getText().toString().trim();
        String type = room_type.getText().toString().trim().toLowerCase();
        String description = hotel_room_description.getText().toString();
        int stars = hotel_stars.getProgress();
        int price = 0;
        int availableAmount = 0;

        // region hotel name validation:
        if (!appGeneralActivities.validateName(hotel_name, this.HOTEL_NAME, this.MIN_LENGTH)) {
            return;
        }
        // endregion hotel name validation

        // validate star count:
        if(stars < this.MIN_STAR_COUNT) {
            Toast.makeText(activity_add_hotel.this, this.STAR_COUNT_ERR_MESSAGE, Toast.LENGTH_LONG).show();
            return;
        }

        // region room type validation:
        if (!appGeneralActivities.validateName(room_type, this.ROOM_TYPE, this.MIN_LENGTH)) {
            return;
        }
        // endregion room type validation

        // region validate price for night:
        try {
            price = Integer.parseInt(price_for_night.getText().toString().trim().toLowerCase());

            if (price < this.MIN_PRICE)
                throw new Exception();
        } catch (Exception e) {
            price_for_night.setError(this.PRICE_ERR_MESSAGE);
            price_for_night.requestFocus();
            return;
        }
        // endregion validate price for night

        // region validate available_amount:
        try {
            availableAmount = Integer.parseInt(available_amount.getText().toString().trim().toLowerCase());

            if (availableAmount < this.MIN_AVAILABLE_AMOUNT)
                throw new Exception();
        } catch (Exception e) {
            available_amount.setError(this.ROOM_AVAILABLE_ERR_MESSAGE);
            available_amount.requestFocus();
            return;
        }
        // endregion validate available_amount

        // region hotel room description validation:
        if (description.isEmpty()) {
            hotel_room_description.setError(this.DESC_EMPTY_ERR_MESSAGE);
            hotel_room_description.requestFocus();
            return;
        }
        // endregion hotel room description validation

        // check if an image was chosen:
        if(imageUri == null) {
            appGeneralActivities.displayAlertDialog(this, this.IMAGE_NOT_FOUND_ERR_TITLE, this.IMAGE_NOT_FOUND_ERR_MESSAGE, R.drawable.warning);
            return;
        }

        Hotel hotel = new Hotel(null, price, availableAmount, name, type, description, null, stars);

        cloudActivities.addProduct(activity_add_hotel.this, hotel, progressBar_add_hotel);
    }

    // currently this method only takes care of the go back button
    // in the ActionBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        appGeneralActivities.click(this);
        // the previous activity was activity_hotels and the user type is employee
        intent = new Intent(activity_add_hotel.this, activity_hotels.class);
        startActivity(intent);
        finish();
        return true;
    }

    private void chooseRoomPicture() {
        Intent intent = new Intent();
        intent.setType(this.INTENT_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, this.SUCCESS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the user chose a picture
        if(requestCode == this.SUCCESS_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            room_image.setImageURI(imageUri);
        }
    }
}