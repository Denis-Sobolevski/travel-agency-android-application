package com.example.tourexpert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerOptions;

public class activity_manage_users extends AppCompatActivity implements View.OnClickListener {

    private AppGeneralActivities appGeneralActivities;
    private CloudActivities cloudActivities;
    private RecyclerView user_recyclerView;
    private UserAdapter userAdapter;

    private RadioButton search_by_email;
    private RadioButton search_by_firstName;

    // region custom ActionBar
    private ViewGroup manage_user_action_bar;
    private ImageView go_back_to_index_admin;
    private SearchView search_bar;
    // endregion custom ActionBar

    private String fieldToSearchBy;
    private static FirebaseRecyclerOptions<User> options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();

        // region RadioButton's
        search_by_email = this.findViewById(R.id.search_by_email);
        search_by_email.setOnClickListener(this);

        search_by_firstName = this.findViewById(R.id.search_by_firstName);
        search_by_firstName.setOnClickListener(this);
        // endregion RadioButton's

        // region recyclerView
        user_recyclerView = this.findViewById(R.id.user_recyclerView);
        user_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        options = cloudActivities.optionsQueryToDisplayAllUsers(activity_manage_users.this);
        // if options was null, it means we have connectivity issues (internet or database), we cannot
        // use options that is null
        if (options != null) {
            userAdapter = new UserAdapter(options);
            userAdapter.startListening();
            user_recyclerView.setAdapter(userAdapter);
        }
        // endregion recyclerView

        // region ActionBar
        // hide the default ActionBar, we will use aur own custom ActionBar
        getSupportActionBar().hide();
        manage_user_action_bar = this.findViewById(R.id.manage_user_action_bar);

        go_back_to_index_admin = manage_user_action_bar.findViewById(R.id.go_back_to_index_admin);
        go_back_to_index_admin.setOnClickListener(this);

        search_bar = manage_user_action_bar.findViewById(R.id.search_bar);
        // setIconifiedByDefault(false) - means that the SearchView will be open
        // from the start
        search_bar.setIconifiedByDefault(false);

        // set a listener to the search view to search employees by email address:
        fieldToSearchBy = SharedPreferencesKeys.EMAIL; // by default
        search_bar_query();
        // endregion ActionBar
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
                options = cloudActivities.displayUsersByQuery(activity_manage_users.this, newText, fieldToSearchBy);
                // if options was null, it means we have connectivity issues (internet or database), we cannot
                // use options that is null
                if (options != null) {
                    userAdapter = new UserAdapter(options);
                    userAdapter.startListening();
                    user_recyclerView.setAdapter(userAdapter);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        appGeneralActivities.click(this);
        switch (v.getId()) {
            case R.id.go_back_to_index_admin:
                // the admin has choosen to go back to the index admin:
                startActivity(new Intent(activity_manage_users.this, activity_index_admin.class));
                finish();
                break;
            // radio group options for search
            case R.id.search_by_email:
                fieldToSearchBy = SharedPreferencesKeys.EMAIL;
                search_bar.setQueryHint("search by email");
                options = cloudActivities.displayUsersByQuery(activity_manage_users.this, search_bar.getQuery().toString(), fieldToSearchBy);
                if (options != null) {
                    userAdapter = new UserAdapter(options);
                    userAdapter.startListening();
                    user_recyclerView.setAdapter(userAdapter);
                }
                break;
            case R.id.search_by_firstName:
                fieldToSearchBy = "firstName";
                search_bar.setQueryHint("search by first name");
                options = cloudActivities.displayUsersByQuery(activity_manage_users.this, search_bar.getQuery().toString(), fieldToSearchBy);
                if (options != null) {
                    userAdapter = new UserAdapter(options);
                    userAdapter.startListening();
                    user_recyclerView.setAdapter(userAdapter);
                }
                break;
        }
    }
}