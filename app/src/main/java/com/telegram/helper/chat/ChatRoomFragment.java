package com.telegram.helper.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseAdapter;
import com.telegram.helper.base.BaseFragment;
import com.telegram.helper.event.ConversationChangeEvent;
import com.telegram.helper.login.TIMHelper;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.v2.V2TIMConversation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

public class ChatRoomFragment extends BaseFragment {

    public static final int CHAT_NEW_LIST = 0;
    public static final int CHAT_FRIEND = 1;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;
    private int mType;

    @Override
    protected void initView(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new BaseAdapter<ChatRoomHolder<V2TIMConversation>>() {
            @Override
            protected BaseViewHolder<ChatRoomHolder<V2TIMConversation>> getViewHolder(ViewGroup parent) {
                return new ChatRoomHolder<>(parent);
            }
        });
        switch (mType) {
            case CHAT_NEW_LIST:
                getNewList();
                break;
            case CHAT_FRIEND:
                getFriendList();
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConversationEvent(ConversationChangeEvent event) {
        BaseAdapter<V2TIMConversation> adapter = (BaseAdapter<V2TIMConversation>) recyclerView.getAdapter();
        if (mType == CHAT_NEW_LIST) {
            if (adapter != null) {
                adapter.getDatas().clear();
                adapter.getDatas().addAll(TIMHelper.getInstance().conversations);
                adapter.notifyDataSetChanged();
            }
        } else if (mType == CHAT_FRIEND) {
            if (adapter != null) {
                adapter.getDatas().clear();
                List<V2TIMConversation> conversations = TIMHelper.getInstance().conversations;
                for (V2TIMConversation conversation : conversations) {
                    if (conversation.getType() != TIMConversationType.C2C.value()) {
                        continue;
                    }
                    adapter.getDatas().add(conversation);
                }
                adapter.notifyDataSetChanged();
            }
        } else {
            if (adapter != null) {
                adapter.getDatas().clear();
                List<V2TIMConversation> conversations = TIMHelper.getInstance().conversations;
                for (V2TIMConversation conversation : conversations) {
                    if (conversation.getType() != TIMConversationType.C2C.value()) {
                        continue;
                    }
                    adapter.getDatas().add(conversation);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void getNewList() {
        TIMHelper.getInstance().getConversationList();
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(false);
    }

    private void getFriendList() {
        TIMHelper.getInstance().getConversationList();
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(false);
    }

    @Override
    public int getLayoutId() {
        return R.layout.base_refresh_layout;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public static class ChatRoomHolder<T> extends BaseAdapter.BaseViewHolder<T> {
        public ChatRoomHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, null));
        }

        @Override
        public void onValue(T t, int position) {

        }
    }
}
