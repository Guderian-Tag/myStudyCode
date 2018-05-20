package com.multimedia.mediacodec;


import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoCut {

    private final static String TAG = "VideoCut";

    private MediaExtractor mediaExtractor;
    private MediaMuxer mediaMuxer;
    private ByteBuffer byteBuffer;
    private int maxInputSize;


    private void init(String sourcePath) throws IOException {
        mediaExtractor = new MediaExtractor();
        String videoName = sourcePath.substring(0,sourcePath.lastIndexOf('.'))+"_out.mp4";
        mediaMuxer = new MediaMuxer(videoName,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mediaMuxer.setOrientationHint(90);
    }

    public void videoCut(String path,long cutPoint,long cutDuration){
        try {
            init(path);
            mediaExtractor.setDataSource(path);
            int numberTracks = mediaExtractor.getTrackCount();
            int videoTrack = -1;
            int audioTrack = -1;
            MediaFormat videoFormat = null;
            MediaFormat audioFormat = null;
            int videoSourceTrack = -1;
            int audioSourceTrack = -1;
            for (int i=0;i<numberTracks;i++) {
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.d(TAG,"mime:"+mime);
                maxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                if (byteBuffer==null) {
                    byteBuffer = ByteBuffer.allocate(maxInputSize);
                }
                if (mime.startsWith("video/")){
                    videoTrack = mediaMuxer.addTrack(format);
                    videoFormat = format;
                    videoSourceTrack = i;
                } else if (mime.startsWith("audio/")) {
                    audioTrack = mediaMuxer.addTrack(format);
                    audioFormat = format;
                    audioSourceTrack = i;
                }
            }
            mediaMuxer.start();
            videoTrackCut(videoFormat,videoSourceTrack,videoTrack,cutPoint,cutDuration);
            audioTrackCut(audioFormat,audioSourceTrack,audioTrack,cutPoint,cutDuration);
            release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void audioTrackCut(MediaFormat format,int audioSourceTrack,int audioTrack,long cutPoint,long cutDuration) {
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int audioMaxInputSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        long audioDuration = format.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG,"rate:"+sampleRate+",c:"+channelCount+",m:"+audioMaxInputSize+",d:"+audioDuration+",t:"+audioTrack);
        mediaExtractor.selectTrack(audioSourceTrack);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs=0;
        long audioSampleTime;
        mediaExtractor.readSampleData(byteBuffer,0);
        if (mediaExtractor.getSampleTime()==0) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer,0);
        long firstAudioRate = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer,0);
        long secondAudioRate = mediaExtractor.getSampleTime();
        audioSampleTime = Math.abs(secondAudioRate-firstAudioRate);
        Log.d(TAG,"sample:"+audioSampleTime);
        mediaExtractor.seekTo(cutPoint,MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        int sampleSize;
        while ((sampleSize=mediaExtractor.readSampleData(byteBuffer,0))!=-1) {
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            long presentationTimeUs = bufferInfo.presentationTimeUs;
            Log.d(TAG,"trackIndex:"+trackIndex+",presentationTimeUs:"+presentationTimeUs);
            if (cutPoint>0 && presentationTimeUs>(cutDuration+cutPoint)) {
                mediaExtractor.unselectTrack(audioSourceTrack);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset=0;
            bufferInfo.size=sampleSize;
            mediaMuxer.writeSampleData(audioTrack,byteBuffer,bufferInfo);
            bufferInfo.presentationTimeUs += audioSampleTime;
        }
        mediaExtractor.unselectTrack(audioSourceTrack);
    }

    private void videoTrackCut(MediaFormat format,int videoSourceTrack,int videoTrack,long cutPoint,long cutDuration){
        int width = format.getInteger(MediaFormat.KEY_WIDTH);
        int height = format.getInteger(MediaFormat.KEY_HEIGHT);
        long duration = format.getLong(MediaFormat.KEY_DURATION);
        Log.d(TAG,"w:"+width+",h:"+height+",m:"+videoTrack+",d:"+duration);
        mediaExtractor.selectTrack(videoSourceTrack);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = 0;
        long videoFrameTimes ;
        mediaExtractor.readSampleData(byteBuffer,0);
        if (mediaExtractor.getSampleFlags()==MediaExtractor.SAMPLE_FLAG_SYNC) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer,0);
        mediaExtractor.advance();
        long firstPTS = mediaExtractor.getSampleTime();
        mediaExtractor.readSampleData(byteBuffer,0);
        mediaExtractor.advance();
        long secondPTS = mediaExtractor.getSampleTime();
        videoFrameTimes = Math.abs(secondPTS-firstPTS);
        Log.d(TAG,"videoFrameTimes:"+videoFrameTimes);
        mediaExtractor.seekTo(cutPoint,MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        int sampleSize;
        while ((sampleSize=mediaExtractor.readSampleData(byteBuffer,0))!=-1) {
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            long presentationTimeUs = bufferInfo.presentationTimeUs;
            int sampleFlag = mediaExtractor.getSampleFlags();
            Log.d(TAG,"trackIndex:"+trackIndex+",flag:"+sampleFlag);
            if (cutDuration!=0 && presentationTimeUs>(cutDuration+cutPoint)) {
                mediaExtractor.unselectTrack(videoSourceTrack);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset=0;
            bufferInfo.flags = sampleFlag;
            bufferInfo.size = sampleSize;
            mediaMuxer.writeSampleData(videoTrack,byteBuffer,bufferInfo);
            bufferInfo.presentationTimeUs += videoFrameTimes;
        }
        mediaExtractor.unselectTrack(videoSourceTrack);
    }

    private void release(){
        mediaMuxer.stop();
        mediaMuxer.release();
        mediaExtractor.release();
        mediaExtractor = null;
    }


}
