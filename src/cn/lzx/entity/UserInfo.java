package cn.lzx.entity;


import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicInteger;

public class UserInfo {

    private static AtomicInteger uidGener =new AtomicInteger(1000);

    private int userId;//uid
    private String nick;//登录名
    private boolean IsAuth=false;//是否认证
    private long time =0;//登录时间
    private String address;//地址
    private Channel channel;//通道

    public int getUserId() {
        return userId;
    }

    public void setUserId() {
        this.userId = uidGener.incrementAndGet();
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public boolean isAuth() {
        return IsAuth;
    }

    public void setAuth(boolean auth) {
        IsAuth = auth;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
