package com.github.zhanghui.integration.core.process.post.impl;

import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.core.process.pre.registrar.impl.InvokeBeanRegistrar;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.utils.AopUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * Description:
 * 在插件正式运行之前 对插件中的Caller 和 Supplier进行全局注册
 * <p>
 * 为什么放在这一步执行呢？ 因为需要插件的spring上下文准备就绪才行
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/24 14:33
 **/
public class PluginInvokeBeanPostProcessor implements PluginPostProcessor {

    @Override
    public void initialize() throws Exception {
        // do nothing
    }

    @Override
    public void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for (PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos) {

            AopUtils.resolveAop(pluginRegistryInfo);

            try {
                String pluginId = pluginRegistryInfo.getPluginWrapper().getPluginId();
                List<String> supplierBeanNames = pluginRegistryInfo.getSupplierBeanNames();

                if (supplierBeanNames.isEmpty()) {
                    continue;
                }
                for (String supplierBeanName : supplierBeanNames) {
                    GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
                    // 添加全局Supplier Bean
                    Object supplierBean = pluginApplicationContext.getBean(supplierBeanName);
                    InvokeBeanRegistrar.addSupplierManifested(pluginId, supplierBeanName, supplierBean);
                }
            } finally {
                AopUtils.recoverAop();
            }
        }
    }

    @Override
    public void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        // do nothing
    }
}
