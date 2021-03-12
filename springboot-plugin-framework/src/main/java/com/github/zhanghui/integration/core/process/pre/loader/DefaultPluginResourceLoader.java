package com.github.zhanghui.integration.core.process.pre.loader;

import com.github.zhanghui.utils.OrderPriority;
import com.github.zhanghui.utils.ScanUtils;
import com.github.zhanghui.integration.core.process.pre.loader.bean.ResourceWrapper;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.BasePlugin;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;

import java.io.IOException;
import java.util.Set;

/**
 * Description:
 * 缺省的插件类加载器（获取插件中的类）
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/6 10:48
 **/
public class DefaultPluginResourceLoader implements PluginResourceLoader {

    @Override
    public String key() {
        return DEFAULT_PLUGIN_RESOURCE_LOADER_KEY;
    }

    @Override
    public ResourceWrapper load(PluginRegistryInfo pluginRegistryInfo) throws IOException{
        BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
        String scanPackage = basePlugin.getScanPackage();

        PluginWrapper pluginWrapper = pluginRegistryInfo.getPluginWrapper();
        Set<String> scanPackageClasses;
        if(pluginWrapper.getRuntimeMode() == RuntimeMode.DEVELOPMENT){
            scanPackageClasses = ScanUtils.scanClassPackageName(scanPackage,basePlugin.getClass());
        }else{
            scanPackageClasses = ScanUtils.scanClassPackageName(scanPackage,basePlugin.getWrapper());
        }

        ResourceWrapper resourceWrapper = new ResourceWrapper();
        resourceWrapper.addClassPackageNames(scanPackageClasses);

        return resourceWrapper;
    }

    @Override
    public void unload(PluginRegistryInfo pluginRegistryInfo, ResourceWrapper resourceWrapper) {
        //do nothing
    }

    @Override
    public OrderPriority order() {
        return OrderPriority.getHighPriority();
    }
}
