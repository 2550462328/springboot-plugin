package com.github.zhanghui;

import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.resolver.PluginResourceResolver;
import com.github.zhanghui.utils.AopUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 10:18
 **/
public class PluginResourceResolvePostProcessor implements PluginPostProcessor {

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for(PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos){

            AopUtils.resolveAop(pluginRegistryInfo);

            try {
                List<Object> configFileObjects = pluginRegistryInfo.getConfigFileObjects();
                SpringBootStaticResourceConfig staticResourceConfig = null;

                for(Object configFileObject : configFileObjects){
                    Class<?>[] interfaces = configFileObject.getClass().getInterfaces();
                    if(interfaces.length > 0 && Arrays.asList(interfaces).contains(SpringBootStaticResourceConfig.class)){
                        staticResourceConfig = (SpringBootStaticResourceConfig) configFileObject;
                    }
                }

                if(staticResourceConfig == null){
                    continue;
                }

                PluginResourceResolver.parse(pluginRegistryInfo,staticResourceConfig);
            } finally {
                AopUtils.recoverAop();
            }
        }
    }

    @Override
    public void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for(PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos){
            PluginResourceResolver.remove(pluginRegistryInfo.getPluginWrapper().getPluginId());
        }
    }
}
