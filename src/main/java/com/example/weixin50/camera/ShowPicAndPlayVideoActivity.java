package com.example.weixin50.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.weixin50.MainActivity;
import com.example.weixin50.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShowPicAndPlayVideoActivity extends AppCompatActivity {

    private final static String ALBUM_PATH = Environment.getExternalStorageDirectory() + "/download_img/";
    private static final String TAG = "ShowPicAndPlayVideoActivity";
    String imgName;
    ImageView iv_play;
    @Bind(R.id.iv_back2)
    ImageView ivBack2;
    @Bind(R.id.iv_confirm)
    ImageView ivConfirm;
    @Bind(R.id.tv_repeat)
    TextView tvRepeat;
    @Bind(R.id.rl_confirm)
    RelativeLayout rlConfirm;


    String videoPath;
    @Bind(R.id.sfv_display)
    SurfaceView sfvDisplay;
    private SurfaceHolder sfh_display2;
    MediaPlayer player;
    boolean isRecord = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);
        ButterKnife.bind(this);


        iv_play = (ImageView) findViewById(R.id.iv_playPic);
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        if (data.getBoolean("isRecord")) {
            isRecord = true;
            videoPath = data.getString("video_path");
            iv_play.setVisibility(View.GONE);
            sfvDisplay.setVisibility(View.VISIBLE);
            Log.d(TAG, "videoPath=" + videoPath);
            showVideo();
        } else {
            isRecord = false;
            iv_play.setVisibility(View.VISIBLE);
            sfvDisplay.setVisibility(View.GONE);
            setImageBitmap(data.getByteArray("bytes"));
        }


    }


    /**
     * 将MainActivity传过来的图片显示在界面当中
     *
     * @param bytes
     */
    public void setImageBitmap(byte[] bytes) {
        Bitmap cameraBitmap = byte2Bitmap();
        // 根据拍摄的方向旋转图像（纵向拍摄时要需要将图像选择90度)
        Matrix matrix = new Matrix();
        matrix.setRotate(TakePicAndVideoActivity.getPreviewDegree(this));


        cameraBitmap = Bitmap
                .createBitmap(cameraBitmap, 0, 0, cameraBitmap.getWidth(),
                        cameraBitmap.getHeight(), matrix, true);
        imgName = Calendar.getInstance().getTimeInMillis() + ".jpg";
        saveFile(cameraBitmap, imgName);
        iv_play.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv_play.setImageBitmap(cameraBitmap);
    }


    //保存图片到本地
    public void saveFile(Bitmap bm, String imgName) {
        File dirFile = new File(ALBUM_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }


        File myFile = new File(ALBUM_PATH + imgName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myFile));
            bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    /**
     * 从Bundle对象中获取数据
     *
     * @return
     */
    public byte[] getImageFormBundle() {
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        byte[] bytes = data.getByteArray("bytes");
        return bytes;
    }


    /**
     * 将字节数组的图形数据转换为Bitmap
     *
     * @return
     */
    private Bitmap byte2Bitmap() {
        byte[] data = getImageFormBundle();
        // 将byte数组转换成Bitmap对象
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        return bitmap;
    }


    @OnClick({R.id.iv_back2, R.id.iv_confirm, R.id.tv_repeat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back2:
                finish();
                break;
            case R.id.iv_confirm:
                Intent i = new Intent(this, MainActivity.class);
                if(isRecord){
                    i.putExtra("toMainPath", videoPath);
                }else{
                    i.putExtra("toMainPath", ALBUM_PATH + imgName);
                }
                i.putExtra("isRecord",isRecord);
                //要启动的activity已经在当前的任务中，那么在该activity之上的activity都会关闭，并且intent会传递给在栈顶的activity


                //如果 Activity 已经是运行在 Task 的 top，则该 Activity 将不会再被启动
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);


                break;
            case R.id.tv_repeat:
                Intent intent1 = new Intent(ShowPicAndPlayVideoActivity.this, TakePicAndVideoActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
    }


    //显示并播放录制的视频
    public void showVideo(){
        sfh_display2 = sfvDisplay.getHolder();
        sfh_display2.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player = new MediaPlayer();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setDisplay(sfh_display2);
                try {
                    player.setDataSource(videoPath);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


            }


            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {


            }
        });
        sfh_display2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
}



