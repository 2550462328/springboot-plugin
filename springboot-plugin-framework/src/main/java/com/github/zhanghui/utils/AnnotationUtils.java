package com.github.zhanghui.utils;

import java.lang.annotation.Annotation;

/**
 * Description:
 * 注解相关工具类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 12:46
 **/
public class AnnotationUtils {

    public static  boolean  hasAnnotation(Class<?> clazz, boolean isAllMatch, Class<? extends Annotation> ...annotationClasses){

        if(clazz == null || annotationClasses == null){
            return false;
        }

        for(Class<? extends Annotation> annotationClass : annotationClasses){

            Annotation annotation = clazz.getAnnotation(annotationClass);

            if(isAllMatch){
                if(annotation == null){
                    return false;
                }
            }else{
                if(annotation != null){
                    return true;
                }
            }
        }

        return isAllMatch;
    }
}
