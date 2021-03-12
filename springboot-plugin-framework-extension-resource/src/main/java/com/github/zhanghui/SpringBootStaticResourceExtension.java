package com.github.zhanghui;

import com.github.zhanghui.extension.AbstractPluginExtension;
import com.github.zhanghui.extension.PluginAfterPreProcessor;
import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.resolver.ResourceWebMvcConfigurer;
import com.github.zhanghui.thymeleaf.PluginThymeleafResourceAfterProcessor;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Description:
 * 插件静态资源扩展加载类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/5 16:48
 **/
public class SpringBootStaticResourceExtension extends AbstractPluginExtension {

    private final static String STATIC_RESOURCE_PLUGIN_EXTENSION = "staticResourcePluginExtension";

    private static String pluginResourcePrefix = "static-plugin";

    /**
     * 对resource资源的访问缓存，设置缓存时间为1小时
     */
    private static CacheControl pluginStaticResourceCacheControl = CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic();

    @Override
    public String key() {
        return STATIC_RESOURCE_PLUGIN_EXTENSION;
    }

    @Override
    public void initialize(ApplicationContext mainApplicationContext) throws Exception {
        List<WebMvcConfigurer> webMvcConfigurers = Lists.newArrayList(new ResourceWebMvcConfigurer());
        // webMvc的处理委托类
        DelegatingWebMvcConfiguration delegatingWebMvcConfiguration = mainApplicationContext.getBean(DelegatingWebMvcConfiguration.class);
        delegatingWebMvcConfiguration.setConfigurers(webMvcConfigurers);
    }

    @Override
    public List<PluginPostProcessor> getPluginPostProcessors(ApplicationContext mainApplicationContext) {
        final List<PluginPostProcessor> pluginPostProcessors = Lists.newArrayList();
        pluginPostProcessors.add(new PluginResourceResolvePostProcessor());
        return pluginPostProcessors;
    }

    @Override
    public List<PluginAfterPreProcessor> getPluginAfterPreProcessors(ApplicationContext mainApplicationContext) {
        final List<PluginAfterPreProcessor> pluginAfterPreProcessors = Lists.newArrayList();
        pluginAfterPreProcessors.add(new PluginThymeleafResourceAfterProcessor());
        return pluginAfterPreProcessors;
    }

    public static CacheControl getCacheControl() {
        return pluginStaticResourceCacheControl;
    }

    public void setCacheControl(CacheControl cacheControl) {
        SpringBootStaticResourceExtension.pluginStaticResourceCacheControl = pluginStaticResourceCacheControl;
    }

    public static String getPluginResourcePrefix() {
        return pluginResourcePrefix;
    }

    public void setPluginResourcePrefix(String pluginResourcePrefix) {
        SpringBootStaticResourceExtension.pluginResourcePrefix = pluginResourcePrefix;
    }
}
