package com.example.tourexpert;

import android.app.Activity;

/**
 * used in activities where there is a RecyclerView Adapter
 * this class is responsible to save the current activity the user is on
 * by setting the savedActivity field so we can access the current activity
 * in the various RecyclerView Adapter's
 */
public class SavedActivity {
    // this is a class used in activity_cities, so we can access
    // the activity in the cityAdapter
    private static Activity savedActivity;

    public static void setActivity(Activity activity) {
        savedActivity = activity;
    }

    public static Activity getActivity() {
        return savedActivity;
    }
}
