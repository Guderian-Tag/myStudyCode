package com.multimedia.project;


import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaDivider {

    private final static String TAG = "MediaDivider";

    private final static String ROOT_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath()+"/Media";
    private final static String MEDIA_DIVIDE_PATH = ROOT_PATH+"/divide";
    private final static String MEDIA_DUBBING_PATH = ROOT_PATH+"/dubbing";
    private String currentTaskPath = null;

    public final static String AUDIO_MIME = "audio";
    public final static String VIDEO_MIME = "video";

    private String videoOutputPath;

    public MediaDivider(){
        initOutputPath();
    }

    private void initOutputPath(){
        File file = new File(ROOT_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        File divide = new File(MEDIA_DIVIDE_PATH);
        if (!divide.exists()) {
            divide.exists();
        }
        currentTaskPath = createCurrentTaskPath();
        File currentTask = new File(currentTaskPath);
        if (!currentTask.exists()) {
            currentTask.mkdirs();
        }
        File dubbingPath = new File(MEDIA_DUBBING_PATH);
        if (!dubbingPath.exists()) {
            dubbingPath.mkdirs();
        }
    }

    private String createCurrentTaskPath(){
        return MEDIA_DIVIDE_PATH+"/"+System.currentTimeMillis();
    }

    public void divideMedia(String sourceMediaPath,String divideMime){
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(sourceMediaPath);
            int trackCount = mediaExtractor.getTrackCount();
            for (int i=0;i<trackCount;i++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (!mime.startsWith(divideMime)) {
                    continue;
                }
                int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                Log.d(TAG,"maxInputSize:"+maxInputSize);
                ByteBuffer videoByteBuffer = ByteBuffer.allocate(maxInputSize);
                if (divideMime.equals(AUDIO_MIME)) {
                    Log.d(TAG,"divide audio media to file");
                    String audioName = currentTaskPath+"/"
                            +sourceMediaPath.substring(sourceMediaPath.lastIndexOf('/')+1,sourceMediaPath.lastIndexOf('.'))
                            +"_audio_out.mp4";
                    Log.d(TAG,"audioName:"+audioName);
                    MediaMuxer mediaMuxer = new MediaMuxer(audioName,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int audioTrack = mediaMuxer.addTrack(mediaFormat);
                    mediaMuxer.start();
                    divideToOutputAudio(mediaExtractor,mediaMuxer,videoByteBuffer,mediaFormat,audioTrack,i);
                    finish(mediaExtractor,mediaMuxer);
                    break;
                } else if (divideMime.equals(VIDEO_MIME)) {
                    Log.d(TAG,"divide video media to file");
                    String videoName = currentTaskPath+"/"
                            +sourceMediaPath.substring(sourceMediaPath.lastIndexOf('/')+1,sourceMediaPath.lastIndexOf('.'))
                            +"_video_out.mp4";
                    Log.d(TAG,"videoName:"+videoName);
                    MediaMuxer mediaMuxer = new MediaMuxer(videoName,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int videoTrack = mediaMuxer.addTrack(mediaFormat);
                    mediaMuxer.start();
                    divideToOutputVideo(mediaExtractor,mediaMuxer,videoByteBuffer,mediaFormat,videoTrack,i);
                    finish(mediaExtractor,mediaMuxer);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    MediaRecorder mediaRecorder;
    public String prepareAudio() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            String recorderName = "dubbing_"+System.currentTimeMillis()+".aac";
            File outPutFile = new File(MEDIA_DUBBING_PATH,recorderName);
            mediaRecorder.setOutputFile(outPutFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            return MEDIA_DUBBING_PATH+"/"+recorderName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void closeMediaRecorder(){
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    public String prepareVideoMedia(String sourceMediaPath){
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(sourceMediaPath);
            int trackCount = mediaExtractor.getTrackCount();
            if (trackCount==1) return sourceMediaPath;
            for (int i=0;i<trackCount;i++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                Log.d(TAG,"trackCount:"+trackCount+",mime:"+mime);
                if (mime.startsWith(VIDEO_MIME)) {
                    int maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    ByteBuffer audioByteBuffer = ByteBuffer.allocate(maxInputSize);
                    String videoName = MEDIA_DUBBING_PATH+"/"
                            +sourceMediaPath.substring(sourceMediaPath.lastIndexOf('/')+1,sourceMediaPath.lastIndexOf('.'))
                            +"_video_out.mp4";
                    MediaMuxer mediaMuxer = new MediaMuxer(videoName,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int audioTrack = mediaMuxer.addTrack(mediaFormat);
                    mediaMuxer.start();
                    divideToOutputAudio(mediaExtractor,mediaMuxer,audioByteBuffer,mediaFormat,audioTrack,i);
                    finish(mediaExtractor,mediaMuxer);
                    return videoName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void divideToOutputVideo(MediaExtractor mediaExtractor,MediaMuxer mediaMuxer,ByteBuffer byteBuffer,MediaFormat format,
                                     int videoTrack,int videoTrackIndex) {
        long videoDuration = format.getLong(MediaFormat.KEY_DURATION);
        mediaExtractor.selectTrack(videoTrackIndex);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = 0;
        long videoFrameTimes;
        mediaExtractor.readSampleData(byteBuffer,0);
        if (mediaExtractor.getSampleFlags()!=MediaExtractor.SAMPLE_FLAG_SYNC) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer,0);
        mediaExtractor.advance();
        long firstFrame = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer,0);
        long secondFrame = mediaExtractor.getSampleTime();
        videoFrameTimes = Math.abs(secondFrame-firstFrame);
        mediaExtractor.seekTo(0,MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        int sampleSize;
        while ((sampleSize=mediaExtractor.readSampleData(byteBuffer,0))!=-1){
            long presentTime = bufferInfo.presentationTimeUs;
            if (presentTime>=videoDuration) {
                mediaExtractor.unselectTrack(videoTrackIndex);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset=0;
            bufferInfo.flags=mediaExtractor.getSampleFlags();
            bufferInfo.size=sampleSize;
            mediaMuxer.writeSampleData(videoTrack,byteBuffer,bufferInfo);
            bufferInfo.presentationTimeUs +=videoFrameTimes;
        }
        mediaExtractor.unselectTrack(videoTrackIndex);
    }

    private void divideToOutputAudio(MediaExtractor mediaExtractor,MediaMuxer mediaMuxer,ByteBuffer byteBuffer,MediaFormat format,
                                     int audioTrack,int audioTrackIndex){
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        Log.d(TAG,"rate:"+sampleRate+",c:"+channelCount);
        long audioDuration = format.getLong(MediaFormat.KEY_DURATION);
        mediaExtractor.selectTrack(audioTrackIndex);//参数为多媒体文件MediaExtractor获取到的track count的索引,选择音频轨道
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = 0;
        long audioSampleSize;
        mediaExtractor.readSampleData(byteBuffer,0);
        if (mediaExtractor.getSampleTime()==0) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer,0);
        long firstRateSample = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer,0);
        long secondRateSample = mediaExtractor.getSampleTime();
        audioSampleSize = Math.abs(secondRateSample-firstRateSample);
        mediaExtractor.seekTo(0,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        int sampleSize;
        while ((sampleSize=mediaExtractor.readSampleData(byteBuffer,0))!=-1) {
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            long presentationTimeUs = bufferInfo.presentationTimeUs;
            Log.d(TAG,"trackIndex:"+trackIndex+",presentationTimeUs:"+presentationTimeUs);
            if (presentationTimeUs>=audioDuration){
                mediaExtractor.unselectTrack(audioTrackIndex);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset=0;
            bufferInfo.size=sampleSize;
            mediaMuxer.writeSampleData(audioTrack,byteBuffer,bufferInfo);//audioTrack为通过mediaMuxer.add()获取到的
            bufferInfo.presentationTimeUs += audioSampleSize;
        }
        mediaExtractor.unselectTrack(audioTrackIndex);
    }

    private void finish(MediaExtractor mediaExtractor,MediaMuxer mediaMuxer){
        mediaMuxer.stop();
        mediaMuxer.release();
        mediaMuxer = null;
        mediaExtractor.release();
        mediaExtractor = null;
    }

    public void mixture(String audioPath,String videoPath){
        MediaFormat audioFormat;
        MediaFormat videoFormat;
        MediaExtractor audioMediaExtractor = new MediaExtractor();
        MediaExtractor videoMediaExtractor = new MediaExtractor();
        ByteBuffer byteBuffer;
        MediaMuxer mediaMuxer = null;
        try {
            String outPutPath = ROOT_PATH + "/mixture_" + System.currentTimeMillis() + ".mp4";
            mediaMuxer = new MediaMuxer(outPutPath
                    , MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            audioMediaExtractor.setDataSource(audioPath);
            int mediaTrackIndex = 0;
            audioFormat = audioMediaExtractor.getTrackFormat(mediaTrackIndex);
            int audioMaxInputSize = 0;
            if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            }
            videoMediaExtractor.setDataSource(videoPath);
            videoFormat = videoMediaExtractor.getTrackFormat(mediaTrackIndex);
            int videoMaxInputSize = 0;
            if (videoFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                videoMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            }
            int bufferSize = Math.max(audioMaxInputSize, videoMaxInputSize);
            byteBuffer = ByteBuffer.allocate(bufferSize);
            int audioTrack = mediaMuxer.addTrack(audioFormat);
            int videoTrack = mediaMuxer.addTrack(videoFormat);
            mediaMuxer.start();
            divideToOutputVideo(videoMediaExtractor, mediaMuxer, byteBuffer, videoFormat, videoTrack, mediaTrackIndex);
            divideToOutputAudio(audioMediaExtractor, mediaMuxer, byteBuffer, audioFormat, audioTrack, mediaTrackIndex);
            videoOutputPath = outPutPath;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            videoMediaExtractor.release();
            audioMediaExtractor.release();
            if (mediaMuxer!=null) {
                mediaMuxer.stop();
                mediaMuxer.release();
            }
        }
    }

    public String getVideoOutputPath(){
        return videoOutputPath;
    }

}
