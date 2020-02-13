package com.bbbyxxx.controller;

import com.bbbyxxx.domain.User;
import com.bbbyxxx.rabbitmq.MQSender;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.redis.UserKey;
import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/demo")
public class SampleController {
    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

   /* @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader(){
        mqSender.sendHeader("hello i`m xiaobai!");
        return Result.success("Hello World!");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout(){
        mqSender.sendFanout("hello i`m xiaobai!");
        return Result.success("Hello World!");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic(){
        mqSender.sendTopic("hello i`m xiaobai!");
        return Result.success("Hello World!");
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        mqSender.send("hello i`m xiaobai!");
        return Result.success("Hello World!");
    }*/

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","xiaobai");
        return "hello";
    }

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SESSION_ERROR);
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx(){
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById,""+1,User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("1111");
        Boolean ret = redisService.set(UserKey.getById,""+1,user);//UserKey:id1
        return Result.success(ret);
    }
}
