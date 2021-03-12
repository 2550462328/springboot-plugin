package com.github.zhanghui.integration.core.process.post.extension;

import com.github.zhanghui.utils.ClassUtils;
import org.springdoc.api.AbstractOpenApiResource;
import org.springdoc.core.OpenAPIService;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 * 在插件动态变化的时候对SpringDoc 实时刷新（类似Swagger Refresh）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/20 10:07
 **/
public class SpringDocControllerProcessorExtension implements PluginControllerExtension {

    private final ApplicationContext applicationContext;

    private List<Class<?>> restControllers;
    private OpenAPIService openAPIService;

    public SpringDocControllerProcessorExtension(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize() {
        AbstractOpenApiResource openApiResource = applicationContext.getBean(AbstractOpenApiResource.class);
        if(openApiResource == null){
            return;
        }
        try {
            restControllers = (List<Class<?>>)ClassUtils.getReflectionFiled(openApiResource,"ADDITIONAL_REST_CONTROLLERS");
        } catch (IllegalAccessException e) {
            restControllers = null;
        }
        openAPIService = applicationContext.getBean(OpenAPIService.class);
    }

    @Override
    public void registry(String pluginId, List<ControllerWrapper> controllerWrappers) throws Exception {
        if(restControllers != null){
            controllerWrappers.forEach(controllerWrapper -> restControllers.add(controllerWrapper.getBeanClass()));
            this.refresh();
        }
    }

    @Override
    public void unRegistry(String pluginId, List<ControllerWrapper> controllerWrappers) throws Exception {
        if(restControllers != null){
            controllerWrappers.forEach(controllerWrapper -> restControllers.remove(controllerWrapper.getBeanClass()));
            this.refresh();
        }
    }

    /**
     * 刷新springDoc上下文
     */
    private void refresh(){
        if(openAPIService != null){
            openAPIService.setCachedOpenAPI(null);
            openAPIService.resetCalculatedOpenAPI();
        }
    }
}
