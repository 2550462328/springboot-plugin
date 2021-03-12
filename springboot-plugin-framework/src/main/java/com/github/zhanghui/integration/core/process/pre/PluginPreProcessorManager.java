package com.github.zhanghui.integration.core.process.pre;

import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.integration.core.process.pre.impl.PluginApplicationContextPreProcessor;
import com.github.zhanghui.integration.core.process.pre.impl.PluginClassResolvePreProcessor;
import com.github.zhanghui.integration.core.process.pre.impl.PluginConfigBeanPreProcessor;
import com.github.zhanghui.integration.core.process.pre.impl.PluginResourceLoaderPreProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.AopUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description:
 * 管理PluginPreProcessor的容器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 9:27
 **/
@Slf4j
public class PluginPreProcessorManager {

    private final ApplicationContext mainApplicationContext;

    private List<PluginPreProcessor> pluginPreProcessors = Collections.synchronizedList(new ArrayList<>());

    public PluginPreProcessorManager(ApplicationContext mainApplicationContext) {
        this.mainApplicationContext = mainApplicationContext;
    }

    public void initialize(){
        pluginPreProcessors.add(new PluginResourceLoaderPreProcessor());
        pluginPreProcessors.add(new PluginClassResolvePreProcessor());
        // 插件扩展类前置处理
        pluginPreProcessors.addAll(PluginExtensionFactory.getPreProcessorExtends());
        pluginPreProcessors.add(new PluginApplicationContextPreProcessor(mainApplicationContext));
        pluginPreProcessors.add(new PluginConfigBeanPreProcessor());
        // 插件扩展类后置处理
        pluginPreProcessors.addAll(PluginExtensionFactory.getAfterPreProcessorsExtends());

        pluginPreProcessors.forEach(PluginPreProcessor::initialize);
    }

    public void registry(PluginRegistryInfo pluginRegistryInfo){
        try {
            AopUtils.resolveAop(pluginRegistryInfo);
            pluginPreProcessors.forEach(pluginPreProcessor -> {
                try {
                    pluginPreProcessor.registry(pluginRegistryInfo);
                } catch (Exception e) {
                    log.error("注册插件 [{}] 出现异常：[{}]",pluginRegistryInfo.getPluginWrapper().getPluginId(),e.getMessage(),e );
                    throw new PluginContextRuntimeException(e.getMessage());
                }
            });
        } finally {
            AopUtils.recoverAop();
        }
    }

    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) {
        pluginPreProcessors.forEach(pluginPreProcessor -> {
            try {
                pluginPreProcessor.unRegistry(pluginRegistryInfo);
            } catch (Exception e) {
                log.error("注销插件 [{}] 失败 ：[{}]",pluginRegistryInfo.getPluginWrapper().getPluginId(),e.getMessage(),e);
                throw new PluginContextRuntimeException(e.getMessage());
            }
        });
    }
}
