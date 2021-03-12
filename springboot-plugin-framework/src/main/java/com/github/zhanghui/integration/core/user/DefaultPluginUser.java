package com.github.zhanghui.integration.core.user;

import com.github.zhanghui.integration.core.support.PluginContextHelper;
import com.google.common.collect.Lists;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Description:
 * 对插件信息的获取
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 15:30
 **/
public class DefaultPluginUser implements PluginUser {

    protected final ApplicationContext mainApplicationContext;

    protected final PluginManager pluginManager;

    public DefaultPluginUser(ApplicationContext mainApplicationContext, PluginManager pluginManager) {
        Objects.requireNonNull(mainApplicationContext, "主环境上下文配置不能为空！");
        Objects.requireNonNull(pluginManager, "插件管理配置不能为空");
        this.mainApplicationContext = mainApplicationContext;
        this.pluginManager = pluginManager;
    }

    @Override
    public <T> T getBean(String name) {
        return getBean(name, true);
    }

    @Override
    public <T> T getBean(Class<T> aClass) {
        return getBean(aClass, true);
    }

    @Override
    public <T> T getPluginBean(String name) {
        return getBean(name, false);
    }

    @Override
    public <T> List<T> getMainBeans(Class<T> aClass) {
        return getBeans(aClass, 1);
    }

    @Override
    public <T> List<T> getPluginBeans(Class<T> aClass) {
        return getBeans(aClass, 2);
    }

    @Override
    public <T> List<T> getBeans(Class<T> aClass) {
        return getBeans(aClass, 3);
    }

    @Override
    public <T> List<T> getPluginBeans(String pluginId, Class<T> aClass) {

        GenericApplicationContext toResolveContext = PluginContextHelper.getPluginApplicationContext(pluginId);
        Map<String, T> beanMap = toResolveContext.getBeansOfType(aClass);
        if (CollectionUtils.isEmpty(beanMap)) {
            return null;
        }
        return (List<T>)beanMap.values();
    }

    @Override
    public <T> List<T> getPluginExtensions(Class<T> tClass) {
        return pluginManager.getExtensions(tClass);
    }

    private <T> T getBean(String beanName, boolean isNeedMainContext) {
        List<ApplicationContext> pluginContexts = PluginContextHelper.getPluginApplicationContexts();

        if (isNeedMainContext) {
            pluginContexts.add(mainApplicationContext);
        }

        Optional<ApplicationContext> optional = pluginContexts.stream().filter(pluginContext -> pluginContext.getBean(beanName) != null)
                .findFirst();

        return optional.isPresent() ? (T) (optional.get().getBean(beanName)) : null;
    }

    private <T> T getBean(Class<?> beanClass, boolean ifNeedMainContext) {
        List<ApplicationContext> pluginContexts = PluginContextHelper.getPluginApplicationContexts();

        if (ifNeedMainContext) {
            pluginContexts.add(mainApplicationContext);
        }

        Optional<ApplicationContext> optional = pluginContexts.stream().filter(pluginContext -> pluginContext.getBean(beanClass) != null)
                .findFirst();


        return optional.isPresent() ? (T) (optional.get().getBean(beanClass)) : null;
    }

    private <T> List<T> getBeans(Class<?> beanClass, int acquireType) {
        List<ApplicationContext> toResolveContexts = Lists.newArrayList();
        switch (acquireType) {
            case 1:
                toResolveContexts.add(mainApplicationContext);
                break;
            case 2:
                toResolveContexts.addAll(PluginContextHelper.getPluginApplicationContexts());
                break;
            case 3:
                toResolveContexts.add(mainApplicationContext);
                toResolveContexts.addAll(PluginContextHelper.getPluginApplicationContexts());
                break;
            default:
                throw new PluginRuntimeException("未识别的环境配置类型：[" + acquireType + "]");

        }
        List<T> beanList = Lists.newArrayList();

        toResolveContexts.stream().map(applicationContext -> {
            Map<String, T> beanMap = (Map<String, T>) applicationContext.getBeansOfType(beanClass);
            if (CollectionUtils.isEmpty(beanMap)) {
                return null;
            }
            return beanMap.values();
        }).filter(Collection::isEmpty).forEach(beanList::addAll);

        return beanList;
    }
}
