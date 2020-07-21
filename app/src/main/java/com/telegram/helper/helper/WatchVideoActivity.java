package com.telegram.helper.helper;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.telegram.helper.R;
import com.telegram.helper.util.SimpleObserver;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.provider.MediaStore.Video.Thumbnails.MICRO_KIND;

public class WatchVideoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private String path;
    private List<MediaData> mDatas;
    Map<Integer, Bitmap> map = new HashMap<>();

    public static void start(Context context, String path) {
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
        Intent intent = new Intent(context, WatchVideoActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_video);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(WatchVideoActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(WatchVideoActivity.this).inflate(R.layout.item_video, viewGroup, false));
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                ImageView videoThumb = viewHolder.itemView.findViewById(R.id.thumb);
                videoThumb.setImageResource(R.mipmap.place_loading);
                videoThumb.setTag(i);
                if (map.get(i) == null) {
                    Observable.just(mDatas.get(i).url).map(new Function<String, Bitmap>() {
                        @Override
                        public Bitmap apply(String url) throws Exception {
                            return ThumbnailUtils.createVideoThumbnail(url, MICRO_KIND);
                        }
                    }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SimpleObserver<Bitmap, ImageView>(videoThumb, false, i) {
                                @Override
                                public void onNext(Bitmap bitmap, ImageView view) {
                                    if (bitmap != null) {
                                        int tag = (int) view.getTag();
                                        if (tag == position) {
                                            view.setImageBitmap(bitmap);
                                            map.put(tag, bitmap);
                                        }
                                    }
                                }
                            });
                } else {
                    videoThumb.setImageBitmap(map.get(i));
                }
                TextView textView = viewHolder.itemView.findViewById(R.id.video_path);
                if (!TextUtils.isEmpty(mDatas.get(i).addTime) && !TextUtils.isEmpty(mDatas.get(i).type)) {
                    textView.setText(String.format("%s : %s : %s", mDatas.get(i).type, mDatas.get(i).name, mDatas.get(i).addTime));
                } else if (!TextUtils.isEmpty(mDatas.get(i).type)) {
                    textView.setText(String.format("%s : %s", mDatas.get(i).type, mDatas.get(i).name));
                } else if (!TextUtils.isEmpty(mDatas.get(i).addTime)) {
                    textView.setText(String.format("%s : %s", mDatas.get(i).name, mDatas.get(i).addTime));
                } else {
                    textView.setText(String.format("%s", mDatas.get(i).name));
                }
                viewHolder.itemView.setOnClickListener(new OnClickListener(i) {
                    @Override
                    public void onClick(View view, int position) {
                        VideoPlayerActivity.start(WatchVideoActivity.this, mDatas.get(position).url);
                    }
                });
                viewHolder.itemView.setTag(i);
                viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int position = (int) view.getTag();
                        String url = mDatas.get(position).url;
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            ClipData mClipData = ClipData.newPlainText("Label", url);
                            cm.setPrimaryClip(mClipData);
                            ToastUtils.showShort("链接已复制");
                        }
                        return false;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        mDatas = new ArrayList<>();
        path = getIntent().getStringExtra("path");
        loadData();
    }

    private void loadData() {
        Observable.just(path).map(new Function<String, List<MediaData>>() {
            @Override
            public List<MediaData> apply(String path) throws Exception {
                List<MediaData> datas = new ArrayList<>();
                File dir = new File(path);
                if (!dir.exists()) {
                    return datas;
                }
                File[] files = dir.listFiles();
                if (files == null) {
                    return datas;
                }
                appendAllData(files, datas);
                return datas;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MediaData>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<MediaData> o) {
                        mDatas.addAll(o);
                        recyclerView.getAdapter().notifyDataSetChanged();
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishRefresh();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void appendAllData(File[] files, List<MediaData> datas) {
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getPath().endsWith(".mp4")
                    || file.getPath().endsWith(".mov")
                    || file.getPath().endsWith(".MP4")
                    || file.getPath().endsWith(".MOV") ) {
                DecimalFormat decimalFormat = new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String string = decimalFormat.format(file.length() / 1024f / 1024f);//返回字符串
                MediaData data = new MediaData(string + "M", file.getPath(), file.getPath(),  new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(file.lastModified())));
                datas.add(data);
            }
        }
    }

    public static class MediaData {
        public String name;
        public String url;
        public String type;
        public String addTime;

        public MediaData(String name, String url, String type, String addTime) {
            this.name = name;
            this.url = url;
            this.type = type;
            this.addTime = addTime;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public abstract static class OnClickListener implements View.OnClickListener {
        int position;

        public OnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            onClick(view, position);
        }

        protected abstract void onClick(View view, int position);
    }
}
