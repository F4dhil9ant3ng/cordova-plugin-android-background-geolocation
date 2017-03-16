package com.geolocation.geolocationplugin;

import android.provider.BaseColumns;

/**
 * Created by user on 2/27/2017.
 */

public final class LocationsDbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private LocationsDbContract() {}

    /* Inner class that defines the table contents */
    public class Locations implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_DISTANCE = "distance";
    }
}