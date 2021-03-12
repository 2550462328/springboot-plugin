package com.github.zhanghui.integration.core.operator;

import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.DefaultPluginFactory;
import com.github.zhanghui.integration.core.PluginFactory;
import com.github.zhanghui.integration.core.support.PluginFileHelper;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import com.github.zhanghui.integration.listener.support.PluginInitializerListenerManager;
import com.github.zhanghui.integration.listener.support.PluginListenerManager;
import com.github.zhanghui.integration.model.plugin.PluginInfo;
import com.github.zhanghui.integration.model.plugin.PluginOperateInfo;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Description:
 * 对插件的操作（启用、停止、卸载、装载）的缺省实现
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 18:55
 **/
@Slf4j
public class DefaultPluginOperator implements PluginOperator {

    private AtomicBoolean isPluginInitial = new AtomicBoolean(false);

    protected final ApplicationContext mainApplicationContext;
    protected final PluginManager pluginManager;
    protected final IntegrationConfiguration configuration;
    protected final PluginListenerManager pluginListenerManager;
    protected final PluginInitializerListenerManager pluginInitializerListenerManager;
    protected final PluginFactory pluginFactory;

    public DefaultPluginOperator(ApplicationContext mainApplicationContext, PluginManager pluginManager, IntegrationConfiguration configuration, PluginListenerManager pluginListenerManager) {
        Objects.requireNonNull(mainApplicationContext, "主配置上下文环境不能为空");
        Objects.requireNonNull(configuration, "当前环境必要配置不得为空");
        Objects.requireNonNull(pluginManager, "插件辅助类不能为空");
        this.mainApplicationContext = mainApplicationContext;
        this.pluginManager = pluginManager;
        this.configuration = configuration;
        this.pluginListenerManager = pluginListenerManager;
        this.pluginInitializerListenerManager = new PluginInitializerListenerManager(mainApplicationContext);
        this.pluginFactory = new DefaultPluginFactory(mainApplicationContext, pluginListenerManager, configuration);
    }

    /**
     * 插件环境初始化
     *
     * @param pluginInitializerListener 插件初始化监听者
     * @return
     * @throws Exception
     */
    @Override
    public synchronized boolean initPlugins(PluginInitializerListener pluginInitializerListener) throws Exception {
        // 判断当前插件环境是否已经初始化了
        if (isPluginInitial.get()) {
            throw new PluginContextRuntimeException("当前插件的环境已经初始化了");
        }
        pluginInitializerListenerManager.addPluginInitializerListeners(pluginInitializerListener);
        log.info("插件环境准备初始化，配置插件目录 [{}]", pluginManager.getPluginsRoots().toString());

        try {
            //  插件初始化事件监听器 开始
            pluginInitializerListenerManager.before();
            if (!configuration.enable()) {
                log.warn("当前配置禁止加载插件，插件加载失败");
                pluginInitializerListenerManager.complete();
                return false;
            }
            // 清理配置插件目录下的空文件（夹）
            PluginFileHelper.cleanEmptyFile(pluginManager.getPluginsRoot());
            // 插件加工厂环境初始化
            pluginFactory.initialize();

            // 装载插件环境信息和资源到内存中
            pluginManager.loadPlugins();
            // 启动插件（更新插件状态）
            pluginManager.startPlugins();
            List<PluginWrapper> pluginWrappers = pluginManager.getStartedPlugins();
            if (pluginWrappers == null || pluginWrappers.isEmpty()) {
                log.warn("在[{}]目录下没有找到可以加载和启动的插件", pluginManager.getPluginsRoots().toString());
                return false;
            }
            // 插件在registry过程中是否出现异常
            boolean ifRegistryException = false;
            for (PluginWrapper pluginWrapper : pluginWrappers) {
                String pluginId = pluginWrapper.getPluginId();
                // 记录插件的操作状态
                addOperatorPluginInfo(pluginId, PluginOperateInfo.OperateType.INSTALL, false);

                try {
                    // 插件加工厂 注册单个插件到主版本中
                    pluginFactory.registry(PluginRegistryInfo.build(pluginWrapper, pluginManager, mainApplicationContext, true));
                } catch (Exception e) {
                    log.error("插件 [{}] 注册失败：[{}]", pluginId, e.getMessage(), e);
                    ifRegistryException = true;
                }
            }
            // 插件加工厂 启动前对插件的操作
            pluginFactory.build();
            isPluginInitial.set(true);

            if (ifRegistryException) {
                return false;
            } else {
                log.info("插件配置目录[{}]下的插件全部加载成功", pluginManager.getPluginsRoots().toString());
                // 插件初始化事件监听器 完成
                pluginInitializerListenerManager.complete();
                return true;
            }
        } catch (Exception e) {
            log.error("插件初始化异常：[{}]", e.getMessage(), e);
            e.printStackTrace();
            // 插件初始化事件监听器 异常
            pluginInitializerListenerManager.failure(e);
            throw new PluginContextRuntimeException("插件初始化异常:" + e.getMessage());
        }
    }

