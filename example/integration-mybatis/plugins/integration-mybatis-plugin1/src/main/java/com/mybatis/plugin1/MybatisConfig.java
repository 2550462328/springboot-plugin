package com.mybatis.plugin1;


import com.github.zhanghui.SpringBootMybatisConfig;
import com.github.zhanghui.annotation.ConfigDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * @author starBlues
 * @version 1.0
 * @since 2020-12-18
 */
@ConfigDefinition
public class MybatisConfig implements SpringBootMybatisConfig {

    @Override
    public Set<String> entityPackages() {
        Set<String> typeAliasesPackage = new HashSet<>();
        typeAliasesPackage.add("com.mybatis.plugin1.entity");
        return typeAliasesPackage;
    }

    @Override
    public Set<String> xmlLocationsMatch() {
        Set<String> xmlLocationsMatch = new HashSet<>();
        xmlLocationsMatch.add("classpath:mapper/*Mapper.xml");
        return xmlLocationsMatch;
    }

}
