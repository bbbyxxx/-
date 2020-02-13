package com.bbbyxxx.controller;

import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.MiaoShaUserService;
import com.bbbyxxx.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginController {
    //引入Logger类和LoggerFactory工厂类来打印日志
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoShaUserService miaoShaUserService;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    /*
    @Valid注解
        用于验证注解是否符合要求，直接加在变量之前，在变量中添加验证信息的要求，当不符合要求时就会在方法中返回message 的错误提示信息。
     */
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        log.info(loginVo.toString());
        //参数校验
       /* String passinput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
        if (StringUtils.isEmpty(passinput)){
            return Result.error(CodeMsg.PASSWORD_EMPTY);
        }else if (StringUtils.isEmpty(mobile)){
            return Result.error(CodeMsg.MOBILE_EMPTY);
        }else if (!ValidateUtil.isMobile(mobile)){
            return Result.error(CodeMsg.MOBILE_ERROR);
        }*/
        //登录
        miaoShaUserService.login(response,loginVo);
        return Result.success(true);
    }

}
