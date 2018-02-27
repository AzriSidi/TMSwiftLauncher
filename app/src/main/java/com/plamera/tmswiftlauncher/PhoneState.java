package com.plamera.tmswiftlauncher;

import android.app.Activity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.widget.TextView;

public class PhoneState extends PhoneStateListener {
    int dbmLevel,asuLevel;
    TextView signalHome,signalMain;
    String signal;
    Activity activity;

    public PhoneState(Activity activity){
        this.activity = activity;
        signalMain = activity.findViewById(R.id.textView11);
        signalHome = activity.findViewById(R.id.textView7);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        asuLevel = signalStrength.getGsmSignalStrength();
        dbmLevel = (signalStrength.getGsmSignalStrength() * 2) - 113;
        signal = dbmLevel + " dBm"+ " " +asuLevel+" asu";
        if(activity instanceof MainActivity){
            signalMain.setText(signal);
        }else if (activity instanceof HomeScreen){
            signalHome.setText(signal);
        }
    }
}
