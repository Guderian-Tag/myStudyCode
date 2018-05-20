package org.together.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by v-fei.wang on 2015/12/21.
 */
public class GuessDBHelper extends SQLiteOpenHelper{

    private static final String TAG = "GuessDBHelper";

    private final static String DB_NAME = "guess.db";
    private final static int VERSION = 1;


    public static final String COUNTRY_ID = "country_id";
    public static final String COUNTRY_NAME = "country_name";
    public static final String COUNTRY_CODE = "country_code";
    public static final String COUNTRY_TABLE = "country";
    private final static String CREATE_COUNTRY_SQL = "create table "+COUNTRY_TABLE+"(" +
            COUNTRY_ID +" integer primary key autoincrement," +
            COUNTRY_NAME + " varchar(20)," +
            COUNTRY_CODE + " varchar(10))";

    public static final String CITY_ID = "city_id";
    public static final String CITY_NAME = "city_name";
    public static final String CITY_CODE = "city_code";
    public static final String CITY_TABLE = "city";
    public static final String PHONETIC_NAME = "phonetic_name";
    private static final String CREATE_CITY_SQL = "create table "+CITY_TABLE+"("+
            CITY_ID + " integer primary key autoincrement," +
            CITY_NAME + " varchar(20)," +
            COUNTRY_CODE + " varchar(10)," +
            CITY_CODE +" varchar(10)," +
            PHONETIC_NAME +" varchar(20))";


    public static final String SPOT_ID = "spot_id";
    public static final String SPOT_NAME = "spot_name";
    public static final String DESCRIPTION = "description";
    public static final String LEVEL = "level";
    public static final String TIP = "tip";
    public static final String IMAGE_NAME = "image_name";
    public static final String SPOT_TABLE = "feature_spot";
    private final static String CREATE_SPOT_SQL = "create table "+SPOT_TABLE+"("
            + SPOT_ID+" integer primary key autoincrement,"
            + SPOT_NAME+" varchar(20),"
            + DESCRIPTION+" text,"
            + LEVEL+" integer,"
            + TIP+" text,"
            + CITY_CODE+" varchar(10),"
            + IMAGE_NAME+" text)";


    public GuessDBHelper(Context context){
        super(context, DB_NAME, null, VERSION);
        Log.d(TAG, "GuessDBHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG,"onCreate");
        Log.d(TAG, "CREATE_COUNTRY_SQL:" + CREATE_COUNTRY_SQL);
        Log.d(TAG, "CREATE_CITY_SQL:" + CREATE_CITY_SQL);

        sqLiteDatabase.execSQL(CREATE_COUNTRY_SQL);
        sqLiteDatabase.execSQL(CREATE_CITY_SQL);
        sqLiteDatabase.execSQL(CREATE_SPOT_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(TAG,"sqLiteDatabase,oldVersion:"+oldVersion+",newVersion:"+newVersion);
    }
}
