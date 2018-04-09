package com.plamera.tmswiftlauncher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.plamera.tmswiftlauncher.Device.DeviceOperate;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class TrackLogService extends Service {
    private static String LOG_TAG;
    private Context context;
    DeviceOperate device;
    Timer timer;
    HttpHandler sh;
    Handler handler;
    JSONObject objLogin,jsonMain;
    String result,json,batteryParam;
    int TIMEOUT = 300000;
    String url = "http://10.54.97.227:9763/EMMWebService/device_operate";

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        LOG_TAG = this.getClass().getSimpleName();
        timer = new Timer();
        handler = new Handler();
        device = new DeviceOperate(this);
        setTimer();
    }

    public void setTimer(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.i(LOG_TAG, "In setTimer");
                            new DeviceOperateAsync().execute();
                        } catch (Exception e) {
                            Log.d(LOG_TAG,"Exception: "+e);
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, TIMEOUT);
    }

    public class DeviceOperateAsync extends AsyncTask<Void, Void, String>{

        public DeviceOperateAsync() {
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(LOG_TAG, "Network or Connection Error");
        }

        @Override
        protected String doInBackground(Void... voids) {
            batteryParam = Global.batLvl+"/"+Global.availMemory+"/"+Global.batState;
            sh = new HttpHandler();
            handler =  new Handler(context.getMainLooper());
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), TIMEOUT); //Timeout Limit
            HttpResponse httpResponse;
            HttpPost httpPost;
            objLogin = new JSONObject();
            jsonMain = new JSONObject();
            try {
                httpPost = new HttpPost(url);
                objLogin.put("staff_no",Global.usernameBB);
                objLogin.put("ic_no",Global.staffIcNo);
                objLogin.put("imei",Global.IMEIPhone);
                objLogin.put("imsi",Global.IMSIsimCardPhone);
                objLogin.put("status",Global.status);
                objLogin.put("battery_status",batteryParam);
                objLogin.put("latitude",Global.latitude);
                objLogin.put("longitude",Global.longitude);
                objLogin.put("ip_address",Global.localIP);
                objLogin.put("ip_nettype",Global.netType);
                objLogin.put("app_ver",Global.strVersion);
                objLogin.put("lac_code",Global.getLac);
                objLogin.put("cell_id",Global.getCid);
                objLogin.put("firm_ver",Global.frmVersion);
                jsonMain.put("device_operate",objLogin);
                json = jsonMain.toString();
                StringEntity se = new StringEntity(json);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);
                httpResponse = client.execute(httpPost);
                Log.d(LOG_TAG, "DeviceApi: "+json);
                    /*Checking response */
                if(httpResponse!=null){
                    InputStream in = httpResponse.getEntity().getContent();
                    result = InputStreamToString(in);
                }else{
                    result = "Did not work!";
                }
            }catch (Exception e){
                Log.d(LOG_TAG, "InputStream: "+e.getLocalizedMessage());
            }
            return result;
        }
    }

    private static String InputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        StringBuilder result = new StringBuilder();
        while((line = bufferedReader.readLine()) != null)
            result.append(line);

        inputStream.close();
        return result.toString();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
