package com.telegram.helper;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.reflect.TypeToken;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.bean.HrefData;
import com.telegram.helper.chat.ChatRoomActivity;
import com.telegram.helper.helper.ListenMusicActivity;
import com.telegram.helper.helper.PictureActivity;
import com.telegram.helper.helper.WatchVideoActivity;
import com.telegram.helper.login.LoginHelper;
import com.telegram.helper.util.ClearUtils;
import com.telegram.helper.util.CustomItemTouchHelperCallBack;
import com.telegram.helper.util.DisableDoubleClickUtils;
import com.telegram.helper.util.GsonGetter;
import com.telegram.helper.views.ClearHolder;

import java.io.File;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static java.io.File.separator;

public class MainActivity extends BaseActivity {

    private String imageDir;
    private String videoDir;
    private String documentDir;
    private String audioDir;
    private List<String> mMainTags = new ArrayList<>();
    private ClearHolder clearHolder;
    private AlertDialog warnDialog;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void initView() {
        super.initView();
        setContentView(R.layout.activity_main);
        imageDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Telegram" + separator + "Telegram Images";
        videoDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Telegram" + separator + "Telegram Video";
        documentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Telegram" + separator + "Telegram Documents";
        audioDir = Environment.getExternalStorageDirectory().getAbsolutePath() + separator + "Telegram" + separator + "Telegram Audio";
        initMainTags();
        RecyclerView recyclerView = findViewById(R.id.main_tags);
        recyclerView.setLayoutManager(ChipsLayoutManager.newBuilder(this)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
                .setOrientation(ChipsLayoutManager.HORIZONTAL).build());
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter
                = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View itemView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_main_tag, viewGroup, false);
                itemView.setOnClickListener(v -> onTagClick(v));
                return new RecyclerView.ViewHolder(itemView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                TextView textTag = viewHolder.itemView.findViewById(R.id.tag);
                textTag.setText(mMainTags.get(position));
                viewHolder.itemView.setTag(textTag.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Drawable drawable = getResources().getDrawable(R.drawable.bg_main_tag);
                    String[] randColor = getRandColor().split("\\*");
                    drawable.setTint(Color.parseColor(randColor[0]));
                    textTag.setBackground(drawable);
                    BigInteger bigint = new BigInteger(randColor[1], 16);
                    int textColor = bigint.intValue();
                    textTag.setTextColor(Color.BLACK);
                }
            }

