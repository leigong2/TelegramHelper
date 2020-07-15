package com.telegram.helper.login;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.blankj.utilcode.util.SPUtils;
import com.telegram.helper.MainActivity;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.base.BaseApplication;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.load_lay)
    View loading;

    public static void start() {
        Activity topActivity = BaseApplication.getInstance().getTopActivity();
        Intent intent = new Intent(topActivity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        topActivity.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        super.initView();
        if (!TIMHelper.getInstance().enableConnect()) {
            loading.setVisibility(View.VISIBLE);
            TIMHelper.getInstance().registerConnect(response -> {
                loading.setVisibility(View.GONE);
                autoLogin();
            });
        } else {
            autoLogin();
        }
    }

    private void autoLogin() {
        if (TIMHelper.getInstance().enableConnect()) {
            String userName = SPUtils.getInstance().getString("userName");
            String passWord = SPUtils.getInstance().getString("passWord");
            if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(passWord)) {
                startLogin(userName, passWord);
            }
        }
    }

    @OnClick(R.id.login)
    public void onViewClicked() {
        if (TextUtils.isEmpty(username.getText().toString().trim()) || TextUtils.isEmpty(password.getText().toString().trim())) {
            return;
        }
        startLogin(username.getText().toString().trim(), password.getText().toString().trim());
    }

    private void startLogin(String userName, String passWord) {
        loading.setVisibility(View.VISIBLE);
        LoginHelper.getInstance().login(userName, passWord, new LoginHelper.CallBack() {
            @Override
            public void onCallBack(boolean response) {
                loading.setVisibility(View.GONE);
                if (response) {
                    MainActivity.start(getActivity());
                    finish();
                }
            }
        });
    }
}
