package com.plamera.tmswiftlauncher.Provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.plamera.tmswiftlauncher.DatabaseHandler;
import com.plamera.tmswiftlauncher.Encap.UserDb;

public class FlagProvider extends ContentProvider {
    private DatabaseHandler dbHandle;
    private static final int ALL_FLAGS = 1;
    private static final int SINGLE_FLAG = 2;
    SQLiteDatabase db;
    private static final String AUTHORITY = "com.plamera.tmswiftlauncher.Provider.FlagProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/flags");
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "flags", ALL_FLAGS);
        uriMatcher.addURI(AUTHORITY, "flags/#", SINGLE_FLAG);
    }

    @Override
    public boolean onCreate() {
        dbHandle = new DatabaseHandler(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        db = dbHandle.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(UserDb.TABLE_DEPLOY_FLAG);

        switch (uriMatcher.match(uri)) {
            case ALL_FLAGS:
                //do nothing
                break;
            case SINGLE_FLAG:
                String id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(UserDb.KEY_ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_FLAGS:
                return "vnd.android.cursor.dir/"+AUTHORITY+".FLAGS";
            case SINGLE_FLAG:
                return "vnd.android.cursor.item/"+AUTHORITY+".FLAGS";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = dbHandle.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_FLAGS:
                //do nothing
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long id = db.insert(UserDb.TABLE_DEPLOY_FLAG, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = dbHandle.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_FLAGS:
                // do nothing
                break;
            case SINGLE_FLAG:
                String id = uri.getPathSegments().get(1);
                selection = UserDb.KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int deleteCount = db.delete(UserDb.TABLE_DEPLOY_FLAG, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        db = dbHandle.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_FLAGS:
                //do nothing
                break;
            case SINGLE_FLAG:
                String id = uri.getPathSegments().get(1);
                selection = UserDb.KEY_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int updateCount = db.update(UserDb.TABLE_DEPLOY_FLAG, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}