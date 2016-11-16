package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by c-liyu on 2016/11/11.
 */
@Controller
@RequestMapping("/seckill")
public class SeckillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("list",seckillService.getSeckillList());
        return "list";
    }

    @RequestMapping(value="{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId,Model model){
        if(seckillId == null){
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if(seckill==null){
            return "redirect:/seckill/list";
        }
        model.addAttribute("seckill",seckill);
        return "detail";
    }
    @RequestMapping(value="{seckillId}/exposer",method = RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
   public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId){
       SeckillResult<Exposer> seckillResult;
       try {
           Exposer exposer=seckillService.exportSeckillUrl(seckillId);
           seckillResult=new SeckillResult<Exposer>(true,exposer);
       }catch (Exception e){
           logger.error(e.getMessage(),e);
           seckillResult=new SeckillResult<Exposer>(false,e.getMessage());
       }
       return seckillResult;
   }

    /**
     * 秒杀执行方法.
     * @param seckillId 秒杀商品ID
     * @param userPhone 秒杀用户手机
     * @param md5 秒杀Key
     * @return
     */
    @RequestMapping(value = "{seckillId}/{md5}/execution",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
   public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                  @CookieValue(value = "userPhone",required = false) Long userPhone,
                                                  @PathVariable("md5") String md5){
       SeckillResult<SeckillExecution> seckillResult;
       SeckillExecution seckillExecution;
       if(userPhone==null){
           seckillResult=new SeckillResult<SeckillExecution>(false,"未注册");
       }else {
           try {
               //dao操作执行秒杀 seckillExecution=seckillService.executeSeckill(seckillId,userPhone,md5);
               //存储过程执行秒杀
               seckillExecution = seckillService.executeSeckillProcedure(seckillId, userPhone, md5);
               seckillResult=new SeckillResult<SeckillExecution>(true,seckillExecution);
           }catch (SeckillCloseException e1){
                seckillExecution=new SeckillExecution(seckillId, SeckillStateEnum.END);
               seckillResult=new SeckillResult<SeckillExecution>(true,seckillExecution);
           }catch (RepeatKillException e2){
               seckillExecution=new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
               seckillResult=new SeckillResult<SeckillExecution>(true,seckillExecution);
           }catch (SeckillException e){
               logger.error(e.getMessage(),e);
               seckillExecution=new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
               seckillResult=new SeckillResult<SeckillExecution>(true,seckillExecution);
           }
       }
       return seckillResult;
   }

    @RequestMapping(value = "time/now",method = RequestMethod.GET,produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> execute(Model model) {
        return new SeckillResult<Long>(true,new Date().getTime());
    }
}
