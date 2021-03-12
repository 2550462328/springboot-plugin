package com.github.zhanghui.integration.core.process.pre.loader;

import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.integration.core.process.pre.loader.PluginResourceLoader;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.utils.OrderPriority;
import com.google.common.collect.Lists;
import org.pf4j.PluginRuntimeException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 16:54
 **/
public class PluginConfigFileResourceLoader implements PluginResourceLoader {

    private static final String PLUGIN_RESOURCE_LOADER_KEY = "pluginResourceLoader";

    private final String pluginConfigPath;

    private final String fileName;

    public PluginConfigFileResourceLoader(String pluginConfigPath, String fileName) {
        this.pluginConfigPath = pluginConfigPath;
        this.fileName = fileName;
    }

    @Override
    public String key() {
        return PLUGIN_RESOURCE_LOADER_KEY;
    }

    @Override
    public ResourceWrapper load(PluginRegistryInfo pluginRegistryInfo){

        List<Supplier<Resource>> resolveResources = Lists.newArrayList();
        // 想方设法的加载fileName配置文件
        resolveResources.add(findInPluginConfigPath());
        resolveResources.add(findInPluginPath(pluginRegistryInfo));
        resolveResources.add(findInPluginClassPath(pluginRegistryInfo));

        for (Supplier<Resource> resourceSupplier : resolveResources) {
            Resource resource = resourceSupplier.get();
            if (resource.exists()) {
                List<Resource> pluginResource = Lists.newArrayList(resource);
                ResourceWrapper resourceWrapper = new ResourceWrapper();
                resourceWrapper.addResources(pluginResource);
                return resourceWrapper;
            }
        }

        throw new PluginContextRuntimeException("找不到指定的插件配置文件：[" + fileName + "]");
    }

    @Override
    public void unload(PluginRegistryInfo pluginRegistryInfo, ResourceWrapper resourceWrapper) {
        //do nothing
    }

    @Override
    public OrderPriority order() {
        return OrderPriority.getHighPriority().down(20);
    }

    /**
     * 直接从传入的pluginConfigPath目录下查找fileName文件
     *
     * @return
     */
    private Supplier<Resource> findInPluginConfigPath() {
        return () -> {
            String filePath = pluginConfigPath + File.separator + fileName;
            Resource resource = new FileSystemResource(filePath);
            return resource;
        };
    }

    /**
     * 从加载的插件根目录下查找fileName文件
     *
     * @return
     */
    private Supplier<Resource> findInPluginPath(PluginRegistryInfo pluginRegistryInfo) {
        return () -> {
            BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
            Path pluginPath = basePlugin.getWrapper().getPluginPath();
            String rootPath = pluginPath.getParent().toString();
            String filePath = rootPath + File.separator + fileName;
            Resource resource = new FileSystemResource(filePath);
            return resource;
        };
    }

    /**
     * 从插件的运行环境下的
     *
     * @return
     */
    private Supplier<Resource> findInPluginClassPath(PluginRegistryInfo pluginRegistryInfo) {
        return () -> {
            ClassLoader pluginClassLoader = pluginRegistryInfo.getDefaultPluginClassLoader();
            Resource resource = new ClassPathResource("/" + fileName, pluginClassLoader);
            return resource;
        };
    }
}
