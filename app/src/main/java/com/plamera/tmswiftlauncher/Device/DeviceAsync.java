package com.plamera.tmswiftlauncher.Device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.plamera.tmswiftlauncher.Global;
import com.plamera.tmswiftlauncher.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceAsync {
    static int timeout = 300000;
    static String TAG = "DeviceAsync";
    static TextView myScroller;
    static String Serveradd;

    public static class CheckServerStatus extends AsyncTask<Void, Void, Void> {
        Context context;
        Activity activity;
        PingServerStatus myPingServerStatus;
        String ServerTime,ServerName;

        public CheckServerStatus(Context context){
            this.context = context;
            this.activity = (Activity) context;
            myScroller = activity.findViewById(R.id.textView1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {

                if (Global.connected3G || Global.connectedToWiFi
                        && Global.loginServer != "SIT") {

                    if ( Global.loginServer == "SIT") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        // Global.URLSwift = "http://58.26.233.1:8080/";
                    } else if (Global.loginServer == "DEV") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.44.11.64:8090/";
                    } else if ( Global.loginServer == "PRE OLD") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.106.132.7/";
                        // Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                    } else if (Global.loginServer == "PRE") {
                        Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.81/";
                    } else {
                        Global.URLSwift = "http://10.54.97.227:8888/";
                        //Global.URLSwift = "http://10.54.7.214/";
                        //Global.URLSwift = "http://10.41.102.70/";
                    }

                    // }

                    Serveradd = Global.URLSwift + "serverInfo.php";

                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters,timeout);
                    HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                    HttpClient client = new DefaultHttpClient(httpParameters);
                    Log.d(TAG,"Serveradd:" + Serveradd );
                    HttpGet request = new HttpGet(Serveradd);

                    HttpResponse response = client.execute(request);

                    InputStream in = response.getEntity().getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in));
                    StringBuilder str = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        str.append(line);
                    }
                    in.close();
                    String serverDateStr = str.toString().trim();
                    ServerStatusExtract(serverDateStr);
                    if (Global.ServerStatus.contains("OK")) {
                        Global.ServerStatus = "Connected to " + ServerName;
                        Global.ServerDate = ServerTime;
                        Log.d(TAG,"HTTP check OK "
                                + Serveradd);
                    } else {
                        Global.ServerStatus = "Not Connected";
                        Log.d(TAG,"HTTP check Not OK "
                                + Serveradd + Global.DisplayUsername);
                        if (Global.currentAPN.contains("Maxis")
                                && Global.connected3G) {
                            myPingServerStatus = new PingServerStatus(context);
                            myPingServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                    if (Global.loginContext != null) {
                        ((Activity) Global.loginContext)
                                .runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (Global.ServerStatus
                                                .contains("Not Connected")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else if (Global.ServerStatus
                                                .contains("Unknown")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else {
                                            if (Global.myStatus.contains("WIFI")) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#FF8000"));
                                            } else if (Global.connected3G) {
                                                myScroller.setBackgroundColor(Color
                                                        .parseColor("#210B61"));
                                            }
                                        }
                                        myScroller.setText(Global.myStatus
                                                + " | SERVER: "
                                                + Global.ServerStatus);
                                    }
                                });
                    }
                } else {
                    // Log.e("Login","SIT");
                    if (Global.DisplayUsername.contains("*") && Global.connectedToWiFi) {
                        Global.ServerStatus = "Connected to gponems";

                        if (Global.loginContext != null) {
                            ((Activity) Global.loginContext)
                                    .runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#FF8000"));
                                            myScroller.setText(Global.myStatus
                                                    + " | SERVER: "
                                                    + Global.ServerStatus);
                                        }

                                    });
                        }
                    }

                }

            } catch (Exception e) {
                ServerTime = "";
                Log.e("Login CheckServerStatus", "LoginException: "
                        + e);

                Global.ServerStatus = "Not Connected";
                if (Global.currentAPN.contains("Maxis") && Global.connected3G) {
                    myPingServerStatus = new PingServerStatus(context);
                    myPingServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (Global.loginContext != null) {
                    ((Activity) Global.loginContext)
                            .runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (Global.ServerStatus
                                            .contains("Not Connected")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);

                                    } else if (Global.ServerStatus
                                            .contains("Unknown")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);

                                    } else {
                                        if (Global.myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#FF8000"));

                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#210B61"));

                                        }

                                    }

                                    myScroller.setText(Global.myStatus
                                            + " | SERVER: "
                                            + Global.ServerStatus);

                                }

                            });
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d(TAG, "Server Status: " + Global.ServerStatus);
            Global.CheckNetworkRunning = false;
            if (Global.ServerStatus.contains("Connected to")){
                if(DeviceInfo.testNetPd != null) {
                    if (DeviceInfo.testNetPd.isShowing()) {
                        DeviceInfo.timer.cancel();
                        DeviceInfo.testNetPd.dismiss();
                    }
                }
                if (Global.checkServerRunning) {
                    Global.checkServerRunning = false;
                    Toast.makeText(context, "Server Status: " + Global.ServerStatus,
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }

        private void ServerStatusExtract(String strData) {
            try {
                int start = 0;
                int end = 0;
                ServerName = "";
                start = strData.indexOf("<serverName>");
                end = strData.indexOf("</serverName>");
                ServerName = strData.substring(start + 12, end - 15);
                start = strData.indexOf("<serverStatus>");
                end = strData.indexOf("</serverStatus>");
                Global.ServerStatus = strData.substring(start + 14, end);
                start = strData.indexOf("<serverTime>");
                end = strData.indexOf("</serverTime>");
                ServerTime = strData.substring(start + 12, end);
                try {
                    Global.ServerDate = ServerTime;
                    String splitDT[] = Global.ServerDate.split(" ");
                    String strDt = splitDT[0];
                    String strTm = splitDT[1];
                    Log.i("Pian", "Date=" + strDt);
                    Log.i("Pian", "Time=" + strTm);
                    SimpleDateFormat inFormat = new SimpleDateFormat(
                            "yyyy-MM-dd");
                    SimpleDateFormat outFormat = new SimpleDateFormat(
                            "MM/dd/yyyy");
                    Date dt = inFormat.parse(strDt);
                    Global.reqUsingServerDate = outFormat.format(dt).toString();
                    Global.updateUsingServerDateTime = Global.reqUsingServerDate
                            + " " + strTm;
                    Log.i("Pian", "Global.reqUsingServerDt="
                            + Global.reqUsingServerDate);
                    Log.i("Pian", "Global.updateUsingServerDateTime="
                            + Global.updateUsingServerDateTime);
                } catch (Exception e) {
                    Log.i("Pian", e.toString());
                }
            } catch (Exception e) {
                Log.i("ServerStatusException", e.toString());
            }
        }
    }

    public static class PingServerStatus extends AsyncTask<Void, Void, Void> {
        Context context;
        Activity activity;
        DeviceInfo deviceInfo;

        public PingServerStatus(Context context) {
            this.context = context;
            this.activity = (Activity) context;
            deviceInfo = new DeviceInfo(context);
            myScroller = activity.findViewById(R.id.textView1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                if (Global.connected3G) {
                    String pingCmd = "ping -c 1 -w 25 -s 1 " + deviceInfo.getLocalIP();
                    java.lang.Process p1 = java.lang.Runtime.getRuntime().exec(pingCmd);
                    int returnVal = p1.waitFor();
                    Log.d("Login PingServerStatus ", String.valueOf(returnVal));
                    Log.d("Login PingServerStatus ", pingCmd);
                    if (returnVal == 0) {
                        Global.CanPing = true;
                        Log.d("Login PingServerStatus", " ping check OK");
                    } else {
                        Global.CanPing = false;
                        Global.ServerStatus = "Not Connected";
                        Global.myStatus = "NETWORK: Ping Fail ";

                        Log.d("Login PingServerStatus", " ping check Not OK "
                                + Global.MaxisRouter);
                    }
                    if (Global.loginContext != null) {
                        ((Activity) Global.loginContext)
                                .runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (Global.ServerStatus
                                                .contains("Not Connected")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else if (Global.ServerStatus
                                                .contains("Unknown")) {
                                            myScroller
                                                    .setBackgroundColor(Color.RED);
                                        } else {
                                            if (Global.myStatus.contains("WIFI")) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#FF8000"));
                                            } else if (Global.connected3G) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#210B61"));
                                            }

                                        }

                                        myScroller.setText(Global.myStatus
                                                + " | SERVER: "
                                                + Global.ServerStatus);
                                    }

                                });
                    }
                }

            } catch (Exception e) {
                Log.e("Login PingServerStatus",
                        " Ping check error" + e.toString());
                Global.CanPing = false;
                Global.ServerStatus = "Not Connected";
                Global.myStatus = "NETWORK: Ping Fail ";

                Log.d("Login PingServerStatus", " ping check Not OK "
                        + Global.MaxisRouter);
                if (Global.loginContext != null) {
                    ((Activity) Global.loginContext)
                            .runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (Global.ServerStatus
                                            .contains("Not Connected")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    } else if (Global.ServerStatus
                                            .contains("Unknown")) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    } else {
                                        if (Global.myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#FF8000"));
                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#210B61"));
                                        }

                                    }

                                    if (!Global.CanPing) {
                                        myScroller
                                                .setBackgroundColor(Color.RED);
                                    }
                                    myScroller.setText(Global.myStatus
                                            + " | SERVER: "
                                            + Global.ServerStatus);
                                }

                            });
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (Global.CanPing) {
                deviceInfo.queryNetwork();
            } else {
                Global.CheckNetworkRunning = false;
                if (Global.checkServerRunning) {
                    Global.checkServerRunning = false;
                    Toast.makeText(context, "Network Status: Ping Fail",
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }
    }

    public static class InitTask extends AsyncTask<Void, Void, Void> {
        Context context;
        Activity activity;
        String serverDateStr;
        DeviceInfo deviceInfo;
        Boolean dateMismatch = false;
        Boolean mismatchDialogDisplayed = false;

        public InitTask(Context context) {
            this.context = context;
            this.activity = (Activity) context;
            deviceInfo = new DeviceInfo(context);
            myScroller = activity.findViewById(R.id.textView1);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // disable login button here
            Log.d("InitTask", "Start");
            Global.InitTaskRunning = true;
            if (Global.LogAsAdmin) {
                Global.LogAsAdmin = false;
            }
            deviceInfo.queryNetwork();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("InitTask", "doInBackground");

            if (!Global.DisplayUsername.contains("*") && Global.connected3G) {
                Log.d("InitTask", "FirstTimeRunLogin");

                if (Global.FirstTimeRunLogin) {
                    Global.FirstTimeRunLogin = false;
                    Log.d("InitTask", "Checking server date/time "
                            + Global.usernameBB + ":");

                    SimpleDateFormat sdfDate = new SimpleDateFormat(
                            "MM/dd/yyyy hh:mm:ss a");
                    Date currentDate = new Date();
                    Date serverDate;
                    serverDateStr = "";
                    dateMismatch = false;

                    try {
                        HttpParams httpParameters = new BasicHttpParams();
                        // set timeout to one minute
                        HttpConnectionParams.setConnectionTimeout(
                                httpParameters, timeout);
                        HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                        HttpClient client = new DefaultHttpClient(
                                httpParameters);

                        // if (Global.connected3G) {
                        //
                        // Global.URLSwift = "http://10.41.102.70/";
                        // Serveradd = Global.URLSwift
                        // + "Mobile/Configuration/time.php";
                        //
                        // } else {
                        if (Global.DisplayUsername.contentEquals("TM")) {
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        } else if (Global.DisplayUsername.contains("*")) {
                            // Global.URLSwift = "http://58.26.233.1:8080/";
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                            // Global.URLSwift =
                            // "http://swift.tmrnd.com.my:8080/";
                        } else if (Global.DisplayUsername.contains("#")) {
                            Global.URLSwift = "http://10.44.11.64:8090/";
                        } else if (Global.DisplayUsername.contains("$")) {
                            Global.URLSwift = "http://10.106.132.7/";
                        } else if (Global.DisplayUsername.contains("@")) {
                            Global.URLSwift = "http://10.41.102.81/";
                        } else if (Global.DisplayUsername.contains("!")) {
                            Global.URLSwift = "http://10.41.102.70/";
                        } else {
                            Global.URLSwift = "http://10.41.102.70/";
                        }

                        // Global.URLSwift = "http://10.41.102.70/"; // hisham
                        // add
                        // cause
                        // "http://swift.tmrnd.com.my:8080/"
                        // always
                        // fail
                        Serveradd = Global.URLSwift
                                + "Mobile/Configuration/time.php";

                        if (Global.DisplayUsername.contains("@")
                                || Global.DisplayUsername.contains("$")) {
                            Serveradd = Global.URLSwift
                                    + "preprod/Mobile/Configuration/time.php";

                        }
                        // }

                        Log.d("InitTask", "Checking server date/time "
                                + Global.URLSwift);
                        HttpGet request = new HttpGet(Serveradd); // HttpGet
                        // request
                        // = new
                        // HttpGet(
                        // "http://10.41.102.70/Mobile/Configuration/time.php");
                        HttpResponse response = client.execute(request);

                        InputStream in = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in));
                        StringBuilder str = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            str.append(line);
                        }
                        in.close();
                        serverDateStr = str.toString().trim();
                        serverDate = sdfDate.parse(serverDateStr);
                        if (serverDateStr.length() == 22) {
                            Global.ServerStatus = "Connected";
                            // Log.d("Login", " Check Date check OK" + Serveradd
                            // + DisplayUsername);
                        } else {
                            Global.ServerStatus = "Not Connected";
                            // Log.d("Login", " Check Date Not OK" + Serveradd
                            // + DisplayUsername);
                        }

                        long diff = Math.abs(serverDate.getTime()
                                - currentDate.getTime());
                        // check if times are within 10 minutes
                        if (diff > 600000) {
                            // Log.d("InitTask",
                            // "Date mismatch, Server: "
                            // + sdfDate.format(serverDate)
                            // + ", device: "
                            // + sdfDate.format(currentDate));
                            // put date in a simpler format
                            SimpleDateFormat sdfDateHuman = new SimpleDateFormat(
                                    "EEE, d MMM yyyy, h:mm:ss a");
                            serverDateStr = sdfDateHuman.format(serverDate);
                            dateMismatch = true;
                        } else {
                            // Log.d("InitTask",
                            // "Date OK, Server: "
                            // + sdfDate.format(serverDate)
                            // + ", device: "
                            // + sdfDate.format(currentDate));
                            dateMismatch = false;
                        }

                    } catch (Exception e) {
                        Log.e("InitTask", e.toString());
                        Global.ServerStatus = "Not Connected";
                        Log.e("InitTask", "Not Connected" + Serveradd
                                + Global.DisplayUsername);

                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Global.InitTaskRunning = false;

            // ahmad to prevent crash
            if (DeviceInfo.pdinit != null) {
                if (DeviceInfo.pdinit.isShowing()) {
                    DeviceInfo.pdinit.dismiss();
                }
            }

            if (Global.ServerStatus.contains("Not Connected")) {
                myScroller.setBackgroundColor(Color.RED);
            } else if (Global.ServerStatus.contains("Unknown")) {
                myScroller.setBackgroundColor(Color.RED);
            } else {
                if (Global.myStatus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
            myScroller.setText(Global.myStatus + " |  SERVER: "
                    + Global.ServerStatus);
            if (dateMismatch) {
                if (!mismatchDialogDisplayed) {
                    mismatchDialogDisplayed = true;
                    String MyMessage = "Please check and correct the date & time in your device\nServer date & time: "
                            + serverDateStr;
                    new AlertDialog.Builder(context)
                            .setTitle("Incorrect device date/time")
                            .setMessage(MyMessage)
                            .setCancelable(false)
                            .setNeutralButton("OK",
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            mismatchDialogDisplayed = false;
                                            activity.startActivity(new Intent(
                                                    android.provider.Settings.ACTION_DATE_SETTINGS));
                                        }

                                    }).show();
                } else {

                }
                // not disturb user again and again
                dateMismatch = false;
            }

            Log.d("InitTask", "Exit");
            // if (pdinit.isShowing()) {
            // pdinit.dismiss();
            // }
        }
    }
}