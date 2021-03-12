package com.github.zhanghui.utils;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.google.common.collect.Lists;
import org.pf4j.PluginWrapper;
import org.springframework.aop.framework.ProxyProcessorSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * Aop相关工具类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/12 10:06
 **/
public class AopUtils {


    private static final List<ProxyWrapper> PROXY_WRAPPER_LIST = Lists.newArrayList();

    /**
     * 是否正在还原代理类的ClassLoader
     */
    private static AtomicBoolean isRecover = new AtomicBoolean(false);

    /**
     * 从mainApplicationContext获取所有的代理对象 并封装成ProxyWrapper
     * <p>
     * 在程序初始化的时候执行一次
     *
     * @param mainApplicationContext
     */
    public synchronized static void registry(ApplicationContext mainApplicationContext) {
        Map<String, ProxyProcessorSupport> proxyProcessorSupportMap = mainApplicationContext.getBeansOfType(ProxyProcessorSupport.class);
        if (proxyProcessorSupportMap == null || proxyProcessorSupportMap.isEmpty()) {
            return;
        }

        for (ProxyProcessorSupport proxyProcessorSupport : proxyProcessorSupportMap.values()) {
            ProxyWrapper proxyWrapper = new ProxyWrapper();
            proxyWrapper.setProxyProcessorSupport(proxyProcessorSupport);
            PROXY_WRAPPER_LIST.add(proxyWrapper);
        }
    }

    /**
     * 提取代理类 替换classLoader
     */
    public synchronized static void resolveAop(PluginRegistryInfo pluginRegistryInfo) {
        if (PROXY_WRAPPER_LIST.isEmpty()) {
            return;
        }

        if (isRecover.get()) {
            throw new IllegalStateException("不能在recover期间执行resolveAop方法");
        }
        isRecover.set(true);
        ClassLoader pluginClassLoader = pluginRegistryInfo.getDefaultPluginClassLoader();

        PROXY_WRAPPER_LIST.forEach(proxyWrapper -> {
            ProxyProcessorSupport proxyProcessorSupport = proxyWrapper.getProxyProcessorSupport();
            ClassLoader classLoader = getClassLoader(proxyProcessorSupport);
            proxyWrapper.setOriginClassLoader(classLoader);
            proxyProcessorSupport.setProxyClassLoader(pluginClassLoader);
        });
    }

    /**
     * 提取代理类 恢复代理类的ClassLoader
     *
     * @return
     */
    public synchronized static void recoverAop() {
        if (PROXY_WRAPPER_LIST.isEmpty()) {
            return;
        }
        PROXY_WRAPPER_LIST.forEach(proxyWrapper -> {
            ProxyProcessorSupport proxyProcessorSupport = proxyWrapper.getProxyProcessorSupport();
            proxyProcessorSupport.setProxyClassLoader(proxyWrapper.getOriginClassLoader());
        });

        // 当前状态为已恢复
        isRecover.set(false);
    }

    /**
     * 获取ProxyProcessorSupport里的proxyClassLoader
     *
     * @param proxyProcessorSupport
     * @return
     */
    private static ClassLoader getClassLoader(ProxyProcessorSupport proxyProcessorSupport) {

        Class aClass = proxyProcessorSupport.getClass();

        while (aClass != null) {

            if (aClass != ProxyProcessorSupport.class) {
                aClass = aClass.getSuperclass();
                continue;
            }

            Field proxyClassLoaderField = ReflectionUtils.findField(aClass, "proxyClassLoader");
            if (!proxyClassLoaderField.isAccessible()) {
                proxyClassLoaderField.setAccessible(true);
            }
            try {
                ClassLoader classLoader = (ClassLoader) proxyClassLoaderField.get(proxyProcessorSupport);
                return classLoader;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * 代理包装类
     */
    public static class ProxyWrapper {

        ProxyProcessorSupport proxyProcessorSupport;
        ClassLoader originClassLoader;

        public ProxyProcessorSupport getProxyProcessorSupport() {
            return proxyProcessorSupport;
        }

        public void setProxyProcessorSupport(ProxyProcessorSupport proxyProcessorSupport) {
            this.proxyProcessorSupport = proxyProcessorSupport;
        }

        public ClassLoader getOriginClassLoader() {
            return originClassLoader;
        }

        public void setOriginClassLoader(ClassLoader originClassLoader) {
            this.originClassLoader = originClassLoader;
        }

        @Override
        public String toString() {
            return "ProxyWrapper{" +
                    "proxyProcessorSupport=" + proxyProcessorSupport +
                    ", originClassLoader=" + originClassLoader +
                    '}';
        }
    }
}
