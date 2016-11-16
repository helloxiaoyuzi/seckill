package org.seckill.service;

import org.junit.Test;
import org.seckill.BaseTest;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by c-liyu on 2016/11/11.
 */
public class SeckillServiceTest extends BaseTest{
    private final static Logger logger= LoggerFactory.getLogger(SeckillServiceTest.class);
    @Autowired
    private SeckillService seckillService;
    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckillList = seckillService.getSeckillList();
        logger.info("list{}",seckillList);
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000l);
        logger.info(seckill.toString());
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1000l);
        logger.info(exposer.toString());
    }

    @Test
    public void executeSeckill() throws Exception {
        //d8e904b013e11215cee9f7739a8cac2b
        SeckillExecution seckillExecution = seckillService.executeSeckill(1000l, 18307211562l, "d8e904b013e11215cee9f7739a8cac2b");
        logger.info("seckillExecution={}",seckillExecution);
    }

    @Test
    public void testExecuteSeckill() throws Exception{
        long seckillId = 1000l;
        long userPhone = 18407211569l;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            logger.info("exposer={}",exposer);
            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, exposer.getMd5());
                logger.info("seckillExecution={}",seckillExecution);
            }catch ( RepeatKillException e1){
                logger.error(e1.getMessage());
            }catch( SeckillCloseException e2){
                logger.error(e2.getMessage());
            }
        }else {
            logger.warn("exposer={}",exposer);
        }
    }

    @Test
    public void executeSeckillProcedure() throws Exception {
        long seckillId=1000l;
        long userPhone = 18307211569l;

        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if(exposer.isExposed()){
            SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId, userPhone, exposer.getMd5());
            System.out.println(seckillExecution.toString());
        }

    }

}