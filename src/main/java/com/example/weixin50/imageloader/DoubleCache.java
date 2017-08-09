package com.example.weixin50.imageloader;

import android.graphics.Bitmap;

/**
 * Created by ldh on 2017/7/14.
 * 双缓存
 */

public class DoubleCache implements ImageCache{
    MemoryCache mMemoryCache = new MemoryCache();
    DiskCache mDiskCache = new DiskCache();

    /**
     * 先从Memory中获取，，如果没有，再从SD卡中获取
     * @param url
     * @return
     */
    public Bitmap get(String url) {

        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap == null) {
            bitmap = mDiskCache.get(url);
        }
        return bitmap;
    }

    /**
     * 将图片缓存到内存与SD卡中
     * @param url
     * @param bmp
     */
    public void put(String url, Bitmap bmp) {
        mMemoryCache.put(url, bmp);
        mDiskCache.put(url, bmp);
    }
}
