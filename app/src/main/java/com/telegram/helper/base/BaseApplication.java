package com.telegram.helper.base;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.telegram.helper.R;
import com.telegram.helper.login.TIMHelper;

import java.util.Stack;

public class BaseApplication extends Application {
    private static BaseApplication sApplication;
    private static Stack<Activity> mActivityStack = new Stack<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        TIMHelper.getInstance().init(this);
        Utils.init(this);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.setBgResource(R.drawable.bg_toast);
    }

    public static BaseApplication getInstance() {
        return sApplication;
    }

    public Activity getTopActivity() {
        if (mActivityStack == null) {
            return null;
        }
        if (mActivityStack.empty()) {
            return null;
        }
        return mActivityStack.lastElement();
    }

    public void addTopActivity(BaseActivity baseActivity) {
        mActivityStack.add(baseActivity);
    }

    public void removeTopActivity(BaseActivity baseActivity) {
        mActivityStack.remove(baseActivity);
    }

}
