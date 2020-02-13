package com.bbbyxxx.validator;

import com.bbbyxxx.util.ValidateUtil;
import org.thymeleaf.util.StringUtils;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
//ConstraintValidator<isMobile,String>  第一个参数为自定义注解类，第二个参数为所要处理的泛型
public class isMobileValidator implements ConstraintValidator<isMobile,String> {
    private boolean required = false;

    @Override
    public void initialize(isMobile isMobile) {
        required = isMobile.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {//校验方法
        if (required){//如果这个值是必须的
            return ValidateUtil.isMobile(s);
        }else{
            if (StringUtils.isEmpty(s)){
                return true;
            }else {
                return ValidateUtil.isMobile(s);
            }
        }
    }
}
