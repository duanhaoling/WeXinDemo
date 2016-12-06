package com.example.weixin50.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ldh on 2016/9/3 0003.
 */
public class ImageLoader {
    //图片缓存
    ImageCache mImageCache = new ImageCache();
    //线程池，线程数量为CPU的数量
    ExecutorService mExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void displayImage(final String url, final ImageView imageView) {
        Bitmap bitmap = mImageCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        imageView.setTag(url);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = downLoadImage(url);
                if (bitmap == null) {
                    return;
                }
                if (imageView.getTag().equals(url)) {
                    imageView.setImageBitmap(bitmap);
                }
                mImageCache.put(url, bitmap);
            }
        });
    }

    public Bitmap downLoadImage(String imageUrl) {
        Bitmap bitmap = null;
        InputStream in = null;
//        FileOutputStream fos = null;
        try {
            URL url = new URL(imageUrl);
            //开启连接
            final HttpURLConnection conn= (HttpURLConnection) url.openConnection();
            //设置超时的时间5000毫秒
            conn.setConnectTimeout(5000);
            //设置获取图片的方式为get
            conn.setRequestMethod("GET");
            //响应码为200，则访问成功
            if (conn.getResponseCode() == 200) {
                //获取连接的输入流，这个输入流就是图片的输入流
                in = conn.getInputStream();
                //构建一个file对象用于存储图片
//                File file = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
//                fos = new FileOutputStream(file);
//                int len = 0;
//                byte[] bytes = new byte[1024];

                bitmap = BitmapFactory.decodeStream(in);
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
