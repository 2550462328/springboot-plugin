package com.github.zhanghui;

import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Description:
 * <p>
 * Mybatis在Bean Registry阶段需要做的事情
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:40
 **/
public class MybatisBeanRegistrar implements PluginBeanRegistrar {

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        List<Object> configFileObjects = pluginRegistryInfo.getConfigFileObjects();
        SpringBootMybatisConfig springBootMybatisConfig = getMybatisConfig(configFileObjects);
        if (springBootMybatisConfig == null) {
            return;
        }

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

        if (springBootMybatisConfig.enableOneselfConfig()) {
            // 自己配置SqlSessionFactoryBean
            springBootMybatisConfig.oneselfConfig(sqlSessionFactoryBean);
        } else {
            // 使用主版本里的SqlSession
            ApplicationContext mainApplicationContext = pluginRegistryInfo.getMainApplicationContext();
            PluginMybatisCoreConfig pluginMybatisCoreConfig = new PluginMybatisCoreConfig(mainApplicationContext);

            DataSource dataSource = pluginMybatisCoreConfig.getDataSource();
            if (dataSource != null) {
                // 设置数据库连接池
                sqlSessionFactoryBean.setDataSource(pluginMybatisCoreConfig.getDataSource());
            }

            Configuration configuration = pluginMybatisCoreConfig.getMybatisConfiguration();
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

            LanguageDriver[] languageDrivers = pluginMybatisCoreConfig.getLanguageDrivers();
            if (languageDrivers != null && languageDrivers.length > 0) {
                for (LanguageDriver languageDriver : languageDrivers) {
                    // 注册Mybatis的自定义sql解析
                    configuration.getLanguageRegistry().register(languageDriver);
                }
            }
        }

        PluginResourceFinder pluginResourceFinder = new PluginResourceFinder(pluginRegistryInfo);
        Class<?>[] aliasesClasses = pluginResourceFinder.getAliasesClasses(springBootMybatisConfig.entityPackages());
        // 设置数据库实体和别名
        sqlSessionFactoryBean.setTypeAliases(aliasesClasses);

        Resource[] xmlResources = pluginResourceFinder.getXmlResources(springBootMybatisConfig.xmlLocationsMatch());
        // 设置数据库访问xml资源
        sqlSessionFactoryBean.setMapperLocations(xmlResources);

        // 保存原有的ClassLoader用于后期还原
        ClassLoader defaultClassLoader = Resources.getDefaultClassLoader();

        try {
            Resources.setDefaultClassLoader(pluginRegistryInfo.getDefaultPluginClassLoader());

            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
            Objects.requireNonNull(sqlSessionFactory);

            SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
            MapperHandler mapperHandler = new MapperHandler();
            // 对单个Mapper操作
            mapperHandler.processMapper(pluginRegistryInfo, sqlSessionFactory, sqlSessionTemplate);

        } finally {
            Resources.setDefaultClassLoader(defaultClassLoader);
        }
    }

    /**
     * 获取Mybaits的配置信息
     *
     * @param configFileObjects
     * @return
     */
    private SpringBootMybatisConfig getMybatisConfig(List<Object> configFileObjects) {
        for (Object configFileObj : configFileObjects) {
            Set<Class<?>> allInterfacesAsSet = ClassUtils.getAllInterfacesAsSet(configFileObj);
            if (allInterfacesAsSet.contains(SpringBootMybatisConfig.class)) {
                return (SpringBootMybatisConfig) configFileObj;
            }
        }
        return null;
    }
}
