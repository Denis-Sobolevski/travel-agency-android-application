package com.example.tourexpert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * utility class mainly containing
 * input validation methods
 */
public class AppGeneralActivities {

    /**
     * will make a click sound when interacting with the interface
     * @param activity - the activity in which the sound will be made
     */
    public void click(Activity activity) {
        MediaPlayer mediaPlayer = MediaPlayer.create(activity, R.raw.click_sound);
        mediaPlayer.start();
    }

    // if only the context is available, like the adapters for recycler view:
    public void click(Context activity) {
        MediaPlayer mediaPlayer = MediaPlayer.create(activity, R.raw.click_sound);
        mediaPlayer.start();
    }

    /**
     * checks the current state of internet connection
     * @param context the activity calling the function
     * @return true if there is internet or wifi connection, false otherwise
     */
    public boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * checks whether we have access to the database
     * @return true if there is a connection to the database, false otherwise
     */
    public boolean isConnectedToDatabase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        return db != null;
    }

    /**
     * function will return the country code for a phone number, like +972 is for israel
     * @return - a String that is the country code by the user SIM card
     */
    public String getCountryDialCode(Activity activity) {
        String conutryId = null;
        String contryDialCode = null;

        TelephonyManager telephonyMngr = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);

        // we get the name of the current country, example: il - which is israel
        conutryId = telephonyMngr.getSimCountryIso().toUpperCase();

        // go look at values/strings.xml so you can see the array:
        String[] arrCountryCode = activity.getResources().getStringArray(R.array.DialingCountryCode);

        // we run on our array of country codes:
        for (int i = 0; i < arrCountryCode.length; i++) {
            String[] arrDial = arrCountryCode[i].split(",");
            // example: if arrCountryCode[i] = 972,IL
            // so arrDial = {972, IL}

            // if we found the name of the current country inside of the array
            // we put the right country number and we break
            if (arrDial[1].trim().equals(conutryId.trim())) {
                contryDialCode = arrDial[0];
                break;
            }
        }

        return "+" + contryDialCode;
    }

    /**
     * display a simple alert dialog message
     * @param context current activity context
     * @param title   title of the dialog
     * @param message message of the dialog
     * @param icon    icon to display
     */
    @SuppressLint("LongLogTag")
    public void displayAlertDialog(Context context, String title, String message, int icon) {
        try {
            AlertDialog.Builder build = new AlertDialog.Builder(context);
            build.setMessage(message)
                    .setTitle(title)
                    .setIcon(icon)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } catch (Exception e) {
            Log.d("exception from displayAlertDialog", e.getLocalizedMessage());
        }
    }

    /**
     * Will return true if the given parameter is a valid phoneNumber, false otherwise
     * @param phone - a String representing the phone number to check
     * @return true if it is a valid phone number, false otherwise
     */
    public boolean isValidPhoneNumber(String phone) {
        for (int i = 0; i < phone.length(); i++)
            if (phone.charAt(i) < '0' || phone.charAt(i) > '9')
                return false;
        return true;
    }

    /**
     * Will return true if the given parameter has only a-z or A-Z and spaces characters, false otherwise
     * @param name - firstName or lastName of the user
     * @return true if the given parameter is a valid full name, false otherwise
     */
    public boolean isValidFullName(String name) {
        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) != ' ')
                if (!(name.charAt(i) >= 'A' && name.charAt(i) <= 'Z')
                        &&
                        !(name.charAt(i) >= 'a' && name.charAt(i) <= 'z'))
                    return false;
        return true;
    }

    /**
     * function gets a EditText, reads its text value
     * validates, returns true if its a valid email address, else returns false and sets an error
     * to this view
     * @param textInputEditText - the EditText we getText() from
     * @return true if the text is a valid email, false otherwise
     */
    public boolean validateEmail(EditText textInputEditText) {
        String email = textInputEditText.getText().toString().trim();

        if (email.isEmpty()) {
            textInputEditText.setError("email address is required");
            textInputEditText.requestFocus();
            // rollback the function
            return false;
        }

        if (email.length() < 6) {
            textInputEditText.setError("email address should be at least 6 characters long");
            textInputEditText.requestFocus();
            return false;
        }

        // checks if the email we got from the user is valid, example: @gmail.com @something..
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEditText.setError("please provide a valid email address");
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * function gets a EditText and whatName - which is the description for the error,
     * can be first name last name or whatever is required in the error
     * returns true if the name is valid, else returns false and sets an error
     * to this view
     * @param textInputEditText - the EditText view we getText() from
     * @param whatName          - can be first name, last name or whatever is required to display in the error
     * @param length            - the desired length for the given EditText to check
     * @return true if consists only of 'spaces' and alphabetic characters
     */
    public boolean validateName(EditText textInputEditText, String whatName, int length) {
        String name = textInputEditText.getText().toString().trim();

        if (name.isEmpty()) {
            textInputEditText.setError(whatName + " is required");
            textInputEditText.requestFocus();
            return false;
        }

        if (name.length() < length) {
            textInputEditText.setError(whatName + " should be at least " + length + " characters long");
            textInputEditText.requestFocus();
            return false;
        }

        // firstName contains numbers, it is not valid:
        if (!this.isValidFullName(name)) {
            textInputEditText.setError(whatName + " should contain only a-z or A-Z characters");
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * function gets a EditText, reads its text value
     * validates, returns true if its a valid phone number, else returns false and sets an error
     * to this view
     * @param textInputEditText - an View which the user has entered his phone number
     * @return true if the text in the given EditText view is a valid phone number, false otherwise
     */
    public boolean validatePhone(EditText textInputEditText) {
        String phone = textInputEditText.getText().toString().trim();

        if (phone.isEmpty()) {
            textInputEditText.setError("phone number is required");
            textInputEditText.requestFocus();
            return false;
        }
        if (phone.length() < 6) {
            textInputEditText.setError("phone number should be at least 6 digits long");
            textInputEditText.requestFocus();
            return false;
        }

        // phone contain characters other than 0-9, it is not valid:
        if (!this.isValidPhoneNumber(phone)) {
            textInputEditText.setError("phone number should only contain digits");
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * function gets a TexInputEditText, reads its text values
     * validates, returns true if it is a valid password, else returns false and sets an error
     * to this view
     * @param textInputEditText - a View which the user has entered his password
     * @return true if the password is valid, false otherwise
     */
    public boolean validatePassword(EditText textInputEditText) {
        String password = textInputEditText.getText().toString().trim();

        if (password.isEmpty()) {
            textInputEditText.setError("password is required");
            textInputEditText.requestFocus();
            return false;
        }
        // FireBase database doesn't accept passwords less than 6 characters !!!
        if (password.length() < 8) {
            textInputEditText.setError("password should be at least 8 characters long");
            textInputEditText.requestFocus();
            return false;
        }
        return true;
    }


}
