package com.multimedia.project;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.multimedia.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordAudioActivity extends AppCompatActivity implements View.OnClickListener{

    private boolean isRecording = false;

    private int SAMPLERATE = 44100;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Media/record");
    File audioFile;

    Button startRecord;
    Button stopRecord;
    Button playRecord;

    AudioTrack audioTrack;
    AudioRecord audioRecord;
    int bufferSize;
    DataOutputStream out;
    RecordAudio audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        if (!path.exists()) {
            path.mkdirs();
        }
        try {
            audioFile = File.createTempFile("record",".pcm",path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        init();
        initAudio();
    }

    private void init() {
        startRecord = (Button) findViewById(R.id.start_record_audio);
        stopRecord = (Button) findViewById(R.id.stop_record_audio);
        playRecord = (Button) findViewById(R.id.play_audio);
        startRecord.setOnClickListener(this);
        stopRecord.setOnClickListener(this);
        playRecord.setOnClickListener(this);
    }

    private void play() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLERATE, channelConfig, audioEncoding);
        byte[] buffers = new byte[bufferSize];
        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFile)));
            audioTrack.play();
            while(dataInputStream.available()>0){
                int i = 0;
                while(dataInputStream.available()>0 && i<buffers.length){
                    buffers[i] = dataInputStream.readByte();
                    i++;
                }
                audioTrack.write(buffers, 0, buffers.length);
            }
            audioTrack.stop();
            dataInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initAudio(){
        final int bufferSize = AudioRecord.getMinBufferSize(SAMPLERATE,AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)*100;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLERATE,AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,bufferSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,SAMPLERATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,AudioTrack.MODE_STREAM);
        audio = new RecordAudio();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_record_audio:
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},1);
                    } else {
                        Message msg = Message.obtain();
                        msg.what=1;
                        msg.setTarget(recordHandler);
                        msg.sendToTarget();
                    }
                } else {
                    Message msg = Message.obtain();
                    msg.what=1;
                    msg.setTarget(recordHandler);
                    msg.sendToTarget();
                }
            case R.id.play_audio:
                play();
                break;
            case R.id.stop_record_audio:
                stopRecordAudio();
                break;
        }
    }

    private  Handler recordHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d("wang","msg:"+msg.what);
            switch (msg.what){
                case 1:
                    isRecording=true;
                    new RecordThread(bufferSize).start();
                    break;
                case 2:
                    break;
            }
        }
    };


    private void stopRecordAudio(){
        isRecording = false;
        if (audioRecord!=null && audioRecord.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop();
        }
        if (out!=null) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1 && permissions[0]==Manifest.permission.RECORD_AUDIO) {
            isRecording = true;
            new RecordThread(bufferSize).start();
        }
    }


    private class RecordThread extends Thread{

        int bufferSize;

        public RecordThread(int bufferSize){
            this.bufferSize = bufferSize;
        }

        private void useByte(){
            byte[] buffers = new byte[bufferSize];
            try {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                audioRecord.startRecording();
                int result;
                while (isRecording) {
                    result = audioRecord.read(buffers,0,bufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION!=result) {
                        out.write(buffers,0,result);
                    }
                }
               // outWav(bufferSize);
                audio.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
                audio.setDstPath(path+"/code.aac");
                audio.prepare();
                audio.putPCMData(buffers);
                audio.dstAudioFormatFromPCM();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void useShort() {
            short[] buffers = new short[bufferSize];
            try {
                DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                audioRecord.startRecording();
                int result = audioRecord.read(buffers,0,bufferSize);
                for (int i=0;i<result;i++){
                    outputStream.write(Short.reverseBytes(buffers[i]));
                }
                audioRecord.stop();
                outputStream.close();
                Log.d("wang","record finish");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            useByte();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord!=null) {
            audioRecord.release();
        }
    }

    private void outWav(int bufferSize) {
        FileInputStream ins = null;
        try {
            ins = new FileInputStream(audioFile);
            File outFile = File.createTempFile("out",".wav",path);
            FileOutputStream out = new FileOutputStream(outFile);
            long totalAudioLen = ins.getChannel().size();
            long dataLen = totalAudioLen+36;
            byte[] outBuffer = new byte[bufferSize];
            int channels = audioRecord.getChannelCount();
            writeWaveFileHeader(out,totalAudioLen,dataLen,SAMPLERATE,channels,16*SAMPLERATE*channels/8);
            while ((ins.read(outBuffer))!=-1) {
                out.write(outBuffer);
            }
            ins.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate,
                                     int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);//数据大小
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';//WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        header[12] = 'f'; // 'fmt '
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';//过渡字节
        //数据大小
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //编码方式 10H为PCM编码格式
        header[20] = 1; // format = 1
        header[21] = 0;
        //通道数
        header[22] = (byte) channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (1 * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        //Data chunk
        header[36] = 'd';//data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
}
