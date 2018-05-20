package com.multimedia.project;


import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class MediaManager {

    private static MediaManager mediaManager;
    private final static String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final static String VIDEO_PATH = ROOT_PATH+"/media/video";
    private final static String AUDIO_PATH = ROOT_PATH+"/media/audio";

    private MediaRecorder mediaRecorder;

    private MediaManager(){}

    public static MediaManager getInstance(){
        initMediaFolder();
        if (mediaManager==null) {
            mediaManager = new MediaManager();
        }
        return mediaManager;
    }

    private static void initMediaFolder() {
        File videoPath = new File(VIDEO_PATH);
        if (!videoPath.exists()) {
            videoPath.mkdirs();
        }
        File audioPath = new File(AUDIO_PATH);
        if (!audioPath.exists()) {
            audioPath.exists();
        }
    }

    /**
     * 录音
     * @param audioResource
     * @param audioOutputFormat
     * @return
     */
    public boolean useMediaRecorderAudio(int audioResource, int audioOutputFormat) {
        final int samplingRate = 8000;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(audioResource);
        mediaRecorder.setOutputFormat(audioOutputFormat);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioSamplingRate(samplingRate);
        mediaRecorder.setOutputFile(AUDIO_PATH+createMediaName("audio")+".3gp");
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void stopUseMediaRecorder() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private String createMediaName(String mime){
        return mime+"_"+System.currentTimeMillis();
    }

    public boolean useMediaRecorderVideo(int videoSource,int videoEncoder) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(videoSource);
        mediaRecorder.setVideoEncoder(videoEncoder);
        return false;
    }

}
