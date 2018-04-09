package com.plamera.tmswiftlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.plamera.tmswiftlauncher.Encap.BlackList;

import java.util.List;

public class BlockOutgoing extends BroadcastReceiver {
    DatabaseHandler db;
    String blockNumber;

    @Override
    public void onReceive(Context context, Intent intent){
        db = new DatabaseHandler(context);
        List<BlackList> blackList = db.getAllBlackList();
        for (BlackList bl : blackList) {
            String log = "Id: " + bl.getId() + " ,BlackListNum: " + bl.getBlackListNumber();
            Log.d("SqliteQueryLog: ", log);
            blockNumber = bl.getBlackListNumber();
            String callNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER).toString();
            if (blockNumber.equals(callNumber)) {
                Log.w("GetOutgoingCallNumber", "Number---->"+callNumber);
                setResultData(null);
                Toast.makeText(context, "Outgoing Call Blocked", Toast.LENGTH_SHORT).show();
            }else {
                Log.w("GetOutgoingCallNumber", "Number---->"+callNumber);
            }
        }
    }
}
