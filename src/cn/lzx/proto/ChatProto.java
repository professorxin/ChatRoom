package cn.lzx.proto;

import cn.lzx.util.DataTimeUtil;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatProto {
    public static final int PING_PROTO = 1 << 8 | 220;
    public static final int PONG_PROTO = 2 << 8 | 220;
    public static final int SYST_PROTO = 3 << 8 | 220;
    public static final int ERRO_PROTO = 4 << 8 | 220;
    public static final int AUTH_PROTO = 5 << 8 | 220;
    public static final int MESS_PROTO = 6 << 8 | 220;
    public static final int RECO_PROTO = 7 << 8 | 220;

    //遇到一个坑，要有getter方法才能转化为json

    //协议字段
    private int uri;
    //内容字段
    private String body;
    //扩展字段
    private Map<String,Object> extend =new HashMap<>();

    public ChatProto(int uri,String body){
        this.uri=uri;
        this.body=body;
    }

    public static String bulidPingProto(){
        ChatProto chatProto=new ChatProto(PING_PROTO,null);
        System.out.println(JSONObject.toJSONString(chatProto));
        return JSONObject.toJSONString(chatProto);

    }

    public static String buildPongProto(){
        ChatProto chatProto=new ChatProto(PONG_PROTO,null);
        return JSONObject.toJSONString(chatProto);
    }

    public static String bulidSystProto(int code,Object mess){
        //System.out.println("系统协议："+code+"   系统消息："+mess);
        ChatProto chatProto=new ChatProto(SYST_PROTO,null);
        chatProto.extend.put("code",code);
        chatProto.extend.put("mess",mess);
        System.out.println(JSONObject.toJSONString(chatProto));
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildErrorProto(int code,String mess){
        ChatProto chatProto=new ChatProto(code,null);
        chatProto.extend.put("code",code);
        chatProto.extend.put("mess",mess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildAuthProto(boolean isSuccess){
        ChatProto chatProto=new ChatProto(AUTH_PROTO,null);
        chatProto.extend.put("isSuccess",isSuccess);
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildMessProto(int uid,String nick,String mess){
        ChatProto chatProto=new ChatProto(MESS_PROTO,mess);
        chatProto.extend.put("uid",uid);
        chatProto.extend.put("nick",nick);
        chatProto.extend.put("time", DataTimeUtil.getCurrentTime());
        System.out.println(JSONObject.toJSONString(chatProto));
        return JSONObject.toJSONString(chatProto);
    }

    public static String buildRecoProto(String mess){
        ChatProto chatProto=new ChatProto(RECO_PROTO,mess);
        System.out.println(JSONObject.toJSONString(chatProto));
        return JSONObject.toJSONString(chatProto);
    }


    public int getUri() {
        return uri;
    }

    public void setUri(int uri) {
        this.uri = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }
}
