package com.github.zhanghui.integration.core.process.pre.classes.group;

import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;
import org.springframework.stereotype.Repository;

import java.lang.annotation.Annotation;

/**
 * 分组存在注解: @Repository
 *
 * @author starBlues
 * @version 2.1.0
 */
public class RepositoryGroup implements PluginClassGroup {

    /**
     * spring @Repository 注解bean
     */
    public static final String GROUP_ID = "spring_repository";


    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationUtils.hasAnnotation(aClass, false, Repository.class);
    }
}
