package com.telegram.helper.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.login.LoginHelper;
import com.telegram.helper.login.TIMHelper;
import com.telegram.helper.util.DisableDoubleClickUtils;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManagerImpl;
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener;
import com.tencent.imsdk.v2.V2TIMUserInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEND;
import static com.tencent.imsdk.TIMConversationType.C2C;
import static com.tencent.imsdk.TIMConversationType.Group;

public class ChatActivity extends BaseActivity implements KeyboardUtils.OnSoftInputChangedListener, TIMHelper.OnReceiveMsgListener  {

    @BindView(R.id.sender)
    EditText sender;
    private ChatFragment chatFragment;
    private String groupId;
    private String userId;

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
        userId = getIntent().getStringExtra("userId");
        groupId = getIntent().getStringExtra("groupId");
        if (TextUtils.isEmpty(groupId)) {
            chatFragment = ChatFragment.getInstance(R.id.fragment_container, getSupportFragmentManager(), userId, C2C);
            setTitle(userId);
        } else {
            chatFragment = ChatFragment.getInstance(R.id.fragment_container, getSupportFragmentManager(), groupId, Group);
            setTitle(groupId);
        }
        findViewById(R.id.sender_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = sender.getText().toString();
                sender.setText("");
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                if (!TextUtils.isEmpty(userId)) {
                    TIMHelper.getInstance().sendMsg(C2C, userId, content, new LoginHelper.CallBack() {
                        @Override
                        public void onCallBack(boolean response) {
                            if (response) {
                                notifyItemChange(content);
                            }
                        }
                    });
                } else {
                    TIMHelper.getInstance().sendGroupMsg(Group, groupId, content, new LoginHelper.CallBack() {
                        @Override
                        public void onCallBack(boolean response) {
                            if (response) {
                                notifyItemChange(content);
                            }
                        }
                    });
                }
            }
        });
        KeyboardUtils.registerSoftInputChangedListener(this, this);
        TIMHelper.getInstance().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtils.unregisterSoftInputChangedListener(this);
        TIMHelper.getInstance().unRegister(this);
    }

    private void notifyItemChange(String content) {
        if (chatFragment != null) {
            chatFragment.notifyItemChange(content);
        }
    }

    private void refreshData() {
        if (chatFragment != null) {
            chatFragment.loadData();
        }
    }

    @Override
    public void onSoftInputChanged(int height) {
        chatFragment.showKeyboard(height);
    }

    @Override
    public void onRecvC2CTextMessage(String msgID, V2TIMUserInfo sender, String text) {
        if (chatFragment != null) {
            if (!TextUtils.isEmpty(userId) && userId.equals(sender.getUserID())) {
                chatFragment.notifyItemChange(sender.getUserID(), text);
            }
        }
    }

    @Override
    public void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text) {
        if (chatFragment != null) {
            if (!TextUtils.isEmpty(groupId) && groupId.equals(groupID)) {
                chatFragment.notifyItemChange(sender.getUserID(), text);
            }
        }
    }
}
