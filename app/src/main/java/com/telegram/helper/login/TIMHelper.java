package com.telegram.helper.login;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.telegram.helper.event.ConversationChangeEvent;
import com.telegram.helper.event.GroupChangeEvent;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMFaceElem;
import com.tencent.imsdk.TIMGroupManager;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMConversationResult;
import com.tencent.imsdk.v2.V2TIMFaceElem;
import com.tencent.imsdk.v2.V2TIMFriendAddApplication;
import com.tencent.imsdk.v2.V2TIMFriendCheckResult;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMFriendOperationResult;
import com.tencent.imsdk.v2.V2TIMFriendshipManager;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMManagerImpl;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.imsdk.v2.V2TIMSimpleMsgListener;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMUserInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tencent.imsdk.v2.V2TIMSDKConfig.V2TIM_LOG_NONE;

public class TIMHelper {
    private static TIMHelper sTimHelper;
    public static final int sdkAppID = 1400397996;
    public static final String sdkSecret = "cb61b88e72585e5ecf9a7137d79e97596ab315fdc1c06322314b60591ba83980";
    private V2TIMSDKConfig sdkConfig;
    private boolean enable;
    private LoginHelper.CallBack callBack;
    public List<V2TIMFriendInfo> friendList = new ArrayList<>();
    public List<V2TIMConversation> conversations = new ArrayList<>();  //我的会话列表
    public List<TIMGroupBaseInfo> mGroupLists = new ArrayList<>();  //我的群组列表

    private TIMHelper() {
        sdkConfig = new V2TIMSDKConfig();
        sdkConfig.setLogLevel(V2TIM_LOG_NONE);
    }

    public static TIMHelper getInstance() {
        if (sTimHelper == null) {
            synchronized (TIMHelper.class) {
                if (sTimHelper == null) {
                    sTimHelper = new TIMHelper();
                }
            }
        }
        return sTimHelper;
    }

    public void registerConnect(LoginHelper.CallBack callBack) {
        this.callBack = callBack;
    }

