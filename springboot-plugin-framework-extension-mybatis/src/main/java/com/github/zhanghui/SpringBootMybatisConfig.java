package com.github.zhanghui;

import org.mybatis.spring.SqlSessionFactoryBean;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:17
 **/
public interface SpringBootMybatisConfig extends MybatisCommonConfig {

    /**
     * 插件自定义Mybatis的SqlSessionFactoryBean
     * SqlSessionFactoryBean 具体配置说明参考 Mybatis 官网
     *
     * @param sqlSessionFactoryBean SqlSessionFactoryBean
     */
    default void oneselfConfig(SqlSessionFactoryBean sqlSessionFactoryBean){
    }
}
