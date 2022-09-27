package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class activity_cities extends AppCompatActivity implements View.OnClickListener {

    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private RecyclerView city_recyclerView;
    private CityAdapter cityAdapter;

    // region custom ActionBar
    private ViewGroup activity_cities_action_bar;
    private ImageView go_back;
    private SearchView search_bar;
    // endregion custom ActionBar

    private RadioButton search_by_city;
    private RadioButton search_by_country;

    private Intent intent;
    private String user_type;

    private FloatingActionButton add_new_city;
    private SharedPreferences shared;

    private String fieldToSearchBy;
    private static FirebaseRecyclerOptions<City> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
        shared = getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);

        // save the current activity in a utility class so we can access
        // it on the cityAdapter:
        SavedActivity.setActivity(activity_cities.this);

        //region SharedPreferencesKeys
        // the user type maybe: user, employee or admin
        user_type = shared.getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);
        //endregion SharedPreferencesKeys

        // region RadioButtons
        search_by_city = this.findViewById(R.id.search_by_city);
        search_by_city.setOnClickListener(this);

        search_by_country = this.findViewById(R.id.search_by_country);
        search_by_country.setOnClickListener(this);
        // endregion RadioButtons

        // region RecyclerView
        city_recyclerView = this.findViewById(R.id.city_recyclerView);
        city_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = cloudActivities.optionsQueryToDisplayAllCities(activity_cities.this);
        // if options was null, it means we have connectivity issues (internet or database), we cannot
        // use options that is null
        if (options != null) {
            cityAdapter = new CityAdapter(options);
            cityAdapter.startListening();
            city_recyclerView.setAdapter(cityAdapter);
        }

        // endregion RecyclerView

        //region ActionBar
        // hide the default ActionBar and initialize our custom ActionBar with searchView
        getSupportActionBar().hide();
        activity_cities_action_bar = this.findViewById(R.id.cities_action_ber);

        go_back = activity_cities_action_bar.findViewById(R.id.go_back);
        go_back.setOnClickListener(this);

        search_bar = activity_cities_action_bar.findViewById(R.id.search_bar);
        search_bar.setIconifiedByDefault(false);
        //endregion ActionBar

        // region FloatingActionButton
        // only if the user type is employee, make this button visible
        if (user_type.equals(UserTypeKeys.EMPLOYEE)) {
            add_new_city = this.findViewById(R.id.add_new_city);
            add_new_city.setVisibility(View.VISIBLE);
            add_new_city.setOnClickListener(this);
        }
        // endregion FloatingActionButton

        // set the default search option by cityName:
        fieldToSearchBy = SharedPreferencesKeys.CITY_NAME;
        // set the query:
        search_bar_query();
    }

    public void search_bar_query() {
        search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // case-insensitive search:
                newText = newText.toLowerCase();
                options = cloudActivities.displayCitiesByQuery(activity_cities.this, newText, fieldToSearchBy);

                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    cityAdapter = new CityAdapter(options);
                    cityAdapter.startListening();
                    city_recyclerView.setAdapter(cityAdapter);
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.go_back:
                if (user_type.equals(UserTypeKeys.EMPLOYEE))
                    intent = new Intent(activity_cities.this, activity_index_employee.class);
                else if (user_type.equals(UserTypeKeys.USER))
                    intent = new Intent(activity_cities.this, activity_index_user.class);
                else if(user_type.equals(UserTypeKeys.ADMIN))
                    intent = new Intent(activity_cities.this, activity_index_admin.class);
                startActivity(intent);
                finish();
                break;
            case R.id.add_new_city:
                // the employee has choose to add a city
                intent = new Intent(activity_cities.this, activity_add_city.class);
                startActivity(intent);
                finish();
                break;

            case R.id.search_by_city:
                fieldToSearchBy = SharedPreferencesKeys.CITY_NAME;
                search_bar.setQueryHint("search by city");
                options = cloudActivities.displayCitiesByQuery(activity_cities.this, search_bar.getQuery().toString(), fieldToSearchBy);

                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    cityAdapter = new CityAdapter(options);
                    cityAdapter.startListening();
                    city_recyclerView.setAdapter(cityAdapter);
                }
                break;

            case R.id.search_by_country:
                fieldToSearchBy = SharedPreferencesKeys.COUNTRY_NAME;
                search_bar.setQueryHint("search by country");
                options = cloudActivities.displayCitiesByQuery(activity_cities.this, search_bar.getQuery().toString(), fieldToSearchBy);

                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    cityAdapter = new CityAdapter(options);
                    cityAdapter.startListening();
                    city_recyclerView.setAdapter(cityAdapter);
                }
                break;
        }
    }
}