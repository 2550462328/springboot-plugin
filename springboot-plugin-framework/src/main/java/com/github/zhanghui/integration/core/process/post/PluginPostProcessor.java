package com.github.zhanghui.integration.core.process.post;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.OrderPriority;

import java.util.List;

/**
 * Description:
 * 处理插件加载运行中的“装饰”
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 9:12
 **/
public interface PluginPostProcessor {

    /**
     * 初始化
     * @throws Exception 初始化异常
     */
    void initialize() throws Exception;


    /**
     * 批量处理插件的注册
     * @param pluginRegistryInfos 插件注册的信息
     * @throws Exception 处理异常
     */
    void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception;


    /**
     * 批量处理插件的卸载
     * @param pluginRegistryInfos 插件注册的信息
     * @throws Exception 处理异常
     */
    void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception;

    /**
     * 执行优先级
     *
     * @return
     */
    default OrderPriority order(){
        return OrderPriority.getMiddlePriority();
    }

}
