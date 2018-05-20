package together.org.tobetogether;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.together.adapter.CityAdapter;
import org.together.dao.GuessDao;
import org.together.data.CityProvider;
import org.together.data.CountryProvider;
import org.together.data.FeatureSpotProvider;
import org.together.data.GuessDBHelper;
import org.together.data.PrepareData;
import org.together.entity.City;
import org.together.utils.XMLConstant;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class StartActivity extends Activity implements Animation.AnimationListener{

    private final static String TAG = "StartActivity";

    RelativeLayout start_layout;
    Button select;
    Button switchCity;
    EditText searchEditText;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    GuessDao guessDao;
    City currentCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        parseXML();
        init();
        testDb();
    }

    private void parseXML(){
        initPreferences();
        parseCountryXML();
        parseCityXML();
        parseFeatureSpotXML();
        initData();
    }

    private void initPreferences(){
        preferences = getSharedPreferences(XMLConstant.GUESSPREFERENCE, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    private void testDb(){
        Cursor c = getContentResolver().query(FeatureSpotProvider.FEATURE_SPOT_URI, null, null, null, null);
        if (c!=null){
            Log.d(TAG, "count:" + c.getCount());
            while (c.moveToNext()){
                String name = c.getString(c.getColumnIndexOrThrow(GuessDBHelper.IMAGE_NAME));
                String code = c.getString(c.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                long id = c.getLong(c.getColumnIndexOrThrow(GuessDBHelper.SPOT_ID));
                Log.d(TAG, "name:" + name + ",code:" + code + ",id:" + id);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initData(){
        guessDao = new GuessDao(this);
        String lastCityCode = preferences.getString(PrepareData.LAST_CITY_CODE,null);
        Log.d(TAG, "lastCityCode:" + lastCityCode);
        if (lastCityCode!=null){
            City city = guessDao.getCityByCode(lastCityCode);
            if (city!=null){
                currentCity = city;
            }else {
                currentCity = guessDao.getFirstCity();
            }
        }else {
            currentCity = guessDao.getFirstCity();
        }
    }

    private void init(){
        start_layout = (RelativeLayout) findViewById(R.id.start_layout);
        Animation startAnimation = getAlphaAnimation();
        startAnimation.setDuration(3000);
        start_layout.setAnimation(startAnimation);
        startAnimation.setAnimationListener(this);
        startAnimation.start();
        select = (Button) findViewById(R.id.select);
        select.setText(currentCity.getCityName());
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = guessDao.getSpotCount(currentCity.getCityCode());
                Log.d(TAG, "count:" + count + ",cityCode:" + currentCity.getCityCode());
                if (count < 1) {
                    Toast.makeText(StartActivity.this, R.string.no_spot_tip, Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(StartActivity.this, LevelActivity.class);
                intent.putExtra(XMLConstant.CITY_CODE, currentCity.getCityCode());
                intent.putExtra(XMLConstant.PHONETIC_NAME,currentCity.getPhoneticName());
                startActivity(intent);
                editor.putString(PrepareData.LAST_CITY_CODE, currentCity.getCityCode());
                editor.commit();
                //finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
        switchCity = (Button) findViewById(R.id.switch_city);
        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopWindow();
            }
        });
    }

    private void showPopWindow(){
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.switch_city, null);
        final List<City> cities = guessDao.getAllCities();
        final ListView listView = (ListView) view.findViewById(R.id.city_list);
        final CityAdapter cityAdapter = new CityAdapter(cities,this);
        listView.setAdapter(cityAdapter);
        searchEditText = (EditText) view.findViewById(R.id.search_city);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<City> list = guessDao.getCityByName(charSequence.toString());
                CityAdapter adapter = new CityAdapter(list,StartActivity.this);
                listView.setAdapter(adapter);
                list = null;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString()==null||editable.toString().length()<1){
                    listView.setAdapter(cityAdapter);
                }
            }
        });
        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()
        final PopupWindow window = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                City city = cities.get(i);
                currentCity = city;
                select.setText(city.getCityName());
                window.dismiss();
            }
        });
        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        window.setFocusable(true);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);
        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.pop_window_anim_style);
        // 在底部显示
        window.showAtLocation(this.findViewById(R.id.select),
                Gravity.BOTTOM, 0, 0);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        start_layout.setBackground(getResources().getDrawable(R.mipmap.level_bg));
        select.setVisibility(View.VISIBLE);
        switchCity.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private Animation getFadeInAnimation(){
        return AnimationUtils.loadAnimation(this,android.R.anim.fade_in);
    }

    private AlphaAnimation getAlphaAnimation(){
        return new AlphaAnimation(0f,1f);
    }


    private void parseCountryXML(){
        XmlResourceParser xmlResourceParser = getResources().getXml(R.xml.country);
        try {
            ContentValues values = null;
            int eventType = xmlResourceParser.getEventType();
            while (xmlResourceParser.getEventType()!= XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        String name = xmlResourceParser.getName();
                        if (name.equals(XMLConstant.DATAVERSION)){
                            xmlResourceParser.next();
                            String dataVersion = xmlResourceParser.getText();
                            int version = Integer.valueOf(dataVersion);
                            int lastVersion = preferences.getInt(XMLConstant.DATAVERSION,-1);
                            Log.d(TAG,"version:"+version+",lastVersion:"+lastVersion);
                            if (lastVersion!=-1&&lastVersion==version){
                                return;
                            }else {
                                editor.putInt(XMLConstant.DATAVERSION,version);
                            }
                        }
                        if (name.equals(XMLConstant.COUNTRY)){
                            values = new ContentValues();
                        }else if (name.equals(XMLConstant.COUNTRYNAME)){
                            xmlResourceParser.next();
                            String cName = xmlResourceParser.getText();
                            values.put(GuessDBHelper.COUNTRY_NAME, cName);
                        }else if (name.equals(XMLConstant.COUNTRYCODE)){
                            xmlResourceParser.next();
                            String code = xmlResourceParser.getText();
                            values.put(GuessDBHelper.COUNTRY_CODE, code);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xmlResourceParser.getName().equals(XMLConstant.COUNTRY)){
                            getContentResolver().insert(CountryProvider.COUNTRY_URI, values);
                            values = null;
                        }
                        break;
                }
                eventType = xmlResourceParser.next();
            }
            editor.commit();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            Log.d(TAG, "XmlPullParserException:" + e.getLocalizedMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void parseCityXML(){
        XmlResourceParser xmlResourceParser = getResources().getXml(R.xml.city);
        try {
            ContentValues values = null;
            int eventType = xmlResourceParser.getEventType();
            while (eventType!=xmlResourceParser.END_DOCUMENT){
                switch (eventType){
                    case XmlResourceParser.START_DOCUMENT:
                        break;
                    case XmlResourceParser.START_TAG:
                        String tag = xmlResourceParser.getName();
                        if (tag.equals(XMLConstant.DATAVERSION)){
                            xmlResourceParser.next();
                            int version = Integer.valueOf(xmlResourceParser.getText());
                            int lastVersion = preferences.getInt(XMLConstant.CITY_DATAVERSION,-1);
                            Log.d(TAG,"version:"+version+",lastVersion:"+lastVersion);
                            if (lastVersion!=-1&&version==lastVersion){
                                return;
                            }else {
                                editor.putInt(XMLConstant.CITY_DATAVERSION,version);
                            }
                        }
                        if (tag.equals(XMLConstant.CITY)){
                            values = new ContentValues();
                        }else if (tag.equals(XMLConstant.CITY_NAME)){
                            xmlResourceParser.next();
                            String cityName = xmlResourceParser.getText();
                            values.put(GuessDBHelper.CITY_NAME, cityName);
                        }else if (tag.equals(XMLConstant.CITY_CODE)){
                            xmlResourceParser.next();
                            String cityCode = xmlResourceParser.getText();
                            values.put(GuessDBHelper.CITY_CODE, cityCode);
                        }else if (tag.equals(XMLConstant.COUNTRYCODE)){
                            xmlResourceParser.next();
                            String countryCode = xmlResourceParser.getText();
                            values.put(GuessDBHelper.COUNTRY_CODE, countryCode);
                        }else if(tag.equals(XMLConstant.PHONETIC_NAME)){
                            xmlResourceParser.next();
                            String phoneticName = xmlResourceParser.getText();
                            values.put(GuessDBHelper.PHONETIC_NAME,phoneticName);
                        }
                        break;
                    case XmlResourceParser.END_TAG:
                        if (xmlResourceParser.getName().equals(XMLConstant.CITY)){
                            getContentResolver().insert(CityProvider.CITY_URI, values);
                            values = null;
                        }
                        break;
                }
                eventType = xmlResourceParser.next();
            }
            editor.commit();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseFeatureSpotXML(){
        XmlResourceParser xmlResourceParser = getResources().getXml(R.xml.feature_spot_info);
        ContentValues values = null;
        try {
            int eventType = xmlResourceParser.getEventType();
            while (eventType!=XmlResourceParser.END_DOCUMENT){
                switch (eventType){
                    case XmlResourceParser.START_DOCUMENT:
                        break;
                    case XmlResourceParser.START_TAG:
                        String tag = xmlResourceParser.getName();
                        if (tag.equals(XMLConstant.DATAVERSION)){
                            xmlResourceParser.next();
                            int lastVersion = preferences.getInt(XMLConstant.SPOT_DATA_VERSION,-1);
                            int version = Integer.valueOf(xmlResourceParser.getText());
                            Log.d(TAG,"version:"+version+",lastVersion:"+lastVersion);
                            if (version!=-1&&version==lastVersion){
                                return;
                            }else {
                                editor.putInt(XMLConstant.SPOT_DATA_VERSION,version);
                            }
                        }
                        if (tag.equals(XMLConstant.FEATURE_SPOT)){
                            values = new ContentValues();
                        }else if (tag.equals(XMLConstant.SPOT_NAME)){
                            xmlResourceParser.next();
                            String name = xmlResourceParser.getText();
                            values.put(GuessDBHelper.SPOT_NAME,name);
                        }else if (tag.equals(XMLConstant.IMAGE_NAME)){
                            xmlResourceParser.next();
                            String imageName = xmlResourceParser.getText();
                            values.put(GuessDBHelper.IMAGE_NAME,imageName);
                        }else if (tag.equals(XMLConstant.CITY_CODE)){
                            xmlResourceParser.next();
                            String cityCode = xmlResourceParser.getText();
                            values.put(GuessDBHelper.CITY_CODE,cityCode);
                        }else if (tag.equals(XMLConstant.TIP)){
                            xmlResourceParser.next();
                            String tip = xmlResourceParser.getText();
                            values.put(GuessDBHelper.TIP,tip);
                        }else if (tag.equals(GuessDBHelper.DESCRIPTION)){
                            xmlResourceParser.next();
                            String description = xmlResourceParser.getText();
                            values.put(GuessDBHelper.DESCRIPTION,description);
                        }else if (tag.equals(GuessDBHelper.LEVEL)){
                            xmlResourceParser.next();
                            int level = Integer.valueOf(xmlResourceParser.getText());
                            values.put(GuessDBHelper.LEVEL,level);
                        }
                        break;
                    case XmlResourceParser.END_TAG:
                        if (xmlResourceParser.getName().equals(XMLConstant.FEATURE_SPOT)){
                            getContentResolver().insert(FeatureSpotProvider.FEATURE_SPOT_URI,values);
                            values = null;
                        }
                        break;
                }
                eventType = xmlResourceParser.next();
            }
            editor.commit();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
