package com.example.weixin50;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.weixin50.widget.BadgeView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mAdapter;
    private List<Fragment> mDatas;

    private TextView mChatTv;
    private TextView mFriendTv;
    private TextView mContactTv;
    private LinearLayout mLinearLayout;
    private BadgeView mBadgeView;

    private int mScreen1_3;
    private ImageView mTabline;
    private int mCurrentIndex;
    private ContactMainTabFragment tab03;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initTabLine();
        initView();
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
        mViewPager = (ViewPager) findViewById(R.id.viewpager_wx);
        mChatTv = (TextView) findViewById(R.id.tv_chat);
        mFriendTv = (TextView) findViewById(R.id.tv_friend);
        mContactTv = (TextView) findViewById(R.id.tv_contact);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_chat_wapper);


        mDatas = new ArrayList<>();
        ChatMainTabFragment tab01 = new ChatMainTabFragment();
        FriendMainTabFragment tab02 = new FriendMainTabFragment();
        tab03 = new ContactMainTabFragment();
        mDatas.add(tab01);
        mDatas.add(tab02);
        mDatas.add(tab03);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
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
                            mBadgeView = new BadgeView(MainActivity.this);
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

    class MyAdapter extends FragmentStatePagerAdapter {
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
