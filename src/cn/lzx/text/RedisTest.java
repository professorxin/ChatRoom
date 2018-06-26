package cn.lzx.text;

import cn.lzx.util.RedisUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisTest {
    @Test
    public void JedisTest(){
        Jedis jedis=new Jedis("localhost");
        //System.out.println("连接成功");
        //System.out.println("服务正在进行"+jedis.ping());
        jedis.set("xingming","林达浪");
        System.out.println(jedis.get("姓名"));
    }

    @Test
    public void JedisPoolTest(){
        Jedis jedis=RedisUtil.getJedis();
        jedis.set("xueha","123");
        System.out.println(jedis.get("xueha"));
        RedisUtil.closeJedis(jedis);
    }
}
