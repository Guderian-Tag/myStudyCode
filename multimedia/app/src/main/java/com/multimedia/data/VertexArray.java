package com.multimedia.data;


import android.opengl.GLES20;

import com.multimedia.utils.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexArray {

    private FloatBuffer floatBuffer = null;

    public VertexArray(float[] vertexData){
        floatBuffer = ByteBuffer.allocateDirect(vertexData.length* Constants.BYTE_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttribPointer(int dataOffset,int attributeLocation,
                                       int componentCount,int strike) {
        floatBuffer.position(dataOffset);
        GLES20.glVertexAttribPointer(attributeLocation,componentCount,GLES20.GL_FLOAT,
                false,strike,floatBuffer);
        GLES20.glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0);
    }

}
