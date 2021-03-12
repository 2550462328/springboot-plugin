package com.github.zhanghui.group;

import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.AnnotationUtils;
import org.apache.ibatis.annotations.Mapper;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:34
 **/
public class PluginMapperGroup implements PluginClassGroup {

    public static final String GROUP_ID = "plugin_mybatis_mapper";

    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        return AnnotationUtils.hasAnnotation(aClass,false, Mapper.class);
    }
}
