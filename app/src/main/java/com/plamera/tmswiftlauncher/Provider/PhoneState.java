package com.plamera.tmswiftlauncher.Provider;

import android.app.Activity;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Log;
import android.widget.TextView;

import com.plamera.tmswiftlauncher.AppsList.HomeScreen;
import com.plamera.tmswiftlauncher.MainActivity;
import com.plamera.tmswiftlauncher.R;

public class PhoneState extends PhoneStateListener {
    int dbmLevel;
    int asuLevel;
    TextView signalMain,signalHome,phoneInfo;
    String TAG = "PhoneState";
    String signal,phoneState;
    Activity activity;

    public PhoneState(Activity activity){
        this.activity = activity;
        signalMain = activity.findViewById(R.id.textView11);
        signalHome = activity.findViewById(R.id.textView7);
        phoneInfo = activity.findViewById(R.id.textView8);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        asuLevel = signalStrength.getGsmSignalStrength();
        dbmLevel = (signalStrength.getGsmSignalStrength() * 2) - 113;
        signal = dbmLevel + " dBm"+ " " +asuLevel+" asu";
        if(activity instanceof MainActivity){
            signalMain.setText(signal);
        }else if (activity instanceof HomeScreen){
            signalHome.setText(signal);
        }
        Log.d(TAG,"SignalStrength: "+signal);
        super.onSignalStrengthsChanged(signalStrength);
    }

    @Override
    public void onServiceStateChanged (ServiceState serviceState) {
        super.onServiceStateChanged(serviceState);
        int state = serviceState.getState();
        Log.d("Phone State", String.valueOf(state));
        switch(state) {
            case ServiceState.STATE_EMERGENCY_ONLY:
                phoneState ="Emergency Only";
                break;
            case ServiceState.STATE_IN_SERVICE:
                phoneState ="In Service";
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                phoneState ="Out Of Service";
                break;
            case ServiceState.STATE_POWER_OFF:
                phoneState ="POWER_OFF";
                break;
            default:
                phoneState = "Unknown";
                break;
        }
        if (activity instanceof HomeScreen){
            phoneInfo.setText(" | "+phoneState);
        }
        Log.d(TAG,"ServiceState: "+phoneState);
    }
}
