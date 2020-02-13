package com.bbbyxxx.access;

import com.alibaba.fastjson.JSON;
import com.bbbyxxx.config.UserArgumentResolver;
import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.domain.User;
import com.bbbyxxx.redis.AccessKey;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.result.Result;
import com.bbbyxxx.service.MiaoShaUserService;
import com.bbbyxxx.service.MiaoshaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {
   @Autowired
    MiaoShaUserService miaoShaUserService;

   @Autowired
    RedisService redisService;

    //方法执行之前
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            MiaoShaUser miaoShaUser = getUser(request,response);
            UserContext.setUser(miaoShaUser);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){//如果没有加注解的话，则直接返回true
                return true;
            }
            //多少秒
            int seconds = accessLimit.seconds();
            //多少次
            int maxCount = accessLimit.maxCount();
            //是否需要登录
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin){
                if (miaoShaUser == null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + miaoShaUser.getId();
            }

            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak,key,Integer.class);
            if (count == null){
                redisService.set(ak,key,1);
            }else if (count < maxCount){
                redisService.incr(ak,key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response,CodeMsg cm) throws Exception{
        response.setContentType("application/json;charset=utf-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("utf-8"));
        out.flush();
        out.close();
    }

    private MiaoShaUser getUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter(MiaoShaUserService.COOKI_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoShaUserService.COOKI_NAME_TOKEN);
        if (StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){//如果都为空，返回登录页面重新登录
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return miaoShaUserService.getByToken(response,token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiNameToken) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0){
            return null;
        }
        for (Cookie cookie:cookies){
            if (cookie.getName().equals(cookiNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
