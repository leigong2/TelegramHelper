package com.telegram.helper.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.telegram.helper.R;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.login.LoginHelper;
import com.telegram.helper.login.TIMHelper;
import com.tencent.imsdk.TIMConversationType;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;

public class ChatActivity extends BaseActivity {

    @BindView(R.id.sender)
    EditText sender;
    private ChatFragment chatFragment;

    public static void start(Context context, String userId, String groupId) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_with_fragment;
    }

    @Override
    protected void initView() {
        super.initView();
        String userId = getIntent().getStringExtra("userId");
        String groupId = getIntent().getStringExtra("groupId");
        if (TextUtils.isEmpty(groupId)) {
            chatFragment = ChatFragment.getInstance(R.id.fragment_container, getSupportFragmentManager(), userId);
        } else {
        }
        sender.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == 0 || actionId == IME_ACTION_SEND) && event != null) {
                    TIMHelper.getInstance().sendMsg(TIMConversationType.C2C, userId, sender.getText().toString(), new LoginHelper.CallBack() {
                        @Override
                        public void onCallBack(boolean response) {
                            if (response) {
                                refreshData();
                            }
                        }
                    });
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshData() {
        if (chatFragment != null) {
            chatFragment.loadData();
        }
    }
}
