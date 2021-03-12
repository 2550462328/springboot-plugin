package com.github.zhanghui.integration.core.process.pre.impl;

import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.utils.CommonUtils;
import com.github.zhanghui.utils.OrderPriority;
import com.github.zhanghui.utils.StringUtils;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.core.process.pre.loader.DefaultPluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Description:
 * 插件资源加载器（加载类）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 10:42
 **/
@Slf4j
public class PluginResourceLoaderPreProcessor implements PluginPreProcessor {

    private final List<PluginResourceLoader> resourceLoaderList = Lists.newArrayList();

    public PluginResourceLoaderPreProcessor() {
        resourceLoaderList.add(new DefaultPluginResourceLoader());
        //添加插件扩展类中的资源加载器
        resourceLoaderList.addAll(PluginExtensionFactory.getResourceLoadersExtends());
        
        CommonUtils.sortWithDES(resourceLoaderList,pluginResourceLoader -> {
            if(pluginResourceLoader.order() == null){
                return OrderPriority.getMiddlePriority().getPriority();
            }
            return pluginResourceLoader.order().getPriority();
        });
    }

    @Override
    public void initialize() {
    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) {
        resourceLoaderList.forEach(resourceLoader ->{
            if(StringUtils.isNull(resourceLoader.key())){
                log.warn("插件加载器 [{}] 未配置key，直接跳过",resourceLoader.getClass().getName());
                return;
            }
            try {
                ResourceWrapper resourceWrapper = resourceLoader.load(pluginRegistryInfo);
                if(resourceWrapper != null) {
                    pluginRegistryInfo.addPluginLoadResource(resourceLoader.key(), resourceWrapper);
                }
            } catch (IOException e) {
                log.error("插件加载器 [{}] 加载插件 [{}] 异常",resourceLoader.getClass().getName(),pluginRegistryInfo.getPluginWrapper().getPluginId());
            }
        });
    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) {
        resourceLoaderList.forEach(resourceLoader ->{

            String key = resourceLoader.key();
            if(StringUtils.isNull(key)){
                log.warn("插件加载器 [{}] 未配置key，直接跳过",resourceLoader.getClass().getName());
            }

            ResourceWrapper resourceWrapper = pluginRegistryInfo.getPluginLoadResource(key);
            if(resourceWrapper == null){
                return;
            }
            try {
                resourceLoader.unload(pluginRegistryInfo,resourceWrapper);
            } catch (Exception e) {
                log.error("插件加载器 [{}] 加载插件 [{}] 异常",resourceLoader.getClass().getName(),pluginRegistryInfo.getPluginWrapper().getPluginId());
            }

        });
    }
}
