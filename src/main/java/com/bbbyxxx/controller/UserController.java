package com.bbbyxxx.controller;

import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
    MiaoShaUserService userService;
	
	@Autowired
    RedisService redisService;
	
    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoShaUser> info(Model model, MiaoShaUser user) {
        return Result.success(user);
    }
    
}
