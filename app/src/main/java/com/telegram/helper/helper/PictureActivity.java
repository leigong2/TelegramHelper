package com.telegram.helper.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.util.DensityUtil;
import com.telegram.helper.R;
import com.telegram.helper.bean.HrefData;
import com.telegram.helper.util.GlideUtils;
import com.telegram.helper.util.GsonGetter;
import com.telegram.helper.util.MemoryCache;
import com.telegram.helper.util.SimpleObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import pl.droidsonroids.gif.GifImageView;

public class PictureActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private List<HrefData> mDatas = new ArrayList<>();
    private List<HrefData> paths;

    public static void start(Context context, List<HrefData> path) {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        Intent intent = new Intent(context, PictureActivity.class);
        MemoryCache.getInstance().put("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 5, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                // 列左、右间隙
                outRect.left = DensityUtil.dp2px(2);
                outRect.right = outRect.left;
            }
        });
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                WatchVideoActivity.ViewHolder viewHolder = new WatchVideoActivity.ViewHolder(LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.item_image_layout, viewGroup, false));
                viewHolder.itemView.setOnClickListener(new WatchVideoActivity.OnClickListener(i) {
                    @Override
                    public void onClick(View view, int position) {
                        if (mDatas == null || mDatas.isEmpty()) {
                            ToastUtils.showShort("图片为空文件夹");
                            return;
                        }
                        PictureGallayActivity.start(PictureActivity.this, paths, (Integer) view.getTag());
                    }
                });
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = (int) view.getTag();
                        HrefData url = mDatas.get(position);
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData mClipData = ClipData.newPlainText("Label", url.text);
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("链接已复制");
                        }
                        return false;
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                viewHolder.itemView.setTag(i);
                if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    ImageView imageView = viewHolder.itemView.findViewById(R.id.image_view);
                    GifImageView gifView = viewHolder.itemView.findViewById(R.id.gif_view);
                    if (TextUtils.isEmpty(mDatas.get(i).href)) {
                        if (mDatas.get(i).text.endsWith("gif")) {
                            gifView.setVisibility(View.VISIBLE);
                            imageView.setVisibility(View.GONE);
                            GlideUtils.getInstance().setFileToView(new File(mDatas.get(i).text), gifView, true);
                        } else {
                            gifView.setVisibility(View.GONE);
                            imageView.setVisibility(View.VISIBLE);
//                            GlideUtils.getInstance().loadUrl(mDatas.get(i).text, imageView, true, true);
                            Glide.with(PictureActivity.this)
                                    .load(mDatas.get(i).text)
                                    .apply(new RequestOptions().override(100, 100).fitCenter().placeholder(R.mipmap.place_loading))
                                    .into(imageView);
                        }
                    }
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        paths = MemoryCache.getInstance().remove("path");
        loadData();
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData();
            }
        });
        refreshLayout.setEnableLoadMore(false);
    }

    private void loadData() {
        if (recyclerView != null && mDatas.size() > 0) {
            recyclerView.scrollToPosition(0);
        }
        mDatas.clear();
        Observable.just(1).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                for (HrefData hrefData : paths) {
                    if (TextUtils.isEmpty(hrefData.href)) {
                        File dir = new File(hrefData.text);
                        addAllFile(dir);
                        if (!mDatas.isEmpty()) {
                            Collections.sort(mDatas, new Comparator<HrefData>() {
                                @Override
                                public int compare(HrefData o1, HrefData o2) {
                                    return getPosition(o1.text) - getPosition(o2.text);
                                }
                            });
                        }
                    }
                }
                return 1;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Integer, Integer>(1, false) {
                    @Override
                    public void onNext(Integer integer, Integer integer2) {
                        if (recyclerView.getAdapter() != null) {
                            recyclerView.getAdapter().notifyDataSetChanged();
                        }
                        if (refreshLayout != null) {
                            refreshLayout.finishLoadMore();
                            refreshLayout.finishRefresh();
                        }
                    }
                });
    }

    private static Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");
    private int getPosition(String o1) {
        String[] split = o1.split("\\.");
        for (int i = o1.length() - (split[split.length - 1].length() + 2); i >= 0; i--) {
            if (pattern.matcher(String.valueOf(o1.charAt(i))).matches()) {
                continue;
            }
            String substring = o1.substring(i + 1, o1.length() - split[split.length - 1].length() - 1);
            try {
                return Integer.parseInt(substring);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    private void addAllFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addAllFile(file);
            } else {
                if (file.getPath().endsWith("png")
                        || file.getPath().endsWith("jpg")
                        || file.getPath().endsWith("gif")
                        || file.getPath().endsWith("webp")) {
                    HrefData hrefData = new HrefData("", "", file.getPath());
                    mDatas.add(hrefData);
                }
            }
        }
    }

    private Handler mHandler = new Handler();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
        SPUtils.getInstance().put("PictureActivityData", GsonGetter.getInstance().getGson().toJson(mDatas));
    }
}
