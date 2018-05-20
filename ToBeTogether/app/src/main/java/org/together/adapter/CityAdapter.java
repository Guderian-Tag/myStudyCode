package org.together.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.together.entity.City;

import java.util.List;

import together.org.tobetogether.R;

/**
 * Created by v-fei.wang on 2015/12/24.
 */
public class CityAdapter extends BaseAdapter {

    List<City> cities;
    Context context;

    public CityAdapter(List<City> cities,Context context){
        this.cities = cities;
        this.context = context;
    }

    @Override
    public int getCount() {
        return cities==null?0:cities.size();
    }

    @Override
    public Object getItem(int i) {
        return cities==null?null:cities.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder viewHolder = null;
        if (convertView==null){
            convertView = inflater.inflate(R.layout.city_item,null);
            viewHolder = new ViewHolder();
            viewHolder.cityName = (TextView) convertView.findViewById(R.id.city_name);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.cityName.setText(cities.get(i).getCityName());
        return convertView;
    }

    private static final class ViewHolder{
        TextView cityName;
    }

}
