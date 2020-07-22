package com.telegram.helper.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.telegram.helper.R;
import com.telegram.helper.util.DisableDoubleClickUtils;
import com.telegram.helper.views.BottomPop;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.os.Build.VERSION_CODES.N;

public class ListenMusicActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SmartRefreshLayout refreshLayout;
    private String path;
    private List<MediaData> mDatas = new ArrayList<>();
    private int mCurPlayPosition;
    private String[] mLongExa = new String[] {"复制", "删除"};


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
        Intent intent = new Intent(context, ListenMusicActivity.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_music);
        recyclerView = findViewById(R.id.recycler_view);
        refreshLayout = findViewById(R.id.refresh_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListenMusicActivity.this));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new WatchVideoActivity.ViewHolder(LayoutInflater.from(ListenMusicActivity.this).inflate(R.layout.item_music, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (viewHolder instanceof WatchVideoActivity.ViewHolder) {
                    ImageView imageView = viewHolder.itemView.findViewById(R.id.iv_pic);
                    loadImage(imageView, mDatas.get(i).pic);
                    TextView textView = viewHolder.itemView.findViewById(R.id.text_view);
                    textView.setText(mDatas.get(i).name);
                    TextView videoView = viewHolder.itemView.findViewById(R.id.video_view);
                    videoView.setText(mDatas.get(i).url);
                    View downloadView = viewHolder.itemView.findViewById(R.id.on_download);
                    downloadView.setVisibility(View.GONE);
                    viewHolder.itemView.setTag(i);
                    viewHolder.itemView.setOnClickListener(new WatchVideoActivity.OnClickListener(i) {
                        @Override
                        public void onClick(View view, int position) {
                            mCurPlayPosition = position;
                            MusicPlayService.start(ListenMusicActivity.this, mDatas.get(mCurPlayPosition), new MusicPlayService.OnPlayListener() {
                                @Override
                                public void onNext(IPlayListener listener) {
                                    if (mDatas.size() > mCurPlayPosition + 1) {
                                        listener.onPlayUrl(mDatas.get(++mCurPlayPosition));
                                    }
                                }

                                @Override
                                public void onPre(IPlayListener listener) {
                                    if (mCurPlayPosition > 0) {
                                        listener.onPlayUrl(mDatas.get(--mCurPlayPosition));
                                    } else {
                                        listener.onPlayUrl(null);
                                    }
                                }
                            });
                        }
                    });
                    viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int i = (int) v.getTag();
                            BottomPop current = BottomPop.getCurrent(ListenMusicActivity.this);
                            for (String s : mLongExa) {
                                current.setItemText(s);
                            }
                            current.show(ListenMusicActivity.this);
                            current.setOnItemClickListener(new BottomPop.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    MediaData mediaData = mDatas.get(i);
                                    switch (position) {
                                        case 0:
                                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                            Uri copyUri;
                                            if (mediaData.url != null && !mediaData.url.startsWith("http")) {
                                                File file = new File(mediaData.url);
                                                if (Build.VERSION.SDK_INT >= N) {
                                                    copyUri = FileProvider.getUriForFile(ListenMusicActivity.this, "androidx.core.content.FileProvider", file);
                                                } else {
                                                    copyUri = Uri.fromFile(file);
                                                }
                                            } else {
                                                copyUri = Uri.parse(mediaData.url);
                                            }
                                            ClipData clipData = ClipData.newUri(getContentResolver(), "URL", copyUri);
                                            clipboardManager.setPrimaryClip(clipData);
                                            ToastUtils.showShort((mediaData.url != null && mediaData.url.startsWith("http") ? "链接" : "路径") + "复制成功！");
                                            break;
                                        case 1:
                                            if (mediaData.url != null && !mediaData.url.startsWith("http")) {
                                                File file = new File(mediaData.url);
                                                if (file.exists()) {
                                                    file.delete();
                                                }
                                            }
                                            if (recyclerView.getAdapter() != null) {
                                                mDatas.remove(i);
                                                recyclerView.getAdapter().notifyItemRemoved(i);
                                            }
                                            ToastUtils.showShort("文件删除成功");
                                            break;
                                    }
                                    current.dismiss();
                                }
                            });
                            return false;
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return mDatas.size();
            }
        });
        path = getIntent().getStringExtra("path");
        preLoad();
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                preLoad();
            }
        });
    }

    private void preLoad() {
        mDatas.clear();
        File dir = new File(path);
        if (!dir.exists() || dir.list() == null || dir.list().length == 0) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            MediaData media = new MediaData(file.getName(), file.getPath(), "");
            media.hideDownload = true;
            mDatas.add(media);
        }
    }

    public static class MediaData {
        public String pic;
        public String name;
        public String url;
        public boolean hideDownload;

        public MediaData(String name, String url, String pic) {
            this.name = name;
            this.url = url;
            this.pic = pic;
        }
    }

    public void loadImage(ImageView imageView, String url) {
        imageView.setImageResource(R.mipmap.default_head);
        Observable.just(url).map(new Function<String, Bitmap>() {
            @Override
            public Bitmap apply(String url) throws Exception {
                URL imageurl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            }
        }).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BitmapObserver<Bitmap>(imageView) {
                    @Override
                    public void onNext(Bitmap bitmap, ImageView imageView) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }

    public abstract class BitmapObserver<T> implements Observer<T> {

        private ImageView imageView;

        public BitmapObserver(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        public void onSubscribe(Disposable d) {

        }

        public abstract void onNext(T t, ImageView imageView);

        @Override
        public void onNext(T o) {
            onNext(o, imageView);
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }
}
