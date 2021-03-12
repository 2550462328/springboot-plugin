package com.mybatis.plugin1.listener;

import com.github.zhanghui.realize.BasePlugin;
import com.github.zhanghui.realize.OneselfListener;
import com.github.zhanghui.utils.OrderPriority;

/**
 * Description:
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/1/28 16:11
 **/
public class OneSelfPluginEventListener implements OneselfListener {

    @Override
    public OrderPriority order() {
        return OrderPriority.getMiddlePriority();
    }

    @Override
    public void startEvent(BasePlugin basePlugin) {
        System.out.println("OneSelfPluginEventListener 监听自身插件启动事件：" + basePlugin);
    }

    @Override
    public void stopEvent(BasePlugin basePlugin) {
        System.out.println("OneSelfPluginEventListener 监听自身插件关闭事件：" + basePlugin);
    }
}
