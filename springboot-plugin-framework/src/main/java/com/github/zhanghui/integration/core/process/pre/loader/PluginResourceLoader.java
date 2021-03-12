package com.github.zhanghui.integration.core.process.pre.loader;

import com.github.zhanghui.utils.OrderPriority;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;

import java.io.IOException;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/6 10:49
 **/
public interface PluginResourceLoader {

    String DEFAULT_PLUGIN_RESOURCE_LOADER_KEY = "defaultPluginResourceLoader";

    /**
     * 加载者的key
     * @return String
     */
    String key();

    /**
     * 根据指定的插件信息加载资源
     * @param pluginRegistryInfo
     * @return
     */
    ResourceWrapper load(PluginRegistryInfo pluginRegistryInfo) throws IOException;

    /**
     * 卸载指定插件的资源信息
     * @param pluginRegistryInfo
     * @param resourceWrapper
     */
    void unload(PluginRegistryInfo pluginRegistryInfo, ResourceWrapper resourceWrapper);

    /**
     * 插件资源信息加载器的优先级
     * @return
     */
    OrderPriority order();
}
