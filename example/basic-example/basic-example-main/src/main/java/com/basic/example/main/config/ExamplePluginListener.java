package com.basic.example.main.config;

import com.github.zhanghui.integration.core.PluginApplication;
import com.github.zhanghui.integration.core.user.PluginUser;
import com.github.zhanghui.integration.listener.PluginListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 插件监听者
 *
 * @author starBlues
 * @version 1.0
 */
public class ExamplePluginListener implements PluginListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PluginUser pluginUser;

    public ExamplePluginListener(PluginApplication pluginApplication){
        this.pluginUser = pluginApplication.getPluginUser();
    }


    @Override
    public void registry(String pluginId, boolean isStartInitial) {
        logger.info("Listener: registry pluginId {}", pluginId);
    }

    @Override
    public void unRegistry(String pluginId) {
        logger.info("Listener: unRegistry pluginId {}", pluginId);
    }

    @Override
    public void failure(String pluginId, Throwable throwable) {

    }
}