    public void init(Context context) {
        // 1. 从 IM 控制台获取应用 SDKAppID，详情请参考 SDKAppID。
        // 2. 初始化 config 对象
        V2TIMSDKConfig config = new V2TIMSDKConfig();
        // 3. 指定 log 输出级别，详情请参考 SDKConfig。
        config.setLogLevel(V2TIMSDKConfig.V2TIM_LOG_INFO);
        // 4. 初始化 SDK 并设置 V2TIMSDKListener 的监听对象。
        // initSDK 后 SDK 会自动连接网络，网络连接状态可以在 V2TIMSDKListener 回调里面监听。
        V2TIMManager.getInstance().initSDK(context, sdkAppID, sdkConfig, new V2TIMSDKListener() {
            // 5. 监听 V2TIMSDKListener 回调
            @Override
            public void onConnecting() {
                // 正在连接到腾讯云服务器
            }

            @Override
            public void onConnectSuccess() {
                // 已经成功连接到腾讯云服务器
                enable = true;
                if (callBack != null) {
                    callBack.onCallBack(true);
                }
                callBack = null;
            }

            @Override
            public void onConnectFailed(int code, String error) {
                // 连接腾讯云服务器失败
                if (callBack != null) {
                    callBack.onCallBack(false);
                }
                callBack = null;
            }
        });
        // 1. 设置会话监听
        V2TIMManager.getConversationManager().setConversationListener(new V2TIMConversationListener() {
            @Override
            public void onNewConversation(List<V2TIMConversation> conversationList) {
                super.onNewConversation(conversationList);
                addConversations(conversationList);
            }

            @Override
            public void onConversationChanged(List<V2TIMConversation> conversationList) {
                super.onConversationChanged(conversationList);
                addConversations(conversationList);
            }
        });
        /*zune: 消息监听**/
        V2TIMManagerImpl.getInstance().addSimpleMsgListener(new V2TIMSimpleMsgListener() {
            @Override
            public void onRecvC2CTextMessage(String msgID, V2TIMUserInfo sender, String text) {
                super.onRecvC2CTextMessage(msgID, sender, text);
                for (OnReceiveMsgListener msgListener : msgListeners) {
                    if (msgListener != null) {
                        msgListener.onRecvC2CTextMessage(msgID, sender, text);
                    }
                }
            }

            @Override
            public void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text) {
                super.onRecvGroupTextMessage(msgID, groupID, sender, text);
                for (OnReceiveMsgListener msgListener : msgListeners) {
                    if (msgListener != null) {
                        msgListener.onRecvGroupTextMessage(msgID, groupID, sender, text);
                    }
                }
            }
        });
    }

    public interface OnReceiveMsgListener {
        void onRecvC2CTextMessage(String msgID, V2TIMUserInfo sender, String text);

        void onRecvGroupTextMessage(String msgID, String groupID, V2TIMGroupMemberInfo sender, String text);
    }

    private List<OnReceiveMsgListener> msgListeners = new ArrayList<>();

    public void register(OnReceiveMsgListener listener) {
        msgListeners.add(listener);
    }

    public void unRegister(OnReceiveMsgListener listener) {
        if (msgListeners.contains(listener)) {
            msgListeners.remove(listener);
        }
    }

    private void addConversations(List<V2TIMConversation> conversationList) {
        if (conversationList == null || conversationList.isEmpty()) {
            return;
        }
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < conversations.size(); i++) {
            V2TIMConversation v2TIMConversation = conversations.get(i);
            if (userIds.contains(v2TIMConversation.getConversationID())) {
                continue;
            }
            userIds.add(v2TIMConversation.getConversationID());
        }
        boolean changed = false;
        for (int i = 0; i < conversationList.size(); i++) {
            V2TIMConversation v2TIMConversation = conversationList.get(i);
            if (userIds.contains(v2TIMConversation.getConversationID())) {
                continue;
            }
            changed = true;
            conversations.add(v2TIMConversation);
        }
        if (changed) {
            ConversationChangeEvent event = new ConversationChangeEvent();
            EventBus.getDefault().post(event);
        }
    }

    private void addGroup(List<TIMGroupBaseInfo> groupBaseInfos) {
        if (groupBaseInfos == null || groupBaseInfos.isEmpty()) {
            return;
        }
        List<String> groupIds = new ArrayList<>();
        for (int i = 0; i < mGroupLists.size(); i++) {
            TIMGroupBaseInfo groupBaseInfo = mGroupLists.get(i);
            if (groupIds.contains(groupBaseInfo.getGroupId())) {
                continue;
            }
            groupIds.add(groupBaseInfo.getGroupId());
        }
        boolean changed = false;
        for (int i = 0; i < groupBaseInfos.size(); i++) {
            TIMGroupBaseInfo groupBaseInfo = groupBaseInfos.get(i);
            if (groupIds.contains(groupBaseInfo.getGroupId())) {
                continue;
            }
            changed = true;
            mGroupLists.add(groupBaseInfo);
        }
        if (changed) {
            GroupChangeEvent event = new GroupChangeEvent();
            EventBus.getDefault().post(event);
        }
    }

    public boolean enableConnect() {
        return enable;
    }

    public void login(String userName, final LoginHelper.CallBack callBack) {
        String userSig = GenerateTestUserSig.genUserSig(userName);
        TIMManager.getInstance().login(userName, userSig, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                if (callBack != null) {
                    callBack.onCallBack(false);
                }
            }

            @Override
            public void onSuccess() {
                if (callBack != null) {
                    callBack.onCallBack(true);
                }
            }
        });
    }

    public void getConversationList() {
        // 2. 先拉取50个本地会话做 UI 展示，nextSeq 第一次拉取传0
        V2TIMManager.getConversationManager().getConversationList(0, 50,
                new V2TIMValueCallback<V2TIMConversationResult>() {
                    @Override
                    public void onError(int code, String desc) {
                        // 拉取会话列表失败
                    }

                    @Override
                    public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                        // 拉取成功，更新 UI 会话列表
                        List<V2TIMConversation> conversationList = v2TIMConversationResult.getConversationList();
                        addConversations(conversationList);
                        if (!v2TIMConversationResult.isFinished()) {
                            V2TIMManager.getConversationManager().getConversationList(
                                    v2TIMConversationResult.getNextSeq(), 50,
                                    new V2TIMValueCallback<V2TIMConversationResult>() {
                                        @Override
                                        public void onError(int code, String desc) {
                                        }

                                        @Override
                                        public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                                            // 拉取成功，更新 UI 会话列表
                                            addConversations(v2TIMConversationResult.getConversationList());
                                        }
                                    });
                        }
                    }
                });
    }

    public void getFriendList() {
        V2TIMFriendshipManager manager = V2TIMManagerImpl.getFriendshipManager();
        manager.getFriendList(new V2TIMValueCallback<List<V2TIMFriendInfo>>() {
            @Override
            public void onError(int i, String s) {
            }

            @Override
            public void onSuccess(List<V2TIMFriendInfo> v2TIMFriendInfos) {
                addFriends(v2TIMFriendInfos);
            }
        });
    }

    private void addFriends(List<V2TIMFriendInfo> friendInfos) {
        if (friendInfos == null || friendInfos.isEmpty()) {
            return;
        }
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < friendList.size(); i++) {
            V2TIMFriendInfo friendInfo = friendList.get(i);
            if (userIds.contains(friendInfo.getUserID())) {
                continue;
            }
            userIds.add(friendInfo.getUserID());
        }
        boolean changed = false;
        for (int i = 0; i < friendInfos.size(); i++) {
            V2TIMFriendInfo friendInfo = friendInfos.get(i);
            if (userIds.contains(friendInfo.getUserID())) {
                continue;
            }
            changed = true;
            friendList.add(friendInfo);
        }
        if (changed) {
            ConversationChangeEvent event = new ConversationChangeEvent();
            EventBus.getDefault().post(event);
        }
    }

    public void getGroupList() {
        TIMGroupManager.getInstance().getGroupList(new TIMValueCallBack<List<TIMGroupBaseInfo>>() {
            @Override
            public void onError(int i, String s) {
                addGroup(new ArrayList<>());
            }

            @Override
            public void onSuccess(List<TIMGroupBaseInfo> timGroupBaseInfos) {
                addGroup(timGroupBaseInfos);
            }
        });
    }

    public void createGroup() {
//        args[0] = zune：
//    │ args[1] = groupId = @TGS#3HGN3DTGP
        String groupType = "Meeting";
        String groupID = null;
        String groupName = "屁眼爱好";
        V2TIMValueCallback<String> callback = new V2TIMValueCallback<String>() {
            @Override
            public void onError(int i, String s) {
                ToastUtils.showShort("创建群组失败");
            }

            @Override
            public void onSuccess(String s) {
                ToastUtils.showShort("创建群组成功");
                LogUtils.i("zune：", "groupId = " + s);
            }
        };
        V2TIMManager.getInstance().createGroup(groupType, groupID, groupName, callback);
    }

    public void joinGroup() {
        V2TIMManager.getInstance().joinGroup("@TGS#3HGN3DTGP", "加入了屁眼爱好群组", new V2TIMCallback() {
            @Override
            public void onError(int i, String s) {
                LogUtils.i("zune：", "进群失败：code = " + i + ", msg = " + s);
            }

            @Override
            public void onSuccess() {
                ToastUtils.showShort("进入群组成功");
            }
        });
    }

    public void sendMsg(TIMConversationType type, String toUserId, String content, LoginHelper.CallBack callBack) {
        V2TIMManagerImpl.getInstance().sendC2CTextMessage(content, toUserId, new V2TIMValueCallback<V2TIMMessage>() {
            @Override
            public void onError(int code, String desc) {
                LogUtils.i("zune：", "code = " + code + ", desc  = " + desc);
                if (callBack != null) {
                    callBack.onCallBack(false);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                if (callBack != null) {
                    callBack.onCallBack(true);
                }
            }
        });
    }

    public static TIMMessage from(String msg) {
        TIMMessage message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(msg);
        message.setCustomStr(LoginHelper.getInstance().getUserId());
        message.addElement(elem);
        return message;
    }

    public static TIMMessage from(String sender, String msg) {
        TIMMessage message = new TIMMessage();
        TIMTextElem elem = new TIMTextElem();
        elem.setText(msg);
        message.addElement(elem);
        message.setCustomStr(sender);
        return message;
    }

    public static CharSequence to(TIMMessage timMessage) {
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
        return stringBuilder;
    }

    public static CharSequence to(V2TIMMessage timMessage) {
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        switch (timMessage.getElemType()) {
            case 8:
                V2TIMFaceElem faceElem = timMessage.getFaceElem();
                stringBuilder.append(Arrays.toString(faceElem.getData()));
                break;
            case 1:
                V2TIMTextElem textElem = timMessage.getTextElem();
                stringBuilder.append(textElem.getText());
                break;
        }
        return stringBuilder;
    }

    public void isFriend(String userID, V2TIMValueCallback<V2TIMFriendCheckResult> callback) {
        V2TIMManager.getFriendshipManager().checkFriend(userID, V2TIMFriendInfo.V2TIM_FRIEND_TYPE_SINGLE, callback);
    }

    public void applyFriend(String userId, V2TIMValueCallback<V2TIMFriendOperationResult> callback) {
        V2TIMFriendAddApplication application = new V2TIMFriendAddApplication(userId);
        V2TIMManager.getFriendshipManager().addFriend(application, callback);
    }

    public void sendGroupMsg(TIMConversationType group, String groupId, String content, LoginHelper.CallBack callBack) {
        V2TIMManagerImpl.getInstance().sendGroupTextMessage(content, groupId, V2TIMMessage.V2TIM_PRIORITY_NORMAL, new V2TIMValueCallback<V2TIMMessage>() {
            @Override
            public void onError(int i, String s) {
                LogUtils.i("zune：", "code = " + i + ", msg = " + s);
                if (callBack != null) {
                    callBack.onCallBack(false);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage v2TIMMessage) {
                if (callBack != null) {
                    callBack.onCallBack(true);
                }
            }
        });
    }
}
