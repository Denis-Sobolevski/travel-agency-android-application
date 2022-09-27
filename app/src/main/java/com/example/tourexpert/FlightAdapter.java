package com.example.tourexpert;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * this class is responsible for the
 * recyclerView that shows Flight Objects list
 * and all the operations done on it
 */
public class FlightAdapter extends FirebaseRecyclerAdapter<Flight, FlightAdapter.myViewHolder> {

    Activity parent_activity;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;
    String user_type;
    DialogPlus update_flight_popup;
    final int INVALID_AMOUNT = 0;
    final int INVALID_TICKET_PRICE = 1;
    long now;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FlightAdapter(@NonNull FirebaseRecyclerOptions<Flight> options) {
        super(options);
        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
        now = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    protected void onBindViewHolder(@NonNull FlightAdapter.myViewHolder holder, int position, @NonNull Flight model) {

        // if the user type is user, filter only valid flights:
        if (user_type.equals(UserTypeKeys.USER)) {
            if (model.getAvailableAmount() <= this.INVALID_AMOUNT || model.getDateOfFlight() < now) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.getLayoutParams().height = 0;
                holder.itemView.getLayoutParams().width = 0;
                return;
            }
        }

        holder.flight_number.setText(model.getKey());
        holder.flight_class.setText(model.getFlightClass());
        holder.source_city.setText(model.getSource());
        holder.destination_city.setText(model.getDestination());

        holder.ticket_amount.setMinValue(1);
        holder.ticket_amount.setMaxValue(model.getAvailableAmount());

        // convert long to time object:
        LocalDateTime convert_date_from_long_to_object =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(model.getDateOfFlight()),
                        TimeZone.getDefault().toZoneId());

        // convert the date to a readable String using formatter:
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' hh:mm a");
        String date_of_flight = convert_date_from_long_to_object.format(formatter);

        holder.date_of_flight.setText(date_of_flight);

        // show price in dollars:
        holder.ticket_price.setText(model.getPrice() + "$");

        // show the current available amount:
        holder.current_amount_available.setText(model.getAvailableAmount()  > 10 ? "10 +" : (model.getAvailableAmount() + ""));

        // activate the employee permissions:
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            holder.employee_permissions.setVisibility(View.VISIBLE);
            holder.more_information.setVisibility(View.VISIBLE);

