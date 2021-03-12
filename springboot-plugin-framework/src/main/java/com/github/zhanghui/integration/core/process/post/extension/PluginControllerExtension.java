package com.github.zhanghui.integration.core.process.post.extension;

import java.util.List;

/**
 * Description:
 * 对插件Controller在初始化、插件注册和插件注销时候的一些扩展
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/20 10:04
 **/
public interface PluginControllerExtension {

    /**
     * 初始化
     */
    void initialize();

    /**
     * 注册
     * @param pluginId 插件id
     * @param controllerWrappers controller 类集合
     * @throws Exception 异常
     */
    void registry(String pluginId, List<ControllerWrapper> controllerWrappers) throws Exception;

    /**
     * 注册
     * @param pluginId 插件id
     * @param controllerWrappers controller 类集合
     * @throws Exception 异常
     */
    void unRegistry(String pluginId, List<ControllerWrapper> controllerWrappers) throws Exception;
}
