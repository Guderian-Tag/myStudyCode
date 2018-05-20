package com.multimedia.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.multimedia.R;
import com.multimedia.utils.LoggerConfig;
import com.multimedia.utils.ShaderHelper;
import com.multimedia.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class SecondRender implements GLSurfaceView.Renderer {

    private final static String TAG = "SecondRender";

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
    private final static String A_POSITION = "a_Position";
    private int aPositionLocation;

    private final static int COLOR_COMPONENT_COUNT = 3;
    private final static String A_COLOR = "a_Color";
    private final static int STRIKE = (POSITION_COMPONENT_COUNT+COLOR_COMPONENT_COUNT)*BYTE_PER_FLOAT;
    private int aColorLocation;

    //add matrix
    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private final float[] modelMatrix = new float[16];


    public SecondRender(Context context) {
        this.context = context;
        float[] tableVerticesWithTriangle2 = {
                0,0,1f,1f,1f,
                -0.5f,-0.8f,0.7f,0.7f,0.7f,
                0.5f,-0.8f,0.7f,0.7f,0.7f,
                0.5f,0.8f,0.7f,0.7f,0.7f,
                -0.5f,0.8f,0.7f,0.7f,0.7f,
                -0.5f,-0.8f,0.7f,0.7f,0.7f,
                    /*line 1*/
                -0.5f,0f,1f,0f,0f,
                0.5f,0f,1f,0f,0f,
                    /*mallets*/
                0f,-0.25f,0f,0f,1f,
                0f,0.25f,1f,0f,0f
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
                .readTextResourceFromFile(context, R.raw.simple_vertex_shader);
        String fragmentResourceText = TextResourceReader
                .readTextResourceFromFile(context,R.raw.simple_fragment2_shader);
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentResourceText);
        program = ShaderHelper.linkProgram(vertexShader,fragmentShader);
        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }
        GLES20.glUseProgram(program);
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(program,A_COLOR);

        vertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIKE,vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation,COLOR_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIKE,vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width,height);

        final float aspectRation = width>height ? (float)width/(float)height : (float)height/(float)width;
        Log.d(TAG,"The aspectRation is:"+aspectRation);
        if (width>height) {
            Matrix.orthoM(projectionMatrix,0,-aspectRation,aspectRation,-1f,1f,-1f,1f);
        } else {
            Matrix.orthoM(projectionMatrix,0,-1f,1f,-aspectRation,aspectRation,-1f,1f);
        }
        /*Matrix.perspectiveM(projectionMatrix,0,45,(float)width/(float)height,1f,10f);
        Matrix.setIdentityM(projectionMatrix, 0);
        Matrix.translateM(projectionMatrix,0,0f,0f,2.5f);
        Matrix.rotateM(projectionMatrix,0,60f,1f,0f,0f);
        final float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);*/
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);
        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);
        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);

    }

}
