package com.plamera.tmswiftlauncher.JwtUtil;

import android.util.Base64;
import android.util.Log;

import com.plamera.tmswiftlauncher.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JwtDecode {
    static JSONObject obj;
    private String staffId;
    private String password;
    private String imei;
    private String imsi;
    private String icNumber;
    private String name;
    private String environment;
    private String userType;
    private String exp;
    private String firmVer;

    public void decoded() throws Exception {
        String TAG = "JWT_DECODED";
        try {
            String[] split = Global.getToken.split("\\.");
            Log.d(TAG, "Header: " + getJson(split[0]));
            Log.d(TAG, "Body: " + getJson(split[1]));
            jsonToString(getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException: "+String.valueOf(e));
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public void jsonToString(String result) {
        try {
            obj = new JSONObject(result);
            staffId = obj.getString("staffId");
            password = obj.getString("password");
            imei = obj.getString("imei");
            imsi = obj.getString("imsi");
            icNumber = obj.getString("icNumber");
            name = obj.getString("name");
            environment = obj.getString("environment");
            userType = obj.getString("loginStatus");
            exp = obj.getString("exp");
            firmVer = obj.getString("firmVer");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getStaffId() {
        return staffId;
    }

    public String getPassword() {
        return password;
    }

    public String getImei() {
        return imei;
    }

    public String getImsi() {
        return imsi;
    }

    public String getIcNumber() {
        return icNumber;
    }

    public String getName() {
        return name;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getUserType() {
        return userType;
    }

    public String getExp() {
        return exp;
    }

    public String getFirmVer() {
        return firmVer;
    }
}
