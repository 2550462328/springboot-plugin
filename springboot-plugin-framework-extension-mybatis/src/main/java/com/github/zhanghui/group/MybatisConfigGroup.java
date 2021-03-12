package com.github.zhanghui.group;

import com.github.zhanghui.SpringBootMybatisConfig;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.mybatisplus.SpringBootMybatisPlusConfig;
import com.github.zhanghui.realize.BasePlugin;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * Description:
 * Mybatis配置分组器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 15:49
 **/
public class MybatisConfigGroup implements PluginClassGroup {

    public static final String GROUP_ID = "MybatisConfigGroup";

    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        if (aClass == null) {
            return false;
        }

        Set<Class<?>> interfaces = ClassUtils.getAllInterfacesAsSet(aClass);
        return interfaces.contains(SpringBootMybatisConfig.class) || interfaces.contains(SpringBootMybatisPlusConfig.class);
    }
}
