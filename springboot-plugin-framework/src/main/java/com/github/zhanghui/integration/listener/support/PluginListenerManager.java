package com.github.zhanghui.integration.listener.support;

import com.github.zhanghui.integration.listener.PluginListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:20
 **/
public class PluginListenerManager {

    private final List<PluginListener> pluginListeners = new ArrayList<>();

    private final List<Class<? extends PluginListener>> pluginListenerClasses = Collections.synchronizedList(new ArrayList<>());

    /**
     * pluginListenerClasses是否已经注入到主版本的Spring上下文
     */
    private boolean isPluginListenerClassesInjected = false;

    public void addPluginListener(PluginListener pluginListener) {
        if (pluginListener != null) {
            pluginListeners.add(pluginListener);
        }
    }

    public void addPluginListener(Class<? extends PluginListener> pluginListenerClass){
        if(pluginListenerClass != null){
            pluginListenerClasses.add(pluginListenerClass);
        }
    }

    /**
     * 插件注册事件
     *
     * @param pluginId 插件id
     * @param isInitialize 是否是程序初始化的时候发生的注册事件
     */
    public void registry(String pluginId, boolean isInitialize){
        for(PluginListener pluginListener : pluginListeners){
            pluginListener.registry(pluginId,isInitialize);
        }
    }

    /**
     * 插件注销事件
     *
     * @param pluginId 插件id
     */
    public void unRegistry(String pluginId) {
        for(PluginListener pluginListener : pluginListeners){
            pluginListener.unRegistry(pluginId);
        }
    }

    /**
     * 插件异常事件
     *
     * @param pluginId 插件id
     * @param throwable 异常信息
     */
    public void failure(String pluginId, Throwable throwable) {
        for(PluginListener pluginListener : pluginListeners){
            pluginListener.failure(pluginId,throwable);
        }
    }

    /**
     * 将pluginListenerClasses 注入到主版本的Spring容器中
     */
    public void injectPluginListenerClasses(GenericApplicationContext mainApplicationContext){
        if(pluginListenerClasses.isEmpty() || isPluginListenerClassesInjected){
            return;
        }

        synchronized (pluginListenerClasses) {
            for (Class<? extends PluginListener> pluginListenerClass : pluginListenerClasses) {
                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(pluginListenerClass).getBeanDefinition();
                mainApplicationContext.registerBeanDefinition(pluginListenerClass.getName(),beanDefinition);
                PluginListener pluginListenerBean = mainApplicationContext.getBean(pluginListenerClass);
                this.addPluginListener(pluginListenerBean);
            }
            pluginListenerClasses.clear();
            isPluginListenerClassesInjected = true;
        }
    }
}
