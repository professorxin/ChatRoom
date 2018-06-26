package cn.lzx.util;

import io.netty.channel.Channel;

import java.net.SocketAddress;

//获取channnel的远程IP地址
public class NettyUtil {

    //final??
    public static String parseRemoteAddress(final Channel channel){
        SocketAddress socketAddress=channel.remoteAddress();
        String addr= socketAddress!=null ? socketAddress.toString(): "";
        if(addr.length()>0){
            int index=addr.lastIndexOf("/");
            //System.out.println("需要截的字段序号"+index);
            if(index>=0){
                //System.out.println("远程ip地址"+addr.substring(index+1));
                return addr.substring(index+1);
            }
            return addr;
        }
        return "";
    }
}
