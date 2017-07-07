package com.example.weixin50.trashcan;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
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
import com.example.weixin50.camera.ShowPicAndPlayVideoActivity;
import com.example.weixin50.widget.CircleProgressView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.weixin50.trashcan.CameraConfiguration.getPreviewDegree1;


public class TakePicTestActivity extends AppCompatActivity implements Camera.PreviewCallback {
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
    ImageView iv_takeBack;
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
    private String fileName;//视频文件名
    String model = android.os.Build.MODEL;//手机的型号
    private Animator animator;
    private boolean isRecordState = false;//是否是视频录制状态
    private int progress;
    private int cameraPosition = 0;//0代表后置摄像头，1代表前置摄像头
    private static int isScreenConfigChange = 0;//0 代表竖屏 3代表横屏
    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private Point screenResolution;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // land do nothing is ok
            Log.i("info", "landscape"); // 横屏
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // port do nothing is ok
            Log.i("info", "portrait"); // 竖屏
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initCamera();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pic);
        ButterKnife.bind(this);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        takepicture = (Button) findViewById(R.id.takepicture);
        iv_takeBack = (ImageView) findViewById(R.id.iv_takeBack);
        surfaceView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        rl_btn = (RelativeLayout) this.findViewById(R.id.buttonLayout);
        timer = (TextView) this.findViewById(R.id.show_time);
        // 设置计时器不可见
//        timer.setVisibility(View.GONE);

        // 设置缓存路径
        mRecVedioPath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/hfdatabase/video/temp/");

        if (!mRecVedioPath.exists()) {
            mRecVedioPath.mkdirs();//可以创建指定目录以及所有的父目录
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
                    camera = Camera.open(cameraPosition);
                    //  CameraConfiguration.setCameraDisplayOrientation(TakePicTestActivity.this, cameraPosition, camera);
                    int result = getPreviewDegree1(TakePicTestActivity.this, cameraPosition);
                    camera.setDisplayOrientation(result);
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
                    mMediaRecorder.setMaxDuration(60 * 1000);
                    mMediaRecorder.setVideoSize(320, 240);
                    mMediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);// 设置帧频率，然后就清晰了
//                    mMediaRecorder.setVideoFrameRate(15);
                    if (cameraPosition == 1) {
                        if (isScreenConfigChange == 3) {
                            mMediaRecorder.setOrientationHint(90);
                        } else {
                            mMediaRecorder.setOrientationHint(270);
                        }
                    } else if (cameraPosition == 0) {
                        mMediaRecorder.setOrientationHint(90);
                    }
                    //    mMediaRecorder.setOrientationHint(90);// 输出旋转90度，保持竖屏录制
                    try {
                        mRecAudioFile = File.createTempFile("Vedio", ".mp4", mRecVedioPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());

                    try {
                        mMediaRecorder.prepare();
                        timer.setVisibility(View.GONE);
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
                return true;
            }
        });
        //返回按钮
        iv_takeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("click——-——————>","点击了");
                parameters = camera.getParameters();
                List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
                int index = getPictureSize(supportedPictureSizes);
                parameters.setPictureSize(supportedPictureSizes.get(index).width, supportedPictureSizes.get(index).height);
                camera.setParameters(parameters);
                camera.takePicture(null, null, new MyPictureCallback());

            }
        });
        takepicture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch: takepicture==setOnTouchListener==ACTION_UP");
                        if (isRecordState) {
                            if (second <= 1) {
//                                showMsg("录制时间太短");
                                Toast.makeText(TakePicTestActivity.this, "录制时间太短", Toast.LENGTH_SHORT).show();
//                                animator.cancel();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (animator.isRunning()) {
                                    animator.end();
                                }
//                                mMediaRecorder.setOnErrorListener(null);
//                                mMediaRecorder.setPreviewDisplay(null);
//                                mMediaRecorder.stop();
//                                mMediaRecorder.reset();
                            } else {
                                if (animator.isRunning()) {
                                    animator.end();
                                }
                            }

                        }
                        break;
                }
                return false;
            }
        });
    }

    protected void onResume() {
        super.onResume();
        timer.setText("点击拍照，长按摄像");
        timer.setVisibility(View.VISIBLE);
        circleProgress.setVisibility(View.GONE);
        isRecordState = false;
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
                if (second > 1) {
                    freeCameraResource();
                    Intent displayIntent = new Intent(TakePicTestActivity.this, ShowPicAndPlayVideoActivity.class);
                    bundle = new Bundle();
                    bundle.putBoolean("isRecord", isRecordState);
                    bundle.putString("video_path", out.getAbsolutePath());//视频路径
                    bundle.putString("video_name", fileName);//视频名
                    displayIntent.putExtras(bundle);
                    startActivity(displayIntent);
//                    finish();
                }
                if (mMediaRecorder != null) {
                    freeMediaRecorderResource();
                }
                timer.setText("点击拍照，长按摄像");
                circleProgress.setVisibility(View.GONE);
                isRecordState = false;
                second = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * 切换前后摄像头
     */
    @OnClick(R.id.iv_turn)
    public void onClick() {
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 0) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    //  camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, cameraPosition));
                    if (model != null) {
                        if (model.equals("MI 5")) {//针对小米五机型的特殊配置
                            if (isScreenConfigChange == 3) {//横屏
                                camera.setDisplayOrientation(0);
                            } else {
                                camera.setDisplayOrientation(270);
                            }

                        } else {
                            camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, cameraPosition));
                        }
                    } else {
                        camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, cameraPosition));
                    }

                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 1;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, cameraPosition));
                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
                    cameraPosition = 0;
                    break;
                }
            }

        }

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

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
//                List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
//                int index = getPictureSize(supportedPictureSizes);
//                parameters.setPreviewSize(supportedPictureSizes.get(index).width, supportedPictureSizes.get(index).height);
//                int PreviewWidth = 0;
//                int PreviewHeight = 0;
//                // 选择合适的预览尺寸
//                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
//                // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
//                if (sizeList.size() > 1) {
//                    Iterator<Camera.Size> itor = sizeList.iterator();
//                    while (itor.hasNext()) {
//                        Camera.Size cur = itor.next();
//                        if (cur.width >= PreviewWidth
//                                && cur.height >= PreviewHeight) {
//                            PreviewWidth = cur.width;
//                            PreviewHeight = cur.height;
//                            break;
//                        }
//                    }
//                }
//                Log.d("size---->", "宽" + PreviewWidth + "高" + PreviewHeight);
//                parameters.setPreviewSize(PreviewWidth, PreviewHeight); // 获得摄像区域的大小
//                parameters.setPictureSize(PreviewWidth, PreviewHeight); // 获得保存图片的大小
                camera.setParameters(parameters);
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, cameraPosition));
                camera.startPreview(); // 开始预览
                isPreview = true;
                // 获得手机的方向
                int rotation = TakePicTestActivity.this.getWindowManager().getDefaultDisplay()
                        .getRotation();
                isScreenConfigChange = rotation;
