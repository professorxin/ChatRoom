package cn.lzx.proto;

public class ChatCode {
    public static final int PING_CODE=10015;
    public static final int PONG_CODE=10016;
    public static final int AUTH_CODE=10000;
    public static final int MESS_CODE=10086;
    public static final int RECO_CODE=10008;

    //把SYST协议系统消息在具体细分
    public static final int SYS_USER_COUNT=20001;//在线人数
    public static final int SYS_AUTH_STATE=20002;//认证结果
    public static final int SYS_USER_ONNAME=20003;//上线用户姓名
    public static final int SYS_USER_LIST=20004;//在线用户列表
    public static final int SYS_USER_UNNAME=20005;//下线用户姓名
}
