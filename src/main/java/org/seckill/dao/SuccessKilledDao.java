package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

/**
 * Created by c-liyu on 2016/11/9.
 */
public interface SuccessKilledDao {

    /**
     * 插入购买明细，可以过滤重复
     * @param seckillId
     * @param userPhone
     * @return 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据id查询SuccessKilled并携带产品对象实体
     * @param seckillId
     * @param userPhone
     * @return
     */
    SuccessKilled queryByIdWitthSeckill(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);
}
