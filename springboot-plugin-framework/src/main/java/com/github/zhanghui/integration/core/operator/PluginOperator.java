package com.github.zhanghui.integration.core.operator;

import com.github.zhanghui.integration.listener.PluginInitializerListener;
import com.github.zhanghui.integration.model.plugin.PluginInfo;
import com.github.zhanghui.integration.model.plugin.PluginOperateInfo;
import org.pf4j.PluginWrapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 操作插件的接口
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/3 10:12
 **/
public interface PluginOperator {

    Map<String, PluginOperateInfo> operatorPluginInfos = new ConcurrentHashMap();

    /**
     * 初始化插件。该方法只能执行一次。
     *
     * @param pluginInitializerListener 插件初始化监听者
     * @return 成功: 返回true; 失败: 抛出异常或者返回false
     * @throws Exception 异常信息
     */
    boolean initPlugins(PluginInitializerListener pluginInitializerListener) throws Exception;

    /**
     * 启用插件 [适用于生产环境、开发环境]
     *
     * @param pluginId 插件id
     * @return 成功返回true.不成功抛出异常或者返回false
     * @throws Exception 异常信息
     */
    boolean start(String pluginId) throws Exception;

    /**
     * 停止插件 [适用于生产环境、开发环境]
     *
     * @param pluginId 插件id
     * @return 成功: 返回true; 失败: 抛出异常或者返回false
     * @throws Exception 异常信息
     */
    boolean stop(String pluginId) throws Exception;

    /**
     * 获取插件信息 [适用于生产环境、开发环境]
     *
     * @return 返回插件信息列表
     */
    List<PluginInfo> getPluginInfo();

    /**
     * 根据插件id获取插件信息 [适用于生产环境、开发环境]
     *
     * @param pluginId 插件id
     * @return 插件信息
     */
    PluginInfo getPluginInfo(String pluginId);

    /**
     * 得到所有插件的包装类 [适用于生产环境、开发环境]
     *
     * @return 返回插件包装类集合
     */
    List<PluginWrapper> getPluginWrapper();

    /**
     * 通过插件id得到插件的包装类 [适用于生产环境、开发环境]
     *
     * @param pluginId 插件id
     * @return 返回插件包装类集合
     */
    PluginWrapper getPluginWrapper(String pluginId);

    /**
     * 添加插件操作信息
     * @param pluginId 插件id
     * @param operatorType 操作类型
     * @param isLock 是否加锁
     */
    default void addOperatorPluginInfo(String pluginId, PluginOperateInfo.OperateType operatorType, boolean isLock) {
        synchronized (PluginOperateInfo.class) {
            PluginOperateInfo operatorPluginInfo = operatorPluginInfos.get(pluginId);
            if (operatorPluginInfo == null) {
                operatorPluginInfo = new PluginOperateInfo();
                operatorPluginInfos.put(pluginId, operatorPluginInfo);
            }
            operatorPluginInfo.setOperateType(operatorType);
            operatorPluginInfo.setLock(isLock);
        }
    }
}
