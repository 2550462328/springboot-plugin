package com.mybatisplus.plugin;

import com.github.zhanghui.annotation.ConfigDefinition;
import com.github.zhanghui.mybatisplus.SpringBootMybatisPlusConfig;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author starBlues
 * @version 1.0
 * @since 2020-12-14
 */
@ConfigDefinition("")
public class MybatisPlusConfig2 implements SpringBootMybatisPlusConfig {
    @Override
    public Set<String> entityPackages() {
        return Sets.newHashSet("com.mybatisplus.plugin.entity");
    }

    @Override
    public Set<String> xmlLocationsMatch() {
        return Sets.newHashSet("classpath:mapper/*Mapper.xml");
    }

}
