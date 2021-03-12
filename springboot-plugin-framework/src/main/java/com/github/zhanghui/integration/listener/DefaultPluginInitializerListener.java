package com.github.zhanghui.integration.listener;

import com.github.zhanghui.extension.PluginExtensionFactory;
import org.springframework.context.ApplicationContext;

/**
 * Description:
 * 缺省系统初始化事件监听器
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 16:23
 **/
public class DefaultPluginInitializerListener implements PluginInitializerListener {

    private final ApplicationContext applicationContext;

    public DefaultPluginInitializerListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void before() {
        PluginExtensionFactory.init(applicationContext);
    }

    @Override
    public void complete() {
        // Nothing
    }

    @Override
    public void failure(Throwable throwable) {
        // Nothing
    }
}
