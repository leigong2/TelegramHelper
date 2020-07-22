package com.telegram.helper.helper;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.blankj.utilcode.util.ToastUtils;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseFragment;
import com.telegram.helper.util.MemoryCache;

public class VideoPlayFragment extends BaseFragment {

    private VideoView videoView;
    private static final String TOAST_ERROR_PLAY = "Paly error, please check url exist!";

    public static VideoPlayFragment getInstance(WatchVideoActivity.MediaData mediaData) {
        VideoPlayFragment fragment = new VideoPlayFragment();
        MemoryCache.getInstance().put("mediaData", mediaData);
        return fragment;
    }

    @Override
    protected void initView(View view) {
        WatchVideoActivity.MediaData mediaData = MemoryCache.getInstance().remove("mediaData");
        videoView = view.findViewById(R.id.video_view);
        String url = mediaData.url;
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                ToastUtils.showShort(TOAST_ERROR_PLAY);
                return false;
            }
        });
        MediaController controller = new MediaController(getContext());
        controller.setAnchorView(videoView);
        controller.setKeepScreenOn(true);
        videoView.setMediaController(controller);
        videoView.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        videoView.stopPlayback();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_view;
    }
}
