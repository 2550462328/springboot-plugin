package com.github.zhanghui.integration.pf4j;

import org.pf4j.PluginManager;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:51
 **/
public interface Pf4jApplicationContext {

    /**
     * 获得插件管理信息（pf4j）
     * @return
     */
    PluginManager getPluginManager();
}
