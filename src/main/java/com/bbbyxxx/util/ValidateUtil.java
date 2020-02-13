package com.bbbyxxx.util;


import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
//正则表达式校验手机号格式是否正确
public class ValidateUtil {
    //如果是1开头后面跟了10个数字，就认为是正确的手机号
    //Pattern类用于创建一个正则表达式,也可以说创建一个匹配模式,它的构造方法是私有的,
    // 不可以直接创建,但可以通过Pattern.complie(String regex)简单工厂方法创建一个正则表达式,
    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String src){
        if (StringUtils.isEmpty(src)){
            return false;
        }
        Matcher m = mobile_pattern.matcher(src);//匹配字符串
        return m.matches();//返回是否匹配
    }

    public static void main(String[] args) {
        System.out.println(isMobile("18392668217"));//true
        System.out.println(isMobile("183926682177"));//false
        System.out.println(isMobile("28392668217"));//false
        System.out.println(isMobile("1839266821"));//false
    }
}
