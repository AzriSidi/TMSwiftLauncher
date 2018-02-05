package com.plamera.tmswiftlauncher.Encap;

public class UserDb {
    public static final String TABLE_LOGIN = "staff_login";
    public static final String TABLE_BLACKLIST = "black_list";
    public static final String TABLE_WHITELIST = "white_list";
    public static final String TABLE_DEPLOY_FLAG = "deploy_flag";
    public static final String TABLE_DEVICE_CONFIG = "device_config_lastupdate";
    public static final String LOG_TAG = "UserManageDb";
    public static final String KEY_ID = "id";
    public static final String KEY_USER_NAME = "staff_no";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_LDAP = "ldap_status";
    public static final String KEY_BLACK_LIST = "blacklist_number";
    public static final String KEY_FLAG = "flag";
    public static final String KEY_WL = "WL";
    public static final String KEY_BL = "BL";
    public static final String KEY_NAME = "name";
    public static final String KEY_PACKAGE = "package";

    public static final String CREATE_TABLE_LOGIN = "CREATE TABLE " + TABLE_LOGIN + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT,"
            + KEY_TOKEN + " TEXT, "+ KEY_LDAP + " TEXT"+")";

    public static final String CREATE_TABLE_BLACKLIST = "CREATE TABLE "
            + TABLE_BLACKLIST + "(" + KEY_ID + " TEXT," + KEY_BLACK_LIST
            + " TEXT" + ")";

    public static final String CREATE_TABLE_WHITELIST = "CREATE TABLE "
            + TABLE_WHITELIST + "(" + KEY_NAME + " TEXT, " +
            KEY_PACKAGE +" TEXT "+")";

    public static final String CREATE_TABLE_DEPLOY_FLAG = "CREATE TABLE "+TABLE_DEPLOY_FLAG+"(" +
            KEY_ID + " INTEGER PRIMARY KEY," + KEY_FLAG + " TEXT" + ")";

    public static final String CREATE_TABLE_DEVICE_CONFIG = "CREATE TABLE "
            + TABLE_DEVICE_CONFIG + "(" +  KEY_WL + " TEXT not null unique," + KEY_BL + " TEXT not null unique" +" )";
}
