package com.github.zhanghui.integration.pf4j;

import com.github.zhanghui.exception.MainContextRuntimeException;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.pf4j.support.finder.ResourcesPluginDescriptorFinder;
import com.github.zhanghui.integration.pf4j.support.provider.ConfigPluginStatusProvider;
import com.github.zhanghui.integration.pf4j.support.resolver.SortDependencyResolver;
import org.pf4j.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:54
 **/
public class DefaultPf4jApplicationContext implements Pf4jApplicationContext {

    private final IntegrationConfiguration integrationConfiguration;

    public DefaultPf4jApplicationContext(IntegrationConfiguration integrationConfiguration) {
        this.integrationConfiguration = integrationConfiguration;
    }

    @Override
    public PluginManager getPluginManager() {
        if (integrationConfiguration == null) {
            throw new MainContextRuntimeException("当前主版本配置信息获取不到，请先进行配置");
        }
        RuntimeMode environment = integrationConfiguration.environment();
        if (environment == null) {
            throw new MainContextRuntimeException("当前运行环境信息未配置");
        }
        List<String> sortedPluginIds = integrationConfiguration.sortInitPluginIds();
        // 开发环境下的插件配置
        if (RuntimeMode.DEVELOPMENT == environment) {
            Path path = Paths.get(getDevPluginDir(integrationConfiguration));
            return createDevPluginManager(path, sortedPluginIds);
        } else {
            Path path = Paths.get(getProdPluginDir(integrationConfiguration));
            return createProdPluginManager(path, sortedPluginIds);
        }
    }

    private String getDevPluginDir(IntegrationConfiguration configuration) {
        String pluginPath = configuration.pluginPath();
        if (Objects.equals("", pluginPath)) {
            pluginPath = "./plugins/";
        }
        return pluginPath;
    }

    private String getProdPluginDir(IntegrationConfiguration configuration) {
        String pluginPath = configuration.pluginPath();
        if (Objects.equals("", pluginPath)) {
            pluginPath = "plugins";
        }
        return pluginPath;
    }

    public static PluginDescriptorFinder getPluginDescriptorFinder(RuntimeMode runtimeMode) {
        if (runtimeMode == RuntimeMode.DEVELOPMENT) {
            return new CompoundPluginDescriptorFinder()
                    .add(new ResourcesPluginDescriptorFinder(RuntimeMode.DEVELOPMENT))
                    .add(new ManifestPluginDescriptorFinder());
        } else {
            return new CompoundPluginDescriptorFinder()
                    .add(new ResourcesPluginDescriptorFinder(RuntimeMode.DEPLOYMENT))
                    .add(new ManifestPluginDescriptorFinder());
        }
    }

    /**
     * dev环境创建PluginManager
     *
     * @param devPath
     * @param sortedPluginIds
     * @return
     */
    private PluginManager createDevPluginManager(Path devPath, List<String> sortedPluginIds){
        return new DefaultPluginManager(devPath) {

            @Override
            protected void initialize() {
                super.initialize();
                dependencyResolver = new SortDependencyResolver(sortedPluginIds,versionManager);
            }

            // 设置当前插件的运行模式
            @Override
            public RuntimeMode getRuntimeMode() {
                System.setProperty("pf4j.mode", RuntimeMode.DEVELOPMENT.toString());
                return RuntimeMode.DEVELOPMENT;
            }

            // PluginDescriptorFinder：根据插件版本中的plugin.resources文件读取Plugin信息
            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                return DefaultPf4jApplicationContext.getPluginDescriptorFinder(RuntimeMode.DEVELOPMENT);
            }

            //PluginStatusProvider：判断哪些插件是启用，哪些插件是禁用
            @Override
            protected PluginStatusProvider createPluginStatusProvider() {
                return new ConfigPluginStatusProvider(
                        integrationConfiguration.enablePluginIds(),
                        integrationConfiguration.disablePluginIds());
            }

            //PluginLoader：插件类信息加载器
            @Override
            protected PluginLoader createPluginLoader() {
                return new CompoundPluginLoader()
                        .add(new DevelopmentPluginLoader(this),this::isDevelopment);
            }

        };
    }

    /**
     * prod环境创建PluginManager
     *
     * @param prodPath
     * @param sortedPluginIds
     * @return
     */
    private PluginManager createProdPluginManager(Path prodPath, List<String> sortedPluginIds){
        return new DefaultPluginManager(prodPath) {

            @Override
            protected void initialize() {
                super.initialize();
                dependencyResolver = new SortDependencyResolver(sortedPluginIds,versionManager);
            }

            @Override
            public RuntimeMode getRuntimeMode() {
                System.setProperty("pf4j.mode", RuntimeMode.DEPLOYMENT.toString());
                return RuntimeMode.DEPLOYMENT;
            }

            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                return DefaultPf4jApplicationContext.getPluginDescriptorFinder(RuntimeMode.DEPLOYMENT);
            }

            @Override
            protected PluginStatusProvider createPluginStatusProvider() {
                return new ConfigPluginStatusProvider(
                        integrationConfiguration.enablePluginIds(),
                        integrationConfiguration.disablePluginIds());
            }

            @Override
            protected PluginLoader createPluginLoader() {
                return new CompoundPluginLoader()
                        .add(new JarPluginLoader(this),this::isNotDevelopment)
                        .add(new DefaultPluginLoader(this),this::isNotDevelopment);
            }
        };
    }
}
