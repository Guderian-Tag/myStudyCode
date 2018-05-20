package com.multimedia.project;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.multimedia.R;

public class MixtureActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String AUDIO_MIME = "audio/*";
    private final static String VIDEO_MIME = "video/*";
    private final static int REQUEST_AUDIO = 1;
    private final static int REQUEST_VIDEO = 2;

    Button selectAudio;
    Button selectVideo;
    Button mix;

    private String audioPath;
    private String videoPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mixture);
        init();
    }

    private void init() {
        selectAudio = (Button) findViewById(R.id.select_audio);
        selectVideo = (Button) findViewById(R.id.select_video);
        mix = (Button) findViewById(R.id.mix);
        mix.setOnClickListener(this);
        selectAudio.setOnClickListener(this);
        selectVideo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.select_audio:
                intent = ProjectUtils.createPickIntent(AUDIO_MIME);
                startActivityForResult(intent,REQUEST_AUDIO);
                break;
            case R.id.select_video:
                intent = ProjectUtils.createPickIntent(VIDEO_MIME);
                startActivityForResult(intent,REQUEST_VIDEO);
                break;
            case R.id.mix:
                if (audioPath!=null && videoPath!=null) {
                    MediaDivider divider = new MediaDivider();
                    divider.mixture(audioPath,videoPath);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {
            Uri uri = data.getData();
            if (requestCode==REQUEST_VIDEO) {
                videoPath = ProjectUtils.getFilePathByUri(getApplicationContext(),uri);
                Log.d("wang",videoPath);
            } else if (requestCode==REQUEST_AUDIO) {
                audioPath = ProjectUtils.getFilePathByUri(getApplicationContext(),uri);
                Log.d("wang",audioPath);
            }
        }
    }
}
