--存储过程
DELIMITER $$
--定义存储过程
--参数：in为输入参数，out为输出参数:1位秒杀成功，0秒杀结束，-1重复秒杀，-2秒杀系统错误
--ROW_COUNT():返回上一条修改类型的SQL(INSERT,DELETE,UPDATE)影响的行数.
--ROW_COUNT()返回值：0表示未修改；大于0表示修改的行数；小于0表示SQL错误或者未执行SQL
CREATE PROCEDURE seckill.execute_seckill(
  IN v_seckill_id BIGINT,
  IN v_phone BIGINT,
  IN v_kill_time  TIMESTAMP,
  OUT r_result INT)
  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION;
    INSERT IGNORE INTO success_killed (seckill_id, user_phone, state,create_time) VALUES (v_seckill_id, v_phone,0,v_kill_time);
    SELECT ROW_COUNT() INTO insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK;
      SET r_result = -1;
    ELSEIF (insert_count < 0) THEN
      ROLLBACK;
      SET r_result = -2;
    ELSE
      UPDATE seckill SET number = number - 1 WHERE seckill_id = v_seckill_id AND end_time > v_kill_time AND start_time < v_kill_time AND number > 0;
      SELECT ROW_COUNT() INTO insert_count;
      IF (insert_count = 0) THEN
        ROLLBACK;
        SET r_result = 0;
      ELSEIF (insert_count < 0) THEN
        ROLLBACK;
        SET r_result = -2;
      ELSE
        COMMIT;
        SET r_result = 1;
      END IF;
    END IF;
  END;
$$