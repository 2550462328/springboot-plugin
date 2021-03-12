package com.github.zhanghui.integration.core.process.pre.registrar;

import com.github.zhanghui.integration.core.process.pre.registrar.generator.PluginBeanNameGenerator;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 15:20
 **/
@Slf4j
public abstract class AbstractPluginBeanRegistrar implements PluginBeanRegistrar {


    protected boolean existBean(PluginRegistryInfo pluginRegistryInfo,String beanName){
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
        return pluginApplicationContext.containsBean(beanName);
    }

    /**
     * 向插件spring上下文中注册bean
     *
     * @param beanClass
     * @return
     */
    protected String register(PluginRegistryInfo pluginRegistryInfo,Class<?> beanClass){
        return register(pluginRegistryInfo,beanClass,null);
    }

    /**
     * 向插件spring上下文中注册bean
     * @param beanName 注册bean名称
     * @param beanClass 注册bean的类
     * @return
     */
    protected String register(String beanName, PluginRegistryInfo pluginRegistryInfo, Class<?> beanClass){
        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(beanClass);
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
        return register(beanName, pluginApplicationContext, beanDefinition,null);
    }

    /**
     * 向插件spring上下文中注册bean
     * @param beanName 注册bean名称
     * @param pluginApplicationContext 插件的spring上下文
     * @param beanDefinition 需要注册的BeanDefinition
     * @param beanDefinitionConsumer 在beanDefinition注册到spring之前对beanDefinition的一些操作
     * @return
     */
    protected String register(String beanName,GenericApplicationContext pluginApplicationContext, AnnotatedGenericBeanDefinition beanDefinition, Consumer<AnnotatedGenericBeanDefinition> beanDefinitionConsumer){

        Objects.requireNonNull(beanName,"bean名称不能为空");

        if(pluginApplicationContext.containsBean(beanName)){
            log.error("插件 spring环境中注册bean [{}] 失败：禁止重复注册",beanName );
            return beanName;
        }

        if(beanDefinitionConsumer != null) {
            beanDefinitionConsumer.accept(beanDefinition);
        }

        pluginApplicationContext.registerBeanDefinition(beanName,beanDefinition);
        return beanName;
    }


    /**
     * 向插件spring上下文中注册bean
     * @param pluginRegistryInfo 插件信息 用来合成beanName
     * @param beanClass 注册bean的类
     * @param beanDefinitionConsumer 在beanDefinition注册到spring之前对beanDefinition的一些操作
     * @return
     */
    protected String register(PluginRegistryInfo pluginRegistryInfo,Class<?> beanClass, Consumer<AnnotatedGenericBeanDefinition> beanDefinitionConsumer){

        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();

        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();

        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(beanClass);

        BeanNameGenerator beanNameGenerator = new PluginBeanNameGenerator(pluginId);
        String beanName =  beanNameGenerator.generateBeanName(beanDefinition, pluginApplicationContext);

        if(pluginApplicationContext.containsBean(beanName)){
            log.error("插件 spring环境中注册bean [{}] 失败：禁止重复注册",beanName );
            return beanName;
        }

        return register(beanName,pluginApplicationContext,beanDefinition,beanDefinitionConsumer);
    }

    /**
     * 插件的spring上下文中注册单例bean
     *
     * @param beanName
     * @param beanObject
     * @return
     */
    protected void registerSingleton(String beanName, PluginRegistryInfo pluginRegistryInfo, Object beanObject){
        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
        DefaultListableBeanFactory listableBeanFactory = pluginApplicationContext.getDefaultListableBeanFactory();
        if(!listableBeanFactory.containsSingleton(beanName)) {
            listableBeanFactory.registerSingleton(beanName, beanObject);
        }
    }

}
