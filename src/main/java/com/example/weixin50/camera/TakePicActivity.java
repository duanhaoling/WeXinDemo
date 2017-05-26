package com.example.weixin50.camera;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weixin50.R;
import com.example.weixin50.widget.CircleProgressView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TakePicActivity extends AppCompatActivity {
    private static final String TAG = "TakePicTestActivity";
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    RelativeLayout rl_btn;
    @Bind(R.id.fl_content)
    FrameLayout flContent;
    //进度条
    @Bind(R.id.circleProgress)
    CircleProgressView circleProgress;
    @Bind(R.id.iv_turn)
    ImageView ivTurn;
    private Camera camera;
    private Camera.Parameters parameters = null;


    Bundle bundle = null; // 声明一个Bundle对象，用来存储数据
    Button takepicture;
    RelativeLayout rl_playPic;
    int w, h;
    protected boolean isPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = true; // true表示没有录像，点击开始；false表示正在录像，点击暂停
    private File mRecVedioPath;
    private File mRecAudioFile;
    //录制视频时的计时器
    private TextView timer;
    private int hour = 0;
    private int minute = 0;
    private int second = 0;
    private boolean bool;


    private Animator animator;
    private boolean isRecordState = false;//是否是视频录制状态
    private int progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pic);
        ButterKnife.bind(this);


        takepicture = (Button) findViewById(R.id.takepicture);
        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
        rl_btn = (RelativeLayout) this.findViewById(R.id.buttonLayout);
        timer = (TextView) this.findViewById(R.id.show_time);
        // 设置计时器不可见
