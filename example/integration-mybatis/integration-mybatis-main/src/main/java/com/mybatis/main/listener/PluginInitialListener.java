package com.mybatis.main.listener;

import com.github.zhanghui.integration.listener.PluginInitializerListener;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/1/28 15:45
 **/
@Component
public class PluginInitialListener implements PluginInitializerListener {

    @Override
    public void before() {
        System.out.println("主版本插件初始化---before：" + Thread.currentThread().getName());
    }

    @Override
    public void complete() {
        System.out.println("主版本插件初始化---complete："+Thread.currentThread().getName());
    }

    @Override
    public void failure(Throwable throwable) {
        System.out.println("主版本插件初始化---failure："+Thread.currentThread().getName());
    }
}
