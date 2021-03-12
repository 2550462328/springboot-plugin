package com.mybatis.plugin1.config;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/12 9:43
 **/
@Aspect
@Component
public class AspectConfig {

    @Pointcut("execution(public * com.mybatis.main.service.TestTestTransactional.transactional())")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(){
        System.out.println("成功拦截到切面：transactional");
    }

    @After("pointcut()")
    public void after(){
        System.out.println("拜拜了，切面：transactional");
    }
}
