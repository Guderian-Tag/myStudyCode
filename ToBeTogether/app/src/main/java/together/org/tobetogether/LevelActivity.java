package together.org.tobetogether;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.together.dao.GuessDao;
import org.together.data.PrepareData;
import org.together.entity.ChoiceAndAnswer;
import org.together.entity.FeatureSpot;
import org.together.texttap.TypeTextView;
import org.together.utils.AnimationsUtil;
import org.together.utils.XMLConstant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class LevelActivity extends Activity implements View.OnClickListener{

    private final static String TAG = "LevelActivity";

    ImageView siteImage;
    TextView score;
    Button tipBtn;
    LinearLayout answerLayout;
    GridLayout choicesLayout;

    List<String> mixArray;
    PrepareData prepareData;
    FeatureSpot currentSpot;
    SharedPreferences preferences;
    List<FeatureSpot> featureSpots;
    int answerLabelIndex = 0;
    int choiceNumber = 0;
    Map<Integer,ChoiceAndAnswer> map = new HashMap<Integer,ChoiceAndAnswer>();

    private static final int CHOICE_ID_START = 3000;
    private static final int ANSWER_ID_START = 1000;

    private static final int MSG_SUCCESS = 1;
    private static final int MSG_ERROR = 2;

    //View alertBuildView = null;
    Dialog successOrErrorDialog = null;
    AlertDialog.Builder builder = null;
    //new code
    int lastSpotId = -1;
    GuessDao guessDao;
    List<FeatureSpot> featureSpots2 = null;
    AssetManager assetManager;
    int currentLevel2= 0;
    String phoneticName = null;
    List<Integer> removeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_level);
        preferences = getSharedPreferences(PrepareData.SPOTS_DB,Context.MODE_PRIVATE);
        guessDao = new GuessDao(this);
        assetManager = getResources().getAssets();
        init();
    }

    private FeatureSpot getCurrentSpot2(){
        phoneticName = getIntent().getStringExtra(XMLConstant.PHONETIC_NAME);
        String cityCode = getIntent().getStringExtra(XMLConstant.CITY_CODE);
        String lastCityCode = preferences.getString(PrepareData.LAST_CITY_CODE,"");
        featureSpots2 = guessDao.getSpotsByCityCode(cityCode);
        if (cityCode==lastCityCode) {
            lastSpotId = preferences.getInt(PrepareData.LAST_SPOT_ID, -1);
            if (lastSpotId == -1) {
                if (featureSpots2 != null && featureSpots2.size() > 0) {
                    Log.d(TAG, featureSpots2.size() + "");
                    currentLevel2 = 0;
                    return featureSpots2.get(0);
                }
            } else {
                Log.d(TAG, "lastSpotId:!=-1");
                currentSpot = guessDao.getSpotsById(lastSpotId);
                currentLevel2 = currentSpot.getLevel() - 1;
                Log.d(TAG, "currentLevel2:" + currentLevel2);
                return currentSpot;
            }
        }else {
            currentLevel2 = 0;
            return featureSpots2.get(0);
        }
        return null;
    }

    private void init(){
        featureSpots = guessDao.getALlSpots();
        currentSpot = getCurrentSpot2();
        Log.d(TAG,"currentSpot name:"+currentSpot.getName()+",tip:"+currentSpot.getTip());
        prepareData = new PrepareData(this);
        siteImage = (ImageView) findViewById(R.id.site_img);
        score = (TextView) findViewById(R.id.score_label);
        tipBtn = (Button) findViewById(R.id.tip_btn);
        tipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(LevelActivity.this);
                View alertBuildView = inflater.inflate(R.layout.success_dialog, null, false);
                ImageView stateImage = (ImageView) alertBuildView.findViewById(R.id.state_icon);
                Button nextBtn = (Button) alertBuildView.findViewById(R.id.next_button);
                TextView tip = (TextView) alertBuildView.findViewById(R.id.description);
                stateImage.setImageDrawable(getResources().getDrawable(R.mipmap.tip_light));
                tip.setText(currentSpot.getTip());
                Log.d(TAG,"tip:"+currentSpot.getTip());
                AlertDialog.Builder success =initDialog();
                success.setView(alertBuildView);
                nextBtn.setText(getResources().getText(R.string.confirm));
                final Dialog dialog2 = success.show();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog2.dismiss();
                    }
                });
            }
        });
        createMixArray(featureSpots,currentSpot);
        createChoiceLayout();
    }

  /*  private FeatureSpot getCurrentSpot(){
        currentSpot = featureSpots.get(currentLevel);
        Log.d(TAG, "currentSpot.getName():" + currentSpot.getName());
        return currentSpot;
    }*/

    private List<String> createMixArray(List<FeatureSpot> spots,FeatureSpot featureSpot){
        //mixArray = prepareData.mixtureArray(featureSpot);
        mixArray = prepareData.createMixtureArray(spots,featureSpot);
        return mixArray;
    }

    private Bitmap getBitmap(String imageName){
        try {
            Log.d(TAG,"imageName:"+imageName);
            InputStream is = assetManager.open(PrepareData.IMAGE_DIR_ROOT+phoneticName+"/"+imageName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Log.d(TAG,"height:"+bitmap.getHeight());
            is.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createChoiceLayout(){
        Bitmap bitmap = getBitmap(currentSpot.getImageName());
        siteImage.setImageBitmap(bitmap);
        String spotName = currentSpot.getName();
        answerLayout = (LinearLayout) findViewById(R.id.answer_layout);
        for (int i=0;i<spotName.length();i++){
            TextView textView = new TextView(this);
            textView.setWidth(120);
            textView.setHeight(120);
            textView.setId(i + ANSWER_ID_START);
            textView.setTextSize(23f);
            TextPaint tp = textView.getPaint();
            tp.setFakeBoldText(true);
            textView.setGravity(Gravity.CENTER);
            textView.setBackground(getResources().getDrawable(R.drawable.text_rect));
            textView.setOnClickListener(this);
            answerLayout.addView(textView);
        }

        choicesLayout = (GridLayout) findViewById(R.id.choices_layout);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        for (int i=0;i<mixArray.size();i++){
            TextView textView = new TextView(this);
            textView.setText(mixArray.get(i));
            textView.setWidth(width/9);
            textView.setHeight(width/9);
            TextPaint tp = textView.getPaint();
            tp.setFakeBoldText(true);
            textView.setTextSize(25f);
            textView.setGravity(Gravity.CENTER);
            textView.setId(CHOICE_ID_START+i);
            textView.setBackground(getResources().getDrawable(R.drawable.text_rect));
            textView.setOnClickListener(this);
            choicesLayout.addView(textView);
        }
    }


    /**
     * Map:choiceId---->answerLabelId(key,value)
     * @param choice
     * @param answerLabel
     */
    private void createChoiceAndAnswerMap(TextView choice,TextView answerLabel){
        ChoiceAndAnswer choiceAndAnswer = new ChoiceAndAnswer();
        choiceAndAnswer.setAnswerLabelId(answerLabel.getId());
        choiceAndAnswer.setChoiceId(choice.getId());
        choiceAndAnswer.setAnswerLabelText(choice.getText().toString());
        choiceAndAnswer.setAnswerLabelIndex(answerLabelIndex);
        map.put(answerLabel.getId(), choiceAndAnswer);
    }

    private boolean isAnswerTrue(int choiceNumber){
        StringBuffer answer = new StringBuffer();
        for (int i=0;i<choiceNumber;i++){
            TextView textView = (TextView) findViewById(ANSWER_ID_START+i);
            String choice = textView.getText().toString();
            answer.append(choice);
        }
        Log.d(TAG, "answer:" + answer.toString());
        if (answer.toString().equals(currentSpot.getName())){
            return true;
        }
        return false;
    }

    private boolean isAnswerTrue(){
        StringBuffer answer = new StringBuffer();
        for (int i=0;i<currentSpot.getName().length();i++){
            TextView textView = (TextView) findViewById(ANSWER_ID_START+i);
            String choice = textView.getText().toString();
            answer.append(choice);
        }
        Log.d(TAG, "answer:" + answer.toString());
        if (answer.toString().equals(currentSpot.getName())){
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        TextView textView = (TextView) view;
        int id = view.getId();
        int nameLength = currentSpot.getName().length();
        if (id>=CHOICE_ID_START&&textView.getText()!=null&&!"".equals(textView.getText().toString())){
          if(choiceNumber<nameLength){
                String choice = textView.getText().toString();
                if (removeList!=null&&removeList.size()>0){
                    Integer first = removeList.get(0);
                    Log.d(TAG,"first:"+first);
                    TextView t = (TextView) findViewById(first);
                    t.setText(choice);
                    createChoiceAndAnswerMap(textView, t);
                    textView.setText("");
                    removeList.remove(0);
                    if (removeList.size()<1){
                        removeList = null;
                        if (isAnswerTrue()){
                            mHander.sendEmptyMessage(MSG_SUCCESS);
                        }else {
                            Log.d(TAG,"Flash shot");
                            mHander.sendEmptyMessage(MSG_ERROR);
                        }
                    }
                }else{
                    TextView answerLabel = (TextView) findViewById(ANSWER_ID_START + answerLabelIndex);
                    answerLabel.setText(choice);
                    createChoiceAndAnswerMap(textView, answerLabel);
                    choiceNumber++;
                    if (answerLabelIndex<nameLength-1){
                        answerLabelIndex++;
                    }
                    if (choiceNumber>=nameLength){
                        choiceNumber = nameLength;
                        //answerLabelIndex = nameLength;
                    }
                    textView.setText("");
                    Log.d(TAG, "answerLabelIndex:"+answerLabelIndex + ",choiceNumber:" + choiceNumber);
                    if (choiceNumber==currentSpot.getName().length()){
                        if (isAnswerTrue(choiceNumber)){
                            choiceNumber = 0;
                            answerLabelIndex = 0;
                            mHander.sendEmptyMessage(MSG_SUCCESS);
                        }else {
                            Log.d(TAG,"Flash shot");
                            mHander.sendEmptyMessage(MSG_ERROR);
                        }
                    }
                }
            }
        }else if(id<ANSWER_ID_START+100){
            if (removeList==null){
                removeList = new ArrayList<Integer>();
            }
            if (!removeList.contains(id)){
                removeList.add(id);
                Collections.sort(removeList, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer integer, Integer t1) {
                        return integer - t1;
                    }
                });
            }
            if(choiceNumber>-1){
                if (textView.getText()!=null){
                    textView.setText("");
                }
                if (map.size()>0){
                    ChoiceAndAnswer choiceAndAnswer = map.get(textView.getId());
                    if (choiceAndAnswer!=null) {
                        Integer choiceId = choiceAndAnswer.getChoiceId();
                        TextView choiceLabel = (TextView) findViewById(choiceId);
                        choiceLabel.setText(choiceAndAnswer.getAnswerLabelText());
                    }
                }
                choiceNumber--;
                if (choiceNumber<0) choiceNumber=0;
                answerLabelIndex--;
                if (answerLabelIndex<0) answerLabelIndex=0;
                Log.d(TAG, "answerLabelIndex:" + answerLabelIndex);
            }
        }
    }
    Handler mHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MSG_SUCCESS:
                    successOrError(MSG_SUCCESS);
                    break;
                case MSG_ERROR:
                    successOrError(MSG_ERROR);
                    break;
                default:
                    break;
            }
        }
    };

    private void reLayout(){
        answerLayout.removeAllViews();
        choicesLayout.removeAllViews();
        createChoiceLayout();
    }

    private  AlertDialog.Builder initDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setView(alertBuildView);
        builder.setCancelable(true);
        builder.create();
        return builder;
    }


    private void successOrError(int what){
        LayoutInflater inflater = LayoutInflater.from(this);
        View alertBuildView = inflater.inflate(R.layout.success_dialog, null, false);
        ImageView stateImage = (ImageView) alertBuildView.findViewById(R.id.state_icon);
        Button nextBtn = (Button) alertBuildView.findViewById(R.id.next_button);
        TextView tip = (TextView) alertBuildView.findViewById(R.id.tip_title);
        TextView description = (TextView) alertBuildView.findViewById(R.id.description);
        switch (what){
            case MSG_SUCCESS:
                stateImage.setImageDrawable(getResources().getDrawable(R.mipmap.sign_success));
                if (nextBtn!=null){
                    nextBtn.setVisibility(View.VISIBLE);
                }
                tip.setText(getResources().getString(R.string.success_tip));
                description.setText(currentSpot.getDescription());
                AlertDialog.Builder success =initDialog();
                success.setView(alertBuildView);
                final Dialog dialog2 = success.show();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        reLayout();
                        dialog2.dismiss();
                    }
                });
                currentLevel2 += 1;
                if (currentLevel2==featureSpots2.size()){
                    Toast.makeText(this, R.string.tongguan, Toast.LENGTH_LONG).show();
                    currentLevel2=0;
                }
                currentSpot = featureSpots2.get(currentLevel2);
                mixArray = createMixArray(featureSpots,currentSpot);
                Log.d(TAG, "currentSpot:" + currentSpot.getName() + ",currentLevel:" + currentLevel2 + ",tip:" + currentSpot.getTip());
                break;
            case MSG_ERROR:
                AnimationsUtil.flashShot(alertBuildView);
                stateImage.setImageDrawable(getResources().getDrawable(R.mipmap.sign_error));
                tip.setText(getResources().getString(R.string.error_tip));
                if (nextBtn!=null){
                     nextBtn.setVisibility(View.GONE);
                }
                AlertDialog.Builder b =initDialog();
                b.setView(alertBuildView);
                final Dialog dialog = b.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                },2000);
                b = null;
                break;
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PrepareData.LAST_SPOT_ID, currentSpot.getId());
        editor.putString(PrepareData.LAST_CITY_CODE, currentSpot.getCityCode());
        editor.commit();
    }
}
