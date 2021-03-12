package com.github.zhanghui.integration.core.process.post.impl;

import com.github.zhanghui.integration.core.process.post.PluginPostProcessor;
import com.github.zhanghui.integration.model.plugin.PluginRegistryInfo;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.realize.OneselfListener;
import com.github.zhanghui.utils.AopUtils;
import com.github.zhanghui.utils.CommonUtils;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description:
 * 处理插件本地事件监听器在插件注册和注销的事件
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/21 10:05
 **/
public class PluginOneselfListenerPostProcessor implements PluginPostProcessor {

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void registry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for (PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos) {

            AopUtils.resolveAop(pluginRegistryInfo);

            try {
                GenericApplicationContext pluginApplicationContext = pluginRegistryInfo.getPluginApplicationContext();
                BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
                Map<String, OneselfListener> beanOfType = pluginApplicationContext.getBeansOfType(OneselfListener.class);
                if (beanOfType == null || beanOfType.size() == 0) {
                    continue;
                }
                List<OneselfListener> oneselfListeners = new ArrayList<>(beanOfType.values());

                oneselfListeners.stream().filter(Objects::nonNull)
                        .sorted(CommonUtils.orderPriority(OneselfListener::order))
                        .forEach(oneselfListener -> {
                            // 插件事件监听器开始事件
                            oneselfListener.startEvent(basePlugin);
                        });
                // 添加扫描到的插件事件监听器到插件信息中
                pluginRegistryInfo.addOneSelfListeners(oneselfListeners);
            } finally {
                AopUtils.recoverAop();
            }
        }
    }

    @Override
    public void unRegistry(List<PluginRegistryInfo> pluginRegistryInfos) throws Exception {
        for (PluginRegistryInfo pluginRegistryInfo : pluginRegistryInfos) {

            BasePlugin basePlugin = pluginRegistryInfo.getBasePlugin();
            List<OneselfListener> oneselfListeners = pluginRegistryInfo.getOneselfListeners();

            if(oneselfListeners == null || oneselfListeners.size() == 0){
                continue;
            }
            oneselfListeners.forEach(oneselfListener -> {
                // 插件事件监听器停止事件
                oneselfListener.stopEvent(basePlugin);
            });
        }
    }
}
