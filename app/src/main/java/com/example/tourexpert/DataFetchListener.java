package com.example.tourexpert;

import java.util.ArrayList;

/**
 * used as a custom listener that listens to a certain
 * async value event which will fetch statistic objects as data
 * and take care of failure
 */
public interface DataFetchListener {

    void onSuccessfulDataFetch(ArrayList<Statistic> statistics);
    void onFailure(String message);
}
