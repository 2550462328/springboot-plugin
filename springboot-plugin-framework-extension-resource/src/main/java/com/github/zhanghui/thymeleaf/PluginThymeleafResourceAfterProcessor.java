package com.github.zhanghui.thymeleaf;

import com.github.zhanghui.extension.PluginAfterPreProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Description:
 * 在每个插件注册的加载或注销时对插件的Thymeleaf资源的处理
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/8 15:09
 **/
@Slf4j
public class PluginThymeleafResourceAfterProcessor implements PluginAfterPreProcessor {

    private static final String PLUGIN_THYMELEAF_TEMPLATE_RESOLVER_KEY = "pluginThymeleafTemplateResolver";

    @Override
    public void initialize() {

    }

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        SpringTemplateEngine springTemplateEngine = getSpringTemplateEngine(pluginRegistryInfo);
        if(springTemplateEngine == null){
            return;
        }
        SpringBootThymeleafConfig springBootThymeleafConfig = getThymeleafConfig(pluginRegistryInfo);
        if(springBootThymeleafConfig == null){
            return;
        }
        ThymeleafConfig thymeleafConfig = new ThymeleafConfig();
        springBootThymeleafConfig.config(thymeleafConfig);
        // 校验用户自定义的thymeleafConfig
        verifyThymeleafConfig(thymeleafConfig);

        ClassLoader pluginClassLoader = pluginRegistryInfo.getDefaultPluginClassLoader();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver(pluginClassLoader);

        // 配置资源解析规则
        templateResolver.setCacheable(thymeleafConfig.isCache());
        templateResolver.setPrefix(thymeleafConfig.getPrefix());
        templateResolver.setSuffix(thymeleafConfig.getSuffix());
        templateResolver.setTemplateMode(thymeleafConfig.getMode());
        if(thymeleafConfig.getEncoding() != null){
            templateResolver.setCharacterEncoding(thymeleafConfig.getEncoding().name());
        }
        if(thymeleafConfig.getTemplateResolverOrder() != null){
            templateResolver.setOrder(thymeleafConfig.getTemplateResolverOrder());
        }
        templateResolver.setCheckExistence(true);

        springTemplateEngine.addTemplateResolver(templateResolver);

        pluginRegistryInfo.addExtension(PLUGIN_THYMELEAF_TEMPLATE_RESOLVER_KEY,templateResolver);
    }

    @Override
    public void unRegistry(PluginRegistryInfo pluginRegistryInfo) throws Exception {

        ClassLoaderTemplateResolver templateResolver = (ClassLoaderTemplateResolver)pluginRegistryInfo.getExtension(PLUGIN_THYMELEAF_TEMPLATE_RESOLVER_KEY);
        if(templateResolver == null){
            return;
        }
        SpringTemplateEngine springTemplateEngine = getSpringTemplateEngine(pluginRegistryInfo);
        Set<ITemplateResolver> springTemplateResolvers = getSpringTemplateResolvers(springTemplateEngine);
        if(springTemplateResolvers != null){
            springTemplateResolvers.remove(templateResolver);
        }
    }

    /**
     * 获取主版本Spring上下文中的Template Engine
     *
     * @param pluginRegistryInfo
     * @return
     */
    private SpringTemplateEngine getSpringTemplateEngine(PluginRegistryInfo pluginRegistryInfo) {
        ApplicationContext mainApplicationContext = pluginRegistryInfo.getMainApplicationContext();
        Map<String, SpringTemplateEngine> springTemplateEngineMap = mainApplicationContext.getBeansOfType(SpringTemplateEngine.class, false, false);
        if (springTemplateEngineMap.size() > 0) {
            return mainApplicationContext.getBean(SpringTemplateEngine.class);
        }
        return null;
    }

    /**
     * 获取插件中对Thymeleaf 的自定义配置
     *
     * @param pluginRegistryInfo
     * @return
     */
    private SpringBootThymeleafConfig getThymeleafConfig(PluginRegistryInfo pluginRegistryInfo) {
        List<Object> configFileObjects = pluginRegistryInfo.getConfigFileObjects();
        for (Object configFileObject : configFileObjects) {

            Class<?>[] interfaces = configFileObject.getClass().getInterfaces();
            if (interfaces.length > 0 && Arrays.asList(interfaces).contains(SpringBootThymeleafConfig.class)) {
                return (SpringBootThymeleafConfig) configFileObject;
            }
        }
        return null;
    }

    /**
     * 校验用户自定义的thymeleafConfig
     *
     * @param thymeleafConfig
     */
    private void verifyThymeleafConfig(ThymeleafConfig thymeleafConfig){
        Objects.requireNonNull(thymeleafConfig.getMode(),"mode不能为空");
        Objects.requireNonNull(thymeleafConfig.getPrefix(),"prefix不能为空");
        Objects.requireNonNull(thymeleafConfig.getSuffix(),"suffix不能为空");

        String prefix = thymeleafConfig.getPrefix();
        if(!prefix.endsWith("/")){
            thymeleafConfig.setPrefix(prefix.concat("/"));
        }
    }

    /**
     * 反射获取SpringTemplateEngine中的templateResolve
     *
     * @param templateEngine
     * @return
     */
    private Set<ITemplateResolver> getSpringTemplateResolvers(SpringTemplateEngine templateEngine){
        Field templateResolvers = ReflectionUtils.findField(templateEngine.getClass(), "templateResolvers");
        if(templateResolvers == null){
            return null;
        }

        if(!templateResolvers.isAccessible()){
            templateResolvers.setAccessible(true);
        }

        try {
            return (Set<ITemplateResolver>)templateResolvers.get(templateEngine);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
 }
