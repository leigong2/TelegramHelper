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
import com.telegram.helper.util.MemoryCache;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.conversation.ConversationManager;
import com.tencent.imsdk.v2.V2TIMUserInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.telegram.helper.login.TIMHelper.from;
import static com.telegram.helper.login.TIMHelper.to;

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
    private TIMConversationType type;

    public static ChatFragment getInstance(@IdRes int resId, FragmentManager fragmentManager, String userId, TIMConversationType type) {
        ChatFragment fragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        MemoryCache.getInstance().put("type", type);
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
            type = MemoryCache.getInstance().remove("type");
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
                if (LoginHelper.getInstance().getUserId().equals(timMessage.getCustomStr())) {
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
        mAdapter.getDatas().clear();
        if (mTIMConversation == null) {
            mTIMConversation = ConversationManager.getInstance().getConversation(type, userId);
        }
        mTIMConversation.getMessage(DEFAULT_MESSAGE_PAGE_SIZE, null, new TIMValueCallBack<List<TIMMessage>>() {
            @Override
            public void onError(int i, String s) {
                refreshLayout.finishRefresh();
            }

            @Override
            public void onSuccess(List<TIMMessage> timMessages) {
                refreshLayout.finishRefresh();
                Collections.reverse(timMessages);
                mAdapter.getDatas().addAll(timMessages);
                mAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(mAdapter.getDatas().size() - 1);
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.base_refresh_layout;
    }

    public void notifyItemChange(String content) {
        mAdapter.getDatas().add(from(content));
        mAdapter.notifyItemChanged(mAdapter.getDatas().size() - 1);
        recyclerView.scrollToPosition(mAdapter.getDatas().size() - 1);
    }

    public void notifyItemChange(String sender, String content) {
        mAdapter.getDatas().add(from(sender, content));
        mAdapter.notifyItemChanged(mAdapter.getDatas().size() - 1);
        recyclerView.scrollToPosition(mAdapter.getDatas().size() - 1);
    }

    public void showKeyboard(int height) {
        if (height > 0) {
            recyclerView.scrollToPosition(mAdapter.getDatas().size() - 1);
        }
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
            textMsgRight.setText(to(timMessage));
        }
    }
}
