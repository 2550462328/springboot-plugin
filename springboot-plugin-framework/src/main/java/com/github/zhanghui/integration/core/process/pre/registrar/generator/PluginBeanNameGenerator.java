package com.github.zhanghui.integration.core.process.pre.registrar.generator;

import com.github.zhanghui.utils.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

/**
 * Description:
 * plugin的beanName 生成器
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 15:35
 **/
public class PluginBeanNameGenerator extends AnnotationBeanNameGenerator {

    private final String pluginId;

    public PluginBeanNameGenerator(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public String generateBeanName(BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {

        if(beanDefinition instanceof AnnotatedBeanDefinition){
            String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) beanDefinition);
            if(StringUtils.isNotNull(beanName)){
                return beanName.concat("@").concat(pluginId);
            }
        }
        return buildDefaultBeanName(beanDefinition,registry).concat("@").concat(pluginId);
    }
}
