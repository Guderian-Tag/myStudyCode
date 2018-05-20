package com.multimedia.programs;


import android.content.Context;
import android.opengl.GLES20;

import com.multimedia.utils.ShaderHelper;
import com.multimedia.utils.TextResourceReader;

public class ShaderProgram {

    //uniform constant
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_COLOR = "u_Color";


    //Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    //Shader program
    protected final int program;
    protected ShaderProgram(Context context,int vertexShaderResourceId,
                            int fragmentShaderResourceId){
        program = ShaderHelper.buildProgram(TextResourceReader.readTextResourceFromFile(context,
                vertexShaderResourceId),TextResourceReader.readTextResourceFromFile(context,fragmentShaderResourceId));
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }

}
