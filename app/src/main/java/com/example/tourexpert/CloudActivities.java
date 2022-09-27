package com.example.tourexpert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * this class contains all the functions that directly interact with the FireBase DataBase
 * this Class HAS TO EXTEND AppCompatActivity to work !!!
 */
public class CloudActivities extends AppCompatActivity {

    /**
     * function gets a source Activity from which we will be calling this function
     * function will try to login the user, and redirect the user from the source Activity
     * to the destination which is determined by the user type
     * if the task is not successful, the user will be redirected to the regular login activity
     *
     * @param source Activity from which this function is being called
     */
    public void attemptRelogin(Activity source) {
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // user details
        String email = shared.getString(SharedPreferencesKeys.EMAIL, SharedPreferencesKeys.DEFAULT_VALUE);
        String password = shared.getString(SharedPreferencesKeys.PASSWORD, SharedPreferencesKeys.DEFAULT_VALUE);
        String type = shared.getString(SharedPreferencesKeys.TYPE, SharedPreferencesKeys.DEFAULT_VALUE);
        String uid = shared.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);

        // fail intent will occur when we was unsuccessful at the re-login task
        Intent fail_intent = new Intent(source, activity_login.class);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // initialize the FireBase authentication Object:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS).child(uid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            User current_user = snapshot.getValue(User.class);
                                            // only if the user account is not disabled, we can continue:
                                            // else we redirect the user to the login activity:
                                            if (current_user.getStatus()) {
                                                // the re-login is successful, now determine
                                                // which index will the user be redirected:
                                                Intent success_intent = null;

                                                switch (type) {
                                                    case UserTypeKeys.USER:
                                                        success_intent = new Intent(source, activity_index_user.class);
                                                        break;
                                                    case UserTypeKeys.EMPLOYEE:
                                                        success_intent = new Intent(source, activity_index_employee.class);
                                                        break;
                                                    case UserTypeKeys.ADMIN:
                                                        success_intent = new Intent(source, activity_index_admin.class);
                                                        break;
                                                }

                                                mAuth.signOut();
                                                source.startActivity(success_intent);
                                                source.finish();
                                            } else {
                                                mAuth.signOut();
                                                // user account is disabled
                                                source.startActivity(fail_intent);
                                                source.finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            // couldn't read the user object from database,
                                            // cant determine type and status
                                            // go to the login page:
                                            mAuth.signOut();
                                            source.startActivity(fail_intent);
                                            source.finish();
                                        }
                                    });
                        } else {
                            mAuth.signOut();
                            // task was not successful
                            source.startActivity(fail_intent);
                            source.finish();
                        }
                    }
                });

            } else {
                // no database connection
                source.startActivity(fail_intent);
                source.finish();
            }
        } else {
            // no internet connection
            source.startActivity(fail_intent);
            source.finish();
        }
    }

    /**
     * function will try and login the user after it checks
     * if his email address is verified, depends on his type he will be
     * redirected to the appropriate activity, if task is interrupted
     * the user will be alerted with the reason for
     *
     * @param source       Activity from which this function will be called
     * @param email        email address of the user
     * @param password     password of the user
     * @param progressBar  the Activity progress bar that will spin during this task
     * @param stayLoggedIn the checkbox for stay logged in status
     */
    public void login(Activity source, String email, String password, View progressBar, boolean stayLoggedIn) {

        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // only if the user is connected to internet and there is access to the Database, attempt
        // to login the user to the appropriate index
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // initialize the FireBase authentication Object:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // check if the user that tries to login has their email verified
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            if (user.isEmailVerified()) {
                                // the user is verified:
                                // get the user object and check his type and status, redirect accordingly:
                                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                // fetch the current user Object and its type from the users collection
                                                User current_user = snapshot.getValue(User.class);

                                                // this user is disabled, alert the user
                                                if (!current_user.getStatus()) {
                                                    appGeneralActivities.displayAlertDialog(source, "login attempt failed", "login attempt failed because " +
                                                            "of the following exception:\n'this account is disabled'", R.drawable.error);
                                                    // stop spinning the progress bar
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                } else {
                                                    // user is not disabled
                                                    String type = current_user.getType();
                                            /*
                                               use Shared preferences to remember the: isLogged, type, email, password and user identifier
                                               for the stay logged in feature
                                               which will let users that has checked the "stay log in"
                                               CheckBox to automatically re-login into their dashboard

                                               the Shared preferences is a Context.MODE_PRIVATE so only
                                               this application has access to read from this file
                                             */
                                                    editor.putString(SharedPreferencesKeys.UID, snapshot.getKey());
                                                    editor.putString(SharedPreferencesKeys.EMAIL, email);
                                                    editor.putString(SharedPreferencesKeys.PASSWORD, password);
                                                    editor.putString(SharedPreferencesKeys.TYPE, type);

                                                    if (stayLoggedIn) {
                                                        editor.putBoolean(SharedPreferencesKeys.LOGGED_IN, true);
                                                    } else { // user does not want to auto log in
                                                        editor.putBoolean(SharedPreferencesKeys.LOGGED_IN, false);
                                                    }
                                                    editor.apply();

                                                    // initialize the default intent, which will stay in login activity
                                                    Intent intent = new Intent(source, activity_login.class);

                                                    // we redirect the user by his type into the appropriate index activity:
                                                    switch (type) {
                                                        case UserTypeKeys.USER:
                                                            intent = new Intent(source, activity_index_user.class);
                                                            break;
                                                        case UserTypeKeys.EMPLOYEE:
                                                            intent = new Intent(source, activity_index_employee.class);
                                                            break;
                                                        case UserTypeKeys.ADMIN:
                                                            intent = new Intent(source, activity_index_admin.class);
                                                            break;
                                                    }

                                                    mAuth.signOut();
                                                    source.startActivity(intent);
                                                    source.finish();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                                // Reading data from the database failed
                                                appGeneralActivities.displayAlertDialog(source, "login attempt failed", "login attempt failed because " +
                                                        "of the following exception:\n'" + error.getDetails() + "'", R.drawable.error);
                                                // stop spinning the progress bar
                                                progressBar.setVisibility(View.INVISIBLE);
                                                mAuth.signOut();
                                            }
                                        });
                            } else {

                                // the user is not verified
                                // send the user an Email Verification
                                // and alert the user that an email verification has been sent
                                user.sendEmailVerification();
                                appGeneralActivities.displayAlertDialog(source, "email verification required",
                                        "please verify your email address, an email verification was sent", R.drawable.information);
                            }
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            mAuth.signOut();
                        } else {
                            // login failed
                            appGeneralActivities.displayAlertDialog(source, "login attempt failed", "login attempt failed because " +
                                    "of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            mAuth.signOut();
                        }
                    }
                });
            } else {
                // no database connection
                appGeneralActivities.displayAlertDialog(source, "login attempt failed", "login attempt failed because " +
                        "of the following exception:\n'database is currently unavailable, please try again later'", R.drawable.warning);
                // stop spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no network connection
            appGeneralActivities.displayAlertDialog(source, "login attempt failed", "login attempt failed because " +
                    "of the following exception:\n'no network connection was found, please try again later'", R.drawable.warning);
            // stop spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * this function will try to register a new user
     * by his email and password, and the given user type which
     * currently can be one of the following:
     * 1. user - when a user tries to register himself
     * 2. employee - when an admin tries to register a new employee account
     * and will try to add this User Object to the
     * users collection
     *
     * @param source      Activity from which this function will be called
     * @param email       email address of the user
     * @param firstName   first name of the user
     * @param lastName    last name of the user
     * @param phone       phone number of the user
     * @param password    the user password
     * @param progressBar the Activity progress bar that will spin during this task
     * @param user_type   the user type that will be assigned to the currently registered user
     */
    public void registerUser(Activity source, String email, String firstName, String lastName, String phone,
                             String password, View progressBar, String user_type) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // only if the user is connected to inter and there is access to the Database, attempt
        // to create a user
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // initialize the FireBase authentication Object:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String finalPhone = phone;
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            // creating User Object from the user's input
                            // this Object will be appended to the users collection
                            User user = new User(email, firstName, lastName, finalPhone, user_type, true, uid);

                            FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS)
                                    .child(uid)
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // successful user registration and
                                    // added to the Collection of users
                                    if (task.isSuccessful()) {
                                        appGeneralActivities.displayAlertDialog(source, user_type + " registration success",
                                                user_type + " has been registered successfully", R.drawable.information);
                                        // stop spinning the progress bar
                                        progressBar.setVisibility(View.INVISIBLE);
                                        mAuth.signOut();

                                    } else {
                                        // user object was not added to the users collection
                                        appGeneralActivities.displayAlertDialog(source, user_type + " registration failed", user_type + " has failed to be registered because " +
                                                "of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                        // stop spinning the progress bar
                                        progressBar.setVisibility(View.INVISIBLE);
                                        mAuth.signOut();
                                    }
                                }
                            });
                        } else {
                            // create user failed
                            appGeneralActivities.displayAlertDialog(source, user_type + " registration failed", user_type + " has failed to be registered because " +
                                    "of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            mAuth.signOut();
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, user_type + " registration failed", user_type + " has failed to be registered because " +
                        "of the following exception:\n'database is currently unavailable, please try again later'", R.drawable.warning);
                // stop spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no network connection
            appGeneralActivities.displayAlertDialog(source, user_type + " registration failed", user_type + " has failed to be registered because " +
                    "of the following exception:\n'no network connection was found, please try again later'", R.drawable.warning);
            // stop spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function receives the email address of a user
     * and will try to send the user an email which he will
     * be able to reset his password with
     *
     * @param source      Activity from which this function will be called
     * @param email       the email address of the account to send password reset request
     * @param progressBar the Activity progress bar that will spin during this task
     */
    public void sendResetPasswordEmailRequest(Activity source, String email, View progressBar) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // initialize the FireBase authentication Object:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // the reset password request has been successfully send
                        // to the user email address
                        if (task.isSuccessful()) {
                            appGeneralActivities.displayAlertDialog(source, "password reset", "an password reset request has been sent to the given email address", R.drawable.information);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            mAuth.signOut();

                        } else {
                            // the reset password request was not sent !!
                            appGeneralActivities.displayAlertDialog(source, "reset password failed", "reset password failed because " +
                                    "of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                            mAuth.signOut();
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "reset password failed", "reset password failed because " +
                        "of the following exception:\n'database is currently unavailable, please try again later'", R.drawable.warning);
                // stop spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no network connection
            appGeneralActivities.displayAlertDialog(source, "reset password failed", "reset password failed because " +
                    "of the following exception:\n'no network connection was found, please try again later'", R.drawable.warning);
            // stop spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * function will try and sign-out() the FireBaseUser and will also
     * clear any data in the SharedPreferencesKeys "general" of any saved Account data
     * ***** THIS FUNCTION IS COMMON BETWEEN ALL USER INTERFACES *****
     *
     * @param source Activity from which this function will be called
     */
    public void logout(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // initialize the FireBase authentication Object:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                AlertDialog.Builder build = new AlertDialog.Builder(source);
                build.setMessage("Are you sure you want to logout ?")
                        .setIcon(R.drawable.information)
                        .setTitle("logout")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // sign the user out, destroy the FireBase Auth session
                                mAuth.signOut();
                                // and clear the saved data for user re-login
                                editor.clear();
                                editor.apply();

                                source.startActivity(new Intent(source, activity_login.class));
                                source.finish();
                            }
                        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "logout failed", "logout attempt failed because " +
                        "of the following exception:\n'database is currently unavailable, please try again later'", R.drawable.warning);
            }
        } else {
            // no network connection
            appGeneralActivities.displayAlertDialog(source, "logout failed", "logout attempt failed because " +
                    "of the following exception:\n'no network connection was found, please try again later'", R.drawable.warning);
        }
    }

    /**
     * function will attempt and display a welcome message
     * with the current user name onto the given TextView
     * if it is unable to read the user's name from the database for some reason
     * we will alert the user upon entering the source activity
     *
     * @param source          Activity from which this function will be called
     * @param welcome_message a TextView which the welcome message will be displayed upon
     */
    public void greetUser(Activity source, TextView welcome_message) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                String user_identifier = shared.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);

                FirebaseDatabase.getInstance()
                        .getReference(DataBaseCollectionsKeys.USERS)
                        .child(user_identifier).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User current_user = snapshot.getValue(User.class);
                        // extract the current user full name and display it:
                        welcome_message.setText("Welcome, " + current_user.firstName + " " + current_user.getLastName());
                        welcome_message.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // couldn't read data from the user's collection:
                        appGeneralActivities.displayAlertDialog(source, "unexpected error", "an unexpected error has accord: '" + error.getMessage() + "'", R.drawable.error);
                    }
                });
            } else {
                // no connection to database:
                appGeneralActivities.displayAlertDialog(source, "database is unavailable",
                        "The database is currently unavailable, basic activities " +
                                "might be unavailable until the database will be available again", R.drawable.warning);
            }
        } else {
            // no connection to a network:
            appGeneralActivities.displayAlertDialog(source, "no network found",
                    "no network connection was found, basic activities " +
                            "might be unavailable until a network connection will be established", R.drawable.warning);
        }
    }

    /**
     * This function mainly used in the activity_user_profile
     * will mainly occupy the fields that will display the user details
     * such as full name of the user, his email address, and EditText views that
     * will contain update-able user details
     *
     * @param source            Activity from which this function will be called
     * @param name_for_display  TextView that will display the current user full name
     * @param email_for_display TextView that will display the current user email address
     * @param first_name        EditText that will display the current user first name
     * @param last_name         EditText that will display the current user last name
     * @param phone             EditText that will display the current user phone number
     */
    public void displayUserDetails(Activity source, TextView name_for_display, TextView email_for_display, EditText first_name,
                                   EditText last_name, EditText phone) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                String user_identifier = shared.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);

                FirebaseDatabase.getInstance()
                        .getReference(DataBaseCollectionsKeys.USERS)
                        .child(user_identifier).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User current_user = snapshot.getValue(User.class);

                        // extract the user details and display them appropriately:
                        name_for_display.setText(current_user.getFirstName() + " " + current_user.getLastName());
                        email_for_display.setText(current_user.getEmail());

                        first_name.setText(current_user.getFirstName());
                        last_name.setText(current_user.getLastName());
                        // display the phone without the country code:
                        String phone_num = current_user.getPhone();
                        phone_num = phone_num.substring(phone_num.indexOf('-') + 1);
                        phone.setText(phone_num);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // couldn't read data from the user's collection:
                        appGeneralActivities.displayAlertDialog(source, "unexpected error", "an unexpected error has accord: '" + error.getMessage() + "'", R.drawable.error);
                    }
                });
            } else {
                // no connection to database:
                appGeneralActivities.displayAlertDialog(source, "database is unavailable", "The database is currently unavailable, basic activities " +
                        "might be unavailable until the database will be available again", R.drawable.warning);
            }
        } else {
            // no connection to a network:
            appGeneralActivities.displayAlertDialog(source, "no network found", "no network connection was found, basic activities " +
                    "might be unavailable until a network connection will be established", R.drawable.warning);
        }
    }

    /**
     * function will read the first name, last name and phone number
     * that the user is interested to update
     * and will attempt to update these details for the currently
     * signed in user
     *
     * @param source      Activity from which this function will be called
     * @param firstName   first name of the user
     * @param lastName    last name of the user
     * @param phone       phone number of the user
     * @param progressBar a progress bar that will spin while the process
     */
    public void updateUserDetails(Activity source, String firstName, String lastName, String phone, View progressBar) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);

        // prepare the Map of the fields that we will update for the user Object
        // inside users Collection of the database:
        HashMap<String, Object> details_to_update = new HashMap<String, Object>();
        // *** key - HAS TO BE IDENTICAL TO THE FIELD KEY IN THE DATABASE !!! ***
        details_to_update.put("firstName", firstName);
        details_to_update.put("lastName", lastName);
        details_to_update.put("phone", phone);

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                String user_identifier = shared.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);
                // and try to update the fields that we have specified in our HashMap:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS).child(user_identifier)
                        .updateChildren(details_to_update).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // we successfully updated the user object inside
                            // the users Collection:
                            appGeneralActivities.displayAlertDialog(source, "update successful", "User details was successfully updated", R.drawable.information);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);

                        } else {
                            // we failed to update the user object:
                            appGeneralActivities.displayAlertDialog(source, "update failed", "User details update attempt failed because " +
                                    "of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "update failed", "The database is currently unavailable, please try again later", R.drawable.error);
                // stop spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "update failed", "No network connection was found, please try again later", R.drawable.error);
            // stop spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function gets the city key to update, will read the city name, country name
     * that the employee is interested to update the city he choose
     * and will attempt to updated these details for the city
     *
     * @param source      context of the Activity from which this function will be called
     * @param cityName    new city name
     * @param countryName new country name
     * @param progressBar a progress bar that will spin while the process
     * @param key         the city unique node key
     */
    public void updateCityDetails(Context source, String cityName, String countryName, View progressBar, String key) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // open a HashMap that will contain key, value for updates:
        Map<String, Object> details_to_update = new HashMap<String, Object>();
        details_to_update.put("cityName", cityName);
        details_to_update.put("countryName", countryName);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                // check if the name the employee gave to the city is unique !!!
                DatabaseReference cities_collection = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.CITIES);

                // check if a city name like this already exists in the cities collection:
                cities_collection.orderByChild("cityName").equalTo(cityName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // a city by the name we want to give to our updated city does exist
                        // only update the country name:
                        if (snapshot.exists()) {
                            details_to_update.remove("cityName");
                        }

                        // attempt and update the city by its key:
                        cities_collection
                                .child(key)
                                .updateChildren(details_to_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // the city details was updated successfully:
                                    appGeneralActivities.displayAlertDialog(source, "update city successful", "the city was successfully updated", R.drawable.information);
                                    // STOP spinning the progress bar
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    // the city details was not updated (connectivity issues while the task was running:
                                    appGeneralActivities.displayAlertDialog(source, "update city failed",
                                            "update city failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                    // STOP spinning the progress bar
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // there was an error updating the city:
                        appGeneralActivities.displayAlertDialog(source, "update city failed",
                                "update city failed because of the following exception:\n'" + error.getMessage() + "'", R.drawable.error);
                        // STOP spinning the progress bar
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "update city failed",
                        "update city failed, the database is currently unavailable, Please try again later", R.drawable.error);
                // STOP spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to the internet
            appGeneralActivities.displayAlertDialog(source, "update city failed",
                    "update city failed, no network connection was found, Please try again later", R.drawable.error);
            // STOP spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function gets source activity, flightKey, progressBar and a map that containing all the fields to update
     * and will try and update the flight :
     *
     * @param source            Activity from which this function will be called
     * @param flightKey         primary key of the flight to update
     * @param details_to_update a map containing the details to update
     * @param progressBar       the progress bar that will spin during this task
     */
    public void updateFlightDetails(Activity source, String flightKey, Map<String, Object> details_to_update, View progressBar) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.FLIGHTS;

        // start spinning the progress bar:
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get the reference to the specific flight
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS)
                        .child(cityKey)
                        .child(category)
                        .child(flightKey)
                        .updateChildren(details_to_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // the flight details was updated successfully:
                            appGeneralActivities.displayAlertDialog(source, "update flight successful", "the flight was successfully updated", R.drawable.information);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            // the flight details was not updated (connectivity issues while the task was running:
                            appGeneralActivities.displayAlertDialog(source, "update flight failed",
                                    "update flight failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "update city failed",
                        "update flight failed, the database is currently unavailable, Please try again later", R.drawable.error);
                // STOP spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to the internet
            appGeneralActivities.displayAlertDialog(source, "update city failed",
                    "update flight failed, no network connection was found, Please try again later", R.drawable.error);
            // STOP spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function gets source activity, hotelKey, progressBar and a map that containing all the fields to update
     * and will try and update the hotel:
     *
     * @param source            Activity from which this function will be called
     * @param hotelKey          primary key of the hotel to update
     * @param details_to_update a map containing the details to update
     * @param progressBar       the progress bar that will spin during this task
     */
    public void updateHotelDetails(Activity source, String hotelKey, Map<String, Object> details_to_update, View progressBar) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.HOTELS;

        // start spinning the progress bar:
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get the reference to the specific Hotel
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS)
                        .child(cityKey)
                        .child(category)
                        .child(hotelKey)
                        .updateChildren(details_to_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // the Hotel details was updated successfully:
                            appGeneralActivities.displayAlertDialog(source, "update hotel successful", "the hotel was successfully updated", R.drawable.information);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            // the hotel details was not updated (connectivity issues while the task was running:
                            appGeneralActivities.displayAlertDialog(source, "update hotel failed",
                                    "update hotel failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "update hotel failed",
                        "update hotel failed, the database is currently unavailable, Please try again later", R.drawable.error);
                // STOP spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to the internet
            appGeneralActivities.displayAlertDialog(source, "update hotel failed",
                    "update hotel failed, no network connection was found, Please try again later", R.drawable.error);
            // STOP spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function gets source activity, attractionKey, progressBar and a map that containing all the fields to update
     * and will try and update the attraction:
     *
     * @param source            Activity from which this function will be called
     * @param attractionKey     primary key of the attraction to update
     * @param details_to_update a map containing the details to update
     * @param progressBar       the progress bar that will spin during this task
     */
    public void updateAttractionDetails(Activity source, String attractionKey, Map<String, Object> details_to_update, View progressBar) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.ATTRACTIONS;

        // start spinning the progress bar:
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get the reference to the specific attraction
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS)
                        .child(cityKey)
                        .child(category)
                        .child(attractionKey)
                        .updateChildren(details_to_update).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // the attraction details was updated successfully:
                            appGeneralActivities.displayAlertDialog(source, "update attraction successful", "the attraction was successfully updated", R.drawable.information);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            // the attraction details was not updated (connectivity issues while the task was running:
                            appGeneralActivities.displayAlertDialog(source, "update attraction failed",
                                    "update attraction failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                            // STOP spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "update attraction failed",
                        "update attraction failed, the database is currently unavailable, Please try again later", R.drawable.error);
                // STOP spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to the internet
            appGeneralActivities.displayAlertDialog(source, "update attraction failed",
                    "update attraction failed, no network connection was found, Please try again later", R.drawable.error);
            // STOP spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * returns a FirebaseRecyclerOptions using a query to display all users
     *
     * @param source Activity from which this function will be called
     * @return a FirebaseRecyclerOptions using a query to display all users
     */
    @SuppressLint("LongLogTag")
    public FirebaseRecyclerOptions<User> optionsQueryToDisplayAllUsers(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        // if and only if we have the necessary connections
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child(DataBaseCollectionsKeys.USERS)
                                , User.class
                        ).build();
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "the database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "no network found",
                    "no network connection could be found, check your internet connection, please try again later", R.drawable.error);
        }
        return null;
    }

    /**
     * function gets a source activity, a text from the user to search by
     * and the field of a user to search by (email, firstName...)
     * and will a query to display all users that match the user text
     *
     * @param source          Activity from which this function will be called
     * @param newText         a text input from the user
     * @param fieldToSearchBy the field of a user to search by (email, firstName...)
     * @return return a query to display all users that match the user text
     */
    @SuppressLint("LongLogTag")
    public FirebaseRecyclerOptions<User> displayUsersByQuery(Activity source, String newText, String fieldToSearchBy) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        // if any only if we have the necessary connections
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(
                                FirebaseDatabase.getInstance().getReference().child(DataBaseCollectionsKeys.USERS)
                                        .orderByChild(fieldToSearchBy)
                                        .startAt(newText)
                                        .endAt(newText + "~")
                                , User.class
                        )
                        .build();
            }
        }
        return null;
    }

    /**
     * function gets a source activity, a text from the user to search by
     * and the field of a city to search by (citName,countryName...)
     * and will return a query to display all cities that match the user text
     *
     * @param source          Activity from which this function will be called
     * @param newText         a text input from the user
     * @param fieldToSearchBy the field of a city to search by (citName,countryName...)
     * @return return a query to display all cities that match the user text
     */
    public FirebaseRecyclerOptions<City> displayCitiesByQuery(Activity source, String newText, String fieldToSearchBy) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // if any only if we have the necessary connections
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<City>()
                        .setQuery(
                                FirebaseDatabase.getInstance().getReference().child(DataBaseCollectionsKeys.CITIES)
                                        .orderByChild(fieldToSearchBy)
                                        .startAt(newText)
                                        .endAt(newText + "~")
                                , City.class
                        ).build();
            }
        }
        return null;
    }

    /**
     * functions gets a context which is the source Activity context
     * a node key of the user to change his status
     * and a map that contains a status key which its value will be true or false
     * depending which button was pressed
     * function will try and access the user node from users collection
     * associated with the given key and will try to update its status value
     *
     * @param source        a context which is the source Activity context
     * @param key           the user key node
     * @param status_update a map that contains a status key which its value will be true or false
     */
    public void updateUserStatus(Context source, String key, Map<String, Object> status_update) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // access the user node to update by the given key:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS).child(key)
                        .updateChildren(status_update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // user status update was successful:
                                    appGeneralActivities.displayAlertDialog(source, "operation successful", "user status was successfully updated", R.drawable.information);
                                } else {
                                    // user status was not updated:
                                    appGeneralActivities.displayAlertDialog(source, "operation failed",
                                            "operation has failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                }
                            }
                        });
            }
        }
    }

    /**
     * functions gets a context which is the source Activity context
     * a node key of the city to change its status
     * and a map that contains a status key which its value will be true or false
     * depending which button was pressed
     * function will try and access the city node from cities collection
     * associated with the given key and will try to update its status value
     *
     * @param source        a context which is the source Activity context
     * @param key           the city key node
     * @param status_update a map that contains a status key which its value will be true or false
     */
    public void updateCityStatus(Context source, String key, Map<String, Object> status_update) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // access the city node to update by the given key:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.CITIES)
                        .child(key).updateChildren(status_update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // city status update was successful:
                                    appGeneralActivities.displayAlertDialog(source, "operation successful", "city status was successfully updated", R.drawable.information);
                                } else {
                                    // city status was not updated:
                                    appGeneralActivities.displayAlertDialog(source, "operation failed",
                                            "operation has failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                }
                            }
                        });
            }
        }
    }

    /**
     * function gets a source activity, city name
     * and country name and a progress bar for the add city task
     * will try and add a city object to the "cities" collection and will
     * notify the user of the task result
     *
     * @param source      Activity from which this function will be called
     * @param cityName    city name to add
     * @param countryName country name to add
     * @param progressBar a progress bar for the add city task
     */
    public void addCity(Activity source, String cityName, String countryName, View progressBar) {

        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the progress bar
        progressBar.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get the reference to the cities node:
                DatabaseReference cities_collection = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.CITIES);

                // check if a city like this already exists in the cities collection:
                cities_collection.orderByChild("cityName").equalTo(cityName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) { // the city does not exist, we can try and add it to the collection:
                            // generate a unique key:
                            String key = cities_collection.push().getKey();

                            // create the city object, each city will by default have true(active) status and 0 on all statistics:
                            City city = new City(cityName, countryName, true, key);

                            // access the city collection and try and add the city object
                            cities_collection.child(key).setValue(city).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        appGeneralActivities.displayAlertDialog(source, "operation successful", "city was successfully added", R.drawable.information);

                                        // stop spinning the progress bar
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        // city was not added to the collection, an error has occurred:
                                        appGeneralActivities.displayAlertDialog(source, "operation failed",
                                                "operation failed because of the following exception:\n'" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                        // stop spinning the progress bar
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        } else {
                            // the city already exists:
                            appGeneralActivities.displayAlertDialog(source, "operation canceled", "operation canceled, the city '" + cityName + "' already exists", R.drawable.warning);
                            // stop spinning the progress bar
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // connectivity issues caused the application to be unable to access the city collection:
                        appGeneralActivities.displayAlertDialog(source, "operation canceled", "operation canceled because of the following exception:\n'" + error.getMessage() + "'",
                                R.drawable.error);
                        // stop spinning the progress bar
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                // no connection to database:
                appGeneralActivities.displayAlertDialog(source, "operation failed", "operation failed, the database is currently unavailable, please try again later", R.drawable.error);
                // stop spinning the progress bar
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            // no connection to internet:
            appGeneralActivities.displayAlertDialog(source, "operation failed", "operation failed, no network connection was found, please try again later", R.drawable.error);
            // stop spinning the progress bar
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * returns a FireBaseRecyclerOptions using a query to display all cities
     *
     * @param source Activity from which this function will be called
     * @return a FirebaseRecyclerOptions using a query to display all cities
     */
    @SuppressLint("LongLogTag")
    public FirebaseRecyclerOptions<City> optionsQueryToDisplayAllCities(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        // if and only if we have the necessary connections
        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<City>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child(DataBaseCollectionsKeys.CITIES), City.class).build();
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "the database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "no network found", "no network connection could be found, check your internet connection, please try again later",
                    R.drawable.error);
        }
        return null;
    }

    /**
     * returns a FireBaseRecyclerOptions using a query to display All Flights
     * by user type,show all flights, even invalid flights
     *
     * @param source Activity from which this function will be called
     * @return a FirebaseRecyclerOptions using a query to display All Flights
     */
    public FirebaseRecyclerOptions<Flight> optionsQueryToDisplayAllFlights(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String city = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.FLIGHTS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // show all the flights, even those that are invalid
                return new FirebaseRecyclerOptions.Builder<Flight>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(city)
                                .child(category), Flight.class).build();
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "the database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "no network found", "no network connection could be found, check your internet connection, please try again later",
                    R.drawable.error);
        }
        return null;
    }

    /**
     * returns a FireBaseRecyclerOptions using a query to display All V Hotels (
     *
     * @param source Activity from which this function will be called
     * @return a FirebaseRecyclerOptions using a query to display All Hotels
     */
    public FirebaseRecyclerOptions<Hotel> optionsQueryToDisplayAllHotels(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String city = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.HOTELS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // show all the hotels, even those that are invalid
                return new FirebaseRecyclerOptions.Builder<Hotel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(city)
                                .child(category), Hotel.class).build();
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "the database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "no network found", "no network connection could be found, check your internet connection, please try again later",
                    R.drawable.error);
        }
        return null;
    }

    /**
     * returns a FireBaseRecyclerOptions using a query to display All Attractions
     * by user type, show all Attractions, even invalid Attractions
     *
     * @param source Activity from which this function will be called
     * @return a FirebaseRecyclerOptions using a query to display All Attractions
     */
    public FirebaseRecyclerOptions<Attraction> optionsQueryToDisplayAllAttractions(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String city = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.ATTRACTIONS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // show all the attractions, even those that are invalid
                return new FirebaseRecyclerOptions.Builder<Attraction>()
                        .setQuery(FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(city)
                                .child(category), Attraction.class).build();
            } else {
                // no connection to database
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "the database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // no connection to internet
            appGeneralActivities.displayAlertDialog(source, "no network found", "no network connection could be found, check your internet connection, please try again later",
                    R.drawable.error);
        }
        return null;
    }

    /**
     * function gets source activity, the query from the user
     * and the field to search by, will return a query that will
     * display all flights that answer that query by user input
     *
     * @param source          Activity from which this function will be called
     * @param newText         input from the user
     * @param fieldToSearchBy field to search by
     * @return a query that will display all flights that answer that query by user input
     */
    public FirebaseRecyclerOptions<Flight> displayFlightsByQuery(Activity source, String newText, String fieldToSearchBy) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.FLIGHTS;
        Query query = null;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // if the field to search by was price, try and parse it into an int and then create a query
                if (fieldToSearchBy.equals("price")) {
                    int ticketPrice = 0;
                    try {
                        ticketPrice = Integer.parseInt(newText);
                    } catch (Exception e) {
                        return null;
                    }

                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .endAt(ticketPrice);

                } else if (fieldToSearchBy.equals("destination")) {
                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .startAt(newText)
                            .endAt(newText + "~");
                }

                return new FirebaseRecyclerOptions.Builder<Flight>().setQuery(query, Flight.class).build();
            }
        }
        return null;
    }

    /**
     * function gets source activity, the query from the user
     * and the field to search by, will return a query that will
     * display all Hotels that answer that query by user input
     *
     * @param source          Activity from which this function will be called
     * @param newText         input from the user
     * @param fieldToSearchBy field to search by
     * @return a query that will display all Hotels that answer that query by user input
     */
    public FirebaseRecyclerOptions<Hotel> displayHotelsByQuery(Activity source, String newText, String fieldToSearchBy) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.HOTELS;
        Query query = null;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                // if the field to search by was star rating, try and parse it into an int and then create a query
                if (fieldToSearchBy.equals("stars")) {
                    int stars = 0;
                    try {
                        stars = Integer.parseInt(newText);
                    } catch (Exception e) {
                        return null;
                    }

                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .startAt(stars);

                } else if (fieldToSearchBy.equals("price")) {
                    int price = 0;
                    try {
                        price = Integer.parseInt(newText);
                    } catch (Exception e) {
                        return null;
                    }

                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .endAt(price);
                }

                return new FirebaseRecyclerOptions.Builder<Hotel>().setQuery(query, Hotel.class).build();
            }
        }
        // there was no internet / database connection:
        return null;
    }

    /**
     * function gets source activity, the query from the user
     * and the field to search by, will return a query that will
     * display all attractions that answer that query by user input
     *
     * @param source          Activity from which this function will be called
     * @param newText         input from the user
     * @param fieldToSearchBy field to search by
     * @return a query that will display all attractions that answer that query by user input
     */
    public FirebaseRecyclerOptions<Attraction> displayAttractionsByQuery(Activity source, String newText, String fieldToSearchBy) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        SharedPreferences shared_userChoice = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);

        String cityKey = shared_userChoice.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
        String category = DataBaseCollectionsKeys.ATTRACTIONS;
        Query query = null;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // if the field to search by was price, try and parse it into an int and then create a query
                if (fieldToSearchBy.equals("price")) {
                    int ticketPrice = 0;
                    try {
                        ticketPrice = Integer.parseInt(newText);
                    } catch (Exception e) {
                        return null;
                    }

                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .endAt(ticketPrice);

                } else if (fieldToSearchBy.equals("type")) {
                    query = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category)
                            .orderByChild(fieldToSearchBy)
                            .startAt(newText)
                            .endAt(newText + "~");
                }
                return new FirebaseRecyclerOptions.Builder<Attraction>().setQuery(query, Attraction.class).build();
            }
        }
        return null;
    }

    /**
     * returns FireBaseRecyclerOptions to display and listen to
     * all the AttractionPurchases of the current user
     *
     * @param source activity from which this method will be called
     * @return FireBaseRecyclerOptions to display and listen to all the AttractionPurchases of the current user
     */
    public FirebaseRecyclerOptions<AttractionPurchase> displayAllAttractionPurchases(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        SharedPreferences general = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        String user_key = general.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);
        String collection = DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY;
        String type = DataBaseCollectionsKeys.ATTRACTIONS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<AttractionPurchase>()
                        .setQuery(FirebaseDatabase.getInstance().getReference()
                                        .child(collection).child(user_key).child(type).orderByChild("dateOfPurchase")
                                , AttractionPurchase.class
                        ).build();
            }
        }
        return null;
    }

    /**
     * returns FireBaseRecyclerOptions to display and listen to
     * all the HotelPurchases of the current user
     *
     * @param source activity from which this method will be called
     * @return FireBaseRecyclerOptions to display and listen to all the HotelPurchases of the current user
     */
    public FirebaseRecyclerOptions<HotelPurchase> displayAllHotelPurchases(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        SharedPreferences general = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        String user_key = general.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);
        String collection = DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY;
        String type = DataBaseCollectionsKeys.HOTELS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                return new FirebaseRecyclerOptions.Builder<HotelPurchase>()
                        .setQuery(FirebaseDatabase.getInstance().getReference()
                                        .child(collection).child(user_key).child(type).orderByChild("dateOfPurchase")
                                , HotelPurchase.class
                        ).build();
            }
        }
        return null;
    }


    /**
     * returns FireBaseRecyclerOptions to display and listen to
     * all the FlightPurchases of the current user
     *
     * @param source activity from which this method will be called
     * @return FireBaseRecyclerOptions to display and listen to all the FlightPurchases of the current user
     */
    public FirebaseRecyclerOptions<FlightPurchase> displayAllFlightPurchases(Activity source) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        SharedPreferences general = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE);
        String user_key = general.getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);
        String collection = DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY;
        String type = DataBaseCollectionsKeys.FLIGHTS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                return new FirebaseRecyclerOptions.Builder<FlightPurchase>()
                        .setQuery(FirebaseDatabase.getInstance().getReference()
                                        .child(collection).child(user_key).child(type).orderByChild("dateOfPurchase")
                                , FlightPurchase.class
                        ).build();
            }
        }
        return null;
    }

    /**
     * function gets a Product object
     * will add the Product under the right city and right category (depends on the instance of the Product)
     *
     * @param source          Activity from which this function will be called
     * @param product         The Product to add
     * @param progressBar_add the progressBar that will load while the task is running
     */
    public void addProduct(Activity source, Product product, View progressBar_add) {
        SharedPreferences shared = source.getSharedPreferences(SharedPreferencesKeys.USER_CHOICE, Context.MODE_PRIVATE);
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        // start spinning the ProgressBar
        progressBar_add.setVisibility(View.VISIBLE);

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {

                String cityToAddKey = shared.getString(SharedPreferencesKeys.KEY, SharedPreferencesKeys.DEFAULT_VALUE);
                String cityToAddName = shared.getString(SharedPreferencesKeys.CITY_NAME, SharedPreferencesKeys.DEFAULT_VALUE);

                if (product instanceof Flight) {
                    String categoryToAdd = DataBaseCollectionsKeys.FLIGHTS;
                    // get reference to this city and its flights category:
                    DatabaseReference flights = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityToAddKey).child(categoryToAdd);
                    // generate a random key:
                    String flightKey = flights.push().getKey();

                    // create the flight object:
                    Flight flight = (Flight) product;
                    flight.setKey(flightKey);

                    // try and add the flight ticket's product object to the collection:
                    flights.child(flightKey).setValue(flight).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                appGeneralActivities.displayAlertDialog(source, "flight is added", "the flight was successfully added to city: '" + cityToAddName + "'", R.drawable.information);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            } else {
                                appGeneralActivities.displayAlertDialog(source, "operation failed", "flight was failed to be added because of the" + "following exception: '" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else if (product instanceof Attraction) {
                    // create the Attraction object:
                    Attraction attraction = (Attraction) product;

                    // region first upload the image file to the Cloud storage:
                    // start a progress dialog:
                    final ProgressDialog pd = new ProgressDialog(source);
                    pd.setTitle("uploading image...");
                    pd.show();

                    final String image_key = "attraction_images/" + UUID.randomUUID().toString();
                    Log.d("image key: ", image_key);

                    Uri image = null;
                    if (source instanceof activity_add_attraction)
                        image = ((activity_add_attraction) source).imageUri;

                    FirebaseStorage.getInstance().getReference()
                            .child(image_key).putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // dismiss the uploading dialog:
                            pd.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            appGeneralActivities.displayAlertDialog(source, "image failed to be uploaded", "the image failed to be uploaded, please try again", R.drawable.error);
                            // dismiss the uploading dialog:
                            pd.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pd.setMessage("Percentage: " + (int) progressPercent + "%");
                        }
                    });

                    // save the image key to the Object:
                    attraction.setImageKey(image_key);
                    // endregion upload the image file to the Cloud storage
                    String categoryToAdd = DataBaseCollectionsKeys.ATTRACTIONS;
                    // get reference to the specific city attraction collection:
                    DatabaseReference attractions = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityToAddKey).child(categoryToAdd);
                    // generate a random key:
                    String attractionKey = attractions.push().getKey();
                    attraction.setKey(attractionKey);

                    // try and add the attraction ticket's product object to the collection:
                    attractions.child(attractionKey).setValue(attraction).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                appGeneralActivities.displayAlertDialog(source, "attraction is added", "the attraction was successfully added to city: '" + cityToAddName + "'", R.drawable.information);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            } else {
                                appGeneralActivities.displayAlertDialog(source, "operation failed", "attraction was failed to be added because of the" + "following exception: '" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else if (product instanceof Hotel) {
                    Hotel hotel = (Hotel) product;

                    // region first upload the image file to the Cloud storage:
                    // start a progress dialog:
                    final ProgressDialog pd = new ProgressDialog(source);
                    pd.setTitle("uploading image...");
                    pd.show();

                    final String image_key = "hotel_images/" + UUID.randomUUID().toString();
                    Log.d("image key: ", image_key);

                    Uri image = null;
                    if (source instanceof activity_add_hotel)
                        image = ((activity_add_hotel) source).imageUri;

                    FirebaseStorage.getInstance().getReference()
                            .child(image_key).putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // dismiss the uploading dialog:
                            pd.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            appGeneralActivities.displayAlertDialog(source, "image failed to be uploaded", "the image failed to be uploaded, please try again", R.drawable.error);
                            // dismiss the uploading dialog:
                            pd.dismiss();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            pd.setMessage("Percentage: " + (int) progressPercent + "%");
                        }
                    });

                    // save the image key to the Object:
                    hotel.setImageKey(image_key);
                    // endregion upload the image file to the Cloud storage

                    // region add hotel Object to the collection:
                    String categoryToAdd = DataBaseCollectionsKeys.HOTELS;

                    // get reference to this city and its hotels category:
                    DatabaseReference hotels = FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityToAddKey).child(categoryToAdd);

                    // generate a random key:
                    String hotelKey = hotels.push().getKey();
                    hotel.setKey(hotelKey);

                    // try and add the hotel object to the collection:
                    hotels.child(hotelKey).setValue(hotel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                appGeneralActivities.displayAlertDialog(source, "hotel successfully added",
                                        "hotel: " + hotel.getName() + " and room type: " + hotel.getType() + " was added", R.drawable.information);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            } else {
                                appGeneralActivities.displayAlertDialog(source, "hotel failed to be added",
                                        "the hotel failed to be added because of the following exception: '" + task.getException().getLocalizedMessage() + "'", R.drawable.error);
                                // stop spinning the progress bar
                                progressBar_add.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                    // endregion add hotel Object to the collection
                }

            } else {
                // database is unavailable
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "database is currently unavailable, please try again later", R.drawable.error);
                // stop spinning the progress bar
                progressBar_add.setVisibility(View.INVISIBLE);
            }
        } else {
            // not connected to internet
            appGeneralActivities.displayAlertDialog(source, "no network connection", "no network connection was found, please try again later", R.drawable.error);
            // stop spinning the progress bar
            progressBar_add.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * function get source activity, the key of the city and the hotel key
     * will get a reference to the hotel to delete from the "hotels" collection
     * and will try and delete it only if it has no more available rooms
     *
     * @param source   function gets source activity, the key of the city
     * @param cityKey  the node key of the city
     * @param hotelKey the node key of the Hotel object to delete
     */
    public void deleteHotel(Activity source, String cityKey, String hotelKey) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        String category = DataBaseCollectionsKeys.HOTELS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get a reference to the Hotel object:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category).child(hotelKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Hotel hotelToDelete = snapshot.getValue(Hotel.class);

                                // if there are no more available amount of rooms, only then delete it:
                                if (hotelToDelete.getAvailableAmount() <= 0) {
                                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // the Hotel object was deleted, now delete the image associated with it:
                                            FirebaseStorage.getInstance().getReference(hotelToDelete.getImageKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    appGeneralActivities.displayAlertDialog(source, "successful delete", "hotel: " + hotelToDelete.getName() + "\n" +
                                                            "room type: " + hotelToDelete.getType() + " was successfully deleted", R.drawable.information);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    appGeneralActivities.displayAlertDialog(source, "operation failed", "cannot delete an hotel while it still has available rooms", R.drawable.warning);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                appGeneralActivities.displayAlertDialog(source, "operation failed", "hotel was not deleted because of the " +
                                        "following exception: '" + error.getMessage() + "'", R.drawable.error);
                            }
                        });
            } else {
                // database is unavailable
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // not connected to internet
            appGeneralActivities.displayAlertDialog(source, "no network connection", "no network connection was found, please try again later", R.drawable.error);
        }
    }

    /**
     * function gets source activity, the key of the city
     * and the flight key to delete, will delete the flight only if its no longer valid
     * (its date is already past or its amount is 0) and will notify the employee
     *
     * @param source    Activity from which this function will be called
     * @param cityKey   the node key of the city of the flight
     * @param flightKey the node key of the flight
     */
    public void deleteInvalidFlight(Activity source, String cityKey, String flightKey) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        String category = DataBaseCollectionsKeys.FLIGHTS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get a snapshot of the Flight to delete:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category).child(flightKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // get access to the flight object:
                                Flight flightToDelete = snapshot.getValue(Flight.class);

                                // get the current time in long value:
                                LocalDateTime now = LocalDateTime.now();
                                ZonedDateTime zdt = ZonedDateTime.of(now, ZoneId.systemDefault());
                                long date = zdt.toInstant().toEpochMilli();

                                // only if the flight is already past its time or its amount is 0, only then we can give the employee
                                // the permission to delete it:
                                if (flightToDelete.getDateOfFlight() < date || flightToDelete.getAvailableAmount() <= 0) {
                                    snapshot.getRef().removeValue();
                                    appGeneralActivities.displayAlertDialog(source, "successful delete", "flight Number:" + flightToDelete.getKey() + " was " +
                                            "successfully deleted", R.drawable.information);

                                } else {
                                    appGeneralActivities.displayAlertDialog(source, "operation failed", "cannot delete a flight which is still relevant", R.drawable.warning);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                appGeneralActivities.displayAlertDialog(source, "operation failed", "flight was not deleted because of the " +
                                        "following exception: '" + error.getMessage() + "'", R.drawable.error);
                            }
                        });

            } else {
                // database is unavailable
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // not connected to internet
            appGeneralActivities.displayAlertDialog(source, "no network connection", "no network connection was found, please try again later", R.drawable.error);
        }
    }

    /**
     * function gets source activity, the key of the city
     * and the attraction key to delete, will delete the attraction only if its no longer valid
     * (its date is already past or its amount is 0) and will notify the employee
     *
     * @param source        Activity from which this function will be called
     * @param cityKey       the node key of the city of the attraction
     * @param attractionKey the node key of the attraction
     */
    public void deleteAttraction(Activity source, String cityKey, String attractionKey) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        String category = DataBaseCollectionsKeys.ATTRACTIONS;

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                // get a snapshot of the Attraction to delete:
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey).child(category).child(attractionKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // get Access to the Attraction object:
                                Attraction attractionToDelete = snapshot.getValue(Attraction.class);

                                // get the current time in long value:
                                LocalDateTime now = LocalDateTime.now();
                                ZonedDateTime zdt = ZonedDateTime.of(now, ZoneId.systemDefault());
                                long date = zdt.toInstant().toEpochMilli();

                                // only if the attraction is already past its time or its amount is 0, only then we can give the employee
                                // the permission to delete it:
                                if (attractionToDelete.getDate() < date || attractionToDelete.getAvailableAmount() <= 0) {
                                    snapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // the Attraction object was deleted, now delete the image associated with it:
                                            FirebaseStorage.getInstance().getReference(attractionToDelete.getImageKey()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    appGeneralActivities.displayAlertDialog(source, "successful delete", attractionToDelete.getType() + " attraction" +
                                                            " was successfully deleted.", R.drawable.information);
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    appGeneralActivities.displayAlertDialog(source, "operation failed", "cannot delete an attraction which is still relevant", R.drawable.warning);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                appGeneralActivities.displayAlertDialog(source, "operation failed", "attraction was not deleted because of the " +
                                        "following exception: '" + error.getMessage() + "'", R.drawable.error);
                            }
                        });
            } else {
                // database is unavailable
                appGeneralActivities.displayAlertDialog(source, "database unavailable", "database is currently unavailable, please try again later", R.drawable.error);
            }
        } else {
            // not connected to internet
            appGeneralActivities.displayAlertDialog(source, "no network connection", "no network connection was found, please try again later", R.drawable.error);
        }
    }

    /**
     * function gets source activity, city key, category and product key for update
     * will update its availableAmount and subtract from it
     *
     * @param source     Activity from which this function will be called
     * @param cityKey    the key of the city of the product
     * @param category   the category of the product
     * @param productKey the key of the product
     * @param subtract   the amount to subtract from the product
     */
    public void updateProductAmount(Activity source, String cityKey, String category, String productKey, int subtract) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.PRODUCTS).child(cityKey)
                        .child(category).child(productKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // open a map which will be sent to the Database to update the available amount:
                            Map<String, Object> updated_amount = new HashMap<String, Object>();

                            // calculate the amount available for a certain Product:
                            updated_amount.put("availableAmount", snapshot.getValue(Product.class).getAvailableAmount() - subtract);

                            // now update the available amount for the certain product:
                            snapshot.getRef().updateChildren(updated_amount);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        }
    }

    /**
     * creates and appends a statistic object for the given city
     *
     * @param source    Activity from which this function will be called
     * @param statistic the statistic object to append
     * @param cityKey   the key of the city
     */
    public void appendStatistic(Activity source, Statistic statistic, String cityKey) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();

        if (appGeneralActivities.isConnectedToInternet(source)) {
            if (appGeneralActivities.isConnectedToDatabase()) {
                FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.STATISTICS)
                        .child(cityKey).push().setValue(statistic);
            }
        }
    }

    /**
     * function add a purchase to a user history purchase
     *
     * @param source         Activity from which this function will be called
     * @param dateOfPurchase date of the purchase
     * @param amount         amount purchased
     * @param price          the price of the purchase
     * @param model          the Object which represents the product purchased
     */
    public Purchase addPurchaseHistoryToUser(Activity source, long dateOfPurchase, int amount, double price, Object model) {
        AppGeneralActivities appGeneralActivities = new AppGeneralActivities();
        String user_id = source.getSharedPreferences(SharedPreferencesKeys.GENERAL, Context.MODE_PRIVATE).getString(SharedPreferencesKeys.UID, SharedPreferencesKeys.DEFAULT_VALUE);

        // generate a random key for the purchase:
        String purchase_key = FirebaseDatabase.getInstance().getReference().push().getKey();

        if (model instanceof Flight) {
            // build the flight purchase:
            Flight flight = (Flight) model;

            FlightPurchase flightPurchase = new FlightPurchase(dateOfPurchase, amount, price, purchase_key, flight.getDateOfFlight(),
                    flight.getSource(), flight.getDestination(), flight.getFlightClass());

            String category = DataBaseCollectionsKeys.FLIGHTS;

            // add the flight purchase for the user:
            FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY)
                    .child(user_id).child(category).child(purchase_key).setValue(flightPurchase);

            return flightPurchase;

        } else if (model instanceof Attraction) {
            // build the attraction purchase:
            Attraction attraction = (Attraction) model;

            AttractionPurchase attractionPurchase = new AttractionPurchase(dateOfPurchase, amount, price, purchase_key, attraction.getDate(), attraction.getType(), attraction.getDescription());

            String category = DataBaseCollectionsKeys.ATTRACTIONS;

            // add the attraction purchase for the user:
            FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY)
                    .child(user_id).child(category).child(purchase_key).setValue(attractionPurchase);

            return attractionPurchase;
        } else if (model instanceof Hotel) {
            Hotel hotel = (Hotel) model;

            String arrivalDate = activity_hotels.arrivalDate;
            String departureDate = activity_hotels.departureDate;

            HotelPurchase hotelPurchase = new HotelPurchase(dateOfPurchase, amount, price, purchase_key, (int) price / amount / hotel.getPrice(), hotel.getName(),
                    hotel.getType(), hotel.getDescription(), arrivalDate, departureDate);

            String category = DataBaseCollectionsKeys.HOTELS;

            // add the hotel purchase for the user:
            FirebaseDatabase.getInstance().getReference(DataBaseCollectionsKeys.USERS_PURCHASE_HISTORY)
                    .child(user_id).child(category).child(purchase_key).setValue(hotelPurchase);

            return hotelPurchase;
        }

        return null;
    }

    /**
     * function fetches statistical data according to the given year
     * @param path String path to the statistics collection
     * @param listener an Interface listener to the result
     */
    public void fetchStatisticData(int year,String path, DataFetchListener listener) {
        ArrayList<Statistic> statistics = new ArrayList<Statistic>();

        // get the start date and end date of data fetch by long values:
        LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0);
        long startDate_long = ZonedDateTime.of(LocalDateTime.from(startDate), ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endDate_long = ZonedDateTime.of(LocalDateTime.from(endDate), ZoneId.systemDefault()).toInstant().toEpochMilli();

        // fetch all the data from the between the dates: year/1/1 and (year+1)/1/1
        FirebaseDatabase.getInstance().getReference().child(path).orderByChild("date").startAt(startDate_long).endBefore(endDate_long)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren())
                            statistics.add(data.getValue(Statistic.class));

                        listener.onSuccessfulDataFetch(statistics);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

}
