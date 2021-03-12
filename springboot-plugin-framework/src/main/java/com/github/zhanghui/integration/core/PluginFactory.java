package com.github.zhanghui.integration.core;

import com.github.zhanghui.integration.listener.PluginListenerCapability;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;

/**
 * Description:
 * 控制单个插件的生命周期（启用、停止、装载和卸载）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 17:21
 **/
public interface PluginFactory extends PluginListenerCapability {

    /**
     * 工厂初始化
     * @throws Exception 初始化异常
     */
    void initialize() throws Exception;


    /**
     * 注册插件。
     * @param pluginRegistryInfo 插件注册信息
     * @return 插件工厂
     * @throws Exception 插件工厂异常
     */
    PluginFactory registry(PluginRegistryInfo pluginRegistryInfo) throws Exception;


    /**
     * 注销插件。
     * @param pluginId 插件id
     * @return 插件工厂
     * @throws Exception 插件工厂异常
     */
    PluginFactory unRegistry(String pluginId) throws Exception;


    /**
     * 注册或者注销后的构建调用
     * @throws Exception 插件工厂异常
     */
    void build() throws Exception;


}
