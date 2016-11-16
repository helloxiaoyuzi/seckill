package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by c-liyu on 2016/11/10.
 */

/**
 * @component 标识是一个组件，不知道到底是什么组件 如果知道具体是哪个组件，就用下面的具体标识
 * @Service 就是一个service
 * @Dao dao
 * @Controller web层入口
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private final static Logger LOG=LoggerFactory.getLogger(SeckillServiceImpl.class);
    private final String slat = "fajsijdP{)(LPLPK:()*(*1231dasda";
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 用注解方式注入依赖，@Autowired是spring提供的，@Resource、@Inject是J2EE规范提供的。
     */
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        //优化到缓存 先查缓存-》没查到-》数据库查-》写入缓存
        Seckill seckill=redisDao.getSeckill(seckillId);
        if (seckill == null) {
            seckill=seckillDao.queryById(seckillId);
            if(seckill==null){
                return new Exposer(false, seckillId);
            }else {
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();    //系统当前时间
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId); //TODO
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     *使用注解控制事务的有点：
     * 1.开发团队达成一致约定，明确注解事务方法的编码风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作PRC/HTTP请求，尽量将这些操作剥离事务之外
     * 3.不是所有方法都需要事务，如一些查询的service，只有一条修改操作的service等等
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (StringUtils.isEmpty(md5) || !md5.equals(getMD5(seckillId))) {
            throw new SeckillException("Seckill data rewrite!");
        }
        //执行秒杀逻辑：1.减库存；2.记录购买行为
        Date nowTime = new Date();
        try {
            //1.减库存
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                //库存不足，导致秒杀活动结束，或者不在秒杀活动期间，秒杀活动未开始
                throw new SeckillCloseException("Seckill is closed!");
            } else {
                //2.记录购买行为
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                if (insertCount <= 0) {
                    //重复秒杀
                    throw new RepeatKillException("Seckill repeated!");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWitthSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            logger.error(e1.getMessage());
            throw e1;
        } catch (RepeatKillException e2) {
            logger.error(e2.getMessage());
            throw e2;
        } catch (SeckillException e) {
            logger.error(e.getMessage());
            //所有的编译期异常转化为运行期异常,spring的声明式事务做rollback
            throw new SeckillException("Seckill inner error:" + e.getMessage());
        }
    }

    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("seckillId", seckillId);
        map.put("userPhone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        //存储过程执行完之后result被赋值
        try {
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result==1) {
                SuccessKilled sk = successKilledDao.queryByIdWitthSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,sk);
            }else {
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }
}
