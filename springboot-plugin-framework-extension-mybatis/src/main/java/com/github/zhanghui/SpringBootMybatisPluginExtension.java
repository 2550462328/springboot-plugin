package com.github.zhanghui;

import com.github.zhanghui.extension.AbstractPluginExtension;
import com.github.zhanghui.group.MybatisConfigGroup;
import com.github.zhanghui.group.PluginEntityAliasGroup;
import com.github.zhanghui.group.PluginMapperGroup;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.mybatisplus.MybatisPlusBeanRegistrar;
import com.github.zhanghui.tkmybatis.TkMybatisBeanRegistrar;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 15:37
 **/
public class SpringBootMybatisPluginExtension extends AbstractPluginExtension {

    private static final String KEY = "springBootMabtisPluginExtension";

    private final LoadType loadType;

    public SpringBootMybatisPluginExtension(LoadType loadType) {
        if(loadType == null){
            this.loadType = LoadType.MYBATIS;
        }else{
            this.loadType = loadType;
        }
    }

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public List<PluginClassGroup> getPluginClassGroups(ApplicationContext mainApplicationContext) {
        final List<PluginClassGroup> classGroups = Lists.newArrayList();
        // Mybatis的配置类分组
        classGroups.add(new MybatisConfigGroup());
        // 数据库实体类和别名分组
        classGroups.add(new PluginEntityAliasGroup());
        // 数据库Mapper访问分组
        classGroups.add(new PluginMapperGroup());

        return classGroups;
    }

    @Override
    public List<PluginBeanRegistrar> getPluginBeanRegistrars(ApplicationContext mainApplicationContext) {
        final List<PluginBeanRegistrar> beanRegistrars = Lists.newArrayList();

        if(loadType == LoadType.MYBATIS){
            beanRegistrars.add(new MybatisBeanRegistrar());
        }else if(loadType == LoadType.MYBATIS_PLUS){
            beanRegistrars.add(new MybatisPlusBeanRegistrar());
        }else{
            beanRegistrars.add(new TkMybatisBeanRegistrar());
        }
        return beanRegistrars;
    }

    /**
     * 加载类型
     */
    public enum LoadType{
        MYBATIS,
        MYBATIS_PLUS,
        TK_MYBATIS;
    }
}
