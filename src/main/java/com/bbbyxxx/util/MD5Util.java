package com.bbbyxxx.util;

import org.apache.commons.codec.digest.DigestUtils;
import java.net.URLEncoder;

public class MD5Util {
    public static String md5(String src){
        return DigestUtils.md5Hex(URLEncoder.encode(src));
    }

    private static final String salt = "1a2b3c4d";

    //输入到form表单加密
    public static String inputPassFormPass(String inputPass){
        String str = ""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    //form表单到数据库二次加密
    public static String formPassToDBPass(String formPass,String salt){
        String str = ""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDbPass(String input,String saltDB){
        String formPass = inputPassFormPass(input);
        String dbPass = formPassToDBPass(formPass,saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(MD5Util.inputPassToDbPass("123456",salt));
    }
}
