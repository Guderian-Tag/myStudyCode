package com.multimedia.mediaview;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by archermind on 17-8-21.
 */

public class PlayVideoView extends SurfaceView implements SurfaceHolder.Callback {

    private MediaPlayer mediaPlayer;
    private String mediaPath;

    public PlayVideoView(Context context) {
        this(context,null);
    }

    public PlayVideoView(Context context, AttributeSet attrs) {
        this(context,attrs,-1);
    }

    public PlayVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("wang","surfaceCreated");
        mediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("wang","surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("wang","surfaceDestroyed");
    }

    private void init(){
        if(mediaPlayer==null) {
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    public void startPlay(String path){
        try {
            mediaPath = path;
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

        }
    };

    private void release() {
        if (mediaPlayer!=null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer=null;
        }
    }
}
