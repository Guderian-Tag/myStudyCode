package org.together.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by v-fei.wang on 2015/12/21.
 */
public class CountryProvider extends ContentProvider{

    private static final String TAG = "CountryProvider";

    private static final String AUTHORITIES = "org.together.data.countryProvider";
    public static final Uri COUNTRY_URI = Uri.parse("content://org.together.data.countryProvider/countries");
    private static final int COUNTRIES = 1;
    private static final int COUNTRIES_ID = 2;//id->item

    private static UriMatcher uriMatcher;
    GuessDBHelper dbHelper;
    private static Map<String,String> projectionMap;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITIES,"countries",COUNTRIES);
        uriMatcher.addURI(AUTHORITIES,"countries/#",COUNTRIES_ID);
    }

    static {
        projectionMap = new HashMap<String, String>();
        projectionMap.put(GuessDBHelper.COUNTRY_ID,GuessDBHelper.COUNTRY_ID);
        projectionMap.put(GuessDBHelper.CITY_NAME,GuessDBHelper.COUNTRY_NAME);
        projectionMap.put(GuessDBHelper.COUNTRY_CODE,GuessDBHelper.COUNTRY_CODE);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new GuessDBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(TAG,String.valueOf(dbHelper==null));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projections, String selections, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GuessDBHelper.COUNTRY_TABLE);
        switch (uriMatcher.match(uri)){
            case COUNTRIES_ID:
                String id = uri.getPathSegments().get(1);
                qb.appendWhere(GuessDBHelper.COUNTRY_ID+"="+id);
                break;
            case COUNTRIES:
                qb.setProjectionMap(projectionMap);
                break;
            default:
                break;
        }
        Cursor c = qb.query(db,projections,selections,selectionArgs,null,null,sortOrder);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case COUNTRIES:
                return "vnd.android.cursor.dir/countries";
            case COUNTRIES_ID:
                return "vnd.android.cursor.item/country";
            default:
                throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(GuessDBHelper.COUNTRY_TABLE,GuessDBHelper.COUNTRY_ID,contentValues);
        if (rowId>0){
            Uri uri1 = ContentUris.withAppendedId(COUNTRY_URI, rowId);
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
