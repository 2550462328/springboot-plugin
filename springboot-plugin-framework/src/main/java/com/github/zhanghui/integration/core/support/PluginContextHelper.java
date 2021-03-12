package com.github.zhanghui.integration.core.support;

import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 插件上下文操作的帮助类
 * 目前存放已注册插件的CRUD
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 14:13
 **/
public class PluginContextHelper {

    /**
     * 已注册的插件上下文信息列表
     */
    private static final Map<String, GenericApplicationContext> PLUGIN_APPLICATION_CONTEXT_MAP = new ConcurrentHashMap<>();

    /**
     * 已注册的插件列表
     * key是pluginId value是插件的注册信息
     */
    private static final Map<String, PluginRegistryInfo> PLUGIN_REGISTRY_INFO_MAP = new ConcurrentHashMap<>();

    public static void addPluginApplicationContext(String pluginId, GenericApplicationContext applicationContext){
        PLUGIN_APPLICATION_CONTEXT_MAP.put(pluginId, applicationContext);
    }

    public static void removePluginApplicationContext(String pluginId){
        PLUGIN_APPLICATION_CONTEXT_MAP.remove(pluginId);
    }

    public static GenericApplicationContext getPluginApplicationContext(String pluginId) {
       return  PLUGIN_APPLICATION_CONTEXT_MAP.get(pluginId);
    }

    static public List<ApplicationContext> getPluginApplicationContexts() {
        Collection<GenericApplicationContext> values = PLUGIN_APPLICATION_CONTEXT_MAP.values();
        if(values.isEmpty()){
            return Collections.emptyList();
        }
        return new ArrayList<>(values);
    }

    public static void addPluginPluginRegistryInfo(String pluginId, PluginRegistryInfo pluginRegistryInfo){
        PLUGIN_REGISTRY_INFO_MAP.put(pluginId, pluginRegistryInfo);
    }

    public static void removePluginRegistryInfo(String pluginId){
        PLUGIN_REGISTRY_INFO_MAP.remove(pluginId);
    }

    public static PluginRegistryInfo getPluginRegistryInfo(String pluginId) {
        return  PLUGIN_REGISTRY_INFO_MAP.get(pluginId);
    }

    static public List<PluginRegistryInfo> getPluginRegistryInfos() {
        Collection<PluginRegistryInfo> values = PLUGIN_REGISTRY_INFO_MAP.values();
        if(values.isEmpty()){
            return Collections.emptyList();
        }
        return new ArrayList<>(values);
    }
}
