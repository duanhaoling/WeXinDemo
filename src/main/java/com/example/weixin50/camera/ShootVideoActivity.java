package com.example.weixin50.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.weixin50.R;
import com.example.weixin50.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 调用系统相机拍摄视频并展示封面
 * Created by ldh on 2017/4/1 .
 */
public class ShootVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private File cacheFile;
    private final String TAG = this.getClass().getSimpleName();
    private String msg1;
    private String msg2;
    private String msg3;

    @Bind(R.id.sf_capture_camera)
    SurfaceView mSurfaceView;
    @Bind(R.id.crm_count_time)
    Chronometer mTimer;
    @Bind(R.id.tv_shoot_information)
    TextView shootInformation;
    @Bind(R.id.ib_stop)
    ImageButton mBtnStartStop;
    @Bind(R.id.tv_shootvideo_cancel)
    TextView tvCancel;
    @Bind(R.id.tv_shoot_time_remind)
    TextView tvTimeRemind;
    @Bind(R.id.rl_shoot_control)
    RelativeLayout rlSRelativeLayout;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private Camera.Parameters mParameters;
    private int mScreenWidth;
    private int mScreenHeight;
    private String mPath;
    private static Handler mHandler;
    private static final int MSG_SHOW_REMINER = 0x100;
    private static final int MSG_HIDE_REMINER = 0x101;

    private static class ReminderHandler extends Handler {
        private final WeakReference<ShootVideoActivity> mActivity;

        public ReminderHandler(ShootVideoActivity activity) {
            this.mActivity = new WeakReference<ShootVideoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ShootVideoActivity activity = mActivity.get();
            if (activity == null | activity.isFinishing() | activity.getSupportFragmentManager().isDestroyed()) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_REMINER:
                    activity.showReminder(true);
                    break;
                case MSG_HIDE_REMINER:
                    activity.showReminder(false);
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindowFeature();
        checkPermission();

    }

    private void initView() {
        setContentView(R.layout.activity_shoot_video);
        ButterKnife.bind(this);
        getScreenMetrix(this);
        mHandler = new ReminderHandler(this);
        openCamera();
        initlistener();
        hasInitView = true;
    }

    private boolean hasInitView = false;

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasInitView) return;
        if (mCamera == null) {
            openCamera();
        }
        mTimer.setText("00:00:00");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        //这里释放相机会导致录制时，按home键以后不再录制
        if (hasInitView) {
            releaseMediaRecorder();
            showOnPreview();
            releaseCamera();
            showReminder(false);
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        releaseCamera();
        super.onDestroy();
    }

    private long lastClickTime = 0;//记录上一次单击的时间，初始为0，MediaRecorder录制时间少于1秒会crash

    private void initlistener() {
        mBtnStartStop.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < 1200) return;
            lastClickTime = currentTime;
            if (isRecording()) {
                stopRecording();
            } else {
                if (startRecording()) {
                    showOnShooting();
                }
            }
        });
        tvCancel.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    public void showReminder(boolean show) {
        if (show) {
            tvTimeRemind.setVisibility(View.VISIBLE);
        } else {
            tvTimeRemind.setVisibility(View.INVISIBLE);
        }
    }

    private void showOnShooting() {
        mTimer.setBase(SystemClock.elapsedRealtime());
        if (mTimer.getText().length() == 7) { //适配部分机型时间格式0：00
            mTimer.setFormat("00:0%s");
        }
        mTimer.start();
        mHandler.sendEmptyMessageDelayed(MSG_SHOW_REMINER, 1000 * 60 * 2);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_REMINER, 1000 * (60 * 2 + 10));
        rlSRelativeLayout.setBackgroundResource(R.color.transparent);
        tvCancel.setVisibility(View.INVISIBLE);
        mBtnStartStop.setBackgroundResource(R.drawable.selector_shoot_video_stop);

    }

    private void showOnPreview() {
        mTimer.stop();
        showReminder(false);
        rlSRelativeLayout.setBackgroundResource(R.color.brokerBlackColor);
        tvCancel.setVisibility(View.VISIBLE);
        mBtnStartStop.setBackgroundResource(R.drawable.selector_shoot_video_start);
    }

    public boolean startRecording() {
        if (prepareMediaRecorder()) {
            mMediaRecorder.start();
            return true;
        } else {
            releaseMediaRecorder();
        }
        return false;
    }

    private void stopRecording() {
        mMediaRecorder.stop();
        releaseMediaRecorder();
        showOnPreview();

        cacheFile = new File(mPath);
        if (cacheFile.exists()) {
            CharSequence text = mTimer.getText();//00:00:00
            VideoPreviewActivity.start(this, mPath, text.subSequence(3, text.length()).toString());
        }
    }

    private boolean isRecording() {
        return mMediaRecorder != null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void initWindowFeature() {
        getSupportActionBar().hide();
//        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏 // crash
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏

        // 设置横屏显示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // 选择支持半透明模式,在有SurfaceView的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    private void checkPermission() {
        initView();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case PermissionConstant.PERMISSIONS_REQUEST_CAMERA:
//                if (grantResults.length > 0) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        checkPermission();
//                    } else {
//                        PermissionUtils.createPermissionDialog(this, msg1, true);
//                    }
//                }
//                break;
//            case PermissionConstant.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
//                if (grantResults.length > 0) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        checkPermission();
//                    } else {
//                        PermissionUtils.createPermissionDialog(this, msg2, true);
//                    }
//                }
//                break;
//            case PermissionConstant.PERMISSIONS_REQUEST_RECORD_AUDIO:
//                if (grantResults.length > 0) {
//                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                        checkPermission();
//                    } else {
//                        PermissionUtils.createPermissionDialog(this, msg3, true);
//                    }
//                }
//            default:
//        }
//    }

    private void openCamera() {
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        try {
            mCamera = Camera.open();
        } catch (RuntimeException e) {
            finish();
        }
    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        try {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        } catch (Exception e) {
            e.printStackTrace();
            //如果手机不支持1080P格式，设置通用格式
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        }
        mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
        mPath = getPath();
        try {
            mMediaRecorder.setOutputFile(mPath);
            mMediaRecorder.prepare();
        } catch (IOException e) {
            releaseMediaRecorder();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        mParameters = mCamera.getParameters();
        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        //获取摄像头支持的previewsize列表
        List<Camera.Size> previewSizes = mParameters.getSupportedPreviewSizes();
        Camera.Size previewSize = getProperSize(previewSizes, (float) mScreenWidth / mScreenHeight);
        if (previewSize != null) {
            mParameters.setPreviewSize(previewSize.width, previewSize.height);
        }

        mCamera.setParameters(mParameters);
        try {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        Log.d(TAG, "自动对焦成功");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "设置相机预览失败");
            //如果预览失败，也要释放相机资源
            releaseCamera();
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null) {
            // 预览surface不存在
            return;
        }

        // 更改时停止预览
        try {
            mCamera.stopPreview();
        } catch (Exception e) {

        }

        // 在此进行缩放、旋转和重新组织格式
        // 以新的设置启动预览
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        mHolder.removeCallback(this);
        releaseCamera();

    }

    /**
     * 获取屏幕的尺寸
     */
    private void getScreenMetrix(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    /**
     * 获取合适的尺寸
     *
     * @return
     */
    private Camera.Size getProperSize(List<Camera.Size> sizeList, float screenRatio) {
        Log.d(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : sizeList) {
            float currentRatio = (float) size.width / size.height;
            if (Math.abs(currentRatio - screenRatio) < .00001) {
                result = size;
                break;
            }
        }
        //如果没有合适的大小，指定默认
        if (result == null) {
            for (Camera.Size size : sizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (Math.abs(curRatio - 4f / 3) < 0.00001) {
                    result = size;
                    break;
                }
            }
        }
        return result;
    }


    private String getPath() {
        String dirPath = Utils.getSDPath();
        if (TextUtils.isEmpty(dirPath)) {
            dirPath = Utils.getDiskCacheDir(this) + "/";
        } else {
            dirPath += "/newbroker/video/";
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) dirFile.mkdirs();
        }
        return dirPath + System.currentTimeMillis() + ".mp4";
    }

}
