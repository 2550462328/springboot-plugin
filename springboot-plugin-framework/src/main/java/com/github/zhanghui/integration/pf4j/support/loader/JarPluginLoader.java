package com.github.zhanghui.integration.pf4j.support.loader;

import org.pf4j.*;
import org.pf4j.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Description:
 * 生产环境下基于jar包加载插件信息
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 15:01
 **/
public class JarPluginLoader implements PluginLoader {

    protected PluginManager pluginManager;

    public JarPluginLoader(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && FileUtils.isJarFile(pluginPath);
    }

    @Override
    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
        // 加载顺序主Application -> Plugin -> Dependency
        PluginClassLoader pluginClassLoader = new PluginClassLoader(pluginManager,pluginDescriptor,this.getClass().getClassLoader(),ClassLoadingStrategy.APD);
        pluginClassLoader.addFile(pluginPath.toFile());
        return pluginClassLoader;
    }
}
