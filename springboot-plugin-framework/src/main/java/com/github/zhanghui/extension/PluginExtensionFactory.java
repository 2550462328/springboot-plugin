package com.github.zhanghui.extension;

import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.utils.CommonUtils;
import com.github.zhanghui.utils.OrderPriority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Description:
 * 插件扩展功能工厂类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/25 20:08
 **/
@Slf4j
public class PluginExtensionFactory {

    private static final List<PluginResourceLoader> RESOURCE_LOADERS_EXTENDS = new ArrayList<>();
    private static final List<PluginBeanRegistrar> BEAN_REGISTRAR_EXTEND = new ArrayList<>();
    private static final List<PluginClassGroup> CLASS_GROUP_EXTENDS = new ArrayList<>();
    private static final List<PluginPreProcessor> PRE_PROCESSOR_EXTENDS = new ArrayList<>();
    private static final List<PluginPostProcessor> POST_PROCESSOR_EXTENDS = new ArrayList<>();
    private static final List<PluginAfterPreProcessor> AFTER_PRE_PROCESSERS_EXTENDS = new ArrayList<>();

    private static AtomicBoolean isPluginExtensionInit = new AtomicBoolean(false);

    /**
     * 插件扩展类容器
     */
    private final static Map<String, AbstractPluginExtension> PLUGIN_EXTENSION_MAP = new HashMap<>();

    /**
     * 初始化
     */
    public static void init(ApplicationContext mainApplicationContext) {
        if (PLUGIN_EXTENSION_MAP.size() == 0) {
            return;
        }
        if (isPluginExtensionInit.get()) {
            log.warn("插件扩展已经全部初始化了，禁止重复初始化");
            return;
        }
        for (AbstractPluginExtension abstractPluginExtension : PLUGIN_EXTENSION_MAP.values()) {
            if (abstractPluginExtension == null) {
                continue;
            }
            try {
                abstractPluginExtension.initialize(mainApplicationContext);
                doInit(abstractPluginExtension, mainApplicationContext);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("插件扩展 [{}] 初始化出现异常：[{}]", abstractPluginExtension.key(), e.getMessage(), e);
            }
        }
        isPluginExtensionInit.set(true);
    }

    /**
     * 针对每个AbstractPluginExtension的初始化
     *
     * @param abstractPluginExtension
     * @param mainApplicationContext
     */
    private static void doInit(AbstractPluginExtension abstractPluginExtension, ApplicationContext mainApplicationContext) {
        iterate(abstractPluginExtension.getPluginBeanRegistrars(mainApplicationContext), null, BEAN_REGISTRAR_EXTEND::add);
        iterate(abstractPluginExtension.getPluginClassGroups(mainApplicationContext), null, CLASS_GROUP_EXTENDS::add);
        iterate(abstractPluginExtension.getPluginResourceLoaders(), PluginResourceLoader::order, RESOURCE_LOADERS_EXTENDS::add);
        iterate(abstractPluginExtension.getPluginPreProcessors(mainApplicationContext), PluginPreProcessor::order, PRE_PROCESSOR_EXTENDS::add);
        iterate(abstractPluginExtension.getPluginPostProcessors(mainApplicationContext), PluginPostProcessor::order, POST_PROCESSOR_EXTENDS::add);
        iterate(abstractPluginExtension.getPluginAfterPreProcessors(mainApplicationContext), PluginAfterPreProcessor::order, AFTER_PRE_PROCESSERS_EXTENDS::add);

        log.info("成功加载插件扩展 [{}]", abstractPluginExtension.key());
    }

    /**
     * 添加扩展类插件
     *
     * @param abstractPluginExtension
     */
    public static void addPluginExtension(AbstractPluginExtension abstractPluginExtension) {
        if (abstractPluginExtension == null) {
            return;
        }
        String pluginExtensionKey = abstractPluginExtension.key();
        if (pluginExtensionKey == null) {
            pluginExtensionKey = abstractPluginExtension.getClass().getCanonicalName();
        }
        PLUGIN_EXTENSION_MAP.put(pluginExtensionKey, abstractPluginExtension);
    }

    public static List<PluginResourceLoader> getResourceLoadersExtends() {
        return RESOURCE_LOADERS_EXTENDS;
    }

    public static List<PluginPreProcessor> getPreProcessorExtends() {
        return PRE_PROCESSOR_EXTENDS;
    }

    public static List<PluginBeanRegistrar> getPluginBeanRegistrarExtends() {
        return BEAN_REGISTRAR_EXTEND;
    }

    public static List<PluginClassGroup> getClassGroupExtends() {
        return CLASS_GROUP_EXTENDS;
    }

    public static List<PluginPostProcessor> getPostProcessorExtends() {
        return POST_PROCESSOR_EXTENDS;
    }

    public static List<PluginAfterPreProcessor> getAfterPreProcessorsExtends() {
        return AFTER_PRE_PROCESSERS_EXTENDS;
    }

    /**
     * 对list的迭代
     *
     * @param list
     * @param orderPriorityFunction
     * @param consumer
     * @param <T>
     */
    private static <T> void iterate(List<T> list, Function<T, OrderPriority> orderPriorityFunction, Consumer<T> consumer) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (orderPriorityFunction != null) {
            list.stream().filter(Objects::nonNull)
                    .sorted(CommonUtils.orderPriority(orderPriorityFunction))
                    .forEach(consumer);
        } else {
            list.stream().filter(Objects::nonNull)
                    .forEach(consumer);
        }
    }
}
