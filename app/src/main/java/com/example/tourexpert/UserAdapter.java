package com.example.tourexpert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * this class is responsible for the activity_manage_users
 * recyclerView and all the operations done on it
 */
public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.myViewHolder> {

    Context parent_activity;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserAdapter(@NonNull FirebaseRecyclerOptions<User> options) {
        super(options);
        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
    }

    @Override
    protected void onBindViewHolder(@NonNull UserAdapter.myViewHolder holder, int position, @NonNull User model) {
        holder.user_email_address.setText("Email: " + model.getEmail());
        holder.user_name.setText("Name: " + model.getFirstName() + ", " + model.getLastName());
        holder.user_phone_number.setText("Phone: " + model.getPhone());

        // show the appropriate activate / de-activate button
        // based on the specific user status
        if (model.getStatus()) {
            holder.deactivate_user.setVisibility(View.VISIBLE);
            holder.activate_user.setVisibility(View.GONE);
        } else {
            holder.activate_user.setVisibility(View.VISIBLE);
            holder.deactivate_user.setVisibility(View.GONE);
        }

        // disable the de-activate / activate button for the shown current user:
        // check the current logged in user key with his model shown in the RecyclerView
        if (model.key.equals(parent_activity.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE))) {
            holder.deactivate_user.setVisibility(View.GONE);
            holder.activate_user.setVisibility(View.GONE);
        }

        // show an avatar based on the specific user type:
        int avatar = 0;
        switch (model.getType()) {
            case UserTypeKeys.ADMIN:
                avatar = R.drawable.profile_admin;
                break;
            case UserTypeKeys.EMPLOYEE:
                avatar = R.drawable.profile_employee;
                break;
            case UserTypeKeys.USER:
                avatar = R.drawable.profile_user;
                break;
        }
        holder.user_portrait.setImageResource(avatar);

        // we open a map that we will be using to change
        // the user status:
        Map<String, Object> status_update = new HashMap<String, Object>();

        // region Button click handlers
        // when the admin has choosen to activate a user:
        holder.activate_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                // put true status:
                status_update.put("status", true);

                if (appGeneralActivities.isConnectedToInternet(parent_activity)) {
                    if (appGeneralActivities.isConnectedToDatabase()) {

                        cloudActivities.updateUserStatus(parent_activity, model.getKey(), status_update);

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
                            "operation failed,no internet connection was found, please try again later",
                            R.drawable.warning
                    );
                }
            }
        });

        // when the admin has choosen the de-active a user:
        holder.deactivate_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                // put false status:
                status_update.put("status", false);

                if (appGeneralActivities.isConnectedToInternet(parent_activity)) {
                    if (appGeneralActivities.isConnectedToDatabase()) {

                        cloudActivities.updateUserStatus(parent_activity, model.getKey(), status_update);

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
        // endregion Button click handlers
    }

    @NonNull
    @Override
    public UserAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                       int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_view, parent, false);
        this.parent_activity = parent.getContext();
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        protected TextView user_email_address, user_name, user_phone_number;
        protected Button activate_user, deactivate_user;
        protected ImageView user_portrait;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            user_email_address = itemView.findViewById(R.id.user_email_address);
            user_name = itemView.findViewById(R.id.user_name);
            user_phone_number = itemView.findViewById(R.id.user_phone_number);

            activate_user = itemView.findViewById(R.id.activate_user);
            deactivate_user = itemView.findViewById(R.id.deactivate_user);

            user_portrait = itemView.findViewById(R.id.user_portrait);
        }
    }
}
