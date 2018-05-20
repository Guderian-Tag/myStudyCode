package com.multimedia.programs;


import android.opengl.GLES20;

import com.multimedia.data.VertexArray;
import com.multimedia.utils.Constants;

public class Mallet {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT+COLOR_COMPONENT_COUNT)* Constants.BYTE_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            0f,-0.4f,0f,0f,1f,
            0f,0.4f,1f,0f,0f
    };
    private final VertexArray vertexArray;

    public Mallet(){
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(ColorShaderProgram colorShaderProgram){
        vertexArray.setVertexAttribPointer(0,
                colorShaderProgram.getaPositionLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
                colorShaderProgram.getaColorLocation(),
                COLOR_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_POINTS,0,2);
    }
}
