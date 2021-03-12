package com.mybatis.plugin1;


import com.github.zhanghui.SpringBootStaticResourceConfig;
import com.github.zhanghui.annotation.ConfigDefinition;
import com.github.zhanghui.thymeleaf.SpringBootThymeleafConfig;
import com.github.zhanghui.thymeleaf.ThymeleafConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author starBlues
 * @version 1.0
 * @since 2020-12-19
 */
@ConfigDefinition
public class ResourceConfig implements SpringBootStaticResourceConfig, SpringBootThymeleafConfig {
    @Override
    public Set<String> locations() {
        Set<String> locations = new HashSet<>();
        locations.add("classpath:static");
        locations.add("file:D:\\aa");
        return locations;
    }

    @Override
    public void config(ThymeleafConfig thymeleafConfig) {
        thymeleafConfig.setPrefix("tf");
        thymeleafConfig.setSuffix(".html");
    }
}
