package com.plamera.tmswiftlauncher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.plamera.tmswiftlauncher.Encap.BlackList;
import com.plamera.tmswiftlauncher.Encap.UserDb;
import com.plamera.tmswiftlauncher.Encap.UserDetail;
import com.plamera.tmswiftlauncher.Encap.WhileList;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";
    private SQLiteDatabase db;
    ContentValues values;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "userManager";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserDb.CREATE_TABLE_LOGIN);
        db.execSQL(UserDb.CREATE_TABLE_BLACKLIST);
        db.execSQL(UserDb.CREATE_TABLE_WHITELIST);
        db.execSQL(UserDb.CREATE_TABLE_DEPLOY_FLAG);
        db.execSQL(UserDb.CREATE_TABLE_DEVICE_CONFIG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(UserDb.LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + UserDb.TABLE_LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + UserDb.TABLE_BLACKLIST);
        db.execSQL("DROP TABLE IF EXISTS " + UserDb.TABLE_WHITELIST);
        db.execSQL("DROP TABLE IF EXISTS "+ UserDb.TABLE_DEPLOY_FLAG);
        db.execSQL("DROP TABLE IF EXISTS "+ UserDb.TABLE_DEVICE_CONFIG);
        onCreate(db);
    }

    //staff_login table
    public void insertContact(UserDetail userDetail) {
        db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(UserDb.KEY_USER_NAME, userDetail.get_staffId());
        values.put(UserDb.KEY_TOKEN, userDetail.get_token());
        db.insert(UserDb.TABLE_LOGIN, null, values);
    }

    public List<UserDetail> getAllContacts() {
        List<UserDetail> userDetailList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + UserDb.TABLE_LOGIN;
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                UserDetail userDetail = new UserDetail();
                userDetail.set_staffId(cursor.getString(1));
                userDetail.set_token(cursor.getString(2));
                userDetailList.add(userDetail);
            } while (cursor.moveToNext());
        }
        return userDetailList;
    }

    public void updateContact(UserDetail userDetail) {
        db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(UserDb.KEY_TOKEN, userDetail.get_token());
        db.update(UserDb.TABLE_LOGIN, values, UserDb.KEY_USER_NAME + " = ?",
                new String[] { String.valueOf(userDetail.get_staffId())});
    }

    public void deleteContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(UserDb.TABLE_LOGIN, null, null);
    }

    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + UserDb.TABLE_LOGIN;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public boolean isLoginExists(String ActualUsername) {
        boolean hasTables = false;
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " +UserDb.TABLE_LOGIN
                + " WHERE "+UserDb.KEY_USER_NAME+" = "+"'"+ActualUsername+"'", null);
        if(cursor != null && cursor.getCount() > 0){
            hasTables=true;
            cursor.close();
        }
        return hasTables;
    }

    //table_blacklist
    public List<BlackList> getAllBlackList() {
        List<BlackList> allBlackList = new ArrayList<BlackList>();
        String selectQuery = "SELECT  * FROM " + UserDb.TABLE_BLACKLIST;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                BlackList blackList = new BlackList();
                blackList.setId(cursor.getString(0));
                blackList.setBlackListNumber(cursor.getString(1));
                allBlackList.add(blackList);
            } while (cursor.moveToNext());
        }
        return allBlackList;
    }



    public void insertBlackList(BlackList blackList) {
        db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(UserDb.KEY_ID, blackList.getId());
        values.put(UserDb.KEY_BLACK_LIST, blackList.getBlackListNumber());
        db.insert(UserDb.TABLE_BLACKLIST, null, values);
    }

    public void updateBlackList(BlackList bl) {
        db = this.getWritableDatabase();
        String id = bl.getId();
        String blackListNo = bl.getBlackListNumber();
        String query = "UPDATE "+UserDb.TABLE_BLACKLIST+" SET  blacklist_number = '"
                +blackListNo+"' Where id = "+"'"+id+"'";
        db.execSQL(query);
    }

    public int getBlackListCount() {
        String countQuery = "SELECT  * FROM " + UserDb.TABLE_BLACKLIST;
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            do {
                BlackList blackList = new BlackList();
                blackList.setId(cursor.getString(0));
                blackList.setBlackListNumber(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        return cursor.getCount();
    }

    public boolean getBlackListTable(String id) {
        String countQuery = "SELECT * FROM " + UserDb.TABLE_BLACKLIST + " Where "
                +UserDb.KEY_ID+"="+"'"+id+"'";
        db = this.getReadableDatabase();
        Log.d("BlackListDB","Query: "+countQuery);
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            do {
                BlackList blackList = new BlackList();
                blackList.setId(cursor.getString(0));
                blackList.setBlackListNumber(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        return true;
    }

    public boolean isBlackListExist(String id) {
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDb.TABLE_BLACKLIST +" Where id = '"+id+ "'", null);
        boolean exist = (cursor.getCount() > 0);
        cursor.close();
        return exist;
    }

    public boolean isWhiteListExist(String WlName) {
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + UserDb.TABLE_WHITELIST +" Where name = '"+WlName+ "'", null);
        boolean exist = (cursor.getCount() > 0);
        cursor.close();
        return exist;
    }

    public void updateWhileList(WhileList wl) {
        db = this.getWritableDatabase();
        String id = wl.getName();
        String blackListNo = wl.getPackage();
        String query = "UPDATE "+UserDb.TABLE_BLACKLIST+" SET  blacklist_number = '"
                +blackListNo+"' Where id = "+"'"+id+"'";
        db.execSQL(query);
    }

    public void insertWhileList(WhileList wl) {
        db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(UserDb.KEY_NAME, wl.getName());
        values.put(UserDb.KEY_PACKAGE, wl.getPackage());
        db.insert(UserDb.TABLE_WHITELIST, null, values);
    }

    public long insertSummaryUpdate(String dtUpdateWhiteList, String dtUpdateBlackList) {
        db = this.getWritableDatabase();
        values = new ContentValues();
        values.put(UserDb.KEY_WL, dtUpdateWhiteList);
        values.put(UserDb.KEY_BL, dtUpdateBlackList);
        long rowInserted = db.insert(UserDb.TABLE_DEVICE_CONFIG, null, values);
        return rowInserted;
    }

    public String[][] getSummaryUpdate() {
        db = this.getWritableDatabase();
        String myColumns[] = new String[] { UserDb.KEY_BL, UserDb.KEY_WL };
        String myData[][];
        Cursor myCursor;
        myCursor = db.query(UserDb.TABLE_DEVICE_CONFIG, myColumns, null, null,
                null, null, null);
        int temp = myCursor.getCount();
        if (temp == 0) {
            myData = null;
        } else {
            myCursor.moveToFirst();
            myData = new String[myCursor.getCount()][4];
            int number = 0;
            try {
                do {
                    for (int i = 0; i < myCursor.getColumnCount(); i++) {
                        myData[number][i] = myCursor.getString(i);
                    }
                    number++;
                } while (myCursor.moveToNext());
            } catch (Exception er) {
                Log.d(TAG, er.toString());
            }
            myCursor.deactivate();
        }
        return myData;
    }

    public String[][] getWhiteList() {
        db = this.getWritableDatabase();
        String myColumns[] = new String[] { UserDb.KEY_NAME, UserDb.KEY_PACKAGE };
        String myData[][];
        Cursor myCursor;
        myCursor = db.query(UserDb.TABLE_WHITELIST, myColumns, null, null,
                null, null, null);
        int temp = myCursor.getCount();
        if (temp == 0) {
            myData = null;
        } else {
            myCursor.moveToFirst();
            myData = new String[myCursor.getCount()][4];
            int number = 0;
            try {
                do {
                    for (int i = 0; i < myCursor.getColumnCount(); i++) {
                        myData[number][i] = myCursor.getString(i);
                    }
                    number++;
                } while (myCursor.moveToNext());
            } catch (Exception er) {
                Log.d(TAG, er.toString());
            }
            myCursor.deactivate();
        }
        return myData;
    }

    public String[][] getMaterialRefAll() {
        return new String[0][];
    }

    public long getLOVReturnCount() {
        return 0;
    }

    public String[][] getBlackList() {
        db = this.getWritableDatabase();
        String myColumns[] = new String[] { UserDb.KEY_ID, UserDb.KEY_BLACK_LIST };
        String myData[][];
        Cursor myCursor;
        myCursor = db.query(UserDb.TABLE_BLACKLIST, myColumns, null, null,
                null, null, null);
        int temp = myCursor.getCount();
        if (temp == 0) {
            myData = null;
        } else {
            myCursor.moveToFirst();
            myData = new String[myCursor.getCount()][4];
            int number = 0;
            try {
                do {
                    for (int i = 0; i < myCursor.getColumnCount(); i++) {
                        myData[number][i] = myCursor.getString(i);
                    }
                    number++;
                } while (myCursor.moveToNext());
            } catch (Exception er) {
                Log.d(TAG, er.toString());
            }
            myCursor.deactivate();
        }
        return myData;
    }
}
