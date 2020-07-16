package com.telegram.helper.chat;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.telegram.helper.MainActivity;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.login.TIMHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class ChatRoomActivity extends BaseActivity {
    @BindView(R.id.content_viewpager)
    ViewPager contentViewpager;
    @BindView(R.id.chat_new_list)
    TextView chatNewList;
    @BindView(R.id.chat_friend)
    TextView chatFriend;
    @BindView(R.id.chat_group)
    TextView chatGroup;
    List<Fragment> mFragments = new ArrayList<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, ChatRoomActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_room;
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.chat_title);
        selectTab(0);
        for (int i = 0; i < 2; i++) {
            ChatRoomFragment fragment = new ChatRoomFragment();
            fragment.setType(i);
            mFragments.add(fragment);
        }
        mFragments.add(new ChatRoomGroupFragment());
        contentViewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        });
        contentViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        TIMHelper.getInstance().joinGroup();
    }

    @OnClick({R.id.chat_new_list, R.id.chat_friend, R.id.chat_group})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.chat_new_list:
                contentViewpager.setCurrentItem(0);
                break;
            case R.id.chat_friend:
                contentViewpager.setCurrentItem(1);
                break;
            case R.id.chat_group:
                contentViewpager.setCurrentItem(2);
                break;
        }
    }

    private void selectTab(int position) {
        chatNewList.setSelected(false);
        chatFriend.setSelected(false);
        chatGroup.setSelected(false);
        chatNewList.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryDark));
        chatFriend.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryDark));
        chatGroup.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryDark));
        switch (position) {
            case 0:
                chatNewList.setSelected(true);
                chatNewList.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                break;
            case 1:
                chatFriend.setSelected(true);
                chatFriend.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                break;
            case 2:
                chatGroup.setSelected(true);
                chatGroup.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                break;
        }
    }
}
