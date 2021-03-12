package com.github.zhanghui.integration.configuration.parser;

import com.github.zhanghui.integration.configuration.PluginConfigDefinition;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;

/**
 * Description:
 *  插件配置解析器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 16:43
 **/
public interface ConfigurationParser {

    /**
     * 配置解析
     * @param pluginRegistryInfo 插件信息
     * @param pluginConfigDefinition 插件配置定义
     * @return 解析后映射值的对象
     * @throws Exception 抛出配置解析异常
     */
    Object parse(PluginRegistryInfo pluginRegistryInfo, PluginConfigDefinition pluginConfigDefinition) throws Exception;
}
