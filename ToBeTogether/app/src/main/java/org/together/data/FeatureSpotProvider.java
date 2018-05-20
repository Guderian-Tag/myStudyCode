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
 * Created by v-fei.wang on 2015/12/25.
 */
public class FeatureSpotProvider extends ContentProvider{

    private final static String AUTHORITY = "org.together.data.featureSpotProvider";
    public final static Uri FEATURE_SPOT_URI = Uri.parse("content://org.together.data.featureSpotProvider/spots");
    private final static int SPOT_ID = 1;
    private final static int SPOTS = 2;
    private GuessDBHelper dbHelper;
    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY,"spots",SPOTS);
        uriMatcher.addURI(AUTHORITY,"spots/#",SPOT_ID);
    }
    private static Map<String,String> projectionMap;
    static {
        projectionMap = new HashMap<String,String>();
        projectionMap.put(GuessDBHelper.SPOT_ID,GuessDBHelper.SPOT_ID);
        projectionMap.put(GuessDBHelper.SPOT_NAME,GuessDBHelper.SPOT_NAME);
        projectionMap.put(GuessDBHelper.IMAGE_NAME,GuessDBHelper.IMAGE_NAME);
        projectionMap.put(GuessDBHelper.TIP,GuessDBHelper.TIP);
        projectionMap.put(GuessDBHelper.CITY_CODE,GuessDBHelper.CITY_CODE);
        projectionMap.put(GuessDBHelper.IMAGE_NAME,GuessDBHelper.IMAGE_NAME);
        projectionMap.put(GuessDBHelper.DESCRIPTION,GuessDBHelper.DESCRIPTION);
        projectionMap.put(GuessDBHelper.LEVEL,GuessDBHelper.LEVEL);
    }
    @Override
    public boolean onCreate() {
        dbHelper = new GuessDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(GuessDBHelper.SPOT_TABLE);
        switch (uriMatcher.match(uri)){
            case SPOT_ID:
                String id = uri.getPathSegments().get(1);
                qb.appendWhere(GuessDBHelper.SPOT_ID+"="+id);
                break;
            case SPOTS:
                qb.setProjectionMap(projectionMap);
                break;
            default:
                break;
        }
        Cursor cursor = qb.query(db,strings,s,strings1,null,null,s1);
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case SPOTS:
                return "vnd.android.cursor.dir/spots";
            case SPOT_ID:
                return "vnd.android.cursor.item/spot";
            default:
                throw new IllegalArgumentException("Unsupported URI:"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(GuessDBHelper.SPOT_TABLE,GuessDBHelper.SPOT_ID,contentValues);
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
