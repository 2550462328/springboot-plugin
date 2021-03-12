package com.mybatis.main.listener;

import com.github.zhanghui.integration.listener.PluginListener;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/1/28 15:47
 **/
public class PluginEventListener1 implements PluginListener {

    @Override
    public void registry(String pluginId, boolean isStartInitial) {
        System.out.println("PluginEventListener1监听新插件注册事件，" + pluginId + "," + isStartInitial + " : " + Thread.currentThread().getName());
    }

    @Override
    public void unRegistry(String pluginId) {
        System.out.println("PluginEventListener1监听新插件取消注册事件，" + pluginId  + " : " + Thread.currentThread().getName());
    }

    @Override
    public void failure(String pluginId, Throwable throwable) {
        System.out.println("PluginEventListener1监听新插件处理失败事件，" + pluginId  + " : " + Thread.currentThread().getName() + " :"  + throwable.getMessage());
    }
}
