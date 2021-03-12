package com.github.zhanghui.integration.model.plugin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;

/**
 * Description:
 * 插件的相关信息
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 15:25
 **/
@AllArgsConstructor
@Getter
@Setter
public class PluginInfo {

    /**
     * 插件基本信息
     */
    private PluginDescriptor pluginDescriptor;

    /**
     * 插件状态
     */
    private PluginState pluginState;

    /**
     * 插件路径
     */
    private String path;

    /**
     * 运行模式
     */
    private String runMode;
}
