package com.github.zhanghui.integration.core;

import com.github.zhanghui.extension.AbstractPluginExtension;
import com.github.zhanghui.integration.listener.PluginListenerCapability;
import com.github.zhanghui.integration.core.operator.PluginOperator;
import com.github.zhanghui.integration.core.user.PluginUser;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import org.springframework.context.ApplicationContext;

/**
 * Description:
 * 主框架（负责系统的初始化工作 和 获取当前框架的上下文信息）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:07
 **/
public interface PluginApplication extends PluginListenerCapability {

    /**
     * 主版本上下文初始化
     * @param applicationContext
     * @param listener
     */
    void initialize(ApplicationContext applicationContext, PluginInitializerListener listener);

    PluginUser getPluginUser();

    PluginOperator getPluginOperator();

    /**
     * 添加插件扩展功能
     * @param pluginExtension
     */
    void addPluginExtension(AbstractPluginExtension pluginExtension);
}
