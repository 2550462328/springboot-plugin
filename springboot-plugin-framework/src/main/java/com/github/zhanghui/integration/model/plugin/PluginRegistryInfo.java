package com.github.zhanghui.integration.model.plugin;

import com.github.zhanghui.exception.PluginExtensionRuntimeException;
import com.github.zhanghui.integration.core.process.post.extension.ControllerWrapper;
import com.github.zhanghui.integration.core.support.PluginContextHelper;
import com.github.zhanghui.realize.ConfigBean;
import com.github.zhanghui.realize.OneselfListener;
import com.github.zhanghui.utils.StringUtils;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.realize.BasePlugin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ClassLoadingStrategy;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 注册插件所必须的信息(需要进行构建)
 * <p>
 * pluginWrapper + pluginManager -> pluginLoadResources -> groupClasses + pluginClasses -> suppierBeanNames -> configBeanList + configFileObjects -> pluginControllerWrappers -> oneselfListeners
 * <p>
 * 插件扩展：extensionMap
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 17:23
 **/
@Slf4j
public class PluginRegistryInfo {

    private final PluginWrapper pluginWrapper;

    /**
     * 插件spring上下文信息
     */
    private final AnnotationConfigApplicationContext pluginApplicationContext;

    private final ApplicationContext mainApplicationContext;

    private final BasePlugin basePlugin;

    private final PluginManager pluginManager;

    /**
     * 存放插件提供的所有通信接口类
     */
    private List<String> suppierBeanNames = Collections.synchronizedList(Lists.newArrayList());
    /**
     * 存储不同插件资源加载器加载的资源
     */
    private final Map<String, ResourceWrapper> pluginLoadResources = new ConcurrentHashMap<>(8);

    /**
     * 对插件里的类进行分组后 装载他们的容器
     * key是分组id
     * value是分组类里的集合
     */
    private final Map<String, List<Class>> groupClasses = new ConcurrentHashMap<>(8);
    /**
     * 插件中的所有类
     */
    private final List<Class<?>> pluginClasses = Lists.newArrayList();
    /**
     * 自定义Bean
     */
    private final List<ConfigBean> configBeanList = Lists.newArrayList();

    /**
     * 插件中的Controller映射包装类
     */
    private final List<ControllerWrapper> pluginControllerWrappers = Lists.newArrayList();

    /**
     * 插件中事件监听器
     */
    private final List<OneselfListener> oneselfListeners = Lists.newArrayList();

    /**
     * 插件的配置文件（被@ConfigDefinition 注解的类）
     */
    private final List<Object> configFileObjects = Lists.newArrayList();

    /**
     * 用于存放插件扩展包中的医院，key是资源的分组名称
     */
    private final Map<String, Object> extensionMap = Maps.newHashMap();

    /**
     * 是否跟随主程序启动而初始化
     */
    private final boolean followingInitial;

    /**
     * 插件提供的类加载器集合，用来缓存不同加载策略的类加载器er
     */
    private Map<ClassLoaderStrategy, PluginClassLoader> pluginClassLoaderMap = Maps.newHashMap();

    private PluginRegistryInfo(PluginWrapper pluginWrapper,
                               PluginManager pluginManager,
                               ApplicationContext mainApplicationContext,
                               boolean followingInitial) {
        this.pluginManager = pluginManager;
        this.pluginWrapper = pluginWrapper;
        this.basePlugin = (BasePlugin) pluginWrapper.getPlugin();
        this.followingInitial = followingInitial;
        this.mainApplicationContext = mainApplicationContext;

        // 生成插件Application
        this.pluginApplicationContext = new AnnotationConfigApplicationContext();
        // 设置插件上下文的类加载环境
        this.pluginApplicationContext.setClassLoader(basePlugin.getWrapper().getPluginClassLoader());
    }

