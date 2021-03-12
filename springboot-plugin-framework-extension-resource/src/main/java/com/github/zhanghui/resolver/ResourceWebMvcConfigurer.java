package com.github.zhanghui.resolver;

import com.github.zhanghui.SpringBootStaticResourceExtension;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Description:
 * 自定义Mvc的资源处理器（这里配置了拦截规则）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/5 17:13
 **/
public class ResourceWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pathPattern = "/" + SpringBootStaticResourceExtension.getPluginResourcePrefix() + "/**";
        // 设置资源请求拦截路径的前缀
        ResourceHandlerRegistration resourceHandlerRegistration = registry.addResourceHandler(pathPattern);

        // 设置对资源的缓存
        CacheControl cacheControl = SpringBootStaticResourceExtension.getCacheControl();
        if(cacheControl != null){
            resourceHandlerRegistration.setCacheControl(cacheControl);
        }else{
            resourceHandlerRegistration.setCacheControl(CacheControl.noStore());
        }
        resourceHandlerRegistration.resourceChain(false).addResolver(new PluginResourceResolver());
    }
}
