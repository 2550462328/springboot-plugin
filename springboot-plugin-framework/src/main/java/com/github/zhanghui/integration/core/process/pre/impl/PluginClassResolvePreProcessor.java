package com.github.zhanghui.integration.core.process.pre.impl;

import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.integration.core.process.pre.classes.group.*;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.StringUtils;
import com.google.common.collect.Lists;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Set;

/**
 * Description:
 * 插件类处理器（分类处理）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 10:43
 **/
public class PluginClassResolvePreProcessor implements PluginPreProcessor {

    List<PluginClassGroup> pluginClassGroups = Lists.newArrayList();

    /**
     * 没有匹配任何类分组的情况下装载类的分组id
     */
    private static final String UNMATCH_GROUP_ID = "unmatch";

    @Override
    public void initialize() {
        pluginClassGroups.add(new ComponentGroup());
        pluginClassGroups.add(new CallerGroup());
        pluginClassGroups.add(new ConfigBeanGroup());
        pluginClassGroups.add(new ConfigDefinitionGroup());
        pluginClassGroups.add(new ControllerGroup());
        pluginClassGroups.add(new OneselfListenerGroup());
        pluginClassGroups.add(new RepositoryGroup());
        pluginClassGroups.add(new SupplierGroup());

        // 添加插件扩展类的类分组器
        pluginClassGroups.addAll(PluginExtensionFactory.getClassGroupExtends());

    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws ClassNotFoundException {
        BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
        ResourceWrapper resourceWrapper = pluginRegistryInfo.getPluginLoadResource(PluginResourceLoader.DEFAULT_PLUGIN_RESOURCE_LOADER_KEY);
        // 插件里没有class
        if (resourceWrapper == null) {
            return;
        }
        List<Resource> resources = resourceWrapper.getResources();
        // 插件没有配置文件（插件属性）
        if (resources == null) {
            return;
        }

        pluginClassGroups.forEach(pluginClassGroup -> {
            pluginClassGroup.initialize(basePlugin);
        });

        Set<String> pluginPackageClasses = resourceWrapper.getClassPackageNames();
        ClassLoader pluginClassLoader = basePlugin.getWrapper().getPluginClassLoader();

        for (String className : pluginPackageClasses) {
            Class pluginClass = Class.forName(className, false, pluginClassLoader);
            if (pluginClass == null) {
                continue;
            }
            boolean isMatchGroup = false;

            for (PluginClassGroup pluginClassGroup : pluginClassGroups) {
                if (pluginClassGroup == null || StringUtils.isNull(pluginClassGroup.groupId())) {
                    return;
                }
                if (pluginClassGroup.filter(pluginClass)) {
                    pluginRegistryInfo.addClassInGroup(pluginClassGroup.groupId(), pluginClass);
                    isMatchGroup = true;
                }
            }
            if(!isMatchGroup){
                pluginRegistryInfo.addClassInGroup(UNMATCH_GROUP_ID, pluginClass);
            }
            //添加进容器中
            pluginRegistryInfo.addPluginClass(pluginClass);
        }


    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) {
        pluginRegistryInfo.clear();
    }
}
