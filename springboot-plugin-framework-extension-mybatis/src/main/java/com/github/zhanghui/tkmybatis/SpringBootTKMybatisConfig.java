package com.github.zhanghui.tkmybatis;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.zhanghui.MybatisCommonConfig;
import org.mybatis.spring.SqlSessionFactoryBean;
import tk.mybatis.mapper.entity.Config;

/**
 * Description:
 * TKMybatis 的配置接口
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/4 16:28
 **/
public interface SpringBootTKMybatisConfig extends MybatisCommonConfig {

    /**
     * 插件自主配置TKMybatis的 SqlSessionFactoryBean
     * SqlSessionFactoryBean 具体配置说明参考 Mybatis 官网
     *
     * @param sqlSessionFactoryBean SqlSessionFactoryBean
     */
    default void oneselfConfig(SqlSessionFactoryBean sqlSessionFactoryBean){
    }

    /**
     * 插件自主配置TKMybatis的 Config
     * Config 具体配置说明参考 https://gitee.com/free/Mapper/wikis/1.1-java?sort_id=208196
     *
     * @param Config config
     */
    default void oneselfConfig(Config config){
    }
}