//        timer.setVisibility(View.GONE);


        // 设置缓存路径
        mRecVedioPath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/hfdatabase/video/temp/");


        if (!mRecVedioPath.exists()) {
            mRecVedioPath.mkdirs();
        }


        //圆形进度条设置
        circleProgress.setBgColor(getResources().getColor(R.color.text_white));
        circleProgress.setProgressColor(getResources().getColor(R.color.colorPrimaryDark));
        ViewTreeObserver observerCircle = circleProgress.getViewTreeObserver();
        observerCircle.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                progress = circleProgress.getmProgress();
                return true;
            }
        });


        surfaceView.getHolder()
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(176, 144); //设置Surface分辨率
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数
        //长按录制
        takepicture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isRecordState = true;
                if (isRecording) {
                    /*
                     * 点击开始录像
                     */
                    if (isPreview) {
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    second = 0;
                    minute = 0;
                    hour = 0;
                    bool = true;
                    if (mMediaRecorder == null)
                        mMediaRecorder = new MediaRecorder();
                    else
                        mMediaRecorder.reset();
                    //拍摄视频时的相机配置
                    if (camera != null) {
                        freeCameraResource();
                    }
                    camera = Camera.open();
                    camera.setDisplayOrientation(90);
                    camera.startPreview();
                    camera.unlock();
                    mMediaRecorder.setCamera(camera);
                    mMediaRecorder.setOnErrorListener(null);
                    mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
                    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//                    mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
//                    mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
                    mMediaRecorder.setMaxDuration(60 * 1000);
                    mMediaRecorder.setVideoSize(320, 240);
                    mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);// 设置帧频率，然后就清晰了
//                    mMediaRecorder.setVideoFrameRate(35);
                    mMediaRecorder.setOrientationHint(getPreviewDegree(TakePicActivity.this));// 输出旋转90度，保持竖屏录制
                    try {
                        mRecAudioFile = File.createTempFile("Vedio", ".mp4", mRecVedioPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());


                    try {
                        mMediaRecorder.prepare();
                        timer.setVisibility(View.VISIBLE);
                        handler.postDelayed(task, 1000);
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    showMsg("开始录制");
//                    scalePic.setBackgroundDrawable(iconStop);
                    isRecording = !isRecording;
                    recordAnimater();
                }
                return false;
            }
        });


        takepicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: takepicture==setOnTouchListener==ACTION_UP");
                        if(isRecordState){
                            if(animator.isRunning()){
                                animator.end();
                            }
                        }
                        break;
                }
                return false;
            }
        });
    }


    /*
    * 视频录制时的进度条动画
    * */
    public void recordAnimater() {
        //设置进度条
        startAnimator();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {


            }


            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i("animator", "stop");
                /*
                     * 点击停止
                     */
                try {
                    bool = false;
//                    isRecordState = false;
                    mMediaRecorder.stop();
                    timer.setText(format(hour) + ":" + format(minute) + ":" + format(second));
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                    //录制完成后播放摄像头
                    freeCameraResource();
                    videoRename();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                isRecording = !isRecording;
//                    scalePic.setBackgroundDrawable(iconStart);
                showMsg("录制完成，已保存");
//                        Intent backIntent = new Intent();
//                        backIntent.putExtra("path", mrv_wx.mVecordFile.getAbsoluteFile().toString());
//                        setResult(RESULT_OK, backIntent);
//                        finish();
                Intent displayIntent = new Intent(TakePicActivity.this, ShowPicActivity.class);
                bundle = new Bundle();
                bundle.putBoolean("isRecord", isRecordState);
                bundle.putString("video_path", out.getAbsolutePath());
                displayIntent.putExtras(bundle);
                startActivity(displayIntent);
            }


            @Override
            public void onAnimationCancel(Animator animation) {


            }


            @Override
            public void onAnimationRepeat(Animator animation) {


            }
        });
    }


    @OnClick(R.id.iv_turn)
    public void onClick() {
    }


    private final class SurfaceCallback implements SurfaceHolder.Callback {


        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (camera != null) {
                    freeCameraResource();
                }
                camera = Camera.open(); // 打开摄像头
                parameters = camera.getParameters();
                //加这句小米手机会黑屏
//                parameters.setPreviewFrameRate(5); // 每秒5帧
                parameters.setPictureFormat(PixelFormat.JPEG);// 设置照片的输出格式
                parameters.set("jpeg-quality", 85);// 照片质量
                int PreviewWidth = 0;
                int PreviewHeight = 0;
                // 选择合适的预览尺寸
                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
                // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
                if (sizeList.size() > 1) {
                    Iterator<Camera.Size> itor = sizeList.iterator();
                    while (itor.hasNext()) {
                        Camera.Size cur = itor.next();
                        if (cur.width >= PreviewWidth
                                && cur.height >= PreviewHeight) {
                            PreviewWidth = cur.width;
                            PreviewHeight = cur.height;
                            break;
                        }
                    }
                }
                parameters.setPreviewSize(PreviewWidth, PreviewHeight); // 获得摄像区域的大小
                parameters.setPictureSize(PreviewWidth, PreviewHeight); // 获得保存图片的大小
//                parameters.setPreviewSize(320, 240); // 设置预览大小
//                parameters.setPictureSize(320,240);
//                    parameters.set("orientation", "portrait");
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(getPreviewDegree(TakePicActivity.this));
                camera.startPreview(); // 开始预览
                isPreview = true;
//                camera.autoFocus(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            surfaceHolder = holder;
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            surfaceHolder = holder;
            Log.d(TAG,"surfaceChanged======");
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
//                    camera.setOneShotPreviewCallback(null);
                    initCamera(width, height);
                    camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                }
            });


        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                if (isPreview) {
                    camera.stopPreview();
                    isPreview = false;
                }
                camera.release(); // 释放照相机
                camera = null;
            }
            surfaceHolder = null;
            surfaceView = null;
            mMediaRecorder = null;
        }
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


    /**
     * 按钮被点击触发的事件
     *
     * @param v
     */
    public void btnOnclick(View v) {
        if (camera != null) {
            switch (v.getId()) {
                case R.id.takepicture:
                    // 拍照
                    camera.takePicture(null, null, new MyPictureCallback());
                    break;
            }
        }
    }


    byte[] picData;


    private final class MyPictureCallback implements Camera.PictureCallback {


        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                bundle = new Bundle();
                bundle.putByteArray("bytes", data);//将图片字节数据保存在bundle中，实现数据交换
                picData = data;
//                saveToSDCard(data);
//                camera.startPreview();//拍完照后，重新开始预览
                if (bundle == null) {
                    Toast.makeText(getApplicationContext(), "请先拍照",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), ShowPicActivity.class);
                    bundle.putBoolean("isRecord", isRecordState);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void initCamera(int width, int height) {
        parameters = camera.getParameters(); // 获取各项参数
        parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
        parameters.setPreviewSize(width, height); // 设置预览大小
//        List<Camera.Size> sizes =parameters.getSupportedPreviewSizes();
//        Camera.Size optimalSize = getOptimalPreviewSize(sizes, 320,240);
//        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        parameters.setPreviewFrameRate(5);  //设置每秒显示4帧
        parameters.setPictureSize(width, height); // 设置保存的图片尺寸
        parameters.setJpegQuality(80); // 设置照片质量
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.startPreview();
        camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!isRecordState) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            Log.d("TakePicTestActivity", "success");
                            w = (int) event.getX();
                            h = (int) event.getY();
//                        setLayout(rlFocus,w-50,h-50);
//                        Rect focusRect = calculateTapArea(w,h,100);
                            mHandler.obtainMessage(0).sendToTarget();
                            initCamera(w, h);
                            camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                        }
                    }
                });
            }


        }else if(event.getAction() == MotionEvent.ACTION_UP){
            Log.d(TAG, "onTouchEvent: ACTION_UP");
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
            Log.d(TAG, "onTouchEvent: ACTION_MOVE");
        }
        return super.onTouchEvent(event);
    }


    /*
     * 覆写返回键监听
     */
    @Override
    public void onBackPressed() {
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
//            videoRename();
        }
        finish();
    }


    @Override
    protected void onPause() {
        super.onPause();
        onBackPressed();
    }


    View view;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