    public static PluginRegistryInfo build(PluginWrapper pluginWrapper,
                                           PluginManager pluginManager,
                                           ApplicationContext mainApplicationContext,
                                           boolean followingInitial) {
        Objects.requireNonNull(pluginWrapper, "构建插件注册信息的插件包装信息不能为空");
        Objects.requireNonNull(pluginManager, "构建插件注册信息的插件管理信息不能为空");
        Objects.requireNonNull(mainApplicationContext, "构建插件注册信息的主版本上下文信息不能为空");
        return new PluginRegistryInfo(pluginWrapper, pluginManager, mainApplicationContext, followingInitial);
    }

    public BasePlugin getBasePlugin() {
        return basePlugin;
    }

    public PluginWrapper getPluginWrapper() {
        return pluginWrapper;
    }

    /**
     * 添加插件中加载的资源
     *
     * @param key             key
     * @param resourceWrapper 资源包装者
     */
    public void addPluginLoadResource(String key, ResourceWrapper resourceWrapper) {
        if (StringUtils.isNull(key)) {
            return;
        }
        if (resourceWrapper == null) {
            return;
        }
        pluginLoadResources.put(key, resourceWrapper);
    }

    /**
     * 获取指定类加载器 加载到的资源信息（类）
     *
     * @param key 类加载器的唯一标识
     * @return
     */
    public ResourceWrapper getPluginLoadResource(String key) {
        return pluginLoadResources.get(key);
    }

    public AnnotationConfigApplicationContext getPluginApplicationContext() {
        return pluginApplicationContext;
    }

    /**
     * 添加插件的supplier bean
     *
     * @param beanName 名称
     */
    public void addSupplierBean(String beanName) {
        if (StringUtils.isNotNull(beanName) && !suppierBeanNames.contains(beanName)) {
            this.suppierBeanNames.add(beanName);
        }
    }

    /**
     * 获取当前插件中的所有Supplier 的beanName
     *
     * @return
     */
    public List<String> getSupplierBeanNames() {
        return this.suppierBeanNames;
    }

    /**
     * 添加类到插件的分组信息中
     *
     * @param groupId 分组id
     * @param aClass  类
     */
    public void addClassInGroup(String groupId, Class<?> aClass) {
        if (StringUtils.isNull(groupId) || aClass == null) {
            return;
        }
        List<Class> classes = groupClasses.computeIfAbsent(groupId, k -> new ArrayList<>());
        classes.add(aClass);
    }

    /**
     * 获取插件中指定分组的类列表
     *
     * @param groupId 分组id
     */
    public List<Class> getClassesFromGroup(String groupId) {
        if (StringUtils.isNull(groupId)) {
            return null;
        }
        return groupClasses.get(groupId);
    }

    /**
     * 默认插件类加载器
     *
     * @return
     */
    public ClassLoader getDefaultPluginClassLoader() {
        return pluginWrapper.getPluginClassLoader();
    }

    /**
     * 根据加载策略获取插件类加载器
     *
     * @param classLoaderStrategy A -- Application P -- Plugin D -- Dependencies
     * @return
     */
    public ClassLoader getPluginClassLoader(ClassLoaderStrategy classLoaderStrategy) {
        if (pluginClassLoaderMap.containsKey(classLoaderStrategy)) {
            return pluginClassLoaderMap.get(classLoaderStrategy);
        }
        ClassLoadingStrategy strategy = null;

        switch (classLoaderStrategy) {
            case ADP:
                strategy = ClassLoadingStrategy.ADP;
                break;
            case APD:
                strategy = ClassLoadingStrategy.APD;
                break;
            case DAP:
                strategy = ClassLoadingStrategy.DAP;
                break;
            case DPA:
                strategy = ClassLoadingStrategy.DPA;
                break;
            case PAD:
                strategy = ClassLoadingStrategy.PAD;
                break;
            case PDA:
                strategy = ClassLoadingStrategy.PDA;
                break;
            default:
                strategy = ClassLoadingStrategy.PDA;
                break;
        }

        PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginManager, pluginWrapper.getDescriptor(), this.getClass().getClassLoader(), strategy);
        pluginClassLoader.addFile(pluginWrapper.getPluginPath().toFile());
        pluginClassLoaderMap.put(classLoaderStrategy,pluginClassLoader);

