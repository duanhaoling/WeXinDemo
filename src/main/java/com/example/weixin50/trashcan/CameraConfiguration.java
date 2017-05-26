package com.example.weixin50.trashcan;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;

import java.util.List;

/**
 * Created by ldh on 2017/5/25.
 */

class CameraConfiguration {
    public static int getPreviewDegree1(Activity activity, int cameraPosition) {
        return 0;
    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }

    public static Camera.Size getBestSupportedSize(List<Camera.Size> supportedPreviewSizes, int w, int h) {
        return null;
    }
}
