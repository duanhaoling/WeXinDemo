package com.example.weixin50.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Desc  :  Uri工具类
 * Date  : 16-3-9.
 * Author: tim.
 */
public class UriUtils {

    public static String getRealPathFromURI(Context context, Uri contentUri) {

        if(contentUri == null) return "";

        String imgPath = null;
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
        if(cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imgPath =  cursor.getString(columnIndex);
        }

        cursor.close();
        return imgPath;
    }


}