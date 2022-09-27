package com.example.tourexpert;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * adapter class for the FireBaseRecyclerAdapter
 * which is responsible to display a list of Hotel Objects on a RecyclerView
 */
public class HotelAdapter extends FirebaseRecyclerAdapter<Hotel, HotelAdapter.myViewHolder> implements DatePickerDialog.OnDateSetListener {

    Activity parent_activity;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;
    String user_type;
    long MAX_BYTES = 1024 * 1024 * 5; // 5Mb is max image size
    final int INVALID_AMOUNT = 0;
    final int MIN_VALID_STAR_AMOUNT = 1;
    final int MIN_VALID_PRICE = 1;

    DialogPlus update_hotel_popup;
    DateTimeFormatter formatter;

    DateTimeFormatter dateTimeFormatter;
    DatePickerDialog datePickerDialog;
    EditText viewToSetDate;

    boolean checkIn;
    boolean checkOut;

    LocalDate checkInDate;
    LocalDate checkOutDate;

    // region Date:
    // initialize a DatePickerDialog View dialog:


    // set the min date available to today
    // endregion Date

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public HotelAdapter(@NonNull FirebaseRecyclerOptions<Hotel> options) {
        super(options);

        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
        dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull Hotel model) {

        // if the user type is user, filter only valid hotels:
        if (user_type.equals(UserTypeKeys.USER)) {
            if (model.getAvailableAmount() <= this.INVALID_AMOUNT) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.getLayoutParams().height = 0;
                holder.itemView.getLayoutParams().width = 0;
                return;
            }
        }

        datePickerDialog = new DatePickerDialog(parent_activity, android.R.style.Theme_DeviceDefault_Dialog, this::onDateSet,
                0, 0, 0);

        datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

        holder.room_amount.setMinValue(1);
        holder.room_amount.setMaxValue(model.getAvailableAmount());
        holder.hotel_stars.setProgress(model.getStars());
        holder.hotel_stars.setIsIndicator(true);

