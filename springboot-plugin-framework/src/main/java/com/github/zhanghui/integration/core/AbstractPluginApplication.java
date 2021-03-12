package com.github.zhanghui.integration.core;

import com.github.zhanghui.exception.MainContextRuntimeException;
import com.github.zhanghui.extension.AbstractPluginExtension;
import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.operator.PluginOperator;
import com.github.zhanghui.integration.core.user.PluginUser;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import com.github.zhanghui.integration.listener.PluginListener;
import com.github.zhanghui.integration.listener.support.PluginListenerManager;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:18
 **/
public abstract class AbstractPluginApplication implements PluginApplication  {

    protected final PluginListenerManager pluginListenerManager = new PluginListenerManager();

    @Override
    public void addPluginExtension(AbstractPluginExtension pluginExtension) {
        if(pluginExtension != null) {
            PluginExtensionFactory.addPluginExtension(pluginExtension);
        }
    }

    @Override
    public void addListener(PluginListener pluginListener) {
        this.pluginListenerManager.addPluginListener(pluginListener);
    }

    @Override
    public void addListener(Class<? extends PluginListener> clazz) {
        this.pluginListenerManager.addPluginListener(clazz);
    }

    @Override
    public void addListener(List<? extends PluginListener> pluginListeners) {
        if(pluginListeners != null && !pluginListeners.isEmpty()){
            pluginListeners.forEach(pluginListenerManager::addPluginListener);
        }
    }

    protected IntegrationConfiguration getConfiguration(ApplicationContext applicationContext){
        IntegrationConfiguration configuration = applicationContext.getBean(IntegrationConfiguration.class);

        if(configuration == null){
            throw new MainContextRuntimeException("当前并没有配置主版本信息，请先配置");
        }
        return configuration;
    }

    @Override
    public PluginUser getPluginUser() {
        return null;
    }
}
