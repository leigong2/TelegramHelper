package com.telegram.helper.login;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseApplication;
import com.telegram.helper.user.UserBean;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMManager;

public class LoginHelper {
    private static LoginHelper sLoginHelper;
    private UserBean userBean;

    private LoginHelper() {
        userBean = new UserBean();
    }

    public static LoginHelper getInstance() {
        if (sLoginHelper == null) {
            synchronized (LoginHelper.class) {
                if (sLoginHelper == null) {
                    sLoginHelper = new LoginHelper();
                }
            }
        }
        return sLoginHelper;
    }

    public void login(String userName, String passWord, CallBack callBack) {
        SPUtils.getInstance().put("userName", userName);
        userBean.userId = userName;
        String localPassword = SPUtils.getInstance().getString("passWord");
        if (!TextUtils.isEmpty(localPassword) && localPassword.equals(passWord)) {
            TIMHelper.getInstance().login(userName, callBack);
        } else if (TextUtils.isEmpty(localPassword)) {
            SPUtils.getInstance().put("passWord", passWord);
            TIMHelper.getInstance().login(userName, callBack);
        } else {
            ToastUtils.showShort(R.string.error_password);
            callBack.onCallBack(false);
        }
    }

    public void logout() {
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                ToastUtils.showShort(BaseApplication.getInstance().getString(R.string.exit_error));
            }

            @Override
            public void onSuccess() {
                SPUtils.getInstance().remove("userName");
                SPUtils.getInstance().remove("passWord");
                LoginActivity.start();
            }
        });
    }

    public UserBean getUser() {
        return userBean;
    }

    public String getUserId() {
        return userBean.userId;
    }

    public interface CallBack {
        void onCallBack(boolean response);
    }
}
