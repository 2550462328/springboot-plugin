package com.basic.example.main.config;

import com.github.zhanghui.integration.core.AutowiredPluginApplication;
import com.github.zhanghui.integration.core.PluginApplication;
import com.github.zhanghui.integration.core.process.post.extension.SpringDocControllerProcessorExtension;
import org.quartz.SchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 插件集成配置
 *
 * @author starBlues
 * @version 1.0
 */
@Configuration
public class PluginBeanConfig {


    /**
     * 定义插件应用。使用可以注入其它操作插件。
     * @return PluginApplication
     */
    @Bean
    public PluginApplication pluginApplication(PluginListener pluginListener,
                                               SchedulerFactory schedulerFactory){
        AutowiredPluginApplication autoPluginApplication = new AutowiredPluginApplication();
        autoPluginApplication.setPluginInitializerListener(pluginListener);
        autoPluginApplication.addListener(ExamplePluginListener.class);
        return autoPluginApplication;
    }

    /**
     * 集成 SpringDoc 的插件刷新
     * @param applicationContext ApplicationContext
     * @return SpringDocControllerProcessor
     */
    @Bean
    public SpringDocControllerProcessorExtension springDocControllerProcessor(ApplicationContext applicationContext){
        return new SpringDocControllerProcessorExtension(applicationContext);
    }

}
