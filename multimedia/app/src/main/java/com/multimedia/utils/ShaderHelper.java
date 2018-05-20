package com.multimedia.utils;


import android.opengl.GLES20;
import android.util.Log;

public class ShaderHelper {

    private final static String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode){
        return compileShader(GLES20.GL_VERTEX_SHADER,shaderCode);
    }

    public static int compileFragmentShader(String shaderCode){
        return compileShader(GLES20.GL_FRAGMENT_SHADER,shaderCode);
    }

    private static int compileShader(int type,String shaderCode){
        final int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId==0) {
            if (LoggerConfig.ON){
                Log.d(TAG,"Cannot create shader!");
            }
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId,shaderCode);
        GLES20.glCompileShader(shaderObjectId);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId,GLES20.GL_COMPILE_STATUS,compileStatus,0);
        if (LoggerConfig.ON){
            Log.d(TAG,"Resource from gl:"+shaderCode+","+GLES20.glGetShaderInfoLog(shaderObjectId));
        }
        if (compileStatus[0]==0){
            GLES20.glDeleteShader(shaderObjectId);
            if (LoggerConfig.ON) {
                Log.d(TAG,"Compilation of shader failed!");
            }
            return 0;
        }
        return shaderObjectId;
    }

    public static int linkProgram(int vertextShaderId,int fragmentShaderId) {
        final int programId = GLES20.glCreateProgram();
        if (programId==0) {
            if (LoggerConfig.ON) {
                Log.d(TAG,"Cannot create program!");
            }
            return 0;
        }
        GLES20.glAttachShader(programId,vertextShaderId);
        GLES20.glAttachShader(programId,fragmentShaderId);

        GLES20.glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programId,GLES20.GL_LINK_STATUS,linkStatus,0);
        if (LoggerConfig.ON){
            Log.d(TAG,"Result of link:"+GLES20.glGetShaderInfoLog(programId));
        }
        if (linkStatus[0]==0){
            GLES20.glDeleteShader(programId);
            if (LoggerConfig.ON) {
                Log.d(TAG,"linking of shader failed!");
                Log.d(TAG,"Error info is:"+GLES20.glGetError());
            }
            return 0;
        }
        return programId;
    }

    public static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validate = new int[1];
        GLES20.glGetProgramiv(programObjectId,GLES20.GL_VALIDATE_STATUS,validate,0);
        Log.d(TAG,"Result of validating program:"+validate[0]+","+GLES20.glGetShaderInfoLog(programObjectId));
        return validate[0]!=0;
    }

    public static int buildProgram(String vertexShaderSource,String fragmentShaderSource){
        int program;
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        program = linkProgram(vertexShader,fragmentShader);
        if (LoggerConfig.ON) {
            validateProgram(program);
        }
        return program;
    }

}
