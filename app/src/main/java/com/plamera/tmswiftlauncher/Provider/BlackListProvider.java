package com.plamera.tmswiftlauncher.Provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.plamera.tmswiftlauncher.DatabaseHandler;
import com.plamera.tmswiftlauncher.Encap.UserDb;

public class BlackListProvider extends ContentProvider {
    private DatabaseHandler dbHandle;
    private static final int ALL_FLAGS = 1;
    private static final int SINGLE_FLAG = 2;
    SQLiteDatabase db;
    private static final String AUTHORITY = "com.plamera.tmswiftlauncher.Provider.BlackListProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/blacklistno");
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "blacklistno", ALL_FLAGS);
        uriMatcher.addURI(AUTHORITY, "blacklistno/#", SINGLE_FLAG);
    }

    @Override
    public boolean onCreate() {
        dbHandle = new DatabaseHandler(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        db = dbHandle.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(UserDb.TABLE_BLACKLIST);

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

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
