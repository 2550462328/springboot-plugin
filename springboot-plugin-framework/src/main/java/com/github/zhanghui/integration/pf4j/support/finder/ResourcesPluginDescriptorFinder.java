package com.github.zhanghui.integration.pf4j.support.finder;

import com.github.zhanghui.exception.MainContextRuntimeException;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.pf4j.RuntimeMode;
import org.pf4j.util.FileUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Description:
 *  获取插件信息
 *  读取 resources 目录下的 plugin.properties 文件
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 14:40
 **/
public class ResourcesPluginDescriptorFinder extends PropertiesPluginDescriptorFinder {

    private final RuntimeMode runtimeMode;

    public ResourcesPluginDescriptorFinder(RuntimeMode runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        pluginPath = getPluginPath(pluginPath);
        return super.isApplicable(pluginPath);
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        pluginPath = getPluginPath(pluginPath);
        return super.find(pluginPath);
    }

    @Override
    protected Properties readProperties(Path pluginPath) {
        Path propertiesPath = getPropertiesPath(pluginPath,propertiesFileName);
        return getProperties(propertiesPath);
    }

    private Path getPluginPath(Path pluginPath){
        if(runtimeMode == RuntimeMode.DEPLOYMENT){
            return pluginPath;
        }else if(runtimeMode == RuntimeMode.DEVELOPMENT){
            return Paths.get(pluginPath.toString(),"src","main","resources");
        }else {
            throw new MainContextRuntimeException("未知的插件运行环境：["+ runtimeMode +"]");
        }
    }

    private static Properties getProperties(Path propertiesPath){
        if (propertiesPath == null) {
            throw new PluginRuntimeException("Cannot find the properties path");
        }


        if (Files.notExists(propertiesPath)) {
            throw new PluginRuntimeException("Cannot find '{}' path", propertiesPath);
        }

        Properties properties = new Properties();

        try (InputStreamReader input = new InputStreamReader(Files.newInputStream(propertiesPath),
                StandardCharsets.UTF_8)) {
            properties.load(input);
        } catch (IOException e) {
            throw new PluginRuntimeException(e);
        } finally {
            FileUtils.closePath(propertiesPath);
        }

        return properties;
    }
}
