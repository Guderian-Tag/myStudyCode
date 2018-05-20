package com.multimedia.my;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.multimedia.R;
import com.multimedia.utils.MatrixHelper;
import com.multimedia.utils.TextResourceReader;
import com.multimedia.utils.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MyRender implements GLSurfaceView.Renderer {

    private static   final int POSITION_COMPONENT_COUNT = 2;
    private static final int  BYTE_PER_FLOAT = 4;
    private FloatBuffer vertexData;
    private Context context;

    private final static int COLOR_COMPONENT_COUNT = 3;
    private final static String A_COLOR = "a_Color";
    private final static int STRIKE = (POSITION_COMPONENT_COUNT+COLOR_COMPONENT_COUNT)*BYTE_PER_FLOAT;
    private int aColorLocation;


    private final static String A_POSITION = "a_Position";
    private int aPositionLocation;

    private final static String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;
    private final float[] modelMatrix = new float[16];

    private int texture;




    public MyRender(Context context) {
        this.context = context;
        vertexData = ByteBuffer.allocateDirect(tableVertices.length*BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVertices);
    }

    private  int compileVertexShader(String shaderCode){
        return compileShader(GLES20.GL_VERTEX_SHADER,shaderCode);
    }

    private  int compileFragmentShader(String shaderCode){
        return compileShader(GLES20.GL_FRAGMENT_SHADER,shaderCode);
    }

    private int compileShader(int type,String shaderCode){
        final int shaderId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderId,shaderCode);
        GLES20.glCompileShader(shaderId);
        Log.d("wang","compile shader:"+GLES20.glGetShaderInfoLog(shaderId));
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderId,GLES20.GL_COMPILE_STATUS,compileStatus,0);
        if (compileStatus[0]==0){
            GLES20.glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }


    private int link(int vertexShader,int fragmentShader){
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program,vertexShader);
        GLES20.glAttachShader(program,fragmentShader);
        GLES20.glLinkProgram(program);
        Log.d("wang","compile program:"+GLES20.glGetShaderInfoLog(program));
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,linkStatus,0);
        if (linkStatus[0]!=GLES20.GL_TRUE){
            GLES20.glDeleteProgram(program);
            Log.d("wang","link error");
           // return 0;
        }
        return program;
    }

    private  int uTextureUnitLocation;
    private  int aTextureCoordinateLocation;
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private int getTextureProgram(){
        String texture1 = TextResourceReader.readTextResourceFromFile(context,R.raw.texture_vertex_shader);
        String texture2 = TextResourceReader.readTextResourceFromFile(context,R.raw.texture_fragment_shader);
        int t1 = compileVertexShader(texture1);
        int t2 = compileFragmentShader(texture2);
        return link(t1,t2);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d("wang","onSurfaceCreated");
        GLES20.glClearColor(0f,0f,0f,0f);
        String shaderCode = TextResourceReader.readTextResourceFromFile(context,R.raw.my_shader);
        String fragmentCode = TextResourceReader.readTextResourceFromFile(context,R.raw.my_fragment);
        int vertexShader = compileVertexShader(shaderCode);
        int fragmentShader = compileFragmentShader(fragmentCode);
        int program = link(vertexShader,fragmentShader);
        GLES20.glUseProgram(program);
        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(program,A_COLOR);
        vertexData.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIKE,vertexData);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX);
        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation,COLOR_COMPONENT_COUNT,GLES20.GL_FLOAT,false,STRIKE,vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);

        GLES20.glUseProgram(getTextureProgram());
        uTextureUnitLocation = GLES20.glGetAttribLocation(program,U_TEXTURE_UNIT);
        aTextureCoordinateLocation = GLES20.glGetAttribLocation(program,A_TEXTURE_COORDINATES);
        vertexData.position(POSITION_COMPONENT_COUNT);
        GLES20.glVertexAttribPointer(uTextureUnitLocation,POSITION_COMPONENT_COUNT,GLES20.GL_FLOAT,
                false,16,vertexData);
        GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation);
        texture = TextureHelper.loadTexture(context, R.mipmap.air_hockey_surface);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        Log.d("wang","onSurfaceChanged");
        GLES20.glViewport(0,0,i,i1);

        MatrixHelper.perspectiveM(projectionMatrix,45,(float)i/(float)i1,1f,10f);
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix,0,0f,0f,-2.5f);
        Matrix.rotateM(modelMatrix,0,-60f,1f,0f,0f);
        final float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,projectionMatrix,0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);
        GLES20.glUniform1i(uMatrixLocation,0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);
        GLES20.glDrawArrays(GLES20.GL_LINES,6,2);


        GLES20.glDrawArrays(GLES20.GL_POINTS,8,1);
        GLES20.glDrawArrays(GLES20.GL_POINTS,9,1);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,10,6);
        GLES20.glDrawArrays(GLES20.GL_LINES,16,2);


    }

    float[] tableVertices = {
            0f,0f,1f,1f,1f,
            -0.6f,-0.6f,0.7f,0.7f,0.7f,
            0.6f,-0.6f,0.7f,0.7f,0.7f,
            0.6f,0.6f,0.7f,0.7f,0.7f,
            -0.6f,0.6f,0.7f,0.7f,0.7f,
            -0.6f,-0.6f,0.7f,0.7f,0.7f,
                    /*line 1*/
            -1f,0f,1f,0f,0f,
            1f,0f,1f,0f,0f,
                    /*mallets*/
            0f,-0.25f,0f,0f,1f,
            0f,0.25f,1f,0f,0f,

            0f,0f,1f,0f,0f,
            0f,-0.5f,1f,0f,0f,
            0.5f,0f,1f,0f,0f,
            0f,0.5f,1f,0f,0f,
            -0.5f,0f,1f,0f,0f,
            0f,-0.5f,1f,0f,0f,

            0f,1f,0f,0f,0f,
            0f,-1f,0f,0f,0f

    };

    private void colorValues(int color){
        float red = Color.red(color)/255;
        float green = Color.green(color)/255;
        float blue = Color.blue(color)/255;
        Log.d("wang","RGB:"+red+","+green+","+blue);
    }
}
