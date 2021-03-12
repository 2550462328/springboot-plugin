package com.github.zhanghui.integration.core.process.pre.classes.group;

import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 分组存在注解: Component、Service
 *
 * @author starBlues
 * @version 2.4.0
 */
public class ComponentGroup implements PluginClassGroup {

    /**
     * spring 组件bean.
     * 包括Component、Service
     */
    public static final String GROUP_ID = "spring_component";

    private final List<PluginClassGroup> customPluginClassFilters = new ArrayList<>();

    public ComponentGroup(){
        customPluginClassFilters.add(new ConfigDefinitionGroup());
        customPluginClassFilters.add(new ConfigBeanGroup());
        customPluginClassFilters.add(new OneselfListenerGroup());
        customPluginClassFilters.add(new CallerGroup());
        customPluginClassFilters.add(new SupplierGroup());
    }


    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        boolean isFilter =  AnnotationUtils.hasAnnotation(aClass, false, Component.class, Service.class,Configuration.class,Controller.class,RestController.class);
        // 不满足Spring基础组件 直接返回
        if(!isFilter){
            return false;
        }

        for(PluginClassGroup customPluginClassFilter : customPluginClassFilters){
            // 如果这个类同时被Spring基础组件和 我们自定义的容器标签注解的情况 应该放到后面给自定义容器解析器去注入
            if(customPluginClassFilter.filter(aClass)){
                return false;
            }
        }
        return true;
    }
}
