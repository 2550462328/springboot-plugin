package com.mybatisplus.main.config;

import com.github.zhanghui.SpringBootMybatisPluginExtension;
import com.github.zhanghui.integration.core.AutowiredPluginApplication;
import com.github.zhanghui.integration.core.PluginApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 插件集成配置
 * @Author: starBlues
 * @Version: 1.0
 * @Create Date Time: 2019-05-30 15:53
 * @Update Date Time:
 * @see
 */
@Configuration
public class PluginBeanConfig {

    /**
     * 定义插件应用。使用可以注入它操作插件。
     * @return PluginApplication
     */
    @Bean
    public PluginApplication pluginApplication(){
        // 实例化自动初始化插件的PluginApplication
        PluginApplication pluginApplication = new AutowiredPluginApplication();
        pluginApplication.addPluginExtension(new SpringBootMybatisPluginExtension(
                SpringBootMybatisPluginExtension.LoadType.MYBATIS_PLUS
        ));
        return pluginApplication;
    }
}
