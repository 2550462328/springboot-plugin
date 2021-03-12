package com.github.zhanghui.integration.core.operator;

import com.github.zhanghui.exception.PluginContextRuntimeException;
import com.github.zhanghui.integration.configuration.IntegrationConfiguration;
import com.github.zhanghui.integration.core.support.PluginContextHelper;
import com.github.zhanghui.integration.listener.PluginInitializerListener;
import com.github.zhanghui.integration.model.plugin.PluginInfo;
import com.github.zhanghui.realize.UnRegistryValidator;
import com.google.common.collect.Lists;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * PluginOperator的包装类
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/22 16:08
 **/
public class PluginOperatorWrapper implements PluginOperator {

    private final PluginOperator pluginOperator;
    private final IntegrationConfiguration configuration;

    public PluginOperatorWrapper(PluginOperator pluginOperator, IntegrationConfiguration configuration) {
        this.pluginOperator = pluginOperator;
        this.configuration = configuration;
    }

    @Override
    public boolean initPlugins(PluginInitializerListener pluginInitializerListener) throws Exception {
        return this.pluginOperator.initPlugins(pluginInitializerListener);
    }

    @Override
    public boolean start(String pluginId) throws Exception {
        if (!isPluginEnable()) {
            return false;
        }
        return pluginOperator.start(pluginId);
    }

    @Override
    public boolean stop(String pluginId) throws Exception {
        if (!isPluginEnable()) {
            return false;
        }
        // 校验是否可以卸载插件
        checkWhetherUnRegistry(pluginId);

        return pluginOperator.stop(pluginId);
    }

    @Override
    public List<PluginInfo> getPluginInfo() {
        if(!isPluginEnable()) {
            return Collections.emptyList();
        }
        return pluginOperator.getPluginInfo();
    }

    @Override
    public PluginInfo getPluginInfo(String pluginId) {
        if(!isPluginEnable()) {
            return null;
        }
        return pluginOperator.getPluginInfo(pluginId);
    }

    @Override
    public List<PluginWrapper> getPluginWrapper() {
        if(!isPluginEnable()) {
            return Collections.emptyList();
        }
        return pluginOperator.getPluginWrapper();
    }

    @Override
    public PluginWrapper getPluginWrapper(String pluginId) {
        if(!isPluginEnable()) {
            return null;
        }
        return pluginOperator.getPluginWrapper(pluginId);
    }

    /**
     * 判断插件功能是否开启
     *
     * @return
     */
    private boolean isPluginEnable() {
        return configuration.enable();
    }

    /**
     * 判断是否允许卸载插件
     */
    private void checkWhetherUnRegistry(String pluginId) throws Exception {
        GenericApplicationContext pluginApplicationContext = PluginContextHelper.getPluginApplicationContext(pluginId);
        if (pluginApplicationContext == null) {
            throw new PluginContextRuntimeException("无效的插件 " + pluginId + "请核对");
        }
        Map<String, UnRegistryValidator> beanOfType = pluginApplicationContext.getBeansOfType(UnRegistryValidator.class);
        if (beanOfType == null || beanOfType.isEmpty()) {
            return;
        }
        List<UnRegistryValidator> unRegistryValidators = Lists.newArrayList(beanOfType.values());
        for (UnRegistryValidator unRegistryValidator : unRegistryValidators) {
            UnRegistryValidator.Result validatorResult = unRegistryValidator.verify();
            if (validatorResult.isVerify()) {
                return;
            }
            String message = validatorResult.getMessage();
            throw new PluginContextRuntimeException("插件" + pluginId + "禁止卸载：" + message);
        }


    }
}
