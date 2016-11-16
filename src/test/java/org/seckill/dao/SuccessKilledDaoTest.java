package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.BaseTest;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by c-liyu on 2016/11/10.
 */
public class SuccessKilledDaoTest extends BaseTest{

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() throws Exception {
        int insertSuccessKilled = successKilledDao.insertSuccessKilled(10002, 18307211569l);
        System.out.println(insertSuccessKilled);
    }

    @Test
    public void queryByIdWitthSeckill() throws Exception {
        SuccessKilled successKilled = successKilledDao.queryByIdWitthSeckill(1000l,18307211569l);
        System.out.println(successKilled.getUserPhone());
        System.out.println(successKilled.getSeckill().getName());
    }

}