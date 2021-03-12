package com.github.zhanghui.utils;

/**
 * Description:
 * 字符集工具类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 11:24
 **/
public class StringUtils {

    public static <T> boolean isNull(T t){
        if(t instanceof String){
            return org.apache.commons.lang3.StringUtils.isBlank((String)t);
        }
        return  t == null;
    }

    public static <T> boolean isNotNull(T t){
        if(t instanceof String){
            return org.apache.commons.lang3.StringUtils.isNotBlank((String) t);
        }
        return  t != null;
    }
}
