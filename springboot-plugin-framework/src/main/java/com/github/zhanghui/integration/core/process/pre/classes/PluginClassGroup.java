package com.github.zhanghui.integration.core.process.pre.classes;

import com.github.zhanghui.realize.BasePlugin;

/**
 * Description:
 * 插件类分组抽象接口
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 12:39
 **/
public interface PluginClassGroup {

    /**
     * 组id
     * @return 组id
     */
    String groupId();

    /**
     * 初始化。每处理一个插件, 该方法调用一次。
     * @param basePlugin 当前插件信息
     */
    void initialize(BasePlugin basePlugin);


    /**
     * 过滤类。
     * @param aClass 类
     * @return 返回true.说明符合该分组器。false不符合该分组器
     */
    boolean filter(Class<?> aClass);
}
