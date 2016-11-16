package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by c-liyu on 2016/11/14.
 */
public class RedisDao {

    private final static Logger logger= LoggerFactory.getLogger(RedisDao.class);

    private final static String SECKILL_KEY_PREFIX="seckill";

    private final JedisPool jedisPool;


    private RuntimeSchema<Seckill> shema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip,int port ){
        jedisPool=new JedisPool(ip,port);
    }

    public Seckill getSeckill(long seckillId){
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                String key=SECKILL_KEY_PREFIX+seckillId;
                byte[] bytes = jedis.get(key.getBytes());
                if(bytes!=null){
                    Seckill seckill=shema.newMessage();
                    ProtobufIOUtil.mergeFrom(bytes,seckill,shema);
                    return seckill;
                }
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill){
        try {
            Jedis jedis=jedisPool.getResource();
            try {
                String key=SECKILL_KEY_PREFIX+seckill.getSeckillId();
                byte[] bytes =ProtobufIOUtil.toByteArray(seckill,shema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout=60*60;
                String result =jedis.setex(key.getBytes(),timeout,bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return null;
    }
}
