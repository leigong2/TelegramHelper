package com.telegram.helper;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.chat.ChatRoomActivity;
import com.telegram.helper.login.LoginHelper;
import com.telegram.helper.util.DisableDoubleClickUtils;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @OnClick({R.id.enter, R.id.exit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.enter:
                ChatRoomActivity.start(this);
                break;
            case R.id.exit:
                if (!DisableDoubleClickUtils.canClick(view)) {
                    return;
                }
                LoginHelper.getInstance().logout();
                break;
        }
    }
}