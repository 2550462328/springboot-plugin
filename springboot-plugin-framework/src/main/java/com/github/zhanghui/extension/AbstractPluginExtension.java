package com.github.zhanghui.extension;

import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessor;
import com.github.zhanghui.integration.core.process.pre.classes.PluginClassGroup;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.registrar.PluginBeanRegistrar;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * Description:
 * 用于扩展插件的功能 比如支持数据库操作 支持静态资源访问 等等
 *
 * 核心也是将扩展的内容加载到插件的Spring上下文环境中
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:16
 **/
public abstract class AbstractPluginExtension {

    /**
     * 扩展标识 唯一key
     *
     * @return
     */
    public abstract String key();


    /**
     * 该扩展初始化的操作
     * 主要是在插件初始化阶段被调用
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @throws Exception 初始化异常
     */
    public void initialize(ApplicationContext mainApplicationContext) throws Exception{
    }

    /**
     * 返回插件的资源加载器
     * 主要是加载插件中的某些资源，比如文件、图片等
     *
     * @return List PluginResourceLoader
     */
    public List<PluginResourceLoader> getPluginResourceLoaders(){
        return null;
    }


    /**
     * 返回扩展的插件中的类分组器。
     * 该扩展主要是对插件中的Class文件分组，然后供 PluginPreProcessor、PluginPostProcessor 阶段使用。
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @return List PluginClassGroup
     */
    public List<PluginClassGroup> getPluginClassGroups(ApplicationContext mainApplicationContext){
        return null;
    }


    /**
     * 返回扩展的插件前置处理者。
     * 该扩展会对每一个插件进行处理
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @return List PluginPreProcessor
     */
    public List<PluginPreProcessor> getPluginPreProcessors(ApplicationContext mainApplicationContext){
        return null;
    }

    /**
     * 返回扩展的bean定义注册者扩展
     * 该扩展会对每一个插件进行处理
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @return List PluginBeanRegistrar
     */
    public List<PluginBeanRegistrar> getPluginBeanRegistrars(ApplicationContext mainApplicationContext){
        return null;
    }

    /**
     * 返回扩展的插件后置处理者。
     * 该扩展会对全部插件进行处理。
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @return List PluginPostProcessor
     */
    public List<PluginPostProcessor> getPluginPostProcessors(ApplicationContext mainApplicationContext){
        return null;
    }

    /**
     * 返回扩展的插件后置处理者。
     * 该扩展会对每一个插件进行处理
     *
     * @param mainApplicationContext 主程序ApplicationContext
     * @return List PluginAfterPreProcessor
     */
    public List<PluginAfterPreProcessor> getPluginAfterPreProcessors(ApplicationContext mainApplicationContext){
        return null;
    }
}
