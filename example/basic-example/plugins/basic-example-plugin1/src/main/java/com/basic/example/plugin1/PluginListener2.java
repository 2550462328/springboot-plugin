package com.basic.example.plugin1;

import com.basic.example.plugin1.config.PluginConfig1;
import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.realize.OneselfListener;
import com.github.zhanghui.utils.OrderPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description
 *
 * @author starBlues
 * @version 1.0
 */
public class PluginListener2 implements OneselfListener {

    private static final Logger logger = LoggerFactory.getLogger(PluginListener2.class);

    private final PluginConfig1 pluginConfig1;

    public PluginListener2(PluginConfig1 pluginConfig1) {
        this.pluginConfig1 = pluginConfig1;
    }


    @Override
    public OrderPriority order() {
        return OrderPriority.getMiddlePriority();
    }

    @Override
    public void startEvent(BasePlugin basePlugin) {
        logger.info("PluginListener2 {} start. pluginConfig1 : {} .", basePlugin.getWrapper().getPluginId(),
                pluginConfig1.getName());
    }

    @Override
    public void stopEvent(BasePlugin basePlugin) {
        logger.info("PluginListener2 {} stop. pluginConfig1 : {} .", basePlugin.getWrapper().getPluginId(),
                pluginConfig1.getName());
    }
}
