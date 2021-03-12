package com.github.zhanghui.integration.core.process.pre.classes.group;

import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.realize.ConfigBean;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * 对接口ConfigBean实现的类分组
 * @see ConfigBean
 *
 * @author starBlues
 * @version 2.2.2
 */
public class ConfigBeanGroup implements PluginClassGroup {


    public static final String GROUP_ID = "config_bean";


    @Override
    public String groupId() {
        return GROUP_ID;
    }

    @Override
    public void initialize(BasePlugin basePlugin) {

    }

    @Override
    public boolean filter(Class<?> aClass) {
        if(aClass == null){
            return false;
        }
        Set<Class<?>> allInterfacesForClassAsSet = ClassUtils.getAllInterfacesForClassAsSet(aClass);
        return allInterfacesForClassAsSet.contains(ConfigBean.class);
    }

}
