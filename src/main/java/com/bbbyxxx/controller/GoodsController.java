package com.bbbyxxx.controller;

import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.redis.GoodsKey;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.GoodsService;
import com.bbbyxxx.service.MiaoShaUserService;
import com.bbbyxxx.vo.GoodsDetailVo;
import com.bbbyxxx.vo.GoodsVo;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    MiaoShaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    //页面缓存
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request, HttpServletResponse response,/*HttpServletResponse response,*/Model model,
                         //                   @CookieValue(value = MiaoShaUserService.COOKI_NAME_TOKEN,required = false)String cookieToken,//从cookie中取
                         //                 @RequestParam(value = MiaoShaUserService.COOKI_NAME_TOKEN,required = false)String paramToken,//从请求参数中取
                         MiaoShaUser user){
       /* if (StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){//如果都为空，返回登录页面重新登录
            return "login";
        }
        //默认从cookie中取出
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        MiaoShaUser user = userService.getByToken(response,token);*/
        model.addAttribute("user",user);
        //查询商品列表
       /* List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);*/
        //return "goods_list";

       //取缓存
       String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
       if (!StringUtils.isEmpty(html)){
           return html;
       }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);

        SpringWebContext ctx = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
       //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    //URL缓存
    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request,HttpServletResponse response,Model model, MiaoShaUser user,
                         @PathVariable("goodsId")long goodsId){
        model.addAttribute("user",user);

        //取缓存
        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        //手动渲染
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);

        //秒杀离开始/结束还有多长时间
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        //秒杀状态
        int miaoshaStatus = 0;
        //倒计时，还有多少秒
        int remainSeconds = 0;

        //知道秒杀的状态
        if (now < startAt){//还没到秒杀时间,倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt-now)/1000);
        }else if (now > endAt){//过了秒杀时间了
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else{//秒杀正在进行
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);
       //return "goods_detail";

        SpringWebContext ctx = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,"html");
        }
        return html;
    }


    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(MiaoShaUser user,
                                        @PathVariable("goodsId")long goodsId){

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        //秒杀离开始/结束还有多长时间
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        //秒杀状态
        int miaoshaStatus = 0;
        //倒计时，还有多少秒
        int remainSeconds = 0;

        //知道秒杀的状态
        if (now < startAt){//还没到秒杀时间,倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt-now)/1000);
        }else if (now > endAt){//过了秒杀时间了
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else{//秒杀正在进行
            miaoshaStatus = 1;
            remainSeconds = 0;
        }

        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }

}
