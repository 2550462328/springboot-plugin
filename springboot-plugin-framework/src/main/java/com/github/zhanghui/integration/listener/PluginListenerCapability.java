package com.github.zhanghui.integration.listener;

import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 9:40
 **/
public interface PluginListenerCapability {

    void addListener(PluginListener pluginListener);

    void addListener(Class<? extends PluginListener> clazz);

    void addListener(List<? extends PluginListener> pluginListeners);
}
