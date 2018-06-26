package cn.lzx.handler;

import cn.lzx.entity.UserInfo;
import cn.lzx.proto.ChatProto;
import cn.lzx.util.NettyUtil;
import cn.lzx.util.RedisUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//channel管理类
public class UserInfoManager {

    //一对读写锁，写锁只能一个线程使用,读锁可以多个线程使用，读-读不互斥，读-写、写-写互斥
    private static ReentrantReadWriteLock rwlock=new ReentrantReadWriteLock(true);

    private static ConcurrentMap<Channel,UserInfo> userInfos=new ConcurrentHashMap<>();

    private static AtomicInteger userCount=new AtomicInteger(0);

    public static void addChannel(Channel channel){
        String address= NettyUtil.parseRemoteAddress(channel);
        //System.out.println("远程IP地址:"+address);
        UserInfo userInfo=new UserInfo();
        userInfo.setChannel(channel);
        userInfo.setAddress(address);
        userInfos.put(channel,userInfo);
    }

    public static boolean saveUser(Channel channel,String nick){
        UserInfo userInfo= userInfos.get(channel);
        if(userInfo==null){
            return false;
        }
        if(!channel.isActive()){
            return false;
        }
        userCount.incrementAndGet();
        userInfo.setNick(nick);
        userInfo.setUserId();
        userInfo.setTime(System.currentTimeMillis());
        userInfo.setAuth(true);
        return true;
    }

    public static void removeChannel(Channel channel){
        try{
            rwlock.writeLock().lock();
            deleteRecord(channel);
            channel.close();
            UserInfo userInfo=userInfos.get(channel);
            if(userInfo!=null){
                UserInfo temp=userInfos.remove(channel);
                if(temp!=null&&temp.isAuth()){
                    userCount.decrementAndGet();
                }
            }
        }finally{
            rwlock.writeLock().unlock();
        }
    }

    public static void broadcastMess(int uid,String nick,String message){
        try{
            rwlock.readLock().lock();
            Set<Channel> channels=userInfos.keySet();
            for(Channel channel:channels){
                UserInfo userInfo=userInfos.get(channel);
                if(userInfo==null||!userInfo.isAuth())continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildMessProto(uid, nick, message)));
            }

        }finally{
            rwlock.readLock().unlock();
        }
    }

    public static void broadcastInfo(int code,Object mess){
        try{
            rwlock.readLock().lock();
            Set<Channel> channels=userInfos.keySet();
            for(Channel channel:channels){
                UserInfo userInfo=userInfos.get(channel);
                if(userInfo==null||!userInfo.isAuth())continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.bulidSystProto(code, mess)));
            }
        }finally{
            rwlock.readLock().unlock();
        }
    }

    public static void broadcastPing(){
        try{
            rwlock.readLock().lock();
            Set<Channel> channels=userInfos.keySet();
            for(Channel channel:channels){
                UserInfo userInfo=userInfos.get(channel);
                if(userInfo==null||!userInfo.isAuth())continue;
                channel.writeAndFlush(new TextWebSocketFrame(ChatProto.bulidPingProto()));
            }
        }finally{
            rwlock.readLock().unlock();
        }
    }

    //删除缓存聊天记录
    public static void deleteRecord(Channel channel){
        Jedis jedis= RedisUtil.getJedis();
        UserInfo userInfo=userInfos.get(channel);
        jedis.del(userInfo.getNick());
        RedisUtil.closeJedis(jedis);
    }

    //获取缓存聊天记录
    public static String getRecord(Channel channel){
        Jedis jedis=RedisUtil.getJedis();
        UserInfo userInfo=userInfos.get(channel);
        String mess=jedis.get(userInfo.getNick());
        RedisUtil.closeJedis(jedis);
        return mess;
    }

    //发送缓存聊天记录
    public static void sendRecord(Channel channel,String mess){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.buildRecoProto(mess)));
    }

    //查询在线用户列表
    public static List<String> getUserNameList(){
        List<String> list=new ArrayList<>();
        try{
            rwlock.readLock().lock();
            Set<Channel> channels=userInfos.keySet();
            for(Channel channel:channels){
                UserInfo userInfo=userInfos.get(channel);
                if(userInfo==null||!userInfo.isAuth()){
                    //System.out.println("使用者"+userInfo+"   使用者认证"+userInfo.isAuth());
                    continue;
                }
                list.add(userInfo.getNick());
            }

        }finally {
            rwlock.readLock().unlock();
        }
        //System.out.println("用户姓名集合："+list);
        return list;

    }

    public static void sendInfo(Channel channel,int code,Object mess){
        channel.writeAndFlush(new TextWebSocketFrame(ChatProto.bulidSystProto(code, mess)));

    }

    public static void scanNotActiveChannel(){
        Set<Channel> channels=userInfos.keySet();
        for(Channel channel:channels){
            UserInfo userInfo=userInfos.get(channel);
            if(userInfo==null)continue;
            if(!channel.isOpen()||!channel.isActive()||
                    (!userInfo.isAuth()&&(userInfo.getTime()-System.currentTimeMillis())>10000)){
                removeChannel(channel);
            }
        }

    }

    public static ConcurrentMap<Channel,UserInfo> getUserInfos(){
        return userInfos;
    }

    public static int getUserCount(){
        return userCount.get();
    }

    public static void updateTime(Channel channel){
        UserInfo userInfo=userInfos.get(channel);
        userInfo.setTime(System.currentTimeMillis());
    }


}
