package com.github.zhanghui.integration.configuration;

import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/22 17:19
 **/
public abstract class DefaultIntegrationConfiguration implements IntegrationConfiguration {

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public String uploadTempPath() {
        return "temp";
    }

    @Override
    public String backupPath() {
        return "backupPlugin";
    }

    @Override
    public String pluginRestPathPrefix() {
        return "/plugins";
    }

    @Override
    public boolean enablePluginIdRestPathPrefix() {
        return true;
    }

    @Override
    public Set<String> enablePluginIds() {
        return null;
    }

    @Override
    public Set<String> disablePluginIds() {
        return null;
    }

    @Override
    public boolean enableSwaggerRefresh() {
        return true;
    }

    @Override
    public List<String> sortInitPluginIds() {
        return null;
    }
}
