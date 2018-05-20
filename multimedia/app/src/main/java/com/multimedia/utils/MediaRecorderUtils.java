package com.multimedia.utils;


import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaRecorderUtils {

    private MediaRecorder mediaRecorder;
    private boolean isInit = false;

    public void initRecord(SurfaceHolder holder,String path){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoEncodingBitRate(5*1024*1024);
        mediaRecorder.setOrientationHint(90);
        // mediaRecorder.setVideoSize(176,248);
        //mediaRecorder.setVideoFrameRate(20);
        mediaRecorder.setPreviewDisplay(holder.getSurface());
        mediaRecorder.setOutputFile(path);
        isInit = true;
    }

    public void start(){
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Log.d("wang","media record start");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("wang",e.getLocalizedMessage());
        }
    }

    public void stop(){
        if (mediaRecorder!=null ) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }

    public String createVideoName(){
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        return Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+format.format(new Date());
    }

}
