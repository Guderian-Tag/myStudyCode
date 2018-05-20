package org.together.data;


import android.content.Context;
import android.util.Log;

import org.together.entity.FeatureSpot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import together.org.tobetogether.R;

/**
 * Created by v-fei.wang on 2015/12/4.
 */
public class PrepareData {

    public static final String SPOTS_DB = "feature_spot";
    public static final String CURRENT_LEVEL = "current_level";
    public static final String CURRENT_SCORE = "current_score";
    public static final String LAST_SPOT_ID = "last_spot_id";
    public static final String LAST_CITY_CODE = "last_city_code";
    public static final String IMAGE_DIR_ROOT = "image/";

   // private static int[] IMAGES = {R.mipmap.zhongshanling,R.mipmap.fuzimiao,R.mipmap.changjdaqiao,R.mipmap.qixiashan,R.mipmap.chaotiangong,R.mipmap.xuanwuhu};
    private static int[] SITENAME = {R.string.zhongshanling,R.string.fuzimiao,R.string.changjdaqiao,R.string.qixiashan,R.string.chaotiangong,R.string.xuanwuhu};


    Context context;

    String[] mixArray;

    public PrepareData(Context context){
        this.context = context;
        init();
    }

    private void init(){
        mixArray = context.getResources().getStringArray(R.array.mix_array);
    }

    /*public List<FeatureSpot> prepare(){
        List<FeatureSpot> featureSpots = new ArrayList<FeatureSpot>();
        for (int i=0;i<IMAGES.length;i++){
            FeatureSpot featureSpot = new FeatureSpot();
            featureSpot.setLevel(i);
            featureSpot.setNameId(SITENAME[i]);
            featureSpot.setName(context.getResources().getString(SITENAME[i]));
            featureSpot.setImageId(IMAGES[i]);
            featureSpots.add(featureSpot);
        }
        Collections.sort(featureSpots, new Comparator<FeatureSpot>() {
            @Override
            public int compare(FeatureSpot f1, FeatureSpot f2) {
                return f1.getLevel()-f2.getLevel();
            }
        });
        return featureSpots;
    }*/

    public List<String> mixtureArray(FeatureSpot featureSpot){
        int lastRandom = -1;
        String name = featureSpot.getName();
        List<String> mixList = new ArrayList<String>();
        Collections.addAll(mixList,mixArray);
        for (int i=0;i<name.length();i++){
            lastRandom = (int) (Math.random()*mixArray.length);
            String nameChar = name.substring(i,i+1);
            mixList.add(lastRandom,nameChar);
        }
        return mixList;
    }

    public List<String> createMixtureArray(List<FeatureSpot> featureSpots,FeatureSpot currentSpot){
        StringBuffer stringBuffer = new StringBuffer(currentSpot.getName());
        int size = featureSpots.size();
        for (int i=0;i<8;i++){
            int index = (int) (Math.random()*size);
            FeatureSpot spot = featureSpots.get(index);
            stringBuffer.append(spot.getName());
        }
        Log.d("PrepareData", "sb" + stringBuffer.toString());
        int len = stringBuffer.length();
        String[] names = new String[len];
        List<String> strs = new ArrayList<String>();
        for (int i=0;i<len;i++){
            int rand = (int) (Math.random()*len);
            String temp = stringBuffer.substring(i,i+1);
            if (temp==null) continue;
            names[rand] = temp;
        }
        for (int i=0;i<len;i++){
            if (names[i]!=null){
                strs.add(names[i]);
            }
        }
        String currentName = currentSpot.getName();
        for (int i=0;i<currentName.length();i++){
            int r = (int) (Math.random()*strs.size());
            String nameChar = currentName.substring(i,i+1);
            strs.add(r,nameChar);
        }
        Log.d("PrepareData","str:"+ strs.size());
        return strs;
    }

}
