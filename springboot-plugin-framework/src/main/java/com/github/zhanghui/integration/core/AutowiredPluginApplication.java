package com.github.zhanghui.integration.core;

import com.github.zhanghui.integration.listener.PluginInitializerListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Description:
 * 自动装配 插件的环境
 * 一键启动
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/22 17:02
 **/
public class AutowiredPluginApplication extends DefaultPluginApplication implements PluginApplication, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private PluginInitializerListener pluginInitializerListener;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.initialize(applicationContext,pluginInitializerListener);
    }

    public void setPluginInitializerListener(PluginInitializerListener pluginInitializerListener) {
        this.pluginInitializerListener = pluginInitializerListener;
    }
}