        return pluginClassLoader;
    }

    /**
     * 添加类到类集合容器
     *
     * @param aClass 类
     */
    public void addPluginClass(Class<?> aClass) {
        if (aClass != null) {
            pluginClasses.add(aClass);
        }
    }

    /**
     * 添加类到类集合容器
     *
     * @param configBeans 待添加的ConfigBean数组
     */
    public void addConfigBean(ConfigBean... configBeans) {
        if (configBeans != null && configBeans.length > 0) {
            for (int i = 0; i < configBeans.length; i++) {
                configBeanList.add(configBeans[i]);
            }
        }
    }

    public boolean isFollowingInitial() {
        return followingInitial;
    }

    /**
     * 获取ConfigBean列表信息
     *
     * @return
     */
    public List<ConfigBean> getConfigBeanList() {
        return configBeanList;
    }

    /**
     * 批量添加Controller包装类
     *
     * @param controllerWrappers
     */
    public void addControllerWrappers(List<ControllerWrapper> controllerWrappers) {
        if (controllerWrappers != null && controllerWrappers.size() > 0) {
            this.pluginControllerWrappers.addAll(controllerWrappers);
        }
    }

    public List<ControllerWrapper> getPluginControllerWrappers() {
        return this.pluginControllerWrappers;
    }

    public void addConfigFileObject(Object configFileObj) {
        if (configFileObj != null) {
            this.configFileObjects.add(configFileObj);
        }
    }

    public List<Object> getConfigFileObjects() {
        return this.configFileObjects;
    }

    /**
     * 批量添加OneSelfListener
     *
     * @param oneselfListenerList
     */
    public void addOneSelfListeners(List<OneselfListener> oneselfListenerList) {
        if (oneselfListenerList != null && oneselfListenerList.size() > 0) {
            this.oneselfListeners.addAll(oneselfListenerList);
        }
    }

    public List<OneselfListener> getOneselfListeners() {
        return this.oneselfListeners;
    }

    public ApplicationContext getMainApplicationContext() {
        return mainApplicationContext;
    }

    /**
     * 添加插件扩展信息
     *
     * @param key
     * @param value
     */
    public void addExtension(String key, Object value) {
        if (extensionMap.containsKey(key)) {
            throw new PluginExtensionRuntimeException("插件扩展信息" + key + "已经存在了，注册失败");
        }
        extensionMap.put(key, value);
    }

    /**
     * 获取插件扩展信息
     *
     * @param key
     */
    public Object getExtension(String key) {
        if (!extensionMap.containsKey(key)) {
            return null;
        }
        return extensionMap.get(key);
    }

    /**
     * 插件停止运行时 进行清除工作
     */
    public void clear() {
        synchronized (this) {
            pluginClasses.clear();
        }
    }

    /**
     * 销毁当前插件信息
     */
    public void destroy() {
        PluginContextHelper.removePluginApplicationContext(getPluginWrapper().getPluginId());

        //关闭插件Spring上下文
        pluginApplicationContext.close();

        // 关闭当前插件下注册的所有PluginClassLoader
        try {
            if (pluginClassLoaderMap.isEmpty()) {
                for (PluginClassLoader pluginClassLoader : pluginClassLoaderMap.values()) {
                    try {
                        pluginClassLoader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            pluginClassLoaderMap.clear();
        }

        // 清除插件资源
        extensionMap.clear();
        configFileObjects.clear();
        groupClasses.clear();
        oneselfListeners.clear();
        pluginClasses.clear();
        pluginControllerWrappers.clear();
        pluginLoadResources.clear();
        configBeanList.clear();
        suppierBeanNames.clear();
    }

    public enum ClassLoaderStrategy {
        APD, ADP, PAD, PDA, DAP, DPA;
    }
}
