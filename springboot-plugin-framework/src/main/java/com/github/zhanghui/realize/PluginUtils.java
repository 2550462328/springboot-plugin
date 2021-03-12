package com.github.zhanghui.realize;

import com.google.common.collect.Lists;
import org.pf4j.PluginDescriptor;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/9 17:11
 **/
public class PluginUtils {

    protected final ApplicationContext mainApplicationContext;
    protected final ApplicationContext pluginApplicationContext;
    protected final PluginDescriptor pluginDescriptor;

    public PluginUtils(ApplicationContext mainApplicationContext, ApplicationContext pluginApplicationContext,PluginDescriptor pluginDescriptor) {
        this.mainApplicationContext = mainApplicationContext;
        this.pluginApplicationContext = pluginApplicationContext;
        this.pluginDescriptor = pluginDescriptor;
    }

    /**
     * 获取主程序的 ApplicationContext
     * @return ApplicationContext
     */
    public ApplicationContext getMainApplicationContext() {
        return mainApplicationContext;
    }

    /**
     * 获取当前插件的 ApplicationContext
     * @return ApplicationContext
     */
    public ApplicationContext getPluginApplicationContext() {
        return pluginApplicationContext;
    }

    /**
     * 获取当前插件的描述信息
     * @return PluginDescriptor
     */
    public PluginDescriptor getPluginDescriptor(){
        return pluginDescriptor;
    }


    /**
     * 获取bean名称得到主程序中的bean
     * @param name bean 名称
     * @param <T> bean 类型
     * @return bean
     */
    public <T> T getMainBean(String name){
        Object bean = mainApplicationContext.getBean(name);
        if(bean == null){
            return null;
        }
        return (T) bean;
    }

    /**
     * 通过bean类型得到主程序中的bean
     * @param aClass bean 类型
     * @param <T> bean 类型
     * @return bean
     */
    public <T> T getMainBean(Class<T> aClass) {
        return mainApplicationContext.getBean(aClass);
    }

    /**
     * 通过接口或者抽象类型得到主程序中的多个实现类型
     * @param aClass bean 类型
     * @param <T> bean 类型
     * @return bean
     */
    public <T> List<T> getMainBeans(Class<T> aClass){
        return Lists.newArrayList(mainApplicationContext.getBeansOfType(aClass).values());
    }

}
