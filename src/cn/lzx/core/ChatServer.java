package cn.lzx.core;

import cn.lzx.handler.MessageHandler;
import cn.lzx.handler.UserAuthHandler;
import cn.lzx.handler.UserInfoManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//服务器启动类
public class ChatServer {
    //将定时任务和线程池相结合的类
    private ScheduledExecutorService executorService= Executors.newScheduledThreadPool(2);


    public void run(int port) throws Exception{

        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workGroup=new NioEventLoopGroup();

        try{
            ServerBootstrap b=new ServerBootstrap();
            b.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected  void initChannel(SocketChannel channel){
                    ChannelPipeline cp=channel.pipeline();
                    cp.addLast(new HttpServerCodec());
                    cp.addLast(new ChunkedWriteHandler());
                    cp.addLast(new HttpObjectAggregator(65536));
                    cp.addLast(new IdleStateHandler(60,0,0));
                    cp.addLast(new UserAuthHandler());
                    cp.addLast(new MessageHandler());
                }
            });
            ChannelFuture cf=b.bind(port).sync();
            /*实现周期性执行*/
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("开始扫描空闲管道");
                    UserInfoManager.scanNotActiveChannel();
                }
            },3,60, TimeUnit.SECONDS);

            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("开始发送ping");
                    UserInfoManager.broadcastPing();
                }
            },3,50,TimeUnit.SECONDS);

            cf.channel().closeFuture().sync();
        }finally {
            if(executorService!=null){
                executorService.shutdown();
            }
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}
