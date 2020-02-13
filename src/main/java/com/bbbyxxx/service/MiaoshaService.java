package com.bbbyxxx.service;


import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.domain.MiaoshaOrder;
import com.bbbyxxx.domain.OrderInfo;
import com.bbbyxxx.redis.MiaoshaKey;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.util.MD5Util;
import com.bbbyxxx.util.UUIDUtil;
import com.bbbyxxx.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoshaService {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional
    public OrderInfo miaosha(MiaoShaUser user, GoodsVo goods){
        //减库存
        boolean success = goodsService.reduceStock(goods);
        if (success){
            // 下订单,order_info 写入秒杀订单  miaosha_order
            return orderService.createOrder(user,goods);
        }else {
            //设置商品卖完了
            setGoodsOver(goods.getId());
            return null;
        }
    }

    private void setGoodsOver(Long goodsId) {
        redisService.set(MiaoshaKey.isGoodsOver,""+goodsId,true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }

    public long getMiaoshaResult(Long userId, long goodsId) {
       MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(userId,goodsId);
       if (order!=null){//说明秒杀成功
           return order.getOrderId();
       }else{
           //判断商品是不是卖完了
           boolean isOver = getGoodsOver(goodsId);
           if (isOver){//卖完了
               return -1;
           }else{
               return 0;
           }
       }
    }

    public void reset(List<GoodsVo> goodsList) {
        goodsService.resetStock(goodsList);
        orderService.deleteOrders();
    }

    public boolean checkPath(MiaoShaUser user, long goodsId, String path) {
        if (user == null||path == null){
            return false;
        }
        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,String.class);
        return path.equals(pathOld);
    }

    public String createMiaoshaPath(MiaoShaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,str);
        return str;
    }

    public BufferedImage createVerifyCode(MiaoShaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //获取画笔
        Graphics g = image.getGraphics();
        //给画笔上色
        g.setColor(new Color(0xDCDCDC));
        //填充背景
        g.fillRect(0, 0, width, height);
        //给画笔上色
        g.setColor(Color.black);
        //画边框
        g.drawRect(0, 0, width - 1, height - 1);
        //生成验证码
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);//画了50个点，在图像上
        }
        //验证码生成
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();//销毁画笔
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    private static int calc(String exp) {
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            //script引擎
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);//计算你字符串的值
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    private static char[] ops = new char[]{'+','-','*'};
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    public static void main(String[] args) {
        System.out.println(calc("1+3+9"));
    }

    public boolean checkVerifyCode(MiaoShaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId,Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0){
            return false;
        }
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+","+goodsId);
        return true;
    }
}
