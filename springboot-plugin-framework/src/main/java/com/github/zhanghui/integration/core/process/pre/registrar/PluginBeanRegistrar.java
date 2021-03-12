package com.github.zhanghui.integration.core.process.pre.registrar;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 15:13
 **/
public interface PluginBeanRegistrar {

    /**
     * 处理该插件的注册
     * @throws Exception 处理异常
     */
    void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception;

    /**
     * 处理该插件的卸载
     * @throws Exception 处理异常
     */
    default void unRegistry(PluginRegistryInfo pluginRegistryInfo) throws Exception{}

}
