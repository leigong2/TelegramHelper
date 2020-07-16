package com.telegram.helper.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseAdapter;
import com.telegram.helper.base.BaseFragment;
import com.telegram.helper.chat.group.GroupMemberListActivity;
import com.telegram.helper.event.GroupChangeEvent;
import com.telegram.helper.login.TIMHelper;
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatRoomGroupFragment extends BaseFragment {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;

    @Override
    protected void initView(View view) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new BaseAdapter<TIMGroupBaseInfo>() {
            @Override
            protected BaseViewHolder<TIMGroupBaseInfo> getViewHolder(ViewGroup parent) {
                return new ChatRoomHolder(parent);
            }
        });
        BaseAdapter<TIMGroupBaseInfo> adapter = (BaseAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.getDatas().addAll(TIMHelper.getInstance().mGroupLists);
            adapter.notifyDataSetChanged();
        }
        TIMHelper.getInstance().getGroupList();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupEvent(GroupChangeEvent event) {
        BaseAdapter<TIMGroupBaseInfo> adapter = (BaseAdapter<TIMGroupBaseInfo>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.getDatas().clear();
            List<TIMGroupBaseInfo> conversations = TIMHelper.getInstance().mGroupLists;
            for (TIMGroupBaseInfo conversation : conversations) {
                adapter.getDatas().add(conversation);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public int getLayoutId() {
        return R.layout.base_refresh_layout;
    }

    public static class ChatRoomHolder extends BaseAdapter.BaseViewHolder<TIMGroupBaseInfo> {
        @BindView(R.id.username)
        TextView username;
        private TIMGroupBaseInfo mCurData;

        public ChatRoomHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, null));
            ButterKnife.bind(this, itemView);
        }

        public int getLayoutId() {
            return R.layout.item_chat_room;
        }

        @Override
        public void onValue(TIMGroupBaseInfo info, int position) {
            mCurData = info;
            username.setText("群：" + info.getGroupName());
        }

        @OnClick(R.id.username)
        public void onViewClicked() {
            if (mCurData != null) {
                GroupMemberListActivity.start(itemView.getContext(), mCurData);
            }
        }
    }
}
