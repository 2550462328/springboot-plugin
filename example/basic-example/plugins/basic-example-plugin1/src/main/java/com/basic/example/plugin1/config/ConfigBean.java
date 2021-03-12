package com.basic.example.plugin1.config;

import com.basic.example.main.config.PluginConfiguration;
import com.github.zhanghui.realize.PluginUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author starBlues
 * @version 1.0
 */
@Configuration
public class ConfigBean {

    @Bean
    public ConfigBeanTest c(PluginUtils pluginUtils){
        PluginConfiguration mainBean = pluginUtils.getMainBean(PluginConfiguration.class);
        System.out.println("configTest: mainBean=" + mainBean);
        ConfigBeanTest configBeanTest = new ConfigBeanTest();
        configBeanTest.name = "hello";
        configBeanTest.age = 16;
        return configBeanTest;
    }

    public static class ConfigBeanTest{
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "ConfigBeanTest{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
