package org.together.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import org.together.data.CityProvider;
import org.together.data.FeatureSpotProvider;
import org.together.data.GuessDBHelper;
import org.together.entity.City;
import org.together.entity.FeatureSpot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by v-fei.wang on 2015/12/24.
 */
public class GuessDao {

    private final static String TAG = "GuessDao";

    Context context;

    public GuessDao(Context context){
        this.context = context;
    }

    public City getFirstCity(){
        Cursor cursor = context.getContentResolver().query(CityProvider.CITY_URI,null, GuessDBHelper.CITY_ID+"=?",new String[]{"1"},null);
        Log.d(TAG,cursor.getCount()+"");
        if (cursor!=null){
            City city = new City();
            while (cursor.moveToNext()){
                int cityId = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_ID));
                String cityName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_NAME));
                String cityCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                String countryCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.COUNTRY_CODE));
                String phoneticName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.PHONETIC_NAME));
                city.setPhoneticName(phoneticName);
                city.setCityId(cityId);
                city.setCityCode(cityCode);
                city.setCityName(cityName);
                city.setCountryCode(countryCode);
            }
            cursor.close();
            return city;
        }
        return null;
    }

    public List<City> getAllCities(){
        Cursor cursor = context.getContentResolver().query(CityProvider.CITY_URI,null, null,null,null);
        if (cursor!=null){
            List<City> cities = new ArrayList<City>();
            while (cursor.moveToNext()){
                City city = new City();
                int cityId = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_ID));
                String cityName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_NAME));
                String cityCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                String countryCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.COUNTRY_CODE));
                String phoneticName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.PHONETIC_NAME));
                city.setPhoneticName(phoneticName);
                city.setCityId(cityId);
                city.setCityCode(cityCode);
                city.setCityName(cityName);
                city.setCountryCode(countryCode);
                cities.add(city);
            }
            cursor.close();
            return cities;
        }
        return null;
    }

    public City getCityByCode(String cityCode){
        Cursor cursor = context.getContentResolver().query(CityProvider.CITY_URI,null, GuessDBHelper.CITY_CODE+" = ?",new String[]{cityCode},null);
        if (cursor!=null){
            City city = new City();
            while (cursor.moveToNext()){
                int cityId = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_ID));
                String cityName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_NAME));
                String countryCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.COUNTRY_CODE));
                String phoneticName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.PHONETIC_NAME));
                city.setPhoneticName(phoneticName);
                city.setCityId(cityId);
                city.setCityCode(cityCode);
                city.setCityName(cityName);
                city.setCountryCode(countryCode);
            }
            cursor.close();
            return city;
        }
        return null;
    }

    public List<City> getCityByName(String name){
        Cursor cursor = context.getContentResolver().query(CityProvider.CITY_URI,null, GuessDBHelper.CITY_NAME+" like ?",new String[]{"%"+name+"%"},null);
        if (cursor!=null){
            List<City> cities = new ArrayList<City>();
            while (cursor.moveToNext()){
                City city = new City();
                int cityId = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_ID));
                String cityName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_NAME));
                String cityCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                String countryCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.COUNTRY_CODE));
                String phoneticName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.PHONETIC_NAME));
                city.setPhoneticName(phoneticName);
                city.setCityId(cityId);
                city.setCityCode(cityCode);
                city.setCityName(cityName);
                city.setCountryCode(countryCode);
                cities.add(city);
            }
            cursor.close();
            return cities;
        }
        return null;
    }

    public List<FeatureSpot> getSpotsByCityCode(String cityCode){
        Cursor cursor = context.getContentResolver().query(FeatureSpotProvider.FEATURE_SPOT_URI,null,GuessDBHelper.CITY_CODE+"=?",new String[]{cityCode},null);
        if (cursor!=null){
            List<FeatureSpot> list = new ArrayList<FeatureSpot>();
            while (cursor.moveToNext()){
                FeatureSpot spot = new FeatureSpot();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_ID));
                String spotName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_NAME));
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.IMAGE_NAME));
                String tip = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.TIP));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.DESCRIPTION));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.LEVEL));
                spot.setLevel(level);
                spot.setCityCode(cityCode);
                spot.setId(id);
                spot.setName(spotName);
                spot.setImageName(imageName);
                spot.setTip(tip);
                spot.setDescription(description);
                list.add(spot);
            }
            cursor.close();
            Collections.sort(list, new Comparator<FeatureSpot>() {
                @Override
                public int compare(FeatureSpot featureSpot, FeatureSpot t1) {
                    return featureSpot.getLevel() - t1.getLevel();
                }
            });
            return list;
        }
        return null;
    }

    public int getSpotCount(String cityCode){
        Cursor cursor = context.getContentResolver().query(FeatureSpotProvider.FEATURE_SPOT_URI,null,GuessDBHelper.CITY_CODE+"=?",new String[]{cityCode},null);
        if (cursor!=null){
            return cursor.getCount();
        }
        return 0;
    }

    public FeatureSpot getSpotsById(int spotId){
        Cursor cursor = context.getContentResolver().query(FeatureSpotProvider.FEATURE_SPOT_URI,null,GuessDBHelper.SPOT_ID+"=?",new String[]{String.valueOf(spotId)},null);
        if (cursor!=null){
            FeatureSpot spot = new FeatureSpot();
            while (cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_ID));
                String spotName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_NAME));
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.IMAGE_NAME));
                String tip = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.TIP));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.DESCRIPTION));
                String cityCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.LEVEL));
                spot.setLevel(level);
                spot.setCityCode(cityCode);
                spot.setId(id);
                spot.setName(spotName);
                spot.setImageName(imageName);
                spot.setTip(tip);
                spot.setDescription(description);
            }
            cursor.close();
            return spot;
        }
        return null;
    }

    public List<FeatureSpot> getALlSpots(){
        Cursor cursor = context.getContentResolver().query(FeatureSpotProvider.FEATURE_SPOT_URI,null,null,null,null);
        if (cursor!=null){
            List<FeatureSpot> list = new ArrayList<FeatureSpot>();
            while (cursor.moveToNext()){
                FeatureSpot spot = new FeatureSpot();
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_ID));
                String spotName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.SPOT_NAME));
                String imageName = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.IMAGE_NAME));
                String tip = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.TIP));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.DESCRIPTION));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow(GuessDBHelper.LEVEL));
                String cityCode = cursor.getString(cursor.getColumnIndexOrThrow(GuessDBHelper.CITY_CODE));
                spot.setLevel(level);
                spot.setCityCode(cityCode);
                spot.setId(id);
                spot.setName(spotName);
                spot.setImageName(imageName);
                spot.setTip(tip);
                spot.setDescription(description);
                list.add(spot);
            }
            cursor.close();
            Collections.sort(list, new Comparator<FeatureSpot>() {
                @Override
                public int compare(FeatureSpot featureSpot, FeatureSpot t1) {
                    return featureSpot.getLevel() - t1.getLevel();
                }
            });
            return list;
        }
        return null;
    }

}
