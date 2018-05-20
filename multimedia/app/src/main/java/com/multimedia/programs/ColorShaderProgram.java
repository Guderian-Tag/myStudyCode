package com.multimedia.programs;


import android.content.Context;
import android.opengl.GLES20;

import com.multimedia.R;

public class ColorShaderProgram extends ShaderProgram{

    private final int uMatrixLocation;
    private final int aPositionLocation;

    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getaColorLocation() {
        return aColorLocation;
    }

    private final int aColorLocation;
    private int uColorLocation;

    public ColorShaderProgram(Context context){
        super(context, R.raw.simple_vertex_shader,R.raw.simple_fragment_shader);
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
        aColorLocation = GLES20.glGetAttribLocation(program,A_COLOR);
        uColorLocation = GLES20.glGetUniformLocation(program,U_COLOR);
    }

    public void setUniforms(float[] matrix){
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
    }

    public void setUniforms(float[] matrix,float r,float g,float b){
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
        GLES20.glUniform4f(uColorLocation,r,g,b,1f);
    }


}