//                    flContent.invalidate();
//                    view = new MyView(TakePicTestActivity.this, w, h);
//                    flContent.addView(view);
//                    mHandler.sendEmptyMessageDelayed(1, 1000);//使选框停留1秒后消失
                    break;
                case 1:
                    flContent.removeView(view);
                    break;
            }
        }
    };


    /*
     * 设置控件所在的位置YY，并且不改变宽高，
* XY为绝对位置
*/
    public void setLayout(View view, int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }


    File out;


    /*
    * 生成video文件名字
    */
    protected void videoRename() {


        String path = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/hfdatabase/video/0/";


        String fileName = new SimpleDateFormat("yyyyMMddHHmmss")
                .format(new Date()) + ".mp4";


        out = new File(path);


        if (!out.exists()) {
            out.mkdirs();
        }


        out = new File(path, fileName);


        if (mRecAudioFile.exists())
            mRecAudioFile.renameTo(out);


    }


    /*
     * 消息提示
     */
    private Toast toast;


    public void showMsg(String arg) {
        if (toast == null) {
            toast = Toast.makeText(this, arg, Toast.LENGTH_SHORT);
        } else {
            toast.cancel();
            toast.setText(arg);
        }


        toast.show();
    }


    /*
     * 格式化时间
     */
    public String format(int i) {
        String s = i + "";


        if (s.length() == 1) {
            s = "0" + s;
        }
        return s;
    }


    /*
     * 定时器设置，实现计时
     */
    private Handler handler = new Handler();


    private Runnable task = new Runnable() {
        public void run() {
            if (bool) {
                handler.postDelayed(this, 1000);
                second++;
                if (second >= 60) {
                    minute++;
                    second = second % 60;
                }
                if (minute >= 60) {
                    hour++;
                    minute = minute % 60;
                }
                timer.setText(format(hour) + ":" + format(minute) + ":" + format(second));
            }
        }
    };


    private void startAnimator() {
        circleProgress.setVisibility(View.VISIBLE);
        animator = ObjectAnimator.ofInt(circleProgress, "progress", progress);
        animator.setDuration(60000);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }


    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.lock();
            camera.release();
            camera = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        freeCameraResource();
        if(mMediaRecorder != null){
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


}