        holder.check_in_date.setInputType(InputType.TYPE_NULL);
        holder.check_in_date.setTextIsSelectable(false);
        holder.check_in_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn = true;
                checkOut = false;
                viewToSetDate = holder.check_in_date;
                datePickerDialog.show();
            }
        });

        holder.check_out_date.setInputType(InputType.TYPE_NULL);
        holder.check_out_date.setTextIsSelectable(false);
        holder.check_out_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn = false;
                checkOut = true;
                viewToSetDate = holder.check_out_date;
                datePickerDialog.show();
            }
        });

        holder.hotel_name.setText(model.getName());
        holder.room_type.setText(model.getType());
        holder.price_for_night.setText(model.getPrice() + "$");

        // download the Image and set it:
        StorageReference image_path = FirebaseStorage.getInstance().getReference(model.imageKey);
        Log.d("image_path: ", model.getImageKey());
        image_path.getBytes(MAX_BYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.hotel_image.setVisibility(View.VISIBLE);
                holder.hotel_image.setImageBitmap(bitmap);
            }
        });

        holder.current_amount_available.setText(model.getAvailableAmount()  > 10 ? "10 +" : (model.getAvailableAmount() + ""));
        holder.room_description.setText(model.getDescription());

        // activate the employee permissions:
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            holder.employee_permissions.setVisibility(View.VISIBLE);
            holder.more_information.setVisibility(View.VISIBLE);

            holder.btn_delete_hotel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    SharedPreferences shared_userChoice = parent_activity.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
                    String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                    cloudActivities.deleteHotel(parent_activity, cityKey, model.getKey());
                }
            });

            holder.btn_edit_hotel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // call a function that sets the edit hotel popup
                    show_update_popup(model);
                }
            });
        }

        // activate user permissions:
        if (user_type.equals(UserTypeKeys.USER)) {
            holder.user_permissions.setVisibility(View.VISIBLE);

            // if the user clicks on this, only then the menu is open:
            holder.open_buy_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // if the rooms_buy_menu was closed, open it and change the icon:
                    if (holder.rooms_buy_menu.getVisibility() == View.GONE) {
                        holder.menu_image.setImageResource(R.drawable.arrow_up);
                        holder.rooms_buy_menu.setVisibility(View.VISIBLE);
                    } else {
                        // the room_buy_menu was open, close it and change the icon:
                        holder.menu_image.setImageResource(R.drawable.arrow_down);
                        holder.rooms_buy_menu.setVisibility(View.GONE);
                    }
                }
            });

            // opens the more information for the user:
            holder.card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    if (holder.more_information.getVisibility() == View.GONE) {
                        holder.more_information.setVisibility(View.VISIBLE);
                    } else {
                        holder.more_information.setVisibility(View.GONE);
                    }
                }
            });

            holder.btn_buy_rooms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);

                    // region check if the the date's are given:
                    if (holder.check_in_date.getText().toString().isEmpty()) {
                        holder.check_in_date.setError("please enter a check-in date:");
                        holder.check_in_date.requestFocus();
                        return;
                    } else
                        holder.check_in_date.setError(null);

                    if (holder.check_out_date.getText().toString().isEmpty()) {
                        holder.check_out_date.setError("please enter a check-out date");
                        holder.check_out_date.requestFocus();
                        return;
                    } else
                        holder.check_out_date.setError(null);

                    // endregion check if the the date's are given:

                    // check if check-out-date is not before or equal check-in-date:
                    if (checkOutDate.isEqual(checkInDate) || checkOutDate.isBefore(checkInDate)) {
                        holder.check_out_date.setError("please enter a valid check-out date");
                        holder.check_out_date.requestFocus();
                        return;
                    } else
                        holder.check_out_date.setError(null);

                    int days = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                    String arrivalDate = checkInDate.format(formatter);
                    String departureDate = checkOutDate.format(formatter);

                    holder.check_in_date.setText("");
                    holder.check_out_date.setText("");
                    checkInDate = null;
                    checkOutDate = null;

                    ((activity_hotels)parent_activity).hotelPurchaseMethod(arrivalDate, departureDate,days, holder.room_amount.getValue(), model);

                    holder.room_amount.setValue(1);
                }
            });
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        appGeneralActivities.click(parent_activity);
        if (checkIn) {
            checkInDate = LocalDate.of(year, month + 1, dayOfMonth);
            Log.d("set: CheckInDate: ", dateTimeFormatter.format(checkInDate));
            viewToSetDate.setText(dateTimeFormatter.format(checkInDate));
        } else if (checkOut) {
            checkOutDate = LocalDate.of(year, month + 1, dayOfMonth);
            Log.d("set: CheckOutDate: ", dateTimeFormatter.format(checkOutDate));
            viewToSetDate.setText(dateTimeFormatter.format(checkOutDate));
        }
    }

    private void show_update_popup(Hotel model) {
        String name = model.getName();
        String type = model.getType();
        int price = model.getPrice();
        int stars = model.getStars();
        int availableAmount = model.getAvailableAmount();
        String description = model.getDescription();
        String hotelKey = model.getKey();

        update_hotel_popup = DialogPlus.newDialog(parent_activity)
                .setContentHolder(new ViewHolder(R.layout.update_hotel_popup))
                .setExpanded(true, 1200) // 1200 is the height of the layout in pixels
                .create();

        View view = update_hotel_popup.getHolderView();

        EditText updated_hotel_name = view.findViewById(R.id.updated_hotel_name);
        EditText updated_room_type = view.findViewById(R.id.updated_room_type);
        EditText updated_room_price = view.findViewById(R.id.updated_room_price);
        RatingBar updated_hotel_stars = view.findViewById(R.id.hotel_stars);
        EditText updated_available_amount = view.findViewById(R.id.updated_available_amount);
        EditText updated_room_description = view.findViewById(R.id.updated_room_description);

        ProgressBar progressBar_update_hotel = view.findViewById(R.id.progressBar_update_hotel);

        Button btn_update_hotel = view.findViewById(R.id.btn_update_hotel);

        updated_hotel_name.setText(name);
        updated_hotel_stars.setProgress(stars);
        updated_room_type.setText(type);
        updated_room_price.setText(price + "");
        updated_available_amount.setText(availableAmount + "");
        updated_room_description.setText(description);

        update_hotel_popup.show();

        btn_update_hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);

                String name = updated_hotel_name.getText().toString().trim();
                String type = updated_room_type.getText().toString().trim().toLowerCase();
                int stars = updated_hotel_stars.getProgress();
                int price = 0;
                int availableAmount = 0;
                String description = updated_room_description.getText().toString().trim();

                // region hotel name validation:
                if (!appGeneralActivities.validateName(updated_hotel_name, "hotel name", 2)) {
                    return;
                }
                // endregion hotel name validation

                // validate star count:
                if(stars < MIN_VALID_STAR_AMOUNT) {
                    Toast.makeText(parent_activity, "please choose at least 1 star for an hotel",Toast.LENGTH_LONG).show();
                    return;
                }

                // region room type validation:
                if (!appGeneralActivities.validateName(updated_room_type, "room type", 2)) {
                    return;
                }
                // endregion room type validation

                // region validate price for night:
                try {
                    price = Integer.parseInt(updated_room_price.getText().toString().trim().toLowerCase());

                    if (price < MIN_VALID_PRICE)
                        throw new Exception();
                } catch (Exception e) {
                    updated_room_price.setError("please enter a positive price for a night at this room");
                    updated_room_price.requestFocus();
                    return;
                }
                // endregion validate price for night

                // region validate available_amount:
                try {
                    availableAmount = Integer.parseInt(updated_available_amount.getText().toString().trim().toLowerCase());

                    if (availableAmount < INVALID_AMOUNT)
                        throw new Exception();
                } catch (Exception e) {
                    updated_available_amount.setError("please enter a valid amount of rooms available");
                    updated_available_amount.requestFocus();
                    return;
                }
                // endregion validate available_amount

                // region hotel room description validation:
                if (description.isEmpty()) {
                    updated_room_description.setError("please enter a room description");
                    updated_room_description.requestFocus();
                    return;
                }
                // endregion hotel room description validation


                Map<String, Object> details_to_update = new HashMap<String, Object>();
                details_to_update.put("name", name);
                details_to_update.put("stars", stars);
                details_to_update.put("type", type);
                details_to_update.put("price", price);
                details_to_update.put("availableAmount", availableAmount);
                details_to_update.put("description", description);

                cloudActivities.updateHotelDetails(parent_activity, hotelKey, details_to_update, progressBar_update_hotel);
            }
        });


    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_card_view, parent, false);

        // get access to the parent activity:
        this.parent_activity = SavedActivity.getActivity();

        // get the current logged in user type from the general shared prefs:
        user_type = this.parent_activity.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);

        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        protected CardView card_view;
        protected TextView hotel_name, room_type, price_for_night;
        protected RatingBar hotel_stars;
        protected ImageView hotel_image;
        protected TextView current_amount_available, room_description;

        protected LinearLayout more_information;

        // employee_permissions will be visible only to employee accounts
        protected LinearLayout employee_permissions;

        // these buttons are in employee_permissions:
        protected Button btn_edit_hotel, btn_delete_hotel;

        // user_permissions will be visible only to user accounts
        protected LinearLayout user_permissions, open_buy_menu, rooms_buy_menu;

        protected ImageView menu_image;

        // these are in rooms_buy_menu, will show up when clicking on open_buy_menu:

        EditText check_in_date;
        EditText check_out_date;
        NumberPicker room_amount;

        protected Button btn_buy_rooms;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            card_view = itemView.findViewById(R.id.card_view);
            hotel_name = itemView.findViewById(R.id.hotel_name);
            hotel_stars = itemView.findViewById(R.id.hotel_stars);
            room_type = itemView.findViewById(R.id.room_type);
            price_for_night = itemView.findViewById(R.id.price_for_night);
            hotel_image = itemView.findViewById(R.id.hotel_image);
            current_amount_available = itemView.findViewById(R.id.current_amount_available);
            room_description = itemView.findViewById(R.id.room_description);

            more_information = itemView.findViewById(R.id.more_information);
            employee_permissions = itemView.findViewById(R.id.employee_permissions);

            btn_edit_hotel = itemView.findViewById(R.id.btn_edit_hotel);
            btn_delete_hotel = itemView.findViewById(R.id.btn_delete_hotel);

            user_permissions = itemView.findViewById(R.id.user_permissions);

            open_buy_menu = itemView.findViewById(R.id.open_buy_menu);
            rooms_buy_menu = itemView.findViewById(R.id.rooms_buy_menu);

            menu_image = itemView.findViewById(R.id.menu_image);

            check_in_date = itemView.findViewById(R.id.check_in_date);
            check_out_date = itemView.findViewById(R.id.check_out_date);
            room_amount = itemView.findViewById(R.id.room_amount);

            btn_buy_rooms = itemView.findViewById(R.id.btn_buy_rooms);


        }
    }

}
