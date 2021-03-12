package com.github.zhanghui.integration.core.process.post;

import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.integration.core.process.post.impl.PluginControllerPostProcessor;
import com.github.zhanghui.integration.core.process.post.impl.PluginInvokeBeanPostProcessor;
import com.github.zhanghui.integration.core.process.post.impl.PluginOneselfListenerPostProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.AopUtils;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 * 插件环境在准备阶段后 在运行时前 需要处理的事情
 * 如果有需要扩展的可以实现PluginPostProcessor接口来扩展
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 9:27
 * @see PluginPostProcessor
 **/
public class PluginPostProcessorManager {

    private final ApplicationContext mainApplicationContext;

    private final List<PluginPostProcessor> pluginPostProcessors = Lists.newArrayList();

    public PluginPostProcessorManager(ApplicationContext mainApplicationContext) {
        this.mainApplicationContext = mainApplicationContext;
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    public void initialize() throws Exception {
        pluginPostProcessors.add(new PluginControllerPostProcessor(mainApplicationContext));
        pluginPostProcessors.add(new PluginInvokeBeanPostProcessor());
        pluginPostProcessors.add(new PluginOneselfListenerPostProcessor());

        //添加插件扩展类中针对所有插件加载后的处理器
        pluginPostProcessors.addAll(PluginExtensionFactory.getPostProcessorExtends());
        for (PluginPostProcessor pluginPostProcessor : pluginPostProcessors) {
            pluginPostProcessor.initialize();
        }
    }

    /**
     * 插件批量注册
     *
     * @param pluginRegistryInfos
     */
    public void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for(PluginPostProcessor pluginPostProcessor : pluginPostProcessors){
            pluginPostProcessor.registry(pluginRegistryInfos);
        }
    }

    /**
     * 插件批量注销
     *
     * @param pluginRegistryInfos
     */
    public void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for(PluginPostProcessor pluginPostProcessor : pluginPostProcessors){
            pluginPostProcessor.unRegistry(pluginRegistryInfos);
        }
    }
}
