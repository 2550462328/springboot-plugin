package com.mybatisplus.main.config;

import com.github.zhanghui.integration.configuration.DefaultIntegrationConfiguration;
import com.google.common.collect.Sets;
import org.pf4j.RuntimeMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;


/**
 * @Description:
 * @Author: starBlues
 * @Version: 1.0
 * @Create Date Time: 2019-05-25 12:36
 * @Update Date Time:
 * @see
 */
@Component
@ConfigurationProperties(prefix = "plugin")
public class PluginConfiguration extends DefaultIntegrationConfiguration {

    /**
     * 运行模式
     *  开发环境: development、dev
     *  生产/部署 环境: deployment、prod
     */
    @Value("${runMode:dev}")
    private String runMode;

    /**
     * 插件的路径
     */
    @Value("${pluginPath:plugins}")
    private String pluginPath;

    /**
     * 插件文件的路径
     */
    @Value("${pluginConfigFilePath:pluginConfigs}")
    private String pluginConfigFilePath;


    @Override
    public RuntimeMode environment() {
        return RuntimeMode.byName(runMode);
    }

    @Override
    public Set<String> enablePluginIds() {

        return Sets.newHashSet("integration-mybatisplus-plugin");
    }

    @Override
    public Set<String> disablePluginIds() {
        return Collections.emptySet();
    }

    @Override
    public String pluginPath() {
        return pluginPath;
    }

    @Override
    public String pluginConfigFilePath() {
        return pluginConfigFilePath;
    }

    /**
     * 重写上传插件包的临时存储路径。只适用于生产环境
     * @return String
     */
    @Override
    public String uploadTempPath() {
        return "temp";
    }

    /**
     * 重写插件备份路径。只适用于生产环境
     * @return String
     */
    @Override
    public String backupPath() {
        return "backupPlugin";
    }

    /**
     * 重写插件RestController请求的路径前缀
     * @return String
     */
    @Override
    public String pluginRestPathPrefix() {
        return "/api/plugin";
    }

    /**
     * 重写是否启用插件id作为RestController请求的路径前缀。
     * 启动则插件id会作为二级路径前缀。即: /api/plugin/pluginId/**
     * @return String
     */
    @Override
    public boolean enablePluginIdRestPathPrefix() {
        return true;
    }

    public String getRunMode() {
        return runMode;
    }

    public void setRunMode(String runMode) {
        this.runMode = runMode;
    }


    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getPluginConfigFilePath() {
        return pluginConfigFilePath;
    }

    public void setPluginConfigFilePath(String pluginConfigFilePath) {
        this.pluginConfigFilePath = pluginConfigFilePath;
    }

    @Override
    public String toString() {
        return "PluginArgConfiguration{" +
                "runMode='" + runMode + '\'' +
                ", pluginPath='" + pluginPath + '\'' +
                ", pluginConfigFilePath='" + pluginConfigFilePath + '\'' +
                '}';
    }
}
