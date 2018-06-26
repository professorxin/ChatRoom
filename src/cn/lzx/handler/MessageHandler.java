package cn.lzx.handler;

import cn.lzx.entity.UserInfo;
import cn.lzx.proto.ChatCode;
import cn.lzx.util.DataTimeUtil;
import cn.lzx.util.RedisUtil;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import redis.clients.jedis.Jedis;

import java.util.Set;


public class MessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        UserInfo userInfo=UserInfoManager.getUserInfos().get(ctx.channel());
        if(userInfo!=null&&userInfo.isAuth()){
            JSONObject object=JSONObject.parseObject(((TextWebSocketFrame)frame).text());
            //利用jedis缓存存放消息记录
            String nick=userInfo.getNick();
            int uid=userInfo.getUserId();
            String time= DataTimeUtil.getCurrentTime();
            String mess=object.getString("mess");
            String message="<div class=\"title\">"+nick+"&nbsp;("+uid+")&nbsp;"+time+"</div><div class=\"item\">"+mess+"</div>";
            Jedis jedis= RedisUtil.getJedis();
            Set<Channel> channels=UserInfoManager.getUserInfos().keySet();
            for(Channel channel:channels){
                UserInfo user=UserInfoManager.getUserInfos().get(channel);
                if(user.isAuth()&&user!=null){
                    if(jedis.get(user.getNick())!=null){
                        String newMessage=jedis.get(user.getNick())+message;
                        jedis.set(user.getNick(),newMessage);
                    }else {
                        jedis.set(user.getNick(),message);
                    }
                    //System.out.println("jedis缓存的信息："+jedis.get(nick));
                }
            }
            RedisUtil.closeJedis(jedis);
            UserInfoManager.broadcastMess(userInfo.getUserId(),userInfo.getNick(),object.getString("mess"));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        UserInfoManager.removeChannel(ctx.channel());
        UserInfoManager.broadcastInfo(ChatCode.SYS_AUTH_STATE,UserInfoManager.getUserCount());
        cause.printStackTrace();
        ctx.close();
    }
}
