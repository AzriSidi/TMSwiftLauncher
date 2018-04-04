package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.plamera.tmswiftlauncher.Location.LocationService;
import com.plamera.tmswiftlauncher.Location.LocationServiceImpl;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class DeviceOperate {
    IntentFilter iFilter;
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    NetworkInfo networkInfo;
    GsmCellLocation cellLocation;
    TelephonyManager telephonyManager;
    LocationService locationService;
    int lac;
    int cid;
    String memory;
    String ssid;
    ConnectivityManager connManager;
    ActivityManager actManager;
    static ActivityManager.MemoryInfo memInfo;
    LocationManager locationManager;
    String TAG = "DeviceOperate";
    Context context;

    @SuppressLint("MissingPermission")
    public DeviceOperate(Context context) {
        this.context = context;
        locationService = LocationServiceImpl.getInstance(context);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batLvlReceiver, iFilter);
        context.registerReceiver(batStatusReceiver, iFilter);
        cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        findLocation();
    }

    public BroadcastReceiver batLvlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent batteryIntent) {
            int rawlevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (rawlevel >= 0 && scale > 0) {
                Global.batLvl = (rawlevel * 100) / scale;
            }
            Global.getLac = Lac();
            Global.getCid = Cid();
            Global.availMemory = getAvailableMemory();
            Log.d(TAG, "availMemory=" + Global.availMemory + " ,LAC=" + Global.getLac + " ,CID=" + Global.getCid);
        }
    };

    public BroadcastReceiver batStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent batteryIntent) {
            int deviceStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            switch (deviceStatus) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    Global.batState = "Unknown";
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    Global.batState = "Charging";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    Global.batState = "Discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    Global.batState = "Not Charging";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    Global.batState = "Full";
                    break;
            }
        }
    };

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(batLvlReceiver);
        context.unregisterReceiver(batStatusReceiver);
    }

    @SuppressLint("MissingPermission")
    public void findLocation() {
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {
                        public void onLocationChanged(Location loc) {
                            try {
                                Global.latitude = String.format("%.07f", loc.getLatitude());
                                Global.longitude = String.format("%.07f", loc.getLongitude());
                                Log.d(TAG, "onLocationChanged: " + Global.latitude + "," + Global.longitude);
                            } catch (Exception e) {
                                Log.e(TAG, "onLocationChanged: " + e.getMessage());
                            }
                        }

                        public void onProviderDisabled(String provider) {
                            Log.d(TAG, "onProviderDisabled");
                        }

                        public void onProviderEnabled(String provider) {
                            Log.d(TAG, "onProviderEnabled");
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                            Log.d(TAG, "onStatusChanged");
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                Global.latitude = String.format("%.07f", location.getLatitude());
                Global.longitude = String.format("%.07f", location.getLongitude());
                Log.d(TAG, "GetLocation= " + Global.latitude + "," + Global.longitude);
            }
        }
    }

    public String getWifiSsid() {
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID();
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public int Lac() {
        if (cellLocation != null){
            lac =  cellLocation.getLac();
        }
        return lac;
    }

    public int Cid(){
        if (cellLocation != null){
            cid =  cellLocation.getCid();
        }
        return cid;
    }

    public String getAvailableMemory(){
        actManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long availableMegs = memInfo.availMem / 1048576L;
        memory = String.valueOf(availableMegs);
        return memory;
    }
}
