package com.github.zhanghui.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Description:
 * class工具类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/20 10:21
 **/
public class ClassUtils {

    public static Object getReflectionFiled(Object o, String fileName) throws IllegalAccessException{

        if(o == null){
            return null;
        }

        Field findFiled = ReflectionUtils.findField(o.getClass(),fileName);
        return resolveFiled(o,findFiled);
    }

    public static Object resolveFiled(Object o, Field targetField) throws IllegalAccessException{
        if(o == null || targetField == null){
            return null;
        }
        if(!targetField.isAccessible()){
            targetField.setAccessible(true);
        }
        return targetField.get(o);
    }
}
