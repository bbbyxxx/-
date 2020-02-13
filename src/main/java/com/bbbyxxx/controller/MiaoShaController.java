package com.bbbyxxx.controller;

import com.bbbyxxx.access.AccessLimit;
import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.domain.MiaoshaOrder;
import com.bbbyxxx.domain.OrderInfo;
import com.bbbyxxx.rabbitmq.MQSender;
import com.bbbyxxx.rabbitmq.MiaoshaMessage;
import com.bbbyxxx.redis.*;
import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.GoodsService;
import com.bbbyxxx.service.MiaoShaUserService;
import com.bbbyxxx.service.MiaoshaService;
import com.bbbyxxx.service.OrderService;
import com.bbbyxxx.util.MD5Util;
import com.bbbyxxx.util.UUIDUtil;
import com.bbbyxxx.vo.GoodsVo;
import com.rabbitmq.client.AMQP;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.lang.model.element.NestingKind;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoShaController implements InitializingBean {
    @Autowired
    MiaoShaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender sender;

    private HashMap<Long,Boolean> localOverMap = new HashMap<>();

    //在系统初始化的时候，将秒杀商品数量存入Redis缓存
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        if (goodsVoList == null){
            return;
        }else{
            for (GoodsVo goodsVo:goodsVoList){
                redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
                localOverMap.put(goodsVo.getId(),false);
            }
        }
    }


    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    /* @RequestMapping("/do_miaosha")
    public String list(Model model, MiaoShaUser user,
                       @RequestParam("goodsId")long goodsId){
        model.addAttribute("user",user);
        if (user == null){//用户没有登录，返回登录页面进行登录
            return "login";
        }
        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();//获取库存
        if (stock<=0){//没有库存了
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        //判断是否已经秒杀成功了,防止一个人秒杀多次
        MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){//说明已经秒杀得到了一个
            model.addAttribute("errmsg",CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods",goods);
        return "order_detail";
    }*/

    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoShaUser user,
                                   @RequestParam("goodsId")long goodsId,
                                   @PathVariable("path") String path){
        model.addAttribute("user",user);
        if (user == null){//用户没有登录，返回登录页面进行登录
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean check = miaoshaService.checkPath(user,goodsId,path);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if(over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //减去Redis里面的库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if (stock < 0){//Redis中没有库存了，秒杀失败了！
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀成功了,防止一个人秒杀多次
        MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){//说明已经秒杀得到了一个
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //没有，则进行秒杀 减库存等等
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);//将消息加入队列中
        return Result.success(0);//0代表排队中

       /* //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();//获取库存
        if (stock<=0){//没有库存了
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀成功了,防止一个人秒杀多次
        MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){//说明已经秒杀得到了一个
           return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        return Result.success(orderInfo);*/
    }

    /**
     *orderId:成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoShaUser user,
                                   @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {//报错
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //判断用户有没有秒杀到商品
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }


    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request,Model model, MiaoShaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        model.addAttribute("user", user);
        if (user == null) {//报错
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //查询访问的次数,5秒钟访问5次
        /*String uri = request.getRequestURI();
        String key = uri+"_"+user.getId();
        Integer count = redisService.get(AccessKey.access,key,Integer.class);
        if (count == null){
            redisService.set(AccessKey.access,key,1);
        }else if (count < 5){
            redisService.incr(AccessKey.access,key);
        }else {
            return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
        }*/
        //验证验证码
        boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }


    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaverifyCode(HttpServletResponse response, Model model, MiaoShaUser user,
                                               @RequestParam("goodsId")long goodsId) {
        if (user == null) {//报错
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createVerifyCode(user,goodsId);
        try{
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}
