package com.github.zhanghui.integration.core.process.pre.classes.group;

import com.github.zhanghui.annotation.ConfigDefinition;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;

/**
 * 分组存在注解: @ConfigDefinition
 *
 * @author starBlues
 * @version 2.1.0
 */
public class ConfigDefinitionGroup implements PluginClassGroup {

    /**
     * 自定义插件配置文件bean @ConfigDefinition
     */
    public static final String GROUP_ID= "config_definition";


    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationUtils.hasAnnotation(aClass, false, ConfigDefinition.class);
    }

}
