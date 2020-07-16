package com.telegram.helper.chat.group;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseActivity;
import com.telegram.helper.base.BaseAdapter;
import com.telegram.helper.chat.ChatActivity;
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_OWNER;

public class GroupMemberListActivity extends BaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SmartRefreshLayout refreshLayout;
    private static TIMGroupBaseInfo sData;
    private long nextSeq;

    public static void start(Context context, TIMGroupBaseInfo data) {
        Intent intent = new Intent(context, GroupMemberListActivity.class);
        sData = data;
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.base_refresh_layout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sData = null;
    }

    @Override
    protected void initView() {
        super.initView();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new BaseAdapter<V2TIMGroupMemberFullInfo>() {
            @NonNull
            @Override
            public BaseViewHolder<V2TIMGroupMemberFullInfo> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new GroupMemberHolder(parent);
            }
        });
        if (sData == null) {
            return;
        }
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                loadData();
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                nextSeq = 0;
                loadData();
            }
        });
        loadData();
    }

    private void loadData() {
        V2TIMValueCallback<V2TIMGroupMemberInfoResult> callback = new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
            @Override
            public void onError(int i, String s) {
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
            }

            @Override
            public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                refreshLayout.finishRefresh();
                refreshLayout.finishLoadMore();
                List<V2TIMGroupMemberFullInfo> memberInfoList = v2TIMGroupMemberInfoResult.getMemberInfoList();
                BaseAdapter<V2TIMGroupMemberFullInfo> adapter = (BaseAdapter) recyclerView.getAdapter();
                adapter.getDatas().addAll(memberInfoList);
                adapter.notifyDataSetChanged();
                nextSeq = v2TIMGroupMemberInfoResult.getNextSeq();
            }
        };
        V2TIMManager.getGroupManager().getGroupMemberList(sData.getGroupId(), V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL
                , nextSeq, callback);
    }

    public static class GroupMemberHolder extends BaseAdapter.BaseViewHolder<V2TIMGroupMemberFullInfo> {
        @BindView(R.id.username)
        TextView username;
        private V2TIMGroupMemberFullInfo mUserInfo;

        public GroupMemberHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, null));
            ButterKnife.bind(this, itemView);
        }

        public int getLayoutId() {
            return R.layout.item_group_member;
        }

        @Override
        public void onValue(V2TIMGroupMemberFullInfo v2TIMGroupMemberFullInfo, int position) {
            mUserInfo = v2TIMGroupMemberFullInfo;
            username.setText(String.format("%s:%s, %s:%s", (v2TIMGroupMemberFullInfo.getRole() == V2TIM_GROUP_MEMBER_ROLE_OWNER ? "群主" : "群成员")
                    , v2TIMGroupMemberFullInfo.getUserID(), "进群时间", getTime(v2TIMGroupMemberFullInfo.getJoinTime())));
        }

        private String getTime(long joinTime) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date(joinTime * 1000);
            return simpleDateFormat.format(date);
        }

        @OnClick(R.id.username)
        public void onViewClicked() {
            ChatActivity.start(itemView.getContext(), mUserInfo.getUserID(), null);
        }
    }
}
