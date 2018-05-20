package com.multimedia.project;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ProjectUtils {

    public static String getFilePathByUri(Context context,Uri uri){
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri,new String[]{MediaStore.Video.Media.DATA},null,null,null);
        if (cursor!=null) {
            while (cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            }
            cursor.close();
        } else {
            path = uri.getPath();
        }
        return path;
    }

    public static Intent createPickIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        return intent;
    }

    public static Intent createPickIntent(String type) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(type);
        return intent;
    }

}
