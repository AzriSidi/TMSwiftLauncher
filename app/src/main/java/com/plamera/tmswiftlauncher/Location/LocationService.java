package com.plamera.tmswiftlauncher.Location;

import android.location.Location;

/**
 * This interface represents the functions of location service.
 */
public interface LocationService {

    /**
     * This method is used to retrieve the location information
     * of the device.
     *
     * @return Returns location details including latitude and longitude.
     */
    Location getLocation();

    /**
     * This method is used to retrieve the last known location information
     * of the device.
     *
     * @return Returns location details including latitude and longitude.
     */
    Location getLastKnownLocation();

}