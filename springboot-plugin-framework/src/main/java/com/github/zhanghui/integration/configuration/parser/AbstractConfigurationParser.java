package com.github.zhanghui.integration.configuration.parser;

import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.configuration.PluginConfigDefinition;
import com.github.zhanghui.integration.core.process.pre.loader.PluginConfigFileResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 16:49
 **/
public abstract class AbstractConfigurationParser implements ConfigurationParser {

    private final IntegrationConfiguration integrationConfiguration;

    private static final Object NO_RESOURCE = null;

    public AbstractConfigurationParser(IntegrationConfiguration integrationConfiguration) {
        Objects.requireNonNull(integrationConfiguration, "主版本配置信息不能为空");
        this.integrationConfiguration = integrationConfiguration;
    }

    @Override
    public Object parse(PluginRegistryInfo pluginRegistryInfo, PluginConfigDefinition pluginConfigDefinition) throws Exception {
        String pluginConfigFileName = pluginConfigDefinition.getFileName();
        Class configDefinitionClass = pluginConfigDefinition.getConfigClass();

        PluginResourceLoader pluginResourceLoader = new PluginConfigFileResourceLoader(
                integrationConfiguration.pluginConfigFilePath(), pluginConfigFileName
        );
        ResourceWrapper resourceWrapper = pluginResourceLoader.load(pluginRegistryInfo);
        if(resourceWrapper == null || resourceWrapper.getResources().size() ==0 ){
            return NO_RESOURCE;
        }
        // 交由子类实现
        Object parseObject = parse(resourceWrapper.getResources().get(0),configDefinitionClass);

        if(parseObject == null){
            return configDefinitionClass.newInstance();
        }

        return parseObject;
    }

    protected abstract Object parse(Resource resource,Class configDefinitionClass) throws Exception;

}
