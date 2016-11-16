package org.seckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * RuntimeException不粗要try/catch，而且Spring的声明式事务只接收RuntimeException回滚策略
 * Created by c-liyu on 2016/11/10.
 */
public class RepeatKillException extends SeckillException{

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
