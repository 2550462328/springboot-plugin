package com.github.zhanghui.tkmybatis;

import com.github.zhanghui.MapperHandler;
import com.github.zhanghui.PluginMybatisCoreConfig;
import com.github.zhanghui.PluginResourceFinder;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.mapperhelper.MapperHelper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Description:
 * Tk Mybatis 在Bean Registry阶段需要做的事情
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:40
 **/
public class TkMybatisBeanRegistrar implements PluginBeanRegistrar {
    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        List<Object> configFileObjects = pluginRegistryInfo.getConfigFileObjects();
        SpringBootTKMybatisConfig springBootTKMybatisConfig = getTKMybatisConfig(configFileObjects);

        if (springBootTKMybatisConfig == null) {
            return;
        }

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        //通用Mapper属性配置
        Config tkConfig = null;

        if (springBootTKMybatisConfig.enableOneselfConfig()) {
            tkConfig = new Config();
            springBootTKMybatisConfig.oneselfConfig(sqlSessionFactoryBean);
            springBootTKMybatisConfig.oneselfConfig(tkConfig);
        } else {
            // 使用主版本里的SqlSession
            ApplicationContext mainApplicationContext = pluginRegistryInfo.getMainApplicationContext();
            PluginMybatisCoreConfig pluginMybatisCoreConfig = new PluginMybatisCoreConfig(mainApplicationContext);

            DataSource dataSource = pluginMybatisCoreConfig.getDataSource();
            if (dataSource != null) {
                // 设置数据库连接池
                sqlSessionFactoryBean.setDataSource(pluginMybatisCoreConfig.getDataSource());
            }

            Configuration configuration = pluginMybatisCoreConfig.getTKMybatisConfiguration();
            // 设置Mybatis配置信息
            sqlSessionFactoryBean.setConfiguration(configuration);

            Interceptor[] interceptors = pluginMybatisCoreConfig.getInterceptors();
            if (interceptors != null && interceptors.length > 0) {
                // 设置Mybatis自定义插件拦截器信息
                sqlSessionFactoryBean.setPlugins(interceptors);
            }

            DatabaseIdProvider databaseIdProvider = pluginMybatisCoreConfig.getDatabaseIdProvider();
            if (databaseIdProvider != null) {
                // 设置数据库唯一标识生成器信息
                sqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider);
            }

            if (mainApplicationContext.getBeanNamesForType(Config.class, false, false).length > 0) {
                tkConfig = mainApplicationContext.getBean(Config.class);
            }
        }

        MapperHelper mapperHelper = new MapperHelper();
        if (tkConfig != null) {
            mapperHelper.setConfig(tkConfig);
        }

        PluginResourceFinder pluginResourceFinder = new PluginResourceFinder(pluginRegistryInfo);
        Class<?>[] aliasesClasses = pluginResourceFinder.getAliasesClasses(springBootTKMybatisConfig.entityPackages());
        // 设置数据库实体和别名
        sqlSessionFactoryBean.setTypeAliases(aliasesClasses);

        Resource[] xmlResources = pluginResourceFinder.getXmlResources(springBootTKMybatisConfig.xmlLocationsMatch());
        // 设置数据库访问xml资源
        sqlSessionFactoryBean.setMapperLocations(xmlResources);

        ClassLoader defaultClassLoader = Resources.getDefaultClassLoader();
        ClassLoader pluginClassLoader = pluginRegistryInfo.getDefaultPluginClassLoader();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Resources.setDefaultClassLoader(pluginRegistryInfo.getDefaultPluginClassLoader());

            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
            Objects.requireNonNull(sqlSessionFactory);

            SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
            // 用于解决Tk中MsUtil的ClassLoader的问题
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            MapperHandler mapperHandler = new MapperHandler();
            // 对单个Mapper操作
            mapperHandler.processMapper(pluginRegistryInfo, mapperHelper, sqlSessionFactory, sqlSessionTemplate);

        } finally {
            Resources.setDefaultClassLoader(defaultClassLoader);
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * 获取TKMybatis的配置信息
     *
     * @param configFileObjects
     * @return
     */
    private SpringBootTKMybatisConfig getTKMybatisConfig(List<Object> configFileObjects) {
        for (Object configFileObj : configFileObjects) {
            Set<Class<?>> allInterfacesAsSet = ClassUtils.getAllInterfacesAsSet(configFileObj);
            if (allInterfacesAsSet.contains(SpringBootTKMybatisConfig.class)) {
                return (SpringBootTKMybatisConfig) configFileObj;
            }
        }
        return null;
    }
}
