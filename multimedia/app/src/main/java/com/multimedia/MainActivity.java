package com.multimedia;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import com.multimedia.utils.MediaRecorderUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private final static int REQUEST_PERMISSION = 1;

    SurfaceView playVideoView;
    MediaPlayer mediaPlayer;
    MediaController mediaController;
    private int currentPosition = 0;

    Button startRecord;
    Button stopRecord;
    Button playRecord;
    MediaRecorderUtils mediaRecorderUtils;
    SurfaceHolder recordHolder;
    String recordPath;

    private void initWidget(){
        mediaRecorderUtils = new MediaRecorderUtils();
        startRecord = (Button) findViewById(R.id.start_record);
        stopRecord = (Button) findViewById(R.id.stop_record);
        startRecord.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_PERMISSION);
                } else {
                    String path = mediaRecorderUtils.createVideoName()+".3gp";
                    recordPath = path;
                    mediaRecorderUtils.initRecord(recordHolder,path);
                }
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
                } else {
                    mediaRecorderUtils.start();
                }
            }
        });
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaRecorderUtils.stop();
            }
        });

        playRecord = (Button) findViewById(R.id.play_video);
        playRecord.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
                } else {
                    play(recordPath);
                }
            }
        });
    }

    private void play(String path) {
        Log.d("wang","play");
        try {
            if (path==null) return;
            Log.d("wang",mediaPlayer.isPlaying()+"");
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("wang",e.getLocalizedMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playVideoView = (SurfaceView) findViewById(R.id.play_view);
        playVideoView.getHolder().addCallback(this);
        mediaController = new MediaController(this);
        mediaController.setAnchorView(findViewById(R.id.root));
        initPlayer();
        initWidget();
    }

    private void initPlayer() {
        if (mediaPlayer==null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnBufferingUpdateListener(bufferingUpdateListener);
    }

    private void play() {
        Log.d("wang","play");
        try {
            Log.d("wang",mediaPlayer.isPlaying()+"");
            mediaPlayer.reset();
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath()+"/VIDEO0009.mp4");
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("wang",e.getLocalizedMessage());
        }
    }

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            mediaPlayer.seekTo(currentPosition);
        }
    };

    private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.d("wang","play onCompletion");
        }
    };

    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
            Log.d("wang","OnBufferingUpdateListener:"+i);
        }
    };


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("wang","onResume");
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        } else {
          //  play();
        }
        mediaController.setMediaPlayer(control);
        mediaController.setEnabled(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mediaController.show();
        return super.onTouchEvent(event);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("wang","onRequestPermissionsResult");
        if (requestCode==REQUEST_PERMISSION) {
            if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[0])) {
                play(recordPath);
            } else if (Manifest.permission.CAMERA.equals(permissions[0])) {
                String path = mediaRecorderUtils.createVideoName();
                recordPath = path;
                mediaRecorderUtils.initRecord(recordHolder,path);
            } else if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permissions[0])) {
                mediaRecorderUtils.start();
            }
        }
    }

    private MediaController.MediaPlayerControl control = new MediaController.MediaPlayerControl() {
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
            currentPosition = mediaPlayer.getCurrentPosition();
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public void seekTo(int i) {
            mediaPlayer.seekTo(i);
        }

        @Override
        public boolean isPlaying() {
            if (mediaPlayer!=null && mediaPlayer.isPlaying()){
                return true;
            }
            return false;
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
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("wang","surfaceCreated");
        mediaPlayer.setDisplay(surfaceHolder);
        recordHolder = surfaceHolder;
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
            mediaPlayer.setDisplay(null);
        }
        mediaController.setEnabled(false);
    }

    private void releaseMedia() {
        if (mediaPlayer==null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("wang","onDestroy");
        releaseMedia();
        control = null;
        playVideoView.getHolder().removeCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("wang","onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("wang","onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("wang","onStop");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("wang","onStart");
    }

}
