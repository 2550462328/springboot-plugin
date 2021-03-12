package com.github.zhanghui;

import com.github.zhanghui.group.PluginMapperGroup;
import com.github.zhanghui.integration.core.process.pre.registrar.generator.PluginBeanNameGenerator;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.google.common.collect.Sets;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import java.util.List;
import java.util.Set;

/**
 * Description:
 * Mybatis中的mapper文件处理器（解析器）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/3 22:07
 **/
public class MapperHandler {

    private final static String MAPPER_INTERFACE_NAMES = "MapperInterfaceNames";

    private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    /**
     * 处理插件中的Mapper
     * 将Mapper注册到Plugin的ApplicationContext中，并且放到插件信息中
     *
     * @param pluginRegistryInfo 插件信息
     */
    public void processMapper(PluginRegistryInfo pluginRegistryInfo, SqlSessionFactory sqlSessionFactory,
                              SqlSessionTemplate sqlSessionTemplate) {

        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();

        List<Class> mapperGroupClasses = pluginRegistryInfo.getClassesFromGroup(PluginMapperGroup.GROUP_ID);
        if (mapperGroupClasses.isEmpty()) {
            return;
        }
        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
        Set<String> mapperBeanNames = Sets.newHashSet();
        BeanNameGenerator beanNameGenerator = new PluginBeanNameGenerator(pluginId);

        for (Class mapperClass : mapperGroupClasses) {
            AnnotatedGenericBeanDefinition annotatedGenericBeanDefinition = new AnnotatedGenericBeanDefinition(mapperClass);
            // 获取mapperClass中Bean的Scope
            ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(annotatedGenericBeanDefinition);
            annotatedGenericBeanDefinition.setScope(scopeMetadata.getScopeName());
            // 生成BeanName
            String mapperBeanName = beanNameGenerator.generateBeanName(annotatedGenericBeanDefinition, pluginApplicationContext);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(annotatedGenericBeanDefinition, mapperBeanName);

            AnnotationConfigUtils.processCommonDefinitionAnnotations(annotatedGenericBeanDefinition);
            // 注册Mapper的BeanDefinition
            BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, pluginApplicationContext);

            // 修改BeanDefinition中的MapperClass 到 MapperFactoryBean
            doInjectProperties(beanDefinitionHolder, mapperClass, sqlSessionFactory, sqlSessionTemplate);
            mapperBeanNames.add(mapperBeanName);
        }
        // 添加到插件信息中
        pluginRegistryInfo.addExtension(MAPPER_INTERFACE_NAMES, mapperBeanNames);
    }

    /**
     * 处理TkMybatis 插件中的Mapper
     * 将Mapper注册到Plugin的ApplicationContext中，并且放到插件信息中
     *
     * @param pluginRegistryInfo 插件信息
     */
    public void processMapper(PluginRegistryInfo pluginRegistryInfo, MapperHelper mapperHelper, SqlSessionFactory sqlSessionFactory,
                              SqlSessionTemplate sqlSessionTemplate) {

        GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();

        List<Class> mapperGroupClasses = pluginRegistryInfo.getClassesFromGroup(PluginMapperGroup.GROUP_ID);
        if (mapperGroupClasses.isEmpty()) {
            return;
        }
        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
        Set<String> mapperBeanNames = Sets.newHashSet();
        BeanNameGenerator beanNameGenerator = new PluginBeanNameGenerator(pluginId);

        for (Class mapperClass : mapperGroupClasses) {
            AnnotatedGenericBeanDefinition annotatedGenericBeanDefinition = new AnnotatedGenericBeanDefinition(mapperClass);
            ScopeMetadata scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(annotatedGenericBeanDefinition);
            annotatedGenericBeanDefinition.setScope(scopeMetadata.getScopeName());
            String mapperBeanName = beanNameGenerator.generateBeanName(annotatedGenericBeanDefinition, pluginApplicationContext);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(annotatedGenericBeanDefinition, mapperBeanName);

            AnnotationConfigUtils.processCommonDefinitionAnnotations(annotatedGenericBeanDefinition);
            // 注册Mapper的BeanDefinition
            BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, pluginApplicationContext);

            // 注入额外属性
            doInjectProperties(beanDefinitionHolder, mapperHelper, mapperClass, sqlSessionFactory, sqlSessionTemplate);
            mapperBeanNames.add(mapperBeanName);
        }
        // 添加到插件信息中
        pluginRegistryInfo.addExtension(MAPPER_INTERFACE_NAMES, mapperBeanNames);
    }

    /**
     * 修改BeanDefinition中的MapperClass 到 MapperFactoryBean
     * 之前是一个接口class，现在经MapperFactoryBean处理之后会变成一个代理类
     *
     * 处理Mybatis + MybatisPlus
     *
     * @param holder
     * @param mapperClass
     * @param sqlSessionFactory
     * @param sqlSessionTemplate
     */
    private void doInjectProperties(BeanDefinitionHolder holder,
                                    Class<?> mapperClass,
                                    SqlSessionFactory sqlSessionFactory,
                                    SqlSessionTemplate sqlSessionTemplate) {
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
        beanDefinition.setBeanClass(org.mybatis.spring.mapper.MapperFactoryBean.class);
        beanDefinition.getPropertyValues().add("addToConfig", true);
        beanDefinition.getPropertyValues().add("sqlSessionFactory", sqlSessionFactory);
        beanDefinition.getPropertyValues().add("sqlSessionTemplate", sqlSessionTemplate);
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
    }

    /**
     * 处理tkMybatis
     *
     * @param holder
     * @param mapperHelper
     * @param mapperClass
     * @param sqlSessionFactory
     * @param sqlSessionTemplate
     */
    private void doInjectProperties(BeanDefinitionHolder holder,
                                    MapperHelper mapperHelper,
                                    Class<?> mapperClass,
                                    SqlSessionFactory sqlSessionFactory,
                                    SqlSessionTemplate sqlSessionTemplate) {
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) holder.getBeanDefinition();
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(mapperClass);
        beanDefinition.setBeanClass(tk.mybatis.spring.mapper.MapperFactoryBean.class);
        beanDefinition.getPropertyValues().add("mapperHelper", mapperHelper);
        beanDefinition.getPropertyValues().add("addToConfig", true);
        beanDefinition.getPropertyValues().add("sqlSessionFactory", sqlSessionFactory);
        beanDefinition.getPropertyValues().add("sqlSessionTemplate", sqlSessionTemplate);
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
    }
}
