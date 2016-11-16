package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.BaseTest;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by c-liyu on 2016/11/9.
 */

public class SeckillDaoTest extends BaseTest{
    //注入dao实现依赖类
    @Resource
    private SeckillDao seckillDao;
    @Test
    public void reduceNumber() throws Exception {
        System.out.println(seckillDao.reduceNumber(1000l,new Date()));
    }

    @Test
    public void queryById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
    }

    @Test
    public void queryAll() throws Exception {
        List<Seckill> seckills = seckillDao.queryAll(0, 5);
        for (Seckill seckill : seckills) {
            System.out.println(seckill.getName());
        }
    }

}