package com.plamera.tmswiftlauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.plamera.tmswiftlauncher.AppsList.HomeScreen;
import com.plamera.tmswiftlauncher.Encap.BlackList;
import com.plamera.tmswiftlauncher.Encap.UserDetail;
import com.plamera.tmswiftlauncher.Encap.WhileList;
import com.plamera.tmswiftlauncher.JwtUtil.JwtDecode;
import com.plamera.tmswiftlauncher.JwtUtil.JwtEncode;
import com.plamera.tmswiftlauncher.Provider.PhoneState;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    EditText userField, passField;
    TelephonyManager tel;
    TextView output,firmWare,dateView,myScroller,
            signalInfo,launchVer,agentVer;
    Intent i;
    String imeiValue, imsiValue ,carrierName;
    String Serveradd, ServerName;
    Boolean checkServerRunning = false;
    Boolean CheckNetworkRunning = false;
    ProgressDialog pd;
    private String myStatus = "";
    Timer CheckNetworkTimer;
    String DisplayUsername;
    BroadcastReceiver networkStateReceiver;
    String username, password, jsonStr;
    HttpHandler sh;
    Handler handler;
    private String savePath = Environment.getExternalStorageDirectory() + "/";
    public boolean logininvisible = false;
    private Boolean InitTaskRunning = false;
    private static ProgressDialog pdinit;
    String serverDateStr = "";
    Boolean dateMismatch = false;
    Boolean mismatchDialogDisplayed = false;
    CheckBox cb_showPwd;
    String TAG = "MainActivity";
    String myKey = "30820241308201aaa00302010202044f543029300d06092a864886f70d01010505003064310b30090603550406130236303111300f0603550408130853656c616e676f72311230100603550407130943796265726a617961310b3009060355040a1302544d310e300c060355040b0c05544d5226443111300f06035504031308544d2053776966743020170d3132303330353033313635375a180f33303038313031303033313635375a3064310b30090603550406130236303111300f0603550408130853656c616e676f72311230100603550407130943796265726a617961310b3009060355040a1302544d310e300c060355040b0c05544d5226443111300f06035504031308544d20537769667430819f300d06092a864886f70d010101050003818d003081890281810089a9975ca27524ee648ba8f6e32f4af02c879e34247f37a13e78e8aad50e955879550e13b676650001baea8497d152b338ab9405010910ace4d609923a6ea1b8c229ba2dede3cc81948710ff7418fea811396057084e0df35284449a167d873f649dbfd8a4fe8edeea505d13ec24439e02f978229b6cc6033927d6beb8c664090203010001300d06092a864886f70d0101050500038181006c9c6bbcab4ea91cbddb30a2ecc4856558dffc59e92dc4a054034098f05eaa99b58c7ac16c251f57a8e5fc7f7665ceceb95be8aef8e3a73accb96f1b6448ff634140b8d420c822589090d4297c23996bdcce17d538cb2c4b712087e2d538e235588a8b49fc8d7aefdd65b5ffc891ec77ef0b25d28f51490dea895caaa367e0ff";
    private String currentVersionName;
    private int currentVersionCode;
    JSONObject objLogin;
    JSONArray arrayLogin;
    String listNum[][];
    protected static final int REQUEST_CODE = 0;
    public boolean readytogotomainmenu = false;
    Timer myTimer;
    LoginTaskAsync LoginTask;
    public String MessageFailDialog = "";
    public boolean readytoshowdialog = false;
    public String DataMobile = "";
    AlertDialog.Builder customBuilder;
    String SimState;
    ImageView image;
    Intent intent;
    JwtEncode jwtEncode;
    JwtDecode jwtDecode;
    PackageInfo packageInfo;
    PackageManager pm;
    int timeout = 300000;
    private Context context = MainActivity.this;
    long currentTime = System.currentTimeMillis();
    DeviceOperate deviceOperate;
    DeviceInfo deviceInfo;

    //url
    String urlLogin = "http://10.54.97.227:9763/EMMWebService/loginApi";
    String urlSwift = "http://10.54.97.227:8888/";
    String urlConfig = "http://10.54.97.227:9763/EMMWebService/device_config";

    @SuppressLint("PackageManagerGetSignatures")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        image = findViewById(R.id.imageView1);
        image.setImageResource(R.drawable.tm_vector);
        myScroller = findViewById(R.id.textView5);
        firmWare = findViewById(R.id.textView7);
        userField = findViewById(R.id.username);
        passField = findViewById(R.id.password);
        output = findViewById(R.id.textView2);
        launchVer = findViewById(R.id.textView23);
        agentVer = findViewById(R.id.textView24);

        DisplayUsername = userField.getText().toString();
        cb_showPwd = findViewById(R.id.checkBox_ShowPassword);
        cb_showPwd.setOnCheckedChangeListener(new CheckBoxListener());
        tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        dateView = findViewById(R.id.textView10);
        CheckNetworkRunning = false;
        Global.loginContext = this;

        checkCurrentVersion();
        loadNdimSavedExchange();
        printNdimExchangeList();
        clearField();

        jwtEncode = new JwtEncode();
        jwtDecode = new JwtDecode();
        Global.mySQLiteAdapter = new DatabaseHandler(this);
        deviceOperate = new DeviceOperate(this);

        LoginToken();
        startAgent();
        getPackage();
        getWhiteList();
        AppVerText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        customBuilder = new AlertDialog.Builder(this);
        try {
            Log.d(TAG, "loginStatus: " + Global.status);
            if (Global.status.equals("Online")) {
                intentLogin();
            } else {
                printNdimExchangeList();
                cb_showPwd.setChecked(false);
                CheckNetworkRunning = false;
                Global.CreateMainMenu = false;
                Global.TTreqSummDate = "";
                Global.FFreqSummDate = "";
                networkStateReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        CheckNetworkRunning = false;
                        checkServerRunning = false;
                        Global.CanPing = true;
                        queryNetwork();
                        DeviceDetail();
                    }
                };
                IntentFilter networkIntentFilter = new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION);
                registerReceiver(networkStateReceiver, networkIntentFilter);
                queryNetwork();
                DeviceDetail();
                CheckNetworkTimer = new Timer();
                CheckNetworkTimer.schedule(new CheckNetworkTimerMethod(), 0, 5000);
                getWSWhiteList getWS = new getWSWhiteList();
                getWS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                SimpleDateFormat sdfDate = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss");
                String format = sdfDate.format(new Date());
                Log.d(TAG, "Current Timestamp: " + format);
                if (readytogotomainmenu) {
                    readytogotomainmenu = false;
                    Global.MustPassLockScreen = false;
                    Global.LoginMonitor = false;
                    //CheckGPSProvider();
                }
                Global.PhoneValid = true;

                // failcallback
                if (readytoshowdialog) {
                    readytoshowdialog = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setMessage(MessageFailDialog)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                // check APN
                String numeric = getAPN();
                if (numeric.contains("50219") || numeric.contains("50213")) {
                    Global.currentAPN = "Celcom";
                    Log.d("getAPN", "Celcom");
                } else if (numeric.contains("50217") || numeric.contains("50212")) {
                    Global.currentAPN = "Maxis";
                    Log.d("getAPN", "Maxis");
                } else {
                    Global.currentAPN = "Unknown";
                    Log.d("getAPN", "Unknown");
                }

                if (Global.upgradeInProgress) {
                    if (Global.frmVersion.startsWith("S7-601ue")) {
                        try {
                            (new Thread(enableInstallation)).start();
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // Log.e("Login",e.toString());
                        }
                        // Log.i("Login", "Enabling installation on vogue");
                    } else {
                        // Log.i("Login", "Launching installation");
                    }
                    Global.upgradeInProgress = false;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType(
                            Uri.fromFile(new File(savePath
                                    + Global.NewVersionFileName)),
                            "application/vnd.android.package-archive");
                    startActivity(i);
                }

                //date
                long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy");
                String dateString = sdf.format(date);
                dateView.setText(dateString);

                clearField();
                AppVerText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    public void getPackage(){
        pm = this.getPackageManager();
        String cert = "";
        try {
            int field = PackageManager.GET_SIGNATURES;
            packageInfo = pm.getPackageInfo("my.com.tm.swift", field);
            Signature[] signatures = packageInfo.signatures;
            cert = signatures[0].toCharsString();
            Log.d(TAG, "Cert: " + cert);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "certException: " + e);
        }
        if (cert.equals(myKey)) {
            Log.d(TAG, "TM SWIFT Mobile " + currentVersionName + " [Release]");
            Global.releaseVersion = true;
        } else {
            Log.d(TAG, "TM SWIFT Mobile " + currentVersionName + " [Debug]");
            Global.releaseVersion = false;
        }
        if (Global.releaseVersion) {
            Global.strVersion = currentVersionName + "/"
                    + Integer.toString(currentVersionCode) + "/R";
        } else {
            Global.strVersion = currentVersionName + "/"
                    + Integer.toString(currentVersionCode) + "/D";
        }

        if (Global.strVersion.length() > 25) {
            int totalStrVer = Global.strVersion.length();
            int totalExtraStr = totalStrVer - 25;
            String tempCrrVerName = Global.strVersion.substring(totalExtraStr,
                    totalStrVer);
            Global.strVersion = tempCrrVerName;
        }
        Log.d(TAG, "strVersion: " + Global.strVersion);
        Log.d(TAG, "releaseVersion: " + Global.releaseVersion);
    }

    public void getWhiteList(){
        if (Global.releaseVersion) {
            Log.d("Login", "Release version - checking whitelist");
            try {
                String data[][] = Global.mySQLiteAdapter.getSummaryUpdate();
                if (data != null) {
                    String WLData[][] = Global.mySQLiteAdapter.getWhiteList();
                    checkMyWhiteList(WLData);
                    Log.d(TAG, "start checking: " + Arrays.deepToString(WLData));
                    getBlackListDB();
                } else {
                    getWSWhiteList getWS = new getWSWhiteList();
                    getWS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                Log.d(TAG, "getSummaryUpdate: " + Arrays.deepToString(data));
                listNum = Global.mySQLiteAdapter.getMaterialRefAll();
                if (listNum == null) {
                    Global.blnUpdateMatNum = true;
                } else {
                    Global.blnUpdateMatNum = false;
                }
                long listLOV = Global.mySQLiteAdapter.getLOVReturnCount();
                Log.i("LOV", "listLOV=" + listLOV);
                if (listLOV == 0) {
                    Global.blnUpdateLOV = true;
                } else {
                    Global.blnUpdateLOV = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.d("Login", "Debug version - not checking whitelist");
            getWSWhiteList getWS = new getWSWhiteList();
            getWS.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //CR-0108
        }
    }

    public String LauncherVer() {
        String versionName = "";
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo(getPackageName(), 0);
            versionName = app.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public String AgentVer() {
        String versionName = "";
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo app = packageManager.getPackageInfo("org.wso2.emm.agent", 0);
            versionName = app.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public void AppVerText(){
        Global.launcherVer = LauncherVer();
        Global.agentVer = AgentVer();
        String fontPath = "fonts/Prototype.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        launchVer.setTypeface(tf);
        agentVer.setTypeface(tf);
        launchVer.setText("LAUNCHER - "+Global.launcherVer + "  |  ");
        agentVer.setText("EMM - "+ Global.agentVer);
    }

    // ***** To enable GPS at main *********************
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == 0) {
            // Global.UseGPSSattelite=true;
            String provider = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            Log.d("onActivityResult", provider);
            if (provider != null) {
                readytogotomainmenu = true;
            }
        }
    }

    private void getBlackListDB() {
        if (Global.blacklistedNumbers == null) {
            Global.blacklistedNumbers = new ArrayList<String>();
        } else {
            Global.blacklistedNumbers.clear();
        }
        try {
            String strBL[][] = Global.mySQLiteAdapter.getBlackList();
            if (strBL == null) {
                Log.i("getBlackListDB", "Unsuccessfull get black list");
            } else {
                for (int i = 0; i < strBL.length; i++) {
                    Log.i("getBlackListDB", "Black List no[" + i + "]=" + strBL[i][0]);
                    Global.blacklistedNumbers.add(strBL[i][0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNdimSavedExchange() {
        SharedPreferences myPrefs = getSharedPreferences("NdimExchange",
                MODE_PRIVATE);

        GlobalNDIM.exchangeSavedTime = myPrefs
                .getString("exchangeSavedTime", "");
        Log.i("Login NDIM", "exchangeSavedTime: <" + GlobalNDIM.exchangeSavedTime + ">");

        GlobalNDIM.exchangeSavedString = myPrefs
                .getString("exchangeSavedString", "");
        Log.i("Login NDIM", "exchangeSavedString: <" + GlobalNDIM.exchangeSavedString + ">");

        if (!GlobalNDIM.exchangeSavedString.isEmpty()) {
            JsonProcessor jp = new JsonProcessor();

            // 2016-03-10 forgot to split before processing
            String[] splitExchangeResult = GlobalNDIM.exchangeSavedString.split(GlobalNDIM.mainSeparator);

            if (splitExchangeResult.length >= 2) {
                if (splitExchangeResult[0].equals("0")) {
                    String[][] tempExchangeList = jp.processGetExchange(splitExchangeResult[1]);

                    if (tempExchangeList == null) {
                        Log.e("Login NDIM", "Null exchange list although exchangeSavedString not empty!");
                    } else {
                        GlobalNDIM.exchangeList = tempExchangeList;
                        Log.i("Login NDIM", GlobalNDIM.exchangeList.length + " exchanges loaded from shared preferences");
                    }

                } else {
                    Log.e("Login NDIM", "Error detected: "
                            + GlobalNDIM.exchangeSavedString);
                }
            } else {
                Log.e("Login NDIM", "Error splitting exchangeSavedString, length: "
                        + splitExchangeResult.length);
            }

        } else {
            Log.e("Login NDIM", "Empty exchangeSavedString detected! Exchange list will be null");
        }
    }

    void printNdimExchangeList() {

        if (GlobalNDIM.exchangeList != null) {
            for (int i = 0; i < GlobalNDIM.exchangeList.length; i++) {
                int j = i + 1; // just for esthetics
                Log.i("Login NDIM", j + ": " + GlobalNDIM.exchangeList[i][0] + ", " + GlobalNDIM.exchangeList[i][1] + ", " + GlobalNDIM.exchangeList[i][2]);
            }
        } else {
            Log.e("Login NDIM", "GlobalNDIM.exchangeList null");
        }

    }

    @SuppressLint("MissingPermission")
    private void DeviceDetail() {
        try {
            //imei
            imeiValue = tel.getDeviceId();
            Log.d(TAG,"ImeiCheck: "+imeiValue);
            if(imeiValue == null){
                Global.IMEIPhone = "-";
            }else{
                Global.IMEIPhone = imeiValue;
            }
            //imsi
            imsiValue = tel.getSimSerialNumber();
            if (imsiValue == null){
                noImsiPopUp();
                Global.IMSIsimCardPhone = "-";
            }else{
                Global.IMSIsimCardPhone = imsiValue;
            }

            carrierName = tel.getNetworkOperatorName();
            if(carrierName.equals("")){
                carrierName = "Not Available";
            }
            //output
            output.setText("Imsi: "+Global.IMSIsimCardPhone+"  |  Imei: "+Global.IMEIPhone);
            output.setBackgroundColor(Color.parseColor("#595959"));

            //firmware
            Global.frmVersion = Build.DISPLAY;
            firmWare.setText("Firmware: "+Global.frmVersion+" | "+carrierName+" | "+getLocalIP()+" | ");
            firmWare.setBackgroundColor(Color.parseColor("#595959"));

            //Signal_Strength
            int simState = tel.getSimState();
            switch (simState) {
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    SimState = "UNKNOWN";
                    break;
            }
            if(SimState == null){
                tel.listen(new PhoneState(this), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }else {
                signalInfo.setText("Not available");
            }
        } catch (Exception e) {
            Log.d("Exception",e.toString());
        }
    }

    public void noImsiPopUp(){
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this);
        builder.setMessage(
                "No IMSI detected. Please check your sim card")
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                dialog.dismiss();
                                //finish();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static class EmmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Global.EmmStatus = intent.getStringExtra("EmmStatus");
        }
    }

    public void AgentIntent(){
        intent = getPackageManager().getLaunchIntentForPackage("org.wso2.emm.agent");
        if (intent != null) {
            startActivity(intent);
        }
    }

    public void startAgent(){
        try {
            intent = new Intent();
            intent.setComponent(new ComponentName("org.wso2.emm.agent", "org.wso2.emm.agent.BroadcastService"));
            startService(intent);
        }catch (Exception ex){
            Log.d(TAG,"broadcastExeception: "+ex.toString());
        }
    }

    // ********************************************************************************
    // added for OTA update - check the version of the currently running app
    // *******************************************************************************
    private void checkCurrentVersion() {
        try {
            PackageInfo pi = getPackageManager().getPackageInfo("my.com.tm.swift",
                    PackageManager.GET_META_DATA);
            currentVersionCode = pi.versionCode;
            currentVersionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void checkMyWhiteList(String strData[][]) {
        try {
            Log.i("checkWhiteList", "Start checkWhiteList");
            if (strData == null) {
                Log.i("checkWhiteList", "StrData Null");
            } else {
                int cApp = 0;
                StringBuilder blApp = new StringBuilder();
                String wlApp = "";
                PackageManager packageManager = getPackageManager();
                List<ApplicationInfo> applist = packageManager
                        .getInstalledApplications(0);
                int i = 0;
                int jum = strData.length;
                for (ApplicationInfo app : applist) {
                    if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
                        // installedApps.add(app);
                    } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                    } else {
                        // installedApps.add(app);
                        String label = (String) packageManager
                                .getApplicationLabel(app);
                        label = label.toUpperCase();
                        for (i = 0; i < jum; i++) {
                            String arrStr = strData[i][1];
                            if (label.equals(arrStr.toUpperCase())) {
                                Log.i(TAG, "label=" + label);
                                wlApp = label;
                            } else {
                                Log.i(TAG, "arrStr=" + arrStr);
                            }
                        }
                        if (wlApp.isEmpty()) {
                            cApp += 1;
                            if (cApp == 1) {
                                blApp = new StringBuilder(cApp + ". " + label);
                            } else {
                                if (cApp <= 21) {
                                    if (cApp == 21) {
                                        blApp.append("\n\n(More than 20 applications detected)");
                                    } else {
                                        blApp.append("\n").append(cApp).append(". ").append(label);
                                    }
                                }
                            }
                        }
                        wlApp = "";
                    }
                }
                Log.i(TAG, "blApp=" + blApp);
                Log.i(TAG, "cApp=" + cApp);
                Global.NumberOFUnauthorize = String.valueOf(cApp);
                if (cApp > 0) {
                    StringBuilder build = new StringBuilder();
                    build.append(
                            "Unauthorized applications have been detected:\n")
                            .append("\n").append(blApp).append("\n")
                            .append("   ");
                    AlertDialog customBuilder = new AlertDialog.Builder(this).create();
                    customBuilder.setTitle("Warning!");
                    customBuilder.setMessage(build.toString());
                    customBuilder.setButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    });
                    customBuilder.show();
                } else {
                    Global.NumberOFUnauthorize = "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckLoginServer() throws Exception {
        customBuilder = new AlertDialog.Builder(this);
        username = userField.getText().toString();
        password = passField.getText().toString();
        UsernamePreference(username);
        if (InitTaskRunning) {
            Toast.makeText(
                    this,
                    "Please wait while the application is being initialized...",
                    Toast.LENGTH_LONG).show();

        } else {
            //no_username
            if (username.length() == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        this);
                builder.setMessage("Please Key In Your Username")
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                    }
                                });
                builder.show();
            }
        }

        if (username.regionMatches(0, "*", 0, 1)) {
            Global.loginServer = "SIT-IGRID";// pian tambah
            Global.URLSwift = "http://10.54.7.214/";
            Global.usernameBB = username.substring(1, username.length());
            Global.passwordBB = password;
        } else if (this.username.regionMatches(0, "#", 0, 1)) {
            Global.loginServer = "REG-IGRID";// pian tambah
            Global.URLSwift = "http://10.54.97.216/";
            Global.usernameBB = username.substring(1, username.length());
            Global.passwordBB = password;
        } else if (this.username.regionMatches(0, "@", 0, 1)) {
            Global.loginServer = "REG";// pian tambah
            Global.URLSwift = "http://10.41.102.81/"; // ahmad ubah
            Global.usernameBB = username.substring(1, username.length());
            Global.passwordBB = password;
        } else if (this.username.regionMatches(0, "$", 0, 1)) {
            Global.loginServer = "SIT";// ahmad tambah
            Global.URLSwift = "http://10.41.102.70/";
            Global.usernameBB = this.username.substring(1, username.length());
            Global.passwordBB = password;
        } else if (username.regionMatches(0, "!", 0, 1)) {
            Global.loginServer = "PRO";// ahmad tambah
            Global.loginServerLevel = "SUPPORT";
            Global.URLSwift = "http://10.41.102.70/";
            Global.usernameBB = username.substring(1, username.length());
            Global.passwordBB = password;
        } else {
            // production
            Global.loginServer = "PRO";// pian tambah
            Global.URLSwift = "http://10.41.102.70/";
            Global.usernameBB = username;
            Global.passwordBB = password;
        }
        if (Global.ServerStatus.contains("Connected to")) {

            LoginTask = new LoginTaskAsync(Global.usernameBB, password);

            // start pian tambah 8/7/2013
            if(Global.loginServer.equals("REG-IGRID")) {
                Global.UrlLogin = urlLogin; //sit
                //Global.URLAuthenticate = "http://10.44.11.64:8008/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                LoginTask.execute();
            } else if (Global.loginServer.equals("SIT-IGRID")) {
                Global.UrlLogin = urlLogin; //sit
                //Global.URLAuthenticate = "http://10.44.11.6:8008/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                LoginTask.execute();
            } else if (Global.loginServer.equals("REG")) {
                Global.UrlLogin = urlLogin; //sit
                //Global.URLAuthenticate = "http://10.41.102.81:8080/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                LoginTask.execute();
            } else if (Global.loginServer.equals("SIT")) {
                if (Global.connectedToWiFi) {
                    Global.UrlLogin = urlLogin; //sit
                    //Global.URLAuthenticate = "http://10.106.132.7:8088/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                    LoginTask.execute();
                } else {
                    Global.UrlLogin = urlLogin; //sit
                    //Global.URLAuthenticate = "http://10.106.132.7:8088/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                    LoginTask.execute();
                }
            } else if (Global.loginServer.equals("PRO")) {
                Global.UrlLogin = urlLogin;//prod
                //Global.URLAuthenticate = "http://10.41.102.70:8080/FLSWIFT_DEVICE_LOGIN/DeviceLoginWSService?wsdl";
                LoginTask.execute();
            } else {
                Global.URLAuthenticate = "";
            }// end pian tambah 8/7/2013
            Log.d(TAG,"Check UrlLogin: "+Global.UrlLogin);
            if (password.regionMatches(0, "+", 0, 1))
                password = password.substring(1,password.length());
            else if (password.regionMatches(0, "*", 0, 1))
                password = password.substring(1,password.length());
            else if (password.regionMatches(0, "#", 0, 1))
                password = password.substring(1,password.length());
            else if (password.regionMatches(0, "$", 0, 1))
                password = password.substring(1,password.length());
            else if (password.regionMatches(0, "@", 0, 1))
                password = password.substring(1,password.length());
            insertLog("Login " + username);
        }else{
            try {
                List<UserDetail> userDetails = Global.mySQLiteAdapter.getAllContacts();
                for (UserDetail con : userDetails) {
                    Global.getToken = con.get_token();
                }
                Log.d(TAG,"tokenDB: "+Global.getToken);
                if (Global.getToken == "") {
                    customBuilder
                            .setMessage("Sorry, your token are not available."
                                    + " Please run test server connection.")
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    });
                    customBuilder.show();
                    clearField();
                }else {
                    jwtDecode.decoded();
                    long convExp = Long.parseLong(jwtDecode.getExp());
                    if(currentTime >= convExp){
                        customBuilder
                                .setMessage("Your token was already expired. "
                                        + "Please run test server connection.")
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(
                                                    DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                        customBuilder.show();
                        clearField();
                    }else{
                        boolean checkStaffId = Global.usernameBB.equals(jwtDecode.getStaffId());
                        boolean checkPassword = Global.passwordBB.equals(jwtDecode.getPassword());
                        boolean checkImei = Global.IMEIPhone.equals(jwtDecode.getImei());
                        boolean checkImsi = Global.IMSIsimCardPhone.equals(jwtDecode.getImsi());
                        if(checkStaffId && checkPassword && checkImei && checkImsi){
                            intentLogin();
                        }else {
                            customBuilder
                                    .setMessage("Staff ID and Password does not match with token."
                                            + " Please run test server connection.")
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(
                                                        DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                            customBuilder.show();
                            clearField();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG,"LoginServer: "+e);
            }
        }
    }

    private class LoginTaskAsync extends AsyncTask<Void, Void, String> {
        private final String actualUsername;
        private final String password;
        String json;

        public LoginTaskAsync(String actualUsername, String password) {
            this.actualUsername = actualUsername;
            this.password = password;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            myTimer.cancel();
            Log.d("LoginTask", "Sign In cancelled due to timeout");
            // ahmad to prevent crash
            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
            FailedCallBack(DataMobile + "Network or Connection Error");
        }

        @Override
        protected void onPreExecute() {
            myTimer = new Timer();
            // timercount = 0;
            myTimer.schedule(new TimerTaskMethod(), timeout);
            pd = ProgressDialog.show(MainActivity.this, "",
                    "Signing In.... Please Wait...", true, false);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d("login", "capp" + Global.NumberOFUnauthorize);
            String result = "";
            customBuilder = new AlertDialog.Builder(context);
            sh = new HttpHandler();
            handler =  new Handler(context.getMainLooper());
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(),timeout); //Timeout Limit
            HttpResponse httpResponse;
            HttpPost httpPost;
            objLogin = new JSONObject();
            try {
                httpPost = new HttpPost(Global.UrlLogin);
                objLogin.put("staff_no",actualUsername);
                objLogin.put("password",password);
                objLogin.put("imei",Global.IMEIPhone);
                objLogin.put("imsi",Global.IMSIsimCardPhone);
                objLogin.put("super_flag",Global.NumberOFUnauthorize);
                objLogin.put("app_ver",currentVersionCode);
                objLogin.put("firm_ver",Global.frmVersion);
                json = objLogin.toString();
                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);
                httpResponse = client.execute(httpPost);
                Log.d(TAG, "RequestApi: "+json);
                Log.d(TAG,"CheckUrlLogin: "+Global.UrlLogin);
                    /*Checking response */
                if(httpResponse!=null){
                    InputStream in = httpResponse.getEntity().getContent();
                    result = InputStreamToString(in);
                }else{
                    result = "Did not work!";
                }
            } catch(Exception e) {
                Log.d("LoginTaskAsync", "InputStream: "+e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String Result) {
            myTimer.cancel();
            Log.d(TAG,"ResponseApi: "+Result);
            Global.responseResult = Result;
            Global.loginResult = Result.toString().contains("SUCCESS");
            if (Global.loginResult) {
                LoginParams();
            }else {
                String mgs = ("Invalid Username or Password."
                        + " Please re-enter your login info or report at : "
                        + "http://10.45.3.139/tmdms/default");
                customBuilder.setMessage(mgs)
                             .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                        dialog.dismiss();
                                    }
                             });
                     customBuilder.show();
                clearField();
            }

            if (pd != null) {
                if (pd.isShowing()) {
                    pd.dismiss();
                }
            }
            super.onPostExecute(Result);
        }
    }

    public void LoginParams(){
        try {
                objLogin = new JSONObject(Global.responseResult);
                Global.ldapStatus = objLogin.getString("LdapStatus");
                Global.staffIcNo = objLogin.getString("IcNo");
                Global.staffName = objLogin.getString("StaffName");
                Global.AppVersion = objLogin.getString("AppName");
                Global.AppCode = objLogin.getString("AppVersion");
                Global.AppVersionLink = objLogin.getString("AppVersionLink");
                Global.AppSize = objLogin.getString("AppSize");
                Global.UserType = objLogin.getString("LoginStatus");
                Global.getToken = jwtEncode.creteToken();
                intentLogin();
                SuccessCallBack("Successfully Login");
        }catch (JSONException e){
            Log.e("JSONException: ",e.toString());
        }catch (NullPointerException e){
            Log.e(TAG, "JSONNull: "+String.valueOf(e));
        }catch (Exception e){
            Log.e(TAG, "Exception: "+String.valueOf(e));
        }
        Log.d(TAG,"MyToken="+Global.getToken);
        if (Global.mySQLiteAdapter.isLoginExists(Global.usernameBB)) {
            Global.mySQLiteAdapter.updateContact(new UserDetail(Global.usernameBB,Global.getToken,Global.ldapStatus));
            Log.d("QueryLogin: ", "Update");
        } else {
            Global.mySQLiteAdapter.insertContact(new UserDetail(Global.usernameBB,Global.getToken,Global.ldapStatus));
            Log.d("QueryLogin: ", "Insert");
        }
    }

    public void LoginToken(){
        List<UserDetail> userDetails = Global.mySQLiteAdapter.getAllContacts();
        for (UserDetail con : userDetails) {
            Global.getToken = con.get_token();
        }
        Log.d(TAG,"tokenDB: "+Global.getToken);
        if (Global.getToken == "") {
            Log.d(TAG,"Token Status=Token Not Exist");
        }else {
            try {
                jwtDecode.decoded();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long convExp = Long.parseLong(jwtDecode.getExp());
            if(currentTime < convExp){
                Log.d(TAG,"Token Status=Token Valid");
                DecodeToken();
            }else{
                Log.d(TAG,"Token Status=Token Expired");
            }
        }
    }

    public void DecodeToken(){
        try {
            jwtDecode.decoded();
            Global.usernameBB = jwtDecode.getStaffId();
            Global.passwordBB = jwtDecode.getPassword();
            Global.loginServer = jwtDecode.getEnvironment();
            Global.staffIcNo = jwtDecode.getIcNumber();
            Global.staffName = jwtDecode.getName();
            Global.UserType = jwtDecode.getUserType();
            Global.IMEIPhone = jwtDecode.getImei();
            Global.IMSIsimCardPhone = jwtDecode.getImsi();
            Global.frmVersion = jwtDecode.getFirmVer();
            intentLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void intentLogin(){
        i = new Intent(context,HomeScreen.class);
        i.addCategory(Intent.CATEGORY_HOME);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("username",username);
        i.putExtra("dataStaff",Global.usernameBB);
        i.putExtra("password",Global.passwordBB);
        i.putExtra("imei",Global.IMEIPhone);
        i.putExtra("imsi",Global.IMSIsimCardPhone);
        i.putExtra("firm_ver",Global.frmVersion);
        i.putExtra("loginServer",Global.loginServer);
        i.putExtra("loginType",Global.UserType);
        i.putExtra("strVersion",Global.strVersion);
        context.startActivity(i);
    }

    // Process after failed login to SWIFT
    public void FailedCallBack(String TextResult) {
        insertLog("FailedCallBack " + TextResult.toString());
        MessageFailDialog = TextResult.toString();

        if (logininvisible)
            readytoshowdialog = true;

        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(MessageFailDialog)
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    clearField();
                                    dialog.cancel();
                                    // MyActivity.this.finish();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    // Class timer check if mobile testnet error
    class TimerTaskMethod extends TimerTask {

        int datamobile = 4;

        public void run() {
            MainActivity.this.runOnUiThread(RunnableTimerMethod);
        }
    }

    private Runnable RunnableTimerMethod = new Runnable() {
        public void run() {
            myTimer.cancel();
            // timercount = 0;
            CheckNetworkRunning = false;
            checkServerRunning = false;
            // Log.d("SignIn",
            // "LoginTask cancelled due to timeout(60 Seconds) ");
            LoginTask.cancel(true);
        }
    };

    private static String InputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : getAPN
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    String getAPN() {
        String numeric = "";
        try {
            final Uri PREFERRED_APN_URI = Uri
                    .parse("content://telephony/carriers/preferapn");
            Cursor c = getContentResolver().query(PREFERRED_APN_URI, null,
                    null, null, null);
            c.moveToFirst();
            int index = c.getColumnIndex("numeric");
            numeric = c.getString(index);
            Log.d("getAPN", numeric);
        } catch (Exception e) {
            Log.d("getAPN", e.toString());
        }
        return numeric;
    }

    public class getWSWhiteList extends AsyncTask<Void, Void, String> {
        String bilID,blackListNum,WlName,WlPackage;
        String _dtUpdateWhiteList = "-";
        String _dtUpdateBlackList = "-";
        String strItem = "", strDate = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            String blnTask = getData();
            return blnTask;
        }

        private String getData() {
            sh = new HttpHandler();
            jsonStr = sh.makeServiceCall(urlConfig);
            Log.d(TAG, "Response from url: " + jsonStr);
            try {
                objLogin = new JSONObject(jsonStr);
                arrayLogin = objLogin.getJSONArray("MODULEDATA");
                Log.d(TAG,"JsonData: "+arrayLogin);
                for (int i = 0; i < arrayLogin.length(); i++) {
                    JSONObject c = arrayLogin.getJSONObject(i);
                    strItem = c.getString("MODULECODE");
                    strDate = c.getString("DATE");
                    Log.d(TAG,"JsonData: "+strItem);
                    if(strItem.equals("WL")) {
                        _dtUpdateWhiteList = strDate;
                    } else if (strItem.equals("BL")) {
                        _dtUpdateBlackList = strDate;
                    }
                }
                long insertResult = Global.mySQLiteAdapter.insertSummaryUpdate(_dtUpdateWhiteList, _dtUpdateBlackList);
                if (insertResult == -1) {
                    Log.e(TAG, "XSuccess insert");
                } else {
                    Log.i(TAG, "Success insert");
                }

                objLogin = new JSONObject(jsonStr);
                arrayLogin = objLogin.getJSONArray("BLACKLIST");
                for (int i = 0; i < arrayLogin.length(); i++) {
                    JSONObject c = arrayLogin.getJSONObject(i);
                    bilID = c.getString("BL_ID");
                    blackListNum = c.getString("HPNO");
                    Log.d(TAG, "CheckValue: " + "Id: "+bilID +" Number: "+ blackListNum);
                    if (Global.mySQLiteAdapter.isBlackListExist(bilID)) {
                        Global.mySQLiteAdapter.updateBlackList(new BlackList(bilID,blackListNum));
                        Log.d("SqliteQuery: ", "UpdateBlackList");
                    } else {
                        Global.mySQLiteAdapter.insertBlackList(new BlackList(bilID,blackListNum));
                        Log.d("SqliteQuery: ", "InsertBlackList");
                    }
                }
                List<BlackList> blackList = Global.mySQLiteAdapter.getAllBlackList();
                for (BlackList bl : blackList) {
                    String log = "Id: " + bl.getId() + " ,BlackListNum: " + bl.getBlackListNumber();
                    Log.d("SqliteQueryLog: ", log);
                }

                objLogin = new JSONObject(jsonStr);
                arrayLogin = objLogin.getJSONArray("WHITELIST");
                for (int i = 0; i < arrayLogin.length(); i++) {
                    JSONObject c = arrayLogin.getJSONObject(i);
                    WlName = c.getString("NAME");
                    WlPackage = c.getString("PACKAGE");
                    if (Global.mySQLiteAdapter.isWhiteListExist(WlName)) {
                        Global.mySQLiteAdapter.updateWhileList(new WhileList(WlName,WlPackage));
                        Log.d("SqliteQuery: ", "UpdateWhiteList");
                    } else {
                        Global.mySQLiteAdapter.insertWhileList(new WhileList(WlName,WlPackage));
                        Log.d("SqliteQuery: ", "InsertWhiteList");
                    }
                }
            }catch (JSONException e){
                Log.d(TAG, "JSONExcept: "+String.valueOf(e));
            }catch (NullPointerException e){
                Log.d(TAG, "JSONNull: "+String.valueOf(e));
            }
            return null;
        }
    }

    public Runnable enableInstallation = new Runnable() {

        @Override
        public void run() {

            Socket socket = null;
            DataOutputStream dataOutputStream = null;

            try {
                socket = new Socket("127.0.0.1", 8888);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataOutputStream.writeUTF("!RC?TEMPSL");

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                if (socket != null) {

                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                if (dataOutputStream != null) {

                    try {
                        dataOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    };

    public void showApps(View v) throws Exception {
        CheckLoginServer();
    }

    public void testConnect(View v){
        try {
            Toast.makeText(MainActivity.this, "IP: " + getLocalIP(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DisplayUsername = ((EditText) findViewById(R.id.username))
                .getText().toString();

        if (checkServerRunning) {
            pd = ProgressDialog
                    .show(MainActivity.this, "",
                            "Please Wait... Testing Server Connection",
                            true, false);
            long delayInMillis = 1000;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    pd.dismiss();
                }
            }, delayInMillis);
        } else if (!checkServerRunning) {
            checkServerRunning = true;
            if (Global.connectedToWiFi) {
                MainActivity.CheckServerStatus myCheckServerStatus = new MainActivity.CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (Global.connected3G) {
                MainActivity.CheckServerStatus myCheckServerStatus = new MainActivity.CheckServerStatus();
                myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public void helpBtn(View v){
        helpPopUp();
    }

    public void Dialer(View v){
        Intent launchDialer = getPackageManager().getLaunchIntentForPackage("com.android.dialer");
        startActivity(launchDialer);
    }

    public void helpPopUp(){
        final String url = "http://10.45.3.139/tmdms/default";
        String title = "How to use CMS:";
        String mgs = "1. Goto "+url+"\n"
                + "2. Click On Contact Us\n"
                + "3. Key In Swift ID\n"
                + "4. If Swift ID Valid, e.g TM35170, Problem " +
                "\u00A0\u00A0\u00A0\u00A0Type will be enabled\n"
                + "5. Select Problem Type\n"
                + "6. Select Sub Problem Type\n"
                + "7. Enter Detail Problem Description\n"
                + "8. Click Submit\n";
        customBuilder.setTitle(title);
        customBuilder.setMessage(mgs)
                .setPositiveButton("Go Link", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                });
        customBuilder.show();
    }

    public void clearField(){
        userField.requestFocus();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        passField.setText(null);
    }

    @Override
    public void onBackPressed() {
        //nothing
    }

    // CHECKBOX
    class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {

            Log.i("CheckBoxListener", "isChecked = " + isChecked);

            if (isChecked) {
                passField.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

            } else {
                passField.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            }

            // move cursor to end of text
            passField.setSelection(passField.getText().length());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            ConnectivityManager connMgr = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            logininvisible = false;

            Global.LogAsAdmin = false;

            // 2014-09-25 amir debug
            Global.lastAlertTime = new Date(1977, 4, 17);
            // moved to here by amir 2013-01-14
            // added by amir 2012-12-20
            Global.detailsStillEmpty = true;
            // added by amir 2013-01-02
            Global.AssignInprogressCount = 0;
            Global.OutstandingMA = 0;// pian tambah 11/07/2013
            Global.TTApointList = "";
            Global.FFreqSummDate = "";
            Global.FFAssignCount = 0;
            Global.allTaskCounter = 0;
            Global.countCF = 0;

            if (Global.FirstTimeRunLogin) {
                if (!InitTaskRunning) {
                    Log.d("Login",
                            "FirstTimeRunLogin true & InitTaskRunning false");
                    pdinit = ProgressDialog.show(MainActivity.this, "",
                            "Please wait for system initialization");

                    (new InitTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Login", "Error onstart " + e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(networkStateReceiver);
            //unregisterReceiver(downloadReceiver);
            CheckNetworkTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // unregisterReceiver(mIntentReceiver);
            // cancel current download if exiting bumblebee
            deviceOperate.unregisterReceiver(this);
            if (Global.waitingForDownload) {
                Global.waitingForDownload = false;
                Global.updateReady = false;
                DownloadManager myDM = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                myDM.remove(Global.downloadID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : CheckNetworkTimerMethod
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    class CheckNetworkTimerMethod extends TimerTask {
        public void run() {
            if (!CheckNetworkRunning) {
                CheckNetworkRunning = true;
                if (Global.connectedToWiFi) {
                    CheckServerStatus myCheckServerStatus = new CheckServerStatus();
                    myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } else if (Global.connected3G) {
                    CheckServerStatus myCheckServerStatus = new CheckServerStatus();
                    myCheckServerStatus.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : queryNetwork
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    private void queryNetwork() {
        String netPre = "";
        try {
            myStatus = "Network: ";
            ConnectivityManager connMgr = (ConnectivityManager) this
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            Global.connected3G = mobile.isConnected();
            Global.connectedToWiFi = wifi.isConnected();
            // added by amir on 2013-01-08

            if (Global.connected3G || Global.connectedToWiFi) {
                Global.localIP = getLocalIP();
            }

            String activeConn;
            if (connMgr.getActiveNetworkInfo() != null) {
                activeConn = connMgr.getActiveNetworkInfo().getSubtypeName();
            } else {
                activeConn = "NONE";
            }

            switch (activeConn) {
                case "LTE":
                    netPre = "4G";
                    break;
                case "EDGE":
                    netPre = "E";
                    break;
                default:
                    netPre = "3G";
                    break;
            }

            String activeConnPlus = activeConn;
            if (Global.connectedToWiFi) {
                activeConnPlus = "WIFI ";
                Global.netType = "WIFI/" + deviceOperate.getWifiSsid();
            }else if (Global.connected3G) {
                // Global.URLSwift = "http://10.41.102.70/";
                activeConnPlus += "";
                Global.netType = netPre+"/"+activeConnPlus;
            }else if ((!Global.connectedToWiFi) && (!Global.connected3G)) {
                activeConnPlus = "None";
                Global.ServerStatus = "Not Connected";
                Global.netType = activeConnPlus;
            }
            Log.d(TAG,"netType="+Global.netType);

            myStatus += activeConnPlus;
            myScroller.setText(myStatus + "  |  Server: "
                    + Global.ServerStatus);

            if(Global.connected3G){
                if(Global.EmmStatus.equals("Not Active")){
                    Log.d(TAG,"EmmStatus: "+"Intent Agent");
                    AgentIntent();
                }
            }

            if (Global.ServerStatus.contains("Not Connected")) {
                myScroller.setBackgroundColor(Color.RED);
            } else if (Global.ServerStatus.contains("Unknown")) {
                myScroller.setBackgroundColor(Color.RED);
            } else {
                if (activeConnPlus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
        } catch (Exception e) {
            Log.e("Login", e.toString());

        }

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : insertLog
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public void insertLog(String infoLine) {
        try {
            SimpleDateFormat cdtFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            String cdtString = cdtFormat.format(new Date());

            String currentDate = (new SimpleDateFormat("yyyyMMdd")
                    .format(new Date()));
            String currentPath = Environment.getExternalStorageDirectory()
                    + "/tmswiftlog_" + currentDate + ".log";

            File currentFile = new File(currentPath);

            // if file does not exist
            if (!currentFile.exists()) {
                // create the file first
                currentFile.createNewFile();
                Log.d("LOGFILE", "New File: " + currentPath);
            }

            // write to file
            FileWriter logFileWritter = new FileWriter(currentFile, true);
            BufferedWriter logBufferWritter = new BufferedWriter(logFileWritter);
            logBufferWritter.write(cdtString + " : " + infoLine + "\n");
            logBufferWritter.close();

            Log.d("LOGFILE", "Append: " + infoLine);
        } catch (Exception e) {
            Log.e("LOGFILE", "Error: " + e.toString());
            e.printStackTrace();
        }

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : getLocalIP
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public String getLocalIP() {
        Boolean useIPv4 = true; // only looks for IPv4 address
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port
                                // suffix
                                return delim < 0 ? sAddr : sAddr.substring(0,
                                        delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("getLocalIP", ex.toString());
        }
        return "Not available";
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : CheckServerStatus
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public class CheckServerStatus extends AsyncTask<Void, Void, Void> {

        String ServerStatus;
        String ServerTime;

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
                        Global.URLSwift = urlSwift;
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

                    if (ServerStatus.contains("OK")) {
                        Global.ServerStatus = "Connected to " + ServerName;
                        Global.ServerDate = ServerTime;
                        Log.d(TAG,"CheckServerStatus:"+" HTTP check OK "
                                + Serveradd);
                    } else {
                        Global.ServerStatus = "Not Connected";
                        Log.d(TAG,"CheckServerStatus:"+ " HTTP check Not OK "
                                + Serveradd + DisplayUsername);
                        if (Global.currentAPN.contains("Maxis")
                                && Global.connected3G) {
                            PingServerStatus myPingServerStatus = new PingServerStatus();
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
                                            if (myStatus.contains("WIFI")) {

                                                myScroller
                                                        .setBackgroundColor(Color

                                                                .parseColor("#FF8000"));

                                            } else if (Global.connected3G) {

                                                myScroller
                                                        .setBackgroundColor(Color

                                                                .parseColor("#210B61"));

                                            }

                                        }

                                        myScroller.setText(myStatus
                                                + "  |  Server: "
                                                + Global.ServerStatus);

                                    }

                                });
                    }
                } else {
                    // Log.e("Login","SIT");
                    if (DisplayUsername.contains("*") && Global.connectedToWiFi) {
                        Global.ServerStatus = "Connected to gponems";

                        if (Global.loginContext != null) {
                            ((Activity) Global.loginContext)
                                    .runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            myScroller.setBackgroundColor(Color
                                                    .parseColor("#FF8000"));
                                            myScroller.setText(myStatus
                                                    + "  |  Server: "
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
                    PingServerStatus myPingServerStatus = new PingServerStatus();
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
                                        if (myStatus.contains("WIFI")) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#FF8000"));

                                        } else if (Global.connected3G) {
                                            myScroller.setBackgroundColor(Color

                                                    .parseColor("#210B61"));

                                        }

                                    }

                                    myScroller.setText(myStatus
                                            + "  |  Server: "
                                            + Global.ServerStatus);

                                }

                            });
                }

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

            CheckNetworkRunning = false;
            if (checkServerRunning) {
                checkServerRunning = false;
                if (pd != null) {
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                }
                Toast.makeText(MainActivity.this,
                        "Server Status: " + Global.ServerStatus,
                        Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }

        private void ServerStatusExtract(String strData) {
            try {
                // Log.i("Pian","ServerData="+strData);
                int start = 0;
                int end = 0;
                ServerName = "";
                start = strData.indexOf("<serverName>");
                end = strData.indexOf("</serverName>");
                ServerName = strData.substring(start + 12, end - 15);
                //Log.i("Pian","ServerName="+strData);
                start = strData.indexOf("<serverStatus>");
                end = strData.indexOf("</serverStatus>");
                ServerStatus = strData.substring(start + 14, end);
                // Log.i("Pian","ServerStatus="+ServerStatus);
                start = strData.indexOf("<serverTime>");
                end = strData.indexOf("</serverTime>");
                ServerTime = strData.substring(start + 12, end);
                // Log.i("Pian","ServerTime="+ServerTime);
                // start-07/05/2015 @Pian - tarikh dari server untuk req/update
                // TT dan FF
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
                // end-07/05/2015 @Pian - tarikh dari server untuk req/update TT
                // dan FF
            } catch (Exception e) {
                Log.i("ServerStatusException", e.toString());
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION NAME : PingServerStatus
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public class PingServerStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                if (Global.connected3G) {
                    String pingCmd = "ping -c 1 -w 25 -s 1 " + getLocalIP();
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
                        myStatus = "Network: Ping Fail ";

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
                                            if (myStatus.contains("WIFI")) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#FF8000"));
                                            } else if (Global.connected3G) {
                                                myScroller
                                                        .setBackgroundColor(Color
                                                                .parseColor("#210B61"));
                                            }

                                        }

                                        myScroller.setText(myStatus
                                                + "  |  Server: "
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
                myStatus = "Network: Ping Fail ";

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
                                        if (myStatus.contains("WIFI")) {
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
                                    myScroller.setText(myStatus
                                            + "  |  Server: "
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
                queryNetwork();
                DeviceDetail();
            } else {
                CheckNetworkRunning = false;
                if (checkServerRunning) {
                    checkServerRunning = false;
                    if (pd != null) {
                        if (pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                    Toast.makeText(MainActivity.this, "Network Status: Ping Fail",
                            Toast.LENGTH_SHORT).show();
                }
            }
            super.onPostExecute(result);
        }
    }

    private class InitTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // disable login button here
            Log.d("InitTask", "Start");
            InitTaskRunning = true;
            if (Global.LogAsAdmin == true) {
                Global.LogAsAdmin = false;
            }
            queryNetwork();
            DeviceDetail();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Global.ServerStatus = "Connected";
            // myScroller
            // .setBackgroundColor(Color
            // .parseColor("#210B61"));
            Log.d("InitTask", "doInBackground");

            if (!DisplayUsername.contains("*") && Global.connected3G) {
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
                        if (DisplayUsername.contentEquals("TM")) {
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                        } else if (DisplayUsername.contains("*")) {
                            // Global.URLSwift = "http://58.26.233.1:8080/";
                            Global.URLSwift = "http://swift.tmrnd.com.my:8080/";
                            // Global.URLSwift =
                            // "http://swift.tmrnd.com.my:8080/";
                        } else if (DisplayUsername.contains("#")) {
                            Global.URLSwift = "http://10.44.11.64:8090/";
                        } else if (DisplayUsername.contains("$")) {
                            Global.URLSwift = "http://10.106.132.7/";
                        } else if (DisplayUsername.contains("@")) {
                            Global.URLSwift = "http://10.41.102.81/";
                        } else if (DisplayUsername.contains("!")) {
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

                        if (DisplayUsername.contains("@")
                                || DisplayUsername.contains("$")) {
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
                            // "Date mismatch, server: "
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
                            // "Date OK, server: "
                            // + sdfDate.format(serverDate)
                            // + ", device: "
                            // + sdfDate.format(currentDate));
                            dateMismatch = false;
                        }

                    } catch (Exception e) {
                        Log.e("InitTask", e.toString());
                        Global.ServerStatus = "Not Connected";
                        Log.e("InitTask", "Not Connected" + Serveradd
                                + DisplayUsername);

                    }

                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            InitTaskRunning = false;

            // ahmad to prevent crash
            if (pdinit != null) {
                if (pdinit.isShowing()) {
                    pdinit.dismiss();
                }
            }

            if (Global.ServerStatus.contains("Not Connected")) {
                myScroller.setBackgroundColor(Color.RED);
            } else if (Global.ServerStatus.contains("Unknown")) {
                myScroller.setBackgroundColor(Color.RED);
            } else {
                if (myStatus.contains("WIFI")) {
                    myScroller.setBackgroundColor(Color.parseColor("#FF8000"));
                } else if (Global.connected3G) {
                    myScroller.setBackgroundColor(Color.parseColor("#210B61"));
                }
            }
            myScroller.setText(myStatus + "|Server: "
                    + Global.ServerStatus);

            // re-enable login button here
            // loginButton.setEnabled(true);
            if (dateMismatch) {
                if (!mismatchDialogDisplayed) {
                    mismatchDialogDisplayed = true;
                    String MyMessage = "Please check and correct the date & time in your device\nServer date & time: "
                            + serverDateStr;
                    new AlertDialog.Builder(MainActivity.this)
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
                                            startActivity(new Intent(
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

    // To save username in preference editor
    private void UsernamePreference(String PreferUsername) {
        SharedPreferences app_preferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putString("usernamePref", PreferUsername);
        editor.commit();
    }

    // Process after successes login to SWIFT
    public void SuccessCallBack(String TextResult) {
        Log.d(TAG, "SuccessCallBack: " + TextResult);
        if (logininvisible) {
            readytogotomainmenu = true;
        }else {
            Global.MustPassLockScreen = false;
            Global.LoginMonitor = false;
            //CheckGPSProvider();
        }
    }
}