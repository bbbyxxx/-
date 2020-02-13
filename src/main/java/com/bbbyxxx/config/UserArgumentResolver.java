package com.bbbyxxx.config;

import com.bbbyxxx.access.UserContext;
import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.service.MiaoShaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Service
//自定义解析器
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
   @Autowired
    MiaoShaUserService miaoShaUserService;
   
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();//获取类型
        return clazz == MiaoShaUser.class;//是的话，才做下面的处理
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        return UserContext.getUser();
    }

    /* @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

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
    }*/


}
