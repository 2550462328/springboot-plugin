package com.github.zhanghui.integration.pf4j.support.provider;

import org.pf4j.PluginStatusProvider;

import java.util.Set;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 15:15
 **/
public class ConfigPluginStatusProvider implements PluginStatusProvider {

    private Set<String> enablePluginIds;
    private Set<String> disablePluginIds;

    public ConfigPluginStatusProvider(Set<String> enablePluginIds, Set<String> disablePluginIds) {
        this.enablePluginIds = enablePluginIds;
        this.disablePluginIds = disablePluginIds;
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        return disablePluginIds.contains("*") || disablePluginIds.contains(pluginId);
    }

    @Override
    public void disablePlugin(String pluginId) {
        if(isPluginDisabled(pluginId)){
            return;
        }
        disablePluginIds.add(pluginId);
        enablePluginIds.remove(pluginId);
    }

    @Override
    public void enablePlugin(String pluginId) {
        if(!isPluginDisabled(pluginId)){
            return;
        }
        enablePluginIds.add(pluginId);
        disablePluginIds.remove(pluginId);
    }
}
