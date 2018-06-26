package cn.lzx.core;

public class ServerMain {

    public static void main(String args[]) throws Exception {
        ChatServer server=new ChatServer();
        server.run(8888);
    }
}