//                camera.autoFocus(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            surfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            surfaceHolder = holder;
            Log.d(TAG, "surfaceChanged======");
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
//                    camera.setOneShotPreviewCallback(null);
                    Camera.Parameters parameters = camera.getParameters();
                    Camera.Size s = CameraConfiguration.getBestSupportedSize(parameters.getSupportedPreviewSizes(), w, h);
                    initCamera(s.width, s.height);
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

    private static Point findBestPreviewSizeValue(List<Camera.Size> sizeList, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int size = 0;
        for (int i = 0; i < sizeList.size(); i++) {
            // 如果有符合的分辨率，则直接返回
            if (sizeList.get(i).width == DEFAULT_WIDTH && sizeList.get(i).height == DEFAULT_HEIGHT) {
                Log.d(TAG, "get default preview size!!!");
                return new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            }

            int newX = sizeList.get(i).width;
            int newY = sizeList.get(i).height;
            int newSize = Math.abs(newX * newX) + Math.abs(newY * newY);
            float ratio = (float) newY / (float) newX;
            Log.d(TAG, newX + ":" + newY + ":" + ratio);
            if (newSize >= size && ratio != 0.75) {  // 确保图片是16：9的
                bestX = newX;
                bestY = newY;
                size = newSize;
            } else if (newSize < size) {
                continue;
            }
        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    static byte[] picData;

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                bundle = new Bundle();
                //   bundle.putByteArray("bytes", data);//将图片字节数据保存在bundle中，实现数据交换
                picData = data;
//                saveToSDCard(data);
//                camera.startPreview();//拍完照后，重新开始预览
                if (bundle == null) {
                    Toast.makeText(getApplicationContext(), "请先拍照",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(TakePicTestActivity.this, ShowPicAndPlayVideoActivity.class);

                    //  intent.setClass(getApplicationContext(), ShowPicAndPlayVideoActivity.class);
                    bundle.putBoolean("isRecord", isRecordState);
                    bundle.putInt("isPosition", cameraPosition);
                    bundle.putInt("isScreenConfigChange", isScreenConfigChange);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    if (camera != null) {
                        freeCameraResource();
                    }
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

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "onTouchEvent: ACTION_UP");
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
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
//        onBackPressed();
        if (mMediaRecorder != null) {
//            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
//            videoRename();
        }
        handler.removeCallbacks(task);
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
////                    flContent.addView(view);
//                    if (flContent.getChildCount() == 3) {
//                        flContent.addView(view);
//                        mHandler.sendEmptyMessageDelayed(1, 1000);//使选框停留1秒后消失
//                    }
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

        fileName = System.currentTimeMillis() + ".mp4";

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
                if (second >= 60) {
                    minute++;
                    second = second % 60;
                }
                if (minute >= 60) {
                    hour++;
                    minute = minute % 60;
                }
                timer.setText(format(hour) + ":" + format(minute) + ":" + format(second));
                second++;
                handler.postDelayed(this, 1000);
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

    /**
     * 释放录像资源
     */
    private void freeMediaRecorderResource() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.setPreviewDisplay(null);
                mMediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        freeCameraResource();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    /**
     * 切记用intent传递图片
     */
    public static byte[] getDatea() {
        byte[] data = picData;
        return data;
    }

    /*获取手机相片的大小*/
    private int getPictureSize(List<Camera.Size> sizes) {
        // 屏幕的宽度
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        Log.d(TAG, "screenWidth=" + screenWidth);
        int index = -1;


        for (int i = 0; i < sizes.size(); i++) {
            if (screenWidth == sizes.get(i).width) {
                index = i;
            }
        }
        // 当未找到与手机分辨率相等的数值,取列表中间的分辨率
        if (index == -1) {
            index = sizes.size() / 2;
        }


        return index;
    }

    /*初始化camera的数据不管什么情况变成后摄像头*/
    private void initCamera() {
        if (camera != null) {
            camera.stopPreview();//停掉原来摄像头的预览
            camera.release();//释放资源
            camera = null;//取消原来摄像头
        }
        camera = Camera.open(0);//打开当前选中的摄像头
        camera.setDisplayOrientation(getPreviewDegree1(TakePicTestActivity.this, 0));
        try {
            camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        camera.startPreview();//开始预览
        cameraPosition = 0;
    }
}
