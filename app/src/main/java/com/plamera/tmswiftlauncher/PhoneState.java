package com.plamera.tmswiftlauncher;

import android.app.Activity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import android.widget.TextView;

import com.plamera.tmswiftlauncher.AppsList.HomeScreen;

public class PhoneState extends PhoneStateListener {
    int dbmLevel,asuLevel;
    TextView signalMain,signalHome;
    String signal;
    Activity activity;

    public PhoneState(Activity activity){
        this.activity = activity;
        signalMain = activity.findViewById(R.id.textView11);
        signalHome = activity.findViewById(R.id.textView7);
        Log.d("PhoneState","this.activity");
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        asuLevel = signalStrength.getGsmSignalStrength();
        dbmLevel = (signalStrength.getGsmSignalStrength() * 2) - 113;
        signal = dbmLevel + " dBm"+ " " +asuLevel+" asu";
        Log.d("PhoneState","StrengthChanged: "+signal);
        if(activity instanceof MainActivity){
            signalMain.setText(signal);
        }else if (activity instanceof HomeScreen){
            signalHome.setText(signal);
        }
    }
}
