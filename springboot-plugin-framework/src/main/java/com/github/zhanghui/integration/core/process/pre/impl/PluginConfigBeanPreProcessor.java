package com.github.zhanghui.integration.core.process.pre.impl;

import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.ConfigBean;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Description:
 * 自定义注入插件spring上下的规则 这里规定了继承ConfigBean接口的类自动注入到Spring中
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 10:50
 **/
@Slf4j
public class PluginConfigBeanPreProcessor implements PluginPreProcessor {

    @Override
    public void initialize()  {

    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) {
        Map<String, ConfigBean> configBeanMap = pluginRegistryInfo.getPluginApplicationContext().getBeansOfType(ConfigBean.class);
        if(configBeanMap == null || configBeanMap.isEmpty()){
            return;
        }
        List<ConfigBean> configBeans = Lists.newArrayList(configBeanMap.values());

        configBeans.forEach(configBean -> {
            try {
                configBean.initialize();
                pluginRegistryInfo.addConfigBean(configBean);
            } catch (Exception e) {
                log.error("初始化 configBean [{}] 失败：[{}]",configBean.getClass().getName(),e.getMessage(),e);
            }
        });

    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo)  {
        List<ConfigBean> configBeanList = pluginRegistryInfo.getConfigBeanList();
        if(configBeanList != null && configBeanList.size() > 0){
            return;
        }
        configBeanList.forEach(configBean -> {
            try {
                configBean.destroy();
            } catch (Exception e) {
                log.error("销毁 configBean [{}] 失败：[{}]",configBean.getClass().getName(),e.getMessage(),e);
            }
        });
    }
}
