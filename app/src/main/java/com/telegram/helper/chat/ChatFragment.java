package com.telegram.helper.chat;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseAdapter;
import com.telegram.helper.base.BaseFragment;
import com.telegram.helper.login.LoginHelper;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.conversation.ConversationManager;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatFragment extends BaseFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;
    private BaseAdapter<TIMMessage> mAdapter;
    private final int LEFT_CHAT = 101;
    private final int RIGHT_CHAT = 102;
    private String userId;
    private TIMConversation mTIMConversation;
    public static final int DEFAULT_MESSAGE_PAGE_SIZE = 50;

    public static ChatFragment getInstance(@IdRes int resId, FragmentManager fragmentManager, String userId) {
        ChatFragment fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(resId, fragment, fragment.getClass().getSimpleName());
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
        return fragment;
    }

    @Override
    protected void initView(View view) {
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mAdapter = new BaseAdapter<TIMMessage>() {
            @NonNull
            @Override
            public BaseViewHolder<TIMMessage> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                switch (viewType) {
                    case LEFT_CHAT:
                        return new LeftChatHolder(parent);
                    case RIGHT_CHAT:
                        return new RightChatHolder(parent);
                }
                return new LeftChatHolder(parent);
            }

            @Override
            public int getItemViewType(int position) {
                TIMMessage timMessage = mAdapter.getDatas().get(position);
                if (LoginHelper.getInstance().getUserId().equals(timMessage.getSender())) {
                    return RIGHT_CHAT;
                } else {
                    return LEFT_CHAT;
                }
            }
        };
        recyclerView.setAdapter(mAdapter);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                loadData();
            }
        });
        loadData();
    }

    public void loadData() {
        if (mTIMConversation == null) {
            mTIMConversation = ConversationManager.getInstance().getConversation(TIMConversationType.C2C, userId);
        }
        mTIMConversation.getMessage(DEFAULT_MESSAGE_PAGE_SIZE, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
                refreshLayout.finishRefresh();
                mAdapter.getDatas().addAll(timMessages);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.base_refresh_layout;
    }

    public static class LeftChatHolder extends BaseAdapter.BaseViewHolder<TIMMessage> {
        @BindView(R.id.text_msg_left)
        TextView textMsgLeft;

        public LeftChatHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, null));
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onValue(TIMMessage timMessage, int position) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (int i = 0; i < timMessage.getElementCount(); ++i) {
                TIMElem element = timMessage.getElement(i);
                switch (element.getType()) {
                    case Face:
                        TIMFaceElem faceElem = (TIMFaceElem) element;
                        stringBuilder.append(Arrays.toString(faceElem.getData()));
                        break;
                    case Text:
                        TIMTextElem textElem = (TIMTextElem) element;
                        stringBuilder.append(textElem.getText());
                        break;
                }
            }
            textMsgLeft.setText(stringBuilder.toString());
        }
    }

    public static class RightChatHolder extends BaseAdapter.BaseViewHolder<TIMMessage> {
        @BindView(R.id.text_msg_right)
        TextView textMsgRight;

        public RightChatHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, null));
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onValue(TIMMessage timMessage, int position) {
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            for (int i = 0; i < timMessage.getElementCount(); ++i) {
                TIMElem element = timMessage.getElement(i);
                switch (element.getType()) {
                    case Face:
                        TIMFaceElem faceElem = (TIMFaceElem) element;
                        stringBuilder.append(Arrays.toString(faceElem.getData()));
                        break;
                    case Text:
                        TIMTextElem textElem = (TIMTextElem) element;
                        stringBuilder.append(textElem.getText());
                        break;
                }
            }
            textMsgRight.setText(stringBuilder.toString());
        }
    }
}