    /**
     * 启用插件
     *
     * @param pluginId 插件id
     * @return
     * @throws Exception
     */
    @Override
    public boolean start(String pluginId) throws Exception {
        PluginWrapper pluginWrapper = getPluginWrapper(pluginId);
        if(pluginWrapper == null || pluginWrapper.getPluginState() == PluginState.STARTED){
            throw new PluginContextRuntimeException("当前插件无效，启用失败");
        }
        try {
            PluginState pluginState = pluginManager.startPlugin(pluginId);
            if(pluginState == PluginState.STARTED){
                addOperatorPluginInfo(pluginId, PluginOperateInfo.OperateType.START,false);
                pluginFactory.registry(PluginRegistryInfo.build(pluginWrapper,pluginManager,mainApplicationContext,false));
                pluginFactory.build();
                log.info("插件 [{}] 启用成功",pluginId);
                return true;
            }
            log.error("插件 [{}] 启用失败，当前插件状态为 [{}]",pluginId,pluginState);
        } catch (Exception e) {
            log.error("插件 [{}] 启用失败：[{}]",pluginId,e.getMessage(),e);
            log.error("准备开始卸载插件 [{}]",pluginId);
            // 卸载插件残留
            this.stop(pluginId);
        }
        return false;
    }

    /**
     * 停止插件
     *
     * @param pluginId 插件id
     * @return
     * @throws Exception
     */
    @Override
    public boolean stop(String pluginId) throws Exception {
        PluginWrapper pluginWrapper = getPluginWrapper(pluginId);
        if(pluginWrapper == null || pluginWrapper.getPluginState() != PluginState.STARTED){
            throw new PluginContextRuntimeException("当前插件无效，停止失败");
        }

        try {
            pluginFactory.unRegistry(pluginId);
            pluginFactory.build();

            PluginState pluginState = pluginManager.stopPlugin(pluginId);

            if(pluginState == PluginState.STOPPED){
                log.info("插件 [{}] 停止成功",pluginId);
                return true;
            }
        } catch (Exception e) {
            log.error("插件 [{}] 停止出现异常：[{}]",pluginId,e.getMessage(),e);
        }
        return false;
    }

    @Override
    public List<PluginInfo> getPluginInfo() {
        List<PluginWrapper> pluginWrappers = getPluginWrapper();

        return pluginWrappers.stream().filter(Objects::nonNull)
                .map(pluginWrapper -> this.getPluginInfo(pluginWrapper))
                .collect(Collectors.toList());
    }

    @Override
    public PluginInfo getPluginInfo(String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if(pluginWrapper == null){
            return null;
        }
        return getPluginInfo(pluginWrapper);
    }

    @Override
    public List<PluginWrapper> getPluginWrapper() {
        return pluginManager.getPlugins();
    }

    @Override
    public PluginWrapper getPluginWrapper(String pluginId) {
        return pluginManager.getPlugin(pluginId);
    }

    /**
     * 获取单个插件基本信息
     * @param pluginWrapper
     * @return
     */
    private PluginInfo getPluginInfo(PluginWrapper pluginWrapper){
        return new PluginInfo(pluginWrapper.getDescriptor(),pluginWrapper.getPluginState(),pluginWrapper.getPluginPath().toAbsolutePath().toString(),pluginWrapper.getRuntimeMode().toString());
    }
}
