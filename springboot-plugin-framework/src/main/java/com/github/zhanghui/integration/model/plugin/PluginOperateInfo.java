package com.github.zhanghui.integration.model.plugin;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:
 * 记录插件被操作的信息和状态
 *
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/4 19:23
 **/
public class PluginOperateInfo {

    private OperateType operateType;

    private AtomicBoolean isLock = new AtomicBoolean(false);

    public OperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(OperateType operateType) {
        if(operateType != null && !isLock.get()) {
            this.operateType = operateType;
        }
    }

    public void setLock(boolean isLock) {
        this.isLock.set(isLock);
    }

    public enum OperateType{
        // 安装插件
        INSTALL,
        // 启动插件
        START;
    }
}
