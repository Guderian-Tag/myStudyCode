package com.multimedia.utils;


import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {

    public static String readTextResourceFromFile(Context context,int resourceId){
        StringBuilder body = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedInputStream = new BufferedReader(inputStreamReader);
        String nextLine;
        try {
            while ((nextLine=bufferedInputStream.readLine())!=null) {
                body.append(nextLine);
                body.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body.toString();
    }

}
