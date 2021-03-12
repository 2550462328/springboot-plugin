package com.github.zhanghui.integration.core.process.pre;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.OrderPriority;

/**
 * Description:
 * 插件初始化中的一些"装饰"
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 20:16
 **/
public interface PluginPreProcessor {

    /**
     * 初始化
     * @throws Exception 初始化异常
     */
    void initialize();


    /**
     * 处理该插件的注册
     * @param pluginRegistryInfo 插件注册的信息
     * @throws Exception 处理异常
     */
    void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception;


    /**
     * 处理该插件的卸载
     * @param pluginRegistryInfo 插件注册的信息
     * @throws Exception 处理异常
     */
    void unRegistry(PluginRegistryInfo pluginRegistryInfo) throws Exception;

    /**
     * 优先级
     *
     * @return
     */
    default  OrderPriority order(){
        return OrderPriority.getMiddlePriority();
    }
}
