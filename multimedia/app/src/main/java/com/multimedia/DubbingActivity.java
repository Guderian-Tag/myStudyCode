package com.multimedia;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaExtractor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.Toast;

import com.multimedia.project.MediaDivider;
import com.multimedia.project.ProjectUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DubbingActivity extends AppCompatActivity implements View.OnClickListener{

    private final static String TAG = "DubbingActivity";

    private final static int REQUEST_READ_STORAGE = 1;
    private final static int REQUEST_RECORD_AUDIO = 2;
    private final static int ACTIVITY_PICK_VIDEO = REQUEST_READ_STORAGE;

    ImageButton selectAndPlay;
    Button startDubbing;
    Button endDubbing;
    SurfaceView playSurfaceView;

    MediaPlayer mediaPlayer;
    MediaController mediaController;
    MediaRecorder mediaRecorder;

    String videoSrc;
    String videoToDubbingPath;
    String audioToDubbingPath;

    MediaDivider divider = new MediaDivider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dubbing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaController.setMediaPlayer(control);
        mediaController.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init() {
        selectAndPlay = (ImageButton) findViewById(R.id.play_button);
        startDubbing = (Button) findViewById(R.id.start_dubbing);
        endDubbing = (Button) findViewById(R.id.end_dubbing);
        selectAndPlay.setOnClickListener(this);
        startDubbing.setOnClickListener(this);
        endDubbing.setOnClickListener(this);

        playSurfaceView = (SurfaceView) findViewById(R.id.media_player);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        PlayVideoHolder holder = new PlayVideoHolder();
        playSurfaceView.getHolder().addCallback(holder);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.media_player));
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            endDubbing();
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.play_button:
                managePlayButton();
                break;
            case R.id.start_dubbing:
                startDubbing();
                break;
            case R.id.end_dubbing:
                endDubbing();
                break;
        }
    }

    private void startDubbing() {
        if (videoSrc==null) {
            Toast.makeText(getApplicationContext(),"Please select a video",Toast.LENGTH_LONG).show();
            return;
        }
        videoToDubbingPath = divider.prepareVideoMedia(videoSrc);
        if (videoToDubbingPath!=null) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(videoToDubbingPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioToDubbingPath = divider.prepareAudio();
    }

    private void endDubbing() {
        if (audioToDubbingPath!=null && videoToDubbingPath!=null) {
            divider.closeMediaRecorder();
            divider.mixture(audioToDubbingPath,videoToDubbingPath);
            try {
                playVideo(divider.getVideoOutputPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void managePlayButton(){
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_READ_STORAGE);
        } else {
            Intent pickVideo = ProjectUtils.createPickIntent();
            startActivityForResult(pickVideo,ACTIVITY_PICK_VIDEO);
        }
    }

    public void test(String path) throws IOException {
        try {
            FileInputStream in = new FileInputStream(path);
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(in.getFD());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void playVideo(Uri uri){
        if (uri==null) return;
        try {
            videoSrc = ProjectUtils.getFilePathByUri(getApplicationContext(),uri);
            mediaPlayer.setDataSource(videoSrc);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if (selectAndPlay.getVisibility()==View.VISIBLE) {
                selectAndPlay.setVisibility(View.INVISIBLE);
            }
            playSurfaceView.setBackground(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playVideo(String path) throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(path);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mediaController.show();
        return super.onTouchEvent(event);
    }

    MediaController.MediaPlayerControl control = new MediaController.MediaPlayerControl() {
        @Override
        public void start() {
            mediaPlayer.start();
        }

        @Override
        public void pause() {
            mediaPlayer.pause();
        }

        @Override
        public int getDuration() {
            return mediaPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int i) {
            mediaPlayer.seekTo(i);
        }

        @Override
        public boolean isPlaying() {
            return mediaPlayer.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            return 0;
        }

        @Override
        public boolean canPause() {
            return true;
        }

        @Override
        public boolean canSeekBackward() {
            return true;
        }

        @Override
        public boolean canSeekForward() {
            return true;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_OK) {
            if (requestCode==ACTIVITY_PICK_VIDEO) {
                Uri uri = data.getData();
                playVideo(uri);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==REQUEST_READ_STORAGE && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent pickVideo = ProjectUtils.createPickIntent();
            startActivityForResult(pickVideo,ACTIVITY_PICK_VIDEO);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null) {
            mediaPlayer = null;
        }
    }

    private class PlayVideoHolder implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            mediaPlayer.setDisplay(surfaceHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mediaPlayer!=null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
            mediaController.setEnabled(false);
        }
    }
}
