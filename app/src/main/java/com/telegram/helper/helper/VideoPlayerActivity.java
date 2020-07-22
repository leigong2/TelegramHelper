package com.telegram.helper.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.telegram.helper.R;
import com.telegram.helper.util.MemoryCache;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {
    private List<WatchVideoActivity.MediaData> mediaData;
    private ViewPager2 recyclerView;
    private int position;

    public static void start(Context context, List<WatchVideoActivity.MediaData> mediaData, int position) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        MemoryCache.getInstance().put("mediaData", mediaData);
        MemoryCache.getInstance().put("position", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.layout_video_list);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mediaData = MemoryCache.getInstance().remove("mediaData");
        position = MemoryCache.getInstance().remove("position");
        if (mediaData == null) {
            mediaData = new ArrayList<>();
        }
        if (position >= mediaData.size()) {
            position = 0;
        }
        recyclerView.setAdapter(new FragmentStateAdapter(this) {

            @NonNull
            @Override
            public VideoPlayFragment createFragment(int position) {
                mCurrentFragment = VideoPlayFragment.getInstance(mediaData.get(position));
                return mCurrentFragment;
            }

            @Override
            public int getItemCount() {
                return mediaData.size();
            }
        });
        recyclerView.setCurrentItem(position, false);
    }

    private VideoPlayFragment mCurrentFragment;
}
