package com.github.zhanghui.integration.core.process.pre.registrar.impl;

import com.github.zhanghui.annotation.ConfigDefinition;
import com.github.zhanghui.integration.configuration.PluginConfigDefinition;
import com.github.zhanghui.integration.configuration.parser.ConfigurationParser;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.configuration.parser.impl.YamlConfigurationParser;
import com.github.zhanghui.integration.core.process.pre.classes.group.ConfigDefinitionGroup;
import com.github.zhanghui.integration.core.process.pre.registrar.AbstractPluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.StringUtils;
import org.pf4j.RuntimeMode;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 * 提供插件配置文件bean的spring注册 （被@ConfigDefinition注释的类）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 16:27
 **/
public class ConfigFileBeanRegistrar extends AbstractPluginBeanRegistrar {

    private final ConfigurationParser configurationParser;
    private final IntegrationConfiguration integrationConfiguration;

    private static final String NULL_CONFIG_FILE_NAME = null;
    private static final String NULL_REGISTER_BEAN = null;

    public ConfigFileBeanRegistrar(ApplicationContext mainApplicationContext) {
        this.integrationConfiguration = mainApplicationContext.getBean(IntegrationConfiguration.class);
        this.configurationParser = new YamlConfigurationParser(integrationConfiguration);
    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        List<Class> configDefinitions = pluginRegistryInfo.getClassesFromGroup(ConfigDefinitionGroup.GROUP_ID);

        for(Class configDefinitionClass : configDefinitions){
            registry(pluginRegistryInfo,configDefinitionClass);
        }
    }

    /**
     * 解析插件配置文件并注册到插件的spring上下文中
     *
     * @param pluginRegistryInfo
     * @param configDefinitionClass
     * @return
     * @throws Exception
     */
    private String registry(PluginRegistryInfo pluginRegistryInfo, Class<?> configDefinitionClass) throws Exception{
        ConfigDefinition configDefinition = configDefinitionClass.getAnnotation(ConfigDefinition.class);
        if(configDefinition == null){
            return NULL_REGISTER_BEAN;
        }
        String fileName = getConfigFileName(configDefinition);
        Object parseObject;
        if(fileName == NULL_CONFIG_FILE_NAME){
            parseObject = configDefinitionClass.newInstance();
        }else{
            PluginConfigDefinition pluginConfigDefinition = new PluginConfigDefinition(fileName,configDefinitionClass);
            parseObject = configurationParser.parse(pluginRegistryInfo,pluginConfigDefinition);
        }

        String beanName = configDefinition.beanName();
        if(StringUtils.isNull(beanName)){
            beanName = configDefinitionClass.getName();
        }
        beanName = beanName.concat("@").concat(pluginRegistryInfo.getPluginWrapper().getPluginId());
        // 注册到插件的Spring上下文中
        super.registerSingleton(beanName,pluginRegistryInfo,parseObject);
        // 注册到插件信息中
        pluginRegistryInfo.addConfigFileObject(parseObject);
        return beanName;
    }

    /**
     * 根据系统环境获取配置文件名
     *
     * @param configDefinition
     * @return
     */
    private String getConfigFileName(ConfigDefinition configDefinition){

        String originFileName = configDefinition.value();

        if(StringUtils.isNull(originFileName)){
            return NULL_CONFIG_FILE_NAME;
        }
        String fileSuffixName = "";
        if(originFileName.lastIndexOf(".") != -1){
            fileSuffixName = ".".concat(originFileName.split("\\.")[1]);
            originFileName = originFileName.split("\\.")[0];
        }

        if(integrationConfiguration.environment() == RuntimeMode.DEVELOPMENT){
            return originFileName.concat(configDefinition.devSuffix())
                    .concat(fileSuffixName);
        }else{
            return originFileName.concat(configDefinition.prodSuffix())
                    .concat(fileSuffixName);
        }
    }
}
