package cn.lzx.handler;

import cn.lzx.proto.ChatCode;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class UserAuthHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
        if(o instanceof FullHttpRequest){
            handlerHttpRequest(ctx,(FullHttpRequest)o);
        }
        if(o instanceof WebSocketFrame)
            handlerWebSocket(ctx,(WebSocketFrame)o);
    }

    public void handlerHttpRequest(ChannelHandlerContext ctx,FullHttpRequest request){
        if(!request.decoderResult().isSuccess()||!"websocket".equals(request.headers().get("Upgrade"))){
            //System.out.println("不能升级为websocket协议");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory=new WebSocketServerHandshakerFactory("ws:localhost:8888/websocket",null,false);
        handshaker=handshakerFactory.newHandshaker(request);
        if(handshaker==null){
            //System.out.println("建立握手失败");
        }else{
            handshaker.handshake(ctx.channel(),request);
            UserInfoManager.addChannel(ctx.channel());
            //System.out.println("握手成功");
        }
    }

    public void handlerWebSocket(ChannelHandlerContext ctx,WebSocketFrame frame){
        //System.out.println("服务器收到的帧："+frame);
        if(frame instanceof CloseWebSocketFrame){
            String userName=UserInfoManager.getUserInfos().get(ctx.channel()).getNick();
            UserInfoManager.broadcastInfo(ChatCode.SYS_USER_UNNAME,userName);
            UserInfoManager.removeChannel(ctx.channel());
            UserInfoManager.broadcastInfo(ChatCode.SYS_USER_COUNT,UserInfoManager.getUserCount());
            UserInfoManager.broadcastInfo(ChatCode.SYS_USER_LIST,UserInfoManager.getUserNameList());
        }
        if(!(frame instanceof TextWebSocketFrame)){
            //System.out.println("不是文本帧");
            return;
        }
        String mess=((TextWebSocketFrame)frame).text();
        JSONObject object= JSONObject.parseObject(mess);
        int code=object.getInteger("code");
        //System.out.println("客户端发过来的协议："+code);
        switch(code){
            case ChatCode.PONG_CODE:
                //
                return;
            case ChatCode.AUTH_CODE:
                String userName=object.getString("nick");
                boolean isSuccess=UserInfoManager.saveUser(ctx.channel(),userName);
                UserInfoManager.sendInfo(ctx.channel(),ChatCode.SYS_AUTH_STATE,isSuccess);
                if(isSuccess){
                    UserInfoManager.broadcastInfo(ChatCode.SYS_USER_COUNT,UserInfoManager.getUserCount());
                    UserInfoManager.broadcastInfo(ChatCode.SYS_USER_ONNAME,userName);
                    UserInfoManager.broadcastInfo(ChatCode.SYS_USER_LIST,UserInfoManager.getUserNameList());
                }
                return;
            case ChatCode.RECO_CODE:
                String record=UserInfoManager.getRecord(ctx.channel());
                UserInfoManager.sendRecord(ctx.channel(),record);
                return;
            case ChatCode.MESS_CODE:
                //文本消息传给下一个Handler处理
                break;

        }
        ctx.fireChannelRead(frame.retain());
    }


    //60秒内ChannelRead方法没有被调用就会触发
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event=(IdleStateEvent)evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                String userName=UserInfoManager.getUserInfos().get(ctx.channel()).getNick();
                UserInfoManager.broadcastInfo(ChatCode.SYS_USER_UNNAME,userName);
                UserInfoManager.removeChannel(ctx.channel());
                UserInfoManager.broadcastInfo(ChatCode.SYS_USER_COUNT,UserInfoManager.getUserCount());
                UserInfoManager.broadcastInfo(ChatCode.SYS_USER_LIST,UserInfoManager.getUserNameList());
            }
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
