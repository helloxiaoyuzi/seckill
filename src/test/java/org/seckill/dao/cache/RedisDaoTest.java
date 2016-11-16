package org.seckill.dao.cache;

import org.junit.Test;
import org.seckill.BaseTest;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by c-liyu on 2016/11/15.
 */
public class RedisDaoTest extends BaseTest{

    @Autowired
    private RedisDao redisDao;
    @Autowired
    private SeckillDao seckillDao;

    @Test
    public  void testRedisDao() throws Exception{
        long seckillId=1000l;
        Seckill seckill=null;
        seckill=redisDao.getSeckill(seckillId);
        if(seckill==null){
            seckill=seckillDao.queryById(seckillId);
            if(seckill!=null){
               String result= redisDao.putSeckill(seckill);
                System.out.println(result);
            }
            seckill=redisDao.getSeckill(seckillId);
            System.out.println(seckill);
        }
        System.out.println(seckill);
    }

}