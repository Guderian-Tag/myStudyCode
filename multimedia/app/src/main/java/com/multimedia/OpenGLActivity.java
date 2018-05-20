package com.multimedia;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.multimedia.mediacodec.VideoCut;
import com.multimedia.opengl.AirHockRender;
import com.multimedia.utils.LoggerConfig;
import com.multimedia.utils.ShaderHelper;
import com.multimedia.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean isRender = false;
    AirHockRender airHockRender;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(getApplicationContext());
        glSurfaceView.setOnTouchListener(onTouchListener);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean isSupport2 = configurationInfo.reqGlEsVersion>=0x20000;
        airHockRender = new AirHockRender(getApplicationContext());
        if (isSupport2) {
            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
            glSurfaceView.setRenderer(airHockRender);
            isRender = true;
        } else {
            Toast.makeText(this,"Device does not support openGL 2",Toast.LENGTH_LONG).show();
        }
        setContentView(glSurfaceView);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            VideoCut videoCut = new VideoCut();
           // videoCut.videoCut(Environment.getExternalStorageDirectory().getAbsolutePath()+"/VIDEO0009.mp4",1000L,21000000L);
        }

    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent!=null) {
                final float nomalizedX = (motionEvent.getX()/(float)view.getWidth()/2)*2-1;
                final float nomalizedY = (motionEvent.getY()/(float)view.getHeight()/2)*2-1;
                if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            airHockRender.handleTouchPress(nomalizedX,nomalizedY);
                        }
                    });
                } else if (motionEvent.getAction()==MotionEvent.ACTION_MOVE) {
                    glSurfaceView.queueEvent(new Runnable() {
                        @Override
                        public void run() {
                            airHockRender.handleTouchDrag(nomalizedX,nomalizedY);
                        }
                    });
                }
                return true;
            }
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                VideoCut videoCut = new VideoCut();
              //  videoCut.videoCut(Environment.getExternalStorageDirectory().getAbsolutePath()+"/VIDEO0009.mp4",1000L,21000000L);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRender) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRender) {
            glSurfaceView.onResume();
        }
    }

    private class GLSurfaceRender implements GLSurfaceView.Renderer{

        private static   final int POSITION_COMPONENT_COUNT = 2;
        private static final int  BYTE_PER_FLOAT = 4;
        private FloatBuffer vertexData;
        private Context context;
        float[] tableVertices = {
                0,0,
                0.5f,0.5f,
                0.5f,-0.5f,
                0.5f,0.5f,
                -0.5f,0.5f,
                -0.5f,-0.5f
        };

        private int program;

        private final static String U_COLOR = "u_Color";
        private int uColorLocation;
        private final static String A_POSITION = "a_Position";
        private int aPositionLocation;


        public GLSurfaceRender(Context context) {
            this.context = context;
            float[] tableVerticesWithTriangle = {
                    /*triangle 1*/
                    -0.5f,-0.5f,
                    0.5f,0.5f,
                    -0.5f,0.5f,
                    /*triangle 2*/
                    -0.5f,-0.5f,
                    0.5f,-0.5f,
                    0.5f,0.5f,
                    /*line 1*/
                    -0.5f,0f,0.5f,0f,
                    /*mallets*/
                    0f,-0.25f,0f,0.25f};
            float[] tableVerticesWithTriangle2 = {
                    0,0,
                    -0.5f,-0.5f,
                    0.5f,-0.5f,
                    0.5f,0.5f,
                    -0.5f,0.5f,
                    -0.5f,-0.5f,
                    /*line 1*/
                    -0.5f,0f,
                    0.5f,0f,
                    /*mallets*/
                    0f,-0.25f,
                    0f,0.25f,
            };
            vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangle2.length*BYTE_PER_FLOAT)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexData.put(tableVerticesWithTriangle2);
        }

        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            GLES20.glClearColor(0f,0f,0f,0f);
            String vertexShaderSource = TextResourceReader
                    .readTextResourceFromFile(context,R.raw.simple_vertex_shader);
            String fragmentResourceText = TextResourceReader
                    .readTextResourceFromFile(context,R.raw.simple_fragment_shader);
            int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
            int fragmentShader = ShaderHelper.compileFragmentShader(fragmentResourceText);
            program = ShaderHelper.linkProgram(vertexShader,fragmentShader);
            if (LoggerConfig.ON) {
                ShaderHelper.validateProgram(program);
            }
            GLES20.glUseProgram(program);

            uColorLocation = GLES20.glGetUniformLocation(program,U_COLOR);
            aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
            vertexData.position(0);
            GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,false,0,vertexData);
            GLES20.glEnableVertexAttribArray(aPositionLocation);
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {
            GLES20.glViewport(0,0,i,i1);
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUniform4f(uColorLocation,1.0f,1.0f,1.0f,1.0f);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

            GLES20.glUniform4f(uColorLocation,1.0f,0f,0f,1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,6,2);

            GLES20.glUniform4f(uColorLocation,0.0f,0f,1.0f,1.0f);
            GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);
            GLES20.glUniform4f(uColorLocation,1.0f,0f,0f,1.0f);
            GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);
        }
    }
}
