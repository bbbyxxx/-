package com.bbbyxxx.rabbitmq;

import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.domain.MiaoshaOrder;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.GoodsService;
import com.bbbyxxx.service.MiaoShaUserService;
import com.bbbyxxx.service.MiaoshaService;
import com.bbbyxxx.service.OrderService;
import com.bbbyxxx.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MQReceiver {
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
        MiaoshaMessage mm = RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoShaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();//获取库存
        if (stock<=0){//没有库存了
            return ;
        }
        //判断是否已经秒杀成功了,防止一个人秒杀多次
        MiaoshaOrder order = orderService.getMiaoShaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){//说明已经秒杀得到了一个
            return ;
        }
        miaoshaService.miaosha(user,goods);
    }


   /* @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("receive topic1 message:"+message);
        System.out.println("receive topic1 message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("receive topic2 message:"+message);
        System.out.println("receive topic2 message:"+message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeaders(byte[] message){
        log.info("receive headers message:"+message);
        System.out.println("receive headers message:"+message);
    }*/

}
