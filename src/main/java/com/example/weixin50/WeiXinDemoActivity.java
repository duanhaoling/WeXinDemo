package com.example.weixin50;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weixin50.widget.BadgeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WeiXinDemoActivity extends AppCompatActivity {

    private static final String TAG = "WeiXinDemoActivity";
    @Bind(R.id.iv_main_search)
    ImageView ivMainSearch;
    @Bind(R.id.iv_main_add)
    ImageView ivMainAdd;
    @Bind(R.id.iv_main_more)
    ImageView ivMainMore;
    @Bind(R.id.tv_chat)
    TextView mChatTv;
    @Bind(R.id.ll_chat_wapper)
    LinearLayout mLinearLayout;
    @Bind(R.id.tv_friend)
    TextView mFriendTv;
    @Bind(R.id.tv_contact)
    TextView mContactTv;
    @Bind(R.id.iv_tabline)
    ImageView mTabline;
    @Bind(R.id.viewpager_wx)
    ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mDatas;

    private BadgeView mBadgeView;

    private int mScreen1_3;
    private int mCurrentIndex;
    private ChatMainTabFragment tab01;
    private FriendMainTabFragment tab02;
    private ContactMainTabFragment tab03;
    private WifiManager wifiMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initTabLine();
        initView();
        ivMainSearch.setOnClickListener(v -> {
            testWifi();
        });
    }


    private void testWifi() {
        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiMgr.getWifiState();
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;
        Log.d(TAG, "wifiId:" + wifiId);
        if (info == null) return;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(info.getSSID())
                .setMessage(info.toString())
                .setPositiveButton("yes", null)
                .show();

    }

    private void initTabLine() {
        mTabline = (ImageView) findViewById(R.id.iv_tabline);
        //两种方式获取WindowManager
//        WindowManager WindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        mScreen1_3 = outMetrics.widthPixels / 3;
        ViewGroup.LayoutParams lp = mTabline.getLayoutParams();
        lp.width = mScreen1_3;
        mTabline.setLayoutParams(lp);
    }

    private void initView() {

        mDatas = new ArrayList<>();
        tab01 = new ChatMainTabFragment();
        tab02 = new FriendMainTabFragment();
        tab03 = new ContactMainTabFragment();
        mDatas.add(tab01);
        mDatas.add(tab02);
        mDatas.add(tab03);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);//不设置为2，两边的页面来回切换时会被置为空白
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabline.getLayoutParams();
                if (mCurrentIndex == 0 && position == 0) {   //0=>1
                    lp.leftMargin = (int) (positionOffset * mScreen1_3);
                } else if (mCurrentIndex == 1 && position == 0) { //1=>0
                    lp.leftMargin = (int) ((mCurrentIndex + positionOffset - 1) * mScreen1_3);
                } else if (mCurrentIndex == 1 && position == 1) { //1=>2
                    lp.leftMargin = (int) ((mCurrentIndex + positionOffset) * mScreen1_3);
                } else if (mCurrentIndex == 2 && position == 1) {  //2=>1
                    lp.leftMargin = (int) ((mCurrentIndex + positionOffset - 1) * mScreen1_3);
                }
                mTabline.setLayoutParams(lp);
            }

            @Override
            public void onPageSelected(int position) {
                resetTextView();
                switch (position) {
                    case 0:
                        if (mBadgeView == null) {
                            mBadgeView = new BadgeView(WeiXinDemoActivity.this);
                            mBadgeView.setBadgeCount(7);
                            mLinearLayout.addView(mBadgeView);
                        } else {
                            mLinearLayout.removeView(mBadgeView);
                            mBadgeView = null;
                        }
                        mChatTv.setTextColor(Color.parseColor("#008000"));
                        break;
                    case 1:
                        mFriendTv.setTextColor(Color.parseColor("#008000"));
                        break;
                    case 2:
                        mContactTv.setTextColor(Color.parseColor("#008000"));
                        break;
                    default:
                        break;
                }
                mCurrentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void resetTextView() {
        mChatTv.setTextColor(Color.BLACK);
        mFriendTv.setTextColor(Color.BLACK);
        mContactTv.setTextColor(Color.BLACK);
    }

    class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }
    }

}
