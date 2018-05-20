package com.multimedia.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureHelper {

    private final static String TAG = "TextureHelper";

    public static int loadTexture(Context context,int resourceId) {
        final int[] textureIds = new int[1];
        GLES20.glGenTextures(1,textureIds,0);
        if (textureIds[0]==0) {
            if (LoggerConfig.ON) {
                Log.d(TAG,"Could not get a new opengl object");
            }
            return 0;
        }
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resourceId,options);
        if (bitmap==null) {
            if (LoggerConfig.ON) {
                Log.d(TAG,"Resource:"+resourceId+" can not be decoded!");
            }
            GLES20.glDeleteTextures(1,textureIds,0);
            return 0;
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
        return textureIds[0];
    }

}
