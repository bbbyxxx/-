package com.bbbyxxx.exception;

import com.bbbyxxx.result.CodeMsg;
import com.bbbyxxx.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
@ControllerAdvice，是Spring3.2提供的新注解,它是一个Controller增强器,
可对controller中被 @RequestMapping注解的方法加一些逻辑处理。最常用的就是异常处理
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    /*
    统一异常处理
        需要配合@ExceptionHandler使用。
        当将异常抛到controller时,可以对异常进行统一处理,规定返回的json格式或是跳转到一个错误页面
     */
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        if (e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getCm());
        }else if (e instanceof BindException){//如果是绑定异常
            //在valid校验中，如果校验不通过，会产生BindException异常，捕捉到异常后可以获取到defaultMessage也就是自定义注解中定义的内容
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();//获取所有的错误
            ObjectError error = errors.get(0);//这里只得到第一个
            //获取异常信息
            String msg = error.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        }else{//如果不是绑定异常
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
