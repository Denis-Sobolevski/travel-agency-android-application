package com.example.tourexpert;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.blongho.country_data.Country;
import com.blongho.country_data.Currency;
import com.blongho.country_data.World;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * this class is responsible for the activity_cities
 * recyclerView and all the operations done on it
 */
public class CityAdapter extends FirebaseRecyclerAdapter<City, CityAdapter.myViewHolder> {

    Activity parent_activity;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;
    String user_type;
    Intent intent;
    DialogPlus update_city_popup;
    final int HEIGHT_OF_POPUP = 880;
    final int SHORTEST_LENGTH_FOR_CITY_NAME = 1;
    final int SHORTEST_LENGTH_FOR_COUNTRY_NAME = 4;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options option object containing the query to listen
     */
    public CityAdapter(@NonNull FirebaseRecyclerOptions<City> options) {
        super(options);
        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
    }

    @Override
    protected void onBindViewHolder(@NonNull CityAdapter.myViewHolder holder, int position, @NonNull City model) {
        // show country name with capital letter
        holder.country_name.setText(model.getCountryName().substring(0, 1).toUpperCase() + model.getCountryName().substring(1));
        holder.city_name.setText(model.getCityName());

        // Use country name to get the Country Object"
        Country country = World.getCountryFrom(model.getCountryName());
        final int flag = country.getFlagResource();

        // set the appropriate flag for the country of specific city:
        holder.country_flag.setImageResource(flag);

        // get the currency Object for the country:
        Currency currency = country.getCurrency();
        holder.country_currency.setText("currency: " + currency.getSymbol() + " " + currency.getName());


        // region Permissions
        // only employee has the permission to edit a city:, disable or enable it:
        // therefore only an employee will see this buttons:
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            holder.btn_edit_city.setVisibility(View.VISIBLE);
            // if the city is activated, give the option to de-activate:
            if (model.getStatus()) {
                holder.btn_disable_city.setVisibility(View.VISIBLE);
                holder.btn_enable_city.setVisibility(View.GONE);
            } else {
                // the city is de-activated, give the option to activate:
                holder.btn_disable_city.setVisibility(View.GONE);
                holder.btn_enable_city.setVisibility(View.VISIBLE);
            }

            // open a map that will contain the updated city status:
            Map<String, Object> status_update = new HashMap<String, Object>();

            // region Button click handlers
            // the employee has chose to enable a city:
            holder.btn_enable_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // the desired status is true:
                    status_update.put("status", true);

                    if (appGeneralActivities.isConnectedToInternet(parent_activity)) {
                        if (appGeneralActivities.isConnectedToDatabase()) {
                            // has necessary connection, attempt:
                            cloudActivities.updateCityStatus(parent_activity, model.getKey(), status_update);

                        } else {
                            // no connection to database:
                            appGeneralActivities.displayAlertDialog(
                                    parent_activity,
                                    "operation failed",
                                    "operation failed, the database is currently unavailable, please try again later",
                                    R.drawable.warning
                            );
                        }
                    } else {
                        // no connection to internet:
                        appGeneralActivities.displayAlertDialog(
                                parent_activity,
                                "operation failed",
                                "operation failed, no internet connection was found, please try again later",
                                R.drawable.warning
                        );
                    }
                }
            });

            // the employee has chose to disable
            holder.btn_disable_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // the desired status is false:
                    status_update.put("status", false);

                    if (appGeneralActivities.isConnectedToInternet(parent_activity)) {
                        if (appGeneralActivities.isConnectedToDatabase()) {
                            // has necessary connection, attempt:
                            cloudActivities.updateCityStatus(parent_activity, model.getKey(), status_update);

                        } else {
                            // no connection to database:
                            appGeneralActivities.displayAlertDialog(
                                    parent_activity,
                                    "operation failed",
                                    "operation failed, the database is currently unavailable, please try again later",
                                    R.drawable.warning
                            );
                        }
                    } else {
                        // no connection to internet:
                        appGeneralActivities.displayAlertDialog(
                                parent_activity,
                                "operation failed",
                                "operation failed, no internet connection was found, please try again later",
                                R.drawable.warning
                        );
                    }
                }
            });

            // employee has chose to edit the city itself:
            holder.btn_edit_city.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    appGeneralActivities.click(parent_activity);
                    // call a function that sets the edit city popup
                    show_update_popup(model.getCityName(), model.getCountryName(), model.getKey());
                }
            });
            // endregion Button click handlers

        }
        // notify a user that this city is unavailable:
        if (!model.getStatus())
            holder.city_unavailable.setVisibility(View.VISIBLE);
        else
            holder.city_unavailable.setVisibility(View.GONE);

        // a regular user and an employee user can browse a city
        // while employee can browse disabled cities,
        // a regular user cant
        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                if (appGeneralActivities.isConnectedToInternet(parent_activity)) {
                    if (appGeneralActivities.isConnectedToDatabase()) {
                        // if the user type is an user and the current city is unavailable, notify the user:
                        if (user_type.equals(UserTypeKeys.USER) && !model.getStatus()) {
                            appGeneralActivities.displayAlertDialog(
                                    parent_activity,
                                    "city unavailable",
                                    model.getCityName() + " is unavailable right now",
                                    R.drawable.warning);
                        } else {
                            // its either a user that tries to browse an available city, employee or an admin
                            // open an shared preferences userChoice:
                            // save:
                            // city key     - for later access to the city node
                            // city name    - for reception
                            // country name - for reception
                            SharedPreferences shared = parent_activity.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = shared.edit();

                            editor.putString(SharedPreferencesKeys.KEY, model.getKey());
                            editor.putString(SharedPreferencesKeys.CITY_NAME, model.getCityName());
                            editor.putString(SharedPreferencesKeys.COUNTRY_NAME, model.getCountryName());
                            editor.apply();

                            if(! user_type.equals(UserTypeKeys.ADMIN)) // user or employee tries to access the categories activity:
                                intent = new Intent(parent_activity, activity_categories.class);
                            else // admin tries to access the report's activity:
                                intent = new Intent(parent_activity, activity_reports.class);

                            // redirect the user to the correct activity:
                            parent_activity.startActivity(intent);
                            parent_activity.finish();

                        }
                    } else {
                        // database is unavailable:
                        appGeneralActivities.displayAlertDialog(
                                parent_activity,
                                "browse failed",
                                "database is currently unavailable, please try again later",
                                R.drawable.warning
                        );
                    }
                } else {
                    // no connection to the internet;
                    appGeneralActivities.displayAlertDialog(
                            parent_activity,
                            "browse failed",
                            "no network connection was found, please try again later",
                            R.drawable.warning
                    );
                }
            }
        });
        // endregion Permissions
    }

    /**
     * function gets a city name and country name and the city node key and manages the city update popup
     *
     * @param cityName    the name of the city to display
     * @param countryName the name of the country to display
     * @param key         the unique key of the city node that was selected to be edited
     */
    private void show_update_popup(String cityName, String countryName, String key) {
        // initialize the DialogPlus popup object to display the update city popup:
        update_city_popup = DialogPlus.newDialog(parent_activity)
                .setContentHolder(new ViewHolder(R.layout.update_city_popup))
                .setExpanded(true, this.HEIGHT_OF_POPUP)
                .create();

        // access the popup view and initialize the EditTexts and button:
        View view = update_city_popup.getHolderView();

        // initialize the EditTexts and button:
        EditText updated_city_name = view.findViewById(R.id.updated_city_name);
        EditText updated_country_name = view.findViewById(R.id.updated_country_name);

        Button btn_update_city = view.findViewById(R.id.btn_update_city);

        View progressBar_update_city = view.findViewById(R.id.progressBar_update_city);

        updated_city_name.setText(cityName);
        updated_country_name.setText(countryName);

        // display the update_city_popup:
        update_city_popup.show();

        // set on click for the update city button:
        btn_update_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                // attempt to update the city details:
                String cityName = updated_city_name.getText().toString().trim().toLowerCase();
                String countryName = updated_country_name.getText().toString().trim().toLowerCase();

                // cityName validation:
                // shortest city name is 1 character, yup i was surprised to
                if (!appGeneralActivities.validateName(updated_city_name, "city name", SHORTEST_LENGTH_FOR_CITY_NAME))
                    return;

                // countryName validation:
                // shortest country name is 4 characters
                if (!appGeneralActivities.validateName(updated_country_name, "country name", SHORTEST_LENGTH_FOR_COUNTRY_NAME))
                    return;

                cloudActivities.updateCityDetails(
                        parent_activity,
                        cityName,
                        countryName,
                        progressBar_update_city,
                        key);
            }
        });
    }

    @NonNull
    @Override
    public CityAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_card_view, parent, false);

        // get access to the parent activity:
        this.parent_activity = SavedActivity.getActivity();

        // get the current logged in user type from the general shared prefs:
        user_type = this.parent_activity.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);

        // initialize the world country flag package:
        World.init(parent_activity);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        protected TextView country_name, city_name, country_currency, city_unavailable;
        protected Button btn_disable_city, btn_enable_city, btn_edit_city;
        protected ImageView country_flag;
        protected CardView card_view;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            country_name = itemView.findViewById(R.id.country_name);
            city_name = itemView.findViewById(R.id.city_name);
            country_currency = itemView.findViewById(R.id.country_currency);
            city_unavailable = itemView.findViewById(R.id.city_unavailable);

            country_flag = itemView.findViewById(R.id.country_flag);
            card_view = itemView.findViewById(R.id.card_view);

            btn_disable_city = itemView.findViewById(R.id.btn_disable_city);
            btn_enable_city = itemView.findViewById(R.id.btn_enable_city);
            btn_edit_city = itemView.findViewById(R.id.btn_edit_city);
        }
    }
}
