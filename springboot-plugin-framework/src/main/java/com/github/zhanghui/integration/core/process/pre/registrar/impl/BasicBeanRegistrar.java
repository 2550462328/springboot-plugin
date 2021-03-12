package com.github.zhanghui.integration.core.process.pre.registrar.impl;

import com.github.zhanghui.integration.core.process.pre.classes.group.*;
import com.github.zhanghui.integration.core.process.pre.registrar.AbstractPluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;

import java.util.List;

/**
 * Description:
 * 提供插件中基础bean的spring注册
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 15:55
 **/
public class BasicBeanRegistrar extends AbstractPluginBeanRegistrar {

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        List<Class> components = pluginRegistryInfo.getClassesFromGroup(ComponentGroup.GROUP_ID);
        List<Class> repositorys = pluginRegistryInfo.getClassesFromGroup(RepositoryGroup.GROUP_ID);
        List<Class> oneselfListeners = pluginRegistryInfo.getClassesFromGroup(OneselfListenerGroup.GROUP_ID);
        List<Class> configBeans = pluginRegistryInfo.getClassesFromGroup(ConfigBeanGroup.GROUP_ID);
        doRegister(pluginRegistryInfo, components);
        doRegister(pluginRegistryInfo, repositorys);
        doRegister(pluginRegistryInfo, oneselfListeners);
        doRegister(pluginRegistryInfo, configBeans);
    }

    private void doRegister(PluginRegistryInfo pluginRegistryInfo, List<Class> pluginClasses) {
        if (pluginClasses == null || pluginClasses.isEmpty()) {
            return;
        }
        pluginClasses.forEach(pluginClass -> {
            super.register(pluginRegistryInfo, pluginClass);
        });
    }


}
