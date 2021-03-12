package com.github.zhanghui.integration.core.process.pre.impl;

import com.github.zhanghui.extension.PluginExtensionFactory;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.integration.core.process.pre.registrar.impl.BasicBeanRegistrar;
import com.github.zhanghui.integration.core.process.pre.registrar.impl.ConfigFileBeanRegistrar;
import com.github.zhanghui.integration.core.process.pre.registrar.impl.InvokeBeanRegistrar;
import com.github.zhanghui.integration.core.support.PluginContextHelper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.PluginUtils;
import com.github.zhanghui.utils.OrderPriority;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * Description:
 * 插件上下文信息准备
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/5 10:49
 **/
@Slf4j
public class PluginApplicationContextPreProcessor implements PluginPreProcessor {

    private final ApplicationContext mainApplicationContext;

    private final List<PluginBeanRegistrar> pluginBeanRegistrars = Lists.newArrayList();

    public PluginApplicationContextPreProcessor(ApplicationContext mainApplicationContext) {
        this.mainApplicationContext = mainApplicationContext;
    }

    @Override
    public void initialize()  {
        pluginBeanRegistrars.add(new BasicBeanRegistrar());
        pluginBeanRegistrars.add(new ConfigFileBeanRegistrar(mainApplicationContext));
        pluginBeanRegistrars.add(new InvokeBeanRegistrar());
        // 添加插件扩展类的 Bean注册器
        pluginBeanRegistrars.addAll(PluginExtensionFactory.getPluginBeanRegistrarExtends());
    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        for(PluginBeanRegistrar pluginBeanRegistrar : pluginBeanRegistrars){
            pluginBeanRegistrar.registry(pluginRegistryInfo);
        }

        addPluginExtension(pluginRegistryInfo);
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();

        ClassLoader mainContextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader pluginContextClassLoader = pluginRegistryInfo.getDefaultPluginClassLoader();

        try{
            Thread.currentThread().setContextClassLoader(pluginContextClassLoader);
            // 刷新插件spring上下文环境
            pluginApplicationContext.refresh();
        }finally {
            Thread.currentThread().setContextClassLoader(mainContextClassLoader);
        }
        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
        PluginContextHelper.addPluginApplicationContext(pluginId,pluginApplicationContext);
    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        for(PluginBeanRegistrar pluginBeanRegistrar : pluginBeanRegistrars){
            try {
                pluginBeanRegistrar.unRegistry(pluginRegistryInfo);
            } catch (Exception e) {
                log.error("在 [{}] 中 注销插件 [{}] 失败",pluginBeanRegistrar.getClass().getName(),pluginRegistryInfo.getPluginWrapper().getPluginId());
            }
        }
    }

    /**
     * 向插件的spring上下文中添加扩展Bean（合成Bean）
     * @param pluginRegistryInfo
     */
    private void addPluginExtension(PluginRegistryInfo pluginRegistryInfo){
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
        PluginUtils pluginUtils = new PluginUtils(mainApplicationContext,pluginApplicationContext,pluginRegistryInfo.getPluginWrapper().getDescriptor());

        String name = pluginUtils.getClass().getName();
        // 这里注入了PluginUtils
        pluginApplicationContext.getBeanFactory().registerSingleton(name,pluginUtils);
        log.debug("成功向 插件[{}] 中注入PluginUtils",pluginRegistryInfo.getPluginWrapper().getPluginId());
    }
}
