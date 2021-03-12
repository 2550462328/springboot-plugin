package com.github.zhanghui.mybatisplus;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.zhanghui.MapperHandler;
import com.github.zhanghui.PluginMybatisCoreConfig;
import com.github.zhanghui.PluginResourceFinder;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Description:
 * MybatisPlus 在Bean Registry阶段需要做的事情
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 16:40
 **/
public class MybatisPlusBeanRegistrar implements PluginBeanRegistrar {

    @Override
    public void registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        List<Object> configFileObjects = pluginRegistryInfo.getConfigFileObjects();
        SpringBootMybatisPlusConfig springBootMybatisPlusConfig = getMybatisPlusConfig(configFileObjects);

        if(springBootMybatisPlusConfig == null){
            return;
        }

        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();

        if(springBootMybatisPlusConfig.enableOneselfConfig()){
            // 自己配置MybatisSqlSessionFactoryBean
            springBootMybatisPlusConfig.oneselfConfig(mybatisSqlSessionFactoryBean);
        }else{
            // 使用主版本里的SqlSession
            PluginMybatisCoreConfig mybatisCoreConfig = new PluginMybatisCoreConfig(pluginRegistryInfo.getMainApplicationContext());
            // 设置数据库连接池
            mybatisSqlSessionFactoryBean.setDataSource(mybatisCoreConfig.getDataSource());

            MybatisConfiguration configuration = mybatisCoreConfig.getMybatisPlusConfiguration();
            // 设置MybatisPlus配置信息
            mybatisSqlSessionFactoryBean.setConfiguration(configuration);

            Interceptor[] interceptors = mybatisCoreConfig.getInterceptors();
            if(interceptors != null && interceptors.length > 0){
                // 设置Mybatis自定义插件拦截器信息
                mybatisSqlSessionFactoryBean.setPlugins(interceptors);
            }

            DatabaseIdProvider databaseIdProvider = mybatisCoreConfig.getDatabaseIdProvider();
            if(databaseIdProvider != null){
                // 设置数据库唯一标识生成器信息
                mybatisSqlSessionFactoryBean.setDatabaseIdProvider(databaseIdProvider);
            }

            LanguageDriver[] languageDrivers = mybatisCoreConfig.getLanguageDrivers();
            if (languageDrivers != null && languageDrivers.length > 0) {
                // 注册Mybatis的自定义sql解析
                mybatisSqlSessionFactoryBean.setScriptingLanguageDrivers(languageDrivers);
            }
            // 针对MybatisPlus的私有配置
            doConfigMybatisPlus(mybatisSqlSessionFactoryBean,pluginRegistryInfo.getMainApplicationContext());
        }

        PluginResourceFinder pluginResourceFinder = new PluginResourceFinder(pluginRegistryInfo);
        Class<?>[] aliasesClasses = pluginResourceFinder.getAliasesClasses(springBootMybatisPlusConfig.entityPackages());
        if(aliasesClasses!= null && aliasesClasses.length>0) {
            // 设置数据库实体和别名
            mybatisSqlSessionFactoryBean.setTypeAliases(aliasesClasses);
        }

        Resource[] xmlResources = pluginResourceFinder.getXmlResources(springBootMybatisPlusConfig.xmlLocationsMatch());
        if(xmlResources != null && xmlResources.length > 0) {
            // 设置数据库访问xml资源
            mybatisSqlSessionFactoryBean.setMapperLocations(xmlResources);
        }

        ClassLoader defaultClassLoader = Resources.getDefaultClassLoader();

        try {
            Resources.setDefaultClassLoader(pluginRegistryInfo.getDefaultPluginClassLoader());

            SqlSessionFactory sqlSessionFactory = mybatisSqlSessionFactoryBean.getObject();
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
     * 获取MybatisPlus的配置信息
     *
     * @param configFileObjects
     * @return
     */
    private SpringBootMybatisPlusConfig getMybatisPlusConfig(List<Object> configFileObjects) {
        for (Object configFileObj : configFileObjects) {
            Set<Class<?>> allInterfacesAsSet = ClassUtils.getAllInterfacesAsSet(configFileObj);
            if (allInterfacesAsSet.contains(SpringBootMybatisPlusConfig.class)) {
                return (SpringBootMybatisPlusConfig) configFileObj;
            }
        }
        return null;
    }

    /**
     * 针对MybatisPlus的私有配置
     *
     * @param sqlSessionFactoryBean
     * @param mainApplicationContext
     */
    private void doConfigMybatisPlus(MybatisSqlSessionFactoryBean sqlSessionFactoryBean, ApplicationContext mainApplicationContext){
        MybatisPlusProperties mybatisPlusProperties = mainApplicationContext.getBean(MybatisPlusProperties.class);
        GlobalConfig globalConfig = mybatisPlusProperties.getGlobalConfig();

        if(mainApplicationContext.getBeanNamesForType(IKeyGenerator.class,false,false).length > 0){
            IKeyGenerator iKeyGenerator = mainApplicationContext.getBean(IKeyGenerator.class);
            // 设置主键生成策略
            globalConfig.getDbConfig().setKeyGenerator(iKeyGenerator);
        }

        if(mainApplicationContext.getBeanNamesForType(MetaObjectHandler.class,false,false).length > 0){
            MetaObjectHandler metaObjectHandler = mainApplicationContext.getBean(MetaObjectHandler.class);
            // 元对象字段填充控制器（自动填充数据库实体类的公共字段）
            globalConfig.setMetaObjectHandler(metaObjectHandler);
        }

        if(mainApplicationContext.getBeanNamesForType(ISqlInjector.class,false,false).length > 0){
            ISqlInjector iSqlInjector = mainApplicationContext.getBean(ISqlInjector.class);
            // 设置sql自动注入器
            globalConfig.setSqlInjector(iSqlInjector);
        }

        sqlSessionFactoryBean.setGlobalConfig(globalConfig);
    }
}
