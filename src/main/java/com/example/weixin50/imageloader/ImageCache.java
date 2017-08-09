package com.example.weixin50.imageloader;

import android.graphics.Bitmap;

/**
 * Created by ldh on 2017/7/14.
 */

public interface ImageCache {

    Bitmap get(String url);

    void put(String url, Bitmap bmp);
}
