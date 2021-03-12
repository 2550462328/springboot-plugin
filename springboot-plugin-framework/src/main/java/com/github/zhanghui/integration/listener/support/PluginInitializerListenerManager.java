package com.github.zhanghui.integration.listener.support;

import com.github.zhanghui.Lifecycle;
import com.github.zhanghui.integration.listener.DefaultPluginInitializerListener;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 15:59
 **/
public class PluginInitializerListenerManager {

    private final List<PluginInitializerListener> pluginInitializerListeners = new ArrayList<>();

    public final ApplicationContext applicationContext;

    public PluginInitializerListenerManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 添加默认的初始化监听者
        pluginInitializerListeners.add(new DefaultPluginInitializerListener(applicationContext));
    }

    public void before() {
        pluginInitializerListeners.forEach(PluginInitializerListener::before);
    }

    public void complete() {
        pluginInitializerListeners.forEach(PluginInitializerListener::complete);
    }

    public void failure(Throwable throwable) {
        pluginInitializerListeners.forEach(pluginInitializerListener -> {
            pluginInitializerListener.failure(throwable);
        });
    }

    /**
     * 添加监听者
     * @param pluginInitializerListener pluginInitializerListener
     */
    public void addPluginInitializerListeners(PluginInitializerListener pluginInitializerListener){
        if(pluginInitializerListener != null){
            pluginInitializerListeners.add(pluginInitializerListener);
        }
    }
}
