package com.github.zhanghui;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.LanguageDriverRegistry;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description:
 * 用于从主版本中获取数据库Mybatis相关的配置
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/3/2 17:15
 **/
public class PluginMybatisCoreConfig {

    private final ApplicationContext mainApplicationContext;

    public PluginMybatisCoreConfig(ApplicationContext mainApplicationContext) {
        this.mainApplicationContext = mainApplicationContext;
    }

    /**
     * 数据连接池
     *
     * @return
     */
    public DataSource getDataSource() {
        return mainApplicationContext.getBean(DataSource.class);
    }

    /**
     * 获取mybatis配置
     *
     * @return
     */
    public Configuration getMybatisConfiguration() {
        Configuration configuration = new Configuration();
        Map<String, ConfigurationCustomizer> customizerMap = mainApplicationContext.getBeansOfType(ConfigurationCustomizer.class);
        if (!customizerMap.isEmpty()) {
            for (ConfigurationCustomizer customizer : customizerMap.values()) {
                customizer.customize(configuration);
            }
        }
        return configuration;
    }

    /**
     * 获取tk mybatis配置
     *
     * @return
     */
    public Configuration getTKMybatisConfiguration() {
        return new Configuration();
    }

    /**
     * 获取mybatisPlus配置
     *
     * @return
     */
    public MybatisConfiguration getMybatisPlusConfiguration() {
        MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
        Map<String, com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer> customizerMap = mainApplicationContext.getBeansOfType(com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer.class);

        if (!customizerMap.isEmpty()) {
            for (com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer customizer : customizerMap.values()) {
                customizer.customize(mybatisConfiguration);
            }
        }
        return mybatisConfiguration;
    }

    /**
     * 获取mybatis拦截器插件
     *
     * @return
     */
    public Interceptor[] getInterceptors() {
        Map<Class<? extends Interceptor>, Interceptor> interceptorMap = new HashMap();

        SqlSessionFactory sqlSessionFactory = mainApplicationContext.getBean(SqlSessionFactory.class);
        // 1. 直接从主版本Mybatis下的SqlSessionFactory中拿
        List<Interceptor> interceptors = sqlSessionFactory.getConfiguration().getInterceptors();

        if(!interceptors.isEmpty()) {
            interceptors.stream().filter(Objects::nonNull).forEach(interceptor -> {
                interceptorMap.put(interceptor.getClass(), interceptor);
            });
        }

        //2. 再遍历主版本下的所有类，过滤出Interceptor

        Map<String, Interceptor> beansOfType = mainApplicationContext.getBeansOfType(Interceptor.class);
        if(!beansOfType.isEmpty()){
            for(Interceptor interceptor : beansOfType.values()){
                if(interceptor!=null) {
                    // 这里如何跟上面重复了会进行覆盖操作
                    interceptorMap.put(interceptor.getClass(),interceptor);
                }
            }
        }

        if(interceptorMap.isEmpty()){
            return null;
        }else{
            return interceptorMap.values().toArray(new Interceptor[0]);
        }
    }

    /**
     * 数据库唯一标识id生成器
     *
     * @return
     */
    public DatabaseIdProvider getDatabaseIdProvider(){
        String[] beanNamesForType = mainApplicationContext.getBeanNamesForType(DatabaseIdProvider.class,false,false);
        if(beanNamesForType.length > 0){
            return mainApplicationContext.getBean(DatabaseIdProvider.class);
        }
        return null;
    }

    /**
     * 自定义sql语言解析器
     *
     * @return
     */
    public LanguageDriver[] getLanguageDrivers(){
        Map<Class<? extends LanguageDriver>, LanguageDriver> classLanguageDriverMap = new HashMap<>();

        SqlSessionFactory sqlSessionFactory = mainApplicationContext.getBean(SqlSessionFactory.class);
        LanguageDriverRegistry languageRegistry = sqlSessionFactory.getConfiguration().getLanguageRegistry();
        //1. 先从主版本的SqlSessionFactory中去拿
        Field languageDriverMapField = ReflectionUtils.findField(languageRegistry.getClass(), "LANGUAGE_DRIVER_MAP");
        if(languageDriverMapField != null){
            if(!languageDriverMapField.isAccessible()){
                languageDriverMapField.setAccessible(true);
            }

            try {
                Map<Class<? extends LanguageDriver>, LanguageDriver> languageDriverMap = (Map<Class<? extends LanguageDriver>, LanguageDriver>)languageDriverMapField.get(languageRegistry);
                if(!languageDriverMap.isEmpty()){
                    classLanguageDriverMap.putAll(languageDriverMap);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Map<String, LanguageDriver> beansOfType = mainApplicationContext.getBeansOfType(LanguageDriver.class);
        if(!beansOfType.isEmpty()){
            for(LanguageDriver languageDriver : beansOfType.values()){
                // 出现重复会进行覆盖
                classLanguageDriverMap.put(languageDriver.getClass(),languageDriver);
            }
        }

        if(classLanguageDriverMap.isEmpty()){
            return null;
        }
        return classLanguageDriverMap.values().toArray(new LanguageDriver[0]);
    }


}
