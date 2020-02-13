package com.bbbyxxx.service;

import com.bbbyxxx.dao.MiaoShaUserDao;
import com.bbbyxxx.domain.MiaoShaUser;
import com.bbbyxxx.exception.GlobalException;
import com.bbbyxxx.redis.MiaoShaUserKey;
import com.bbbyxxx.redis.RedisService;
import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.util.MD5Util;
import com.bbbyxxx.util.UUIDUtil;
import com.bbbyxxx.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoShaUserService {
    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    MiaoShaUserDao miaoShaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoShaUser getById(long id){
        //取缓存
        MiaoShaUser user = redisService.get(MiaoShaUserKey.getById,""+id,MiaoShaUser.class);
        if (user!=null){//缓存有直接拿
            return user;
        }
        //没有，从数据库取
        user = miaoShaUserDao.getById(id);
        if (user!=null){
            redisService.set(MiaoShaUserKey.getById,""+id,user);
        }
        return user;
    }

    public boolean updatePassword(String token,long id,String formPass){
        //取user
        MiaoShaUser user = getById(id);
        if (user == null){
            throw  new  GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoShaUser toBeUpdate = new MiaoShaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoShaUserDao.update(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoShaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoShaUserKey.token,token,user);
        return true;
    }

    public boolean login(HttpServletResponse response,LoginVo loginVo){
        if (loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号是否存在
        MiaoShaUser user = miaoShaUserDao.getById(Long.parseLong(mobile));
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();//从数据库中获取密码
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(password,saltDB);//二次加密后的密码
        if (!calcPass.equals(dbPass)){//如果获取的密码和数据库的密码不一样
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie,到这里已经登陆成功了
        String token = UUIDUtil.uuid();
        addCookie(response,token,user);
        return true;
    }

    public MiaoShaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoShaUser user = redisService.get(MiaoShaUserKey.token,token,MiaoShaUser.class);
        //延长有效期
        if (user != null){
            addCookie(response,token,user);
        }
        return user;
    }

    //实现分布式session
    private void addCookie(HttpServletResponse response,String token,MiaoShaUser user){
        //生成cookie,到这里已经登陆成功了
        redisService.set(MiaoShaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoShaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
