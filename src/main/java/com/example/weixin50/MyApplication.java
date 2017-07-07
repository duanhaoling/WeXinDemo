package com.example.weixin50;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * Created by ldh on 2016/9/23 0023.
 */
public class MyApplication extends Application {

    private ImageLoader imageLoader;
    private static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .build();

        imageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        imageLoader.init(config);
        //复现android.view.WindowManager$BadTokenException
        /*try {
            Thread.currentThread().sleep(30000L);
            Log.d("duanhao", "say hello in main!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


       /* new Thread() {
            @Override
            public void run() {
                try {
                    String name = Thread.currentThread().getName();
                    sleep(10000L);
                    Log.d("duanhao", name + " ->10s");
                    sleep(10000L);
                    Log.d("duanhao", name + " ->20s");
                    sleep(10000L);
                    Log.d("duanhao", name + " ->30s");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();*/
    }

    public static Context getInstance() {
        return sInstance;
    }
}
