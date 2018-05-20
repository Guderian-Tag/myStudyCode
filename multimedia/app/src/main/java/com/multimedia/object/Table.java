package com.multimedia.object;


import android.opengl.GLES20;

import com.multimedia.data.VertexArray;
import com.multimedia.programs.TextureShaderProgram;
import com.multimedia.utils.Constants;

public class Table {

    private final static int POSITION_COMPONENT_COUNT = 2;
    private final static int TEXTURE_COORDINATE_COMPONENT_COUNT = 2;
    private final static int STRIDE = (POSITION_COMPONENT_COUNT+TEXTURE_COORDINATE_COMPONENT_COUNT)
            * Constants.BYTE_PER_FLOAT;
    private final static float[] VERTEXDATA = {
            0f,0f, 0.5f,0.5f,
            -0.5f,-0.8f,0f,0.9f,
            0.5f,-0.8f,1f,0.9f,
            0.5f,0.8f,1f,0.1f,
            -0.5f,0.8f,0f,0.1f,
            -0.5f,-0.8f,0.9f,0.1f
    };
    private final VertexArray vertexArray;

    public Table(){
        vertexArray = new VertexArray(VERTEXDATA);
    }


    public void bindData(TextureShaderProgram textureShaderProgram){
        vertexArray.setVertexAttribPointer(0,
                textureShaderProgram.getaPositionLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT,
                textureShaderProgram.getaTextureCoordinateLocation(),
                TEXTURE_COORDINATE_COMPONENT_COUNT,STRIDE);
    }

    public void draw(){
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,6);
    }

}
