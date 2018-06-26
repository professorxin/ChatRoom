package cn.lzx.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {

    private RedisUtil(){};

    public static JedisPool pool=null;

    static {
        JedisPoolConfig config=new JedisPoolConfig();
        config.setMaxTotal(30);
        config.setMaxIdle(10);

        pool=new JedisPool(config,"127.0.0.1",6379);
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void closeJedis(Jedis jedis){
        if(jedis!=null){
            jedis.close();
        }
    }
}
