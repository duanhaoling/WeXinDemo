package com.example.weixin50;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weixin50.camera.MediaRecorderActivity;
import com.example.weixin50.camera.TakePicActivity;
import com.example.weixin50.camera.TakePictueActivity;
import com.example.weixin50.fileproviderserver.FileProviderDemo;
import com.example.weixin50.imageloader.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ldh on 2016/8/31 0031.
 */
public class ChatMainTabFragment extends Fragment {
    @Bind(R.id.tv_tab01)
    TextView mTv;
    @Bind({R.id.bt1, R.id.bt2, R.id.bt3, R.id.bt4, R.id.bt5})
    List<Button> bts;
    @Bind(R.id.iv_chat)
    ImageView iv_chat;

    private ImageLoader imageLoader;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab01, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initViews() {
        bts.get(0).setOnClickListener(view -> jsonObjectRequest());

        bts.get(1).setOnClickListener(view -> testImageLoader());

        bts.get(2).setOnClickListener(view -> takePicture());

        bts.get(3).setOnClickListener(view -> takePic());

        bts.get(4).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), FileProviderDemo.class);
            startActivity(intent);
        });
    }

    private void gotoActivity(Class<? extends AppCompatActivity> clazz) {
        Intent intent = new Intent(getContext(), clazz);
        startActivity(intent);
    }

    private void takePic(){
        gotoActivity(TakePicActivity.class);
    }

    private void takePicture() {
        gotoActivity(TakePictueActivity.class);

    }

    private void jsonObjectRequest() {
        gotoActivity(MediaRecorderActivity.class);
    }

    private void testImageLoader() {
        if (imageLoader == null) {
            imageLoader = new ImageLoader();
        }
        String url = "http://imgsrc.baidu.com/forum/pic/item/2fdda3cc7cd98d10ab49e9b2213fb80e7aec9090.jpg";
        imageLoader.displayImage(url, iv_chat);
    }
}
