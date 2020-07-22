package com.telegram.helper.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.telegram.helper.R;
import com.telegram.helper.base.BaseAdapter;
import com.telegram.helper.base.BaseFragment;
import com.telegram.helper.event.ConversationChangeEvent;
import com.telegram.helper.login.TIMHelper;
import com.telegram.helper.util.GsonGetter;
import com.tencent.imsdk.friendship.TIMFriendResult;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMFriendAddApplication;
import com.tencent.imsdk.v2.V2TIMFriendCheckResult;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMFriendOperationResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.telegram.helper.login.TIMHelper.to;
import static com.tencent.imsdk.v2.V2TIMFriendCheckResult.V2TIM_FRIEND_RELATION_TYPE_IN_MY_FRIEND_LIST;

public class ChatRoomFragment<T> extends BaseFragment {

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
        recyclerView.setAdapter(new BaseAdapter<ChatRoomHolder<T>>() {
            @NonNull
            @Override
            public BaseViewHolder<ChatRoomHolder<T>> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        BaseAdapter<T> adapter = (BaseAdapter<T>) recyclerView.getAdapter();
        if (mType == CHAT_NEW_LIST) {
            if (adapter != null) {
                adapter.getDatas().clear();
                adapter.getDatas().addAll((Collection<? extends T>) TIMHelper.getInstance().conversations);
                adapter.notifyDataSetChanged();
            }
        } else if (mType == CHAT_FRIEND) {
            if (adapter != null) {
                adapter.getDatas().clear();
                adapter.getDatas().addAll((Collection<? extends T>) TIMHelper.getInstance().friendList);
                adapter.notifyDataSetChanged();
            }
        } else {
            if (adapter != null) {
                adapter.getDatas().clear();
                adapter.getDatas().addAll((Collection<? extends T>) TIMHelper.getInstance().friendList);
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
        BaseAdapter<T> adapter = (BaseAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.getDatas().clear();
            adapter.getDatas().addAll((Collection<? extends T>) TIMHelper.getInstance().conversations);
            adapter.notifyDataSetChanged();
        }
        TIMHelper.getInstance().getConversationList();
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(false);
    }

    private void getFriendList() {
        BaseAdapter<T> adapter = (BaseAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.getDatas().clear();
            adapter.getDatas().addAll((Collection<? extends T>) TIMHelper.getInstance().friendList);
            adapter.notifyDataSetChanged();
        }
        TIMHelper.getInstance().getFriendList();
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
        @BindView(R.id.username)
        TextView username;
        @BindView(R.id.apply)
        TextView apply;
        T t;

        public ChatRoomHolder(@NonNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false));
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onValue(T t, int position) {
            this.t = t;
            username.setTag(position);
            if (t instanceof V2TIMConversation) {
                V2TIMConversation conversation = (V2TIMConversation) t;
                TIMHelper.getInstance().isFriend(conversation.getUserID(), new MyV2TIMValueCallback<V2TIMFriendCheckResult>(position) {
                    @Override
                    public void onFriend(boolean isFriend, int position) {
                        int tag = (int) username.getTag();
                        if (tag == position) {
                            apply.setVisibility(isFriend ? View.GONE : View.VISIBLE);
                        }
                    }
                });
                username.setText(String.format("%s:%s%s", conversation.getUserID(), to(conversation.getLastMessage()), conversation.getDraftTimestamp()));
            } else if (t instanceof V2TIMFriendInfo) {
                V2TIMFriendInfo friendInfo = (V2TIMFriendInfo) t;
                username.setText(String.format("%s:%s%s", friendInfo.getUserID(), GsonGetter.getInstance().getGson().toJson(friendInfo.getUserProfile())
                        , friendInfo.getFriendRemark()));
            }
        }

        @OnClick({R.id.username, R.id.apply})
        public void onViewClicked(View view) {
            switch (view.getId()) {
                case R.id.username:
                    if (t instanceof V2TIMConversation) {
                        ChatActivity.start(itemView.getContext(), ((V2TIMConversation) t).getUserID(), "");
                    } else if (t instanceof V2TIMFriendInfo) {
                        ChatActivity.start(itemView.getContext(), ((V2TIMFriendInfo) t).getUserID(), "");
                    }
                    break;
                case R.id.apply:
                    String userId = null;
                    if (t instanceof V2TIMConversation) {
                        userId = ((V2TIMConversation) t).getUserID();
                    } else if (t instanceof V2TIMFriendInfo) {
                        userId = ((V2TIMFriendInfo) t).getUserID();
                    }
                    int tag = (int) username.getTag();
                    TIMHelper.getInstance().applyFriend(userId, new MyV2TIMValueCallback<V2TIMFriendOperationResult>(tag) {
                        @Override
                        public void onFriend(boolean isFriend, int position) {
                            int tag = (int) username.getTag();
                            if (tag == position) {
                                apply.setVisibility(isFriend ? View.GONE : View.VISIBLE);
                            }
                        }
                    });
                    break;
            }
        }
    }

    public abstract static class MyV2TIMValueCallback<T> implements V2TIMValueCallback<T> {
        int position;

        public MyV2TIMValueCallback(int position) {
            this.position = position;
        }

        @Override
        public void onError(int i, String s) {
            onFriend(false, position);
        }

        @Override
        public void onSuccess(T t) {
            if (t instanceof V2TIMFriendCheckResult) {
                int resultCode = ((V2TIMFriendCheckResult) t).getResultCode();
                LogUtils.i("zune：", "resultCode =  " + resultCode);
                if (resultCode == 0 && ((V2TIMFriendCheckResult) t).getResultType() == V2TIM_FRIEND_RELATION_TYPE_IN_MY_FRIEND_LIST) {
                    onFriend(true, position);
                } else {
                    onFriend(false, position);
                }
            } else if (t instanceof TIMFriendResult) {
                int resultCode = ((TIMFriendResult) t).getResultCode();
                LogUtils.i("zune：", "resultCode =  " + resultCode);
                if (resultCode == 0) {
                    onFriend(true, position);
                } else {
                    onFriend(false, position);
                }
            }
        }

        public abstract void onFriend(boolean isFriend, int position);
    }
}
