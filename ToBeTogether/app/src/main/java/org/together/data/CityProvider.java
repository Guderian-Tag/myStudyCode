package org.together.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by v-fei.wang on 2015/12/23.
 */
public class CityProvider extends ContentProvider{

    private static final String TAG = "CityProvider";

    private static final String AUTHORITIES = "org.together.data.cityProvider";
    public static Uri CITY_URI = Uri.parse("content://org.together.data.cityProvider/cities");
    private static UriMatcher uriMatcher;
    private final static int CITY_ID = 1;
    private final static int CITIES = 2;
    GuessDBHelper dbHelper;
    private static Map<String,String> projectionMap;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES,"cities",CITIES);
        uriMatcher.addURI(AUTHORITIES,"cities/#",CITY_ID);
    }

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(GuessDBHelper.CITY_NAME,GuessDBHelper.CITY_NAME);
        projectionMap.put(GuessDBHelper.COUNTRY_CODE,GuessDBHelper.COUNTRY_CODE);
        projectionMap.put(GuessDBHelper.CITY_ID,GuessDBHelper.CITY_ID);
        projectionMap.put(GuessDBHelper.CITY_CODE,GuessDBHelper.CITY_CODE);
        projectionMap.put(GuessDBHelper.PHONETIC_NAME,GuessDBHelper.PHONETIC_NAME);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new GuessDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projections, String selections, String[] selectionArgs, String sort) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GuessDBHelper.CITY_TABLE);
        switch (uriMatcher.match(uri)){
            case CITY_ID:
                String id = uri.getPathSegments().get(1);
                qb.appendWhere(GuessDBHelper.CITY_ID+"="+id);
                break;
            case CITIES:
                qb.setProjectionMap(projectionMap);
                break;
            default:
                break;
        }
        Cursor cursor = qb.query(db,projections,selections,selectionArgs,null,null,sort);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case CITIES:
                return "vnd.android.cursor.dir/cities";
            case CITY_ID:
                return "vnd.android.cursor.item/city";
            default:
                throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(GuessDBHelper.CITY_TABLE,GuessDBHelper.CITY_ID,contentValues);
        if (rowId>0){
            Uri uri1 = ContentUris.withAppendedId(uri,rowId);
            getContext().getContentResolver().notifyChange(uri1,null);
            return uri1;
        }
        throw new SQLException("Failed to insert row into "+uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
