package com.github.zhanghui.mybatisplus;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.zhanghui.MybatisCommonConfig;

/**
 * Description:
 * MybatisPlus 的配置接口
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:18
 **/
public interface SpringBootMybatisPlusConfig extends MybatisCommonConfig {

    /**
     * 插件自主配置Mybatis-Plus的MybatisSqlSessionFactoryBean
     * MybatisSqlSessionFactoryBean 具体配置说明参考 Mybatis-plus 官网
     *
     * @param sqlSessionFactoryBean MybatisSqlSessionFactoryBean
     */
    default void oneselfConfig(MybatisSqlSessionFactoryBean sqlSessionFactoryBean){
    }
}
