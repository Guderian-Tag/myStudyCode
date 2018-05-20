package com.multimedia.project;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;

import com.multimedia.R;

import java.io.IOException;

public class ProjectActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private final static int ACTION_PICK_REQUEST = 100;
    private final static int REQUEST_DIVIDE_AUDIO = 1;
    private final static int REQUEST_DIVIDE_VIDEO = 2;

    Button selectVideo;
    MediaPlayer mediaPlayer;
    SurfaceView videoPreviewSurface;
    Button divideAudio;
    Button divideVideo;
    String sourceVideoPath;

    MediaController mediaController;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        init();
    }

    private void init() {
        selectVideo = (Button) findViewById(R.id.select_video);
        selectVideo.setOnClickListener(onClickListener);
        videoPreviewSurface = (SurfaceView) findViewById(R.id.video_preview_surface);
        videoPreviewSurface.getHolder().addCallback(this);
        divideAudio = (Button) findViewById(R.id.divide_audio);
        divideAudio.setOnClickListener(onClickListener);
        divideVideo = (Button) findViewById(R.id.divide_video);
        divideVideo.setOnClickListener(onClickListener);

    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.select_video:
                    Intent intent = ProjectUtils.createPickIntent();
                    startActivityForResult(intent,ACTION_PICK_REQUEST);
                    break;
                case R.id.divide_audio:
                    divideAudioToFile();
                    break;
                case R.id.divide_video:
                    divideVideoToFile();
                    break;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.M)
    private void divideVideoToFile(){
        if (sourceVideoPath==null) {
            Toast.makeText(getApplicationContext(),"Please select a video!",Toast.LENGTH_LONG).show();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_DIVIDE_VIDEO);
        } else {
            MediaDivider divider = new MediaDivider();
            divider.divideMedia(sourceVideoPath,MediaDivider.VIDEO_MIME);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void divideAudioToFile() {
        if (sourceVideoPath==null) {
            Toast.makeText(getApplicationContext(),"Please select a video!",Toast.LENGTH_LONG).show();
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_DIVIDE_AUDIO);
        } else {
            MediaDivider divider = new MediaDivider();
            divider.divideMedia(sourceVideoPath,MediaDivider.AUDIO_MIME);
        }
    }

    private void previewVideo(Uri uri){
        try {
            if (mediaPlayer==null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(),uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoPreviewSurface);
        mediaController.setMediaPlayer(new DoubleArrowSeekBar(mediaPlayer));
        mediaController.setEnabled(true);
        mediaController.show(3000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mediaController!=null) {
            mediaController.show();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {
            if (requestCode==ACTION_PICK_REQUEST) {
                Uri uri = data.getData();
                String path = ProjectUtils.getFilePathByUri(getApplicationContext(),uri);
                Log.d("wang","uri:"+uri.toString()+",path:"+path);
                sourceVideoPath = path;
                previewVideo(uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==REQUEST_DIVIDE_AUDIO) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                MediaDivider divider = new MediaDivider();
                divider.divideMedia(sourceVideoPath,MediaDivider.AUDIO_MIME);
            }
        } else if (requestCode==REQUEST_DIVIDE_VIDEO) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                MediaDivider divider = new MediaDivider();
                divider.divideMedia(sourceVideoPath,MediaDivider.VIDEO_MIME);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("wang","surfaceCreated");
        if (mediaPlayer==null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("wang","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("wang","surfaceDestroyed");
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoPreviewSurface.getHolder().removeCallback(this);
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
