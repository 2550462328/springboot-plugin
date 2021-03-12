package com.github.zhanghui.integration.listener;

/**
 * Description:
 * 插件事件监听器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 9:47
 **/
public interface PluginListener {

    /**
     * 监听插件启用事件
     * @param pluginId
     * @param isStartInitial
     */
    void registry(String pluginId, boolean isStartInitial);

    /**
     * 监听插件停止事件
     * @param pluginId
     */
    void unRegistry(String pluginId);

    /**
     * 监听插件启用或停止失败事件
     * @param pluginId
     * @param throwable
     */
    void failure(String pluginId, Throwable throwable);
}