            @Override
            public int getItemCount() {
                return mMainTags.size();
            }
        };
        CustomItemTouchHelperCallBack callback = new CustomItemTouchHelperCallBack();
        callback.setOnItemMove(new CustomItemTouchHelperCallBack.OnItemMove() {
            @Override
            public boolean onMove(int fromPosition, int toPosition) {
                //1、交换数据
                sort(mMainTags, fromPosition, toPosition);
                //2、刷新
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.setBgResource(R.drawable.bg_toast);
        ClearUtils.getInstance().getAppProcessName(this);
    }

    /**
     * 获取十六进制的颜色代码.例如  "#5A6677"
     * 分别取R、G、B的随机值，然后加起来即可
     *
     * @return String
     */
    public static String getRandColor() {
        String R, G, B, R1, G1, B1;
        Random random = new Random();
        int r = random.nextInt(256);
        R1 = Integer.toHexString(255 - r).toUpperCase();
        R = Integer.toHexString(r).toUpperCase();
        int g = random.nextInt(256);
        G1 = Integer.toHexString(255 - g).toUpperCase();
        G = Integer.toHexString(g).toUpperCase();
        int b = random.nextInt(256);
        B1 = Integer.toHexString(255 - b).toUpperCase();
        B = Integer.toHexString(b).toUpperCase();

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        return "#80" + R + G + B + "*" + "FF" + R1 + G1 + B1;
    }

    /*zune: 将fromPosition，转移到toPosition, 缺位的顺次补上**/
    private void sort(List<String> mainTags, int fromPosition, int toPosition) {
        List<String> tempSrc = new ArrayList<>(mainTags);
        if (fromPosition < toPosition) {
            for (int i = 0; i < mainTags.size(); i++) {
                if (i == fromPosition) {
                    for (int j = 0; j < toPosition - fromPosition; j++) {
                        mainTags.set(i + j, tempSrc.get(i + j + 1));
                    }
                    mainTags.set(toPosition, tempSrc.get(fromPosition));
                    break;
                }
            }
        }
        if (fromPosition > toPosition) {
            for (int i = 0; i < mainTags.size(); i++) {
                if (i == toPosition) {
                    mainTags.set(toPosition, tempSrc.get(fromPosition));
                    for (int j = 0; j < fromPosition - toPosition; j++) {
                        mainTags.set(i + j + 1, tempSrc.get(i + j));
                    }
                    break;
                }
            }
        }
    }

    private void initMainTags() {
        String json = SPUtils.getInstance().getString("mMainTags");
        List<String> temp = GsonGetter.getInstance().getGson().fromJson(json, new TypeToken<List<String>>() {
        }.getType());
        if (temp != null && !temp.isEmpty()) {
            mMainTags.addAll(temp);
            return;
        }
        this.mMainTags.add("删除冗余文件夹");
        this.mMainTags.add("视频");
        this.mMainTags.add("歌曲");
        this.mMainTags.add("图片");
        this.mMainTags.add("进入聊天室");
        this.mMainTags.add("退出聊天室");
    }


    private void onTagClick(View v) {
        String text = (String) v.getTag();
        switch (text) {
            case "删除冗余文件夹": //删除冗余文件夹");
                deleteDir();
                break;
            case "视频": //视频");
                WatchVideoActivity.start(MainActivity.this, videoDir);
                break;
            case "歌曲": //歌曲");
                ListenMusicActivity.start(MainActivity.this, audioDir);
                break;
            case "图片": //图片");
                lookPic();
                break;
            case "进入聊天室": //进入聊天室");
                ChatRoomActivity.start(this);
                break;
            case "退出聊天室": //退出聊天室");
                if (!DisableDoubleClickUtils.canClick(v)) {
                    return;
                }
                LoginHelper.getInstance().logout();
                break;
        }
    }

    private void lookPic() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        List<HrefData> datas = new ArrayList<>();
        String path = new File(imageDir).getPath();
        HrefData data = new HrefData("", "", path);
        String documentPath = new File(documentDir).getPath();
        HrefData documentData = new HrefData("", "", documentPath);
        datas.add(data);
        datas.add(documentData);
        PictureActivity.start(this, datas);
    }

    /**
     * zune: 删除文件夹
     **/
    private void deleteDir() {
        if (Build.VERSION.SDK_INT >= 23) {
            int writePermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        showWarnDialog();
    }

    private void showWarnDialog() {
        warnDialog = new AlertDialog.Builder(this)
                .setTitle("删除后，仅保留手机文件夹{Android、Bing、DCIM、Documents、Download、Picture、Pictures、Music、Pictures、QQBrowser、Telegram、tencent、Video}\n是否继续？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        warnDialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        warnDialog.dismiss();
                        if (clearHolder == null) {
                            clearHolder = new ClearHolder(findViewById(R.id.clear_root));
                        }
                        clearHolder.startLoad();
                        ClearUtils.getInstance().delete(Environment.getExternalStorageDirectory().getPath(), new Observer<List<String>>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(List<String> integer) {
                                ToastUtils.showShort("删除完成");
                                clearHolder.stopLoad();
                            }

                            @Override
                            public void onError(Throwable e) {
                                ToastUtils.showShort("刪除失败");
                            }

                            @Override
                            public void onComplete() {
                            }
                        });
                    }
                })
                .create();
        try {
            Class<?> mAlert = warnDialog.getClass();
            Field field = mAlert.getDeclaredField("mAlert");
            field.setAccessible(true);
            Field mTitleView = field.get(warnDialog).getClass().getDeclaredField("mTitleView");
            mTitleView.setAccessible(true);
            Object AlertController = field.get(warnDialog);
            mTitleView.set(AlertController, new TextView(this));
            warnDialog.show();
            Object obj = mTitleView.get(AlertController);
            TextView textView = (TextView) obj;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            textView.setSingleLine(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }
}