            holder.btn_delete_flight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    SharedPreferences shared_userChoice = parent_activity.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
                    String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                    cloudActivities.deleteInvalidFlight(parent_activity, cityKey, model.getKey());
                }
            });

            holder.btn_edit_flight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // call a function that sets the edit flight popup
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
                    // if the ticket_buy_menu was closed, open it and change the icon:
                    if (holder.tickets_buy_menu.getVisibility() == View.GONE) {
                        holder.menu_image.setImageResource(R.drawable.arrow_up);
                        holder.tickets_buy_menu.setVisibility(View.VISIBLE);
                    } else {
                        // the ticket_buy_menu was open, close it and change the icon:
                        holder.menu_image.setImageResource(R.drawable.arrow_down);
                        holder.tickets_buy_menu.setVisibility(View.GONE);
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


            holder.btn_buy_tickets.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // call the flight purchase method which is in the parent activity
                    ((activity_flights) parent_activity).flightPurchaseMethod(holder.ticket_amount.getValue(), model);
                }
            });
        }
    }


    private void show_update_popup(Flight model) {
        String source = model.getSource();
        String destination = model.getDestination();
        String flightClass = model.getFlightClass();
        int ticketPrice = model.getPrice();
        int availableAmount = model.getAvailableAmount();
        String flightKey = model.getKey();

        update_flight_popup = DialogPlus.newDialog(parent_activity)
                .setContentHolder(new ViewHolder(R.layout.update_flight_popup))
                .setExpanded(true, 1200) // 1200 is the height of the layout in pixels
                .create();

        View view = update_flight_popup.getHolderView();

        EditText updated_flight_source = view.findViewById(R.id.updated_flight_source);
        EditText updated_flight_destination = view.findViewById(R.id.updated_flight_destination);
        EditText updated_flight_class = view.findViewById(R.id.updated_flight_class);
        EditText updated_ticket_price = view.findViewById(R.id.updated_ticket_price);
        EditText updated_available_amount = view.findViewById(R.id.updated_available_amount);
        Button btn_update_flight = view.findViewById(R.id.btn_update_flight);

        View progressBar_update_flight = view.findViewById(R.id.progressBar_update_flight);

        updated_flight_source.setText(source);
        updated_flight_destination.setText(destination);
        updated_flight_class.setText(flightClass);
        updated_ticket_price.setText(ticketPrice + "");
        updated_available_amount.setText(availableAmount + "");

        update_flight_popup.show();

        btn_update_flight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                String sourceOfFlight = updated_flight_source.getText().toString().trim().toLowerCase();
                String flightClass = updated_flight_class.getText().toString().trim();
                String destination = updated_flight_destination.getText().toString().trim().toLowerCase();
                int ticketPrice = 0;
                int availableAmount = 0;

                // region flight_source validation:
                if (!appGeneralActivities.validateName(updated_flight_source, "source credentials", 4)) {
                    return;
                }
                // endregion flight_source validation

                // region flight_destination validation:
                if (!appGeneralActivities.validateName(updated_flight_destination, "destination credentials", 4)) {
                    return;
                }
                // endregion flight_destination validation

                // region flight_destination validation:
                if (!appGeneralActivities.validateName(updated_flight_class, "flight class", 2)) {
                    return;
                }
                // endregion flight_destination validation

                // region validate ticket_price:
                try {
                    ticketPrice = Integer.parseInt(updated_ticket_price.getText().toString().trim().toLowerCase());

                    if (ticketPrice < INVALID_TICKET_PRICE)
                        throw new Exception();
                } catch (Exception e) {
                    updated_ticket_price.setError("please enter a positive price for a ticket");
                    updated_ticket_price.requestFocus();
                    return;
                }
                // endregion validate ticket_price

                // region validate available_amount:
                try {
                    availableAmount = Integer.parseInt(updated_available_amount.getText().toString().trim().toLowerCase());

                    if (availableAmount < INVALID_AMOUNT)
                        throw new Exception();
                } catch (Exception e) {
                    updated_available_amount.setError("please enter a valid amount of tickets available");
                    updated_available_amount.requestFocus();
                    return;
                }
                // endregion validate available_amount

                Map<String, Object> details_to_update = new HashMap<String, Object>();
                details_to_update.put("source", sourceOfFlight);
                details_to_update.put("destination", destination);
                details_to_update.put("flightClass", flightClass);
                details_to_update.put("price", ticketPrice);
                details_to_update.put("availableAmount", availableAmount);

                cloudActivities.updateFlightDetails(parent_activity, flightKey, details_to_update, progressBar_update_flight);
            }
        });
    }

    @NonNull
    @Override
    public FlightAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flight_card_view, parent, false);

        // get access to the parent activity:
        this.parent_activity = SavedActivity.getActivity();

        // get the current logged in user type from the general shared prefs:
        user_type = this.parent_activity.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);

        return new myViewHolder(view);
    }


    class myViewHolder extends RecyclerView.ViewHolder {
        protected CardView card_view;

        protected TextView source_city, destination_city, flight_number, flight_class, date_of_flight, ticket_price, current_amount_available;

        protected LinearLayout more_information;

        // employee_permissions will be visible only to employee accounts
        protected LinearLayout employee_permissions;

        // these buttons are in employee_permissions:
        protected Button btn_edit_flight, btn_delete_flight;

        // user_permissions will be visible only to user accounts
        protected LinearLayout user_permissions, open_buy_menu, tickets_buy_menu;

        protected ImageView menu_image;

        // these are in tickets_buy_menu, will show up when clicking on open_buy_menu:
        protected NumberPicker ticket_amount;
        protected Button btn_buy_tickets;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            card_view = itemView.findViewById(R.id.card_view);

            source_city = itemView.findViewById(R.id.source_city);
            destination_city = itemView.findViewById(R.id.destination_city);
            flight_number = itemView.findViewById(R.id.flight_number);
            flight_class = itemView.findViewById(R.id.flight_class);
            date_of_flight = itemView.findViewById(R.id.date_of_flight);
            ticket_price = itemView.findViewById(R.id.ticket_price);
            current_amount_available = itemView.findViewById(R.id.current_amount_available);

            more_information = itemView.findViewById(R.id.more_information);
            employee_permissions = itemView.findViewById(R.id.employee_permissions);

            btn_edit_flight = itemView.findViewById(R.id.btn_edit_flight);
            btn_delete_flight = itemView.findViewById(R.id.btn_delete_flight);

            user_permissions = itemView.findViewById(R.id.user_permissions);
            open_buy_menu = itemView.findViewById(R.id.open_buy_menu);
            tickets_buy_menu = itemView.findViewById(R.id.tickets_buy_menu);

            menu_image = itemView.findViewById(R.id.menu_image);

            ticket_amount = itemView.findViewById(R.id.ticket_amount);
            btn_buy_tickets = itemView.findViewById(R.id.btn_buy_tickets);
        }
    }

}
