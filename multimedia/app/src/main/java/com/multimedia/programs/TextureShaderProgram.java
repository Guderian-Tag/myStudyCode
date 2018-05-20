package com.multimedia.programs;


import android.content.Context;
import android.opengl.GLES20;

import com.multimedia.R;

public class TextureShaderProgram extends ShaderProgram {

    //Uniform locations
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;
    private final int aTextureCoordinateLocation;

    //Attribute locations
    private final int aPositionLocation;

    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getaTextureCoordinateLocation() {
        return aTextureCoordinateLocation;
    }

    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);
        uMatrixLocation = GLES20.glGetUniformLocation(program,U_MATRIX);
        uTextureUnitLocation = GLES20.glGetUniformLocation(program,U_TEXTURE_UNIT);
        aPositionLocation = GLES20.glGetAttribLocation(program,A_POSITION);
        aTextureCoordinateLocation = GLES20.glGetAttribLocation(program,A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix,int textureId){
        GLES20.glUniformMatrix4fv(uMatrixLocation,1,false,matrix,0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
        GLES20.glUniform1i(uTextureUnitLocation,0);
    }

}
