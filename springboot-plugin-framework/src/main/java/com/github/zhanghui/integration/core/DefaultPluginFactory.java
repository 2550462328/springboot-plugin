package com.github.zhanghui.integration.core;

import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.process.post.PluginPostProcessorManager;
import com.github.zhanghui.integration.core.process.pre.PluginPreProcessorManager;
import com.github.zhanghui.integration.core.support.PluginContextHelper;
import com.github.zhanghui.integration.listener.PluginListener;
import com.github.zhanghui.integration.listener.extension.SwaggerRefreshPluginListener;
import com.github.zhanghui.integration.listener.support.PluginListenerManager;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * 控制插件的生命周期（启用、停止、装载和卸载）的缺省实现类
 * 对每个插件都会进行 initialize（只执行一次） -> registry -> build操作 包括unRegistry操作
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 17:33
 **/
public class DefaultPluginFactory implements PluginFactory {

    private final ApplicationContext mainApplicationContext;
    /**
     * 插件监听器管理器
     */
    private final PluginListenerManager pluginListenerManager;

    private final IntegrationConfiguration configuration;
    /**
     * 插件环境准备阶段的Processor
     */
    private final PluginPreProcessorManager pluginPreProcessorManager;
    /**
     * 插件环境准备阶段后 运行前的Processor
     */
    private final PluginPostProcessorManager pluginPostProcessorManager;

    /**
     * 需要在注册或者注销事件后处理的插件信息列表
     */
    private List<PluginRegistryInfo> buildPluginRegistryList = new ArrayList<>();

    /**
     * 当前插件存在的生命周期，默认Build
     */
    private BuildType buildType = BuildType.BUILD;

    public DefaultPluginFactory(ApplicationContext mainApplicationContext, PluginListenerManager pluginListenerManager, IntegrationConfiguration configuration) {
        Objects.requireNonNull(mainApplicationContext);
        Objects.requireNonNull(configuration);
        this.mainApplicationContext = mainApplicationContext;
        this.pluginListenerManager = pluginListenerManager;
        this.configuration = configuration;
        this.pluginPreProcessorManager = new PluginPreProcessorManager(mainApplicationContext);
        this.pluginPostProcessorManager = new PluginPostProcessorManager(mainApplicationContext);

        // 从mainApplicationContext获取所有的代理对象 并封装成ProxyWrapper
        AopUtils.registry(mainApplicationContext);
    }

    @Override
    public void initialize() throws Exception {
        // 添加默认的插件事件监听器，可扩展
        addDefaultPluginListener();
        pluginPreProcessorManager.initialize();
        pluginPostProcessorManager.initialize();
    }

    @Override
    public synchronized PluginFactory registry(PluginRegistryInfo pluginRegistryInfo) throws Exception {
        String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
        if(PluginContextHelper.getPluginRegistryInfo(pluginId) != null){
            throw new PluginContextRuntimeException("插件" + pluginId + "已经注册，无须重复注册");
        }
        // 只有当插件生命周期为BUILD或者REGISTER的时候才允许注册插件
        if(!buildPluginRegistryList.isEmpty() && buildType == BuildType.UNINSTALL){
            throw new PluginContextRuntimeException("插件" + pluginId + "注册失败，当前非插件的注册周期");
        }
        try {
            pluginPreProcessorManager.registry(pluginRegistryInfo);

            PluginContextHelper.addPluginPluginRegistryInfo(pluginId,pluginRegistryInfo);
            buildPluginRegistryList.add(pluginRegistryInfo);

            return this;
        } catch (Exception e) {
            pluginListenerManager.failure(pluginId,e);
            throw e;
        }finally {
            buildType = BuildType.REGISTER;
        }
    }

    @Override
    public PluginFactory unRegistry(String pluginId) throws Exception {
        PluginRegistryInfo pluginRegistryInfo = PluginContextHelper.getPluginRegistryInfo(pluginId);

        if(pluginRegistryInfo == null){
            throw new PluginContextRuntimeException("插件" + pluginId + "没有注册，注销失败");
        }
        // 只有当插件生命周期为BUILD或者UNINSTALL的时候才允许注销插件
        if(!buildPluginRegistryList.isEmpty() && buildType == BuildType.REGISTER){
            throw new PluginContextRuntimeException("插件" + pluginId + "注册失败，当前非插件的注销周期");
        }

        try {
            pluginPreProcessorManager.unRegistry(pluginRegistryInfo);
            // 将待build的插件放到列表中，等所有插件registry之后再统一build
            buildPluginRegistryList.add(pluginRegistryInfo);
            return this;
        } catch (Exception e) {
            // 注销出现异常情况需要销毁插件信息
            pluginRegistryInfo.destroy();
            pluginListenerManager.failure(pluginId,e);
            throw e;
        } finally {
            PluginContextHelper.removePluginRegistryInfo(pluginId);
            buildType = BuildType.UNINSTALL;
        }
    }

    @Override
    public void build() throws Exception {
        if(buildPluginRegistryList.isEmpty()){
            return;
        }
        pluginListenerManager.injectPluginListenerClasses((GenericApplicationContext) mainApplicationContext);

        try {
            if(buildType == BuildType.REGISTER){
                registryBuild();
            }else if(buildType == BuildType.UNINSTALL){
                unRegistryBuild();
            }
        } finally {
            if(buildType == BuildType.UNINSTALL){
                for(PluginRegistryInfo pluginRegistryInfo : buildPluginRegistryList){
                    pluginRegistryInfo.destroy();
                }
            }
            buildPluginRegistryList.clear();
            buildType = BuildType.BUILD;
        }
    }

    /**
     * 插件注册时 运行前的构建
     */
    private void  registryBuild() throws Exception{
        pluginPostProcessorManager.registry(buildPluginRegistryList);

        for(PluginRegistryInfo pluginRegistryInfo : buildPluginRegistryList){
            pluginListenerManager.registry(pluginRegistryInfo.getPluginWrapper().getPluginId(),pluginRegistryInfo.isFollowingInitial());
        }
    }

    /**
     * 插件注销时的构建
     */
    private void unRegistryBuild() throws Exception{
        pluginPostProcessorManager.unRegistry(buildPluginRegistryList);
        for(PluginRegistryInfo pluginRegistryInfo : buildPluginRegistryList){
            pluginListenerManager.unRegistry(pluginRegistryInfo.getPluginWrapper().getPluginId());
        }
    }

    @Override
    public void addListener(PluginListener pluginListener) {
        pluginListenerManager.addPluginListener(pluginListener);
    }

    @Override
    public void addListener(Class<? extends PluginListener> clazz) {
        pluginListenerManager.addPluginListener(clazz);
    }

    @Override
    public void addListener(List<? extends PluginListener> pluginListeners) {
        if (pluginListeners != null && !pluginListeners.isEmpty()) {
            for (PluginListener pluginListener : pluginListeners) {
                pluginListenerManager.addPluginListener(pluginListener);
            }
        }
    }

    /**
     * 添加默认的插件事件监听器
     */
    private void addDefaultPluginListener() {
        if (configuration.enableSwaggerRefresh()) {
            pluginListenerManager.addPluginListener(new SwaggerRefreshPluginListener(mainApplicationContext));
        }
    }

    /**
     * 当前插件构造的生命周期
     */
    public enum BuildType {
        //初始化
        BUILD,
        // 注册
        REGISTER,
        // 注销
        UNINSTALL;
    }
}
